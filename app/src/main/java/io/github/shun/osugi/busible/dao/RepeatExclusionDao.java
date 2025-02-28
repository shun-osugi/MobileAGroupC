package io.github.shun.osugi.busible.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;

import io.github.shun.osugi.busible.entity.RepeatExclusion;

@Dao
public interface RepeatExclusionDao {
    @Insert
    void insert(RepeatExclusion exclusion);

    @Delete
    void delete(RepeatExclusion exclusion);

    @Query("SELECT * FROM repeat_exclusion WHERE repeatId = :repeatId")
    LiveData<List<RepeatExclusion>> getExclusionsForRepeat(int repeatId);
}

