package io.github.shun.osugi.busible.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import android.text.method.LinkMovementMethod;
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

        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean wasChecked = preferences.getBoolean("colorTypeSwitch", false); // デフォルト値は false
        binding.colorTypeSwitch.setChecked(wasChecked);

        binding.colorTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveColorTypeSetting(isChecked);
        });

        // プライバシーポリシーへ飛ぶリンクを設定
        TextView linkText = findViewById(R.id.linkText);
        String textWithLink = "<a href='https://sites.google.com/ccmailg.meijo-u.ac.jp/privacy-policy'>こちら</a>";
        linkText.setText(HtmlCompat.fromHtml(textWithLink, HtmlCompat.FROM_HTML_MODE_LEGACY));
        linkText.setMovementMethod(LinkMovementMethod.getInstance());

        // 「？」ボタンのクリックイベントを設定
        ImageButton helpButton = findViewById(R.id.help_button);
        helpButton.setOnClickListener(view -> {
            showHelpDialog(0);
        });

        // 「？」ボタンのクリックイベントを設定
        ImageButton helpButton2 = findViewById(R.id.help_button2);
        helpButton2.setOnClickListener(view -> {
            showHelpDialog2(0);
        });
    }

    // ヘルプ情報を表示するメソッド
    private void showHelpDialog(int pageIndex) {
        // 色のマッピング
        Map<Integer, String> colorMap = new HashMap<>();

        for (int i = 0; i <= 7; i++) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(
                    getResources().getIdentifier("border" + i + (binding.colorTypeSwitch.isChecked() ? "_mono" : ""), "drawable", getPackageName()));

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
                        "　強度の値を設定することで、カレンダーの背景色が次のルールに基づいて変化します。<br>" +
                        "　強度は各曜日に対して <font color='blue'>-3 から +3</font> の範囲で設定可能です。",
                "<b>強度と背景色の対応</b><br><br>" +
                        "<font color='" + colorMap.get(7) + "'>強度7</font><br>" +
                        "<font color='" + colorMap.get(6) + "'>強度6</font><br>" +
                        "<font color='" + colorMap.get(5) + "'>強度5</font><br>" +
                        "<font color='" + colorMap.get(4) + "'>強度4</font><br>" +
                        "<font color='" + colorMap.get(3) + "'>強度3</font><br>" +
                        "<font color='" + colorMap.get(2) + "'>強度2</font><br>" +
                        "<font color='" + colorMap.get(1) + "'>強度1</font><br>" +
                        "<font color='grey'>強度その他</font>",
                "<b>具体例</b><br><br>" +
                        "　例えば、前後日の強度を0と仮定し、月曜日のデフォルト強度を<b><font color='blue'>+3</font></b> と設定して" +
                        "月曜日に<font color='"+colorMap.get(4) + "'>強度4</font>の予定を入れると、最終的な月曜日の強度は<font color='" + colorMap.get(7) + "'>強度7</font>になります。<br><br>" +
                        "　逆に、同じ条件で月曜日のデフォルト強度を<b><font color='blue'>-3</font></b> と設定して" +
                        "月曜日に<font color='"+colorMap.get(4) + "'>強度4</font>の予定を入れると、最終的な月曜日の強度は<font color='" + colorMap.get(1) + "'>強度1</font>になります。"
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
            if(pageIndex==3){
                builder.setPositiveButton("閉じる", (dialog, which) -> {
                    dialog.dismiss(); // ダイアログを閉じる
                });
            }
        }


        builder.setCancelable(true); // 外をタップして閉じられるようにする
        builder.create().show();
    }

    // ヘルプ情報を表示するメソッド
    private void showHelpDialog2(int pageIndex) {
        // 色のマッピング
        Map<Integer, String> colorMap = new HashMap<>();
        Map<Integer, String> colorMap_mono = new HashMap<>();

        for (int i = 0; i <= 7; i++) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(
                    getResources().getIdentifier("border" + i , "drawable", getPackageName()));

            if (drawable instanceof GradientDrawable gradientDrawable) {
                // Extracting color from GradientDrawable
                ColorStateList colorStateList = gradientDrawable.getColor();
                if (colorStateList != null) {
                    int color = colorStateList.getDefaultColor();
                    colorMap.put(i, String.format("#%06X", (0xFFFFFF & color))); // Convert to #RRGGBB
                }
            }

            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable_mono = getResources().getDrawable(
                    getResources().getIdentifier("border" + i + "_mono", "drawable", getPackageName()));

            if (drawable_mono instanceof GradientDrawable gradientDrawable) {
                // Extracting color from GradientDrawable
                ColorStateList colorStateList_mono = gradientDrawable.getColor();
                if (colorStateList_mono != null) {
                    int color = colorStateList_mono.getDefaultColor();
                    colorMap_mono.put(i, String.format("#%06X", (0xFFFFFF & color))); // Convert to #RRGGBB
                }
            }
        }

        // ヘルプ情報をリストで定義
        List<String> helpPages2 = Arrays.asList(
                "<b>単色モード</b><br><br>" +
                        "　単色モードを使用すると、カレンダーにおける予定の強度が赤色のグラデーションで表示されるようになります。<br>" +
                        "　強度の差が分かりづらい場合や、色彩の違いを認識しづらい場合は、単色モードをONにすると認識しやすくなる可能性があります。<br>",
                "<b>モードによる色の違い</b><br><br>" +
                        "強度7 : <font color='" + colorMap.get(7) + "'>単色モードON  </font><font color='" + colorMap_mono.get(7) + "'>単色モードOFF</font><br>" +
                        "強度6 : <font color='" + colorMap.get(6) + "'>単色モードON  </font><font color='" + colorMap_mono.get(6) + "'>単色モードOFF</font><br>" +
                        "強度5 : <font color='" + colorMap.get(5) + "'>単色モードON  </font><font color='" + colorMap_mono.get(5) + "'>単色モードOFF</font><br>" +
                        "強度4 : <font color='" + colorMap.get(4) + "'>単色モードON  </font><font color='" + colorMap_mono.get(4) + "'>単色モードOFF</font><br>" +
                        "強度3 : <font color='" + colorMap.get(3) + "'>単色モードON  </font><font color='" + colorMap_mono.get(3) + "'>単色モードOFF</font><br>" +
                        "強度2 : <font color='" + colorMap.get(2) + "'>単色モードON  </font><font color='" + colorMap_mono.get(2) + "'>単色モードOFF</font><br>" +
                        "強度1 : <font color='" + colorMap.get(1) + "'>単色モードON  </font><font color='" + colorMap_mono.get(1) + "'>単色モードOFF</font><br>" +
                        "強度その他 : <font color='grey'>モードに関わらず共通</font><br>" +
                        "</table>"
        );

        // ダイアログのビューをカスタマイズ
        View dialogView2 = LayoutInflater.from(this).inflate(R.layout.dialog_help, null);
        TextView textView2 = dialogView2.findViewById(R.id.textView);
        textView2.setText(HtmlCompat.fromHtml(helpPages2.get(pageIndex), HtmlCompat.FROM_HTML_MODE_LEGACY));

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setView(dialogView2)
                .setPositiveButton(pageIndex < helpPages2.size() - 1 ? "次へ" : "閉じる", (dialog, which) -> {
                    if (pageIndex < helpPages2.size() - 1) {
                        showHelpDialog2(pageIndex + 1); // 次のページへ
                    }
                });

        // 2ページ目以降の場合、前のページに戻るボタンを追加
        if (pageIndex > 0) {
            builder2.setNegativeButton("前へ", (dialog, which) -> {
                showHelpDialog(pageIndex - 1); // 前のページへ
            });
            builder2.setPositiveButton("閉じる", (dialog, which) -> {
                dialog.dismiss(); // ダイアログを閉じる
            });
        }


        builder2.setCancelable(true); // 外をタップして閉じられるようにする
        builder2.create().show();
    }

    private void saveColorTypeSetting(boolean isChecked) {
        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("colorTypeSwitch", isChecked);
        editor.apply(); // 非同期で保存
    }

}