package io.github.shun.osugi.busible.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import io.github.shun.osugi.busible.databinding.ActivityAddScheduleBinding;
import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;

public class AddScheduleActivity extends AppCompatActivity {

    private static final String TAG = "AddScheduleActivity";
    private ActivityAddScheduleBinding binding;

    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH); // 1-based
    private int selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private String color = "#FFFFFF";  // 色の初期値(とりあえず白)

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ScheduleViewModel scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        DateViewModel dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

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
            String strong = strongOptions[binding.spinnerNumber.getValue()];
            String repeatOption = repeatOptions[binding.answer.getValue()];
            String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;

            LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(selectedYear, selectedMonth, selectedDay);
            dateLiveData.observe(this, date -> {
                int dateId = getOrMakeDateId(dateViewModel, date);
                saveSchedule(scheduleViewModel, dateId, title, memo, strong, startTime, endTime, color, repeatOption);

            });

            // ダイアログを表示
            String message = "タイトル: " + title + "\n日付: " + selectedDate + "\n開始時間: " + startTime + "\n終了時間: " + endTime +
                    "\n強度: " + strong + "\nメモ: " + memo + "\n繰り返し: " + repeatOption;
            showConfirmationDialog(message);
        });
    }

    // データベースに保存
    private void saveSchedule(ScheduleViewModel scheduleViewModel,int dateId, String title, String memo, String strong,
                              String startTime, String endTime, String color, String repeatOption) {
        Schedule schedule = new Schedule();
        schedule.setDateId(dateId);
        schedule.setTitle(title);
        schedule.setMemo(memo);
        schedule.setStrong(Integer.parseInt(strong));
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setColor(color);
        schedule.setRepeat(repeatOption);

        // スケジュールを非同期で保存
        scheduleViewModel.insert(schedule);
        Log.d(TAG, "Schedule By ID: " + schedule.getTitle());
    }


    // 確認ダイアログを表示
    private void showConfirmationDialog(String message) {
        if (!isFinishing() && !isDestroyed()) {  // アクティビティが終了していない場合のみ表示
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("確認")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .setCancelable(true);

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
            this.selectedMonth = selectedMonth;
            this.selectedDay = selectedDay;

            // データ更新
            String dateText = selectedYear + "/" + (this.selectedMonth + 1) + "/" + this.selectedDay;
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

    // dateId取得
    private int getOrMakeDateId(DateViewModel dateViewModel, Date date) {
        if (date != null) {
            return date.getId();
        }else{
            Date newdate = new Date();
            newdate.setYear(selectedYear);
            newdate.setMonth(selectedMonth);
            newdate.setDay(selectedDay);
            dateViewModel.insert(newdate);

            return newdate.getId();
        }
    }
}
