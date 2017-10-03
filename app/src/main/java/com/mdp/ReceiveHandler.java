package com.mdp;

import android.util.Log;

import java.math.BigInteger;

public class ReceiveHandler {
    private static final String TAG = ReceiveHandler.class.getSimpleName();

    private static final String command_forward = "f";
    private static final String command_right = "tr";
    private static final String command_left = "tl";

    private static final String status_protocol = "STATUS";
    private static final String status_idle = "IDLE";
    private static final String status_moving = "MOVING";
    private static final String status_exploring = "EXPLORING";
    private static final String status_exploring_end = "EXPLORINGEND";
    private static final String status_turning = "TURNING";

    private static final String turning_left = "LEFT";
    private static final String turning_right = "RIGHT";
    private static final String turning_back = "BACK";

    private static final String map_protocol = "MAP";

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
        switch(protocol){
            case status_protocol:
                //update status and perform the required actions
                String [] s2 = parameters.split("//");
                String status = s2[0];
                Log.d(TAG, status);

                ma.updateStatusText(status);
                switch(status){
                    case status_idle:
                        //enable controls
                        ma.enableControls();
                        break;
                    case status_moving:
                        //update map of robot movement
                        ma.mapHandler.moveFront();
                        //update map status
                        //updateMap(s2[1]);
                        break;
                    case status_exploring:
                        //set exploring so as to not enable controls
                        ma.setExploring(true);
                        break;
                    case status_exploring_end:
                        //exploration ended
                        ma.setExploring(false);
                        break;
                    case status_turning:
                        //update map on robot turning
                        String direction = s2[1];
                        switch(direction){
                            case turning_left:
                                ma.mapHandler.setRotatedDirection(1);
                                break;
                            case turning_right:
                                ma.mapHandler.setRotatedDirection(2);
                                break;
                            case turning_back:
                                ma.mapHandler.setRotatedDirection(3);
                                break;
                            default:
                                break;
                        }
                        //updateMap(s2[1]);
                        break;
                    default:
                        //update status text only
                        break;
                }
                break;
            case map_protocol:
                updateMap(s[1]);
                break;
            default:
                break;
        }
    }

    private void updateMap(String hexa){
        //split map string into 2 required values
        String [] s = hexa.split(";;");
        String map1 = s[0];
        String map2 = s[1];

        String map1_s = hexToBinaryString(map1);
        String map2_s = hexToBinaryString(map2);

        Log.d(TAG, "map1_s: " + map1_s);
        Log.d(TAG, "map2_s: " + map2_s);

        int [][] scouted_arr = new int[20][15];
        int [][] obs_arr = new int[20][15];

        int xPos = 0;
        int yPos = 0;
        int map2_pos = 0;

        Log.d(TAG, "No. of map1 char: " + map1_s.length());
        int count = 0;
        for(int i = 2; i < map1_s.length() - 2; i++){
            if(map1_s.charAt(i) == '1'){
                count++;
            }
        }

        for(int i = 2; i < map1_s.length() - 2; i++){
            if(map1_s.charAt(i) == '1'){
                //area is scouted, put into scouted array
                scouted_arr[yPos][xPos] = 1;
                if(map2_s.charAt(map2_pos) == '1'){
                    obs_arr[yPos][xPos] = 1;
                }
                else{
                    obs_arr[yPos][xPos] = 0;
                }
                //prevents app from crahing in the case where map2 loaded poorly
                if(map2_pos < map2_s.length()){
                    map2_pos++;
                }
            }
            else{
                scouted_arr[yPos][xPos] = 0;
                obs_arr[yPos][xPos] = 0;
            }
            if(xPos >= 14){
                xPos = 0;
                yPos++;
            }
            else{
                xPos++;
            }
        }

        ma.mapHandler.setObsArr(obs_arr);
        //mh.setScoutedArr(scouted_arr);
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
