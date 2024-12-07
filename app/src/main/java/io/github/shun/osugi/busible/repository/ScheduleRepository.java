package io.github.shun.osugi.busible.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

import io.github.shun.osugi.busible.dao.ScheduleDao;
import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.entity.Schedule;

public class ScheduleRepository {

    public ScheduleDao scheduleDao; // DAOへの参照

    // コンストラクタでデータベースのインスタンスを取得
    public ScheduleRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application); // AppDatabaseを使ってDBインスタンスを取得
        scheduleDao = db.scheduleDao(); // DAOを取得
    }

    // 日付IDに基づいてスケジュールを取得
    public LiveData<List<Schedule>> getSchedulesByDateId(int dateId) {
        return scheduleDao.getSchedulesByDateId(dateId); // DAOメソッドを呼び出し
    }

    // IDでScheduleを取得
    public LiveData<Schedule> getScheduleById(int id) {
        return scheduleDao.getScheduleById(id);
    }
}
