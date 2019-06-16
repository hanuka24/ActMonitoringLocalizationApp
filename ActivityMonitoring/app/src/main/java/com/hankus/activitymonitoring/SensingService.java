package com.hankus.activitymonitoring;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Arrays.sort;

public class SensingService extends Service implements SensorEventListener {

    private String mTag = "Sensing Service";
    private String mState;

    private AccData mAccSamples;

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorMag;

    private boolean mWasWalking;

    private double mVariance;
    private double mStandardDeviation;
    private double mAutocorrelationMax;

    private float mIdleThreshold;
    private float mWalkingThreshold;
    private float mOrientationOffset;

    private long mStartTime;
    private long mWalkingTime;

    private int mSteptime = 750; //ms

    private float[] mRotation;
    private float[] mI;
    private float[] mags;
    private float[] mAccels;
    private float[] mOrientationValues = {0f, 0f, 0f};

    private ArrayList<Float> mOrientations;

    Callbacks mActivity;
    private final IBinder mBinder = new LocalBinder();


    public void startMonitoring()
    {
        mAccSamples.clear();
        mSensorManager.registerListener(this, mSensorAcc,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void stopMonitoring()
    {
        mSensorManager.unregisterListener(this, mSensorAcc);


        //check for movement
        mAccSamples.extractFeatures();
        double mean = calculateXYZMean(mAccSamples.mFeatures.mMeanX, mAccSamples.mFeatures.mMeanY, mAccSamples.mFeatures.mMeanZ);
        calculateStandardDeviation(mean);
        calculateAutocorrelation(mean);
        checkMovement();

        mVariance = 0.0;
        mStandardDeviation = 0.0;

        if(mState.equals("IDLE") && mWasWalking)
        {
            mWasWalking = false;
            Log.wtf(mTag, "Walked for " + mWalkingTime + "ms");
            mActivity.makeStep((int)(mWalkingTime/ mSteptime), getOrientationMedian() + mOrientationOffset);
        }
        else
            startMonitoring();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Do what you need in onStartCommand when service has been started

        mAccSamples = new AccData();
        mOrientations = new ArrayList<>();

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Log.wtf(mTag, "onStartCommand");

        mRotation = new float[16];
        mI = new float[16];
        mags = new float[3];
        mAccels = new float[3];

        mStartTime = 0;
        mState = "IDLE";


        mVariance = 0.0;
        mStandardDeviation = 0.0;
        mAutocorrelationMax = 0.0;
        mIdleThreshold = 0.8f;
        mWalkingThreshold = 0.7f;
        mState = "Debug";
        mWasWalking = false;

        mOrientationOffset = 1.95f;

        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_GAME);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
       Log.wtf(mTag, "onDestroy");
       mSensorManager.unregisterListener(this, mSensorAcc);
       mSensorManager.unregisterListener(this, mSensorMag);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public SensingService getServiceInstance(){
            return SensingService.this;
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.mActivity = (Callbacks)activity;
    }

    //callbacks interface for communication with service clients!
    public interface Callbacks{
        public void makeStep(int steps, float direction);
        public void updateActivity(String activity);
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

                mAccels = event.values.clone();

                if(mAccSamples.getSize() < 40)//mAccSamples.getNumberOfSamples())
                    mAccSamples.addSample(x,y,z, 0);
                else
                    stopMonitoring();
                break;
        }

        if (mags!=null && mAccels !=null) {
            boolean success =  SensorManager.getRotationMatrix(mRotation, mI, mAccels, mags);
            if(success)
            {
                SensorManager.getOrientation(mRotation, mOrientationValues);
                mOrientations.add(-mOrientationValues[0]);
                mags = null;
                mAccels = null;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private double calculateXYZMean(double x_mean, double y_mean, double z_mean)
    {
        return Math.sqrt(Math.pow(x_mean, 2) + Math.pow(y_mean, 2) + Math.pow(z_mean, 2));
    }

    private void checkMovement()
    {
        // System.out.println("Standard Deviation " + mStandardDeviation);
        // System.out.println("Autocorrelation " + mAutocorrelationMax);
        if(mStandardDeviation < mIdleThreshold)
        {
            mActivity.updateActivity("IDLE");
            if(!mState.equals("IDLE"))
            {
                mState = "IDLE";
                mWalkingTime = System.currentTimeMillis() - mStartTime;
                mStartTime = 0;
                mWasWalking = true;
            }
            else
             mOrientations.clear();
        }
        else if(mAutocorrelationMax > mWalkingThreshold && !mState.equals("WALKING"))
        {
            mActivity.updateActivity("WALKING");
            mState = "WALKING";
            mStartTime = System.currentTimeMillis();
        }
    }


    private void calculateStandardDeviation(double mean)
    {
        ArrayList<AccDataSample> accData = mAccSamples.mAccData;

        double sum = 0.0;
        for(int i = 0; i < accData.size() - 1; i++)
        {
            sum += Math.pow(accData.get(i).getSum() - mean, 2);
        }
        mVariance = sum / accData.size();
        mStandardDeviation = Math.sqrt(mVariance);
    }

    private void calculateAutocorrelation(double mean)
    {
        ArrayList<AccDataSample> accData = mAccSamples.mAccData;

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
            autocorrelation[h] = m[h] / mVariance;
        }

        sort(autocorrelation);
        mAutocorrelationMax = autocorrelation[accData.size() - 1];
    }

    /**
     * Gets the median of the orientation measurements to ignore outliers.
     * The mean was tried but it achieved worse results as orientation measurements
     * while turning also had an effect on them.
     *
     * @return
     */
//https://stackoverflow.com/questions/41117879/problems-finding-median-of-arraylist/41118061
    public float getOrientationMedian(){

        if(mOrientations.isEmpty())
            return 0.0f;

        Collections.sort(mOrientations);

        float middle = 0f;
        if (mOrientations.size()%2 == 1) {
            middle = (mOrientations.get(mOrientations.size()/2) + mOrientations.get(mOrientations.size()/2 - 1))/2;
        } else {
            middle = mOrientations.get(mOrientations.size() / 2);
        }

        return middle;
    }

}
