package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.shun.osugi.busible.dao.DateDao;
import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.entity.Date;

public class DateViewModel extends AndroidViewModel {

    private DateDao dateDao;
    private LiveData<List<Date>> allDates;

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
    public long insert(Date date) {
        // ExecutorService を使ってスレッドを管理し、非同期処理の完了を future.get() で待つ
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Long> callable = () -> {
            long newId = dateDao.insert(date);
            date.setId((int) newId);
            return newId;
        };

        Future<Long> future = executor.submit(callable);
        long newId = 0;
        try {
            newId = future.get();  // `get()` で実行完了を待って ID を取得
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return newId;
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
