package com.alba.simpleacc.screens.configs.data;

import android.content.Context;
import com.alba.simpleacc.R;
import com.alba.simpleacc.database.Data;
import com.alba.simpleacc.database.DataDao;
import com.alba.simpleacc.screens.configs.domain.AppConfig;
import com.alba.simpleacc.screens.configs.domain.SliderConfig;
import com.alba.simpleacc.screens.configs.domain.ConfigsState;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Acts as the data source for application settings
 * Executes system shell commands and updates the database asynchronously
 * Provides clean data to the presentation layer
 */
public class SettingsRepository {
    private final Context context;
    private final DataDao dataDao;
    // Use of Executor for background tasks instead of creating a new Thread() every time
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SettingsRepository(Context context, DataDao dataDao) {
        this.context = context;
        this.dataDao = dataDao;
    }

    // Load data

    public interface OnSettingsLoadedCallback {
        void onLoaded(ConfigsState state);
    }

    public void loadSettings(OnSettingsLoadedCallback callback) {
        executor.execute(() -> {
            ConfigsState state = new ConfigsState();

            // Sliders

            // Capacity (Default 0)
            state.resumeCapacity = parseFloatSafe(getDbValue(SliderConfig.RESUME_CAPACITY.dbKey), SliderConfig.RESUME_CAPACITY.defaultValue);
            state.pauseCapacity = parseFloatSafe(getDbValue(SliderConfig.PAUSE_CAPACITY.dbKey), SliderConfig.PAUSE_CAPACITY.defaultValue);

            // Current Limit (Default 0 = No limit)
            state.currentLimit = parseFloatSafe(getDbValue(SliderConfig.CURRENT.dbKey), SliderConfig.CURRENT.defaultValue);

            // Temperature (Default 50 45)
            state.tempMax = parseFloatSafe(getDbValue(SliderConfig.TEMP_MAX.dbKey), SliderConfig.TEMP_MAX.defaultValue);
            state.tempResume = parseFloatSafe(getDbValue(SliderConfig.TEMP_RESUME.dbKey), SliderConfig.TEMP_RESUME.defaultValue);

            // Switches
            state.daemonEnabled = parseBooleanSafe(getDbValue(AppConfig.DAEMON.dbKey), AppConfig.DAEMON.defaultValue);
            state.passThrough = parseBooleanSafe(getDbValue(AppConfig.PASS_THROUGH.dbKey), AppConfig.PASS_THROUGH.defaultValue);
            state.forceOff = parseBooleanSafe(getDbValue(AppConfig.FORCE_OFF.dbKey), AppConfig.FORCE_OFF.defaultValue);
            state.offMid = parseBooleanSafe(getDbValue(AppConfig.OFF_MID.dbKey), AppConfig.OFF_MID.defaultValue);
            state.idleAbove = parseBooleanSafe(getDbValue(AppConfig.IDLE_ABOVE.dbKey), AppConfig.IDLE_ABOVE.defaultValue);
            state.statusWorkaround = parseBooleanSafe(getDbValue(AppConfig.STATUS_WORKAROUND.dbKey), AppConfig.STATUS_WORKAROUND.defaultValue);
            state.currentWorkaround = parseBooleanSafe(getDbValue(AppConfig.CURRENT_WORKAROUND.dbKey), AppConfig.CURRENT_WORKAROUND.defaultValue);

            // Return the populated state to the Main Thread via callback
            callback.onLoaded(state);
        });
    }

    // Execute actions

    public void toggleFeature(AppConfig feature, boolean isEnabled) {
        executor.execute(() -> {
            int commandRes = isEnabled ? feature.enableCommandRes : feature.disableCommandRes;
            runShellCommand(context.getString(commandRes));
            updateDb(feature.dbKey, Boolean.toString(isEnabled));
        });
    }

    public void updateCapacity(float pause, float resume) {
        executor.execute(() -> {
            // Logic: remove decimal points ".0" as per original code
            String sPause = formatFloat(pause);
            String sResume = formatFloat(resume);

            String command = context.getString(R.string.command_resume_pause_capacity) + sPause + " " + sResume;
            runShellCommand(command);

            updateDb(SliderConfig.PAUSE_CAPACITY.dbKey, sPause);
            updateDb(SliderConfig.RESUME_CAPACITY.dbKey, sResume);
        });
    }

    public void updateCurrent(float value) {
        executor.execute(() -> {
            String sValue = formatFloat(value);

            if (sValue.equals("0")) {
                runShellCommand(context.getString(R.string.command_default_current));
            } else {
                runShellCommand(context.getString(R.string.command_max_current) + sValue);
            }
            updateDb(SliderConfig.CURRENT.dbKey, sValue);
        });
    }

    public void updateTemperature(float pause, float resume) {
        executor.execute(() -> {
            String sPause = formatFloat(pause);
            String sResume = formatFloat(resume);

            runShellCommand(context.getString(R.string.command_resume_temp) + sResume);
            runShellCommand(context.getString(R.string.command_max_temp) + sPause);

            updateDb(SliderConfig.TEMP_MAX.dbKey, sPause);
            updateDb(SliderConfig.TEMP_RESUME.dbKey, sResume);
        });
    }

    // Helpers

    private void runShellCommand(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDb(String key, String value) {
        Data data = dataDao.getByKey(key);
        if (data != null) {
            data.value = value;
            dataDao.update(data);
        }
    }

    /** Returns only the integer part of the float. */
    private String formatFloat(float value) {
        return String.valueOf((int) value);
    }

    // Helper Methods for Null Safety

    /**
     * Safely retrieves the value string from DataDao
     * Prevents NullPointerException if the row doesn't exist in DB
     */
    private String getDbValue(String key) {
        Data data = dataDao.getByKey(key);
        return (data != null) ? data.value : null;
    }

    /**
     * Parses a string to boolean. Returns false if value is null or not "true"
     */
    private boolean parseBooleanSafe(String value, boolean fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        return "true".equalsIgnoreCase(value);
    }

    private float parseFloatSafe(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}