package com.alba.accpause;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import com.alba.accpause.batData.BatteryInfoParser;
import com.alba.accpause.database.Data;
import com.alba.accpause.database.DataDao;
import com.alba.accpause.database.DataParser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private Context context;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public HomeFragment() {
        // Required empty public constructor
    }

    boolean chrEnabled = true;
    BatteryInfoParser batInfo;
    private static final long UPDATE_INTERVAL_MILLIS = 2000; // ms
    private Handler handler;
    private Runnable runnable;

    private TextView tempValue;
    private TextView currentValue;
    private TextView levelValue;
    private TextView stateValue;
    private TextView chargingValue;
    private TextView powerValue;
    private Button pause;
    private ColorStateList backgroundTintList;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentConfigs.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        batInfo = new BatteryInfoParser();
        handler = new Handler(Looper.getMainLooper()); // Ensures it runs on the UI thread
        runnable = this::updateBatteryInfo;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the periodic task when the Activity is paused to prevent leaks or unnecessary work
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start the periodic task when the Activity is resumed
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        // I also get a reference to the button
        pause = view.findViewById(R.id.filledButton);
        tempValue = view.findViewById(R.id.tempValue);
        currentValue = view.findViewById(R.id.currentValue);
        levelValue = view.findViewById(R.id.levelValue);
        stateValue = view.findViewById(R.id.stateValue);
        chargingValue = view.findViewById(R.id.chargingValue);
        powerValue = view.findViewById(R.id.powerValue);


        //Get the material color of the button
        backgroundTintList = pause.getBackgroundTintList();

        pause.setOnClickListener(v -> {
            onButtonClick(v,backgroundTintList);
        });

        return view;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Start the periodic task when the Activity is resumed
        handler.postDelayed(runnable, 300);
    }

    public void updateBatteryInfo(){
        HashMap<String,String> bInfo = batInfo.getBatteryInfo();
        tempValue.setText(bInfo.get("temp"));
        currentValue.setText(bInfo.get("current_now"));
        levelValue.setText(bInfo.get("level"));
        stateValue.setText(bInfo.get("status"));
        powerValue.setText(bInfo.get("power_now"));
        if (chrEnabled)
            chargingValue.setText("Enabled");
        else
            chargingValue.setText("Disabled");

        handler.postDelayed(runnable, UPDATE_INTERVAL_MILLIS);
    }

    public void onButtonClick(View view, ColorStateList primaryColor){

        if (chrEnabled) {

            try {
                Runtime.getRuntime().exec("su -c /dev/acca --disable");
                pause.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.home_button_disabled));
                Toast.makeText(getActivity(), "Charging disabled",
                        Toast.LENGTH_SHORT).show();
                chrEnabled = false;
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Failed to get su permission",
                        Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        } else {
            try {
                Runtime.getRuntime().exec("su -c /dev/acca --enable");
                pause.setBackgroundTintList(primaryColor);
                Toast.makeText(getActivity(), "Charging enabled",
                        Toast.LENGTH_SHORT).show();
                chrEnabled = true;
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Failed to get su permission",
                        Toast.LENGTH_SHORT).show();
                throw new RuntimeException(e);
            }
        }
    }
}