package com.hankus.activitymonitoring;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;

public class TrainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private String tag = "DEBUG - Train activity: ";
    private int NUM_SAMPLES = 70;

    private String currentActivity;

    private File currentFile;

    // TextViews to display current sensor values
    private TextView mTextSensorAccX;
    private TextView mTextSensorAccY;
    private TextView mTextSensorAccZ;

    private TextView mTextDebug;

    private Button mstartWalkButton;
    private Button mstartStandButton;
    private Button mstartSitButton;
    private Button mstartIdle;
    private Button mdeleteLatestButton;
    private Button mdeleteFile;

    private boolean sensorEnabled;

    private AccData accData;

    private FileOutputStream fOutStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        //Get permission to write to file
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //init Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //init TextViews
        mTextSensorAccX = (TextView) findViewById(R.id.label_acc_x);
        mTextSensorAccY = (TextView) findViewById(R.id.label_acc_y);
        mTextSensorAccZ = (TextView) findViewById(R.id.label_acc_z);
        mTextDebug = (TextView) findViewById(R.id.debug);

        //init Buttons
        mstartWalkButton = findViewById(R.id.walk_button);
        mstartStandButton = findViewById(R.id.stand_up_button);
        mstartSitButton = findViewById(R.id.sit_down_button);
        mstartIdle = findViewById(R.id.idle_button);
        mdeleteLatestButton = findViewById(R.id.delete_latest);
        mdeleteFile = findViewById(R.id.delete_file);

        mstartWalkButton.setOnClickListener(this);
        mstartStandButton.setOnClickListener(this);
        mstartSitButton.setOnClickListener(this);
        mstartIdle.setOnClickListener(this);
        mdeleteLatestButton.setOnClickListener(this);
        mdeleteFile.setOnClickListener(this);

        //init Variables
        fOutStream = null;
        currentActivity = "Unknown";
        sensorEnabled = false;

        // set current file
        getCurrentFile();

        accData = new AccData();

        if (mSensorAcc == null) {
            mTextDebug.setText("Required sensor not available!");
            //TODO: close App and notify user
            //should be done in mainactivity!
        }

        debug("onCreate complete");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //read sensor data and store to file
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        mTextSensorAccX.setText(getResources().getString(R.string.label_acc_x, x));
        mTextSensorAccY.setText(getResources().getString(R.string.label_acc_y, y));
        mTextSensorAccZ.setText(getResources().getString(R.string.label_acc_z, z));

        if(accData.getSize() < NUM_SAMPLES)
            accData.addSample(x,y,z);
        else
            stopMonitoring();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        debug("Activity started");
    }


    @Override
    protected void onStop() {
        super.onStop();
        debug("Activity stopped");
        //TODO: save state
    }

    private void startMonitoring() {
        if(!sensorEnabled) {
            debug("Start monitoring");
            accData.clear();
            setButtonText("begin");
            mSensorManager.registerListener(this, mSensorAcc,
                    SensorManager.SENSOR_DELAY_GAME);
            sensorEnabled = true;
        }
    }

    private void stopMonitoring()
    {
        debug("Stop monitoring");
        mSensorManager.unregisterListener(this, mSensorAcc);
        sensorEnabled = false;
        saveData();
        setButtonText("end");
    }

    private void setButtonText(String control) {
        int background = 0;
        if(control.equals("begin"))
        {
            background = R.drawable.rounded_button_grey;
        }
        else if(control.equals("end"))
        {
            background = R.drawable.rounded_button_blue;
        }
        switch (currentActivity) {
            case "walking":
                mstartWalkButton.setActivated(!sensorEnabled);
                mstartWalkButton.setBackgroundResource(background);
                break;
            case "standing_up":
                mstartStandButton.setActivated(!sensorEnabled);
                mstartStandButton.setBackgroundResource(background);
                break;
            case "sitting_down":
                mstartSitButton.setActivated(!sensorEnabled);
                mstartSitButton.setBackgroundResource(background);
                break;
            case "idle":
                mstartIdle.setActivated(!sensorEnabled);
                mstartIdle.setBackgroundResource(background);
                break;
            default:
                break;
        }
    }

    private void saveData()
    {
        debug("Extract Features");
        accData.extractFeatures();
        debug("Compute String");
        String data_string = String.format("%2f;%.2f;%.2f;%.2f;%s\n",
                accData.features.min, accData.features.max, accData.features.index_max, accData.features.mean,
                currentActivity);
        debug("Save Data");
        if(currentFile == null)
        {
            getCurrentFile();
        }
        try {
            fOutStream = new FileOutputStream(currentFile, true);
            fOutStream.write(data_string.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeFile();
    }

    private void closeFile()
    {
        debug("Close File: " + currentFile.toString());
        try {
            fOutStream.flush();
            fOutStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteLatestSample()  {
        if(currentFile != null && currentFile.exists()) {
            try{
                RandomAccessFile f = new RandomAccessFile(currentFile, "rw");
                long length = f.length() - 1;
                byte b;
                do {
                    length -= 1;
                    f.seek(length);
                    b = f.readByte();
                } while(b != 10 && length > 0);
                if(length == 0)
                {
                    deleteFile();
                }else{
                    debug("Delete latest sample: " + currentFile.toString());
                    f.setLength(length+1);
                    f.close();
                }
            }catch(IOException e)
            {
                debug(e.getMessage());
            }

        }
        else
        {
            debug("Latest sample not available");
        }
    }

    private void deleteFile()
    {
        if(currentFile != null && currentFile.exists()) {
            debug("No more samples, deleted File!");
            currentFile.delete();
            currentFile = null;
        }
        else
        {
            debug("Training file not available");
        }
    }


    //Button Click Handler
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.walk_button:
                currentActivity = "walking";
                startMonitoring();
                break;
            case R.id.stand_up_button:
                currentActivity = "standing_up";
                startMonitoring();
                break;
            case R.id.sit_down_button:
                currentActivity = "sitting_down";
                startMonitoring();
                break;
            case R.id.idle_button:
                currentActivity = "idle";
                startMonitoring();
                break;
            case R.id.delete_latest:
                deleteLatestSample();
                break;
            case R.id.delete_file:
                deleteFile();
                break;

            default:
                //do nothing

        }
    }

    private void debug(String msg)
    {
        Log.wtf(tag, msg);
        mTextDebug.setText(msg);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void getCurrentFile()
    {
        String root = Environment.getExternalStorageDirectory().toString();
        currentFile = new File (root, getResources().getString(R.string.file_name));
    }
}
