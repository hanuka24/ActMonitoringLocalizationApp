package com.hankus.activitymonitoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LocalizationActivity extends AppCompatActivity {

    private AccData accSamples;
    private MapView mapView;
    private String tag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        accSamples = new AccData();
        tag = "Localization Activity";

        //init Background map
        mapView = findViewById(R.id.map_view);

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
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.wtf(tag, "Touch down");
                // touch down code
                mapView.addCircle();
                break;

            case MotionEvent.ACTION_MOVE:
                mapView.addCircle();
                Log.wtf(tag, "Touch move");
                // touch move code
                break;

            case MotionEvent.ACTION_UP:
                mapView.clear();
                Log.wtf(tag, "Touch up");
                // touch up code
                break;
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_bot, R.anim.slide_in_top);
    }
}
