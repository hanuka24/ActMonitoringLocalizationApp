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

import java.util.Random;

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

    int mCount;

    int mCurrentX;
    int mCurrentY;

    float mOrientationOffset;
    float mOrientation;

    private TextView mOrientationText;

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


        //init TextViews
        mOrientationText = (TextView) findViewById(R.id.orientation);

        mOrientationOffset = 0;
        mOrientation = 0;

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
