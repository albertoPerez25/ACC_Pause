package com.alba.accpause.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Data {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "key")
    public String key;
    @ColumnInfo(name = "value")
    public String value;
}
