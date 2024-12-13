package io.github.shun.osugi.busible.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;


import org.json.JSONObject;

import io.github.shun.osugi.busible.entity.Date;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.model.BusyData;
import io.github.shun.osugi.busible.model.HolidayApiFetcher;
import io.github.shun.osugi.busible.model.PrefDataStore;
import io.github.shun.osugi.busible.R;
import io.github.shun.osugi.busible.databinding.ActivityCalendarBinding;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;


public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private ActivityCalendarBinding binding;
    private PrefDataStore prefDataStore;

    private DateViewModel dateViewModel;
    private ScheduleViewModel scheduleViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefDataStore = PrefDataStore.getInstance(this);

        // ViewModelの初期化
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);

        // 現在の年と月を取得して headerText に設定
        Calendar calendar = Calendar.getInstance();
        updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        setHolidays(calendar.get(Calendar.YEAR));

        // 前月ボタンのクリックリスナーを設定
        binding.lastMonthButton.setOnClickListener(view -> {
            calendar.add(Calendar.MONTH, -1);
            updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
            refreshCalendarData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        });

        // 次月ボタンのクリックリスナーを設定
        binding.nextMonthButton.setOnClickListener(view -> {
            calendar.add(Calendar.MONTH, 1);
            updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
            refreshCalendarData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        });

        //ホームボタンのクリックリスナー
        binding.homeButton.setOnClickListener(view -> {
            recreate();
        });

        //設定ボタンのクリックリスナー
        binding.settingButton.setOnClickListener(view -> {
            Intent intent = new Intent(CalendarActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        // 他の初期設定;
        binding.addButton.setOnClickListener(view -> {
            var intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    // 祝日データを格納するセット
    private Set<String> holidaySet = new HashSet<>();
    // 祝日を読み込んだ年を格納するセット
    private Set<Integer> fetchedYears = new HashSet<>();

    // HolidayApiFetcher で祝日データを取得してセットに格納
    private void setHolidays(int year) {

        //----------祝日に関するプログラム(ここから)-----------------

        //祝日のデータ取得（json形式で帰ってくる）
        HolidayApiFetcher apiFetcher = new HolidayApiFetcher();
        apiFetcher.fetchHolidayData(year, new HolidayApiFetcher.HolidayCallback() {
            @Override
            public void onHolidayDataReceived(JSONObject holidayData) {
                try {
                    Iterator<String> keys = holidayData.keys();
                    while (keys.hasNext()) {
                        String key = keys.next(); // 祝日の日付 (yyyy-MM-dd)
                        holidaySet.add(key);
                    }
                    fetchedYears.add(year);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // ログに出力
                Log.d("HolidayApiFetcher", "Received holiday data: " + holidayData.toString());

            }

            @Override
            public void onError(Exception e) {
                // エラー処理
                e.printStackTrace();
            }
        });

        //----------祝日に関するプログラム(ここまで)-----------------
    }

    // ヘッダーテキストを更新するメソッド
    private void updateHeaderText(int year, int month) {
        String headerText = year + "年 " + (month+1) + "月";
        binding.headerText.setText(headerText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();  // 現在のカレンダーを取得
        TableLayout tableLayout = findViewById(R.id.calender);

        // 現在の年と月を取得して headerText に設定
        updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        // 重複してカレンダーが生成されないようにビューをクリア
        tableLayout.removeAllViews();

        // 画面全体の高さを取得し、セルの高さを計算
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        int marginHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 152, getResources().getDisplayMetrics());
        int cellHeight = (screenHeight - marginHeight) / 6;

        // カレンダーテーブルに各セルを追加
        int idCounter = 0;
        for (int i = 0; i < 6; i++) {
            TableRow tableRow = new TableRow(this);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            tableRow.setLayoutParams(rowParams);
            for (int j = 0; j < 7; j++) {
                LinearLayout linearLayout = new LinearLayout(this);

                linearLayout.setClipChildren(true);
                linearLayout.setClipToPadding(true);

                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        0,
                        cellHeight
                );
                linearLayout.setLayoutParams(params);
                linearLayout.setTextAlignment(Button.TEXT_ALIGNMENT_CENTER);
                linearLayout.setBackgroundResource(R.drawable.borderx);
                linearLayout.setId(idCounter);
                tableRow.addView(linearLayout);
                idCounter++;
            }
            tableLayout.addView(tableRow);
        }
        // データの再描画
        refreshCalendarData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    private void refreshCalendarData(int year, int month) {

        // 読み込み終了までロック
        binding.progressBar.setVisibility(View.VISIBLE);  // ローディング表示開始
        binding.lastMonthButton.setEnabled(false);        // ボタン無効化
        binding.nextMonthButton.setEnabled(false);

        // カレンダーの初期化
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDay = 0;
        BusyData[] busys = new BusyData[43];
        for (int h = 0; h < 42; h++) {
            busys[h] = new BusyData();
        }

        // 未取得の年なら祝日を新たに取得
        if (!fetchedYears.contains(year)) {setHolidays(year);}

        // カレンダーの最初の曜日を決定
        firstDay = switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY -> 0;
            case Calendar.MONDAY -> 1;
            case Calendar.TUESDAY -> 2;
            case Calendar.WEDNESDAY -> 3;
            case Calendar.THURSDAY -> 4;
            case Calendar.FRIDAY -> 5;
            case Calendar.SATURDAY -> 6;
            default -> firstDay;
        };

        // 各セルに日付の数字、日付の円、予定のボタンを追加
        for (int i = 0; i < 42; i++) {
            LinearLayout linearLayout = findViewById(i);
            linearLayout.removeAllViews();  // 全てのビューをクリア
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setBackgroundColor(Color.rgb(188, 188, 188));   // 背景色をリセット

            FrameLayout dateFrame = new FrameLayout(this);

            // 日付を計算
            int date = i + 1 - firstDay;
            if (date <= 0) {
                calendar.add(Calendar.MONTH, -1);
                date += calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addCircle(dateFrame,year,month,date, true);
                addDate(dateFrame,linearLayout,year,month,date,true);
                calendar.add(Calendar.MONTH, 1);
                addSchedule(linearLayout,year,month-1,date,busys,i);
                busys[i].setGray(true);
            } else if (date > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                date -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addCircle(dateFrame,year,month,date, true);
                addDate(dateFrame,linearLayout,year,month,date,true);
                addSchedule(linearLayout,year,month+1,date,busys,i);
                busys[i].setGray(true);
            } else {
                addCircle(dateFrame,year,month,date, false);
                addDate(dateFrame,linearLayout,year,month,date,false);
                setDefaultBusy(year,month, date,busys,i);
                addSchedule(linearLayout,year,month,date,busys,i);
                busys[i].setGray(false);
            }
        }
    }

    // 日付の円の表示
    private void addCircle(FrameLayout frameLayout, int year, int month, int date, boolean grey) {
        ImageView imageView = new ImageView(this);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        imageParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imageParams);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle));
        Calendar today = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);

        // 今日を判定
        boolean isToday = today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == date
                && !grey;

        // 今日なら色を変更
        if(isToday) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_blue)); // 土曜日は青色
            } else if (dayOfWeek == Calendar.SUNDAY) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_red)); // 日曜日は赤色
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_black)); // 平日は黒色
            }
        }
        frameLayout.addView(imageView);
    }

    // 日付の表示
    private void addDate(FrameLayout frameLayout, LinearLayout linearLayout,int year, int month, int date, boolean grey) {
        TextView textView = new TextView(this);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textParams);
        textView.setText(date + "");
        frameLayout.addView(textView);

        Calendar today = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);

        // 今日を判定
        boolean isToday = today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == date
                && !grey;

        String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, date);

        if(grey){
            textView.setTextColor(Color.GRAY); // 当月以外の日付はグレー
        } else if(isToday) {
            // 今日を強調表示
            textView.setTextColor(Color.WHITE); // 今日は白色
        } else if (holidaySet.contains(formattedDate)) {
            textView.setTextColor(Color.RED); // 祝日は赤色
        } else {
            // 曜日による色分け
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.SATURDAY) {
                textView.setTextColor(Color.BLUE); // 土曜日は青色
            } else if (dayOfWeek == Calendar.SUNDAY) {
                textView.setTextColor(Color.RED); // 日曜日は赤色
            } else {
                textView.setTextColor(Color.BLACK); // 平日は黒色
            }
        }
        linearLayout.addView(frameLayout);
    }

    // 予定の表示(id判定)
    private void addSchedule(LinearLayout linearLayout, int year, int month, int day, BusyData busys[], int cell) {

        // 日付ごとに1つのボタンを生成
        Button dateButton = new Button(this);
        dateButton.setTextSize(9);
        dateButton.setEllipsize(TextUtils.TruncateAt.END);
        dateButton.setMaxLines(1);
        dateButton.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout.LayoutParams dateButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        dateButton.setLayoutParams(dateButtonParams);

        LinearLayout scheduleLayout = new LinearLayout(this);
        scheduleLayout.setLayoutParams( new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        scheduleLayout.setOrientation(LinearLayout.VERTICAL);

        // 日付からスケジュールを取得
        LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(year, month, day);
        dateLiveData.observe(this, date -> {
            if (date != null) {
                int dateId = date.getId();
                addScheduleById(dateId, linearLayout, dateButton, scheduleLayout, year, month, day, busys, cell);
            }
        });

        //繰り返しの予定
        String[] repeatOptions = {"毎週", "隔週", "毎月"};
        for (String repeatOption : repeatOptions) {
            LiveData<List<Schedule>> repeatScheduleLiveData = scheduleViewModel.getSchedulesByRepeat(repeatOption);
            repeatScheduleLiveData.observe(this, repeatSchedules -> {
                if (repeatSchedules != null) {
                    for (Schedule repeatSchedule : repeatSchedules) {
                        int repeatDataId = repeatSchedule.getDateId();
                        LiveData<Date> repeatDateLiveData = dateViewModel.getDateById(repeatDataId);
                        repeatDateLiveData.observe(this, repeatDates -> {
                            int dataYear = repeatDates.getYear();
                            int dataMonth = repeatDates.getMonth();
                            int dataDay = repeatDates.getDay();

                            Calendar calendar0 = Calendar.getInstance();
                            calendar0.set(year, month, day);
                            int dayOfWeek0 = calendar0.get(Calendar.DAY_OF_WEEK);
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.set(dataYear, dataMonth, dataDay);
                            int dayOfWeek1 = calendar1.get(Calendar.DAY_OF_WEEK);

                            switch (repeatOption){
                                case "毎週":
                                    if((year > dataYear || (year == dataYear && month > dataMonth ) || (year == dataYear && month == dataMonth && day > dataDay )) && dayOfWeek0 == dayOfWeek1){
                                        addScheduleById(repeatDataId, linearLayout, dateButton, scheduleLayout, year, month, day, busys, cell);
                                    }
                                    break;
                                case "隔週":
                                    break;
                                case "毎月":
                                    if((year > dataYear || (year == dataYear && month > dataMonth )) && day == dataDay){
                                        addScheduleById(repeatDataId, linearLayout, dateButton, scheduleLayout, year, month, day, busys, cell);
                                    }
                                    break;
                            }
                        });
                    }
                }
            });
        }


        // 読み込み終了後、ロック解除
        binding.progressBar.setVisibility(View.GONE);  // ローディング表示終了
        binding.lastMonthButton.setEnabled(true);      // ボタン再有効化
        binding.nextMonthButton.setEnabled(true);
        viewBusy(busys);
    }

    // 予定の表示(idで実行)
    private void addScheduleById(int id, LinearLayout linearLayout, Button dateButton, LinearLayout scheduleLayout, int year, int month, int day, BusyData busys[], int cell) {
        LiveData<List<Schedule>> scheduleLiveData = scheduleViewModel.getSchedulesByDateId(id);
        scheduleLiveData.observe(this, schedules -> {
                    if (schedules != null) {

                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                        LayoutInflater inflater = this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_detail_layout, null);
                        bottomSheetDialog.setContentView(dialogView);
                        LinearLayout scheduleContainer = dialogView.findViewById(R.id.schedule_container);

                        TextView dialogDate = dialogView.findViewById(R.id.date);
                        dialogDate.setText(year + "/" + (month+1) + "/" + day);

                        ImageButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);
                        buttonCancel.setOnClickListener(cancel -> {
                            // ダイアログを閉じる
                            bottomSheetDialog.dismiss();
                        });

                        scheduleLayout.removeAllViews();
                        scheduleContainer.removeAllViews();

                        for (Schedule schedule : schedules) {

                            // 予定の各フィールドを取得
                            String title = schedule.getTitle();
                            String startTime = schedule.getStartTime();
                            String endTime = schedule.getEndTime();
                            int strong = schedule.getStrong();
                            String eventColor = schedule.getColor();


                            Log.d(TAG, "Schedule By ID: " + title + schedule.getId());

                            //忙しさの表示
                            busys[cell].setBusy(strong);
                            viewBusy(busys);

                            // カレンダー表示用テキストビューを生成
                            TextView indexTextView = new TextView(this);
                            indexTextView.setText(title);
                            indexTextView.setTextColor(Color.WHITE);
                            indexTextView.setTextSize(16);
                            indexTextView.setSingleLine(true);
                            indexTextView.setBackgroundColor(Color.parseColor(eventColor));
                            indexTextView.setGravity(Gravity.CENTER_VERTICAL);
                            LinearLayout.LayoutParams indexLayoutParams = (new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            indexLayoutParams.setMargins(5, 0, 5, 5);
                            indexTextView.setLayoutParams(indexLayoutParams);
                            scheduleLayout.addView(indexTextView);

                            // ダイアログ用のレイアウトを生成
                            LinearLayout detailDialog = new LinearLayout(this);
                            detailDialog.setOrientation(LinearLayout.HORIZONTAL);
                            detailDialog.setBackgroundColor(Color.WHITE);
                            LinearLayout.LayoutParams detailDialogParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    150
                            );
                            detailDialogParams.topMargin = 10;
                            detailDialog.setLayoutParams(detailDialogParams);
                            detailDialog.setGravity(Gravity.CENTER_VERTICAL);

                            // TextView（時間）
                            TextView timeTextView = new TextView(this);
                            timeTextView.setText(startTime + " ~ " + endTime);
                            timeTextView.setPadding(20, 0, 0, 0);
                            timeTextView.setGravity(Gravity.CENTER_VERTICAL);
                            timeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, 1
                            ));
                            detailDialog.addView(timeTextView);

                            // TextView (タイトル)
                            TextView titleTextView = new TextView(this);
                            titleTextView.setText(schedule.getTitle());
                            titleTextView.setTextSize(20);
                            LinearLayout.LayoutParams titleTextParams = new LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, 3
                            );
                            titleTextParams.leftMargin = 30;
                            titleTextView.setLayoutParams(titleTextParams);
                            titleTextView.setGravity(Gravity.CENTER_VERTICAL);
                            detailDialog.addView(titleTextView);

                            // ボタン用LinearLayout
                            LinearLayout buttonContainer = new LinearLayout(this);
                            buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
                            LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            );
                            buttonContainer.setGravity(Gravity.END);
                            buttonContainer.setLayoutParams(buttonContainerParams);

                            // ImageButton (削除ボタン)
                            ImageButton deleteButton = new ImageButton(this);
                            deleteButton.setBackgroundColor(Color.TRANSPARENT);
                            deleteButton.setImageResource(R.drawable.ic_delete);
                            deleteButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            );
                            buttonParams.rightMargin = 50;
                            deleteButton.setLayoutParams(buttonParams);
                            buttonContainer.addView(deleteButton);

                            deleteButton.setOnClickListener(edit -> {
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                                builder2.setTitle("予定を削除しますか？")
                                        .setPositiveButton("削除", (dialog2, which) -> {
                                            // 削除し、ダイアログを閉じる
                                            scheduleViewModel.delete(schedule);
                                            bottomSheetDialog.dismiss();
                                            refreshCalendarData(year, month+1);
                                        })
                                        .setNegativeButton("キャンセル", (dialog2, which) -> {
                                            // ダイアログを閉じる
                                            bottomSheetDialog.dismiss();
                                        })
                                        .show();
                            });

                            // ImageButton (編集ボタン)
                            ImageButton editButton = new ImageButton(this);
                            editButton.setBackgroundColor(Color.TRANSPARENT);
                            editButton.setImageResource(R.drawable.ic_arrow_forward);
                            editButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            editButton.setLayoutParams(buttonParams);
                            buttonContainer.addView(editButton);

                            editButton.setOnClickListener(edit -> {
                                // Intent を作成して EditSchedule へ遷移
                                Intent intent = new Intent(CalendarActivity.this, EditScheduleActivity.class);
                                intent.putExtra("scheduleId", schedule.getId());
                                startActivity(intent);
                            });

                            detailDialog.addView(buttonContainer);

                            scheduleContainer.addView(detailDialog);
                        }

                        bottomSheetDialog.setCancelable(false);

                        dateButton.setOnClickListener(showDialog -> {
                            bottomSheetDialog.show();
                        });
                        dateButton.bringToFront();

                        if (scheduleLayout.getParent() != null) {
                            ((ViewGroup) scheduleLayout.getParent()).removeView(scheduleLayout);
                        }

                        if (dateButton.getParent() != null) {
                            ((ViewGroup) dateButton.getParent()).removeView(dateButton);
                        }

                        FrameLayout frameLayout = new FrameLayout(this);
                        frameLayout.addView(scheduleLayout);
                        frameLayout.addView(dateButton);
                        linearLayout.addView(frameLayout);

                    }else{
                        LiveData<Date> livedate = dateViewModel.getDateById(id);
                        livedate.observe(this, date -> {
                            dateViewModel.delete(date);
                        });
                    }
                });
    }

    //デフォルトの忙しさ反映(当月の曜日走査)
    private void setDefaultBusy(int year, int month, int day, BusyData busys[], int cell) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch(dayOfWeek){
            case Calendar.SUNDAY :
                busys[cell].setDefaultBusy(prefDataStore.getInteger("sunBusy").orElse(0));
                break;
            case Calendar.MONDAY:
                busys[cell].setDefaultBusy(prefDataStore.getInteger("monBusy").orElse(0));
                break;
            case Calendar.TUESDAY:
                busys[cell].setDefaultBusy(prefDataStore.getInteger("tueBusy").orElse(0));
                break;
            case Calendar.WEDNESDAY:
                busys[cell].setDefaultBusy(prefDataStore.getInteger("wedBusy").orElse(0));
                break;
            case Calendar.THURSDAY:
                busys[cell].setDefaultBusy(prefDataStore.getInteger("thuBusy").orElse(0));
                break;
            case Calendar.FRIDAY:
                busys[cell].setDefaultBusy(prefDataStore.getInteger("friBusy").orElse(0));
                break;
            case Calendar.SATURDAY:
                busys[cell].setDefaultBusy(prefDataStore.getInteger("satBusy").orElse(0));
                break;
        }
    }

    // 忙しさの表示
    private void viewBusy(BusyData[] busydata) {
        int busy;
        for(int i=0; i<42; i++){
            LinearLayout ll = findViewById(i);
            if(i == 0){
                busy = busydata[i].getBusy() + busydata[i+1].getBusy()/3;
            } else if (i == 41) {
                busy = busydata[i].getBusy() + busydata[i-1].getBusy()/3;
            } else {
                busy = busydata[i].getBusy() + (busydata[i-1].getBusy()+busydata[i+1].getBusy())/2;
            }
            busy += busydata[i].getDefaultBusy();
            if(busy > 7){busy = 7;}
            switch (busy) {
                case 7 -> ll.setBackgroundResource(R.drawable.border7);
                case 6 -> ll.setBackgroundResource(R.drawable.border6);
                case 5 -> ll.setBackgroundResource(R.drawable.border5);
                case 4 -> ll.setBackgroundResource(R.drawable.border4);
                case 3 -> ll.setBackgroundResource(R.drawable.border3);
                case 2 -> ll.setBackgroundResource(R.drawable.border2);
                case 1 -> ll.setBackgroundResource(R.drawable.border1);
                case 0 -> ll.setBackgroundResource(R.drawable.border0);
                default -> ll.setBackgroundResource(R.drawable.borderx);
            }
            if(busydata[i].getGray() == true){
                ll.setBackgroundResource(R.drawable.borderx);
            }
        }
    }

    // 忙しさの表示(ダイアログ用)
    private String stringBusy(int busy) {
        switch (busy) {
            case 5 : return "⑤";
            case 4 : return "④";
            case 3 : return "③";
            case 2 : return "②";
            case 1 : return "①";
        }
        return "";
    }
}