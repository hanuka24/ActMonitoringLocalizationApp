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
import android.view.View;
import android.widget.Button;
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
import java.util.List;
import java.util.Vector;

import static com.hankus.activitymonitoring.KNN.classify;
import static com.hankus.activitymonitoring.KNN.findKNearestNeighbors;

public class MonitorActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private String tag = "DEBUG - Monitor activity: ";
    private int NUM_SAMPLES = 70;

    private int sampleCount;

    private AccData accSamples;

    private ArrayList<Features> trainingData;

    // TextViews to display current sensor values
    private TextView mTextWalkProb;
    private TextView mTextStandupProb;
    private TextView mTextSitdownProb;
    private TextView mPredictedActivity;

    private TextView mTextDebug;

    private Button mstartMonitoringButton;


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

        //init Buttons
        mstartMonitoringButton = findViewById(R.id.start_activity_monitoring_button);
        mstartMonitoringButton.setOnClickListener(this);

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //init variables
        sampleCount = 0;
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

            default:
                //do nothing

        }

    }

    private void startMonitoring()
    {
        debug("Start monitoring");
        accSamples.clear();
        mstartMonitoringButton.setEnabled(false);
        mstartMonitoringButton.setBackgroundResource(R.drawable.rounded_button_grey);
        mSensorManager.registerListener(this, mSensorAcc,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void stopMonitoring()
    {
        debug("Stop monitoring");
        mstartMonitoringButton.setEnabled(true);
        mstartMonitoringButton.setBackgroundResource(R.drawable.rounded_button_green);
        mSensorManager.unregisterListener(this, mSensorAcc);
        predictActivity();
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

            String activity_pred = classify(neighbors);

            mPredictedActivity.setText(getResources().getString(R.string.predicted_activity, activity_pred));
        }

    }

    private void loadTrainingData()
    {
        debug("Load trainingsdata");
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File (root, "Trainingsdata.txt");
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

}
