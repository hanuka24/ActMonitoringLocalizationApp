package com.hankus.activitymonitoring;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static java.util.Arrays.sort;

public class LocalizationActivity extends AppCompatActivity implements View.OnClickListener, SensingService.Callbacks {

    private MapView mMapView;
    private String mTag;

    private ImageView mCompassView;
    private int mCompassImageOffset = 90;

    private ParticleSet mParticles;
    private float mOrientation = 0.f;
    private int mSteps;

    private TextView mOrientationText;
    private TextView mTextDebug;
    private TextView mStepCountText;

    Intent mServiceIntent;
    SensingService mSensingService;
    boolean mServiceBound = false;

    boolean mMoveSinglePoint;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        mTag = "Localization Activity";

        //init Background map
        mMapView = findViewById(R.id.map_view);

        //init Buttons
        findViewById(R.id.add_particle_button).setOnClickListener(this);
        findViewById(R.id.init_particles_button).setOnClickListener(this);
        findViewById(R.id.show_walls_button).setOnClickListener(this);

        //init TextViews
        mOrientationText = (TextView) findViewById(R.id.orientation);
        mTextDebug = (TextView) findViewById(R.id.localization_debug);
        mStepCountText = (TextView) findViewById(R.id.step_count);
        mCompassView = (ImageView) findViewById(R.id.Compass);

        //init Particles
        mParticles = new ParticleSet();
        mMapView.setParticleSet(mParticles);

        mMoveSinglePoint = false;

        //Start sensing
        mServiceIntent = new Intent(LocalizationActivity.this, SensingService.class);
        startService(mServiceIntent); //Starting the service
        bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.wtf(mTag, "onServiceConnected called");
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            SensingService.LocalBinder binder = (SensingService.LocalBinder) service;
            mSensingService = binder.getServiceInstance(); //Get instance of your service!
            mSensingService.registerClient(LocalizationActivity.this); //Activity register in the service as client for callabcks!
            mServiceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.wtf(mTag, "onServiceDisconnected called");
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
        if(mServiceBound)
            unbindService(mConnection);
        mServiceBound = false;
        stopService(mServiceIntent);
    }


    @Override
    public void makeStep(int steps, float direction)
    {
       Log.wtf(mTag, "Movement detected, move particles");
       mStepCountText.setText(getResources().getString(R.string.step_count, steps));
       mOrientationText.setText(getResources().getString(R.string.orientation, direction * 180 / Math.PI));
       mOrientation = direction;
       mSteps = steps;
       if(mMoveSinglePoint)
           moveSinglePoint();
       else
       {
           new ComputeStep().execute(); //apply particle filter in AsyncTask
           mCompassView.setRotation(360 - (((mOrientation * 180f / (float) Math.PI)) + 360) % 360 - mCompassImageOffset);
       }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_particle_button:
                mParticles.clear();
                Particle start = mParticles.createRandomParticle();
                mParticles.mPosY = start.getY();
                mParticles.mPosX = start.getX();
                mMoveSinglePoint = true;
                mMapView.update();
                break;
            case R.id.init_particles_button:
                mParticles.initParticles();
                mMapView.update();
                mMoveSinglePoint = false;
                break;
            case R.id.show_walls_button:
                mMapView.drawWalls();
                mMapView.update();
                break;
            default:
                    break;
        }
    }

    public void updateActivity(String activity)
    {
        mTextDebug.setText(activity);
        //if(mActivity == "IDLE")
        //    mStepCountText.setText(getResources().getString(R.string.step_count, 0.0f));
    }

    private void moveSinglePoint()
    {
        mParticles.mPosX = mParticles.mPosX + (int)(mSteps * mParticles.mParticleFilter.mStepwidth *mParticles.mScaleMeterX * Math.sin((double) mOrientation));
        mParticles.mPosY = mParticles.mPosY + (int)(mSteps * mParticles.mParticleFilter.mStepwidth *mParticles.mScaleMeterY * Math.cos((double) mOrientation));

        Log.wtf(mTag, "Position X: " + mParticles.mPosX);
        Log.wtf(mTag, "Position Y: " + mParticles.mPosY);

        mMapView.update();
        mSensingService.startMonitoring();
    }

    private class ComputeStep extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {}

        @Override
        protected Void doInBackground(Void ...params) {

            Log.wtf(mTag, "Start calculation");
            mParticles.doParticleFilter(mSteps, mOrientation);
            return null;
        }


        @Override
        protected void onProgressUpdate(Void ...params){

        }

        @Override
        protected void onPostExecute(Void params) {
            Log.wtf(mTag, "Performed calculation");
            mMapView.update();
            mSensingService.startMonitoring();
        }

    }

}
