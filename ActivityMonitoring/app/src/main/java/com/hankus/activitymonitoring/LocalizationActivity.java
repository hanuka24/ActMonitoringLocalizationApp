package com.hankus.activitymonitoring;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Connection;

import static java.util.Arrays.sort;

public class LocalizationActivity extends AppCompatActivity implements View.OnClickListener, SensingService.Callbacks {

    private MapView mapView;
    private String tag;

    private ParticleSet mParticles;
    float mOrientation = 0.f;
    int mSteps;

    private TextView mOrientationText;
    private TextView mTextDebug;
    private TextView mStepCountText;

    Intent serviceIntent;
    SensingService sensingService;

    boolean mMoveSinglePoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        tag = "Localization Activity";

        //init Background map
        mapView = findViewById(R.id.map_view);

        //init Buttons
        findViewById(R.id.add_particle_button).setOnClickListener(this);
        findViewById(R.id.init_particles_button).setOnClickListener(this);
        findViewById(R.id.show_walls_button).setOnClickListener(this);
        findViewById(R.id.set_orientation_button).setOnClickListener(this);

        //init TextViews
        mOrientationText = (TextView) findViewById(R.id.orientation);
        mTextDebug = (TextView) findViewById(R.id.localization_debug);
        mStepCountText = (TextView) findViewById(R.id.step_count);

        //init Particles
        mParticles = new ParticleSet();
        mapView.setParticleSet(mParticles);

        mMoveSinglePoint = false;

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
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_bot, R.anim.slide_in_top);

    }

    @Override
    public void onStop(){
        super.onStop();
        unbindService(mConnection);
        stopService(serviceIntent);
    }


    @Override
    public void makeStep(int steps, float direction)
    {
       Log.wtf(tag, "Movement detected, move particles");
       mStepCountText.setText(getResources().getString(R.string.step_count, steps));
       mOrientationText.setText(getResources().getString(R.string.orientation, direction * 180 / Math.PI));
       mOrientation = direction;
       mSteps = steps;
       if(mMoveSinglePoint)
           moveSinglePoint();
       else
        new ComputeStep().execute(); //apply particle filter in AsyncTask
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_particle_button:
                mParticles.clear();
                Particle start = mParticles.createRandomParticle();
                mParticles.posY = start.getY();
                mParticles.posX = start.getX();
                mMoveSinglePoint = true;
                mapView.update();
                break;
            case R.id.init_particles_button:
                mParticles.initParticles();
                mapView.update();
                mMoveSinglePoint = false;
                break;
            case R.id.show_walls_button:
                mapView.drawWalls();
                mapView.update();
                break;
            default:
                    break;
        }
    }

    public void updateActivity(String activity)
    {
        mTextDebug.setText(activity);
        if(activity == "IDLE")
            mStepCountText.setText(getResources().getString(R.string.step_count, 0));
    }

    private void moveSinglePoint()
    {
        mParticles.posX = mParticles.posX  + (int)(mSteps * mParticles.mParticleFilter.STEPWIDTH*mParticles.mScaleMeterX * Math.sin((double) mOrientation));
        mParticles.posY = mParticles.posY  + (int)(mSteps * mParticles.mParticleFilter.STEPWIDTH*mParticles.mScaleMeterY * Math.cos((double) mOrientation));

        Log.wtf(tag, "Position X: " + mParticles.posX);
        Log.wtf(tag, "Position Y: " + mParticles.posY);

        mapView.update();
        sensingService.startMonitoring();
    }

    private class ComputeStep extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {}

        @Override
        protected Void doInBackground(Void ...params) {

            Log.wtf(tag, "Start calculation");
            mParticles.doParticleFilter(mSteps, mOrientation);
            return null;
        }


        @Override
        protected void onProgressUpdate(Void ...params){

        }

        @Override
        protected void onPostExecute(Void params) {
            Log.wtf(tag, "Performed calculation");
            mapView.update();
            sensingService.startMonitoring();
        }

    }



}
