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

        // 2. データ挿入後に全ての日付を取得して表示
        dateViewModel.getAllDates().observe(this, dates -> {
            if (dates != null && !dates.isEmpty()) {
                Date dateToProcess = dates.get(0);
                // 更新処理
                dateToProcess.setDay(3);
                dateViewModel.update(dateToProcess); // 更新
                Log.d(TAG, "Updated Date: " + dateToProcess);

                // 削除処理
                dateViewModel.delete(dateToProcess);
                Log.d(TAG, "Deleted Date: " + dateToProcess);
            }

            // 更新後に再度全データを取得
            dateViewModel.getAllDates().observe(this, new Observer<List<Date>>() {
                boolean hasLogged = false; // 一度だけログを表示するフラグ

                @Override
                public void onChanged(List<Date> allDates) {
                    if (!hasLogged) {
                        // 最初のデータ取得時だけログを表示
                        Log.d(TAG, "All Dates after deletion: " + allDates.toString());
                        hasLogged = true;  // ログを一度だけ表示したことを記録
                    }
                }
            });

            // 3. 特定の日付を取得して表示
            // ここで再度特定の日付を取得して表示
            dateViewModel.getDateBySpecificDay(2024, 12, 2).observe(this, new Observer<Date>() {
                @Override
                public void onChanged(Date date) {
                    if (date != null) {
                        Log.d(TAG, "Date Found: " + date);
                    }
                }
            });
        });
    }
}