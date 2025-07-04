package com.alba.accpause;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.alba.accpause.database.AppDatabase;
import com.alba.accpause.database.DataDao;
import com.alba.accpause.database.DataParser;
import com.google.android.material.color.DynamicColors;

import java.util.concurrent.CountDownLatch;

public class ACCPause extends Application {

    private AppDatabase database;
    @Override
    public void onCreate() {
        super.onCreate();
        this.updateDatabase();
        DynamicColors.applyToActivitiesIfAvailable(this);
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "ACC_configs").build();
        // Force dark mode for the entire application (just till it is completed)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public DataDao getDataDao() {
        return database.dataDao();
    }

    private void updateDatabase(){
        new Thread(() -> {
            DataParser.updateConfigsDatabase("/dev/acc --set",getApplicationContext());
            databaseInitialized.countDown();
        }).start();
    }
    public static CountDownLatch databaseInitialized = new CountDownLatch(1);
}

