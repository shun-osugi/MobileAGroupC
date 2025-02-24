package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.dao.RepeatExclusionDao;
import io.github.shun.osugi.busible.entity.Repeat;
import io.github.shun.osugi.busible.entity.RepeatExclusion;

public class RepeatExclusionViewModel extends AndroidViewModel {
    private final RepeatExclusionDao repeatExclusionDao;
    private final ExecutorService executorService;

    public RepeatExclusionViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        repeatExclusionDao = database.repeatExclusionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /*public void insert(RepeatExclusion exclusion) {
        executorService.execute(() -> repeatExclusionDao.insert(exclusion));
    }*/
    public long insert(RepeatExclusion exclusion) {
        // ExecutorService を使ってスレッドを管理し、非同期処理の完了を future.get() で待つ
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Long> callable = () -> {
            long newId = repeatExclusionDao.insert(exclusion);
            exclusion.setId((int) newId);
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


    public void delete(RepeatExclusion exclusion) {
        executorService.execute(() -> repeatExclusionDao.delete(exclusion));
    }

    public List<RepeatExclusion> getExclusionsForRepeat(int repeatId) {
        return repeatExclusionDao.getExclusionsForRepeat(repeatId);
    }
}
