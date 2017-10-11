package com.mdp;

import android.util.Log;
import android.widget.Toast;

import java.math.BigInteger;

public class ReceiveHandler {
    private static final String TAG = ReceiveHandler.class.getSimpleName();

    private static final String status_protocol = "status";
    private static final String explore_protocol = "explore";
    private static final String turn_protocol = "2";
    private static final String calibrate_done_protocol = "1";

    private static final int north_protocol = 3;
    private static final int east_protocol = 0;
    private static final int south_protocol = 1;
    private static final int west_protocol = 2;

    private static final String command_completed = "-2";

    private static final String map_protocol = "map";
    private static final String position_protocol = "position";
    private static final String orientation_protocol = "orientation";

    private MainActivity ma;

    public ReceiveHandler(MainActivity a){
        ma = a;
    }

    public void received(String r){
        String [] s = r.split("::");
        String protocol = s[0];
        String parameters = "";
        if(s.length > 1){
            parameters = s[1];
        }
        parameters = parameters.split("\n")[0];
        //Arduino Protocol handling
        if(Character.isDigit(protocol.charAt(0))){
            String [] ard = protocol.split(",");
            String ard_protocol = ard[0];
            switch(ard_protocol){
                case turn_protocol:
                    //turn robot
                    if(ma.mapHandler.botSet()){
                        int angle = Integer.parseInt(ard[1]);
                        int wise = Integer.parseInt(ard[2]);
                        if(ma.mapHandler.botSet()){
                            int direction = ma.mapHandler.determineDirection(angle, wise);
                            ma.mapHandler.setDirection(direction);
                        }
                    }
                    break;
                case calibrate_done_protocol:
                    ma.updateStatusText(ma.status_idle);
                    Toast.makeText(ma.getBaseContext(), "Calibration Completed", Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
        else {


            switch (protocol) {
                case status_protocol:
                    //update status and perform the required actions
                    String[] s2 = parameters.split("//");
                    String status = s2[0];
                    Log.d(TAG, status);

                    ma.updateStatusText(status);
                    switch (status) {
                        default:
                            //update status text only
                            break;
                    }
                    break;
                case command_completed:
                    Log.d(TAG, "command_completed");
                    ma.updateStatusText(ma.status_idle);
                    break;
                case map_protocol:
                    updateMap(s[1]);
                    break;
                case position_protocol:
                    updatePosition(s[1]);
                    break;
                case orientation_protocol:
                    updateOrientation(s[1]);
                    break;
                default:
                    break;
            }
        }
    }

    private void updateMap(String hexa){
        //split map string into 2 required values
        String[] s = hexa.split(";;");
        String map1 = s[0].toUpperCase();
        String map2 = s[1].toUpperCase();

        String map1_s = hexToBinaryString(map1);
        String map2_s = hexToBinaryString(map2);

        int[][] scouted_arr = new int[20][15];
        int[][] obs_arr = new int[20][15];

        int xPos = 0;
        int yPos = 0;
        int map2_pos = 0;

        for (int i = 2; i < map1_s.length() - 2; i++) {
            if (map1_s.charAt(i) == '1') {
                //area is scouted, put into scouted array
                scouted_arr[yPos][xPos] = 1;
                if (map2_s.charAt(map2_pos) == '1') {
                    obs_arr[yPos][xPos] = 1;
                } else {
                    obs_arr[yPos][xPos] = 0;
                }
                //prevents app from crashing in the case where map2 loaded poorly
                if (map2_pos < map2_s.length()) {
                    map2_pos++;
                }
            } else {
                scouted_arr[yPos][xPos] = 0;
                obs_arr[yPos][xPos] = 0;
            }
            if (xPos >= 14) {
                xPos = 0;
                yPos++;
            } else {
                xPos++;
            }
        }

        //update info
        ma.mapHandler.updateInfo(hexa, obs_arr, scouted_arr, -1, -1);


        if(ma.auto_enabled){
            ma.mapHandler.updateMapUI();
        }
        //ma.mapHandler.setObsArr(obs_arr);
        //ma.mapHandler.setPath(scouted_arr);
    }

    private void updatePosition(String position){
        //split map string into 2 required values
        String[] s = position.split(";;");
        Log.d(TAG, s[0]);
        try {
            int robot_x = Integer.parseInt(s[0]);
            int robot_y = Integer.parseInt(s[1].replace("\n", ""));
            //update info
            ma.mapHandler.updateInfo(null, null, null, robot_x, robot_y);
            if(ma.auto_enabled) {
                ma.mapHandler.updateMapUI();
            }
        }
        catch(Exception e){
            return;
        }
    }

    private void updateOrientation(String orientation){
        try{
            int robot_orientation = Integer.parseInt(orientation.replace("\n", ""));
            int old_direction = ma.mapHandler.direction;

            switch(robot_orientation){
                case north_protocol:
                    ma.mapHandler.direction = ma.mapHandler.NORTH;
                    break;
                case east_protocol:
                    ma.mapHandler.direction = ma.mapHandler.EAST;
                    break;
                case south_protocol:
                    ma.mapHandler.direction = ma.mapHandler.SOUTH;
                    break;
                case west_protocol:
                    ma.mapHandler.direction = ma.mapHandler.WEST;
                    break;
                default:
                    break;
            }

            if(ma.mapHandler.direction != old_direction){
                Log.d(TAG, "Setting Turning Status");
                ma.updateStatusText(ma.status_turning);
            }

            //ma.mapHandler.direction= robot_orientation;

            //update info
            ma.mapHandler.updateInfo(null, null, null, -1, -1);
            if(ma.auto_enabled){
                ma.mapHandler.updateMapUI();
            }
        }
        catch(Exception e){
            return;
        }

    }

    //converts hexadecimal string into a binary string
    private static String hexToBinaryString(String data) {
        String returnString = "";
        for(int i = 0; i < data.length(); i++){
            switch(data.charAt(i)){
                case '0':
                    returnString += "0000";
                    break;
                case '1':
                    returnString += "0001";
                    break;
                case '2':
                    returnString += "0010";
                    break;
                case '3':
                    returnString += "0011";
                    break;
                case '4':
                    returnString += "0100";
                    break;
                case '5':
                    returnString += "0101";
                    break;
                case '6':
                    returnString += "0110";
                    break;
                case '7':
                    returnString += "0111";
                    break;
                case '8':
                    returnString += "1000";
                    break;
                case '9':
                    returnString += "1001";
                    break;
                case 'A':
                    returnString += "1010";
                    break;
                case 'B':
                    returnString += "1011";
                    break;
                case 'C':
                    returnString += "1100";
                    break;
                case 'D':
                    returnString += "1101";
                    break;
                case 'E':
                    returnString += "1110";
                    break;
                case 'F':
                    returnString += "1111";
                    break;

            }
        }

        return returnString;
    }

}
