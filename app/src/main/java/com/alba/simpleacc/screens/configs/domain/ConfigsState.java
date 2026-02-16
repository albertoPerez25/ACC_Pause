package com.alba.simpleacc.screens.configs.domain;

/** Centralized acc configs data in one class **/
public class ConfigsState {
    public float resumeCapacity = SliderConfig.RESUME_CAPACITY.defaultValue;
    public float pauseCapacity = SliderConfig.PAUSE_CAPACITY.defaultValue;
    public float currentLimit = SliderConfig.CURRENT.defaultValue;
    public float tempResume = SliderConfig.TEMP_RESUME.defaultValue;
    public float tempMax = SliderConfig.TEMP_MAX.defaultValue;

    // Switches states
    public boolean daemonEnabled = AppConfig.DAEMON.defaultValue;
    public boolean passThrough = AppConfig.PASS_THROUGH.defaultValue;
    public boolean forceOff = AppConfig.FORCE_OFF.defaultValue;
    public boolean offMid = AppConfig.OFF_MID.defaultValue;
    public boolean idleAbove = AppConfig.IDLE_ABOVE.defaultValue;
    public boolean statusWorkaround = AppConfig.STATUS_WORKAROUND.defaultValue;
    public boolean currentWorkaround = AppConfig.CURRENT_WORKAROUND.defaultValue;
}