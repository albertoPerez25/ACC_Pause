package com.alba.simpleacc.screens.configs.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alba.simpleacc.screens.configs.domain.AppConfig;
import com.alba.simpleacc.screens.configs.domain.ConfigsState;
import com.alba.simpleacc.screens.configs.data.SettingsRepository;

/**
 * Bridge between the UI and data layers
 * Manages UI state and sanitizes database values to prevent slider crashes
 * Processes user interactions by delegating commands to the repository
 */
public class ConfigsViewModel extends ViewModel {

    private final SettingsRepository repository;
    private final MutableLiveData<ConfigsState> uiState = new MutableLiveData<>();

    public ConfigsViewModel(SettingsRepository repository) {
        this.repository = repository;
    }

    /**
     * Exposes the current configuration state to the Fragment
     * The Fragment should observe this to update UI elements (sliders, switches) initially
     */
    public LiveData<ConfigsState> getUiState() {
        return uiState;
    }

    /**
     * Loads the initial configuration from the Database asynchronously
     * This replaces the old massive thread in the Fragment's onViewCreated
     */
    public void loadInitialSettings() {
        repository.loadSettings(state -> {
            // Stop the UI crashing because of the step size
            sanitizeStateForSliders(state);
            // Post value to main thread
            uiState.postValue(state);
        });
    }

    /**
     * Prevents UI synchronization errors and potential deadlocks by enforcing
     * the Slider's discrete step size
     */
    private void sanitizeStateForSliders(ConfigsState state) {
        // Sliders with a stepSize of 5 will throw exceptions if the data isn't
        // multipliers of 5
        state.pauseCapacity = snapToMultipleOf5(state.pauseCapacity);
        state.resumeCapacity = snapToMultipleOf5(state.resumeCapacity);

        // Clamping to prevent illegal values
        state.pauseCapacity = Math.max(0f, Math.min(100f, state.pauseCapacity));
        state.resumeCapacity = Math.max(0f, Math.min(100f, state.resumeCapacity));

        // Distinct buffer between thresholds to prevent more illegal values
        if (state.resumeCapacity >= state.pauseCapacity) {
            state.resumeCapacity = state.pauseCapacity - 5f;

            // the Resume threshold cannot drop below zero.
            if (state.resumeCapacity < 0f) {
                state.resumeCapacity = 0f;
                state.pauseCapacity = 5f;
            }
        }
    }

    /**
     * snaps to the nearest multiple of 5
     */
    private float snapToMultipleOf5(float value) {
        return (float) (Math.round(value / 5.0) * 5.0);
    }

    /**
     * Handles Switches
     * @param feature The specific feature enum (e.g., DAEMON, FORCE_OFF)
     * @param isEnabled The new state of the switch
     */
    public void onFeatureToggled(AppConfig feature, boolean isEnabled) {
        repository.toggleFeature(feature, isEnabled);

        // Handle pass-through specific capacity logic
        if (feature == AppConfig.PASS_THROUGH) {
            ConfigsState currentState = uiState.getValue();

            if (currentState != null) {
                currentState.passThrough = isEnabled;

                if (isEnabled) {
                    // Reuse slider logic to apply tight limits
                    onPassThroughCapacityChanged(currentState.pauseCapacity);
                } else {
                    // Restore safe slider values to daemon
                    repository.updateCapacity(currentState.pauseCapacity, currentState.resumeCapacity);
                    uiState.postValue(currentState);
                }
            }
        }
    }

    /**
     * Handle single slider changes during pass-through
     */
    public void onPassThroughCapacityChanged(float pause) {
        ConfigsState currentState = uiState.getValue();
        if (currentState != null) {
            currentState.pauseCapacity = pause;
            uiState.postValue(currentState);
        }
        // Apply tight range to daemon
        repository.updateCapacity(pause, pause - 1f);
    }

    /**
     * Handles changes in Capacity Slider and PassThrough slider logic
     * Logic extracted from setChargeLimit()
     *
     * @param pause The upper limit (stop charging)
     * @param resume The lower limit (start charging)
     */
    public void onCapacityChanged(float pause, float resume) {
        // In the original code, string manipulation was done here
        // Raw floats to the repository to keep ViewModel clean
        ConfigsState currentState = uiState.getValue();
        if (currentState != null) {
            currentState.pauseCapacity = pause;
            currentState.resumeCapacity = resume;
        }
        repository.updateCapacity(pause, resume);
    }

    /**
     * Handles changes in Current Slider
     * Logic extracted from currentSlider listener
     *
     * @param value The selected current value (e.g., 500, 1000, or 0 for default).
     */
    public void onCurrentChanged(float value) {
        repository.updateCurrent(value);
    }

    /**
     * Handles changes in Temperature Slider
     * Logic extracted from setTempLimit()
     *
     * @param pause The max temp limit
     * @param resume The resume temp limit
     */
    public void onTemperatureChanged(float pause, float resume) {
        repository.updateTemperature(pause, resume);
    }
}