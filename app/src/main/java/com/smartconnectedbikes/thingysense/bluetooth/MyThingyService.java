package com.smartconnectedbikes.thingysense.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyConnection;
import com.opencsv.CSVWriter;

public class MyThingyService extends BaseThingyService {
    private ThingyBinder thingyBinder = new ThingyBinder();

    @Nullable
    @Override
    public BaseThingyBinder onBind(Intent intent) {
        return thingyBinder;
    }

    public class ThingyBinder extends BaseThingyBinder {
        @Override
        public ThingyConnection getThingyConnection(BluetoothDevice device) {
            return mThingyConnections.get(device);
        }
    }
    public void CSV_write(String filename, String[] data)
    {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = filename;
        String filePath = baseDir + File.separator + fileName;
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
            writer = new CSVWriter(mFileWriter);
        }
        else
        {
            try {
                writer = new CSVWriter(new FileWriter(filePath));
                Toast.makeText(getApplicationContext(), "Error in File Creating", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Toast.makeText(getApplicationContext(), "Saving sensor data", Toast.LENGTH_SHORT).show();
            writer.writeNext(data);
            writer.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}