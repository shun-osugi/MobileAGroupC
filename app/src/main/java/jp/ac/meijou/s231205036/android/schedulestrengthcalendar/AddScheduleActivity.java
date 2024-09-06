package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityAddScheduleBinding;

public class AddScheduleActivity extends AppCompatActivity {

    private ActivityAddScheduleBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firestore インスタンスを取得
        db = FirebaseFirestore.getInstance();

        // Spinner の設定
        String[] strongOptions = {"1", "2", "3", "4", "5"};
        var adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strongOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerNumber.setAdapter(adapter);

        String[] repeatOptions = {"毎週", "隔週", "毎月", "なし"};
        var repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.answer.setAdapter(repeatAdapter);

        // 保存ボタンのクリックイベント
        binding.save.setOnClickListener(view -> {
            var title = binding.inputText.getText().toString();
            var startTime = binding.TimeFirst.getText().toString();
            var endTime = binding.TimeFinal.getText().toString();
            var memo = binding.memo.getText().toString();
            var answer = binding.answer.getSelectedItem().toString();
            var intensity = binding.spinnerNumber.getSelectedItem().toString();

            // Firestore に保存するデータを作成
            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("タイトル", title);
            scheduleData.put("開始時間", startTime);
            scheduleData.put("終了時間", endTime);
            scheduleData.put("強度", intensity);
            scheduleData.put("メモ", memo);
            scheduleData.put("繰り返し", answer);

            // 日付データを追加 (後で変数化)
            String collectionName = "aaa"; // 他のコレクションと被らない名前
            String year = "2024";
            String month = "9";
            String day = "6";  // ここに日付の変数を追加

            // Firestore にデータを保存
            saveDataToFirestore(collectionName, year, month, day, scheduleData);

            // メッセージの表示
            var message = "タイトル : " + title + "\n開始時間 : " + startTime + "\n終了時間 : " + endTime +
                    "\n強度 : " + intensity + "\nメモ : " + memo + "\n繰り返し : " + answer;
            showConfirmationDialog(message);
        });
    }

    private void showConfirmationDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    // Firestore にデータを保存するメソッド
    public void saveDataToFirestore(String collectionName, String documentYear, String month, String day, Map<String, Object> data) {
        db.collection(collectionName)
                .document(documentYear)
                .collection(month)
                .document(day)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Data successfully saved!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error saving data: " + e.getMessage());
                });
    }
}
