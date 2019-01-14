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
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity {


    BluetoothAdapter mBluetoothAdapter = null;
    List<MyBluetoothDevice> devices = new ArrayList<>();
    ListView list = null;
    List<String> listItems;
    ArrayAdapter<String> listViewAdapter;

    Button buttonScan = null;
    EditText beaconId = null;
    boolean running = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                MyBluetoothDevice myDevice = new MyBluetoothDevice();
                myDevice.setDevice(device);
                myDevice.setStrength(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                devices.add(myDevice);
                addItemsToList();
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


        list = (ListView) findViewById(R.id.list);
        buttonScan = (Button) findViewById(R.id.buttonScan);
        beaconId = (EditText) findViewById(R.id.beaconId);
        listItems = new ArrayList<>();

        listViewAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);

        list.setAdapter(listViewAdapter);

        if(buttonScan != null) {
            buttonScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!running) {
                        devices.clear();
                        checkBTPermissions();
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, filter);
                        mBluetoothAdapter.startDiscovery();
                        Toast.makeText(MainActivity.this, "Start discovery", Toast.LENGTH_SHORT).show();
                        running = true;
                    } else {
                        mBluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(mReceiver);
                        try {
                            postResults();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this, "Found " + devices.size() + " results", Toast.LENGTH_LONG).show();
                        //Toast.makeText(MainActivity.this, "Stop discovery", Toast.LENGTH_SHORT).show();
                        running = false;
                    }
                }
            });
        }
    }

    private void postResults() throws JSONException {
        final String urlAdress = "http://145.93.37.51:8070/pathfinding/add";
        Toast.makeText(this, "Start to push data", Toast.LENGTH_SHORT).show();

        final JSONObject jsonAllDevices = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (MyBluetoothDevice device : devices) {
            JSONObject jsonDevice = new JSONObject();
            jsonDevice.put("mac", device.getDevice().getAddress());
            jsonDevice.put("strength", device.getStrength());
            jsonArray.put(jsonDevice);
        }
        jsonAllDevices.put("devices", jsonArray);

        jsonAllDevices.put("beacon", beaconId.getText());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.net.URL url = new URL(urlAdress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    Log.i("JSON", jsonAllDevices.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonAllDevices.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void addItemsToList(){
        listItems.clear();
        for (MyBluetoothDevice device : devices) {
            listItems.add(device.getDevice().getAddress() + device.getStrength() + "dBm");
        }
        listViewAdapter.notifyDataSetChanged();
    }

    public void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
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
        unregisterReceiver(mReceiver);
    }

}
