package com.mdp;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private ArrayList<String> listArr = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        listArr.add("BlueTooth");
        listArr.add("Coordinates");
        listArr.add("Functions");

        ListView list = (ListView) findViewById(R.id.settings_lv);
        ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listArr);
        list.setAdapter(listArrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectFrag(position);
            }
        });


    }

    private void selectFrag(int pos){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        switch (pos){
            case 0:
                ft.replace(R.id.fragment, new BluetoothFragment());
                break;
            case 1:
                ft.replace(R.id.fragment, new CoordinatesFragment());
                break;
            case 2:
                ft.replace(R.id.fragment, new FunctionsFragment());
                break;
        }

        ft.commit();
    }


}
