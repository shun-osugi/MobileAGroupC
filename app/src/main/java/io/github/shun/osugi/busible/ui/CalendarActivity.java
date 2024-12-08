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
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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
        for (int h = 0; h < 43; h++) {
            busys[h] = new BusyData();
        }
        // 完了した回数
        AtomicInteger calls = new AtomicInteger(0);

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

            FrameLayout frameLayout = new FrameLayout(this);

            // 日付を計算
            int date = i + 1 - firstDay;
            if (date <= 0) {
                calendar.add(Calendar.MONTH, -1);
                date += calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addCircle(frameLayout,year,month,date, true);
                addDate(frameLayout,linearLayout,year,month,date,true);
                calendar.add(Calendar.MONTH, 1);
                addSchedule(frameLayout,linearLayout,year,month-1,date,busys,i,calls);
                busys[i].setGray(true);
            } else if (date > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                date -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addCircle(frameLayout,year,month,date, true);
                addDate(frameLayout,linearLayout,year,month,date,true);
                addSchedule(frameLayout,linearLayout,year,month+1,date,busys,i,calls);
                busys[i].setGray(true);
            } else {
                addCircle(frameLayout,year,month,date, false);
                addDate(frameLayout,linearLayout,year,month,date,false);
                setDefaultBusy(year,month, date,busys,i);
                addSchedule(frameLayout,linearLayout,year,month,date,busys,i,calls);
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

    // 予定の表示
    private void addSchedule(FrameLayout frameLayout, LinearLayout linearLayout, int year, int month, int day, BusyData busys[], int cell, AtomicInteger calls) {
        LiveData<Date> dateLiveData = dateViewModel.getDateBySpecificDay(year, month, day);
        dateLiveData.observe(this, date -> {
            if (date != null) {
                int dateId = date.getId();
                LiveData<List<Schedule>> scheduleLiveData = scheduleViewModel.getSchedulesByDateId(dateId);
                scheduleLiveData.observe(this, schedules -> {
                    if (schedules != null) {
                        for (Schedule schedule : schedules) {

                            // 予定の各フィールドを取得
                            String title = schedule.getTitle();
                            String startTime = schedule.getStartTime();
                            String endTime = schedule.getEndTime();
                            int strong = schedule.getStrong();
                            String memo = schedule.getMemo();
                            String repeat = schedule.getRepeat();

                            Log.d(TAG, "Schedule By ID: " + title + schedule.getId());

                            //忙しさの保存
                            busys[cell].setBusy(strong);

                            // ボタンの生成
                            Button button = new Button(this);
                            button.setText(title);
                            button.setTextSize(9);
                            button.setEllipsize(TextUtils.TruncateAt.END);
                            button.setMaxLines(1);

                            // Buttonの高さを固定
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    100  // 固定の高さ
                            );
                            button.setLayoutParams(buttonParams);

                            // 詳細ダイアログを表示
                            button.setOnClickListener(viewDialog -> {
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                                LayoutInflater inflater = this.getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.dialog_detail_layout, null);

                                bottomSheetDialog.setContentView(dialogView);

                                TextView dialogStrong = dialogView.findViewById(R.id.strong);
                                TextView dialogTitle = dialogView.findViewById(R.id.title);
                                TextView dialogDate = dialogView.findViewById(R.id.date);
                                TextView dialogTime = dialogView.findViewById(R.id.time);
                                //TextView dialogRepeat = dialogView.findViewById(R.id.repeat);
                                //TextView dialogMemo = dialogView.findViewById(R.id.memo);
                                ImageButton buttonEdit = dialogView.findViewById(R.id.buttonEdit);
                                ImageButton buttonDelete = dialogView.findViewById(R.id.buttonDelete);
                                ImageButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);

                                dialogStrong.setText(stringBusy(Integer.valueOf(strong)));
                                dialogTitle.setText(title);
                                dialogDate.setText(year + "/" + (month+1) + "/" + day);
                                dialogTime.setText(startTime + " ~ " + endTime);
                                //dialogRepeat.setText(repeat);
                                //dialogMemo.setText(memo);

                                bottomSheetDialog.show();


                                buttonEdit.setOnClickListener(edit -> {
                                    // Intent を作成して EditSchedule へ遷移
                                    Intent intent = new Intent(CalendarActivity.this, EditScheduleActivity.class);
                                    intent.putExtra("scheduleId", schedule.getId());
                                    startActivity(intent);
                                });

                                buttonDelete.setOnClickListener(delete -> {
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

                                buttonCancel.setOnClickListener(cancel -> {
                                    // ダイアログを閉じる
                                    bottomSheetDialog.dismiss();
                                });
                            });

                            linearLayout.addView(button);
                        }
                    }
                });
            }
        });

        //データ取得のカウンタ(忙しさ設定用)
        int calledcount = calls.incrementAndGet();
        if (calledcount == 42) {
            viewBusy(busys);
        }

        // 読み込み終了後、ロック解除
        binding.progressBar.setVisibility(View.GONE);  // ローディング表示終了
        binding.lastMonthButton.setEnabled(true);      // ボタン再有効化
        binding.nextMonthButton.setEnabled(true);
    }


    //デフォルトの忙しさ反映(当月の曜日走査)
    private void setDefaultBusy(int year, int month, int date, BusyData busys[], int cell) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
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
                busy = busydata[i].getBusy() + (busydata[i+1].getBusy()+busydata[i+1].getBusy())/2;
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