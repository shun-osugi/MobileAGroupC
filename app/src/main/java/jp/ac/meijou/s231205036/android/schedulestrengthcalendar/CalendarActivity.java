package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
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
        // カレンダーの初期化
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        String name = "aaa";
        //Calendar calendar = Calendar.getInstance();
        //int year = 2024;
        //int month = 10 - 1;
        int date = 1;
        int dayNum = 0;
        int firstDay = 0;
        calendar.set(year, month, date);

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

        CalendarCell cell = new CalendarCell(0, 0);

        for (int i = 0; i < 42; i++) {
            LinearLayout linearLayout = findViewById(i);
            linearLayout.removeAllViews();  // 全てのビューをクリア
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            FrameLayout frameLayout = new FrameLayout(this);

            ImageView imageView = new ImageView(this);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            imageParams.gravity = Gravity.CENTER;
            imageView.setLayoutParams(imageParams);

            Drawable drawable = getResources().getDrawable(R.drawable.ic_circle);
            imageView.setImageDrawable(drawable);

            TextView textView = new TextView(this);
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = Gravity.CENTER;
            textView.setLayoutParams(textParams);

            // 日付を計算
            int day = dayNum + 1 - firstDay;
            if (day <= 0) {
                day += calendar.getActualMaximum(Calendar.DAY_OF_MONTH - 1);
                textView.setTextColor(Color.GRAY);  // 前月の日付はグレー
            } else if (day > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                day -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                textView.setTextColor(Color.GRAY);  // 前月の日付はグレー
            }

            textView.setText(day + "");
            dayNum++;

            frameLayout.addView(imageView);
            frameLayout.addView(textView);

            FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            frameLayout.setLayoutParams(params2);
            linearLayout.addView(frameLayout);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String documentPath = name + "/" + year + "/" + (month+1) + "/" + day;
            DocumentReference calendarRef = db.document(documentPath);

            calendarRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String data = task.getResult().getString("タイトル");

                        //忙しさ表示
                        //viewBusy(Integer.valueOf(task.getResult().getString("強度")), linearLayout, CalendarCell);
                        cell.setday1Busy(Integer.valueOf(task.getResult().getString("強度")));
                        viewBusy(linearLayout, cell);

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
                } else {
                    System.err.println("Error getting document: " + task.getException());
                }
            });
        }
    }

    //忙しさ表示関数
    private void viewBusy(final LinearLayout ll, CalendarCell cell) {
        int busy = cell.getday1Busy() + cell.getday0Busy() % 2;
        if(busy > 7){
            busy = 7;
        }
        switch (busy) {
            case 7 -> ll.setBackgroundColor(Color.rgb(157, 73, 255));
            case 6 -> ll.setBackgroundColor(Color.rgb(255, 73, 73));
            case 5 -> ll.setBackgroundColor(Color.rgb(255, 149, 73));
            case 4 -> ll.setBackgroundColor(Color.rgb(255, 215, 73));
            case 3 -> ll.setBackgroundColor(Color.rgb(186, 155, 73));
            case 2 -> ll.setBackgroundColor(Color.rgb(73, 255, 146));
            case 1 -> ll.setBackgroundColor(Color.rgb(73, 255, 233));
            case 0 -> ll.setBackgroundColor(Color.rgb(188, 188, 188));
            default -> ll.setBackgroundColor(Color.rgb(0,0,0));
        }
        //cell.shiftBusy();
    }
}