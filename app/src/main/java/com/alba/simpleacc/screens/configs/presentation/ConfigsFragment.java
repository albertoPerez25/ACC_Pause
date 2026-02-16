package com.alba.simpleacc.screens.configs.presentation;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.alba.simpleacc.ACCPause;
import com.alba.simpleacc.R;
import com.alba.simpleacc.database.DataDao;
import com.alba.simpleacc.screens.configs.chargeonce.ChargeOnceActivity;
import com.alba.simpleacc.screens.configs.data.SettingsRepository;
import com.alba.simpleacc.screens.configs.domain.AppConfig;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

/**
 * Controls the visual interface for configurations
 * Observes state changes from the ViewModel to update UI components
 * Captures user inputs to trigger business logic
 */
public class ConfigsFragment extends Fragment {

    // Logic
    private ConfigsViewModel viewModel;
    private boolean isUpdatingUi = false; // Flag to prevent accidental triggers to the listeners

    // UI Components
    private MaterialSwitch enableDaemonSwitch;
    private MaterialSwitch fakePassThrSwitch;
    private MaterialSwitch forceOffSwitch;
    private MaterialSwitch offMidSwitch;
    private MaterialSwitch idleAboveSwitch;
    private MaterialSwitch statusWorkaroundSwitch;
    private MaterialSwitch currentWorkaroundSwitch;

    private RangeSlider chargeLevelSlider;
    private Slider passThrSlider;
    private Slider currentSlider;
    private RangeSlider tempSlider;

    private TextView capacityLabel;
    private TextView currentLabel;
    private TextView tempLabel;

    public ConfigsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dependency Injection
        DataDao dao = ((ACCPause) requireContext().getApplicationContext()).getDataDao();
        SettingsRepository repository = new SettingsRepository(requireContext(), dao);

        // Factory to pass the repository to the ViewModel constructor
        ConfigsViewModelFactory factory = new ConfigsViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(ConfigsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        setupListeners(view); // Click listeners (user actions)
        setupObservers();     // LiveData observers (updates from DB)

        viewModel.loadInitialSettings();
    }

    private void initViews(View view) {
        enableDaemonSwitch = view.findViewById(R.id.mySwitch);
        fakePassThrSwitch = view.findViewById(R.id.fakePassThrSwitch);
        forceOffSwitch = view.findViewById(R.id.forceOffSwitch);
        offMidSwitch = view.findViewById(R.id.offMidSwitch);
        idleAboveSwitch = view.findViewById(R.id.idleAboveSwitch);
        statusWorkaroundSwitch = view.findViewById(R.id.statusWorkaroundSwitch);
        currentWorkaroundSwitch = view.findViewById(R.id.currentWorkaroundSwitch);

        chargeLevelSlider = view.findViewById(R.id.capacitySlider);
        currentSlider = view.findViewById(R.id.currentSlider);
        passThrSlider = view.findViewById(R.id.passThgLevelSlider);
        tempSlider = view.findViewById(R.id.tempSlider);

        capacityLabel = view.findViewById(R.id.capacityLabel);
        currentLabel = view.findViewById(R.id.currentLabel);
        tempLabel = view.findViewById(R.id.tempLabel);
    }

    private void setupObservers() {
        // Executes when the ViewModel loads the data or when the configuration changes.
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            isUpdatingUi = true;
            try {
                // Update Switches (Remove listeners temporarily to avoid loops)
                updateSwitchState(enableDaemonSwitch, state.daemonEnabled);
                updateSwitchState(fakePassThrSwitch, state.passThrough);
                updateSwitchState(forceOffSwitch, state.forceOff);
                updateSwitchState(offMidSwitch, state.offMid);
                updateSwitchState(idleAboveSwitch, state.idleAbove);
                updateSwitchState(statusWorkaroundSwitch, state.statusWorkaround);
                updateSwitchState(currentWorkaroundSwitch, state.currentWorkaround);

                // Update Sliders & Labels
                float displayResume = state.passThrough ?
                        (state.pauseCapacity - 1f) : state.resumeCapacity; // Format text based on pass-through state

                chargeLevelSlider.setValues(state.resumeCapacity, state.pauseCapacity);
                passThrSlider.setValue(state.pauseCapacity);
                capacityLabel.setText(getString(R.string.charging_capacity_description,
                        fmt(state.pauseCapacity), fmt(displayResume)));

                currentSlider.setValue(state.currentLimit);
                if (state.currentLimit == 0) {
                    currentLabel.setText(getString(R.string.charging_current_description_no_limit));
                } else {
                    currentLabel.setText(getString(R.string.charging_current_description, fmt(state.currentLimit)));
                }

                tempSlider.setValues(state.tempResume, state.tempMax);
                tempLabel.setText(getString(R.string.temp_limit_description,
                        fmt(state.tempMax), fmt(state.tempResume)));

                // UI Logic for visibility
                togglePassThroughUI(state.passThrough);
            } finally {
                isUpdatingUi = false;
            }
        });
    }

    private void setupListeners(View view) {
        // Generic Switches
        bindSwitch(enableDaemonSwitch, AppConfig.DAEMON);
        bindSwitch(fakePassThrSwitch, AppConfig.PASS_THROUGH);
        bindSwitch(forceOffSwitch, AppConfig.FORCE_OFF);
        bindSwitch(offMidSwitch, AppConfig.OFF_MID);
        bindSwitch(idleAboveSwitch, AppConfig.IDLE_ABOVE);
        bindSwitch(statusWorkaroundSwitch, AppConfig.STATUS_WORKAROUND);
        bindSwitch(currentWorkaroundSwitch, AppConfig.CURRENT_WORKAROUND);

        // Label Clicks (Delegate to Switch)
        setupLabelClick(view, R.id.daemonLabel, enableDaemonSwitch);
        setupLabelClick(view, R.id.fakePassThrLabel, fakePassThrSwitch);
        setupLabelClick(view, R.id.forceOffLabel, forceOffSwitch);
        setupLabelClick(view, R.id.offMidLabel, offMidSwitch);
        setupLabelClick(view, R.id.idleAboveLabel, idleAboveSwitch);
        setupLabelClick(view, R.id.statusWorkaroundLabel, statusWorkaroundSwitch);
        setupLabelClick(view, R.id.currentWorkaroundLabel, currentWorkaroundSwitch);

        // Sliders

        // Capacity (Range)
        chargeLevelSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                float resume = chargeLevelSlider.getValues().get(0);
                float pause = chargeLevelSlider.getValues().get(1);
                viewModel.onCapacityChanged(pause, resume);
                // Update text immediately for responsiveness
                capacityLabel.setText(getString(R.string.charging_capacity_description, fmt(pause), fmt(resume)));
            }
        });

        // PassThrough (Single)
        passThrSlider.addOnChangeListener((slider, pause, fromUser) -> {
            if (fromUser) {
                viewModel.onPassThroughCapacityChanged(pause);
            }
        });

        // Current
        currentSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.onCurrentChanged(value);
                if (value == 0) {
                    currentLabel.setText(getString(R.string.charging_current_description_no_limit));
                } else {
                    currentLabel.setText(getString(R.string.charging_current_description, fmt(value)));
                }
            }
        });

        // Temperature
        tempSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                float resume = tempSlider.getValues().get(0);
                float pause = tempSlider.getValues().get(1);
                viewModel.onTemperatureChanged(pause, resume);
                tempLabel.setText(getString(R.string.temp_limit_description, fmt(pause), fmt(resume)));
            }
        });

        // Misc
        view.findViewById(R.id.chargeOnceTo).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChargeOnceActivity.class)));
    }

    //Helper Methods

    /**
     * Binds a Switch to a feature using the AppConfig enum.
     */
    private void bindSwitch(MaterialSwitch sw, AppConfig config) {
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUi) {
                return; // Prevent triggering from programmatic changes
            }

            viewModel.onFeatureToggled(config, isChecked);

            if (config == AppConfig.PASS_THROUGH) {
                togglePassThroughUI(isChecked);
            }
        });
    }

    private void setupLabelClick(View root, int labelId, MaterialSwitch sw) {
        root.findViewById(labelId).setOnClickListener(v -> sw.toggle());
    }

    private void togglePassThroughUI(boolean isEnabled) {
        if (isEnabled) {
            chargeLevelSlider.setVisibility(View.GONE);
            passThrSlider.setVisibility(View.VISIBLE);
        } else {
            chargeLevelSlider.setVisibility(View.VISIBLE);
            passThrSlider.setVisibility(View.GONE);
        }
    }

    // Format float to string
    private String fmt(float value) {
        return String.valueOf((int) value);
    }

    // Toolbar Setup
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        AppBarLayout appBarLayout = view.findViewById(R.id.appBar);

        // Setup Shape
        ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder(
                requireContext(), R.style.ShapeAppearance_CollapsingToolbar, 0).build();
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
        materialShapeDrawable.setFillColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
        ViewCompat.setBackground(collapsingToolbar, materialShapeDrawable);

        // Setup Collapse Animation
        final String collapsedTitle = "ACC Configurations";
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isTitleVisible = true;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                scrollRange = appBarLayout.getTotalScrollRange();
                float collapseFactor = 1f - (Math.abs(verticalOffset) / (float) scrollRange);
                if (collapseFactor < 0.1f && !isTitleVisible) {
                    toolbar.setTitle(collapsedTitle);
                    toolbar.animate().alpha(1f).setDuration(200).start();
                    isTitleVisible = true;
                } else if (collapseFactor > 0.1f && isTitleVisible) {
                    toolbar.animate().alpha(0f).setDuration(200).start();
                    isTitleVisible = false;
                }
            }
        });
    }

    // ViewModel Factory (Internal Class)
    // Necessary to pass arguments (Repository) to the ViewModel constructor
    static class ConfigsViewModelFactory implements ViewModelProvider.Factory {
        private final SettingsRepository repository;

        public ConfigsViewModelFactory(SettingsRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ConfigsViewModel.class)) {
                return (T) new ConfigsViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    /**
     * Update switch visually without triggering animations
     */
    private void updateSwitchState(MaterialSwitch sw, boolean isChecked) {
        if (sw.isChecked() != isChecked) {
            sw.setChecked(isChecked);
            sw.jumpDrawablesToCurrentState();
        }
    }
}