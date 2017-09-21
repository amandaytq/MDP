package com.mdp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView connect_text;

    private boolean hasBT = false;

    private Button forward_btn;
    private Button left_btn;
    private Button right_btn;

    private static String command_forward = "f";
    private static String command_right = "tr";
    private static String command_left = "tl";

    private MapHandler mapHandler;

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

        Button test = (Button) findViewById(R.id.btn2);
        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(((EditText)findViewById(R.id.et1)).getText().toString());
                int y = Integer.parseInt(((EditText)findViewById(R.id.et2)).getText().toString());
                mapHandler.setStartPoint(x, y);
            }
        });

        connect_text = (TextView) findViewById(R.id.connect_text);

        forward_btn = (Button) findViewById(R.id.btn_top);
        forward_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapHandler.moveFront();
            }
        });
        left_btn = (Button) findViewById(R.id.btn_left);
        left_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {mapHandler.setDirection(mapHandler.LEFT);
            }
        });
        right_btn = (Button) findViewById(R.id.btn_right);
        right_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { mapHandler.setDirection(mapHandler.RIGHT);
            }
        });

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

    }

    public void sendCommand(String s){
        byte[] bytes = s.getBytes(Charset.defaultCharset());
        MainApplication.getBTConnection().write(bytes);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "start: MainActivity");
        handleBluetooth();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        Log.d(TAG, "destroy: MainActivity");
    }

    public void handleBluetooth(){
        if(MainApplication.getBTConnection() != null){
            if(MainApplication.getBTConnection().isConnected() != false) {
                hasBT = true;
                connect_text.setText("Connected to: " + MainApplication.getBTConnection().getDeviceName());
            }
            else{
                hasBT = false;
                Log.d(TAG, "BTConnection: Not Connected");
                connect_text.setText("Not Connected");
            }
        }
        else{
            hasBT = false;
            Log.d(TAG, "BTConnection: null");
            connect_text.setText("Not Connected");
        }
    }

    public void enterBluetooth(View view){
        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }


}
