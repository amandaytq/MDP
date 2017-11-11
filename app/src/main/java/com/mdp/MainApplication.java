package com.mdp;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
/*
* MDP Team 13
* @version 1.0
* @author Amanda Chan	<achan016@e.ntu.edu.sg>
* @author Tan Peng Hian <ptan023@e.ntu.edu.sg>
*/
public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();;
    public static final String receiveCommand = "com.mdp.RECEIVECOMMAND";
    public static final String connectionSuccessCommand = "com.mdp.CONNECTIONSUCCESSFUL";
    public static final String connectionFailCommand = "com.mdp.CONNECTIONFAIL";
    public static final String reconnectedCommand = "com.mdp.RECONNECTED";
    public static final String nextCalibration = "com.mdp.NEXTCALIBRATION";

    private static MainApplication sInstance;

    private static BluetoothConnectionService mBluetoothConnection = null;

    public static MainApplication getApplication(){
        return sInstance;
    }

    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "Init Called");
        sInstance = this;
    }

    public static void initializeBTConnection(Context mContext){
        mBluetoothConnection = new BluetoothConnectionService(mContext);
    }

    public static void setCurrentActivity(Activity a){
        mBluetoothConnection.setCurrentActivity(a);
    }

    public static BluetoothConnectionService getBTConnection(){
        return mBluetoothConnection;
    }

    public static void handleDisconnect(){
        mBluetoothConnection.handleDisconnection();
    };
}
