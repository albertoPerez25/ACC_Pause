package com.alba.accpause.database;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DataDao {
    @Query("SELECT * FROM Data")
    List<Data> getAll();

    @Query("SELECT * FROM Data WHERE `key` = :key")
    Data getByKey(String key);

    @Insert
    void insertAll(Data[] data);
    @Insert
    void insert(Data data);

    @Update
    void update(Data data);
    @Update
    void updateAll(Data... data);
}
