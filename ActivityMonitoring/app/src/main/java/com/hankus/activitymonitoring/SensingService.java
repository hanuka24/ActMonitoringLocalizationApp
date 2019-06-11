package com.hankus.activitymonitoring;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.sort;

public class SensingService extends Service implements SensorEventListener {

    private String tag = "Sensing Service";

    private AccData accSamples;

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorMag;

    boolean mWasWalking;
    long mWalkingTime;

    private double variance;
    private double standard_deviation;
    private double autocorrelation_max;
    private float idle_threshold;
    private float walking_threshold;
    private String state;
    long start_time;

    int mCount;
    int mCurrentX;
    int mCurrentY;
    int mStepTime;


    private float[] Rotation;
    private float[] I;
    private float[] mags;
    private float[] accels;
    private float[] orientationValues = {0f, 0f, 0f};



    float mOrientationOffset;
    float mOrientation;

    public static final float TWENTY_FIVE_DEGREE_IN_RADIAN = 0.436332313f;
    public static final float ONE_FIFTY_FIVE_DEGREE_IN_RADIAN = 2.7052603f;


    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;
    Callbacks activity;
    private final IBinder mBinder = new LocalBinder();

    private float[] applyLowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + (float)0.5 * (input[i] - output[i]);
        }
        return output;
    }

    public void startMonitoring()
    {
        accSamples.clear();
        mSensorManager.registerListener(this, mSensorAcc,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void stopMonitoring()
    {
        mSensorManager.unregisterListener(this, mSensorAcc);
        accSamples.extractFeatures();

        double mean = calculateXYZMean(accSamples.features.x_mean, accSamples.features.y_mean, accSamples.features.z_mean);
        calculateStandardDeviation(mean);
        calculateAutocorrelation(mean);
        checkMovement();

        variance = 0.0;
        standard_deviation = 0.0;

        if(state.equals("IDLE") && mWasWalking)
        {
            mWasWalking = false;
            Log.wtf(tag, "Walked for " + mWalkingTime + "ms");
            activity.makeStep((int)mWalkingTime/mStepTime + 1, mOrientation + mOrientationOffset);
        }
        else
            startMonitoring();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Do what you need in onStartCommand when service has been started

        accSamples = new AccData();

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Log.wtf(tag, "onStartCommand");

        Rotation = new float[16];
        I = new float[16];
        mags = new float[3];
        accels = new float[3];
        mCount = 0;
        mCurrentX = 100;
        mCurrentY = 200;
        start_time = 0;
        mStepTime = 750; // in milliseconds
        variance = 0.0;
        standard_deviation = 0.0;
        autocorrelation_max = 0.0;
        idle_threshold = 0.8f;
        walking_threshold = 0.7f;
        state = "Debug";
        mWasWalking = false;

        mOrientationOffset = 1.95f;
        mOrientation = 0.1f;

        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_UI);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                //read sensor data and store to file
                double x = event.values[0];
                double y = event.values[1];
                double z = event.values[2];


                accels = event.values.clone();

                long timestamp = Calendar.getInstance().getTimeInMillis();

                if(accSamples.getSize() < 40)//accSamples.getNumberOfSamples())
                    accSamples.addSample(x,y,z, timestamp);
                else
                    stopMonitoring();
                break;
        }

        if (mags!=null && accels!=null) {
            boolean success =  SensorManager.getRotationMatrix(Rotation, I, accels, mags);
            if(success)
            {
                SensorManager.getOrientation(Rotation, orientationValues);
                mOrientation = (-orientationValues[0] * 0.9f + mOrientation * 0.1f);
                mags = null;
                accels = null;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public SensingService getServiceInstance(){
            return SensingService.this;
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    //callbacks interface for communication with service clients!
    public interface Callbacks{
        public void makeStep(int steps, float direction);
    }


    private double calculateXYZMean(double x_mean, double y_mean, double z_mean)
    {
        return Math.sqrt(Math.pow(x_mean, 2) + Math.pow(y_mean, 2) + Math.pow(z_mean, 2));
    }

    private void checkMovement()
    {
        // System.out.println("Standard Deviation " + standard_deviation);
        // System.out.println("Autocorrelation " + autocorrelation_max);
        if(standard_deviation < idle_threshold)
        {
            if(!state.equals("IDLE"))
            {
                state = "IDLE";
                mWalkingTime = System.currentTimeMillis() - start_time;
                start_time = 0;
                mWasWalking = true;
            }
        }
        else if(autocorrelation_max > walking_threshold && !state.equals("WALKING"))
        {
            state = "WALKING";
            start_time = System.currentTimeMillis();
        }
    }


    private void calculateStandardDeviation(double mean)
    {
        ArrayList<AccDataSample> accData = accSamples.accData;

        double sum = 0.0;
        for(int i = 0; i < accData.size() - 1; i++)
        {
            sum += Math.pow(accData.get(i).getSum() - mean, 2);
        }
        variance = sum / accData.size();
        standard_deviation = Math.sqrt(variance);
    }

    private void calculateAutocorrelation(double mean)
    {
        ArrayList<AccDataSample> accData = accSamples.accData;

        double [] temp = new double[accData.size()];
        double [] m = new double[accData.size()];
        double [] autocorrelation = new double[accData.size()];

        for(int h = 0; h < accData.size(); h++)
        {
            for(int t = 1; t < accData.size() - h; t++)
            {
                temp[h] = temp[h] + Math.pow(accData.get(h + t).getSum() - mean, 2);
                m[h] = temp[h] / accData.size();
            }
            autocorrelation[h] = m[h] / variance;
        }

        sort(autocorrelation);
        autocorrelation_max = autocorrelation[accData.size() - 1];
    }

}
