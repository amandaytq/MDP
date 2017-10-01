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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Amandaaa on 21/9/17.
 */


public class MapHandler {
    private GridView gv;
    private GridView mgv;

    public int poswp;
    public int possp;
    int lastwp = -1;
    int direction = EAST;

    int[] startpoint = {-1};
    int[] robotPos = {-1};

    boolean response = false;
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

    public MapHandler(GridView gridView, GridView mapGridView, Context con){
        gv = gridView;
        mgv = mapGridView;
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
        for (int i=0; i<robotPos.length;i++){
            ImageView oldv= (ImageView)mgv.getChildAt(robotPos[i]);
            oldv.setImageResource(0);
            robotPos[i] += distance;
        }
        setBot();
    }

    public void setDirection(int dir){

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
        ImageView riv1 = (ImageView)mgv.getChildAt(robotPos[0]);
        ImageView riv2 = (ImageView)mgv.getChildAt(robotPos[1]);
        ImageView riv3 = (ImageView)mgv.getChildAt(robotPos[2]);
        ImageView riv4 = (ImageView)mgv.getChildAt(robotPos[3]);
        ImageView riv5 = (ImageView)mgv.getChildAt(robotPos[4]);
        ImageView riv6 = (ImageView)mgv.getChildAt(robotPos[5]);
        ImageView riv7 = (ImageView)mgv.getChildAt(robotPos[6]);
        ImageView riv8 = (ImageView)mgv.getChildAt(robotPos[7]);
        ImageView riv9 = (ImageView)mgv.getChildAt(robotPos[8]);
        ImageView arr = (ImageView)mgv.getChildAt(robotPos[direction]);

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
        switch (direction){
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

    public void setStartPoint(int x, int y){
        int pos = getPos(x,y);
        int[] newstartpoint = {pos-21,pos-20,pos-19,pos-1,pos,pos+1,pos+19,pos+20,pos+21};

        if (startpoint.length<9){
            for (int i=0; i<newstartpoint.length;i++){
                ImageView v= (ImageView)gv.getChildAt(newstartpoint[i]);
                changeColor(v,YELLOW);
            }
        }else if(newstartpoint[0] != startpoint[0]){
            for (int i=0; i<startpoint.length;i++) {
                ImageView v2 = (ImageView) gv.getChildAt(startpoint[i]);
                ImageView botv = (ImageView) mgv.getChildAt(robotPos[i]);
                botv.setImageResource(0);
                changeColor(v2, BLUE);
            }

            for (int i=0; i<newstartpoint.length;i++){
                ImageView v= (ImageView)gv.getChildAt(newstartpoint[i]);
                changeColor(v,YELLOW);
            }
        }
        startpoint = newstartpoint;
        robotPos = newstartpoint;
        setBot();
    }

    public void setObs(int x, int y){
        ImageView v = (ImageView)mgv.getChildAt(getPos(x,y));
        v.setImageResource(R.drawable.blue);
        changeColor(v, BLACK);
        obsArrayList.add(getPos(x,y));
    }

    public void removeObs(int x, int y){
        int index = obsArrayList.indexOf(getPos(x, y));
        ImageView v = (ImageView)mgv.getChildAt(getPos(x,y));
        v.setImageResource(0);
        if(index >=0)
            obsArrayList.remove(index);
    }

    public void alertView(final Button btn, String title, String message, boolean choice, final int mode) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        switch (mode){
                            case SETWP:
                                setWayPoint(btn);
                                break;

                            case SETSP:
                                setStartPoint(getCoordinates(possp)[0],getCoordinates(possp)[1]);
                                btn.setText("Set Start");
                                btn.setActivated(false);
                                mgv.setOnItemClickListener(null);
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

    public void setWayPoint(Button setwp_btn){
        ImageView newWPv= (ImageView)gv.getChildAt(poswp);
        boolean overlapped = false;

        for (int i=0; i<startpoint.length;i++){
            if (startpoint[i] == poswp) {
                overlapped = true;
                break;
            }
        }

        if(!overlapped) {
            if (lastwp != -1) {
                ImageView lastWPv= (ImageView)gv.getChildAt(lastwp);
                changeColor(lastWPv, BLUE);
            }

            setwp_btn.setText("Set WP");
            setwp_btn.setActivated(false);
            mgv.setOnItemClickListener(null);
            changeColor(newWPv, YELLOW);
            lastwp = poswp;

        }else{
            Log.i("res", "yes");
            alertView(null, "Opps!", "You have selected an invalid point.", false, ALERT);
        }
    }

    public int getPos(int x, int y){
        return gv.getNumColumns()*x + y;
    }

    public int[] getCoordinates(int pos){
        int[] coords = {0,0};
        coords[1] = pos%gv.getNumColumns();
        coords[0] = (pos-coords[1])/gv.getNumColumns();
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