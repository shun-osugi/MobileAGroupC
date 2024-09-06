package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityCalendarBinding;
import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityMainBinding;


public class CalendarActivity extends AppCompatActivity {

    private ActivityCalendarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TableLayout tableLayout = findViewById(R.id.calender);

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
                linearLayout.setBackgroundColor(Color.rgb(255, 0, 0));
                linearLayout.setId(idCounter);
                tableRow.addView(linearLayout);
                idCounter++;
            }
            tableLayout.addView(tableRow);
        }

        binding.addButton.setOnClickListener(view -> {
            var intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });
    }

    protected void onStart() {
        super.onStart();
        Calendar calendar = Calendar.getInstance();
        int year  = 2024;
        int month = 10 - 1;
        int date  = 1;
        int dayNum = 0;
        int firstDay = 0;
        calendar.set(year, month, date);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                firstDay = 0;
                break;
            case Calendar.MONDAY:
                firstDay = 1;
                break;
            case Calendar.TUESDAY:
                firstDay = 2;
                break;
            case Calendar.WEDNESDAY:
                firstDay = 3;
                break;
            case Calendar.THURSDAY:
                firstDay = 4;
                break;
            case Calendar.FRIDAY:
                firstDay = 5;
                break;
            case Calendar.SATURDAY:
                firstDay = 6;
                break;
        }

        for (int i = 0; i < 42; i++) {
            LinearLayout linearLayout = findViewById(i);

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
            int day = dayNum + 1 - firstDay;
            if (day <= 0) {
                day += calendar.getActualMaximum(Calendar.DAY_OF_MONTH - 1);
            } else if (day > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                day -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
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
        }
    }

}