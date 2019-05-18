package com.hankus.activitymonitoring;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import static java.util.Arrays.sort;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener {

    private AccData accSamples;
    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private double variance;
    private double standard_deviation;
    private double autocorrelation_max;
    private float idle_threshold;
    private float walking_threshold;
    private String state;

    private TextView mTextDebug;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        accSamples = new AccData();

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mSensorAcc,
                SensorManager.SENSOR_DELAY_GAME);

        mTextDebug = (TextView) findViewById(R.id.localization_debug);

        variance = 0.0;
        standard_deviation = 0.0;
        autocorrelation_max = 0.0;
        idle_threshold = 0.03f;
        walking_threshold = 0.7f;
        state = "Debug";

    }

    private void startMonitoring()
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
        mTextDebug.setText(state);

        startMonitoring();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //read sensor data and store to file
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        long timestamp = Calendar.getInstance().getTimeInMillis();

        if(accSamples.getSize() < accSamples.getNumberOfSamples())
            accSamples.addSample(x,y,z, timestamp);
        else
        {
            stopMonitoring();
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

    private double calculateXYZMean(double x_mean, double y_mean, double z_mean)
    {
        return Math.sqrt(Math.pow(x_mean, 2) + Math.pow(y_mean, 2) + Math.pow(z_mean, 2));
    }

    private void checkMovement()
    {
        System.out.println(autocorrelation_max);
        if(standard_deviation < idle_threshold)
        {
            state = "IDLE";
        }
        else if(autocorrelation_max > walking_threshold)
        {
            state = "WALKING";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void initialBelief()
    {

    }

    public void sense()
    {

    }

    public void resampling()
    {

    }

    public void move()
    {

    }

    public void detectWall()
    {

    }

    public void updateWeights()
    {

    }

    public void killParticles()
    {

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_bot, R.anim.slide_in_top);
    }
}
