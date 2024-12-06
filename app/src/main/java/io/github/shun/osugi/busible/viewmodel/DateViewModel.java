package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.github.shun.osugi.busible.dao.DateDao;
import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.entity.Date;

public class DateViewModel extends AndroidViewModel {

    private DateDao dateDao;
    private LiveData<List<Date>> allDates;
    private LiveData<Date> dateBySpecificDay;
    private MutableLiveData<Boolean> insertSuccess;

    public DateViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        dateDao = db.dateDao();
    }

    // LiveDataを返すメソッド
    public LiveData<Boolean> getInsertSuccess() {
        return insertSuccess;
    }

    // 全ての日付を取得
    public LiveData<List<Date>> getAllDates() {
        return allDates;
    }

    // 特定の日付を取得
    public LiveData<Date> getDateBySpecificDay(int year, int month, int day) {
        return dateDao.getDateBySpecificDay(year, month, day);
    }

    // 日付を挿入
    public long insert(Date date) {
        new Thread(() -> {
            long newId = dateDao.insert(date);
            date.setId((int) newId); // 自動生成されたIDをセット
            // 保存成功後の処理
            Log.d("AddScheduleActivity", "Date 保存成功, ID: " + newId);
            insertSuccess.postValue(true); // 保存完了を通知

        }).start();
        return 0;
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
