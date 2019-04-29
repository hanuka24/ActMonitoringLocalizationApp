package com.hankus.activitymonitoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LocalizationActivity extends AppCompatActivity {

    private AccData accSamples;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        accSamples = new AccData();

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
