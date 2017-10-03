package com.mdp;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothFragment extends Fragment {
    //paired list
    private ArrayAdapter<String> pairedArrayAdapter;
    private ListView paired_list;
    private ArrayList<String> pairedArray = new ArrayList<String>();

    private BluetoothAdapter mBluetoothAdapter;

    BluetoothDevice mBTDevice;


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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getContext(), "Bluetooth is not supported on this device, please use another device", Toast.LENGTH_LONG).show();
        }

        //find all paired device and display them
        paired_list = (ListView) view.findViewById(R.id.paired_list);
        pairedArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, pairedArray);
        paired_list.setAdapter(pairedArrayAdapter);

        displayPairedDevices();
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
                    Log.d("Bluetooth", address);
                    BluetoothDevice connect_device = mBluetoothAdapter.getRemoteDevice(address);
                    mBTDevice = connect_device;
                    //startConnection(connect_device);
                }
            });
        }
    }

}
