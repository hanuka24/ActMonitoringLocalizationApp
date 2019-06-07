package com.hankus.activitymonitoring;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.sort;

public class LocalizationActivity extends AppCompatActivity implements View.OnClickListener, SensingService.Callbacks {

    private MapView mapView;
    private String tag;

    private Button mAddParticleButton;
    private Button mInitParticleButton;
    private Button mClearParticleButton;
    private Button mSetOrientationButton;


    private ParticleSet mParticles;


    Particle mMovingPoint;

    float mOrientation;
    int mSteps;

    private TextView mOrientationText;
    private TextView mTextDebug;
    private TextView mStepCount;

    Intent serviceIntent;
    SensingService sensingService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        tag = "Localization Activity";

        Log.wtf(tag, "onCreate");

        //init Background map
        mapView = findViewById(R.id.map_view);

        //init Buttons
        mAddParticleButton = findViewById(R.id.add_particle_button);
        mAddParticleButton.setOnClickListener(this);
        mInitParticleButton = findViewById(R.id.init_particles_button);
        mInitParticleButton.setOnClickListener(this);
        mClearParticleButton = findViewById(R.id.clear_particle_button);
        mClearParticleButton.setOnClickListener(this);
        mSetOrientationButton = findViewById(R.id.set_orientation_button);
        mSetOrientationButton.setOnClickListener(this);


        //init TextViews
        mOrientationText = (TextView) findViewById(R.id.orientation);
        mTextDebug = (TextView) findViewById(R.id.localization_debug);
        mStepCount = (TextView) findViewById(R.id.step_count);

        mParticles = new ParticleSet();
        mapView.setParticleSet(mParticles);
        //Start sensing

        serviceIntent = new Intent(LocalizationActivity.this, SensingService.class);

        startService(serviceIntent); //Starting the service
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.wtf(tag, "onServiceConnected called");
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            SensingService.LocalBinder binder = (SensingService.LocalBinder) service;
            sensingService = binder.getServiceInstance(); //Get instance of your service!
            sensingService.registerClient(LocalizationActivity.this); //Activity register in the service as client for callabcks!

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.wtf(tag, "onServiceDisconnected called");
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        Log.wtf(tag, "onResume");

        mMovingPoint = new Particle(mapView.getMapWidth()/2, mapView.getMapHeight()/2, 0,5);

    }

    @Override
    public void makeStep(int steps, float direction)
    {
       Log.wtf(tag, "Movement detected, move particles");
       mOrientationText.setText(getResources().getString(R.string.orientation, direction * 180 / Math.PI));
       mOrientation = direction;
       mSteps = steps;
       new ComputeStep().execute();

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
                mParticles.addParticle(new Particle(mapView.getMapWidth()/2, mapView.getMapHeight()/2, 0, 2));
                mapView.update();

                break;
            case R.id.init_particles_button:
                mParticles.initParticles();
                mapView.update();
                break;
            case R.id.clear_particle_button:
                mapView.drawWalls();
                mapView.update();
                break;
            default:
                    break;
        }
    }

    private class ComputeStep extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {}

        @Override
        protected Void doInBackground(Void ...params) {
            mParticles.moveParticles(5  * mSteps, mOrientation);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void ...params){

        }

        @Override
        protected void onPostExecute(Void params) {
            Log.wtf(tag, "Performed calculation");
            mapView.update();
        }

    }



}
