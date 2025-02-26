package io.github.shun.osugi.busible.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputBinding;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.github.shun.osugi.busible.R;
import io.github.shun.osugi.busible.dao.RepeatDao;
import io.github.shun.osugi.busible.dao.RepeatExclusionDao;
import io.github.shun.osugi.busible.dao.ScheduleDao;
import io.github.shun.osugi.busible.databinding.ActivityAddScheduleBinding;
import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.entity.Repeat;
import io.github.shun.osugi.busible.entity.RepeatExclusion;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;
import io.github.shun.osugi.busible.viewmodel.RepeatViewModel;
import io.github.shun.osugi.busible.viewmodel.RepeatExclusionViewModel;



public class AddScheduleActivity extends AppCompatActivity {

    private static final String TAG = "AddScheduleActivity";
    private ActivityAddScheduleBinding binding;

    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH); // 1-based
    private int selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private String selectedColor = "#FF0000";

    private RepeatViewModel repeatViewModel;
    private RepeatExclusionViewModel repeatExclusionViewModel;


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
        repeatViewModel = new ViewModelProvider(this).get(RepeatViewModel.class);
        repeatExclusionViewModel = new ViewModelProvider(this).get(RepeatExclusionViewModel.class);


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


        // 色ボタンのクリックリスナー設定
        binding.colorRed.setOnClickListener(v -> setColorAndCheck("#FF0000", binding.checkRed));
        binding.colorGreen.setOnClickListener(v -> setColorAndCheck("#008D00", binding.checkGreen));
        binding.colorBlue.setOnClickListener(v -> setColorAndCheck("#0000FF", binding.checkBlue));
        binding.colorPurple.setOnClickListener(v -> setColorAndCheck("#8F35B5", binding.checkPurple));
        binding.colorOrange.setOnClickListener(v -> setColorAndCheck("#FF6C00", binding.checkOrange));
        binding.colorWhite.setOnClickListener(v -> setColorAndCheck("#FFFFFF", binding.checkWhite));

        // 初期状態で保存ボタンの色を薄くする
        binding.save.setTextColor(Color.parseColor("#B0B0B0")); // 無効化時の色
        // 最初に赤を選択された状態にする
        setColorAndCheck("#FF0000", binding.checkRed);


        // タイトルの入力チェック
        binding.inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkSaveButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 保存ボタンのクリックリスナー
        binding.save.setOnClickListener(v -> {
            String title = binding.inputText.getText().toString();
            String startTime = binding.TimeFirst.getText().toString();
            String endTime = binding.TimeFinal.getText().toString();

            boolean isTitleNotEmpty = !title.isEmpty();
            boolean isValidTime = isValidTimeRange(startTime, endTime);

            if (isTitleNotEmpty && isValidTime) {
                // **条件を満たしている場合のみデータを保存**
                String memo = binding.memo.getText().toString();
                String strong = strongOptions[binding.spinnerNumber.getValue()];
                String repeatOption = repeatOptions[binding.answer.getValue()];

                LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(selectedYear, selectedMonth, selectedDay);
                dateLiveData.observe(this, date -> {
                    int dateId = getOrMakeDateId(dateViewModel, date);
                    saveSchedule(scheduleViewModel, dateId, title, memo, strong, startTime, endTime, selectedColor, repeatOption,new ScheduleViewModel.OnInsertCallback() {
                        @Override
                        public void onInsertCompleted(int scheduleId) {
                            // 日付から週番号と曜日を計算
                            int week = getWeekOfMonth(selectedYear, selectedMonth, selectedDay);
                            int dayOfWeek = getDayOfWeek(selectedYear, selectedMonth, selectedDay);
                            if (repeatOption != "なし"){
                                // スケジュール保存後にリピートデータを保存
                                saveScheduleWithRepeat(dateId, scheduleId, selectedDay, week, dayOfWeek, repeatOption);
                            }else {
                                Log.d(TAG,"なしのため繰り返しには保存しない");
                            }
                        }
                    });

                    dateLiveData.removeObservers(this);
                    // 保存処理の最後にカレンダー更新用のデータを渡す
                    Intent resultIntent = new Intent(AddScheduleActivity.this, CalendarActivity.class);
                    resultIntent.putExtra("selectedYear", selectedYear);
                    resultIntent.putExtra("selectedMonth", selectedMonth);
                    startActivity(resultIntent);

                    finish(); // 画面を閉じる
                });
            } else {
                // **条件を満たしていない場合、データは保存せずエラーメッセージを表示**
                if (!isTitleNotEmpty && !isValidTime) {
                    Toast.makeText(binding.save.getContext(), "タイトルを入力し, 開始時間は\n終了時間より先にしてください", Toast.LENGTH_SHORT).show();
                } else if (!isTitleNotEmpty) {
                    Toast.makeText(binding.save.getContext(), "タイトルを入力してください", Toast.LENGTH_SHORT).show();
                } else if (!isValidTime) {
                    Toast.makeText(binding.save.getContext(), "開始時間は終了時間より\n先にしてください", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // データベースに保存
    private void saveSchedule(ScheduleViewModel scheduleViewModel,int dateId, String title, String memo, String strong,
                              String startTime, String endTime, String selectedColor, String repeatOption, ScheduleViewModel.OnInsertCallback onSuccess) {
            // スケジュール保存
            Schedule schedule = new Schedule();
            schedule.setDateId(dateId);
            schedule.setTitle(title);
            schedule.setMemo(memo);
            schedule.setStrong(Integer.parseInt(strong));
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setColor(selectedColor);
            schedule.setRepeat(repeatOption);

            scheduleViewModel.insert(schedule, scheduleId -> {
                Log.d(TAG, "Schedule saved: " + schedule.getTitle() + " with ID: " + scheduleId);
                onSuccess.onInsertCompleted(scheduleId); // 保存完了後にコールバックを実行
            });
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


    // 週番号を取得
    private int getWeekOfMonth(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();

        // 月初の日を設定
        calendar.set(year, month - 1, 1);  // 月は0ベースなので、-1
        int startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 月初の曜日（1=日曜日, 2=月曜日, ..., 7=土曜日）

        // 入力された日付を設定
        calendar.set(year, month - 1, day);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);  // 月の日付

        // 月初からの経過日数
        int daysSinceStartOfMonth = dayOfMonth - 1;

        // 月の最初の週を基準にして週番号を計算
        int weekNumber = (daysSinceStartOfMonth + startDayOfWeek - 1) / 7 + 1;

        return weekNumber;
    }

    // 曜日を取得 (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
    private int getDayOfWeek(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.get(Calendar.DAY_OF_WEEK) + 40; // 1=Sunday, 2=Monday, ...
    }

    private void saveScheduleWithRepeat(int dateId, int scheduleId, int selectedDay,int week,int Dow, String repeatOption) {

            // リピートデータを保存
            Repeat repeat = new Repeat();
            repeat.setDateId(dateId); // dateId
            repeat.setScheduleId(scheduleId); // ScheduleのIDを設定
            if (repeatOption == "毎週"){
                repeat.setRepeat(Dow);
            }else if (repeatOption == "隔週"){
                repeat.setRepeat(week);
            } else if (repeatOption == "毎月") {
                repeat.setRepeat(selectedDay);
            }else {
                repeat.setRepeat(0);
            }
        repeatViewModel.insert(repeat);

            Log.d(TAG, "Repeat saved:" + repeatOption + " ," + repeat.getRepeat());

//            // リピート除外データを保存
//            RepeatExclusion exclusion = new RepeatExclusion();
//            exclusion.setRepeatId(exclusion.getRepeatId()); // RepeatのIDを設定
//            exclusion.setDate(selectedYear+ "/"+(selectedMonth+1)+ "/"+selectedDay);
//            repeatExclusionViewModel.insert(exclusion);
//
//            Log.d(TAG, "Repeat Exclusion saved: " + exclusion.getDate());
    }


    // 時間ピッカー
    private void showTimePickerDialog(final EditText editText) {
        String currentText = editText.getText().toString();
        int hour = 9, minute = 0;

        if (currentText.matches("\\d{2}:\\d{2}")) {
            String[] parts = currentText.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(time);
                    checkSaveButtonState();
                }, hour, minute, true);

        timePickerDialog.show();
    }

    // 保存ボタンの状態を更新するメソッド
    private void checkSaveButtonState() {

        // もし getText() が取得される前にボタンが押されたら
        if (binding.inputText.getText() == null || binding.TimeFirst.getText() == null || binding.TimeFinal.getText() == null) {
            Toast.makeText(binding.save.getContext(), "タイトルを入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = binding.inputText.getText().toString();
        String startTime = binding.TimeFirst.getText().toString();
        String endTime = binding.TimeFinal.getText().toString();

        boolean isTitleNotEmpty = !title.isEmpty();
        boolean isValidTime = isValidTimeRange(startTime, endTime);

        if (isTitleNotEmpty && isValidTime) {
            // 条件を満たす場合（タイトルがあり、開始時間 < 終了時間）
            binding.save.setTextColor(Color.parseColor("#034AFF")); // 青色
        } else {
            // 条件を満たさない場合（タイトルがない or 開始時間 >= 終了時間）
            binding.save.setTextColor(Color.parseColor("#A0A0A0")); // 灰色
        }
    }

    //タイトルと時間の両方をチェック
    private boolean isValidTimeRange(String startTime, String endTime) {
        if (!startTime.matches("\\d{2}:\\d{2}") || !endTime.matches("\\d{2}:\\d{2}")) {
            return false;
        }

        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");

        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);

        return (endHour > startHour) || (endHour == startHour && endMinute > startMinute);
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
        String startTimeText = String.format("%02d:%02d", 9, 0);
        binding.TimeFirst.setText(startTimeText);

        // 終了時刻フィールドの初期化
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        String endTimeText = String.format("%02d:%02d", 10, 0);
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
            long newId = dateViewModel.insert(newdate);

            return (int)newId;
        }
    }

    private void setColorAndCheck(String selectedColor, ImageView selectedCheck) {
        setColor(selectedColor); // 既存の色設定処理

        // すべてのチェックを非表示にする
        binding.checkRed.setVisibility(View.GONE);
        binding.checkGreen.setVisibility(View.GONE);
        binding.checkBlue.setVisibility(View.GONE);
        binding.checkPurple.setVisibility(View.GONE);
        binding.checkOrange.setVisibility(View.GONE);
        binding.checkWhite.setVisibility(View.GONE);

        // 選択した色のチェックマークを表示
        selectedCheck.setVisibility(View.VISIBLE);
    }

    private void setColor(String color) {
        selectedColor = color;
        binding.colorRed.setAlpha(color.equals("#FF0000") ? 1.0f : 0.5f);
        binding.colorGreen.setAlpha(color.equals("#008D00") ? 1.0f : 0.5f);
        binding.colorBlue.setAlpha(color.equals("#0000FF") ? 1.0f : 0.5f);
        binding.colorPurple.setAlpha(color.equals("#8F35B5") ? 1.0f : 0.5f);
        binding.colorOrange.setAlpha(color.equals("#FF6C00") ? 1.0f : 0.5f);
        binding.colorWhite.setAlpha(color.equals("#FFFFFF") ? 1.0f : 0.5f);
    }


}
