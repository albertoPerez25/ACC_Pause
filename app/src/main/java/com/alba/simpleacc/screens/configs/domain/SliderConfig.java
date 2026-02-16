package com.alba.simpleacc.screens.configs.domain;

/**
 * Centralizes configuration constants for numerical features
 * Links database keys with their safe default fallback values
 */
public enum SliderConfig {
    RESUME_CAPACITY("resume_capacity", 0f),
    PAUSE_CAPACITY("pause_capacity", 0f),
    CURRENT("max_charging_current", 0f),
    TEMP_MAX("max_temp", 50f),
    TEMP_RESUME("resume_temp", 45f);

    public final String dbKey;
    public final float defaultValue;

    SliderConfig(String dbKey, float defaultValue) {
        this.dbKey = dbKey;
        this.defaultValue = defaultValue;
    }
}
