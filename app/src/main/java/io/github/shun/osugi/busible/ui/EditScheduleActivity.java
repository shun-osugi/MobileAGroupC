package io.github.shun.osugi.busible.ui;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import io.github.shun.osugi.busible.databinding.ActivityAddScheduleBinding;
import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;

public class EditScheduleActivity extends AppCompatActivity {

    private ActivityAddScheduleBinding binding;

    private int selectedYear, selectedMonth, selectedDay;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ScheduleViewModel scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        DateViewModel dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);

        // Button名を変更
        binding.save.setText("変更を適用");
        binding.back.setText("キャンセル");

        // Spinner の設定
        String[] strongOptions = {"1","2", "3", "4", "5"};
        binding.spinnerNumber.setMinValue(0);
        binding.spinnerNumber.setMaxValue(strongOptions.length - 1);
        binding.spinnerNumber.setDisplayedValues(strongOptions);
        binding.spinnerNumber.setWrapSelectorWheel(true);

        String[] repeatOptions = {"なし", "毎週", "隔週", "毎月"};
        binding.answer.setMinValue(0);
        binding.answer.setMaxValue(repeatOptions.length - 1);
        binding.answer.setDisplayedValues(repeatOptions);
        binding.answer.setWrapSelectorWheel(true);

        binding.inputDate.setOnClickListener(view -> showDatePickerDialog());
        binding.TimeFirst.setOnClickListener(view -> showTimePickerDialog(binding.TimeFirst));
        binding.TimeFinal.setOnClickListener(view -> showTimePickerDialog(binding.TimeFinal));

        binding.back.setOnClickListener(view -> {
            finish(); // 現在のアクティビティを終了して前の画面に戻る
        });

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

        // CalendarActivityからscheduleIDを取得
        Intent intent = getIntent();
        int scheduleId = intent.getIntExtra("scheduleId", -1);

        scheduleViewModel.getScheduleById(scheduleId).observe(this, schedule -> {
            if(schedule != null) {
                // 予定の各フィールドを取得
                int initialdateId = schedule.getDateId();
                String initialTitle = schedule.getTitle();
                String initialStartTime = schedule.getStartTime();
                String initialEndTime = schedule.getEndTime();
                int initialStrong = schedule.getStrong();
                String initialMemo = schedule.getMemo();
                String initialRepeat = schedule.getRepeat();

                // UIに反映
                dateViewModel.getDateById(initialdateId).observe(this, date -> {
                    if (date != null) {
                        // Dateデータが取得できたら、UIに反映
                        binding.inputDate.setText(date.getYear() + "/" + (date.getMonth() + 1) + "/" + date.getDay());
                        binding.inputDate2.setText(date.getYear() + "/" + (date.getMonth() + 1) + "/" + date.getDay());
                    }
                });
                binding.inputText.setText(initialTitle);
                binding.TimeFirst.setText(initialStartTime);
                binding.TimeFinal.setText(initialEndTime);
                binding.memo.setText(initialMemo);

                binding.spinnerNumber.setValue(
                        java.util.Arrays.asList(strongOptions).indexOf(initialStrong + "")
                );

                binding.answer.setValue(
                        java.util.Arrays.asList(repeatOptions).indexOf(initialRepeat)
                );

                binding.inputDate.setText(selectedYear + "/" + selectedMonth + "/" + selectedDay);

                // 保存ボタンのクリックイベント
                binding.save.setOnClickListener(view -> {
                    var title = binding.inputText.getText().toString();
                    var startTime = binding.TimeFirst.getText().toString();
                    var endTime = binding.TimeFinal.getText().toString();
                    var memo = binding.memo.getText().toString();
                    var strong = strongOptions[binding.spinnerNumber.getValue()];
                    var repeat = repeatOptions[binding.answer.getValue()];

                    // 日付データを追加 (後で変数化)
                    String year = String.valueOf(selectedYear);
                    String month = String.valueOf(selectedMonth);
                    String day = String.valueOf(selectedDay);  // ここに日付の変数を追加

                    // DateIdを取得または作成
                    LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(selectedYear, selectedMonth, selectedDay);
                    dateLiveData.observe(this, date -> {
                        int dateId = getOrMakeDateId(dateViewModel, date);
                        schedule.setDateId(dateId);

                    });

                    // データを保存
                    schedule.setTitle(title);
                    schedule.setStartTime(startTime);
                    schedule.setEndTime(endTime);
                    schedule.setStrong(Integer.parseInt(strong));
                    schedule.setMemo(memo);
                    schedule.setRepeat(repeat);

                    scheduleViewModel.update(schedule);

                    // メッセージの表示
                    var message = "タイトル : " + title + "\n日付 : "+year + "/" + month + "/" + day + "\n開始時間 : " + startTime + "\n終了時間 : " + endTime +
                            "\n強度 : " + strong + "\nメモ : " + memo + "\n繰り返し : " + repeat;
                    showConfirmationDialog(message);
                });
            }
        });
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
            String dateText = selectedYear + "/" + (this.selectedMonth + 1)+ "/" + this.selectedDay;
            binding.inputDate.setText(dateText);
            binding.inputDate2.setText(dateText); // 2つ目の日付フィールドも更新
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%2d:%02d", selectedHour, selectedMinute);
                    editText.setText(time);
                }, hour, minute, true); // 'true' for 24-hour format

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
