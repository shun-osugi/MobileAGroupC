package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TableLayout tableLayout = findViewById(R.id.calender);

        for (int i = 0; i < 6; i++) {
            TableRow tableRow = new TableRow(this);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
            );
            tableRow.setLayoutParams(rowParams);
            for (int j = 0; j < 7; j++) {
                Button button = new Button(this);
                button.setText("Button " + (i * 7 + j));
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1f
                );
                button.setLayoutParams(params);
                button.setTextAlignment(Button.TEXT_ALIGNMENT_CENTER);
                button.setEllipsize(android.text.TextUtils.TruncateAt.END);
                button.setBackgroundColor(Color.rgb(255, 0, 0));
                tableRow.addView(button);
            }
            tableLayout.addView(tableRow);
        }
    }
}