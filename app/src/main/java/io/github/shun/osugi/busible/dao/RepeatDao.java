package io.github.shun.osugi.busible.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.github.shun.osugi.busible.entity.Repeat;

@Dao
public interface RepeatDao {

    // 繰り返しデータの挿入
    @Insert
    void insert(Repeat repeat);

    // 繰り返しデータの更新
    @Update
    void update(Repeat repeat);

    // 繰り返しデータの削除
    @Delete
    void delete(Repeat repeat);

    // ID で繰り返しデータを取得
    @Query("SELECT * FROM repeat WHERE id = :id")
    LiveData<Repeat> getRepeatById(int id);

    // 特定の scheduleId を持つ繰り返しデータを取得
    @Query("SELECT * FROM repeat WHERE scheduleId = :scheduleId")
    LiveData<List<Repeat>> getRepeatsByScheduleId(int scheduleId);

    // 特定の dateId を持つ繰り返しデータを取得
    @Query("SELECT * FROM repeat WHERE dateId = :dateId")
    LiveData<List<Repeat>> getRepeatsByDateId(int dateId);

    // すべての繰り返しデータを取得
    @Query("SELECT * FROM repeat")
    LiveData<List<Repeat>> getAllRepeats();
}
