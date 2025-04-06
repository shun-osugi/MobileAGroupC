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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.shun.osugi.busible.R;
import io.github.shun.osugi.busible.databinding.ActivityAddScheduleBinding;
import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.entity.Repeat;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.RepeatViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;

public class EditScheduleActivity extends AppCompatActivity {

    private static final String TAG = "EditScheduleActivity";
    private ActivityAddScheduleBinding binding;

    private int selectedYear = Calendar.getInstance().get(Calendar.YEAR);
    private int selectedMonth = Calendar.getInstance().get(Calendar.MONTH); // 1-based
    private int selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private String selectedColor = "#FF0000";

    private DateViewModel dateViewModel;
    private ScheduleViewModel scheduleViewModel;
    private RepeatViewModel repeatViewModel;

    // 色とボタン・チェックマークの対応をマップにする
    private final Map<String, Pair<View, ImageView>> colorToButtonMap = new HashMap<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModelの初期化
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        repeatViewModel = new ViewModelProvider(this).get(RepeatViewModel.class);

        // CalendarActivityから取得したscheduleIDを基にscheduleを取得
        Intent intent = getIntent();
        int scheduleId = intent.getIntExtra("scheduleId", -1);
        scheduleViewModel.getScheduleById(scheduleId).observe(this, schedule -> {

            if(schedule != null) {

                // 繰り返しがあるか判別
                boolean repeat = schedule.getRepeat();

                // Button名を変更
                binding.incident.setText("イベントを編集");
                binding.save.setText("変更を適用");
                binding.back.setText("キャンセル");

                // Spinnerの設定
                String[] strongOptions = {"1", "2", "3", "4", "5"};
                binding.spinnerNumber.setMinValue(0);
                binding.spinnerNumber.setMaxValue(strongOptions.length - 1);
                binding.spinnerNumber.setDisplayedValues(strongOptions);
                binding.spinnerNumber.setWrapSelectorWheel(true);

                String[] repeatOptions = {"なし", "毎週"/*, "隔週"*/, "毎月"};
                binding.answer.setMinValue(0);
                binding.answer.setMaxValue(repeatOptions.length - 1);
                binding.answer.setDisplayedValues(repeatOptions);
                binding.answer.setWrapSelectorWheel(true);

                // 初期値設定
                initializeFields(dateViewModel, schedule, repeatOptions);

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


                // 色とボタン・チェックマークの対応関係をセット
                colorToButtonMap.put("#FF0000", new Pair<>(binding.colorRed, binding.checkRed));
                colorToButtonMap.put("#008D00", new Pair<>(binding.colorGreen, binding.checkGreen));
                colorToButtonMap.put("#0000FF", new Pair<>(binding.colorBlue, binding.checkBlue));
                colorToButtonMap.put("#8F35B5", new Pair<>(binding.colorPurple, binding.checkPurple));
                colorToButtonMap.put("#FF6C00", new Pair<>(binding.colorOrange, binding.checkOrange));

                // 色ボタンのクリックリスナー設定
                for (Map.Entry<String, Pair<View, ImageView>> entry : colorToButtonMap.entrySet()) {
                    String color = entry.getKey();
                    View button = entry.getValue().first;
                    ImageView check = entry.getValue().second;
                    button.setOnClickListener(v -> setColorAndCheck(color));
                }
                String initialColor = schedule.getColor();
                setColorAndCheck(initialColor);


                // 保存ボタンのクリックイベント
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
                        boolean isRepeat = !repeatOption.equals("なし");  // `!=` ではなく `.equals()` を使う

                        // 日付から週番号と曜日を計算
                        int week = getWeekOfMonth(selectedYear, selectedMonth, selectedDay);
                        int dayOfWeek = getDayOfWeek(selectedYear, selectedMonth, selectedDay);

                        LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(selectedYear, selectedMonth, selectedDay);
                        dateLiveData.observe(this, date -> {
                            int dateId = getOrMakeDateId(dateViewModel, date);
                            updateSchedule(scheduleViewModel, schedule, dateId, title, memo, strong, startTime, endTime, selectedColor, isRepeat);
                            dateLiveData.removeObservers(this);

                            if (isRepeat) {
                                if (repeat) {
                                    repeatViewModel.getRepeatByScheduleId(scheduleId).observe(this, repeatSchedules -> {
                                        if (repeatSchedules != null) {
                                            for (Repeat repeatSchedule : repeatSchedules) {
                                                if (repeatSchedule.getRepeat() == getRepeatType(selectedDay, week, dayOfWeek, repeatOption)) {
                                                    Log.d(TAG, "R->R:delete&update");
                                                    updateScheduleWithRepeat(repeatSchedule, dateId, scheduleId, selectedDay, week, dayOfWeek, repeatOption);
                                                }else{
                                                    Log.d(TAG, "R->R:delete&new");
                                                    repeatViewModel.delete(repeatSchedule);
                                                    saveScheduleWithRepeat(dateId, scheduleId, selectedDay, week, dayOfWeek, repeatOption);
                                                }
                                            }
                                        }else{
                                            Log.d(TAG, "->R:new");
                                            saveScheduleWithRepeat(dateId, scheduleId, selectedDay, week, dayOfWeek, repeatOption);
                                        }
                                    });
                                }else{
                                    Log.d(TAG, "->R:new");
                                    saveScheduleWithRepeat(dateId, scheduleId, selectedDay, week, dayOfWeek, repeatOption);
                                }
                            }else{
                                if(repeat) {
                                    repeatViewModel.getRepeatByScheduleId(scheduleId).observe(this, repeatSchedules -> {
                                        if (repeatSchedules != null) {
                                            for (Repeat repeatSchedule : repeatSchedules) {
                                                Log.d(TAG, "R->:delete");
                                                repeatViewModel.delete(repeatSchedule);
                                            }
                                        }
                                    });
                                }
                            }

                            // 保存処理の最後にカレンダー更新用のデータを渡す
                            Intent resultIntent = new Intent(EditScheduleActivity.this, CalendarActivity.class);
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

            }else{
                finish();
            }
        });
    }

    // データベースを更新
    private void updateSchedule(ScheduleViewModel scheduleViewModel, Schedule schedule,int dateId, String title, String memo, String strong,
                              String startTime, String endTime, String selectedColor, Boolean isRepeat) {
        schedule.setDateId(dateId);
        schedule.setTitle(title);
        schedule.setMemo(memo);
        schedule.setStrong(Integer.parseInt(strong));
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setColor(selectedColor);
        schedule.setRepeat(isRepeat);

        scheduleViewModel.update(schedule);
        Log.d(TAG, "Schedule By ID: " + schedule.getTitle());
    }

    private void updateScheduleWithRepeat(Repeat repeat,int dateId, int scheduleId, int selectedDay,int week,int Dow, String repeatOption) {
        //変更必要
        // リピートデータを保存
        repeat.setDateId(dateId); // dateId
        repeat.setScheduleId(scheduleId); // ScheduleのIDを設定
        if (repeatOption.equals("毎週")) {
            repeat.setRepeat(Dow);
        } else if (repeatOption.equals("隔週")) {
            repeat.setRepeat(week * (-1));
        } else if (repeatOption.equals("毎月")) {
            repeat.setRepeat(selectedDay);
        } else {
            repeat.setRepeat(0);
        }

        repeatViewModel.update(repeat);

        Log.d(TAG, "Repeat update:" + repeatOption + " ," + repeat.getRepeat());
    }

    private void saveScheduleWithRepeat(int dateId, int scheduleId, int selectedDay,int week,int Dow, String repeatOption) {

        // リピートデータを保存
        Repeat repeat = new Repeat();
        repeat.setDateId(dateId); // dateId
        repeat.setScheduleId(scheduleId); // ScheduleのIDを設定
        repeat.setRepeat(getRepeatType(selectedDay, week, Dow, repeatOption)); // Repeatを設定

        repeatViewModel.insert(repeat);

        Log.d(TAG, "Repeat saved:" + repeatOption + " ," + repeat.getRepeat());

    }

    private int getRepeatType(int selectedDay,int week,int Dow, String repeatOption) {
        if (repeatOption.equals("毎週")) {
            return(Dow);
        } else if (repeatOption.equals("隔週")) {
            return(week * (-1));
        } else if (repeatOption.equals("毎月")) {
            return(selectedDay);
        } else {
            return(0);
        }
    }

    // 日付ピッカー
    private void showDatePickerDialog() {
        // 現在選択されている日付を取得（初期値として今日の日付）
        int year = this.selectedYear != 0 ? this.selectedYear : Calendar.getInstance().get(Calendar.YEAR);
        int month = this.selectedMonth != 0 ? this.selectedMonth : Calendar.getInstance().get(Calendar.MONTH);
        int day = this.selectedDay != 0 ? this.selectedDay : Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog の表示
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

    private void checkSaveButtonState() {
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
    private void initializeFields(DateViewModel dateViewModel, Schedule schedule, String[] repeatOptions) {
        // 予定の各フィールドを取得
        int initialdateId = schedule.getDateId();
        String initialTitle = schedule.getTitle();
        String initialStartTime = schedule.getStartTime();
        String initialEndTime = schedule.getEndTime();
        int initialStrong = schedule.getStrong();
        String initialMemo = schedule.getMemo();
        Boolean initialRepeat = schedule.getRepeat(); // ここで繰り返しの設定を取得
        String initialColor = schedule.getColor();

        dateViewModel.getDateById(initialdateId).observe(this, date -> {
            if (date != null) {
                // 日付フィールドの初期化
                int year = date.getYear();
                int month = date.getMonth();
                int day = date.getDay();

                String dateText = year + "/" + (month + 1) + "/" + day;
                binding.inputDate.setText(dateText);

                selectedYear = date.getYear();
                selectedMonth = date.getMonth();
                selectedDay = date.getDay();
            }
        });

        // 各フィールドの初期化
        binding.inputText.setText(initialTitle);
        binding.TimeFirst.setText(initialStartTime);
        binding.TimeFinal.setText(initialEndTime);
        binding.spinnerNumber.setValue(initialStrong - 1);
        binding.memo.setText(initialMemo);

        // 繰り返し設定を取得してスピナーに反映
        if (initialRepeat != null) {
            // `initialRepeat` が null でない場合のみ設定
            if (initialRepeat) {
                // 繰り返しが有効なら、繰り返しの種類をセット
                initializeRepeatSpinner(schedule, repeatOptions);
            } else {
                // 繰り返しなしなら「なし」をセット
                binding.answer.setValue(0);
            }
        } else {
            // `initialRepeat` が null の場合も「なし」を設定
            binding.answer.setValue(0);
        }

        setColorAndCheck(initialColor);
    }

    // 繰り返しスピナーの初期化メソッド
    private void initializeRepeatSpinner(Schedule schedule, String[] repeatOptions) {
        // RepeatViewModelのインスタンス取得
        RepeatViewModel repeatViewModel = new ViewModelProvider(this).get(RepeatViewModel.class);

        // scheduleIdで繰り返しデータを取得
        repeatViewModel.getRepeatByScheduleId(schedule.getId()).observe(this, repeatList -> {
            int repeatValue;

            if (repeatList == null || repeatList.isEmpty()) {
                // Repeatデータが存在しない場合、「なし」を初期値に設定
                repeatValue = 0;
            } else {
                // データが存在する場合、最初の要素の `repeat` を取得
                repeatValue = repeatList.get(0).getRepeat();
            }

            int index = 0; // デフォルト「なし」

            // `repeatValue` に基づきスピナーの値を設定
            if (repeatValue < 0) {
                index = 2; // 負の値なら「隔週」
            } else if (repeatValue >= 1 && repeatValue <= 31) {
                index = 3; // 1~31なら「毎月」
            } else if (repeatValue >= 41 && repeatValue <= 47) {
                index = 1; // 41~47なら「毎週」
            }

            // スピナーの初期値設定
            binding.answer.setValue(index);
        });
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

    private void setColorAndCheck(String selectedColor) {
        setColor(selectedColor); // 色の選択処理

        // すべてのチェックマークを非表示にする
        for (Pair<View, ImageView> pair : colorToButtonMap.values()) {
            pair.second.setVisibility(View.GONE);
        }

        // 選択された色に対応するチェックマークを表示
        if (colorToButtonMap.containsKey(selectedColor)) {
            colorToButtonMap.get(selectedColor).second.setVisibility(View.VISIBLE);
        }
    }

    private void setColor(String color) {
        selectedColor = color;

        // すべてのボタンを半透明にし、選択されたボタンのみ有効化
        for (Map.Entry<String, Pair<View, ImageView>> entry : colorToButtonMap.entrySet()) {
            entry.getValue().first.setAlpha(entry.getKey().equals(color) ? 1.0f : 0.5f);
        }
    }

}
