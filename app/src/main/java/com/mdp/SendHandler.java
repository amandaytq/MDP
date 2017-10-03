package com.mdp;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;

public class SendHandler {

    private static final String alg_protocol = "A";
    private static final String and_protocol = "B";
    private static final String ard_protocol = "C";

    private static final String command_forward = "w";
    private static final String command_turn_right = "d";
    private static final String command_turn_left = "a";
    private static final String command_back = "s";

    private static final String command_request_map = "map";

    private static final String command_explore = "explore";

    private static final String command_send_waypoint = "waypoint";
    private static final String command_send_startpoint = "startpoint";
    private static final String command_send_endpoint = "endpoint";

    private static final String command_turn_angle = "2";

    private MainActivity ma;

    public SendHandler(MainActivity a){
        ma = a;
    }

    public void move(String movement){
        switch(movement){
            case "forward":
                //move forward
                sendCommand(ard_protocol, command_forward);
                //move bot on android device
                if(ma.mapHandler.botSet()){
                    ma.mapHandler.moveFront();
                }
                break;
            case "back":
                //move backwards
                sendCommand(ard_protocol, command_back);
                //move bot on android device
                if(ma.mapHandler.botSet()){
                    //ma.mapHandler.moveBack();
                }
                break;
            default:
                break;
        }
    }

    public void turn(String direction){
        switch(direction){
            case "left":
                //turn left
                sendCommand(ard_protocol, command_turn_left);
                //move bot on android device
                if(ma.mapHandler.botSet()){
                    ma.mapHandler.setRotatedDirection(ma.mapHandler.LEFT);
                }
                break;
            case "right":
                //turn right
                sendCommand(ard_protocol, command_turn_right);
                //move bot on android device
                if(ma.mapHandler.botSet()){
                    ma.mapHandler.setRotatedDirection(ma.mapHandler.RIGHT);
                }
                break;
            default:
                break;
        }
    }

    public void position(int position){
        //determine direction to turn
        int turn = ma.mapHandler.determineTurn(position);
        if(ma.mapHandler.botSet()){
            ma.mapHandler.setDirection(position);
        }
        int wise = 1;
        if(turn < 0){
            wise = 0;
        }
        turn = Math.abs(turn);
        Log.d("SendHandler", command_turn_angle + "," + turn + "," + wise);
        sendCommand(ard_protocol, command_turn_angle + "," + turn + "," + wise);
    }

    public void requstMapData(){
        sendCommand(alg_protocol, command_request_map);
    }

    public void beginExplore(){
        sendCommand(alg_protocol, command_explore);
    }

    public void sendWayPoint(int x, int y) {
        sendCommand(alg_protocol, command_send_waypoint + "x:y");
    }

    public void sendStartPoint(int x, int y) {
        sendCommand(alg_protocol, command_send_startpoint + "x:y");
    }

    public void sendEndPoint(int x, int y) {
        sendCommand(alg_protocol, command_send_endpoint + "x:y");
    }

    public void sendFunction(int function){
        //get function text
        SharedPreferences mPref = ma.getSharedPreferences("sendCommand", 0);
        String functionText = mPref.getString("f" + function, "");

        sendCommand("", functionText);
    }

    private void sendCommand(String protocol, String command){
        String toBeSent = and_protocol+protocol+command;
        if(MainApplication.getBTConnection().isConnected()){
            byte[] bytes = toBeSent.getBytes(Charset.defaultCharset());

            MainApplication.getBTConnection().write(bytes);
        }
        else{
            Toast.makeText(ma.getBaseContext(), "Bluetooth Connection not established", Toast.LENGTH_SHORT).show();
        }

    }

}
