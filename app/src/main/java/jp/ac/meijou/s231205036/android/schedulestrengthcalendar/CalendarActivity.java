package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
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
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;


import org.json.JSONObject;

import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityCalendarBinding;


public class CalendarActivity extends AppCompatActivity {
    private ActivityCalendarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 現在の年と月を取得して headerText に設定
        Calendar calendar = Calendar.getInstance();
        updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

        //----------祝日に関するプログラム(ここから)-----------------

        //祝日のデータ取得（json形式で帰ってくる）
        HolidayApiFetcher apiFetcher = new HolidayApiFetcher();
        int year = calendar.get(Calendar.YEAR);
        apiFetcher.fetchHolidayData(year, new HolidayApiFetcher.HolidayCallback() {
            @Override
            public void onHolidayDataReceived(JSONObject holidayData) {
                setHolidays(holidayData);
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

        // 前月・次月ボタンのクリックリスナーを設定
        binding.lastMonthButton.setOnClickListener(view -> {
            calendar.add(Calendar.MONTH, -1);
            updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
            refreshCalendarData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        });

        binding.nextMonthButton.setOnClickListener(view -> {
            calendar.add(Calendar.MONTH, 1);
            updateHeaderText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
            refreshCalendarData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        });

        // 他の初期設定;
        binding.addButton.setOnClickListener(view -> {
            var intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        binding.settingButton.setOnClickListener(view -> {
            Intent intent = new Intent(CalendarActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.addButton.setOnClickListener(view -> {
            var intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });



        binding.settingButton.setOnClickListener(view -> {
            // Intent を作成して Setting.java へ遷移
            Intent intent = new Intent(CalendarActivity.this, SettingActivity.class);
            startActivity(intent);
        });
    }

    // 祝日データを格納するセット
    private Set<String> holidaySet = new HashSet<>();

    // HolidayApiFetcher で取得したデータをセットに格納
    private void setHolidays(JSONObject holidayData) {
        try {
            Iterator<String> keys = holidayData.keys();
            while (keys.hasNext()) {
                String key = keys.next(); // 祝日の日付 (yyyy-MM-dd)
                holidaySet.add(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                linearLayout.setBackgroundResource(R.drawable.border0);
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
        // Firestoreの呼び出しが完了した回数
        AtomicInteger calls = new AtomicInteger(0);

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
            } else if (date > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                date -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addCircle(frameLayout,year,month,date, true);
                addDate(frameLayout,linearLayout,year,month,date,true);
                addSchedule(frameLayout,linearLayout,year,month+1,date,busys,i,calls);
            } else {
                addCircle(frameLayout,year,month,date, false);
                addDate(frameLayout,linearLayout,year,month,date,false);
                addSchedule(frameLayout,linearLayout,year,month,date,busys,i,calls);
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
        boolean isToday = today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == date
                && !grey;
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
    private void addSchedule(FrameLayout frameLayout, LinearLayout linearLayout, int year, int month, int date, BusyData busys[], int cell, AtomicInteger calls) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionPath = year + "/" + (month + 1) + "/" + date;
        CollectionReference calendarRef = db.collection(collectionPath);

        calendarRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // ここでドキュメントIDを取得し、その中のフィールドを取得する
                    String documentID = document.getId();  // 一意のdocumentID

                    // 予定の各フィールドを取得
                    String title = document.getString("タイトル");
                    String startTime = document.getString("開始時間");
                    String endTime = document.getString("終了時間");
                    String strong = document.getString("強度");
                    String memo = document.getString("メモ");
                    String repeat = document.getString("繰り返し");

                    //忙しさの保存
                    busys[cell].setDay1Busy(Integer.valueOf(strong));

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
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        LayoutInflater inflater = this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_detail_layout, null);

                        builder.setView(dialogView);

                        TextView dialogStrong = dialogView.findViewById(R.id.strong);
                        TextView dialogTitle = dialogView.findViewById(R.id.title);
                        TextView dialogDate = dialogView.findViewById(R.id.date);
                        TextView dialogTime = dialogView.findViewById(R.id.time);
                        TextView dialogRepeat = dialogView.findViewById(R.id.repeat);
                        TextView dialogMemo = dialogView.findViewById(R.id.memo);
                        ImageButton buttonEdit = dialogView.findViewById(R.id.buttonEdit);
                        ImageButton buttonDelete = dialogView.findViewById(R.id.buttonDelete);
                        ImageButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);

                        dialogStrong.setText(stringBusy(Integer.valueOf(strong)));
                        dialogTitle.setText(title + "");
                        dialogDate.setText(year + "/" + (month+1) + "/" + date);
                        dialogTime.setText(startTime + " ~ " + endTime);
                        dialogRepeat.setText(repeat + "");
                        dialogMemo.setText(memo + "");

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        buttonEdit.setOnClickListener(edit -> {
                            // Intent を作成して EditSchedule へ遷移
                            Intent intent = new Intent(CalendarActivity.this, EditScheduleActivity.class);
                            intent.putExtra("collectionPath", documentID);
                            startActivity(intent);
                        });

                        buttonDelete.setOnClickListener(delete -> {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                            builder2.setTitle("予定を削除しますか？")
                            .setPositiveButton("削除", (dialog2, which) -> {
                                // 削除し、ダイアログを閉じる
                                db.collection(collectionPath).document(documentID).delete();
                                dialog.dismiss();
                                refreshCalendarData(year, month+1);
                            })
                            .setNegativeButton("キャンセル", (dialog2, which) -> {
                                // ダイアログを閉じる
                                dialog.dismiss();
                            })
                                    .show();

                        });

                        buttonCancel.setOnClickListener(cancel -> {
                            // ダイアログを閉じる
                            dialog.dismiss();
                        });
                    });

                    linearLayout.addView(button);
                }
                binding.progressBar.setVisibility(View.GONE);  // ローディング表示終了
                binding.lastMonthButton.setEnabled(true);      // ボタン再有効化
                binding.nextMonthButton.setEnabled(true);
            } else {
                System.err.println("Error getting document: " + task.getException());
                binding.progressBar.setVisibility(View.GONE);  // ローディング表示終了
                binding.lastMonthButton.setEnabled(true);      // ボタン再有効化
                binding.nextMonthButton.setEnabled(true);
            }
            //データ取得のカウンタ
            int calledcount = calls.incrementAndGet();
            if (calledcount == 42) {
                viewBusy(busys);
            }
        });
    }

    // 忙しさの表示
    private void viewBusy(BusyData[] busydata) {
        for(int i=0; i<42; i++){
            LinearLayout ll = findViewById(i);
            int busy = busydata[i].getDay1Busy() + busydata[i].getDay0Busy()/2;
            switch (busy) {
                case 7 -> ll.setBackgroundResource(R.drawable.border7);
                case 6 -> ll.setBackgroundResource(R.drawable.border6);
                case 5 -> ll.setBackgroundResource(R.drawable.border5);
                case 4 -> ll.setBackgroundResource(R.drawable.border4);
                case 3 -> ll.setBackgroundResource(R.drawable.border3);
                case 2 -> ll.setBackgroundResource(R.drawable.border2);
                case 1 -> ll.setBackgroundResource(R.drawable.border1);
                case 0 -> ll.setBackgroundResource(R.drawable.border0);
            }
            busydata[i+1].setDay0Busy(busy);
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