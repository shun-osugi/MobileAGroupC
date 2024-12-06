package io.github.shun.osugi.busible.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;
import io.github.shun.osugi.busible.R;

public class Activity extends AppCompatActivity {

    private static final String TAG = "Activity";
    private ScheduleViewModel scheduleViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_);

        // ViewModelの初期化
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // データ挿入、更新、削除、取得を確認
        testDatabaseOperations();
    }

    private void testDatabaseOperations() {
        // 新しいスケジュールを作成
        Schedule newSchedule = new Schedule();
        newSchedule.setDateId(1);
        newSchedule.setTitle("Test Schedule");
        newSchedule.setStartTime("10:00");
        newSchedule.setEndTime("11:00");
        newSchedule.setRepeat("Daily");

        // 1. データベースに挿入
        scheduleViewModel.insert(newSchedule);

        // 2. 全スケジュールを取得して確認
        scheduleViewModel.getAllSchedules().observe(this, new Observer<List<Schedule>>() {
            @Override
            public void onChanged(List<Schedule> schedules) {
                Log.d("Activity", "All Schedules: " + schedules);
            }
        });

        // 3. データを更新
        newSchedule.setTitle("Updated Test Schedule");
        scheduleViewModel.update(newSchedule);

        // 4. IDを指定して取得
        scheduleViewModel.getScheduleById(1).observe(this, new Observer<Schedule>() {
            @Override
            public void onChanged(Schedule schedule) {
                if (schedule != null) {
                    Log.d(TAG, "Schedule By ID: " + schedule.getTitle());
                }
            }
        });

        // 5. データを削除
        scheduleViewModel.delete(newSchedule);

        // 削除後の確認
        scheduleViewModel.getScheduleById(1).observe(this, new Observer<Schedule>() {
            @Override
            public void onChanged(Schedule schedule) {
                if (schedule == null) {
                    Log.d(TAG, "Schedule deleted successfully.");
                }
            }
        });
    }
}
