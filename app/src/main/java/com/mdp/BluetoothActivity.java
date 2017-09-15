package com.mdp;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = BluetoothActivity.class.getSimpleName();
    protected static final int SUCCESS_CONNECT = 10;
    protected static final int MESSAGE_READ = 11;

    private Switch bluetooth_toggle;
    private BluetoothAdapter mBluetoothAdapter;
    private static int REQUEST_ENABLE_BT = 1;

    //device list
    private ArrayAdapter<String> deviceArrayAdapter;
    private ListView device_list;
    private ArrayList<String> deviceArray = new ArrayList<String>();

    //paired list
    private ArrayAdapter<String> pairedArrayAdapter;
    private ListView paired_list;
    private ArrayList<String> pairedArray = new ArrayList<String>();

    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Button discover_button;
    private Button test_send;
    private Button start_connection;
    private EditText test_text;

    BluetoothDevice mBTDevice;

    //BroadcastReceiver for bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        bluetooth_toggle.setChecked(false);
                        disableUI();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetooth_toggle.setChecked(true);
                        enableUI();
                        displayPairedDevices();
                        break;
                }
            }
        }
    };

    private BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            //Toast.makeText(getBaseContext(), "Device Found!", Toast.LENGTH_SHORT).show();
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, device.getName() + " " + device.getAddress());
            // Add the name and address to an array adapter to show in a ListView
            deviceArray.add(device.getName() + "\n" + device.getAddress());
            deviceArrayAdapter.notifyDataSetChanged();

        }
        }
    };

    private BroadcastReceiver discoverFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Toast.makeText(getBaseContext(), "Stopped Discovering", Toast.LENGTH_SHORT).show();
            discover_button.setEnabled(true);
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private BroadcastReceiver pairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                //means device paired
                Log.d(TAG, "Devices Paired");
                updateListViews(mDevice);
            }
        }
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //init Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getBaseContext(), "Bluetooth is not supported on this device, please use another device", Toast.LENGTH_LONG).show();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }

        test_send = (Button) findViewById(R.id.test_send);
        test_text = (EditText) findViewById(R.id.test_text);
        start_connection = (Button) findViewById(R.id.btn_start_connection);

        start_connection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startConnection(mBTDevice);
            }
        });

        test_send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                byte[] bytes = test_text.getText().toString().getBytes(Charset.defaultCharset());
                MainApplication.getBTConnection().write(bytes);
            }
        });

        //find all paired device and display them
        paired_list = (ListView) findViewById(R.id.paired_list);
        pairedArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedArray);
        paired_list.setAdapter(pairedArrayAdapter);

        displayPairedDevices();

        //set broadcastreceiver
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        //setup device_list
        device_list = (ListView) findViewById(R.id.device_list);
        deviceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceArray);
        device_list.setAdapter(deviceArrayAdapter);

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String item = (String) parent.getItemAtPosition(position);
            String[] split = item.split("\n");
            String address = split[1];

            Log.d(TAG, address);

            BluetoothDevice connect_device = mBluetoothAdapter.getRemoteDevice(address);

            Boolean isBonded = false;
            try {
                isBonded = createBond(connect_device);
                if (isBonded) {
                    //if pairing is commencing, listen for if pairing is successful
                    registerReceiver(pairingReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        /*ConnectThread connect_thread = new ConnectThread(connect_device);
        connect_thread.run();*/
            }
        });

        //init Button(s)
        discover_button = (Button) findViewById(R.id.btn_discover);

        Toolbar bluetooth_toolbar = (Toolbar) findViewById(R.id.bluetooth_toolbar);
        setSupportActionBar(bluetooth_toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            unregisterReceiver(discoverFinishReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no discoverFinishReceiver registered");
        }
        try{
            unregisterReceiver(pairingReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no pairingReceiver registered");
        }
        try{
            unregisterReceiver(discoverReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no discoverReceiver registered");
        }
        try{
            unregisterReceiver(mReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no mReceiver registered");
        }
    }

    public void startBTConnection(BluetoothDevice device, UUID mUUID){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        MainApplication.getBTConnection().startClient(device, mUUID);
    }

    public void startConnection(BluetoothDevice device){
        if(MainApplication.getBTConnection() == null){
            MainApplication.initializeBTConnection(BluetoothActivity.this);
        }
        mBTDevice = device;
        Log.d(TAG, String.valueOf(device == null));
        startBTConnection(device, uuid);
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_toolbar_menu, menu);

        MenuItem bluetoothSwitch = menu.findItem(R.id.mySwitch);
        bluetoothSwitch.setActionView(R.layout.bluetooth_layout);

        bluetooth_toggle = (Switch) menu.findItem(R.id.mySwitch).getActionView().findViewById(R.id.toggle_bluetooth);

        //Check if bluetooth is already on, if on, switch is to be toggled on 1st
        if (mBluetoothAdapter.isEnabled()) {
            bluetooth_toggle.setChecked(true);
            enableUI();
        } else {
            disableUI();
        }

        bluetooth_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Turn on Bluetooth
                    mBluetoothAdapter.enable();
                    enableUI();
                    displayPairedDevices();
                    //searchDevices();
                } else {
                    //Turn off Bluetooth
                    mBluetoothAdapter.disable();
                    disableUI();
                }
            }
        });

        return true;
    }

    public void searchDevices(View view) {
        Toast.makeText(getBaseContext(), "Begin Discovering", Toast.LENGTH_SHORT).show();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        deviceArray.clear();
        deviceArrayAdapter.notifyDataSetChanged();
        if (mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Re-Scanning for Devices");
            mBluetoothAdapter.cancelDiscovery();

            mBluetoothAdapter.startDiscovery();
            registerReceiver(discoverReceiver, filter);
            registerReceiver(discoverFinishReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Scanning for Devices");
            mBluetoothAdapter.startDiscovery();
            registerReceiver(discoverReceiver, filter);
            registerReceiver(discoverFinishReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }
        discover_button.setEnabled(false);
    }

    public void disableUI() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
        ListView deviceList = (ListView) findViewById(R.id.device_list);
        deviceList.setEnabled(false);
        mBluetoothAdapter.cancelDiscovery();
        deviceArray.clear();
        deviceArrayAdapter.notifyDataSetChanged();
    }

    public void enableUI() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }
        ListView deviceList = (ListView) findViewById(R.id.device_list);
        deviceList.setEnabled(true);
    }

    public static class connectDeviceDialogFragment extends android.support.v4.app.DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_ask_connect)
                    .setPositiveButton(R.string.dialog_yes_connect, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "Attempting to connect");
                            //BluetoothDevice btDevice =
                            //ConnectThread connect = new ConnectThread()
                        }
                    })
                    .setNegativeButton(R.string.dialog_no_connect, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private void updateListViews(BluetoothDevice btDevice){
        //find bluetooth device from list
        String targetString = btDevice.getName() + "\n" + btDevice.getAddress();
        Log.d(TAG, targetString);
        for(int i = 0; i < deviceArray.size(); i++){
            Log.d(TAG, String.valueOf(deviceArray.get(i).compareTo(targetString) == 0));
            if(deviceArray.get(i).compareTo(targetString) == 0){
                Log.d(TAG, deviceArray.get(i));
                deviceArray.remove(i);
                deviceArrayAdapter.notifyDataSetChanged();
            }
        }
        pairedArray.add(targetString);
        pairedArrayAdapter.notifyDataSetChanged();
    }

    public void displayPairedDevices(){
        if(mBluetoothAdapter.isEnabled()){
            //empty arraylist
            pairedArray.clear();
            pairedArrayAdapter.notifyDataSetChanged();

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    pairedArray.add(deviceName + "\n" + deviceHardwareAddress);
                }
            }
            pairedArrayAdapter.notifyDataSetChanged();
            paired_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = (String) parent.getItemAtPosition(position);
                    String[] split = item.split("\n");
                    String address = split[1];
                    Log.d(TAG, address);
                    BluetoothDevice connect_device = mBluetoothAdapter.getRemoteDevice(address);
                    mBTDevice = connect_device;
                    startConnection(connect_device);
                }
            });
        }
    }
}
