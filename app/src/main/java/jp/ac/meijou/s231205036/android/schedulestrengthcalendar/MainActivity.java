package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ActivityMainBinding binding;
    private PrefDataStore prefDataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefDataStore = PrefDataStore.getInstance(this);

        // 永続化されたユーザー名の取得
        String savedUser = prefDataStore.getString("name").orElse(null);

        // データが存在する場合は、即座に CalendarActivity に遷移
        if (savedUser != null && !savedUser.isEmpty()) {
            navigateToCalendar();
        }

        // データが存在しない場合は、ユーザー名を入力させて保存し、CalendarActivity へ遷移
        binding.button.setOnClickListener(view -> {
            var user = binding.editTextText.getText().toString();
            if (!user.isEmpty()) {
                var flag = "0";
                prefDataStore.setString("name", user);
                prefDataStore.setString("flag", flag);
                navigateToCalendar();
            }
        });
    }

    // CalendarActivity に遷移するメソッド
    private void navigateToCalendar() {
        var intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
        finish();  // 現在の Activity を終了
    }
}
