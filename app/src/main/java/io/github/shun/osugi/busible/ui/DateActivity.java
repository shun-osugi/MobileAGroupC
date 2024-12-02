package io.github.shun.osugi.busible.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.R;


public class DateActivity extends AppCompatActivity {

    private static final String TAG = "DateActivity";
    private DateViewModel dateViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);

        // ViewModelの初期化
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // データ挿入、更新、削除、取得を確認
        testDatabaseOperations();
    }

    private void testDatabaseOperations() {
        // 新しい日付を作成
        Date newDate = new Date();
        newDate.setYear(2024);
        newDate.setMonth(12);
        newDate.setDay(2);

        // 1. 日付をデータベースに挿入
        dateViewModel.insert(newDate);

        // 2. 全ての日付を取得して確認
        dateViewModel.getAllDates().observe(this, new Observer<List<Date>>() {
            @Override
            public void onChanged(List<Date> dates) {
                Log.d(TAG, "All Dates: " + dates);
            }
        });

        // 3. 特定の日付を取得
        dateViewModel.getDateBySpecificDay(2024, 12, 2).observe(this, new Observer<Date>() {
            @Override
            public void onChanged(Date date) {
                if (date != null) {
                    Log.d(TAG, "Date: " + date.getYear() + "-" + date.getMonth() + "-" + date.getDay());
                }
            }
        });

        // 4. 日付を更新
        newDate.setDay(3);
        dateViewModel.update(newDate);

        // 5. 日付を削除
        Log.d("DateActivity", "Deleting Date: " + newDate.toString());
        dateViewModel.delete(newDate);
    }
}
