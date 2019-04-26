package com.hankus.activitymonitoring;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Object;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.hankus.activitymonitoring.KNN.classify;
import static com.hankus.activitymonitoring.KNN.findKNearestNeighbors;

public class MonitorActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private String tag = "DEBUG - Monitor activity: ";
    private int NUM_SAMPLES = 70;

    private boolean continousMonitoring;
    private AccData accSamples;

    private ArrayList<Features> trainingData;

    // TextViews to display current sensor values
    private TextView mTextWalkProb;
    private TextView mTextStandupProb;
    private TextView mTextSitdownProb;
    private TextView mPredictedActivity;

    private TextView mTextDebug;

    private Button mStartMonitoringButton;
    private CheckBox mContinousMonitoringCheckbox;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        //Get permission to read from file
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        //init Textviews
        mTextDebug = (TextView) findViewById(R.id.debug);
        mTextWalkProb = (TextView) findViewById(R.id.walking_prob);
        mTextSitdownProb = (TextView) findViewById(R.id.sitting_down_prob);
        mTextStandupProb = (TextView) findViewById(R.id.standing_up_prob);
        mPredictedActivity = (TextView) findViewById(R.id.predicted_activity);

        //init Progressbar
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        //init Buttons
        mStartMonitoringButton = findViewById(R.id.start_activity_monitoring_button);
        mStartMonitoringButton.setOnClickListener(this);
        mContinousMonitoringCheckbox = findViewById(R.id.continous_monitoring_checkbox);
        mContinousMonitoringCheckbox.setOnClickListener(this);

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //init variables
        continousMonitoring = false;
        accSamples = new AccData();
        trainingData = new ArrayList<Features>();

        //load training data
        loadTrainingData();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //read sensor data and store to file
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        mProgressBar.setProgress(accSamples.getSize() * 100 /NUM_SAMPLES);

        if(accSamples.getSize() < NUM_SAMPLES)
            accSamples.addSample(x,y,z);
        else
            stopMonitoring();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.start_activity_monitoring_button:
                debug("Start monitoring");
                startMonitoring();
                break;
            case R.id.continous_monitoring_checkbox:
                debug("Enable/disable continous monitoring");
                continousMonitoring = !continousMonitoring;
                break;
            default:
                //do nothing

        }

    }

    private void startMonitoring()
    {
        debug("Start monitoring");
        accSamples.clear();
        mStartMonitoringButton.setEnabled(false);
        mStartMonitoringButton.setBackgroundResource(R.drawable.rounded_button_grey);
        mSensorManager.registerListener(this, mSensorAcc,
                SensorManager.SENSOR_DELAY_GAME);

    }

    private void stopMonitoring()
    {
        debug("Stop monitoring");
        mStartMonitoringButton.setEnabled(true);
        mStartMonitoringButton.setBackgroundResource(R.drawable.rounded_button_green);
        mSensorManager.unregisterListener(this, mSensorAcc);
        predictActivity();
        if(continousMonitoring)
            startMonitoring();
    }


    private void debug(String msg)
    {
        Log.wtf(tag, msg);
        mTextDebug.setText(msg);
    }

    private void predictActivity()
    {
        accSamples.extractFeatures();

        ArrayList<Features> neighbors = findKNearestNeighbors(trainingData, accSamples.features, 3);

        if(neighbors == null)
        {
            debug("Not enough trainingsdata is available");
        }else {

            debug("Neighbor1: " + neighbors.get(0).activity + "\nNeighbor2: " + neighbors.get(1).activity
                    + "\nNeighbor3: " + neighbors.get(2).activity);

            Pair<String, HashMap<String, Double>> classification = classify(neighbors);
            String activity_pred = classification.first;

            mPredictedActivity.setText(getResources().getString(R.string.predicted_activity, activity_pred));

            HashMap<String, Double> probabilities = classification.second;
            Double walking = probabilities.containsKey("walking") ? probabilities.get("walking") : 0.0;
            Double stand = probabilities.containsKey("standing_up") ? probabilities.get("standing_up") : 0.0;
            Double sit = probabilities.containsKey("sitting_down") ? probabilities.get("sitting_down") : 0.0;

            mTextWalkProb.setText(getResources().getString(R.string.walking_prob, walking));
            mTextStandupProb.setText(getResources().getString(R.string.standing_up_prob, stand));
            mTextSitdownProb.setText(getResources().getString(R.string.sitting_down_prob, sit));
        }

    }

    private void loadTrainingData()
    {
        debug("Load trainingsdata");
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File (root, getResources().getString(R.string.file_name));
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));

            String line = reader.readLine();
            while (line != null) {
                String features = new String(line);
                String[] featuresSplit = features.split(";");

                double min = Double.parseDouble(featuresSplit[0]);
                double max = Double.parseDouble(featuresSplit[1]);
                double index_max = Double.parseDouble(featuresSplit[2]);
                double mean = Double.parseDouble(featuresSplit[3]);
                String activity = featuresSplit[4];

                trainingData.add(new Features(mean, min, max, index_max, activity));

                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        debug("Number of trainings samples: " +  String.valueOf(trainingData.size()));
        for (Features f : trainingData
             ) {
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
