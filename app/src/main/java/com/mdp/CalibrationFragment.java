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
import android.widget.Toast;
/*
* MDP Team 13
* @version 1.0
* @author Amanda Chan	<achan016@e.ntu.edu.sg>
* @author Tan Peng Hian <ptan023@e.ntu.edu.sg>
*/
public class CalibrationFragment extends Fragment {
    private Button calibrate_left;
    private Button calibrate_right;
    private Button calibrate_front;

    public CalibrationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setClickable(true);

        calibrate_front = (Button) view.findViewById(R.id.cali_front);
        calibrate_left = (Button) view.findViewById(R.id.cali_left);
        calibrate_right = (Button) view.findViewById(R.id.cali_right);

        calibrate_front.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.sendHandler.sendCalibration("front");
                Toast.makeText(getContext(), "Calibrating Front", Toast.LENGTH_SHORT);
            }
        });
        calibrate_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.sendHandler.sendCalibration("left");
                Toast.makeText(getContext(), "Calibrating Left", Toast.LENGTH_SHORT);
            }
        });
        calibrate_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.sendHandler.sendCalibration("right");
                Toast.makeText(getContext(), "Calibrating Right", Toast.LENGTH_SHORT);
            }
        });
    }
}
