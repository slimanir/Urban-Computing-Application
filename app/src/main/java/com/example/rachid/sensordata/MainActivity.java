package com.example.rachid.sensordata;

/**
 * Created by Rachid on 20/02/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ValueEventListener {

    private static final String TAG="MainActivity";

    private SensorManager sensorManager;
    Sensor accelerometer; // defining the sensor
    TextView xValue, yValue, zValue;
    DatabaseReference databaseReference;
    Button button;


    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;
//    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//    DatabaseReference mRootReference = firebaseDatabase.getReference();
//    DatabaseReference mHeadingReference = mRootReference.child("accleration across (x,y,z)->timestamp: acceleration");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });


        Log.d(TAG, "OnCreate: Initializing Sensor Services"); // Using the service
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL); // Listener registration.
        Log.d(TAG, "onCreate:Registered accelerometer listener");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("AccelerometerData");


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG,"onSensorChanged: X : " + sensorEvent.values[0] + "  Y :  " + sensorEvent.values[1] +  "  Z :  " + sensorEvent.values[2]); // Results shown in the log

/**
 * Getting the values of X, Y and Z axises and displaying the values in real time in the screen
 */
        xValue.setText("X Axis : " + sensorEvent.values[0]);
        yValue.setText("Y Axis : " + sensorEvent.values[1]);
        zValue.setText("Z Axis : " + sensorEvent.values[2]);
        Float X = sensorEvent.values[0];
        Float Y = sensorEvent.values[1];
        Float Z =sensorEvent.values[2];

        // Getting the systems current time
        long curTime = System.currentTimeMillis();
        // Check the last sensor update in the last 100 ms
        if ((curTime - lastUpdate) > 200) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            // Calculating the phone's accelerometer speed

            float speed = Math.abs(X + Y + Z - last_x - last_y - last_z)/ diffTime * 10000;
            // detect if the phone was Shaked or not

            if (speed > SHAKE_THRESHOLD) {
                // Telling the user that the phone was Shaked
                Toast.makeText(MainActivity.this,"Phone was Shaked", Toast.LENGTH_SHORT).show();
            }

            last_x = X;
            last_y = Y;
            last_z = Z;
        }

        /**
         * Uploading the data in the firebase realtime database
         */

        databaseReference.child(String.valueOf(new Date().getTime())).setValue(" X Axis : " + X + "|" +  " Y Axis : " +  Y + "|"  + " Z Axis :" + Z);

    }
    // When the app is in the background the accelerometer is in standby mode to save battery consumption
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    // When the app is opened again the accelerometer start to update the values.
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
    }
    // When we leave the application, the accelerometer stops
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
    // Writing the accelerometer Values to the database.
    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

}