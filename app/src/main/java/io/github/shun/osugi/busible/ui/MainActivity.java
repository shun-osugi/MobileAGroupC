package io.github.shun.osugi.busible.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            navigateToCalendar();
    }
    // CalendarActivity に遷移するメソッド
    private void navigateToCalendar() {
        var intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
        finish();  // 現在の Activity を終了
    }
}
