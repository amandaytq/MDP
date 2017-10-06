package com.mdp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by dorae on 9/12/2017.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MDP";

    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private Activity currentActivity;

    private String deviceName = "";

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    private boolean selfClosed = false;

    public BluetoothConnectionService(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnectThread = null;
        start();
    }

    public void setCurrentActivity(Activity activity){
        currentActivity = activity;
    }

    public boolean isConnected(){
        if(mConnectedThread != null)
        {
            return mConnectedThread.isConnected();
        }
        else{
            return false;
        }
    }

    public String getDeviceName(){
        return deviceName;
    }

    /* This thread runs while attempting to make an outgoing connection with a device */
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID input_uuid){
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceName = device.getName();
            deviceUUID = input_uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            //Get a BluetoothSocket for a connection with the given Bluetooth Device
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID");
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            //always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                //This is a locking call and will only return on a
                //successful connection or an exception
                mmSocket.connect();

                //send intent to current Activity that connection is successful
                Log.d(TAG, "Sending Intent");
                Intent intent = new Intent();
                intent.setAction(MainApplication.connectionSuccessCommand);
                intent.putExtra("deviceId", mmDevice.getName() + "\n" + mmDevice.getAddress());
                currentActivity.sendBroadcast(intent);

                Log.d(TAG, "run: ConnectThread connected.");
            }catch(IOException e){
                //close the socket
                try{
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket");
                }catch(IOException e1){
                    Log.d(TAG, "mConnectThread: run: Unable to close connection in socket "+ e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + uuid);

                //send intent to current Activity that connection is unsuccessful
                Log.d(TAG, "Sending Intent");
                Intent intent = new Intent();
                intent.setAction(MainApplication.connectionFailCommand);
                intent.putExtra("address", mmDevice.getAddress());
                currentActivity.sendBroadcast(intent);
            }

            connected(mmSocket, mmDevice);
        }
        public void cancel(){
            try{
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            }catch(IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    /*Start the service*/
    public synchronized void start(){
        Log.d(TAG, "start");

        //cancel any thread attempting to make a connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        /*if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }*/
    }

    /*
    AcceptThread starts and sits waiting for a connection.
    Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread
    */
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started");

        //if it is connected, close the previous connection
        if(mConnectedThread != null){
            if(mConnectedThread.isConnected()){
                mConnectThread.cancel();
                mConnectedThread.cancel();
            }
        }

        //initProgress dialog
        mProgressDialog = ProgressDialog.show(currentActivity, "Connecting Bluetooth", "Please Wait......", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: Starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressDialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            try{
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }catch(IOException e){
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024]; //buffer store for the stream

            int bytes; //bytes returned from read()

            //keep listening to the InputStream until an Exception occurs
            while(true) {
                //read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    //send command
                    Intent intent = new Intent();
                    intent.setAction(MainApplication.receiveCommand);
                    intent.putExtra("command", incomingMessage);
                    currentActivity.sendBroadcast(intent);

                } catch (IOException e) {
                    Log.e(TAG, "read: Error Reading InputStream");
                    cancel();
                    break;
                }
            }
        }

        //call this from the MainActivity to send data to the remote device
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputStream: " + text);
            try{
                mmOutStream.write(bytes);
            }catch(IOException e){
                Log.e(TAG, "write: Error writing to outputStream: " + e.getMessage());
            }
        }

        //Call this to confirm if there is a connection or not
        public boolean isConnected(){
            return mmSocket.isConnected();
        }

        //Call this from the main activity to shutdown the connection
        public void cancel(){
            try{
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice){
        Log.d(TAG, "connected: Starting");

        //start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void closeConnection(){
        mConnectedThread.cancel();
        mConnectedThread = null;
        mConnectThread.cancel();
        mConnectThread = null;

        selfClosed = true;
    }

    /*
        Write to the ConnectedThread in an unsynchornized manner
     */
    public void write(byte[] out){
        //create temp obj
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: write called");
        //perform the write
        mConnectedThread.write(out);
    }

    public void handleDisconnection(){
        if (!selfClosed){
            Log.d(TAG, "mBluetoothConnection: handle disconnection");
            mConnectedThread.cancel();
            //rerun connectThread
            mConnectThread.cancel();
            mConnectThread = null;
            mConnectThread = new ConnectThread(mmDevice, uuid);
            mConnectThread.run();
        }
        else{
            return;
        }
    }
}
