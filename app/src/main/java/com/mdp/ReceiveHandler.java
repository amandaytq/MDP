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

    private MapHandler mh;
    private MainActivity ma;

    public ReceiveHandler(MapHandler h, MainActivity a){
        mh = h;
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
                        mh.moveFront();
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
                                mh.setDirection(1);
                                break;
                            case turning_right:
                                mh.setDirection(2);
                                break;
                            case turning_back:
                                mh.setDirection(3);
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

        String map1_s = new BigInteger(map1, 16).toString(2);
        String map2_s = new BigInteger(map2, 16).toString(2);

        int [][] scouted_arr = {};
        int [][] obs_arr = {};

        int xPos = 0;
        int yPos = 0;
        int map2_pos = 0;

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
                map2_pos++;
            }
            else{
                scouted_arr[yPos][xPos] = 0;
                obs_arr[yPos][xPos] = 0;
            }
        }

        mh.setObsArr(obs_arr);
        //mh.setScoutedArr(scouted_arr);
    }
}
