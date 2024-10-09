package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityAddScheduleBinding;

public class EditScheduleActivity extends AppCompatActivity {

    private ActivityAddScheduleBinding binding;

    private int selectedYear, selectedMonth, selectedDay;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // CalendarActivityからdocumentIDを取得し、Firebaseに接続
        Intent intent = getIntent();
        String collectionPath = intent.getStringExtra("collectionPath");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference calendarRef = db.collection(collectionPath);

        // Button名を変更
        binding.save.setText("変更を適用");
        binding.back.setText("キャンセル");

        // Spinner の設定
        String[] strongOptions = {"1","2", "3", "4", "5"};
        var adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strongOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerNumber.setAdapter(adapter);

        String[] repeatOptions = {"なし", "毎週", "隔週", "毎月"};
        var repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.answer.setAdapter(repeatAdapter);

        binding.inputDate.setOnClickListener(view -> showDatePickerDialog());
        binding.TimeFirst.setOnClickListener(view -> showTimePickerDialog(binding.TimeFirst));
        binding.TimeFinal.setOnClickListener(view -> showTimePickerDialog(binding.TimeFinal));

        binding.back.setOnClickListener(view -> {
            finish(); // 現在のアクティビティを終了して前の画面に戻る
        });

        binding.TimeFirst.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showTimePickerDialog(binding.TimeFirst);
                return true;
            }
            return false;
        });

        binding.TimeFinal.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showTimePickerDialog(binding.TimeFinal);
                return true;
            }
            return false;
        });

        binding.inputDate.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showDatePickerDialog();
                return true;
            }
            return false;
        });

        calendarRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // 予定の各フィールドを取得
                    String initialTitle = document.getString("タイトル");
                    String initialStartTime = document.getString("開始時間");
                    String initialEndTime = document.getString("終了時間");
                    String initialIntensity = document.getString("強度");
                    String initialMemo = document.getString("メモ");
                    String initialRepeat = document.getString("繰り返し");
                    String documentID = document.getId();

                    // UIに反映
                    binding.inputText.setText(initialTitle);
                    binding.TimeFirst.setText(initialStartTime);
                    binding.TimeFinal.setText(initialEndTime);
                    binding.memo.setText(initialMemo);
                    binding.spinnerNumber.setSelection(adapter.getPosition(initialIntensity));
                    binding.answer.setSelection(repeatAdapter.getPosition(initialRepeat));
                    binding.inputDate.setText(selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay);

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
                        String year = String.valueOf(selectedYear);
                        String month = String.valueOf(selectedMonth);
                        String date = String.valueOf(selectedDay);  // ここに日付の変数を追加

                        // Firestore にデータを保存
                        setDataToFirestore(db, year, month, date, documentID, scheduleData);

                        // メッセージの表示
                        var message = "タイトル : " + title + "\n日付 : "+year + "/" + month + "/" + date + "\n開始時間 : " + startTime + "\n終了時間 : " + endTime +
                                "\n強度 : " + intensity + "\nメモ : " + memo + "\n繰り返し : " + answer;
                        showConfirmationDialog(message);
                    });
                }
            }
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

    private void showDatePickerDialog() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {

            this.selectedYear = selectedYear;
            this.selectedMonth = selectedMonth + 1;
            this.selectedDay = selectedDay;

            binding.inputDate.setText(selectedYear + "/" + this.selectedMonth + "/" + this.selectedDay);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Firestore にデータを保存するメソッド
    public void setDataToFirestore(FirebaseFirestore db,String documentYear, String month, String date,String documentID, Map<String, Object> data) {
        db.collection(documentYear)
                .document(month)
                .collection(date)
                .document(documentID)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Data successfully saved!");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error saving data: " + e.getMessage());
                });
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%2d:%02d", selectedHour, selectedMinute);
                    editText.setText(time);
                }, hour, minute, true); // 'true' for 24-hour format

        timePickerDialog.show();
    }

}
