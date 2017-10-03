package com.mdp;

public class ReceiveHandler {
    private static final String command_forward = "f";
    private static final String command_right = "tr";
    private static final String command_left = "tl";

    private static final String status_protocol = "STATUS";
    private static final String status_idle = "IDLE";
    private static final String status_moving = "MOVING";
    private static final String status_exploring = "EXPLORING";
    private static final String status_turning = "TURNING";

    private static final String map_protocol = "MAP";

    private static final String obs_protocol = "OBS";

    private MapHandler mh;
    private MainActivity ma;

    public ReceiveHandler(MapHandler mh, MainActivity ma){
        mh = mh;
        ma = ma;
    }

    public void received(String r){
        String [] s = r.split("::");
        String protocol = s[0];
        String parameters = s[1];
        switch(protocol){
            case status_protocol:
                //update status and perform the required actions
                String [] s2 = parameters.split("/");
                String status = s2[0];
                ma.updateStatusText(status);
                switch(status){
                    case status_idle:
                        //enable controls
                        break;
                    case status_moving:
                        //disable controls
                        //update map of robot movement

                        break;
                    case status_exploring:
                        //disable controls
                        break;
                    case status_turning:
                        //disable controls
                        //update map on robot turning
                        break;
                    default:
                        //update status text only
                        break;
                }

                break;
            case map_protocol:
                //read parameter as map and populate map
                break;
            case obs_protocol:
                //read parameter as list of obstacles and populate map

            default:
                break;
        }
    }
}
