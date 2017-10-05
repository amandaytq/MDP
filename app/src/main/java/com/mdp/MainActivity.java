package com.mdp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView connect_text;

    private Button forward_btn;
    private Button back_btn;
    private Button top_btn;
    private Button bottom_btn;
    private Button left_btn;
    private Button right_btn;

    private Button turn_left_btn;
    private Button turn_right_btn;

    private TextView status_text;
    //ROBOT STATUS
    public static final String status_idle = "Idle";
    public static final String status_moving = "Moving";
    public static final String status_turning = "Turning";

    private Button f1_call;
    private Button f2_call;

    private Button explore_btn;
    private Button shortest_path_btn;

    private Switch auto_switch;
    private Button update_map_btn;
    public boolean auto_enabled = false;
    public boolean map_requested = false;

    public static MapHandler mapHandler;
    public ReceiveHandler receiveHandler;
    public SendHandler sendHandler;

    private int count = 0;

    //for Joy stick
    RelativeLayout layout_joystick;
    JoyStickClass js;

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
        final GridView mapgv = (GridView) findViewById(R.id.mapgv);
        mapgv.setAdapter(new ImageAdapter(this));

        final GridView pathgv = (GridView) findViewById(R.id.pathgv);
        pathgv.setAdapter(new MapImageAdapter(this));

        final GridView obsgv = (GridView) findViewById(R.id.obsgv);
        obsgv.setAdapter(new ImageObsAdapter(this));

        final GridView robotgv = (GridView) findViewById(R.id.robotgv);
        robotgv.setAdapter(new ImageAdapter(this));

        mapHandler = new MapHandler(mapgv, pathgv, obsgv, robotgv, this);

        connect_text = (TextView) findViewById(R.id.connect_text);

        //init control buttons
//        forward_btn = (Button) findViewById(R.id.btn_forward);
//        forward_btn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                sendHandler.move("forward");
//                //move bot on android device
//                if(mapHandler.botSet()){
//                    mapHandler.moveFront();
//                }
//            }
//        });
//        back_btn = (Button) findViewById(R.id.btn_back);
//        back_btn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                sendHandler.move("back");
//                //move bot on android device
//                if(mapHandler.botSet()){
//                    //mapHandler.moveBack();
//                }
//            }
//        });

        turn_left_btn = (Button) findViewById(R.id.btn_turn_left);
        turn_left_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mapHandler.botSet()){
                    sendHandler.turn("left");
                    updateStatusText(status_turning);
                    mapHandler.setRotatedDirection(mapHandler.LEFT);
                }
            }
        });
        turn_right_btn = (Button) findViewById(R.id.btn_turn_right);
        turn_right_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mapHandler.botSet()){
                    sendHandler.turn("right");
                    updateStatusText(status_turning);
                    mapHandler.setRotatedDirection(mapHandler.RIGHT);
                }
            }
        });
        left_btn = (Button) findViewById(R.id.btn_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(mapHandler.botSet()){
                    sendHandler.position(mapHandler.WEST);
                    updateStatusText(status_turning);
                    mapHandler.setDirection(mapHandler.WEST);
                }
            }
        });
        right_btn = (Button) findViewById(R.id.btn_right);
        right_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(mapHandler.botSet()){
                    sendHandler.position(mapHandler.EAST);
                    updateStatusText(status_turning);
                    mapHandler.setDirection(mapHandler.EAST);
                }

            }
        });
        top_btn = (Button) findViewById(R.id.btn_top);
        top_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(mapHandler.botSet()){
                    sendHandler.position(mapHandler.NORTH);
                    mapHandler.setDirection(mapHandler.NORTH);
                }
            }
        });
        bottom_btn = (Button) findViewById(R.id.btn_btm);
        bottom_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mapHandler.botSet()){
                    sendHandler.position(mapHandler.SOUTH);
                    updateStatusText(status_turning);
                    mapHandler.setDirection(mapHandler.SOUTH);
                }
            }
        });

        status_text = (TextView) findViewById(R.id.status_text);

        //init BTConnection from MainApplication
        if(MainApplication.getBTConnection() == null){
            MainApplication.initializeBTConnection(MainActivity.this);
        }

        final Button setwp_btn = (Button)findViewById(R.id.set_wp);
        final Button setsp_btn = (Button)findViewById(R.id.set_sp);

        setwp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setwp_btn.isActivated()){
                    setwp_btn.setText("Set WP");
                    setwp_btn.setActivated(false);
                    setsp_btn.setEnabled(true);
                    robotgv.setOnItemClickListener(null);
                }else{
                    setwp_btn.setText("cancel");
                    setwp_btn.setActivated(true);
                    setsp_btn.setEnabled(false);
                    robotgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            int[] coord = mapHandler.getCoordinates(position);
                            mapHandler.alertView(setwp_btn,setsp_btn, "Confirm set way point?", "Are you sure that you want to set \n " +
                                    "the point ("+coord[0]+", "+coord[1]+") as your way point?",true,mapHandler.SETWP);
                            mapHandler.poswp = position;
                        }
                    });
                }
            }
        });

        setsp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setsp_btn.isActivated()){
                    setsp_btn.setText("Set Start");
                    setsp_btn.setActivated(false);
                    setwp_btn.setEnabled(true);
                    robotgv.setOnItemClickListener(null);
                }else{
                    setsp_btn.setText("cancel");
                    setwp_btn.setEnabled(false);
                    setsp_btn.setActivated(true);
                    robotgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            int[] coord = mapHandler.getCoordinates(position);
                            mapHandler.alertView(setsp_btn, setwp_btn, "Confirm set start point?", "Are you sure that you want to set \n " +
                                    "the point ("+coord[0]+", "+coord[1]+") as your start point?",true,mapHandler.SETSP);
                            mapHandler.possp = position;
                        }
                    });
                }
            }
        });

        //setup function buttons
        f1_call = (Button) findViewById(R.id.f1_call);
        f1_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHandler.sendFunction(1);
            }
        });

        f2_call = (Button) findViewById(R.id.f2_call);
        f2_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHandler.sendFunction(2);
            }
        });

        //setup explore & SP buttons
        explore_btn = (Button) findViewById(R.id.explore);
        explore_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendHandler.beginExplore();
            }
        });
        shortest_path_btn = (Button) findViewById(R.id.shortest_path);
        shortest_path_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendHandler.beginShortestPath();
            }
        });

        //init auto switch
        update_map_btn = (Button) findViewById(R.id.btn_request_map);
        update_map_btn.setEnabled(false);
        update_map_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!auto_enabled){
                    map_requested = true;
                    sendHandler.requestMapData();
                }
            }
        });

        auto_switch = (Switch) findViewById(R.id.auto_switch);
        auto_switch.setChecked(true);
        auto_enabled = true;
        auto_switch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(auto_switch.isChecked()){
                    auto_enabled = true;
                    //disable update map
                    update_map_btn.setEnabled(false);
                    map_requested = false;
                }
                else{
                    auto_enabled = false;
                    update_map_btn.setEnabled(true);
                    map_requested = false;
                }
            }
        });

        //init receiveHandler & sendHandler
        receiveHandler = new ReceiveHandler(MainActivity.this);
        sendHandler = new SendHandler(MainActivity.this);

        registerReceiver(commandReceiver, new IntentFilter(MainApplication.receiveCommand));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        registerReceiver(disconnectReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        //Joystick
        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext(), layout_joystick, R.drawable.image_button);
        js.setStickSize(80, 80);
        js.setLayoutSize(210, 210);
        js.setLayoutAlpha(150);
        js.setStickAlpha(130);
        js.setOffset(45);
        js.setMinimumDistance(20);
        js.stickOriginalPos();
        js.draw();

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {

                    if(mapHandler.botSet()) {
                        count++;
                        int direction = js.get4Direction();
                        if (direction == JoyStickClass.STICK_UP) {
                            //upwards
                            //sendHandler.move("forward");
                            if (count / 5 == 0)
                                mapHandler.move(mapHandler.UP);

                        } else if (direction == JoyStickClass.STICK_DOWN) {
                            //downwards
                            // sendHandler.move("back");
                            if (count / 2 == 0)
                                mapHandler.move(mapHandler.DOWN);
                        }
                    }

                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    //when it leaves up
                    js.stickOriginalPos();
                    count = 0;


                }
                return true;
            }
        });
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
        if(MainApplication.getBTConnection().isConnected()){
            MainApplication.getBTConnection().closeConnection();
        }
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
    }
    public void disableControls(){
        Log.d(TAG, "disableControls: called");
    }
}
