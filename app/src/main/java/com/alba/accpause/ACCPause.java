package com.alba.accpause;

import android.app.Application;

import androidx.room.Room;

import com.alba.accpause.database.AppDatabase;
import com.alba.accpause.database.DataDao;
import com.alba.accpause.database.ProcessParser;
import com.google.android.material.color.DynamicColors;

public class ACCPause extends Application {

    private AppDatabase database;
    @Override
    public void onCreate() {
        super.onCreate();
        this.updateDatabase();
        DynamicColors.applyToActivitiesIfAvailable(this);
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "ACC_configs").build();
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public DataDao getDataDao() {
        return database.dataDao();
    }

    private void updateDatabase(){
        new Thread(() -> {
            ProcessParser.updateConfigsDatabase("/dev/acc --set",getApplicationContext());
        }).start();
    }
}

