package com.alba.accpause;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alba.accpause.database.Data;
import com.alba.accpause.database.DataDao;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MaterialSwitch enableDaemonSwitch;
    private RangeSlider chargeLevelSlider;
    private float chargeLevelUpperValue;
    private float chargeLevelLowerValue;
    private Context context;
    private CompoundButton.OnCheckedChangeListener daemonSwitch_listener;
    private Slider currentSlider;
    private String currentValue;
    private float chargeCurrentValue;
    private TextView capacityLabel;
    private TextView currentLabel;
    private MaterialSwitch fakePassThrSwitch;
    private CompoundButton.OnCheckedChangeListener passThrSwitch_listener;


    public ConfigsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentConfigs.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigsFragment newInstance(String param1, String param2) {
        ConfigsFragment fragment = new ConfigsFragment();
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configs, container, false);

        enableDaemonSwitch = view.findViewById(R.id.mySwitch);
        ConstraintLayout enableDaemonLabel = view.findViewById(R.id.daemonLabel);
        ConstraintLayout fakePassThrLabel = view.findViewById(R.id.fakePassThrLabel);
        chargeLevelSlider = view.findViewById(R.id.capacitySlider);
        currentSlider = view.findViewById(R.id.currentSlider);

        capacityLabel = view.findViewById(R.id.capacityLabel);
        currentLabel = view.findViewById(R.id.currentLabel);

        enableDaemonLabel.setOnClickListener(v ->
                enableDaemonSwitch.setChecked(!enableDaemonSwitch.isChecked()));

        fakePassThrLabel.setOnClickListener(v ->
                fakePassThrSwitch.setChecked(!fakePassThrSwitch.isChecked()));

        ConstraintLayout chargeOnceLabel = view.findViewById(R.id.chargeOnceTo);
        chargeOnceLabel.setOnClickListener(v ->
                // launch a new activity
                startActivity(new Intent(getActivity(), ChargeOnceActivity.class)));

        /////////////
        daemonSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean acc_enabled;
                try{
                    if (isChecked) {
                        //Toast.makeText(getActivity(), "ACC Enabled", Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "Pass through enabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/accd");
                        acc_enabled = true;
                    } else {
                        //Toast.makeText(getActivity(), "ACC Disabled", Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "Pass through disabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/accd.");
                        acc_enabled = false;
                    }
                    final boolean finalAcc_enabled = acc_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("daemon_enabled");
                        d_enabled.value = Boolean.toString(finalAcc_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        enableDaemonSwitch.setOnCheckedChangeListener(daemonSwitch_listener);

        passThrSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean passThr_enabled;
                try{
                    if (isChecked) {
                        //Toast.makeText(getActivity(), "ACC Enabled", Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "ACC Enabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/acca --set");//TODO
                        passThr_enabled = true;
                    } else {
                        //Toast.makeText(getActivity(), "ACC Disabled", Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "ACC Disabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/acca  --set");
                        passThr_enabled = false;
                    }
                    final boolean finalPassThr_enabled = passThr_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("prioritize_batt_idle_mode");
                        d_enabled.value = Boolean.toString(finalPassThr_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        enableDaemonSwitch.setOnCheckedChangeListener(daemonSwitch_listener);

        chargeLevelSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                try {
                    // acc pause_capacity resume_capacity
                    if (!fromUser){ // To ensure the listener isn't called when the slider reverts to its saved value.
                        return;
                    }

                    Float resume = chargeLevelSlider.getValues().get(0);
                    Float pause = chargeLevelSlider.getValues().get(1);

                    String resume_capacity = Float.toString(resume);
                    String pause_capacity = Float.toString(pause);

                    final String finalResume_capacity = resume_capacity.substring(0, resume_capacity.length()-2);
                    final String finalPause_capacity = pause_capacity.substring(0, pause_capacity.length()-2);
                    //Toast.makeText(getActivity(), finalResume_capacity+" "+finalPause_capacity, Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, finalResume_capacity+" "+finalPause_capacity, Snackbar.LENGTH_SHORT).show();
                    Runtime.getRuntime().exec("su -c /dev/acc " + finalPause_capacity + " " + finalResume_capacity);

                    capacityLabel.setText(getString(R.string.charging_capacity_description, finalPause_capacity, finalResume_capacity));

                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();

                        Data pauseData = dataDao.getByKey("pause_capacity");
                        pauseData.value = finalPause_capacity;

                        Data resumeData = dataDao.getByKey("resume_capacity");
                        resumeData.value = finalResume_capacity;

                        dataDao.updateAll(pauseData,resumeData);
                    }).start();


                } catch (IOException e) {
                    //Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        });

        currentSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                try {
                    if (!fromUser){ // To ensure the listener isn't called when the slider reverts to its saved value.
                        return;
                    }
                    currentValue = Float.toString(currentSlider.getValue());
                    final String finalCurrentValue = currentValue.substring(0, currentValue.length()-2);

                    if (finalCurrentValue.equals("0")) { // default current limit
                        Runtime.getRuntime().exec("su -c /dev/acca --set --current -");
                        currentLabel.setText(getString(R.string.charging_current_description_no_limit));
                    }
                    else {
                        Runtime.getRuntime().exec("su -c /dev/acca  --set --current " + finalCurrentValue);
                        currentLabel.setText(getString(R.string.charging_current_description,finalCurrentValue));
                    }
                    Snackbar.make(view, finalCurrentValue, Snackbar.LENGTH_SHORT).show();

                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();

                        Data currentData = dataDao.getByKey("max_charging_current");
                        currentData.value = finalCurrentValue;

                        dataDao.update(currentData);
                    }).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        new Thread(() -> {
            ACCPause application = (ACCPause) context.getApplicationContext();
            DataDao dataDao = application.getDataDao();
            String key = "resume_capacity";
            String value = dataDao.getByKey(key).value;
            boolean acc_enabled = true;

            if (value != null)
                chargeLevelLowerValue = Float.parseFloat(value);
            else
                chargeLevelLowerValue = 0.0f;
            final String lower = value;

            key = "pause_capacity";
            value = dataDao.getByKey(key).value;
            if (value != null)
                chargeLevelUpperValue = Float.parseFloat(value);
            else
                chargeLevelUpperValue = 0.0f;
            final String upper = value;
            List<Float> initialValues = Arrays.asList(chargeLevelLowerValue, chargeLevelUpperValue);
            chargeLevelSlider.setValues(initialValues);
            //capacityLabel.setText(getString(R.string.charging_capacity_description,upper,lower));
            if (getActivity() != null && isAdded()) { // Always check for null and attachment
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        capacityLabel.setText(getString(R.string.charging_capacity_description, upper, lower));
                    }
                });
            }

            key = "max_charging_current";
            value = dataDao.getByKey(key).value;
            if (value != null){
                chargeCurrentValue = Float.parseFloat(value);
                currentLabel.setText(getString(R.string.charging_current_description,value));
            }
            else{
                chargeCurrentValue = 0.0f;
                currentLabel.setText(getString(R.string.charging_current_description_no_limit));
            }
            currentSlider.setValue(chargeCurrentValue);

            key = "daemon_enabled";
            value = dataDao.getByKey(key).value;
            if (value != null)
                acc_enabled = Boolean.parseBoolean(value);

            enableDaemonSwitch.setOnCheckedChangeListener(null);
            enableDaemonSwitch.setChecked(acc_enabled);
            enableDaemonSwitch.jumpDrawablesToCurrentState();
            enableDaemonSwitch.setOnCheckedChangeListener(daemonSwitch_listener);

        }).start();
        return view;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}