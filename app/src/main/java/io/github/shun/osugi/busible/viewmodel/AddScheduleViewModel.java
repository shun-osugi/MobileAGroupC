package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.dao.ScheduleDao;
import io.github.shun.osugi.busible.entity.Schedule;

import android.os.AsyncTask;

import java.util.List;

public class AddScheduleViewModel extends AndroidViewModel {
    private final ScheduleDao scheduleDao;
    private final LiveData<List<Schedule>> allSchedules;
    private final MutableLiveData<Boolean> insertSuccess = new MutableLiveData<>();

    public AddScheduleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        scheduleDao = db.scheduleDao();
        allSchedules = scheduleDao.getAllSchedules();
    }

    public LiveData<List<Schedule>> getAllSchedules() {
        return allSchedules;
    }

    public LiveData<Boolean> getInsertSuccess() {
        return insertSuccess;
    }

    // 非同期で挿入し、成功した場合に通知を設定
    public void insert(Schedule schedule) {
        new InsertAsyncTask(scheduleDao, insertSuccess).execute(schedule);
    }

    private static class InsertAsyncTask extends AsyncTask<Schedule, Void, Void> {
        private final ScheduleDao scheduleDao;
        private final MutableLiveData<Boolean> insertSuccess;

        InsertAsyncTask(ScheduleDao dao, MutableLiveData<Boolean> insertSuccess) {
            this.scheduleDao = dao;
            this.insertSuccess = insertSuccess;
        }

        @Override
        protected Void doInBackground(Schedule... schedules) {
            scheduleDao.insert(schedules[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            insertSuccess.postValue(true); // 挿入が完了したことを通知
        }
    }
}
