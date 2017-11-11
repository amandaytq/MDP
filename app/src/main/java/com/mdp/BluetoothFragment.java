package com.mdp;


import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/*
* MDP Team 13
* @version 1.0
* @author Amanda Chan	<achan016@e.ntu.edu.sg>
* @author Tan Peng Hian <ptan023@e.ntu.edu.sg>
*/
public class BluetoothFragment extends Fragment {

    private static final String TAG = BluetoothActivity.class.getSimpleName();

    private Switch bluetooth_toggle;
    private BluetoothAdapter mBluetoothAdapter;

    //unpaired device list
    private ArrayAdapter<String> deviceArrayAdapter;
    private ListView device_list;
    private ArrayList<String> deviceArray = new ArrayList<String>();

    //paired list
    private ArrayAdapter<String> pairedArrayAdapter;
    private ListView paired_list;
    private ArrayList<String> pairedArray = new ArrayList<String>();

    //connected list
    private ArrayAdapter<String> connectedArrayAdapter;
    private ListView connected_list;
    private ArrayList<String> connectedArray = new ArrayList<String>();

    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int DEVICEPAIRED = 0;
    private static final int CONNECTIONSUCCESS = 1;
    private static final int DISCONNECTED = 2;
    private static final int PAIRED_LIST = 3;
    private static final int DEVICE_LIST = 4;
    private static final int CONNECTED_LIST = 5;

    private Button discover_button;

    BluetoothDevice mBTDevice;

    private boolean manualConnecting = false;
    private boolean madeConnection = false;

    View v;

    //declaring broadcast receivers
    //BroadcastReceiver for bluetooth
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
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
                        madeConnection = false;
                        manualConnecting = false;
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
                Log.d(TAG, "Found " + device.getName() + " " + device.getAddress());
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
            discover_button.setEnabled(true);
        }
    };

    private BroadcastReceiver pairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    //means device paired
                    Log.d(TAG, "Devices Paired");
                    Toast.makeText(getContext(), "Devices Paired", Toast.LENGTH_SHORT).show();
                    updateListViews(mDevice, DEVICEPAIRED);
                    reenableList(mDevice.getAddress(), DEVICE_LIST);
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    //means device paired
                    Log.d(TAG, "Devices Not Paired");
                    Toast.makeText(getContext(), "Pairing Failed", Toast.LENGTH_SHORT).show();
                    reenableList(mDevice.getAddress(), DEVICE_LIST);
                }
            }
        }
    };

    //BroadcastReceiver to catch when a bluetooth connection broadcasts
    private BroadcastReceiver BTConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //when connection is successful
            if (MainApplication.connectionSuccessCommand.equals(action)) {
                String id = intent.getStringExtra("deviceId");
                Toast.makeText(getContext(), "Connection Successful", Toast.LENGTH_SHORT).show();
                manualConnecting = false;
                madeConnection = true;
                updateListViews(id, CONNECTIONSUCCESS);
            }
            //when connection failed
            if (MainApplication.connectionFailCommand.equals(action)) {
                Toast.makeText(getContext(), "Connection Failed", Toast.LENGTH_SHORT).show();
                String address = intent.getStringExtra("address");

                //re-enable disabled button
                reenableList(address, PAIRED_LIST);
            }
            //when connection stops
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                Log.d(TAG, "ACTION_ACL_DISCONNECT : Received Disconnect Notice");
                //if user is just manual connecting to a device, do not handle disconnection
                Log.d(TAG, "manualConnecting: " + String.valueOf(manualConnecting));
                if(!manualConnecting){
                    //if connection was made before, attempt to reconnect to device
                    if(madeConnection){
                        Toast.makeText(getContext(), "Device disconnected, attempting to reconnect....", Toast.LENGTH_SHORT).show();
                        updateListViews(null, DISCONNECTED);
                        getContext().registerReceiver(reconnectedReceiver, new IntentFilter(MainApplication.reconnectedCommand));
                        MainApplication.handleDisconnect();
                    }
                }
            }
        }

    };

    //broadcast receiver to handle when reconnection is done
    private BroadcastReceiver reconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainApplication.reconnectedCommand)) {
                Log.d(TAG, "reconnectedReceiver: Reconnected");

                //re-add device back

                //unregister receiver
                try{
                    getContext().unregisterReceiver(reconnectedReceiver);
                }catch(Exception e){
                    Log.d(TAG, "There is no reconnectedReceiver registered");
                }

            }
        }
    };


    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setClickable(true);

        //initializing
        v = view;
        discover_button = (Button) v.findViewById(R.id.btn_discover1);

        //init Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getContext(), "Bluetooth is not supported on this device, please use another device", Toast.LENGTH_LONG).show();
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }


        //register all listeners
        //bluetoothReceiver - Handles bluetooth status (ON/OFF)
        getContext().registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        //setup unpaired device list (device_list)
        device_list = (ListView) v.findViewById(R.id.device_list1);
        deviceArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, deviceArray);
        device_list.setAdapter(deviceArrayAdapter);

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String[] split = item.split("\n");
                String address = split[1];

                //disable button
                view.setEnabled(false);
                view.setClickable(false);
                //stop discovering
                mBluetoothAdapter.cancelDiscovery();

                Log.d(TAG, "onItemClick: Begin Pairing with " + address);

                BluetoothDevice connect_device = mBluetoothAdapter.getRemoteDevice(address);

                try {
                    createBond(connect_device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //find all paired device and display them
        paired_list = (ListView) v.findViewById(R.id.paired_list);
        pairedArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, pairedArray);
        paired_list.setAdapter(pairedArrayAdapter);

        displayPairedDevices();

        //init connected device list
        connected_list = (ListView) v.findViewById(R.id.connected_list);
        connectedArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, connectedArray);
        connected_list.setAdapter(connectedArrayAdapter);

        connected_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String[] split = item.split("\n");
                String address = split[1];

                disconnectDevice();
            }
        });

        //register all listeners
        //pairingReceiver - Handles when pairing starts
        getContext().registerReceiver(pairingReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        //bluetoothReceiver - Handles bluetooth status (ON/OFF)
        getContext().registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        //discoverReceiver - Handles when an unpaired device is found
        getContext().registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        //discoverFinishReceiver - Handles when discover finished
        getContext().registerReceiver(discoverFinishReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        //BTConnectReceiver - Handle events related to connecting to an already paired bluetooth device
        IntentFilter BTConnectFilter = new IntentFilter();
        BTConnectFilter.addAction(MainApplication.connectionSuccessCommand);
        BTConnectFilter.addAction(MainApplication.connectionFailCommand);
        BTConnectFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getContext().registerReceiver(BTConnectReceiver, BTConnectFilter);

        bluetooth_toggle = (Switch) v.findViewById(R.id.bt_switch);

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
                } else {
                    //Turn off Bluetooth
                    mBluetoothAdapter.disable();
                    disableUI();
                }
            }
        });

        discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Begin Discovering", Toast.LENGTH_SHORT).show();
                deviceArray.clear();
                deviceArrayAdapter.notifyDataSetChanged();
                if (mBluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "Re-Scanning for Devices");
                    mBluetoothAdapter.cancelDiscovery();
                    mBluetoothAdapter.startDiscovery();
                }
                if (!mBluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "Scanning for Devices");
                    mBluetoothAdapter.startDiscovery();
                }
                discover_button.setEnabled(false);
            }
        });

        connected_list = (ListView) v.findViewById(R.id.connected_list);
        connectedArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, connectedArray);
        connected_list.setAdapter(connectedArrayAdapter);

        //if device is currently connected, display deice on the connected device list
        if(MainApplication.getBTConnection().isConnected()){
            String deviceName = MainApplication.getBTConnection().getDeviceName();
            String listItem = null;
            for(int i = 0; i < pairedArray.size(); i++){
                if(pairedArray.get(i).contains(deviceName)){
                    listItem = pairedArray.get(i);
                    pairedArray.remove(i);
                    pairedArrayAdapter.notifyDataSetChanged();
                }
            }
            if(listItem != null){
                //update connected_list
                connectedArray.add(listItem);
                connectedArrayAdapter.notifyDataSetChanged();
            }

        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //unregister all receivers
        try{
            getContext().unregisterReceiver(discoverFinishReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no discoverFinishReceiver registered");
        }
        try{
            getContext().unregisterReceiver(pairingReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no pairingReceiver registered");
        }
        try{
            getContext().unregisterReceiver(discoverReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no discoverReceiver registered");
        }
        try{
            getContext().unregisterReceiver(bluetoothReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no bluetoothReceiver registered");
        }
        try{
            getContext().unregisterReceiver(BTConnectReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no bluetoothReceiver registered");
        }
        try{
            getContext().unregisterReceiver(reconnectedReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no reconnectedReceiver registered");
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d(TAG, "resume: MainActivity");
        if(MainApplication.getBTConnection() != null){
            MainApplication.setCurrentActivity(getActivity());
        }
    }

    public void startConnection(BluetoothDevice device){
        manualConnecting = true;
        if(MainApplication.getBTConnection() == null){
            MainApplication.initializeBTConnection(getActivity());
        }
        mBTDevice = device;
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        MainApplication.getBTConnection().startClient(device, uuid);
    }

    public void createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
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

    private void updateListViews(Object obj, int type){
        String targetString;
        switch(type){
            case DEVICEPAIRED:
                BluetoothDevice btDevice = (BluetoothDevice) obj;

                //find bluetooth device from list
                targetString = btDevice.getName() + "\n" + btDevice.getAddress();
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
                break;
            case CONNECTIONSUCCESS:
                targetString = (String) obj;
                //move connected device up
                for(int i = 0; i < pairedArray.size(); i++){
                    Log.d(TAG, String.valueOf(pairedArray.get(i).compareTo(targetString) == 0));
                    if(pairedArray.get(i).compareTo(targetString) == 0){
                        Log.d(TAG, pairedArray.get(i));
                        pairedArray.remove(i);
                        pairedArrayAdapter.notifyDataSetChanged();
                    }
                }

                if(connectedArray.size() > 0){
                    //remove previous list item if there is 1 device already
                    connectedArray.clear();
                }
                connectedArray.add(targetString);
                connectedArrayAdapter.notifyDataSetChanged();
                break;
            case DISCONNECTED:
                String current_connected = connectedArray.get(0);

                //move connected device back to paired
                pairedArray.add(current_connected);
                pairedArrayAdapter.notifyDataSetChanged();

                connectedArray.clear();
                connectedArrayAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }

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
                    BluetoothDevice connect_device = mBluetoothAdapter.getRemoteDevice(address);
                    mBTDevice = connect_device;
                    startConnection(connect_device);
                }
            });
        }
    }

    public void disableUI() {
        discover_button.setEnabled(false);
        ListView deviceList = (ListView) v.findViewById(R.id.device_list1);
        deviceList.setEnabled(false);
        mBluetoothAdapter.cancelDiscovery();
        deviceArray.clear();
        deviceArrayAdapter.notifyDataSetChanged();
        pairedArray.clear();
        pairedArrayAdapter.notifyDataSetChanged();
    }

    public void enableUI() {
        discover_button.setEnabled(true);
        ListView deviceList = (ListView) v.findViewById(R.id.device_list1);
        deviceList.setEnabled(true);

        //reload paired array
        displayPairedDevices();
    }

    //method to re-enable listView buttons based on type
    public void reenableList(String address, int type){
        ListView target = null;
        switch(type){
            //unpaired list
            case DEVICE_LIST:
                target = device_list;
                break;
            case PAIRED_LIST:
                target = paired_list;
                break;
            case CONNECTED_LIST:
                target = connected_list;
                break;
            default:
                break;
        }

        try{
            //enable all buttons
            for(int i = 0; i < target.getAdapter().getCount(); i++){
                if(!target.getChildAt(i).isEnabled()){
                    target.getChildAt(i).setEnabled(true);
                }
            }
        }catch(Exception e){
            Log.e(TAG, "reenableList: Error re-enabling ListView");
        }
    }

    //method to disconnect from bluetooth device
    public void disconnectDevice(){
        //stop discovering
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: Disconnecting with device");

        MainApplication.getBTConnection().closeConnection();

        //add device back into paired list
        String listItem = connectedArray.get(0);
        connectedArray.remove(0);
        connectedArrayAdapter.notifyDataSetChanged();

        pairedArray.add(listItem);
        pairedArrayAdapter.notifyDataSetChanged();

        madeConnection = false;

        Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }
}
