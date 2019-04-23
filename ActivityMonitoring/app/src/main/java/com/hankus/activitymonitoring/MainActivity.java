package com.hankus.activitymonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String tag = "DEBUG - Main Activity:";

    private TextView mTextDebug;

    private Button msstartMonitoring;
    private Button msstartTraining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextDebug = (TextView) findViewById(R.id.debug);

        //init Buttons
        msstartMonitoring = findViewById(R.id.start_monitoring_button);
        msstartTraining = findViewById(R.id.start_training_button);

        msstartMonitoring.setOnClickListener(this);
        msstartTraining.setOnClickListener(this);

        debug("onCreate complete");
    }

    @Override
    protected void onStart() {
        super.onStart();
        debug("App started");
    }


    @Override
    protected void onStop() {
        super.onStop();
        debug("App stopped");
        //TODO: save state
    }


    //Button Click Handler
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start_monitoring_button:
                Intent intent1 = new Intent(this, MonitorActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.start_training_button:
                Intent intent2 = new Intent(this, TrainActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            default:
                //do nothing

        }
    }

    public void debug(String msg)
    {
        Log.wtf(tag, msg);
        mTextDebug.setText(msg);
    }
}
