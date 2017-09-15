package com.mdp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by dorae on 9/14/2017.
 */

public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();;
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

    public static BluetoothConnectionService getBTConnection(){
        return mBluetoothConnection;
    }
}
