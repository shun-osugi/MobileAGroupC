package io.github.shun.osugi.busible.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.github.shun.osugi.busible.entity.Schedule;

@Dao
public interface ScheduleDao {
    // スケジュールの挿入
    @Insert
    long insert(Schedule schedule);

    // スケジュールの更新
    @Update
    void update(Schedule schedule);

    // スケジュールの削除
    @Delete
    void delete(Schedule schedule);

    // idでスケジュールを取得
    @Query("SELECT * FROM schedule WHERE id = :id")
    LiveData<Schedule> getScheduleById(int id);

    // dateIdでスケジュールを取得
    @Query("SELECT * FROM schedule WHERE Dateid = :dateId")
    LiveData<List<Schedule>> getSchedulesByDateId(int dateId);

    // 全スケジュールを取得
    @Query("SELECT * FROM schedule")
    LiveData<List<Schedule>> getAllSchedules();

    // 繰り返し設定でスケジュールを取得
    @Query("SELECT * FROM schedule WHERE repeat = :repeat")
    LiveData<List<Schedule>> getSchedulesByRepeat(Boolean repeat);

    // 特定の開始時間を持つスケジュールを取得
    //@Query("SELECT * FROM schedule WHERE startTime = :startTime")
    //LiveData<List<Schedule>> getSchedulesByStartTime(String startTime);

    // 特定の終了時間を持つスケジュールを取得
    //@Query("SELECT * FROM schedule WHERE endTime = :endTime")
    //LiveData<List<Schedule>> getSchedulesByEndTime(String endTime);

}