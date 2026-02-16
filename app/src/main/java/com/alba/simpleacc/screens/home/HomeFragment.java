package com.alba.simpleacc.screens.home;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
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

import com.alba.simpleacc.ACCPause;
import com.alba.simpleacc.R;
import com.alba.simpleacc.batData.BatteryInfoParser;
import com.alba.simpleacc.database.Data;
import com.alba.simpleacc.database.DataDao;
import com.google.android.material.appbar.AppBarLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
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
    private TextView daemonValue;
    private TextView powerValue;
    private DataDao dataDao;
    private Context context;

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
        handler.postDelayed(runnable, 300);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        // I also get a reference to the button
        Button pause = view.findViewById(R.id.disableChargingButton);
        Button resume = view.findViewById(R.id.enableChargingButton);
        tempValue = view.findViewById(R.id.tempValue);
        currentValue = view.findViewById(R.id.currentValue);
        levelValue = view.findViewById(R.id.levelValue);
        stateValue = view.findViewById(R.id.stateValue);
        daemonValue = view.findViewById(R.id.daemonValue);
        powerValue = view.findViewById(R.id.powerValue);
        dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();


        //Get the material color of the button
        //backgroundTintList = pause.getBackgroundTintList();

        pause.setOnClickListener(this::pauseListener);
        resume.setOnClickListener(this::resumeListener);

        return view;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbarHome);

        AppBarLayout appBarLayout = view.findViewById(R.id.appBarHome);

        final String collapsedTitle = "Battery Stats";

        try {
            Runtime.getRuntime().exec("su");

            try {
                Runtime.getRuntime().exec("su -c /dev/acca -i");
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Failed to initialize ACC",
                        Toast.LENGTH_LONG).show();
                requireActivity().finish();
            }

        } catch (IOException e) {
            Toast.makeText(getActivity(), "Failed to get su permission",
                    Toast.LENGTH_LONG).show();
            requireActivity().finish();
        }

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isTitleVisible = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                scrollRange = appBarLayout.getTotalScrollRange();

                // Animate size
                float collapseFactor = 1f - (Math.abs(verticalOffset) / (float) scrollRange);

                if (collapseFactor < 0.1f && !isTitleVisible) {
                    // Fully collapsed
                    toolbar.setTitle(collapsedTitle);
                    toolbar.animate().alpha(1f).setDuration(200).start();
                    isTitleVisible = true;
                } else if (collapseFactor > 0.1f && isTitleVisible){
                    // Expanded or in-between
                    toolbar.animate().alpha(0f).setDuration(200).start();
                    isTitleVisible = false;

                }
            }
        });
    }

    public void updateBatteryInfo(){

        HashMap<String,String> bInfo = batInfo.getBatteryInfo();
        tempValue.setText(bInfo.get("temp"));
        currentValue.setText(bInfo.get("current_now"));
        levelValue.setText(bInfo.get("level"));
        stateValue.setText(bInfo.get("status"));
        powerValue.setText(bInfo.get("power_now"));

        boolean daemonEnabled = batInfo.getDaemonStatus();
        if (daemonEnabled)
            daemonValue.setText("Enabled");
        else
            daemonValue.setText("Disabled");

        new Thread(() -> {
            Data d_enabled = dataDao.getByKey("daemon_enabled");
            d_enabled.value = Boolean.toString(daemonEnabled);
            dataDao.update(d_enabled);
        }).start();

        handler.postDelayed(runnable, UPDATE_INTERVAL_MILLIS);
    }

    public void pauseListener(View view){
        try {
            Runtime.getRuntime().exec(context.getString(R.string.command_disable_charging));
            Toast.makeText(getActivity(), "Charging disabled",
                    Toast.LENGTH_SHORT).show();
            chrEnabled = false;
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Failed to get su permission",
                    Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    public void resumeListener(View view){
        try {
            Runtime.getRuntime().exec(context.getString(R.string.command_enable_charging));
            Toast.makeText(getActivity(), "Charging enabled",
                    Toast.LENGTH_SHORT).show();
            chrEnabled = true;
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Failed to get su permission",
                    Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }
/*    public void pauseListener(View view, ColorStateList primaryColor){

        if (chrEnabled) {

            try {
                Runtime.getRuntime().exec(String.valueOf(R.string.command_disable_charging));
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
                Runtime.getRuntime().exec(String.valueOf(R.string.command_enable_charging));
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
    */
}