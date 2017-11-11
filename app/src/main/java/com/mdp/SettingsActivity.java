package com.mdp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;
/*
* MDP Team 13
* @version 1.0
* @author Amanda Chan	<achan016@e.ntu.edu.sg>
* @author Tan Peng Hian <ptan023@e.ntu.edu.sg>
*/
public class SettingsActivity extends AppCompatActivity {

    private ArrayList<String> listArr = new ArrayList<String>();

    private Switch bluetooth_toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        listArr.add("Bluetooth");
        listArr.add("Start and End points"); //coordinateFragment
        listArr.add("Functions");
        listArr.add("Obstacles");
        listArr.add("Calibration");

        ListView list = (ListView) findViewById(R.id.settings_lv);
        ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listArr);
        list.setAdapter(listArrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectFrag(position);
            }
        });

        Toolbar bluetooth_toolbar = (Toolbar) findViewById(R.id.settings_bar);
        setSupportActionBar(bluetooth_toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


    }

    private void selectFrag(int pos){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        BluetoothFragment f1 = new BluetoothFragment();
        CoordinatesFragment f2 = new CoordinatesFragment();
        FunctionsFragment f3 = new FunctionsFragment();
        ObstaclesFragment f4 = new ObstaclesFragment();
        CalibrationFragment f5 = new CalibrationFragment();

        switch (pos){
            case 0:
                ft.replace(R.id.fragment, f1);
                break;
            case 1:
                ft.replace(R.id.fragment, f2);
                break;
            case 2:
                ft.replace(R.id.fragment, f3);
                break;
            case 3:
                ft.replace(R.id.fragment, f4);
                break;
            case 4:
                ft.replace(R.id.fragment, f5);
                break;
        }

        ft.commit();
    }


}
