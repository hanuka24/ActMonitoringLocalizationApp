package com.hankus.activitymonitoring;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Locale;

public class TrainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private String mTag = "DEBUG - Train mActivity: ";
    private String mCurrentActivity;

    private File mCurrentFile;

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

    private boolean mSensorEnabled;

    private AccData mAccData;

    private FileOutputStream mfOutStream;

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
        mfOutStream = null;
        mCurrentActivity = "Unknown";
        mSensorEnabled = false;

        // set current file
        getCurrentFile();

        mAccData = new AccData();

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

        long timestamp = Calendar.getInstance().getTimeInMillis();

        mTextSensorAccX.setText(getResources().getString(R.string.label_acc_x, x));
        mTextSensorAccY.setText(getResources().getString(R.string.label_acc_y, y));
        mTextSensorAccZ.setText(getResources().getString(R.string.label_acc_z, z));

        if(mAccData.getSize() < mAccData.getNumberOfSamples())
            mAccData.addSample(x,y,z, timestamp);
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
        mSensorManager.unregisterListener(this, mSensorAcc);
    }

    private void startMonitoring() {
        if(!mSensorEnabled) {
            debug("Start monitoring");
            mAccData.clear();
            setButtonText("begin");
            mSensorManager.registerListener(this, mSensorAcc,
                    SensorManager.SENSOR_DELAY_GAME);
            mSensorEnabled = true;
        }
    }

    private void stopMonitoring()
    {
        debug("Stop monitoring");
        mSensorManager.unregisterListener(this, mSensorAcc);
        mSensorEnabled = false;
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
        switch (mCurrentActivity) {
            case "walking":
                mstartWalkButton.setActivated(!mSensorEnabled);
                mstartWalkButton.setBackgroundResource(background);
                break;
            case "standing_up":
                mstartStandButton.setActivated(!mSensorEnabled);
                mstartStandButton.setBackgroundResource(background);
                break;
            case "sitting_down":
                mstartSitButton.setActivated(!mSensorEnabled);
                mstartSitButton.setBackgroundResource(background);
                break;
            case "idle":
                mstartIdle.setActivated(!mSensorEnabled);
                mstartIdle.setBackgroundResource(background);
                break;
            default:
                break;
        }
    }

    private void saveData()
    {
        debug("Extract Features");
        mAccData.extractFeatures();
        debug("Compute String");
        String data_string = String.format(Locale.ENGLISH, "%f;%f;%f;%f;%f;%f;%s\n",
                mAccData.mFeatures.mMin, mAccData.mFeatures.mMax, mAccData.mFeatures.mMeanX, mAccData.mFeatures.mMeanY, mAccData.mFeatures.mMeanZ,
                mAccData.mFeatures.mFrequency, mCurrentActivity);
        debug("Save Data");
        if(mCurrentFile == null)
        {
            getCurrentFile();
        }
        try {
            mfOutStream = new FileOutputStream(mCurrentFile, true);
            mfOutStream.write(data_string.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeFile();
    }

    private void closeFile()
    {
        debug("Close File: " + mCurrentFile.toString());
        try {
            mfOutStream.flush();
            mfOutStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void deleteLatestSample()  {
        if(mCurrentFile != null && mCurrentFile.exists()) {
            try{
                RandomAccessFile f = new RandomAccessFile(mCurrentFile, "rw");
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
                    debug("Delete latest sample: " + mCurrentFile.toString());
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
        if(mCurrentFile != null && mCurrentFile.exists()) {
            debug("No more samples, deleted File!");
            mCurrentFile.delete();
            mCurrentFile = null;
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
                mCurrentActivity = "walking";
                startMonitoring();
                break;
            case R.id.stand_up_button:
                mCurrentActivity = "standing_up";
                startMonitoring();
                break;
            case R.id.sit_down_button:
                mCurrentActivity = "sitting_down";
                startMonitoring();
                break;
            case R.id.idle_button:
                mCurrentActivity = "idle";
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
        Log.wtf(mTag, msg);
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
        mCurrentFile = new File (root, getResources().getString(R.string.file_name));
    }
}
