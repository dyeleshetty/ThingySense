package com.smartconnectedbikes.thingysense;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.smartconnectedbikes.thingysense.bluetooth.MyThingyService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ThingyDeviceActivity extends AppCompatActivity implements  ThingySdkManager.ServiceConnectionListener, LocationListener, SensorEventListener {
    private static final String TAG = ThingyDeviceActivity.class.getName();
    private BluetoothDevice bluetoothDevice;
    private ThingySdkManager thingySdkManager;
    private ThingyListener thingyListener;
    private LocationManager locationManager;
    private Button exportButton;
    private RadioButton smoothRd, tilesRd, unpavedRd, goodRd, mediumRd, badRd;
    private int roadTypeInt = -1;
    private int roadQualityInt = -1;

    private TextView status;

    private SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
    private String timeText = sdf.format(new Date());
    private String fileName = "ThingyData_"+timeText+".csv";
    private String timeStamp;
    private boolean first = true;
    private boolean exportFlag = false;
    private float[] mAccEntry = {0,0,0};
    private float[] mGyrEntry = {0,0,0};
    private float[] mComEntry = {0,0,0};
    private long mSensorTimeStamp;


    private Location mLocation;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer, senGyroscope, senCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thingy_device);
        status = (TextView) findViewById(R.id.status);
        exportButton = (Button) findViewById(R.id.exportButton);
        smoothRd = (RadioButton) findViewById(R.id.smoothRoad);
        tilesRd = (RadioButton) findViewById(R.id.tilesRoad);
        unpavedRd = (RadioButton) findViewById(R.id.unpavedRoad);
        goodRd = (RadioButton) findViewById(R.id.goodRoad);
        mediumRd = (RadioButton) findViewById(R.id.mediumRoad);
        badRd = (RadioButton) findViewById(R.id.badRoad);
        first = true;
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senCompass = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senCompass , SensorManager.SENSOR_DELAY_NORMAL);

        // TODO 2: Get the BluetoothDevice from the intent
        Intent intent = getIntent();
        bluetoothDevice = intent.getParcelableExtra(MainActivity.BLUETOOTH_DEVICE_KEY);
        // TODO 3: Get an instance of ThingySdkManager using ThingySdkManager.getInstance()
        thingySdkManager = ThingySdkManager.getInstance();

        if (ActivityCompat.checkSelfPermission((Activity) this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        mLocation = locationManager.getCurrentLocation();
        Log.i(TAG,"Location acquired: Lat = "+String.valueOf(mLocation.getLatitude()));

         thingyListener = new ThingyListener() {
            @Override
            public void onDeviceConnected(BluetoothDevice device, int connectionState) {

            }

            @Override
            public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {

            }

            @Override
            public void onServiceDiscoveryCompleted(BluetoothDevice device) {
                thingySdkManager.enableRawDataNotifications(device,true);
                //The below line could potentially change the sampling frequency!!
                thingySdkManager.setMotionConfigurationCharacteristic(device, 100, 100,10,100, 0x00);

            }

            @Override
            public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

            }

            @Override
            public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

            }

            @Override
            public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

            }

            @Override
            public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

            }

            @Override
            public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

            }

            @Override
            public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

            }

            @Override
            public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

            }

            @Override
            public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

            }

            @Override
            public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

            }

            @Override
            public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

            }

            @Override
            public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

            }

            @Override
            public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
//                if(exportFlag) {
//                    Log.i(TAG, "Accx = " + x);
//                    Log.i(TAG, "Accy = " + y);
//                    Log.i(TAG, "Accz = " + z);
//                    timeStamp = String.valueOf(System.currentTimeMillis());
//                    first = false;
//                    String[] acc_entries = {timeStamp, String.valueOf(x), String.valueOf(y), String.valueOf(z), String.valueOf(roadTypeInt),String.valueOf(roadQualityInt)};
////                    accEntry.add(acc_entries);
//                    CSV_write("acc",acc_entries);
//                }
//                else status.setText("Connection Successful.");
            }

            @Override
            public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
//                if(exportFlag){
//                    Log.i(TAG, "Gyrx = " + x);
//                    Log.i(TAG, "Gyry = " + y);
//                    Log.i(TAG, "Gyrz = " + z);
//                    String[] gyr_entries = {timeStamp,String.valueOf(x), String.valueOf(y), String.valueOf(z)};
////                    gyrEntry.add(gyr_entries);
//                    CSV_write("gyr",gyr_entries);
//                }

            }

            @Override
            public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
//                if(exportFlag){
//                    Log.i(TAG, "Comx = " + x);
//                    Log.i(TAG, "Comy = " + y);
//                    Log.i(TAG, "Comz = " + z);
//                    String[] com_entries = {timeStamp, String.valueOf(x), String.valueOf(y), String.valueOf(z)};
////                    comEntry.add(com_entries);
//                    CSV_write("com",com_entries);
//                }


            }

            @Override
            public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

            }

            @Override
            public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

            }

            @Override
            public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

            }

            @Override
            public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

            }

            @Override
            public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

            }

            @Override
            public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

            }

             @Override
             public void onMotionChangedEvent(BluetoothDevice bluetoothDevice, float xa, float ya, float za, float xg, float yg, float zg, float xc, float yc, float zc) {
                 if(exportFlag) {
                     Log.i(TAG, "Accx = " + xa);
                     Log.i(TAG, "Accy = " + ya);
                     Log.i(TAG, "Accz = " + za);
                     Log.i(TAG, "Gyrx = " + xg);
                     Log.i(TAG, "Gyry = " + yg);
                     Log.i(TAG, "Gyrz = " + zg);
                     Log.i(TAG, "Comx = " + xc);
                     Log.i(TAG, "Comy = " + yc);
                     Log.i(TAG, "Comz = " + zc);
                     Log.i(TAG,"Lat = " + String.valueOf(mLocation.getLatitude()));
                     Log.i(TAG,"Lon = " + String.valueOf(mLocation.getLongitude()));
                     Log.i(TAG,"Speed = " + String.valueOf(mLocation.getSpeed()));
                     timeStamp = String.valueOf(System.currentTimeMillis());
                     first = false;
                     String[] acc_entries = {timeStamp, String.valueOf(xa), String.valueOf(ya), String.valueOf(za),
                             String.valueOf(xg), String.valueOf(yg), String.valueOf(zg),
                             String.valueOf(xc), String.valueOf(yc), String.valueOf(zc), "\t",
                             String.valueOf(mSensorTimeStamp), String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()),
                             String.valueOf(mAccEntry[0]), String.valueOf(mAccEntry[1]), String.valueOf(mAccEntry[2]),
                             String.valueOf(mGyrEntry[0]), String.valueOf(mGyrEntry[1]), String.valueOf(mGyrEntry[2]),
                             String.valueOf(mComEntry[0]), String.valueOf(mComEntry[1]), String.valueOf(mComEntry[2]),
                             String.valueOf(mLocation.getSpeed()), String.valueOf(roadTypeInt), String.valueOf(roadQualityInt)};
//                    accEntry.add(acc_entries);
                     CSV_write("out",acc_entries);
                 }
                 else status.setText("Connection Successful.");
             }
         };
         exportButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 exportFlag = !exportFlag;
                 if (exportFlag) exportButton.setText("Stop Export");
                 else {
                     exportButton.setText("Export to CSV");
                 }
             }
         });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        // TODO 4a: Bind the MyThingyService to the ThingySdkManager
        thingySdkManager.bindService(this, MyThingyService.class);
        // TODO 6a: Register your custom ThingyListener
        ThingyListenerHelper.registerThingyListener(this,thingyListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
//        CSV_write(null);
        // TODO 4b: Unbind the MyThingyService
        thingySdkManager.unbindService(this);
        // TODO 6a: Unregister your custom ThingyListener
        ThingyListenerHelper.unregisterThingyListener(this, thingyListener);
        status.setText("Disconnected; Stopping...");
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "Thingy Service Connected");
        // TODO 7: Connect to the ThingyBoard using the ThingySdkManager object
        thingySdkManager.connectToThingy(this, bluetoothDevice, MyThingyService.class);
    }



    public void CSV_write(String filename, String[] data)
    {
        status.setText("Connection Successful; Exporting...");
        String filePath = String.valueOf(getFileStreamPath(filename+fileName));
//        Log.i(TAG, "Directory===>"+baseDir);
//        String filePath = baseDir + File.separator + fileName;
        Log.i(TAG, "FilePath===>"+filePath);
        File f = new File(filePath);
        CSVWriter writer = null;
        // File exist
        if(f.exists()&&!f.isDirectory())
        {
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error in File Opening", Toast.LENGTH_SHORT).show();
            }
            writer = new CSVWriter(mFileWriter,
                                CSVWriter.DEFAULT_SEPARATOR,
                                CSVWriter.NO_QUOTE_CHARACTER,
                                CSVWriter.NO_ESCAPE_CHARACTER,
                                CSVWriter.DEFAULT_LINE_END);
        }
        else
        {
            try {
                writer = new CSVWriter(new FileWriter(filePath),
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
                Toast.makeText(getApplicationContext(), "Error in File Creating", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Toast.makeText(getApplicationContext(), "Saving sensor data", Toast.LENGTH_SHORT).show();
                if(data.length > 0) {
                    writer.writeNext(data);
                }
            writer.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        String[] gps_entries = {timeStamp, String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),"0"};
//        gpsEntry.add(gps_entries);
        mLocation = location;
//        if(exportFlag)CSV_write("gps", gps_entries);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        String strType ="";
        String strQty="";

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.smoothRoad:
                if (checked) {
                    roadTypeInt = 0;
                    strType = "Smooth Road selected.";
                }
                    break;
            case R.id.tilesRoad:
                if (checked) {
                    roadTypeInt = 1;
                    strType = "Tiles Road selected.";
                }
                    break;
            case R.id.unpavedRoad:
                if (checked) {
                    roadTypeInt = 2;
                    strType = "Unpaved Road selected.";
                }
                    break;
            case R.id.goodRoad:
                if (checked) {
                    roadQualityInt = 0;
                    strQty = "Good Road selected.";
                }
                    break;
            case R.id.mediumRoad:
                if (checked) {
                    roadQualityInt = 1;
                    strQty = "Medium Road selected.";
                }
                    break;
            case R.id.badRoad:
                if (checked) {
                    roadQualityInt = 2;
                    strQty = "Bad Road selected.";
                }
                    break;
        }
//        Toast.makeText(getApplicationContext(), strType+"\t"+strQty, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        mSensorTimeStamp = sensorEvent.timestamp;
        if(sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            mAccEntry[0] = sensorEvent.values[0];
            mAccEntry[1] = sensorEvent.values[1];
            mAccEntry[2] = sensorEvent.values[2];
        }
        if(sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            mGyrEntry[0] = sensorEvent.values[0];
            mGyrEntry[1] = sensorEvent.values[1];
            mGyrEntry[2] = sensorEvent.values[2];
        }
        if(sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
            mComEntry[0] = sensorEvent.values[0];
            mComEntry[1] = sensorEvent.values[1];
            mComEntry[2] = sensorEvent.values[2];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}