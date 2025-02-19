package io.github.shun.osugi.busible.ui;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.shun.osugi.busible.model.PrefDataStore;
import io.github.shun.osugi.busible.R;
import io.github.shun.osugi.busible.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    private PrefDataStore prefDataStore;

    private int[] defaultBusy = new int[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backButton.setOnClickListener(view -> {
            finish();
        });

        prefDataStore = PrefDataStore.getInstance(this);
        binding.sundaySlider.setProgress(prefDataStore.getInteger("sunBusy").orElse(0));
        binding.mondaySlider.setProgress(prefDataStore.getInteger("monBusy").orElse(0));
        binding.tuesdaySlider.setProgress(prefDataStore.getInteger("tueBusy").orElse(0));
        binding.wednesdaySlider.setProgress(prefDataStore.getInteger("wedBusy").orElse(0));
        binding.thursdaySlider.setProgress(prefDataStore.getInteger("thuBusy").orElse(0));
        binding.fridaySlider.setProgress(prefDataStore.getInteger("friBusy").orElse(0));
        binding.saturdaySlider.setProgress(prefDataStore.getInteger("satBusy").orElse(0));

        // SeekBarの値を変数に代入
        binding.sundaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("sunBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.mondaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("monBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.tuesdaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("tueBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.wednesdaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("wedBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.thursdaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("thuBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.fridaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("friBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.saturdaySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefDataStore.setInteger("satBusy", progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 「？」ボタンのクリックイベントを設定
        ImageButton helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(view -> {
            showHelpDialog(0);
        });
    }

    // ヘルプ情報を表示するメソッド
    private void showHelpDialog(int pageIndex) {
        // 色のマッピング
        Map<Integer, String> colorMap = new HashMap<>();

        for (int i = 0; i <= 7; i++) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(
                    getResources().getIdentifier("border" + i, "drawable", getPackageName()));

            if (drawable instanceof GradientDrawable gradientDrawable) {
                // Extracting color from GradientDrawable
                ColorStateList colorStateList = gradientDrawable.getColor();
                if (colorStateList != null) {
                    int color = colorStateList.getDefaultColor();
                    colorMap.put(i, String.format("#%06X", (0xFFFFFF & color))); // Convert to #RRGGBB
                }
            }
        }

// ヘルプ情報をリストで定義
        List<String> helpPages = Arrays.asList(
                "<b>デフォルト強度</b><br><br>" +
                        "　各曜日にあらかじめ設定しておける強度の値のことです。<br>" +
                        "　強度の値を設定することで、カレンダーの背景色が次のルールに基づいて変化します。",
                "<b>1. 設定できる範囲</b><br><br>" +
                        "　強度は各曜日に対して <font color='blue'>-3 から +3</font> の範囲で設定可能です。<br>"+
                        "　また、背景色を単色バージョンか多色バージョンかを選ぶことができます。背景色のルールと具体例は、選択したバージョンの方を選んでください。",
                "<b>2. 背景色のルール(多色バージョン)</b><br><br>"+
                        "強度7 :<font color='purple'> 紫色</font><br>" +
                        "強度6 :<font color='red'> 赤色</font><br>" +
                        "強度5 :<font color='#FFA500'> オレンジ色</font><br>" +
                        "強度4 :<font color='#FFD700'> 黄色</font><br>" +
                        "強度3 :<font color='green'> 黄緑色</font><br>" +
                        "強度2 :<font color='#006400'> 緑色</font><br>" +
                        "強度1 :<font color='#1E90FF'> 水色</font><br>" +
                        "強度その他 :<font color='gray'> 灰色</font>",
                "<b>3. 具体例</b><br><br>" +
                        "　例えば、前後日の強度を0と仮定し、月曜日のデフォルト強度を<b><font color='blue'>+3</font></b> と設定して" +
                        "月曜日に<font color='#FFD700'> 強度4</font>の予定を入れると、<b>強度が7</b> となり背景色が<font color='purple'>紫色</font>に変化します。<br><br>" +
                        "　逆に、同じ条件で月曜日のデフォルト強度を<b><font color='blue'>-3</font></b> と設定して" +
                        "月曜日に<font color='#FFD700'> 強度4</font>の予定を入れると、<b>強度が1</b> となり背景色が<font color='#1E90FF'>水色</font>となります。",

                "<b>2. 背景色のルール(単色バージョン)</b><br><br>" +
                        "強度7 :<font color='" + colorMap.get(7) + "'> 紫色</font><br>" +
                        "強度6 :<font color='" + colorMap.get(6) + "'> 赤色</font><br>" +
                        "強度5 :<font color='" + colorMap.get(5) + "'> オレンジ色</font><br>" +
                        "強度4 :<font color='" + colorMap.get(4) + "'> 黄色</font><br>" +
                        "強度3 :<font color='" + colorMap.get(3) + "'> 黄緑色</font><br>" +
                        "強度2 :<font color='" + colorMap.get(2) + "'> 緑色</font><br>" +
                        "強度1 :<font color='" + colorMap.get(1) + "'> 水色</font><br>" +
                        "強度その他 :<font color='grey'> 灰色</font>",
                "<b>3. 具体例</b><br><br>" +
                        "　例えば、前後日の強度を0と仮定し、月曜日のデフォルト強度を<b><font color='blue'>+3</font></b> と設定して" +
                        "月曜日に<font color='"+colorMap.get(4) + "'>強度4</font>の予定を入れると、<b>強度が7</b> となり背景色が<font color='" + colorMap.get(7) + "'>紫色</font>に変化します。<br><br>" +
                        "　逆に、同じ条件で月曜日のデフォルト強度を<b><font color='blue'>-3</font></b> と設定して" +
                        "月曜日に<font color='"+colorMap.get(4) + "'>強度4</font>の予定を入れると、<b>強度が1</b> となり背景色が<font color='" + colorMap.get(1) + "'>水色</font>となります。"
        );

        // ダイアログのビューをカスタマイズ
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_help, null);
        TextView textView = dialogView.findViewById(R.id.textView);
        textView.setText(HtmlCompat.fromHtml(helpPages.get(pageIndex), HtmlCompat.FROM_HTML_MODE_LEGACY));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton(pageIndex < helpPages.size() - 1 ? "次へ" : "閉じる", (dialog, which) -> {
                    if (pageIndex < helpPages.size() - 1) {
                        showHelpDialog(pageIndex + 1); // 次のページへ
                    }
                });

        // 2ページ目以降の場合、前のページに戻るボタンを追加
        if (pageIndex > 0) {
            builder.setNegativeButton("前へ", (dialog, which) -> {
                showHelpDialog(pageIndex - 1); // 前のページへ
            });
            if(pageIndex==1){
                builder.setNegativeButton("単色バージョン", (dialog, which) -> {
                    showHelpDialog(pageIndex+3); // 単色バージョンのページへ
                });
                builder.setPositiveButton("多色バージョン", (dialog, which) -> {
                    showHelpDialog(pageIndex+1); // 多色バージョンのページへ
                });
            }
            if(pageIndex==3){
                builder.setPositiveButton("閉じる", (dialog, which) -> {
                    dialog.dismiss(); // ダイアログを閉じる
                });
            }
        }


        builder.setCancelable(true); // 外をタップして閉じられるようにする
        builder.create().show();
    }


}