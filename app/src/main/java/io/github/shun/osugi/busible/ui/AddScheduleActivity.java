package io.github.shun.osugi.busible.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import io.github.shun.osugi.busible.R;
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

    private String selectedColor = "#00FF00";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初期値設定
        initializeFields();


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

        binding.colorRed.setOnClickListener(v -> setColor("#FF0000"));
        binding.colorGreen.setOnClickListener(v -> setColor("#00FF00"));
        binding.colorBlue.setOnClickListener(v -> setColor("#0000FF"));

        // 初期状態で保存ボタンを無効化し、色を薄くする
        binding.save.setEnabled(false);
        binding.save.setTextColor(Color.parseColor("#B0B0B0")); // 無効化時の色

        // 入力されたタイトルに応じて保存ボタンを有効化
        binding.inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                String title = binding.inputText.getText().toString();
                // タイトルが空でない場合に保存ボタンを有効化
                if (!title.isEmpty()) {
                    // タイトルが入力されている場合、ボタンの色を元に戻す
                    binding.save.setTextColor(Color.parseColor("#034AFF")); // 元の色に変更
                    binding.save.setEnabled(true); // ボタンを有効にする
                } else {
                    // タイトルが空の場合、ボタンを無効にして色を薄くする
                    binding.save.setTextColor(Color.parseColor("#A0A0A0")); // 薄い色に変更
                    binding.save.setEnabled(false); // ボタンを無効にする
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // 保存ボタンのクリックイベント
        binding.save.setOnClickListener(view -> {
            String title = binding.inputText.getText().toString();
            String startTime = binding.TimeFirst.getText().toString();
            String endTime = binding.TimeFinal.getText().toString();
            String memo = binding.memo.getText().toString();
            String strong = strongOptions[binding.spinnerNumber.getValue()];
            String repeatOption = repeatOptions[binding.answer.getValue()];
            LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(selectedYear, selectedMonth, selectedDay);
            dateLiveData.observe(this, date -> {
                int dateId = getOrMakeDateId(dateViewModel, date);
                saveSchedule(scheduleViewModel, dateId, title, memo, strong, startTime, endTime, selectedColor, repeatOption);
                finish();  // 画面を閉じて前の画面に戻る
            });
        });
    }

    // データベースに保存
    private void saveSchedule(ScheduleViewModel scheduleViewModel,int dateId, String title, String memo, String strong,
                              String startTime, String endTime, String selectedColor, String repeatOption) {
        Schedule schedule = new Schedule();
        schedule.setDateId(dateId);
        schedule.setTitle(title);
        schedule.setMemo(memo);
        schedule.setStrong(Integer.parseInt(strong));
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setColor(selectedColor);
        schedule.setRepeat(repeatOption);

        // スケジュールを非同期で保存
        scheduleViewModel.insert(schedule);
        Log.d(TAG, "Schedule By ID: " + schedule.getTitle());
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
            String dateText = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
            binding.inputDate.setText(dateText);
        }, year, month, day);

        datePickerDialog.show();
    }

    // 時間ピッカー
    private void showTimePickerDialog(final EditText editText) {
        // 現在のテキストフィールドに表示されている時刻を取得
        String currentText = editText.getText().toString();
        int hour = 9; // デフォルト値
        int minute = 0;

        // 時刻フォーマットが正しい場合、初期値を解析
        if (currentText.matches("\\d{2}:\\d{2}")) {
            String[] parts = currentText.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%2d:%02d", selectedHour, selectedMinute);
                    editText.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    // フィールドを初期化するメソッド
    private void initializeFields() {
        Calendar calendar = Calendar.getInstance();

        // 日付フィールドの初期化
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String dateText = year + "/" + (month + 1) + "/" + day;
        binding.inputDate.setText(dateText);

        // 開始時刻フィールドの初期化
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        String startTimeText = String.format("%2d:%02d", 9, 0);
        binding.TimeFirst.setText(startTimeText);

        // 終了時刻フィールドの初期化
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        String endTimeText = String.format("%2d:%02d", 10, 0);
        binding.TimeFinal.setText(endTimeText);
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

    private void setColor(String color) {
        selectedColor = color;
        binding.colorRed.setAlpha(color.equals("#FF0000") ? 1.0f : 0.5f);
        binding.colorGreen.setAlpha(color.equals("#00FF00") ? 1.0f : 0.5f);
        binding.colorBlue.setAlpha(color.equals("#0000FF") ? 1.0f : 0.5f);
    }

}
