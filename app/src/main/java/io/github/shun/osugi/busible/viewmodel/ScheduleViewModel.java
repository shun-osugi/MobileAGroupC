package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.github.shun.osugi.busible.dao.ScheduleDao;
import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.entity.Schedule;

public class ScheduleViewModel extends AndroidViewModel {
    private ScheduleDao scheduleDao;
    private LiveData<List<Schedule>> allSchedules;
    private LiveData<Schedule> scheduleById;
    private LiveData<List<Schedule>> schedulesByRepeat;
    private LiveData<List<Schedule>> schedulesByStartTime;
    private LiveData<List<Schedule>> schedulesByEndTime;

    public ScheduleViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        scheduleDao = db.scheduleDao();
        allSchedules = scheduleDao.getAllSchedules();
    }

    // スケジュールの挿入
    public void insert(Schedule schedule) {
        new Thread(() -> scheduleDao.insert(schedule)).start();
    }

    // スケジュールの更新
    public void update(Schedule schedule) {
        new Thread(() -> scheduleDao.update(schedule)).start();
    }

    // スケジュールの削除
    public void delete(Schedule schedule) {
        new Thread(() -> scheduleDao.delete(schedule)).start();
    }

    // IDでスケジュールを取得
    public LiveData<Schedule> getScheduleById(int id) {
        return scheduleDao.getScheduleById(id);
    }

    // 全スケジュールを取得
    public LiveData<List<Schedule>> getAllSchedules() {
        return allSchedules;
    }


}
