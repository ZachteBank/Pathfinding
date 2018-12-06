package com.fontys.bramk.pathfinding;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    BluetoothAdapter mBluetoothAdapter = null;
    List<String> strings = new ArrayList<>();
    Button buttonScan = null;
    boolean running = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                strings.add(deviceHardwareAddress);
                Toast.makeText(MainActivity.this, deviceHardwareAddress, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "No bluetoothadapter found", Toast.LENGTH_SHORT).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        buttonScan = (Button) findViewById(R.id.buttonScan);
        if(buttonScan != null) {
            buttonScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!running) {
                        checkBTPermissions();
                        mBluetoothAdapter.startDiscovery();
                        Toast.makeText(MainActivity.this, "Start discovery", Toast.LENGTH_SHORT).show();
                        running = true;
                    } else {
                        mBluetoothAdapter.cancelDiscovery();

                        Toast.makeText(MainActivity.this, "Found " + strings.size() + " results", Toast.LENGTH_LONG).show();
                        //Toast.makeText(MainActivity.this, "Stop discovery", Toast.LENGTH_SHORT).show();
                        running = false;
                    }
                }
            });
        }
    }

    public void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            Toast.makeText(this, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.", Toast.LENGTH_LONG).show();

            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Toast.makeText(this, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

}
