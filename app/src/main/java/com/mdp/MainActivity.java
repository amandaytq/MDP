package com.mdp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView connect_text;

    private Button forward_btn;
    private Button left_btn;
    private Button right_btn;

    private TextView status_text;
    private boolean isExploring = false;

    private static String command_forward = "f";
    private static String command_right = "tr";
    private static String command_left = "tl";

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private String f1Command = "";
    private String f2Command = "";
    private EditText f1_text;
    private EditText f2_text;
    private Button f1_call;
    private Button f2_call;
    private Button f1_save;
    private Button f2_save;

    private Switch auto_switch;
    public static MapHandler mapHandler;
    private ReceiveHandler receiveHandler;
    private SendHandler sendHandler;

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                    handleBluetooth();
                    break;
            }
        }
        }
    };

    //broadcast receiver to handle command receiving
    private BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(MainApplication.receiveCommand)) {
                String command = intent.getStringExtra("command");
                Log.d(TAG, "commandReceiver: Command " + command + " received.");
                receiveHandler.received(command);
            }
        }
    };

    //broadcast receiver to handle disconnection
    /*private BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };*/

    private BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG, "disconnectReceiver: Received Disconnect Notice");
                Toast.makeText(getBaseContext(), "Device disconnected, attempting to reconnect....", Toast.LENGTH_SHORT).show();
                MainApplication.handleDisconnect();
                handleBluetooth();
                //register receiver to listen when reconnection is made
                registerReceiver(reconnectedReceiver, new IntentFilter(MainApplication.reconnectedCommand));
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

                //unregister receiver
                unregisterReceiver(reconnectedReceiver);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "create: MainActivity");

        //define variables
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        final GridView mapgridview = (GridView) findViewById(R.id.mapgridview);
        mapgridview.setAdapter(new MapImageAdapter(this));
        mapHandler = new MapHandler(gridview, mapgridview, this);

        connect_text = (TextView) findViewById(R.id.connect_text);

        forward_btn = (Button) findViewById(R.id.btn_top);
        forward_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                sendCommand(command_forward);
                if(mapHandler.botSet()){
                    mapHandler.moveFront();
                }
            }
        });
        left_btn = (Button) findViewById(R.id.btn_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(command_left);
                if(mapHandler.botSet()){
                    mapHandler.setRotatedDirection(mapHandler.LEFT);
                }
            }
        });
        right_btn = (Button) findViewById(R.id.btn_right);
        right_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand(command_right);
                if(mapHandler.botSet()){
                    mapHandler.setRotatedDirection(mapHandler.RIGHT);
                }

            }
        });

        status_text = (TextView) findViewById(R.id.status_text);

        //init BTConnection from MainApplication
        if(MainApplication.getBTConnection() == null){
            MainApplication.initializeBTConnection(MainActivity.this);
        }

        Button set_obs = (Button) findViewById(R.id.set_obs);
        set_obs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(((EditText)findViewById(R.id.obsx)).getText().toString());
                int y = Integer.parseInt(((EditText)findViewById(R.id.obsy)).getText().toString());
                mapHandler.setObs(x,y);
            }
        });

        Button rmv_obs = (Button) findViewById(R.id.rmv_obs);
        rmv_obs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(((EditText)findViewById(R.id.obsx)).getText().toString());
                int y = Integer.parseInt(((EditText)findViewById(R.id.obsy)).getText().toString());
                mapHandler.removeObs(x,y);
            }
        });

        final Button setwp_btn = (Button)findViewById(R.id.set_wp);
        setwp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setwp_btn.isActivated()){
                    setwp_btn.setText("Set WP");
                    setwp_btn.setActivated(false);
                    mapgridview.setOnItemClickListener(null);
                }else{
                    setwp_btn.setText("cancel");
                    setwp_btn.setActivated(true);
                    mapgridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            int[] coord = mapHandler.getCoordinates(position);
                            mapHandler.alertView(setwp_btn, "Confirm set way point?", "Are you sure that you want to set \n " +
                                    "the point ("+coord[0]+", "+coord[1]+") as your way point?",true,mapHandler.SETWP);
                            mapHandler.poswp = position;
                        }
                    });
                }
            }
        });

        final Button setsp_btn = (Button)findViewById(R.id.set_sp);
        setsp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setsp_btn.isActivated()){
                    setsp_btn.setText("Set Start");
                    setsp_btn.setActivated(false);
                    mapgridview.setOnItemClickListener(null);
                }else{
                    setsp_btn.setText("cancel");
                    setsp_btn.setActivated(true);
                    mapgridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            int[] coord = mapHandler.getCoordinates(position);
                            mapHandler.alertView(setsp_btn, "Confirm set start point?", "Are you sure that you want to set \n " +
                                    "the point ("+coord[0]+", "+coord[1]+") as your start point?",true,mapHandler.SETSP);
                            mapHandler.possp = position;
                        }
                    });
                }
            }
        });

        //setup function buttons
        mPref = getSharedPreferences("storedCommand", 0);
        mEditor = mPref.edit();

        f1_text = (EditText) findViewById(R.id.f1_text);
        f1_save = (Button)findViewById(R.id.f1_save);
        f1_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCommand = f1_text.getText().toString();

                Log.d(TAG, "onClick: Saving new Command F1: " + newCommand);

                mEditor.putString("f1", newCommand).commit();
                f1Command = newCommand;
                Toast.makeText(getBaseContext(), "Save Successful", Toast.LENGTH_SHORT).show();
            }
        });
        f1_call = (Button) findViewById(R.id.f1_call);
        f1_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Sending Command F1: " + f1Command);

                sendCommand(f1Command);
            }
        });

        f2_text = (EditText) findViewById(R.id.f2_text);
        f2_save = (Button)findViewById(R.id.f2_save);
        f2_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCommand = f2_text.getText().toString();

                Log.d(TAG, "onClick: Saving new Command F2: " + newCommand);

                mEditor.putString("f2", newCommand).commit();
                f2Command = newCommand;
                Toast.makeText(getBaseContext(), "Save Successful", Toast.LENGTH_SHORT).show();
            }
        });
        f2_call = (Button) findViewById(R.id.f2_call);
        f2_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Sending Command F2: " + f2Command);

                sendCommand(f2Command);
            }
        });

        //init auto switch
        auto_switch = (Switch) findViewById(R.id.auto_switch);

        //init receiveHandler & sendHandler
        receiveHandler = new ReceiveHandler(MainActivity.this);
        sendHandler = new SendHandler(MainActivity.this);

        registerReceiver(commandReceiver, new IntentFilter(MainApplication.receiveCommand));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        registerReceiver(disconnectReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    public void sendCommand(String s){
        if(MainApplication.getBTConnection().isConnected()){
            byte[] bytes = s.getBytes(Charset.defaultCharset());
            MainApplication.getBTConnection().write(bytes);
        }
        else{
            Toast.makeText(getBaseContext(), "Bluetooth Connection not established", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "start: MainActivity");
        handleBluetooth();
    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.d(TAG, "resume: MainActivity");

        f1Command = mPref.getString("f1", "");
        f2Command = mPref.getString("f2", "");

        f1_text.setText(f1Command);
        f2_text.setText(f2Command);

        //reregister receivers
        registerReceiver(commandReceiver, new IntentFilter(MainApplication.receiveCommand));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(disconnectReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        if(MainApplication.getBTConnection() != null){
            MainApplication.setCurrentActivity(MainActivity.this);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        unregisterReceiver(commandReceiver);
        unregisterReceiver(bluetoothReceiver);
        unregisterReceiver(disconnectReceiver);
        try{
            unregisterReceiver(reconnectedReceiver);
        }catch(Exception e){
            Log.d(TAG, "There is no reconnectedReceiver registered");
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        Log.d(TAG, "destroy: MainActivity");
    }

    public void handleBluetooth(){
        if(MainApplication.getBTConnection() != null){
            if(MainApplication.getBTConnection().isConnected() != false) {
                connect_text.setText(MainApplication.getBTConnection().getDeviceName());
            }
            else{
                Log.d(TAG, "BTConnection: Not Connected");
                connect_text.setText("Not Connected");
            }
        }
        else{
            Log.d(TAG, "BTConnection: null");
            connect_text.setText("Not Connected");
        }
    }

    public void enterBluetooth(View view){
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }


    public void updateStatusText(String s){
        String substring = s.substring(1).toLowerCase();
        String status = s.substring(0, 1) + substring;
        Log.d(TAG, status);
        status_text.setText(status);
    }

    public void enterSettings(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public boolean isAuto(){
        return auto_switch.isChecked();
    }

    public void enableControls(){
        Log.d(TAG, "enableControls: called");
        //enable only if it is not exploring
        if(!isExploring){

        }
    }
    public void disableControls(){
        Log.d(TAG, "disableControls: called");
    }

    public void setExploring(Boolean b){
        isExploring = b;
        if (!b){
            enableControls();
        }
    }

    public void overrideControl(){
        //force back control of controls
        isExploring = false;
        enableControls();
    }
}
