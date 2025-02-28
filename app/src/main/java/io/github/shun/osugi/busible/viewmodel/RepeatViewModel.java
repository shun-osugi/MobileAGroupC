package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.dao.RepeatDao;
import io.github.shun.osugi.busible.entity.Repeat;
import io.github.shun.osugi.busible.entity.Schedule;

public class RepeatViewModel extends AndroidViewModel {
    private final RepeatDao repeatDao;
    private final ExecutorService executorService;

    public RepeatViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        repeatDao = database.repeatDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Repeat repeat) {
        executorService.execute(() -> repeatDao.insert(repeat));
    }

    public void update(Repeat repeat) {
        executorService.execute(() -> repeatDao.update(repeat));
    }

    public void delete(Repeat repeat) {
        executorService.execute(() -> repeatDao.delete(repeat));
    }

    public LiveData<List<Repeat>> getAllRepeats() {
        return repeatDao.getAllRepeats();
    }

    public LiveData<Repeat> getRepeatById(int id) {
        return repeatDao.getRepeatById(id);
    }

    // scheduleIdでスケジュールを取得
    public LiveData<List<Repeat>> getRepeatByScheduleId(int id) {
        return repeatDao.getRepeatsByScheduleId(id);
    }

    }
