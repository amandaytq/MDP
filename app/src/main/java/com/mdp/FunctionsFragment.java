package com.mdp;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class FunctionsFragment extends Fragment {

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private EditText f1_text;
    private EditText f2_text;

    private String f1Command = "";
    private String f2Command = "";

    private Button f1_save;
    private Button f2_save;

    public FunctionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_functions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setup function buttons
        mPref = getContext().getSharedPreferences("storedCommand", 0);
        mEditor = mPref.edit();

        f1_text = (EditText) view.findViewById(R.id.f1_text);
        f1_save = (Button)view.findViewById(R.id.f1_save);
        f1_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCommand = f1_text.getText().toString();

                Log.d("functions", "onClick: Saving new Command F1: " + newCommand);

                mEditor.putString("f1", newCommand).commit();
                f1Command = newCommand;
                Toast.makeText(getContext(), "Save Successful", Toast.LENGTH_SHORT).show();
            }
        });

        f2_text = (EditText) view.findViewById(R.id.f2_text);
        f2_save = (Button)view.findViewById(R.id.f2_save);
        f2_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCommand = f2_text.getText().toString();

                Log.d("Functions", "onClick: Saving new Command F2: " + newCommand);

                mEditor.putString("f2", newCommand).commit();
                f2Command = newCommand;
                Toast.makeText(getContext(), "Save Successful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Functions", "resume: MainActivity");

        f1Command = mPref.getString("f1", "");
        f2Command = mPref.getString("f2", "");

        mPref = getContext().getSharedPreferences("storedCommand", 0);


        f1_text.setText(f1Command);
        f2_text.setText(f2Command);
    }
}
