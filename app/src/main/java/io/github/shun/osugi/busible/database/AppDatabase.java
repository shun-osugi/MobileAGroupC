package io.github.shun.osugi.busible.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.github.shun.osugi.busible.dao.ScheduleDao;
import io.github.shun.osugi.busible.entity.Schedule;

@Database(entities = {Schedule.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ScheduleDao scheduleDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "schedule_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
