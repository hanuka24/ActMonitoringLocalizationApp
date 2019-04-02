package com.example.actmonitoringlocalizatin;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.w3c.dom.Text;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;

    private String tag = "DEBUG";

    private int fileCount;

    // TextViews to display current sensor values
    private TextView mTextSensorAccX;
    private TextView mTextSensorAccY;
    private TextView mTextSensorAccZ;

    private TextView mTextDebug;

    private Button mNewFileButton;
    private Button mStartButton;

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
        mNewFileButton = findViewById(R.id.newfile_button);
        mStartButton = findViewById(R.id.start_button);
        mNewFileButton.setOnClickListener(this);
        mStartButton.setOnClickListener(this);

        //init Variables
        enableSensor = true;
        fileCount = 0;
        fOutStream = null;

        if (mSensorAcc == null) {
            mTextDebug.setText("Required sensor not available!");
            //TODO: close App and notify user
        }
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

        mTextSensorAccX.setText(getResources().getString(R.string.label_acc_x, x));
        mTextSensorAccY.setText(getResources().getString(R.string.label_acc_y, y));
        mTextSensorAccZ.setText(getResources().getString(R.string.label_acc_z, z));

        addDataToProcess (x, y, z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        debug("App stopped");
        //TODO: save state
    }

    private void toggleSensor() {
        if(fOutStream != null){
            if(enableSensor){
                debug("Start monitoring");
                mStartButton.setText("Stop Activity");
                mSensorManager.registerListener(this, mSensorAcc,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
            else
            {
                debug("Stop monitoring");
                mStartButton.setText("Start Activity");
                mSensorManager.unregisterListener(this, mSensorAcc);
                closeFile();
            }
            enableSensor = !enableSensor;
        }
        else
            debug("Create File first!");
    }

    private void addDataToProcess(double x, double y, double z)
    {
        String text = String.format("%.2f;%.2f;%.2f\n", x, y, z);

        try{
            fOutStream.write(text.getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } }

    private void newFile() {

        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File (root, "Activity" + fileCount + ".txt");
        fileCount++;
        debug("New File:" + file.toString());
        if (file.exists ())
            file.delete ();
        try {
            fOutStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeFile()
    {
        debug("Close File");
        try {
            fOutStream.flush();
            fOutStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Button Clck Handler
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.newfile_button:
                newFile();
                break;
            case R.id.start_button:
                toggleSensor();
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
