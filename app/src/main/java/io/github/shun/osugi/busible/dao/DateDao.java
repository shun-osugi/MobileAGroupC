package io.github.shun.osugi.busible.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;


import io.github.shun.osugi.busible.entity.Date;

import java.util.List;

@Dao
public interface DateDao {

    // 非同期でDateを挿入するメソッド
    @Insert
    long insert(Date date);

    // 非同期で複数のDateを挿入するメソッド
    @Insert
    void insertAll(List<Date> dates);

    // 非同期でDateを更新するメソッド
    @Update
    void update(Date date);

    // 非同期でDateを削除するメソッド
    @Delete
    void delete(Date date);

    // 非同期で全ての日付を取得するメソッド
    @Query("SELECT * FROM date")
    LiveData<List<Date>> getAllDates();

    // 非同期で特定の日付を取得するメソッド
    @Query("SELECT * FROM date WHERE year = :year AND month = :month AND day = :day")
    LiveData<Date> getDateBySpecificDay(int year, int month, int day);

}
