package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import jp.ac.meijou.s231205036.android.schedulestrengthcalendar.databinding.ActivityAddScheduleBinding;



public class AddScheduleActivity extends AppCompatActivity {

    private ActivityAddScheduleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        binding = ActivityAddScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] StrongOptions = {"1", "2", "3", "4", "5"};
        var adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StrongOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerNumber.setAdapter(adapter);

        String[] repeatOptions = {"毎週", "隔週", "毎月", "なし"};
        var repeatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repeatOptions);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.answer.setAdapter(repeatAdapter);

        binding.save.setOnClickListener(view -> {
            var title = binding.inputText.getText().toString();
            var startTime = binding.TimeFirst.getText().toString();
            var endTime = binding.TimeFinal.getText().toString();
            var memo = binding.memo.getText().toString();
            var answer = binding.answer.getSelectedItem().toString();
            var intensity = binding.spinnerNumber.getSelectedItem().toString();


            var Message = "タイトル : " + title + "\n開始時間 : " + startTime + "\n終了時間 : " + endTime +
                    "\n強度 : " + intensity + "\nメモ : " + memo + "\n繰り返し : " + answer;

            showConfirmationDialog(Message);
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
}