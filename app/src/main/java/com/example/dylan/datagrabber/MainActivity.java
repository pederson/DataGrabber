package com.example.dylan.datagrabber;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mGyro;
    private FileWriter writer;
    private FileWriter magwriter;
    private FileWriter gyrowriter;
    private double[] state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = new double[15];
        for (int i=0; i<15; i++) state[i] = 0.0;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
    }

    public void onStartClick(View view){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
        TextView title = (TextView)findViewById(R.id.textView);
        title.setText("CAPTURING");

    }

    public void onStopClick(View view){
        mSensorManager.unregisterListener(this);
        TextView title = (TextView)findViewById(R.id.textView);
        title.setText("Hello World!");
    }

    protected void onResume(){
        super.onResume();
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath()+"/DataGrabber");
        dir.mkdirs();
        File sdCardFile = new File(dir,"accelerometer_data.txt");
        File magFile = new File(dir,"magnetometer_data.txt");
        File gyroFile = new File(dir,"gyroscope_data.txt");
        Log.d("TAG",sdCardFile.getPath());
        try {
            writer = new FileWriter(sdCardFile,false);
            writer.write("t,ax,ay,az \n");//,mx,my,mz,mxb,myb,mzb,wx,wy,wz,wdx,wdy,wdz \n");
            writer.flush();

            magwriter = new FileWriter(magFile,false);
            magwriter.write("t,mx,my,mz,mxb,myb,mzb \n");
            magwriter.flush();

            gyrowriter = new FileWriter(gyroFile,false);
            gyrowriter.write("t,wx,wy,wz,wdx,wdy,wdz \n");
            gyrowriter.flush();
        }
        catch(IOException ex){
            Log.d("fileopen","Failed to open file");
            ex.printStackTrace();
        }
    }

    protected void onPause(){
        super.onPause();

        if(writer != null){
            try{
                writer.close();
            }
            catch(IOException ex){
                Log.d("fileclose","Failed to close file");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        double t=event.timestamp;
        Sensor sen = event.sensor;
        int offset=0;
        if (sen.getType() == Sensor.TYPE_ACCELEROMETER){
            offset = 0;
            try{
                writer.write(""+t);
                for (int i=0; i<event.values.length; i++) writer.write(","+event.values[i]);
                writer.write("\n");
            }
            catch(IOException ex){
                Log.d("TAG","Failed to write to file");
            }
        }
        else if (sen.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED){
            offset = 3;
            try{
                magwriter.write(""+t);
                for (int i=0; i<event.values.length; i++) magwriter.write(","+event.values[i]);
                magwriter.write("\n");
            }
            catch(IOException ex){
                Log.d("TAG","Failed to write to file");
            }
        }
        else if (sen.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED){
            offset = 9;
            try{
                gyrowriter.write(""+t);
                for (int i=0; i<event.values.length; i++) gyrowriter.write(","+event.values[i]);
                gyrowriter.write("\n");
            }
            catch(IOException ex){
                Log.d("TAG","Failed to write to file");
            }
        }

        // fill in the state
        for (int i=offset; i<offset+event.values.length; i++) state[i] = event.values[i-offset];


    }
}
