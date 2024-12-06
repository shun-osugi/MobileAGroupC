package io.github.shun.osugi.busible.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import io.github.shun.osugi.busible.databinding.ActivityAddScheduleBinding;
import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;

public class AddScheduleActivity extends AppCompatActivity {

    private ActivityAddScheduleBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private DateViewModel dateViewModel;


    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // 1-based
    private int selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private String color = "#FFFFFF";  // 色の初期値(とりあえず白)

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModel の取得
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // Spinnerの設定
        String[] strongOptions = {"1", "2", "3", "4", "5"};
        binding.spinnerNumber.setMinValue(0);
        binding.spinnerNumber.setMaxValue(strongOptions.length - 1);
        binding.spinnerNumber.setDisplayedValues(strongOptions);
        binding.spinnerNumber.setWrapSelectorWheel(true);

        String[] repeatOptions = {"なし", "毎週", "隔週", "毎月"};
        binding.answer.setMinValue(0);
        binding.answer.setMaxValue(repeatOptions.length - 1);
        binding.answer.setDisplayedValues(repeatOptions);
        binding.answer.setWrapSelectorWheel(true);

        // 日付選択ダイアログ
        binding.inputDate.setOnClickListener(view -> showDatePickerDialog());
        binding.TimeFirst.setOnClickListener(view -> showTimePickerDialog(binding.TimeFirst));
        binding.TimeFinal.setOnClickListener(view -> showTimePickerDialog(binding.TimeFinal));

        binding.back.setOnClickListener(view -> finish()); // アクティビティ終了

        binding.TimeFirst.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showTimePickerDialog(binding.TimeFirst);
                return true;
            }
            return false;
        });

        binding.TimeFinal.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showTimePickerDialog(binding.TimeFinal);
                return true;
            }
            return false;
        });

        binding.inputDate.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showDatePickerDialog();
                return true;
            }
            return false;
        });

        // 保存ボタンのクリックイベント
        binding.save.setOnClickListener(view -> {
            String title = binding.inputText.getText().toString();
            String startTime = binding.TimeFirst.getText().toString();
            String endTime = binding.TimeFinal.getText().toString();
            String memo = binding.memo.getText().toString();
            String intensity = strongOptions[binding.spinnerNumber.getValue()];
            String repeatOption = repeatOptions[binding.answer.getValue()];
            String date = selectedYear + "/" + selectedMonth + "/" + selectedDay;

            // 非同期でデータを保存
            new Thread(() -> {
                // Dateエンティティの保存
                Date date1 = new Date();
                date1.setYear(selectedYear);
                date1.setMonth(selectedMonth);
                date1.setDay(selectedDay);
                Log.d("AddScheduleActivity", "Date 作成: " + date1.toString());

                // Date保存
                long dateId = dateViewModel.insert(date1);
                Log.d("AddScheduleActivity", "Date 保存成功, ID: " + dateId);

                // Scheduleエンティティの作成
                Schedule schedule = new Schedule();
                schedule.setTitle(title);
                schedule.setMemo(memo);
                schedule.setStartTime(startTime);
                schedule.setEndTime(endTime);
                schedule.setColor(color);
                schedule.setStrong(intensity);
                schedule.setRepeat(repeatOption);
                schedule.setDateId((int) dateId); // DateのIDを外部キーとして設定

                Log.d("AddScheduleActivity", "Schedule 作成: " + schedule.toString());

                // Schedule保存
                scheduleViewModel.insert(schedule);
                Log.d("AddScheduleActivity", "Schedule 保存成功");

                // UIスレッドでの処理
                runOnUiThread(() -> {
                    // ダイアログとトースト表示の前にログを追加
                    Log.d("AddScheduleActivity", "UIスレッドにてダイアログ表示");
                    Toast.makeText(this, "スケジュールが保存されました！", Toast.LENGTH_SHORT).show();

                    // ダイアログを表示
                    String message = "タイトル: " + title + "\n日付: " + date + "\n開始時間: " + startTime + "\n終了時間: " + endTime +
                            "\n強度: " + intensity + "\nメモ: " + memo + "\n繰り返し: " + repeatOption;
                    showConfirmationDialog(message);
                    finish();
                });

            }).start();

        });
        }
// 確認ダイアログを表示
        private void showConfirmationDialog (String message) {
            if (!isFinishing() && !isDestroyed()) { // アクティビティが終了していない場合のみ表示
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("確認")
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setCancelable(false);

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getWindow().setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }
        }


        // 日付ピッカー
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            this.selectedYear = selectedYear;
            this.selectedMonth = selectedMonth + 1; // 月は0-basedなので1を足す
            this.selectedDay = selectedDay;

            // データ更新
            String dateText = selectedYear + "/" + this.selectedMonth + "/" + this.selectedDay;
            binding.inputDate.setText(dateText);
            binding.inputDate2.setText(dateText); // 2つ目の日付フィールドも更新
        }, year, month, day);

        datePickerDialog.show();
    }

    // 時間ピッカー
    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%2d:%02d", selectedHour, selectedMinute);
                    editText.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }
}
