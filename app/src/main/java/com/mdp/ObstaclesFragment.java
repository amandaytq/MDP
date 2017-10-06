package com.mdp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ObstaclesFragment extends Fragment {


    View v;
    EditText x_txt;
    EditText y_txt;

    public ObstaclesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_obstacles, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setClickable(true);

        v = view;
        x_txt = (EditText) v.findViewById(R.id.x_obs);
        y_txt = (EditText) v.findViewById(R.id.y_obs);

        Button set_obs = (Button) v.findViewById(R.id.set_obs);
        set_obs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(x_txt.getText().toString());
                int y = Integer.parseInt(y_txt.getText().toString());
                MainActivity.mapHandler.setObs(x,y);
                //Toast.makeText(getContext(), " X: "+x+", Y:"+y, Toast.LENGTH_SHORT).show();
            }
        });

        Button rmv_obs = (Button) v.findViewById(R.id.rmv_obs);
        rmv_obs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int x = Integer.parseInt(x_txt.getText().toString());
                int y = Integer.parseInt(y_txt.getText().toString());
                MainActivity.mapHandler.removeObs(x,y);
            }
        });
    }
}
