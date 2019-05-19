package com.hankus.activitymonitoring;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static java.util.Arrays.sort;

public class LocalizationActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private AccData accSamples;
    private MapView mapView;
    private String tag;

    private Button mAddParticleButton;
    private Button mDeleteParticleButton;
    private Button mClearParticleButton;
    private Button mSetOrientationButton;

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorMag;

    private float[] Rotation;
    private float[] I;
    private float[] mags;
    private float[] accels;
    private float[] orientationValues = {0f, 0f, 0f};

    private double variance;
    private double standard_deviation;
    private double autocorrelation_max;
    private float idle_threshold;
    private float walking_threshold;
    private String state;

    int mCount;
    int mCurrentX;
    int mCurrentY;
    int mStepTime;

    float mOrientationOffset;
    float mOrientation;

    private TextView mOrientationText;
    private TextView mTextDebug;
    private TextView mStepCount;

    long start_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        accSamples = new AccData();
        tag = "Localization Activity";

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        //init Background map
        mapView = findViewById(R.id.map_view);

        //init Buttons
        mAddParticleButton = findViewById(R.id.add_particle_button);
        mAddParticleButton.setOnClickListener(this);
        mDeleteParticleButton = findViewById(R.id.delete_particle_button);
        mDeleteParticleButton.setOnClickListener(this);
        mClearParticleButton = findViewById(R.id.clear_particle_button);
        mClearParticleButton.setOnClickListener(this);
        mSetOrientationButton = findViewById(R.id.set_orientation_button);
        mSetOrientationButton.setOnClickListener(this);

        Rotation = new float[16];
        I = new float[16];
        mags = null;
        accels= null;
        mCount = 0;
        mCurrentX = 0;
        mCurrentY = 0;
        start_time = 0;
        mStepTime = 750; // in milliseconds
        variance = 0.0;
        standard_deviation = 0.0;
        autocorrelation_max = 0.0;
        idle_threshold = 0.8f;
        walking_threshold = 0.7f;
        state = "Debug";


        //init TextViews
        mOrientationText = (TextView) findViewById(R.id.orientation);
        mTextDebug = (TextView) findViewById(R.id.localization_debug);
        mStepCount = (TextView) findViewById(R.id.step_count);

        mOrientationOffset = 0;
        mOrientation = 0;

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mSensorAcc,
                SensorManager.SENSOR_DELAY_GAME);
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
        System.out.println("Standard Deviation " + standard_deviation);
        System.out.println("Autocorrelation " + autocorrelation_max);
        if(standard_deviation < idle_threshold)
        {
            if(!state.equals("IDLE"))
            {
                state = "IDLE";
                long time = System.currentTimeMillis() - start_time;
                mStepCount.setText(getResources().getString(R.string.step_count, (int) time / mStepTime));
                start_time = 0;
            }
        }
        else if(autocorrelation_max > walking_threshold && !state.equals("WALKING"))
        {
            state = "WALKING";
            start_time = System.currentTimeMillis();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_GAME);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_particle_button:
                move();
                mapView.addParticle(new Particle(mCurrentX, mCurrentY, 0, 2));
                mapView.update();
                break;
            case R.id.delete_particle_button:
                mapView.removeParticle();
                mapView.update();
                break;
            case R.id.clear_particle_button:
                mapView.clear();
                break;
            case R.id.set_orientation_button:
                mOrientationOffset = orientationValues[0];
                break;
            default:
                    break;
        }
    }

    void move()
    {
        int x = mCurrentX + (int)(5 * Math.sin((double) mOrientation));
        int y = mCurrentY + (int)(5 * Math.cos((double) mOrientation));

        if(x < 0)
            x = 0;
        if(x > mapView.getMapWidth())
            x = mapView.getMapWidth() - 1;
        if(y < 0)
            y = 0;
        if(y > mapView.getMapHeight())
            y = mapView.getMapHeight() - 1;

        mCurrentX = x;
        mCurrentY = y;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
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
                break;
        }

        if (mags!=null && accels!=null) {
            SensorManager.getRotationMatrix(Rotation, I, accels, mags);
            SensorManager.getOrientation(Rotation, orientationValues);


            final float rad2deg = (float)(180.0f/Math.PI);

            float incl = SensorManager.getInclination(I);
            if (mCount++ > 50) {
                mOrientation = (float)((orientationValues[0] - mOrientationOffset));
                mCount = 0;
                Log.wtf("Compass", "yaw: " + (int)((orientationValues[0] - mOrientationOffset) *rad2deg) +
                        "  pitch: " + (int)(orientationValues[1]*rad2deg) +
                        "  roll: " + (int)(orientationValues[2]*rad2deg) +
                        "  incl: " + (int)(incl*rad2deg)
                );
            }

            mOrientationText.setText(getResources().getString(R.string.orientation,mOrientation * rad2deg));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
