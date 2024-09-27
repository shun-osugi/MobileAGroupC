package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Calendar;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        // 重複してカレンダーが生成されないようにビューをクリア
        tableLayout.removeAllViews();

        int idCounter = 0;
        for (int i = 0; i < 6; i++) {
            TableRow tableRow = new TableRow(this);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
            );
            tableRow.setLayoutParams(rowParams);
            for (int j = 0; j < 7; j++) {
                LinearLayout linearLayout = new LinearLayout(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1f
                );
                linearLayout.setLayoutParams(params);
                linearLayout.setTextAlignment(Button.TEXT_ALIGNMENT_CENTER);
                linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
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
        String name = "aaa";
        int firstDay = 0;

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

        int yesterdayBusy = 0;
        for (int i = 0; i < 42; i++) {
            LinearLayout linearLayout = findViewById(i);
            linearLayout.removeAllViews();  // 全てのビューをクリア
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setBackgroundColor(Color.rgb(188, 188, 188));   // 背景色をリセット

            FrameLayout frameLayout = new FrameLayout(this);
            addCircle(frameLayout);

            // 日付を計算
            int date = i + 1 - firstDay;
            if (date <= 0) {
                calendar.add(Calendar.MONTH, -1);
                date += calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addDate(frameLayout,linearLayout,date,true);
                calendar.add(Calendar.MONTH, 1);
                addSchedule(frameLayout,linearLayout,name,year,month-1,date,yesterdayBusy);
            } else if (date > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                date -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                addDate(frameLayout,linearLayout,date,true);
                addSchedule(frameLayout,linearLayout,name,year,month+1,date,yesterdayBusy);
            } else {
                addDate(frameLayout,linearLayout,date,false);
                addSchedule(frameLayout,linearLayout,name,year,month,date,yesterdayBusy);
            }
        }
    }

    // 日付の円の表示
    private void addCircle(FrameLayout frameLayout) {
        ImageView imageView = new ImageView(this);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        imageParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imageParams);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_circle);
        imageView.setImageDrawable(drawable);
        frameLayout.addView(imageView);
    }

    // 日付の表示
    private void addDate(FrameLayout frameLayout, LinearLayout linearLayout, int date, boolean grey) {
        TextView textView = new TextView(this);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textParams);
        textView.setText(date + "");
        frameLayout.addView(textView);
        if(grey){textView.setTextColor(Color.GRAY);} // 当月以外の日付はグレー
        linearLayout.addView(frameLayout);
    }

    // 予定の表示
    private void addSchedule(FrameLayout frameLayout, LinearLayout linearLayout, String name, int year, int month, int date, int yesterdayBusy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String documentPath = name + "/" + year + "/" + (month+1) + "/" + date;
        DocumentReference calendarRef = db.document(documentPath);

        calendarRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    String data = task.getResult().getString("タイトル");
                    viewBusy(yesterdayBusy, Integer.valueOf(task.getResult().getString("強度")), linearLayout);

                    Button button = new Button(this);
                    button.setText(data);
                    button.setTextSize(9);
                    button.setEllipsize(TextUtils.TruncateAt.END);
                    button.setMaxLines(1);

                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    button.setLayoutParams(buttonParams);

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
        });
    }

    // 忙しさの表示
    private int viewBusy(final int yesterdayBusy,final int todayBusy, final LinearLayout ll) {
        int busy = todayBusy;

        switch (busy) {
            case 7 -> ll.setBackgroundColor(Color.rgb(157, 73, 255));
            case 6 -> ll.setBackgroundColor(Color.rgb(255, 73, 73));
            case 5 -> ll.setBackgroundColor(Color.rgb(255, 149, 73));
            case 4 -> ll.setBackgroundColor(Color.rgb(255, 215, 73));
            case 3 -> ll.setBackgroundColor(Color.rgb(186, 155, 73));
            case 2 -> ll.setBackgroundColor(Color.rgb(73, 255, 146));
            case 1 -> ll.setBackgroundColor(Color.rgb(73, 255, 233));
            case 0 -> ll.setBackgroundColor(Color.rgb(188, 188, 188));
        }
        return busy;
    }
}