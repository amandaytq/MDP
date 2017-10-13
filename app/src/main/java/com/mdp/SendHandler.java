package com.mdp;

import android.content.Context;
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

    private static final String command_turn_back = "2,180,1";

    private static final String command_explore = "exploration";

    private static final String command_shortest_path = "shortestpath";

    private static final String command_send_waypoint = "waypoint";
    private static final String command_send_startpoint = "startpoint";
    private static final String command_send_endpoint = "endpoint";

    private static final String command_forward_joy = "o";
    private static final String command_stop_joy = "p";

    private static final String command_turn_angle = "2";

    private static final String command_calibrate_right = "5";
    private static final String command_calibrate_left = "6";
    private static final String command_calibrate_front = "7";

    private static final String [] calibrate_process = {"turn", "front", "turn", "right"};

    private MainActivity ma;

    public SendHandler(MainActivity a){
        ma = a;
    }

    public void move(String movement){

        switch(movement){
            case "forward":
                //move forward
                sendCommand(ard_protocol, command_forward);
                break;
            case "back":
                //move backwards
                sendCommand(ard_protocol, command_back);
                break;
            default:
                break;
        }
        ma.updateStatusText(ma.status_moving);
    }

    public void moveJoy(String movement){

        switch(movement){
            case "move":
                //move forward
                sendCommand(ard_protocol, command_forward_joy);
                ma.updateStatusText(ma.status_moving);
                break;
            case "stop":
                //move backwards
                sendCommand(ard_protocol, command_stop_joy);
                ma.updateStatusText(ma.status_idle);

                break;
            default:
                break;
        }
        ma.updateStatusText(ma.status_moving);
    }

    public void turn(String direction){
        switch(direction){
            case "left":
                //turn left
                sendCommand(ard_protocol, command_turn_left);
                break;
            case "right":
                //turn right
                sendCommand(ard_protocol, command_turn_right);
                break;
            case "back":
                //turn back
                sendCommand(ard_protocol, command_turn_back);
                break;
            default:
                break;
        }
        ma.updateStatusText(ma.status_turning);
    }

    public void position(int position){
        //determine direction to turn
        int turn = ma.mapHandler.determineTurn(position);

        int wise = 1;
        if(turn < 0){
            wise = 0;
        }
        turn = Math.abs(turn);
        Log.d("SendHandler", command_turn_angle + "," + turn + "," + wise);
        sendCommand(ard_protocol, command_turn_angle + "," + turn + "," + wise);
    }

    public void beginExplore(){
        sendCommand(alg_protocol, command_explore);
    }

    public void beginShortestPath(){
        sendCommand(alg_protocol, command_shortest_path);
    }

    public void sendWayPoint(int x, int y) {
        sendCommand(alg_protocol, command_send_waypoint + ":"+x+":"+y);
    }

    public void sendStartPoint(int x, int y) {
        sendCommand(alg_protocol, command_send_startpoint + ":"+x+":"+y);
    }

    public void sendEndPoint(int x, int y) {
        sendCommand(alg_protocol, command_send_endpoint + ":"+x+":"+y);
    }

    public void sendFunction(int function){
        //get function text
        SharedPreferences mPref = ma.getSharedPreferences("sendCommand", 0);
        String functionText = mPref.getString("f" + function, "");

        //Toast.makeText(getBaseContext(), "Sending F"+function+" Command: "+ functionText).show();

        sendCommand("", functionText);
    }

    public void sendCalibration(String direction){
        switch(direction){
            case "front":
                sendCommand(ard_protocol, command_calibrate_front);
                break;
            case "left":
                sendCommand(ard_protocol, command_calibrate_left);
                break;
            case "right":
                sendCommand(ard_protocol, command_calibrate_right);
                break;
        }
        ma.updateStatusText("Calibrating");
    }

    public boolean calibrationProcess(int sequence){
        if (sequence > calibrate_process.length - 1){
            return false;
        }

        switch(calibrate_process[sequence]){
            case "turn":
                sendCommand(ard_protocol, command_turn_back);
                break;
            case "front":
                sendCommand(ard_protocol, command_calibrate_front);
                break;
            case "right":
                sendCommand(ard_protocol, command_calibrate_right);
                break;
        }

        return true;
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
