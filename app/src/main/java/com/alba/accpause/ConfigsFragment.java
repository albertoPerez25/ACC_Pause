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

import com.alba.accpause.ACCPause;
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
    private Slider passThrSlider;
    private RangeSlider tempSlider;
    private TextView tempLabel;

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
        fakePassThrSwitch = view.findViewById(R.id.fakePassThrSwitch);
        ConstraintLayout enableDaemonLabel = view.findViewById(R.id.daemonLabel);
        ConstraintLayout fakePassThrLabel = view.findViewById(R.id.fakePassThrLabel);
        chargeLevelSlider = view.findViewById(R.id.capacitySlider);
        currentSlider = view.findViewById(R.id.currentSlider);
        passThrSlider = view.findViewById(R.id.passThgLevelSlider);
        tempSlider = view.findViewById(R.id.tempSlider);

        capacityLabel = view.findViewById(R.id.capacityLabel);
        currentLabel = view.findViewById(R.id.currentLabel);
        tempLabel = view.findViewById(R.id.tempLabel);

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
                        Snackbar.make(view, "ACC Enabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/accd");
                        acc_enabled = true;
                    } else {
                        //Toast.makeText(getActivity(), "ACC Disabled", Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "ACC Disabled", Snackbar.LENGTH_SHORT).show();
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
                        Snackbar.make(view, "Pass through enabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/acca --set prioritize_batt_idle_mode=true");
                        passThr_enabled = true;
                        float pause = passThrSlider.getValue();
                        setChargeLimit(pause-1,pause,view);
                        passThrSlider.setValue(pause);
                        chargeLevelSlider.setVisibility(View.GONE);
                        passThrSlider.setVisibility(View.VISIBLE);

                    } else {
                        //Toast.makeText(getActivity(), "ACC Disabled", Toast.LENGTH_SHORT).show();
                        Snackbar.make(view, "Pass through disabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec("su -c /dev/acca  --set prioritize_batt_idle_mode=false");
                        passThr_enabled = false;
                        Float pause = chargeLevelSlider.getValues().get(1);
                        float resume = chargeLevelSlider.getValues().get(1)-5;
                        chargeLevelSlider.setValues(resume,pause);
                        setChargeLimit(resume,pause,view);
                        passThrSlider.setVisibility(View.GONE);
                        chargeLevelSlider.setVisibility(View.VISIBLE);
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
        fakePassThrSwitch.setOnCheckedChangeListener(passThrSwitch_listener);

        passThrSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                try {
                    // acc pause_capacity resume_capacity
                    if (!fromUser){ // To ensure the listener isn't called when the slider reverts to its saved value.
                        return;
                    }

                    float pause = passThrSlider.getValue();
                    float resume = pause - 1;

                    String [] finalCapacities = setChargeLimit(resume,pause,view);

                } catch (IOException e) {
                    //Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        });

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

                    String [] finalCapacities = setChargeLimit(resume,pause,view);

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

        tempSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                try {
                    // acc pause_capacity resume_capacity
                    if (!fromUser){ // To ensure the listener isn't called when the slider reverts to its saved value.
                        return;
                    }

                    Float resume = tempSlider.getValues().get(0);
                    Float pause = tempSlider.getValues().get(1);

                    setTempLimit(resume,pause,view);
                } catch (IOException e) {
                    //Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        });

        return view;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Thread(() -> {

            float chargeLevelLowerValue;
            float chargeLevelUpperValue;
            String upperCapacityString = null;
            String lowerCapacityString = null;
            float chargeCurrentValue;
            float tempLowerValue;
            float tempUpperValue;
            String tempLowerString;
            String tempUpperString;
            boolean acc_enabled_from_db = true; // Default value

            try {
                ACCPause.databaseInitialized.await();

                ACCPause application = (ACCPause) context.getApplicationContext();
                DataDao dataDao = application.getDataDao();

                // Retrieve all necessary data from the database
                String key = "resume_capacity";
                String value = dataDao.getByKey(key).value;
                if (value != null) {
                    chargeLevelLowerValue = Float.parseFloat(value);
                    lowerCapacityString = value; // Store for UI update
                } else {
                    chargeLevelLowerValue = 0.0f;
                    lowerCapacityString = "0.0"; // Default string for UI
                }

                key = "pause_capacity";
                value = dataDao.getByKey(key).value;
                if (value != null) {
                    chargeLevelUpperValue = Float.parseFloat(value);
                    upperCapacityString = value; // Store for UI update
                } else {
                    chargeLevelUpperValue = 0.0f;
                    upperCapacityString = "0.0"; // Default string for UI
                }

                List<Float> initialValues = Arrays.asList(chargeLevelLowerValue, chargeLevelUpperValue);

                boolean prioritizeBattIdleMode = false;
                key = "prioritize_batt_idle_mode";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    prioritizeBattIdleMode = true;
                }

                String currentLimitDescription = null;
                key = "max_charging_current";
                value = dataDao.getByKey(key).value;
                if (value != null && !value.equals("0")) {
                    chargeCurrentValue = Float.parseFloat(value);
                    currentLimitDescription = getString(R.string.charging_current_description, value);
                } else {
                    chargeCurrentValue = 0.0f;
                    currentLimitDescription = getString(R.string.charging_current_description_no_limit);
                }

                key = "max_temp";
                value = dataDao.getByKey(key).value;
                if (value != null){
                    tempUpperValue = Float.parseFloat(value);
                    tempUpperString = value;
                } else {
                    tempUpperValue = 50.0f;
                    tempUpperString = "50.0";
                }

                key = "resume_temp";
                value = dataDao.getByKey(key).value;
                if (value != null){
                    tempLowerValue = Float.parseFloat(value);
                    tempLowerString = value;
                } else {
                    tempLowerValue = 50.0f;
                    tempLowerString = "50.0";
                }

                List<Float> tempInitialValues = Arrays.asList(tempLowerValue, tempUpperValue);

                key = "daemon_enabled";
                value = dataDao.getByKey(key).value;
                if (value != null) {
                    acc_enabled_from_db = Boolean.parseBoolean(value);
                }

                // Post UI updates to the main thread.
                if (getActivity() != null && isAdded()) {
                    final String finalUpperCapacityString = upperCapacityString;
                    final String finalLowerCapacityString = lowerCapacityString;
                    final List<Float> finalInitialValues = initialValues;
                    final boolean finalPrioritizeBattIdleMode = prioritizeBattIdleMode;
                    final float finalChargeCurrentValue = chargeCurrentValue;
                    final String finalCurrentLimitDescription = currentLimitDescription;
                    final String finalTempLowerString = tempLowerString;
                    final String finalTempUpperString = tempUpperString;
                    final List<Float> finalTempInitialValues = tempInitialValues;
                    final boolean finalAccEnabledFromDb = acc_enabled_from_db;

                    getActivity().runOnUiThread(() -> {
                        chargeLevelSlider.setValues(finalInitialValues);
                        passThrSlider.setValue(finalInitialValues.get(1)); // Assuming this is correct

                        capacityLabel.setText(getString(R.string.charging_capacity_description, finalUpperCapacityString, finalLowerCapacityString));

                        if (finalPrioritizeBattIdleMode) {
                            chargeLevelSlider.setVisibility(View.GONE);
                            passThrSlider.setVisibility(View.VISIBLE);
                            fakePassThrSwitch.setChecked(true);
                        } else {
                            chargeLevelSlider.setVisibility(View.VISIBLE); // Ensure it's visible if not prioritized
                            passThrSlider.setVisibility(View.GONE);
                            fakePassThrSwitch.setChecked(false);
                        }

                        currentSlider.setValue(finalChargeCurrentValue);
                        currentLabel.setText(finalCurrentLimitDescription);

                        tempSlider.setValues(finalTempInitialValues);
                        tempLabel.setText(getString(R.string.temp_limit_description, finalTempLowerString, finalTempUpperString));

                        enableDaemonSwitch.setOnCheckedChangeListener(null); // Temporarily remove listener to prevent callback during programmatic set
                        enableDaemonSwitch.setChecked(finalAccEnabledFromDb);
                        enableDaemonSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately
                        enableDaemonSwitch.setOnCheckedChangeListener(daemonSwitch_listener); // Re-add the listener
                    });
                }

            } catch (InterruptedException e) {
                // Handle interruption if the thread is interrupted while waiting
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Catch other potential exceptions during data parsing or DB access
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        capacityLabel.setText("Error loading data.");
                    });
                }
            }
        }).start();
    }

    private String[] setChargeLimit(float resume, float pause, View view) throws IOException{
        String resume_capacity = Float.toString(resume);
        String pause_capacity = Float.toString(pause);

        final String finalResume_capacity = resume_capacity.substring(0, resume_capacity.length()-2);
        final String finalPause_capacity = pause_capacity.substring(0, pause_capacity.length()-2);
        //Toast.makeText(getActivity(), finalResume_capacity+" "+finalPause_capacity, Toast.LENGTH_SHORT).show();
        //Snackbar.make(view, finalResume_capacity+" "+finalPause_capacity, Snackbar.LENGTH_SHORT).show();
        Runtime.getRuntime().exec("su -c /dev/acc " + finalPause_capacity + " " + finalResume_capacity);

        capacityLabel.setText(getString(R.string.charging_capacity_description, finalPause_capacity, finalResume_capacity));

        new Thread(() -> {
            DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();

            Data pauseData = dataDao.getByKey("pause_capacity");
            pauseData.value = finalPause_capacity; //Pause

            Data resumeData = dataDao.getByKey("resume_capacity");
            resumeData.value = finalResume_capacity; //Resume

            dataDao.update(pauseData);
        }).start();

        return new String[]{finalResume_capacity,finalPause_capacity};
    }

    private String[] setTempLimit(float resume, float pause, View view) throws IOException{
        String resume_capacity = Float.toString(resume);
        String pause_capacity = Float.toString(pause);

        final String finalResume_capacity = resume_capacity.substring(0, resume_capacity.length()-2);
        final String finalPause_capacity = pause_capacity.substring(0, pause_capacity.length()-2);

        Runtime.getRuntime().exec("su -c /dev/acc --set max_temp=" + finalPause_capacity);
        Runtime.getRuntime().exec("su -c /dev/acc --set resume_temp=" + finalResume_capacity);

        capacityLabel.setText(getString(R.string.temp_limit_description, finalPause_capacity, finalResume_capacity));

        new Thread(() -> {
            DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();

            Data pauseData = dataDao.getByKey("max_temp");
            pauseData.value = finalPause_capacity; //Pause

            Data resumeData = dataDao.getByKey("resume_temp");
            resumeData.value = finalResume_capacity; //Resume

            dataDao.update(pauseData);
        }).start();

        return new String[]{finalResume_capacity,finalPause_capacity};
    }
}