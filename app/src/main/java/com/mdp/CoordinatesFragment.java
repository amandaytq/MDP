package com.mdp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class CoordinatesFragment extends Fragment {
    public CoordinatesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coordinates, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button setSP = (Button) getView().findViewById(R.id.setSP);
        setSP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(((EditText) getView().findViewById(R.id.x_sp)).getText().toString());
                int y = Integer.parseInt(((EditText) getView().findViewById(R.id.y_sp)).getText().toString());
                MainActivity.mapHandler.setStartPoint(x, y);
            }
        });

        Button setEP = (Button) getView().findViewById(R.id.setEP);
        setEP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(((EditText) getView().findViewById(R.id.x_ep)).getText().toString());
                int y = Integer.parseInt(((EditText) getView().findViewById(R.id.y_ep)).getText().toString());
                MainActivity.mapHandler.setEndPoint(x, y);
            }
        });
    }
}
