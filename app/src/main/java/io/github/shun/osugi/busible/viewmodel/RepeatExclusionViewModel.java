package io.github.shun.osugi.busible.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.shun.osugi.busible.database.AppDatabase;
import io.github.shun.osugi.busible.dao.RepeatExclusionDao;
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

    public void insert(RepeatExclusion exclusion) {
        executorService.execute(() -> repeatExclusionDao.insert(exclusion));
    }

    public void delete(RepeatExclusion exclusion) {
        executorService.execute(() -> repeatExclusionDao.delete(exclusion));
    }

    public List<RepeatExclusion> getExclusionsForRepeat(int repeatId) {
        return repeatExclusionDao.getExclusionsForRepeat(repeatId);
    }
}
