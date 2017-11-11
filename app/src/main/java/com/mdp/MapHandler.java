package com.mdp;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
/*
* MDP Team 13
* @version 1.0
* @author Amanda Chan	<achan016@e.ntu.edu.sg>
* @author Tan Peng Hian <ptan023@e.ntu.edu.sg>
*/
public class MapHandler {
    private GridView gv;
    private GridView mgv;

    private GridView mapgv;
    private GridView pathgv;
    private GridView obsgv;
    private GridView robotgv;

    public int poswp;
    public int possp;
    public int direction = EAST;

    public  int[] robotPos = {-1};

    int lastwp = -1;

    int[] startpoint = {-1};
    int[] endpoint = {-1};

    Context context;

    private static final int BLUE = 1;
    private static final int YELLOW = 2;
    private static final int BLACK = 3;
    private static final int GREEN = 4;

    public static final int SETWP = 1;
    public static final int SETSP = 2;
    public static final int ALERT = 3;

    public static final int NORTH = 1;
    public static final int SOUTH = 7;
    public static final int EAST = 5;
    public static final int WEST = 3;

    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    public static final int UP = 1;
    public static final int DOWN = 2;

    private static final int[] dirArray = {EAST, SOUTH, WEST, NORTH};
    private static final ArrayList<Integer> obsArrayList = new ArrayList<Integer>();
    private static final ArrayList<Integer> pathArrayList = new ArrayList<Integer>();

    public String map_string = null;

    public MapHandler(GridView omapgv, GridView opathgv, GridView oobsgv, GridView orobotgv, Context con){
        //mapgv to store items that are static such as start point, way point and end point.
        //pathgv to store path that are explored
        //obsgv to store the number obstacles
        //robotgv to store the position of robot
        mapgv = omapgv;
        pathgv = opathgv;
        obsgv = oobsgv;
        robotgv = orobotgv;
        context = con;
    }

    public void updateInfo(String newMapString, int[][] obsarr, int[][] patharr, int robotposx, int robotposy){
        if(newMapString != null){
            map_string = newMapString;
        }
        if(robotposx != -1 && robotposy != -1){
            //for robot
            int pos = getPos(robotposx, robotposy);
            //if robot moved
            if(pos != robotPos[4]){
                MainActivity main = (MainActivity) context;
                Log.d("Status", "Updating Status to Moving");
                main.updateStatusText(main.status_moving);
            }

            int[] newrobotpos = {pos-21,pos-20,pos-19,pos-1,pos,pos+1,pos+19,pos+20,pos+21};
            robotPos = newrobotpos;


        }

        if(obsarr != null){
            obsArrayList.clear();
            setObsArr(obsarr);
        }

        if(patharr != null){
            pathArrayList.clear();
            setPath(patharr);
        }

    }

    public void updateMapUI(){
        for (int i = 0; i<mapgv.getChildCount();i++){
           ImageView v1 = (ImageView)obsgv.getChildAt(i);
            ImageView v2 = (ImageView)pathgv.getChildAt(i);
            ImageView v3 = (ImageView)robotgv.getChildAt(i);
            v1.setImageResource(0);
            v3.setImageResource(0);
            v2.setImageResource(R.drawable.blue);
           changeColor(v1, BLUE);
            changeColor(v2, BLUE);
        }
        //setting robot position on the map
        if(botSet())
            setBot();

        //setting of obstacles based on information in the obsArrayList
        for (int i = 0; i<obsArrayList.size();i++) {
            ImageView v1 = (ImageView) obsgv.getChildAt(obsArrayList.get(i));
            v1.setImageResource(R.drawable.blue);
            changeColor(v1, BLACK);

            ImageView v2 = (ImageView) pathgv.getChildAt(pathArrayList.get(i));
            v2.setImageResource(R.drawable.blue);
            changeColor(v2, GREEN);
        }

        for (int i = 0; i<pathArrayList.size();i++){
            ImageView v2 = (ImageView)pathgv.getChildAt(pathArrayList.get(i));
            v2.setImageResource(R.drawable.blue);
            changeColor(v2, GREEN);
//
//            ImageView v3 = (ImageView)pathgv.getChildAt(startpoint[i]);
//            v3.setImageResource(R.drawable.blue);
//            changeColor(v3, YELLOW);
//
//            ImageView v4 = (ImageView)pathgv.getChildAt(endpoint[i]);
//            v4.setImageResource(R.drawable.blue);
//            changeColor(v3, YELLOW);
//
//            ImageView v5 = (ImageView)pathgv.getChildAt(poswp);
//            v5.setImageResource(R.drawable.blue);
//            changeColor(v5, YELLOW);
        }


    }

    public void move(int dir){
        if(botSet()){
            int val = 1;
            if (dir == UP){
                val = 1;
            }else if(dir == DOWN){
                val = -1;
            }
            int distance = 0;
            switch (direction) {
                case NORTH:
                    distance = -20*val;
                    break;
                case SOUTH:
                    distance = 20*val;
                    break;
                case EAST:
                    distance = 1*val;
                    break;
                case WEST:
                    distance = -1*val;
                    break;
            }

            int[] point1 = getCoordinates(robotPos[0] + distance);

            if (point1[0] < 0 || point1[0] > 12 || point1[1] < 0 || point1[1] > 17) {
                Toast.makeText(context, "Boundary reached!", Toast.LENGTH_SHORT).show();
            } else {
                for (int i = 0; i < robotPos.length; i++) {
                    ImageView oldv = (ImageView) robotgv.getChildAt(robotPos[i]);
                    oldv.setImageResource(0);
                    robotPos[i] += distance;
                }
                setBot();
            }
        }else{
            Toast.makeText(context, "Robot have not been set yet", Toast.LENGTH_SHORT).show();
        }

    }

    public void setRotatedDirection(int dir){

        int currDirection = -1;

        for (int i=0; i<dirArray.length;i++) {
            if(dirArray[i] == direction) {
                currDirection = i;
                break;
            }
        }

        switch (dir){
            case RIGHT:
                if(currDirection + 1 > dirArray.length-1){
                    direction = dirArray[0];
                }else {
                    direction = dirArray[currDirection + 1];
                }
                break;

            case LEFT:
                if(currDirection - 1 < 0){
                    direction = dirArray[dirArray.length-1];
                }else {
                    direction = dirArray[currDirection - 1];
                }
                break;
        }

        setBot();
    }

    public void setDirection(int position){
        int index = 0;
        for(int i = 0; i < dirArray.length; i++){
            if(dirArray[i] == position){
                index = i;
                break;
            }
        }
        direction = dirArray[index];
        setBot();
    }

    public void setBot(){
        ImageView riv1 = (ImageView) robotgv.getChildAt(robotPos[0]);
        ImageView riv2 = (ImageView) robotgv.getChildAt(robotPos[1]);
        ImageView riv3 = (ImageView) robotgv.getChildAt(robotPos[2]);
        ImageView riv4 = (ImageView) robotgv.getChildAt(robotPos[3]);
        ImageView riv5 = (ImageView) robotgv.getChildAt(robotPos[4]);
        ImageView riv6 = (ImageView) robotgv.getChildAt(robotPos[5]);
        ImageView riv7 = (ImageView) robotgv.getChildAt(robotPos[6]);
        ImageView riv8 = (ImageView) robotgv.getChildAt(robotPos[7]);
        ImageView riv9 = (ImageView) robotgv.getChildAt(robotPos[8]);
        ImageView arr = (ImageView) robotgv.getChildAt(robotPos[direction]);

        if(riv1 != null && riv2 != null && riv3 != null && riv4 != null && riv5 != null && riv6 != null && riv7 != null && riv8 != null && riv9 != null) {
            riv1.setImageResource(R.drawable.circle);
            riv1.setRotation(-90);
            riv3.setImageResource(R.drawable.circle);
            riv3.setRotation(0);
            riv7.setImageResource(R.drawable.circle);
            riv7.setRotation(-180);
            riv9.setImageResource(R.drawable.circle);
            riv9.setRotation(90);

            riv2.setImageResource(R.drawable.grey);
            riv4.setImageResource(R.drawable.grey);
            riv5.setImageResource(R.drawable.grey);
            riv6.setImageResource(R.drawable.grey);
            riv8.setImageResource(R.drawable.grey);

            arr.setImageResource(R.drawable.arrow);
            switch (direction) {
                case NORTH:
                    arr.setRotation(-90);
                    break;
                case SOUTH:
                    arr.setRotation(90);
                    break;
                case EAST:
                    arr.setRotation(0);
                    break;
                case WEST:
                    arr.setRotation(-180);
                    break;
            }
        }
    }

    public void setStartPoint(int x, int y){
        int pos = getPos(x,y);
        int[] newstartpoint = {pos-21,pos-20,pos-19,pos-1,pos,pos+1,pos+19,pos+20,pos+21};

        if (startpoint.length<9){
            for (int i=0; i<newstartpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newstartpoint[i]);
                v.setImageResource(R.drawable.blue);
                changeColor(v,YELLOW);
            }
        }else if(newstartpoint[0] != startpoint[0]){
            for (int i=0; i<mapgv.getChildCount();i++){
                ImageView v1= (ImageView)mapgv.getChildAt(i);
                ImageView v2= (ImageView)robotgv.getChildAt(i);
                ImageView v3= (ImageView)obsgv.getChildAt(i);
                ImageView v4= (ImageView)pathgv.getChildAt(i);
                v2.setImageResource(0);
                v3.setImageResource(0);
                v1.setImageResource(0);
                v4.setImageResource(R.drawable.blue);
                changeColor(v4, BLUE);
                changeColor(v1,BLUE);
            }

            for (int i=0; i<newstartpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newstartpoint[i]);
                v.setImageResource(R.drawable.blue);
                changeColor(v,YELLOW);
            }
        }
        startpoint = newstartpoint;
        robotPos = newstartpoint;
        direction = EAST;

        Toast.makeText(context, "Start point have successfully been set", Toast.LENGTH_SHORT).show();

        //send info to algorithm
        MainActivity main = (MainActivity) context;
        main.sendHandler.sendStartPoint(x, y);

        setBot();
    }

    public void setEndPoint(int x, int y){
        int pos = getPos(x,y);
        int[] newendpoint = {pos-21,pos-20,pos-19,pos-1,pos,pos+1,pos+19,pos+20,pos+21};

        if (endpoint.length<9){
            for (int i=0; i<newendpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newendpoint[i]);
                v.setImageResource(R.drawable.blue);
                changeColor(v,YELLOW);
            }
        }else if(newendpoint[0] != startpoint[0]){
            for (int i=0; i<endpoint.length;i++){
                ImageView v1= (ImageView)mapgv.getChildAt(endpoint[i]);
                v1.setImageResource(R.drawable.blue);
                changeColor(v1,BLUE);
            }

            for (int i=0; i<newendpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newendpoint[i]);
                v.setImageResource(R.drawable.blue);
                changeColor(v,YELLOW);
            }
        }
        endpoint = newendpoint;
        Toast.makeText(context, "End point have successfully been set", Toast.LENGTH_SHORT).show();
        //send info to algorithm
        MainActivity main = (MainActivity) context;
        main.sendHandler.sendEndPoint(x, y);
    }

    public void setObs(int x, int y){
        if (obsArrayList.contains(getPos(x,y))){
            Toast.makeText(context, "Obstacles already exist", Toast.LENGTH_SHORT).show();
        }else {
            obsArrayList.add(getPos(x, y));
            updateMapUI();
        }
        Toast.makeText(context, "Obstacles have successfully been set", Toast.LENGTH_SHORT).show();
    }

    public void removeObs(int x, int y){
        if(obsArrayList.contains(getPos(x,y))) {
            int index = obsArrayList.indexOf(getPos(x, y));
            ImageView v = (ImageView) obsgv.getChildAt(getPos(x, y));
            v.setImageResource(0);
            if (index >= 0)
                obsArrayList.remove(index);
            Toast.makeText(context, "Obstacle have successfully been removed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Obstacle not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void setObsArr(int[][] arr) {
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[y].length; x++) {
                if(arr[y][x] == 1) {
//                    ImageView v = (ImageView) obsgv.getChildAt(getPos(x, y));
//                    v.setImageResource(R.drawable.blue);
//                    changeColor(v, BLACK);
                    obsArrayList.add(getPos(x, y));
                }
            }
        }
    }

    public void removeObsArr (int[][] arr){
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[x].length; x++) {
                if(arr[x][y] == 1) {
                    int index = obsArrayList.indexOf(getPos(x, y));
                    ImageView v = (ImageView) obsgv.getChildAt(getPos(x, y));
                    v.setImageResource(0);
                    if (index >= 0)
                        obsArrayList.remove(index);
                }
            }
        }

    }

    public void setPath(int[][] arr) {
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[y].length; x++) {
                if(arr[y][x] == 1) {
//                    ImageView v = (ImageView) pathgv.getChildAt(getPos(x, y));
//                    v.setImageResource(R.drawable.blue);
//                    changeColor(v, GREEN);
                    pathArrayList.add(getPos(x, y));
                }
            }
        }
    }

    public void removePath (int[][] arr){
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[x].length; x++) {
                if(arr[x][y] == 1) {
                    int index = pathArrayList.indexOf(getPos(x, y));
                    ImageView v = (ImageView) pathgv.getChildAt(getPos(x, y));
                    v.setImageResource(0);
                    if (index >= 0)
                        pathArrayList.remove(index);
                }
            }
        }
    }

    public boolean isObstructed(int dir){
        if(botSet()){
            int val= 1;
            if (dir == UP){
                val = 1;
            }else if(dir == DOWN){
                val = -1;
            }
            ArrayList<Integer> range = new ArrayList<>();
            switch (direction){
                case NORTH:
                    range.add(robotPos[0]-20*val);
                    range.add(robotPos[1]-20*val);
                    range.add(robotPos[2]-20*val);
                    break;

                case SOUTH:
                    range.add(robotPos[6]+20*val);
                    range.add(robotPos[7]+20*val);
                    range.add(robotPos[8]+20*val);
                    break;

                case EAST:
                    range.add(robotPos[2]+1*val);
                    range.add(robotPos[5]+1*val);
                    range.add(robotPos[8]+1*val);
                    break;

                case WEST:
                    range.add(robotPos[0]-1*val);
                    range.add(robotPos[4]-1*val);
                    range.add(robotPos[6]-1*val);
                    break;
            }

            if(obsArrayList.containsAll(range)){
                return true;
            }else{
                return false;
            }
        }else{
            Toast.makeText(context, "Robot have not been set yet", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void alertView(final Button btn1,final Button btn2, String title, String message, boolean choice, final int mode) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        switch (mode){
                            case SETWP:
                                setWayPoint(btn1, btn2);
                                btn1.setEnabled(true);
                                btn2.setEnabled(true);
                                robotgv.setOnItemClickListener(null);
                                break;

                            case SETSP:
                                setStartPoint(getCoordinates(possp)[0],getCoordinates(possp)[1]);
                                btn1.setText("Set Start");
                                btn1.setActivated(false);
                                btn2.setEnabled(true);
                                robotgv.setOnItemClickListener(null);
                                break;

                            case ALERT:
                                dialoginterface.dismiss();
                                break;
                        }
                    }
                });
        if(choice){
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    dialoginterface.cancel();
                }});
        }
        dialog.show();

    }

    public void setWayPoint(Button setwp_btn, Button setsp_btn){
        ImageView newWPv= (ImageView)mapgv.getChildAt(poswp);

        boolean overlapped = false;

        for (int i=0; i<startpoint.length;i++){
            if (startpoint[i] == poswp) {
                overlapped = true;
                break;
            }
        }

        if(!overlapped) {
            if (lastwp != -1) {
                ImageView lastWPv= (ImageView)mapgv.getChildAt(lastwp);
                lastWPv.setImageResource(0);
                changeColor(lastWPv, BLUE);
            }

            setwp_btn.setText("Set WP");
            setwp_btn.setActivated(false);
            setwp_btn.setEnabled(false);
            newWPv.setImageResource(R.drawable.blue);
            changeColor(newWPv, YELLOW);
            lastwp = poswp;

            int [] coordinates = getCoordinates(poswp);
            MainActivity main = (MainActivity) context;
            main.sendHandler.sendWayPoint(coordinates[0], coordinates[1]);

        }else{
            Log.i("res", "yes");
            alertView(null, null, "Opps!", "You have selected an invalid point.", false, ALERT);
        }
    }

    public int getPos(int x, int y){
        return mapgv.getNumColumns()*x + y;
    }

    public int[] getCoordinates(int pos){
        int[] coords = {0,0};
        coords[1] = pos%mapgv.getNumColumns();
        coords[0] = (pos-coords[1])/mapgv.getNumColumns();
        return coords;
    }

    public void changeColor(ImageView v,int color){
        switch (color){
            case BLUE:
                v.clearColorFilter();
                break;
            case YELLOW:
                v.setColorFilter(Color.YELLOW);
                break;
            case BLACK:
                v.setColorFilter(Color.BLACK);
                break;
            case GREEN:
                v.setColorFilter(Color.GREEN);
                break;
        }
    }

    public boolean botSet(){
        return robotPos.length > 1;
    }

    public int determineTurn(int request_direction){

        //compare direction with requested direction
        int arrayDir = 0;
        int arrayNewDir = 0;
        for(int i = 0; i < dirArray.length; i++){
            if(dirArray[i] == request_direction){
                arrayNewDir = i;
            }
            if(dirArray[i] == direction){
                arrayDir = i;
            }
        }
        Log.d("MapHandler", "arrayPos - initial: " + arrayDir);
        Log.d("MapHandler", "arrayPos - new: " + arrayNewDir);

        int difference = arrayNewDir - arrayDir;

        if(difference <= -3){
            difference = 1;
        }
        else if(difference >= 3){
            difference = -1;
        }

        return difference*90;
    }

    public int determineDirection(int angle, int wise){
        int arrayDir = 0;
        for(int i = 0; i < dirArray.length; i++){
            if(dirArray[i] == direction){
                arrayDir = i;
            }
        }
        int steps = angle/90;
        int arrayMovement = 0;
        if(wise == 1){
            arrayMovement = 1;
        }
        else{
            arrayMovement = -1;
        }

        arrayDir = arrayDir + (arrayMovement * steps);
        return dirArray[arrayDir];
    }
}
