package com.alba.simpleacc.screens.configs.domain;

import com.alba.simpleacc.R;

/**
 * Centralizes configuration constants for toggle features
 * Links database keys with their corresponding shell commands and default states
 */
public enum AppConfig {
    DAEMON("daemon_enabled", R.string.command_start_acc, R.string.command_disable_charging, false),
    PASS_THROUGH("prioritize_batt_idle_mode", R.string.command_pass_through_enable, R.string.command_pass_through_disable, false),
    FORCE_OFF("force_off", R.string.command_force_off_enable, R.string.command_force_off_disable, false),
    OFF_MID("off_mid", R.string.command_off_mid_enable, R.string.command_off_mid_disable, false),
    IDLE_ABOVE("allow_idle_above_pcap", R.string.command_allow_idle_above_enable, R.string.command_allow_idle_above_disable, false),
    STATUS_WORKAROUND("batt_status_workaround", R.string.command_batt_status_workaround_enable, R.string.command_batt_status_workaround_disable, false),
    CURRENT_WORKAROUND("current_workaround", R.string.command_current_workaround_enable, R.string.command_current_workaround_disable, false);

    public final String dbKey;
    public final int enableCommandRes;
    public final int disableCommandRes;
    public final boolean defaultValue;

    AppConfig(String dbKey, int enableCmd, int disableCmd, boolean defaultValue) {
        this.dbKey = dbKey;
        this.enableCommandRes = enableCmd;
        this.disableCommandRes = disableCmd;
        this.defaultValue = defaultValue;
    }
}