package com.hankus.activitymonitoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class LocalizationActivity extends AppCompatActivity implements View.OnClickListener {

    private AccData accSamples;
    private MapView mapView;
    private String tag;

    private Button mAddParticleButton;
    private Button mDeleteParticleButton;
    private Button mClearParticleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        accSamples = new AccData();
        tag = "Localization Activity";

        //init Background map
        mapView = findViewById(R.id.map_view);

        //init Buttons
        mAddParticleButton = findViewById(R.id.add_particle_button);
        mAddParticleButton.setOnClickListener(this);
        mDeleteParticleButton = findViewById(R.id.delete_particle_button);
        mDeleteParticleButton.setOnClickListener(this);
        mClearParticleButton = findViewById(R.id.clear_particle_button);
        mClearParticleButton.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_particle_button:
                mapView.addParticle(new Particle((int)(mapView.getMapWidth() * Math.random()),
                        (int)(mapView.getMapHeight() * Math.random()), 0, (float)Math.random()));
                mapView.update();
                break;
            case R.id.delete_particle_button:
                mapView.removeParticle();
                mapView.update();
                break;
            case R.id.clear_particle_button:
                mapView.clear();
                break;
        }
    }
}
