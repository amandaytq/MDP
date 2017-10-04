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

    private static final String command_explore = "exploration";
    private static final String command_stop_explore = "stopexploration";

    private static final String command_shortest_path = "shortestpath";
    private static final String command_stop_shortest_path = "stopshortestpath";

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
                break;
            case "back":
                //move backwards
                sendCommand(ard_protocol, command_back);
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
                break;
            case "right":
                //turn right
                sendCommand(ard_protocol, command_turn_right);
                break;
            default:
                break;
        }
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

    public void requestMapData(){
        sendCommand(alg_protocol, command_request_map);
    }

    public void beginExplore(){
        sendCommand(alg_protocol, command_explore);
    }
    public void stopExplore(){
        sendCommand(alg_protocol, command_stop_explore);
    }

    public void beginShortestPath(){
        sendCommand(alg_protocol, command_shortest_path);
    }
    public void stopShortestPath(){
        sendCommand(alg_protocol, command_stop_shortest_path);
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
