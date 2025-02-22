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

public class RepeatViewModel extends AndroidViewModel {
    private final RepeatDao repeatDao;
    private final ExecutorService executorService;

    public RepeatViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        repeatDao = database.repeatDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // 繰り返し予定を挿入
    public void insert(Repeat repeat) {
        executorService.execute(() -> repeatDao.insert(repeat));
    }

    // 繰り返し予定を更新
    public void update(Repeat repeat) {
        executorService.execute(() -> repeatDao.update(repeat));
    }

    // 繰り返し予定を削除
    public void delete(Repeat repeat) {
        executorService.execute(() -> repeatDao.delete(repeat));
    }

    // 全ての繰り返し予定を取得
    public LiveData<List<Repeat>> getAllRepeats() {
        return repeatDao.getAllRepeats();
    }

    // 特定の種類の繰り返し予定を取得
    public LiveData<List<Repeat>> getSpecificRepeats(String repeatType) {
        return repeatDao.getSpecificRepeats(repeatType);
    }

    // IDで繰り返し予定を取得
    public LiveData<Repeat> getRepeatById(int id) {
        return repeatDao.getRepeatById(id);
    }

    }
