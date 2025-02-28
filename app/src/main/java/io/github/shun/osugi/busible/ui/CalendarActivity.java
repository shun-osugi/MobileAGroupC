package io.github.shun.osugi.busible.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;


import org.json.JSONObject;

import io.github.shun.osugi.busible.entity.Repeat;
import io.github.shun.osugi.busible.entity.Schedule;
import io.github.shun.osugi.busible.model.BusyData;
import io.github.shun.osugi.busible.model.HolidayApiFetcher;
import io.github.shun.osugi.busible.model.PrefDataStore;
import io.github.shun.osugi.busible.R;
import io.github.shun.osugi.busible.databinding.ActivityCalendarBinding;
import io.github.shun.osugi.busible.viewmodel.DateViewModel;
import io.github.shun.osugi.busible.viewmodel.RepeatViewModel;
import io.github.shun.osugi.busible.viewmodel.ScheduleViewModel;


public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private ActivityCalendarBinding binding;
    private PrefDataStore prefDataStore;

    private DateViewModel dateViewModel;
    private ScheduleViewModel scheduleViewModel;
    private RepeatViewModel repeatViewModel;

    /* ---------- 主要 な 関数 ---------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        EdgeToEdge.enable(this);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefDataStore = PrefDataStore.getInstance(this);

        // ViewModelの初期化
        dateViewModel = new ViewModelProvider(this).get(DateViewModel.class);
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        repeatViewModel = new ViewModelProvider(this).get(RepeatViewModel.class);

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

        // カレンダーテーブルの作成
        makeCalendarTable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Calendar calendar = Calendar.getInstance();  // 現在のカレンダーを取得

        // Intentの取得
        Intent resultIntent = getIntent();
        int year = resultIntent.getIntExtra("selectedYear", -1);
        int month = resultIntent.getIntExtra("selectedMonth", -1);

        if (year == -1 || month == -1) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
        }else{
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
        }

        Log.d(TAG,"onResume : " + year + "/" + month);

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

        // ヘッダーを更新
        updateHeaderText(year, month);

        // 未取得の年なら祝日を新たに取得
        if (!fetchedYears.contains(year)) {setHolidays(year);}

        // データの再描画
        refreshCalendarData(year, month);
    }

    // 曜日・日付の計算、外枠の作成
    private void refreshCalendarData(int year, int month) {

        // 読み込み終了までロック
        binding.progressBar.setVisibility(View.VISIBLE);  // ローディング表示開始
        binding.lastMonthButton.setEnabled(false);        // ボタン無効化
        binding.nextMonthButton.setEnabled(false);

        Log.d(TAG,"refresh calendar : " + year + "/" + month);

        // カレンダーの初期化
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDay = 0;

        BusyData[] busys = new BusyData[43];
        for (int h = 0; h < 42; h++) {
            busys[h] = new BusyData();
        }

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
                searchSchedule(linearLayout,year,month-1,date,busys,i);
                busys[i].setGray(true);
            } else if (date > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                date -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addCircle(dateFrame,year,month,date, true);
                addDate(dateFrame,linearLayout,year,month,date,true);
                searchSchedule(linearLayout,year,month+1,date,busys,i);
                busys[i].setGray(true);
            } else {
                addCircle(dateFrame,year,month,date, false);
                addDate(dateFrame,linearLayout,year,month,date,false);
                setDefaultBusy(year,month, date,busys,i);
                searchSchedule(linearLayout,year,month,date,busys,i);
                busys[i].setGray(false);
            }
            viewBusy(busys, i);
        }
    }

    // 日付から予定の有無を判断し、日付毎のレイアウトを生成
    private void searchSchedule(LinearLayout linearLayout, int year, int month, int day, BusyData busys[], int cell) {

        // 各日のスケジュール数の記録
        Integer[] numSchedules = new Integer[42];
        for (int h = 0; h < 42; h++) {
            numSchedules[h] = 0;
        }

        // ダイアログボタン用のフレームを生成
        FrameLayout frameLayout = getButtonFrame();
        linearLayout.addView(frameLayout);

        // 予定超過数を表示するテキストを生成
        TextView moreTextView = getMoreTextView();
        linearLayout.addView(moreTextView);

        // 日付毎に1つのボタンを生成
        Button dateButton = getDateButton();
        frameLayout.addView(dateButton);

        // 日付毎に予定表示用のレイアウトを作成
        LinearLayout scheduleLayout = getScheduleLayout();
        frameLayout.addView(scheduleLayout);

        // BottomSheetDialogを生成
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_detail_layout, null);
        bottomSheetDialog.setContentView(dialogView);
        LinearLayout scheduleContainer = dialogView.findViewById(R.id.schedule_container);
        TextView strongText = dialogView.findViewById(R.id.strong);

        TextView dialogDate = dialogView.findViewById(R.id.date);
        dialogDate.setText(year + "/" + (month+1) + "/" + day);

        ImageButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(cancel -> {
            // ダイアログを閉じる
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        dateButton.setOnClickListener(showDialog -> {
            if (numSchedules[cell] > 0) {
                bottomSheetDialog.show();
            }
        });
        dateButton.bringToFront();

        // 予定の取得
        observeOnce(dateViewModel.getDateBySpecificDay(year, month, day), date -> {
            if (date != null) {
                int dateId = date.getId();
                observeOnce(scheduleViewModel.getSchedulesByDateId(dateId), schedules -> {
                    if (schedules != null) {
                        for (Schedule schedule : schedules) {
                            addSchedule(schedule, moreTextView, scheduleLayout, bottomSheetDialog, scheduleContainer, strongText, year, month, busys, numSchedules, cell);
                        }
                    }else{
                        dateViewModel.delete(date);
                    }
                });
            }
        });

        // 繰り返し予定を取得
        observeOnce(repeatViewModel.getAllRepeats(), repeatSchedules -> {
            if (repeatSchedules != null) {
                for (Repeat repeatSchedule : repeatSchedules) {
                    // 繰り返し条件を満たすスケジュールをrepeatSchedulesに追加
                    int repeatType = repeatSchedule.getRepeat();
                    int week = getWeekOfMonth(year, month, day);
                    int dayOfWeek = getDayOfWeek(year, month, day);
                    Log.d(TAG, "repeatType:" + repeatType + "week:" + week + "dayOfWeek:" + dayOfWeek);

                    if (repeatType == week || repeatType == day || repeatType == dayOfWeek) {
                        int firstDateId = repeatSchedule.getDateId();
                        observeOnce(dateViewModel.getDateById(firstDateId), firstDate -> {
                            Calendar cellDay = Calendar.getInstance();
                            cellDay.set(year, month, day);
                            Calendar firstDay = Calendar.getInstance();
                            firstDay.set(firstDate.getYear(), firstDate.getMonth(), firstDate.getDay());

                            if (cellDay.after(firstDay)) {
                                observeOnce(scheduleViewModel.getScheduleById(repeatSchedule.getScheduleId()), schedule -> {
                                    if (schedule != null) {
                                        addSchedule(schedule, moreTextView, scheduleLayout, bottomSheetDialog, scheduleContainer, strongText, year, month, busys, numSchedules, cell);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });

        // 読み込み終了後、ロック解除
        binding.progressBar.setVisibility(View.GONE);  // ローディング表示終了
        binding.lastMonthButton.setEnabled(true);      // ボタン再有効化
        binding.nextMonthButton.setEnabled(true);
    }

    // 予定の追加
    private void addSchedule(Schedule schedule, TextView moreTextView, LinearLayout scheduleLayout, BottomSheetDialog bottomSheetDialog, LinearLayout scheduleContainer, TextView strongText, int year, int month, BusyData[] busys, Integer[] numSchedules, int cell) {
        // 予定の各フィールドを取得
        String title = schedule.getTitle();
        String startTime = schedule.getStartTime();
        String endTime = schedule.getEndTime();
        int strong = schedule.getStrong();
        String eventColor = schedule.getColor();
        String memo = schedule.getMemo();

        Log.d(TAG, "Schedule By ID[" + cell + "]: " + title + " id: " + schedule.getId());

        // 忙しさの表示
        busys[cell].setBusy(strong);
        // スケジュール数の取得
        numSchedules[cell]++;
        int countSchedule = numSchedules[cell];
        Log.d(TAG, "count:" + countSchedule);

        // カレンダー表示用テキストビューを生成
        if (countSchedule < 4) {
            TextView indexTextView = getIndexTextView(title, eventColor);
            scheduleLayout.addView(indexTextView);
            // 予定超過数を更新
            moreTextView.setText("");
        }else{
            // 予定超過数を更新
            moreTextView.setText("+" + (countSchedule - 3));
            Log.d(TAG, "more:" + (countSchedule - 3));
        }

        // ダイアログ内予定表示用のレイアウトを生成
        LinearLayout detailDialog = getDialogLayout();

        // TextView（時間）
        TextView timeTextView = getDialogTimeTextView(startTime, endTime);
        detailDialog.addView(timeTextView);

        // LinearLayout (タイトル + メモ)
        LinearLayout titleContainer = getTitleContainer();

        // TextView (タイトル)
        TextView titleTextView = getTitleTextView(title);
        titleContainer.addView(titleTextView);

        // TextView (メモ)
        if(memo != null && !memo.isEmpty()) {
            TextView memoTextView = getMemoTextView(memo);
            titleContainer.addView(memoTextView);
        }

        detailDialog.addView(titleContainer);

        // ボタン用LinearLayout
        LinearLayout buttonContainer = getButtonContainer();

        // ImageButton (削除ボタン)
        ImageButton deleteButton = getDeleteButton();
        buttonContainer.addView(deleteButton);

        deleteButton.setOnClickListener(edit -> {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2.setTitle("予定を削除しますか？")
                    .setPositiveButton("削除", (dialog2, which) -> {

                        Log.d(TAG, "delete:" + schedule.getTitle());

                        scheduleViewModel.delete(schedule);

                        bottomSheetDialog.dismiss();

                        Intent resultIntent = new Intent(CalendarActivity.this, CalendarActivity.class);
                        resultIntent.putExtra("selectedYear", year);
                        resultIntent.putExtra("selectedMonth", month);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(resultIntent);
                        finish(); // 画面を閉じる

                    })
                    .setNegativeButton("キャンセル", (dialog2, which) -> {
                        // ダイアログを閉じる
                        bottomSheetDialog.dismiss();
                    })
                    .show();
        });

        // ImageButton (編集ボタン)
        ImageButton editButton = getEditButton();
        buttonContainer.addView(editButton);

        editButton.setOnClickListener(edit -> {
            bottomSheetDialog.dismiss();
            // Intent を作成して EditSchedule へ遷移
            Intent intent = new Intent(CalendarActivity.this, EditScheduleActivity.class);
            intent.putExtra("scheduleId", schedule.getId());
            startActivity(intent);
        });
        detailDialog.addView(buttonContainer);

        scheduleContainer.addView(detailDialog);

        viewBusy(busys, cell - 1);
        viewBusy(busys, cell + 1);
        strongText.setText(stringBusy(viewBusy(busys, cell)));
    }

    /* ---------- 祝日 に 関する 記述 ---------- */

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
    }

    /* ---------- UI 表示 ---------- */

    // ヘッダーテキストを更新するメソッド
    private void updateHeaderText(int year, int month) {
        String headerText = year + "年 " + (month+1) + "月";
        binding.headerText.setText(headerText);
    }

    // カレンダーテーブルの作成
    private void makeCalendarTable(){
        Log.d(TAG,"make calendar");

        // カレンダーテーブルの作成
        TableLayout tableLayout = findViewById(R.id.calender);

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

    /* ---------- UI 関連 の 設定 ---------- */

    // 日付のボタン用のフレームを生成
    private @NonNull FrameLayout getButtonFrame() {
        FrameLayout frameLayout = new FrameLayout(this);
        LinearLayout.LayoutParams frameLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        frameLayout.setLayoutParams(frameLayoutParams);
        return frameLayout;
    }

    // 予定超過数を表示するテキストを生成
    private @NonNull TextView getMoreTextView() {
        TextView moreTextView = new TextView(this);
        moreTextView.setTextColor(Color.WHITE);
        moreTextView.setTextSize(16);
        moreTextView.setTypeface(null, Typeface.BOLD);
        moreTextView.setSingleLine(true);
        moreTextView.setBackgroundColor(Color.TRANSPARENT);
        moreTextView.setGravity(Gravity.END);

        LinearLayout.LayoutParams moreLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        moreLayoutParams.setMargins(5, 0, 5, 5);
        moreTextView.setLayoutParams(moreLayoutParams);
        return moreTextView;
    }

    // 日付のボタンを生成
    private @NonNull Button getDateButton() {
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
        return dateButton;
    }

    // 予定表示用のレイアウトを生成
    private @NonNull LinearLayout getScheduleLayout() {
        LinearLayout scheduleLayout = new LinearLayout(this);
        scheduleLayout.setLayoutParams( new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        scheduleLayout.removeAllViews();
        scheduleLayout.setOrientation(LinearLayout.VERTICAL);
        return scheduleLayout;
    }

    // カレンダー上に表示する予定のテキストを生成
    private @NonNull TextView getIndexTextView(String title, String eventColor) {
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
        return indexTextView;
    }

    // ダイアログ内予定表示用のレイアウトを生成
    private @NonNull LinearLayout getDialogLayout() {
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
        return detailDialog;
    }

    // ダイアログ内予定のテキスト（時間）を生成
    private @NonNull TextView getDialogTimeTextView(String startTime, String endTime) {
        TextView timeTextView = new TextView(this);
        timeTextView.setText(startTime + " ~ " + endTime);
        timeTextView.setPadding(20, 0, 0, 0);
        timeTextView.setGravity(Gravity.CENTER_VERTICAL);
        timeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));
        return timeTextView;
    }

    // ダイアログ内予定のテキスト（タイトル・メモ）を含むレイアウトを生成
    private @NonNull LinearLayout getTitleContainer() {
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 3
        );
        titleContainerParams.leftMargin = 30;
        titleContainer.setLayoutParams(titleContainerParams);
        return titleContainer;
    }

    // ダイアログ内予定のテキスト（タイトル）を生成
    private @NonNull TextView getTitleTextView(String title) {
        TextView titleTextView = new TextView(this);
        titleTextView.setText(title);
        titleTextView.setTextSize(20);
        titleTextView.setGravity(Gravity.CENTER_VERTICAL);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView.setSingleLine(true);
        LinearLayout.LayoutParams titleTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleTextParams.rightMargin = 30;
        titleTextView.setLayoutParams(titleTextParams);
        return titleTextView;
    }

    // ダイアログ内予定のテキスト（メモ）を生成
    private @NonNull TextView getMemoTextView(String memo) {
        TextView memoTextView = new TextView(this);
        memoTextView.setText(memo);
        memoTextView.setTextSize(14);
        memoTextView.setEllipsize(TextUtils.TruncateAt.END);
        memoTextView.setSingleLine(true);
        LinearLayout.LayoutParams memoTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        memoTextParams.leftMargin = 30;
        memoTextParams.rightMargin = 30;
        memoTextView.setLayoutParams(memoTextParams);
        return memoTextView;
    }

    // ダイアログ内予定のボタンを含むレイアウトを生成
    private @NonNull LinearLayout getButtonContainer() {
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        buttonContainer.setGravity(Gravity.END);
        buttonContainer.setLayoutParams(buttonContainerParams);
        return buttonContainer;
    }

    // ダイアログ内予定の削除ボタンを生成
    private @NonNull ImageButton getDeleteButton() {
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
        return deleteButton;
    }

    // ダイアログ内予定の編集ボタンを生成
    private @NonNull ImageButton getEditButton() {
        ImageButton editButton = new ImageButton(this);
        editButton.setBackgroundColor(Color.TRANSPARENT);
        editButton.setImageResource(R.drawable.ic_arrow_forward);
        editButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        editButton.setLayoutParams(buttonParams);
        return editButton;
    }

    /* ---------- 忙しさ の 表示 ---------- */

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
    private int viewBusy(BusyData[] busydata, int i) {
        int busy;
        LinearLayout ll = findViewById(i);
        if(i == 0){
            busy = busydata[i].getBusy() + busydata[i+1].getBusy()/3;
        } else if (i == 41) {
            busy = busydata[i].getBusy() + busydata[i-1].getBusy()/3;
        } else {
            busy = busydata[i].getBusy() + (busydata[i-1].getBusy()+busydata[i+1].getBusy())/2;
        }
        busy += busydata[i].getDefaultBusy();
        if(busy != 0){Log.d(TAG, "viewBusy[ " + i + "]:"+ busy);}
        if(busy > 7){busy = 7;}
        if(busy < 0){busy = 0;}
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
            if(busy != 0){Log.d(TAG, "viewBusy[ " + i + "]:grey");}
        }
        return busy;
    }

    /* ---------- データ 変換 ---------- */

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

    // 忙しさの表示(ダイアログ用)
    private String stringBusy(int busy) {
        if (busy >= 7) {
            return "⑦+";
        }
        switch (busy) {
            case 6: return "⑥";
            case 5: return "⑤";
            case 4: return "④";
            case 3: return "③";
            case 2: return "②";
            case 1: return "①";
            default: return "";
        }
    }


    /* ---------- observe の 解放 ---------- */

    private <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observe(this, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);
            }
        });
    }

}