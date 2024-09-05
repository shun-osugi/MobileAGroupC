package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

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

        // 保存されたユーザー名を取得して表示
        String savedUser = prefDataStore.getString("name").orElse("");
        String saveFlag = prefDataStore.getString("flag").orElse("");
        binding.textView.setText(saveFlag);

        binding.button.setOnClickListener(view -> {
            var user = binding.editTextText.getText().toString();
            var flag = "0";
            prefDataStore.setString("name", user);
            prefDataStore.setString("flag",flag);
        });
    }

}