package io.github.shun.osugi.busible.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import io.github.shun.osugi.busible.dao.ScheduleDao;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.dao.DateDao;
import io.github.shun.osugi.busible.entity.Date;

@Database(entities = {Schedule.class, Date.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ScheduleDao scheduleDao();
    // DateDaoのインターフェースを提供
    public abstract DateDao dateDao();

    // Singletonパターンでインスタンスを一度だけ作成する
    private static volatile AppDatabase INSTANCE;

    // データベースインスタンスの取得
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
  // Roomデータベースのインスタンスを作成                  
  INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "busible_database")
                            .fallbackToDestructiveMigration() // バージョン変更時にデータベースを再作成
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
