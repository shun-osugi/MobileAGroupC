package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.github.shun.osugi.busible.dao.DateDao;
import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.entity.Date;

public class DateViewModel extends AndroidViewModel {

    private DateDao dateDao;
    private LiveData<List<Date>> allDates;
    private LiveData<Date> dateBySpecificDay;

    public DateViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        dateDao = db.dateDao();
        allDates = dateDao.getAllDates();
    }

    // 全ての日付を取得
    public LiveData<List<Date>> getAllDates() {
        return allDates;
    }

    // 特定の日付を取得
    public LiveData<Date> getDateBySpecificDay(int year, int month, int day) {
        return dateDao.getDateBySpecificDay(year, month, day);
    }

    // IDで日付を取得
    public LiveData<Date> getDateById(int id) {
        return dateDao.getDateById(id);
    }

    // 日付を挿入
    public void insert(Date date) {
        new Thread(() -> {
            long newId = dateDao.insert(date);
            date.setId((int) newId); // 自動生成されたIDをセット
        }).start();
    }

    // 日付を複数挿入
    public void insertAll(List<Date> dates) {
        new Thread(() -> dateDao.insertAll(dates)).start();
    }

    // 日付を更新
    public void update(Date date) {
        new Thread(() -> dateDao.update(date)).start();
    }

    // 日付を削除
    public void delete(Date date) {
        new Thread(() -> dateDao.delete(date)).start();
    }
}
