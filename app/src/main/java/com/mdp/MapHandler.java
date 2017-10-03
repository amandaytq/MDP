package com.mdp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Amandaaa on 21/9/17.
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

    public static final int SETWP = 1;
    public static final int SETSP = 2;
    public static final int ALERT = 3;

    private static final int NORTH = 1;
    private static final int SOUTH= 7;
    private static final int EAST = 5;
    private static final int WEST = 3;

    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private static final int[] dirArray = {EAST, SOUTH, WEST, NORTH};
    private static final ArrayList<Integer> obsArrayList = new ArrayList<Integer>();
    private static final ArrayList<Integer> pathArrayList = new ArrayList<Integer>();

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

    public void moveFront(){
        int distance = 0;
        switch (direction){
            case NORTH:
                distance = -20;
                break;
            case SOUTH:
                distance = 20;
                break;
            case EAST:
                distance = 1;
                break;
            case WEST:
                distance = -1;
                break;
        }

        int[] point1 = getCoordinates(robotPos[0]+distance);

        if(point1[0] < 0 || point1[0] > 12 || point1[1] < 0 || point1[1] > 17){
            Toast.makeText(context, "Boundary reached!",Toast.LENGTH_SHORT).show();
        }else{
            for (int i=0; i<robotPos.length;i++){
                ImageView oldv = (ImageView)robotgv.getChildAt(robotPos[i]);
                oldv.setImageResource(0);
                robotPos[i] += distance;
            }
            setBot();
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
                changeColor(v,YELLOW);
            }
        }else if(newstartpoint[0] != startpoint[0]){
            for (int i=0; i<mapgv.getChildCount();i++){
                ImageView v1= (ImageView)mapgv.getChildAt(i);
                ImageView v2= (ImageView)robotgv.getChildAt(i);
                v2.setImageResource(0);
                changeColor(v1,BLUE);
            }

            for (int i=0; i<newstartpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newstartpoint[i]);
                changeColor(v,YELLOW);
            }
        }
        startpoint = newstartpoint;
        robotPos = newstartpoint;
        direction = EAST;
        setBot();
    }

    public void setEndPoint(int x, int y){
        int pos = getPos(x,y);
        int[] newendpoint = {pos-21,pos-20,pos-19,pos-1,pos,pos+1,pos+19,pos+20,pos+21};

        if (startpoint.length<9){
            for (int i=0; i<newendpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newendpoint[i]);
                changeColor(v,YELLOW);
            }
        }else if(newendpoint[0] != startpoint[0]){
            for (int i=0; i<endpoint.length;i++){
                ImageView v1= (ImageView)mapgv.getChildAt(i);
                changeColor(v1,BLUE);
            }

            for (int i=0; i<newendpoint.length;i++){
                ImageView v= (ImageView)mapgv.getChildAt(newendpoint[i]);
                changeColor(v,YELLOW);
            }
        }
        endpoint = newendpoint;
    }

    public void setObs(int x, int y){
        ImageView v = (ImageView)obsgv.getChildAt(getPos(x,y));
        v.setImageResource(R.drawable.blue);
        changeColor(v, BLACK);
        obsArrayList.add(getPos(x,y));
    }

    public void removeObs(int x, int y){
        int index = obsArrayList.indexOf(getPos(x, y));
        ImageView v = (ImageView)obsgv.getChildAt(getPos(x,y));
        v.setImageResource(0);
        if(index >=0)
            obsArrayList.remove(index);
    }

    public void setObsArr(int[][] arr) {
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[y].length; x++) {
                if(arr[y][x] == 1) {
                    ImageView v = (ImageView) obsgv.getChildAt(getPos(x, y));
                    v.setImageResource(R.drawable.blue);
                    changeColor(v, BLACK);
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
                    ImageView v = (ImageView) pathgv.getChildAt(getPos(x, y));
                    v.setImageResource(R.drawable.blue);
                    changeColor(v, BLACK);
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

    public boolean isObstructed(){
        ArrayList<Integer> range = new ArrayList<>();
        switch (direction){
            case NORTH:
                range.add(robotPos[0]-20);
                range.add(robotPos[1]-20);
                range.add(robotPos[2]-20);
                break;

            case SOUTH:
                range.add(robotPos[6]+20);
                range.add(robotPos[7]+20);
                range.add(robotPos[8]+20);
                break;

            case EAST:
                range.add(robotPos[2]+1);
                range.add(robotPos[5]+1);
                range.add(robotPos[8]+1);
                break;

            case WEST:
                range.add(robotPos[0]-1);
                range.add(robotPos[4]-1);
                range.add(robotPos[6]-1);
                break;
        }

        if(obsArrayList.containsAll(range)){
            return true;
        }else{
            return false;
        }
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
                changeColor(lastWPv, BLUE);
            }

            setwp_btn.setText("Set WP");
            setwp_btn.setActivated(false);
            setwp_btn.setEnabled(false);
            changeColor(newWPv, YELLOW);
            lastwp = poswp;

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
        }
    }

    public boolean botSet(){
        return robotPos.length > 1;
    }

}
