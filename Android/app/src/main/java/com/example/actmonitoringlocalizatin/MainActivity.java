package com.example.actmonitoringlocalizatin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private String tag = "DEBUG";

    private String mselectedButton;

    private int fileCount;
    private int fileCountWalk;
    private int fileCountStand;
    private int fileCountSit;

    private File currentFile;

    // TextViews to display current sensor values
    private TextView mTextSensorAccX;
    private TextView mTextSensorAccY;
    private TextView mTextSensorAccZ;

    private TextView mTextDebug;

    private Button mstartWalkButton;
    private Button mstartStandButton;
    private Button mstartSitButton;
    private Button mdeleteLatestButton;

    private boolean enableSensor;

    private FileOutputStream fOutStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        mdeleteLatestButton = findViewById(R.id.delete_latest);
        mstartWalkButton.setOnClickListener(this);
        mstartStandButton.setOnClickListener(this);
        mstartSitButton.setOnClickListener(this);
        mdeleteLatestButton.setOnClickListener(this);

        //init Variables
        enableSensor = true;
        fileCount = 0;
        fileCountWalk = 0;
        fileCountStand = 0;
        fileCountSit = 0;
        fOutStream = null;
        currentFile = null;

        if (mSensorAcc == null) {
            mTextDebug.setText("Required sensor not available!");
            //TODO: close App and notify user
        }

        debug("onCreate complete");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        addDataToProcess (timestamp, x, y, z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        getFileCount();
        debug("App started");
    }

    private void getFileCount()
    {
        debug("Get file count");

        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File (root, "Filecounters.txt");

        try {
            // open the file for reading
            InputStream fis = new FileInputStream(file);

            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            String fileCounter = new String(data);
            String[] fileCountersSplit = fileCounter.split(";");
            fileCountWalk = Integer.valueOf(fileCountersSplit[0]);
            fileCountStand = Integer.valueOf(fileCountersSplit[1]);
            fileCountSit = Integer.valueOf(fileCountersSplit[2]);
        } catch (Exception e) {
            // print stack trace.
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        debug("App stopped");
        saveFileCount();
        //TODO: save state
    }

    private void saveFileCount()
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File (root, "Filecounters.txt");
        fileCount++;
        debug("New File:" + file.toString());
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(String.format("%d;%d;%d", fileCountWalk, fileCountStand, fileCountSit).getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleSensor() {
        if(enableSensor){
            newFile();
            debug("Start monitoring");
            setButtonText();
            mSensorManager.registerListener(this, mSensorAcc,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        else
        {
            debug("Stop monitoring");
            setButtonText();
            mSensorManager.unregisterListener(this, mSensorAcc);
            closeFile();
        }
        enableSensor = !enableSensor;
    }

    private void setButtonText()
    {
        if(enableSensor)
        {
            switch (mselectedButton)
            {
                case "walking":
                    mstartWalkButton.setText("Stop " + mselectedButton);
                    break;
                case "standing_up":
                    mstartStandButton.setText("Stop " + mselectedButton);
                    break;
                case "sitting_down":
                    mstartSitButton.setText("Stop " + mselectedButton);
                    break;
                default:
                    break;
            }
        } else
        {
            switch (mselectedButton)
            {
                case "walking":
                    mstartWalkButton.setText("Start " + mselectedButton);
                    break;
                case "standing_up":
                    mstartStandButton.setText("Start " + mselectedButton);
                    break;
                case "sitting_down":
                    mstartSitButton.setText("Start " + mselectedButton);
                    break;
                default:
                    break;
            }
        }

    }

    private void addDataToProcess(long timestamp, double x, double y, double z)
    {
        String text = String.format("%d;%.2f;%.2f;%.2f\n", timestamp, x, y, z);

        try{
            fOutStream.write(text.getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } }

    private void newFile() {

        String root = Environment.getExternalStorageDirectory().toString();
        currentFile = new File (root, mselectedButton + "_" + fileCount + ".txt");
        fileCount++;
        debug("New File: " + currentFile.toString());
        if (currentFile.exists ())
            currentFile.delete ();
        try {
            fOutStream = new FileOutputStream(currentFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void deleteLatestSample()
    {
        if(currentFile != null && currentFile.exists()) {
            debug("Delete latest sample: " + currentFile.toString());
            currentFile.delete();
            currentFile = null;
        }
        else
        {
            debug("Latest sample not available");
        }
    }


    //Button Click Handler
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.walk_button:
                mselectedButton = "walking";
                fileCount = fileCountWalk;
                toggleSensor();
                setButtonText();
                fileCountWalk = fileCount;
                break;
            case R.id.stand_up_button:
                mselectedButton = "standing_up";
                fileCount = fileCountStand;
                toggleSensor();
                fileCountStand = fileCount;
                break;
            case R.id.sit_down_button:
                mselectedButton = "sitting_down";
                fileCount = fileCountSit;
                toggleSensor();
                fileCountSit = fileCount;
                break;
            case R.id.delete_latest:
                deleteLatestSample();
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
}
