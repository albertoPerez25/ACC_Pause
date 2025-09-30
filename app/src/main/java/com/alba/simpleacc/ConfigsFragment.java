package com.alba.simpleacc;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alba.simpleacc.database.Data;
import com.alba.simpleacc.database.DataDao;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ConfigsFragment extends Fragment {
    private MaterialSwitch enableDaemonSwitch;
    private RangeSlider chargeLevelSlider;
    private Context context;
    private CompoundButton.OnCheckedChangeListener daemonSwitch_listener;
    private Slider currentSlider;
    private String currentValue;
    private TextView capacityLabel;
    private TextView currentLabel;
    private MaterialSwitch fakePassThrSwitch;
    private MaterialSwitch forceOffSwitch;
    private MaterialSwitch offMidSwitch;
    private MaterialSwitch idleAboveSwitch;
    private MaterialSwitch statusWorkaroundSwitch;
    private MaterialSwitch currentWorkaroundSwitch;
    private Slider passThrSlider;
    private RangeSlider tempSlider;
    private TextView tempLabel;

    public ConfigsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configs, container, false);

        enableDaemonSwitch = view.findViewById(R.id.mySwitch);
        fakePassThrSwitch = view.findViewById(R.id.fakePassThrSwitch);
        forceOffSwitch = view.findViewById(R.id.forceOffSwitch);
        offMidSwitch = view.findViewById(R.id.offMidSwitch);
        idleAboveSwitch = view.findViewById(R.id.idleAboveSwitch);
        statusWorkaroundSwitch = view.findViewById(R.id.statusWorkaroundSwitch);
        currentWorkaroundSwitch = view.findViewById(R.id.currentWorkaroundSwitch);
        ConstraintLayout enableDaemonLabel = view.findViewById(R.id.daemonLabel);
        ConstraintLayout fakePassThrLabel = view.findViewById(R.id.fakePassThrLabel);
        ConstraintLayout forceOffLabel = view.findViewById(R.id.forceOffLabel);
        ConstraintLayout offMidLabel = view.findViewById(R.id.offMidLabel);
        ConstraintLayout idleAboveLabel = view.findViewById(R.id.idleAboveLabel);
        ConstraintLayout statusWorkaroundLabel = view.findViewById(R.id.statusWorkaroundLabel);
        ConstraintLayout currentWorkaroundLabel = view.findViewById(R.id.currentWorkaroundLabel);
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

        forceOffLabel.setOnClickListener(v ->
                forceOffSwitch.setChecked(!forceOffSwitch.isChecked()));

        offMidLabel.setOnClickListener(v ->
                offMidSwitch.setChecked(!offMidSwitch.isChecked()));

        idleAboveLabel.setOnClickListener(v ->
                idleAboveSwitch.setChecked(!idleAboveSwitch.isChecked()));

        statusWorkaroundLabel.setOnClickListener(v ->
                statusWorkaroundSwitch.setChecked(!statusWorkaroundSwitch.isChecked()));

        currentWorkaroundLabel.setOnClickListener(v ->
                currentWorkaroundSwitch.setChecked(!currentWorkaroundSwitch.isChecked()));

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
                        //Snackbar.make(view, "ACC Enabled", Snackbar.LENGTH_SHORT).setAnchorView(R.id.configsFragment).show();
                        Runtime.getRuntime().exec(context.getString(R.string.command_start_acc));
                        acc_enabled = true;
                    } else {
                        //Snackbar.make(view, "ACC Disabled", Snackbar.LENGTH_SHORT).setAnchorView(R.id.configsFragment).show();
                        Runtime.getRuntime().exec(context.getString(R.string.command_disable_charging));
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

        CompoundButton.OnCheckedChangeListener passThrSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean passThr_enabled;
                try {
                    if (isChecked) {
                        //Snackbar.make(view, "Pass through enabled", Snackbar.LENGTH_SHORT).show();
                        Runtime.getRuntime().exec(context.getString(R.string.command_pass_through_enable));
                        passThr_enabled = true;
                        float pause = passThrSlider.getValue();
                        setChargeLimit(pause - 1, pause, view);
                        passThrSlider.setValue(pause);
                        chargeLevelSlider.setVisibility(View.GONE);
                        passThrSlider.setVisibility(View.VISIBLE);

                    } else {
                        //Snackbar.make(view, "Pass through disabled", Snackbar.LENGTH_SHORT).setAnchorView(R.id.configsFragment).show();
                        Runtime.getRuntime().exec(context.getString(R.string.command_pass_through_disable));
                        passThr_enabled = false;
                        Float pause = chargeLevelSlider.getValues().get(1);
                        float resume = chargeLevelSlider.getValues().get(1) - 5;
                        chargeLevelSlider.setValues(resume, pause);
                        setChargeLimit(resume, pause, view);
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

        CompoundButton.OnCheckedChangeListener forceOffSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean forceOff_enabled;
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec(context.getString(R.string.command_force_off_enable));
                        forceOff_enabled = true;
                    } else {
                        Runtime.getRuntime().exec(context.getString(R.string.command_force_off_disable));
                        forceOff_enabled = false;
                    }
                    final boolean finalForceOff_enabled = forceOff_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("force_off");
                        d_enabled.value = Boolean.toString(finalForceOff_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        forceOffSwitch.setOnCheckedChangeListener(forceOffSwitch_listener);

        CompoundButton.OnCheckedChangeListener offMidSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean switch_enabled;
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec(context.getString(R.string.command_off_mid_enable));
                        switch_enabled = true;
                    } else {
                        Runtime.getRuntime().exec(context.getString(R.string.command_off_mid_disable));
                        switch_enabled = false;
                    }
                    final boolean finalSwitch_enabled = switch_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("off_mid");
                        d_enabled.value = Boolean.toString(finalSwitch_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        offMidSwitch.setOnCheckedChangeListener(offMidSwitch_listener);

        CompoundButton.OnCheckedChangeListener idleAboveSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean switch_enabled;
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec(context.getString(R.string.command_allow_idle_above_enable));
                        switch_enabled = true;
                    } else {
                        Runtime.getRuntime().exec(context.getString(R.string.command_allow_idle_above_disable));
                        switch_enabled = false;
                    }
                    final boolean finalSwitch_enabled = switch_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("allow_idle_above_pcap");
                        d_enabled.value = Boolean.toString(finalSwitch_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        idleAboveSwitch.setOnCheckedChangeListener(idleAboveSwitch_listener);

        CompoundButton.OnCheckedChangeListener statusWorkaroundSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean switch_enabled;
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec(context.getString(R.string.command_batt_status_workaround_enable));
                        switch_enabled = true;
                    } else {
                        Runtime.getRuntime().exec(context.getString(R.string.command_batt_status_workaround_disable));
                        switch_enabled = false;
                    }
                    final boolean finalSwitch_enabled = switch_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("batt_status_workaround");
                        d_enabled.value = Boolean.toString(finalSwitch_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        statusWorkaroundSwitch.setOnCheckedChangeListener(statusWorkaroundSwitch_listener);

        CompoundButton.OnCheckedChangeListener currentWorkaroundSwitch_listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean switch_enabled;
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec(context.getString(R.string.command_current_workaround_enable));
                        switch_enabled = true;
                    } else {
                        Runtime.getRuntime().exec(context.getString(R.string.command_current_workaround_disable));
                        switch_enabled = false;
                    }
                    final boolean finalSwitch_enabled = switch_enabled;
                    new Thread(() -> {
                        DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();
                        Data d_enabled = dataDao.getByKey("current_workaround");
                        d_enabled.value = Boolean.toString(finalSwitch_enabled);
                        dataDao.update(d_enabled);
                    }).start();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to get su permission", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        };
        currentWorkaroundSwitch.setOnCheckedChangeListener(currentWorkaroundSwitch_listener);

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
                        Runtime.getRuntime().exec(context.getString(R.string.command_default_current));
                        currentLabel.setText(getString(R.string.charging_current_description_no_limit));
                    }
                    else {
                        Runtime.getRuntime().exec(context.getString(R.string.command_max_current) + finalCurrentValue);
                        currentLabel.setText(getString(R.string.charging_current_description,finalCurrentValue));
                    }
                    //Snackbar.make(view, finalCurrentValue, Snackbar.LENGTH_SHORT).show();

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

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");

        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapsingToolbar);

        // Background like some custom roms android 12 background headers with rounded bottom corners
        ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder(
                getContext(),
                R.style.ShapeAppearance_CollapsingToolbar,
                0
        ).build();

        // Create MaterialShapeDrawable with desired fill color
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        int color = ContextCompat.getColor(requireContext(), R.color.black); // e.g. R.color.primaryColor
        materialShapeDrawable.setFillColor(ColorStateList.valueOf(color));

        // Apply the drawable as background
        ViewCompat.setBackground(collapsingToolbar, materialShapeDrawable);

        AppBarLayout appBarLayout = view.findViewById(R.id.appBar);

        final String collapsedTitle = "ACC Configurations";

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
            boolean acc_enabled_from_db = false; // Default value
            boolean prioritizeBattIdleMode = false;
            boolean forceOff = false;
            boolean offMid = false;
            boolean allowIdleAbovePcap = false;
            boolean battStatusWorkaround = false;
            boolean currentWorkaround = false;

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

                // Switches
                key = "prioritize_batt_idle_mode";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    prioritizeBattIdleMode = true;
                }

                key = "force_off";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    forceOff = true;
                }

                key = "off_mid";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    offMid = true;
                }

                key = "allow_idle_above_pcap";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    allowIdleAbovePcap = true;
                }

                key = "batt_status_workaround";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    battStatusWorkaround = true;
                }

                key = "current_workaround";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    currentWorkaround = true;
                }

                key = "daemon_enabled";
                value = dataDao.getByKey(key).value;
                if (value != null && value.equals("true")) {
                    acc_enabled_from_db = true;
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
                    final boolean finalForceOff = forceOff;
                    final boolean finalOffMid = offMid;
                    final boolean finalAllowIdleAbovePcap = allowIdleAbovePcap;
                    final boolean finalBattStatusWorkaround = battStatusWorkaround;
                    final boolean finalCurrentWorkaround = currentWorkaround;

                    getActivity().runOnUiThread(() -> {
                        chargeLevelSlider.setValues(finalInitialValues);
                        passThrSlider.setValue(finalInitialValues.get(1)); // Assuming this is correct

                        capacityLabel.setText(getString(R.string.charging_capacity_description, finalUpperCapacityString, finalLowerCapacityString));

                        if (finalPrioritizeBattIdleMode) {
                            chargeLevelSlider.setVisibility(View.GONE);
                            passThrSlider.setVisibility(View.VISIBLE);
                            fakePassThrSwitch.setChecked(true);
                            fakePassThrSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately
                        } else {
                            chargeLevelSlider.setVisibility(View.VISIBLE); // Ensure it's visible if not prioritized
                            passThrSlider.setVisibility(View.GONE);
                            fakePassThrSwitch.setChecked(false);
                            fakePassThrSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately
                        }

                        currentSlider.setValue(finalChargeCurrentValue);
                        currentLabel.setText(finalCurrentLimitDescription);

                        tempSlider.setValues(finalTempInitialValues);
                        tempLabel.setText(getString(R.string.temp_limit_description, finalTempUpperString, finalTempLowerString));

                        enableDaemonSwitch.setOnCheckedChangeListener(null); // Temporarily remove listener to prevent callback during programmatic set
                        enableDaemonSwitch.setChecked(finalAccEnabledFromDb);
                        enableDaemonSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately
                        enableDaemonSwitch.setOnCheckedChangeListener(daemonSwitch_listener); // Re-add the listener

                        offMidSwitch.setChecked(finalOffMid);
                        offMidSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately

                        forceOffSwitch.setChecked(finalForceOff);
                        forceOffSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately

                        idleAboveSwitch.setChecked(finalAllowIdleAbovePcap);
                        idleAboveSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately

                        statusWorkaroundSwitch.setChecked(finalBattStatusWorkaround);
                        statusWorkaroundSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately

                        currentWorkaroundSwitch.setChecked(finalCurrentWorkaround);
                        currentWorkaroundSwitch.jumpDrawablesToCurrentState(); // Update visual state immediately
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
        Runtime.getRuntime().exec(context.getString(R.string.command_resume_pause_capacity)
                                    + finalPause_capacity + " " + finalResume_capacity);

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

        Runtime.getRuntime().exec(context.getString(R.string.command_resume_temp) + finalResume_capacity);

        Runtime.getRuntime().exec(context.getString(R.string.command_max_temp) + finalPause_capacity);

        tempLabel.setText(getString(R.string.temp_limit_description, finalPause_capacity , finalResume_capacity ));

        new Thread(() -> {
            DataDao dataDao = ((ACCPause) context.getApplicationContext()).getDataDao();

            Data pauseData = dataDao.getByKey("max_temp");
            pauseData.value = finalPause_capacity; //Pause

            Data resumeData = dataDao.getByKey("resume_temp");
            resumeData.value = finalResume_capacity; //Resume

            dataDao.update(pauseData);
            dataDao.update(resumeData);
        }).start();

        return new String[]{finalResume_capacity,finalPause_capacity};
    }
}