package io.github.shun.osugi.busible.entity;

// 必要なインポート文
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedule") // テーブル名を "schedule" に設定
public class Schedule {
    @PrimaryKey(autoGenerate = true) // 主キーを自動生成
    private int id;

    private String title; // 予定のタイトル
    private String memo; // 詳細な説明
    private String startTime; // 開始時間
    private String endTime; // 終了時間
    private String color; // 色
    private String repeat; //繰り返し

    // コンストラクタ（必要に応じて追加）
    public Schedule() {
        // デフォルトコンストラクタ
    }

    // Getter と Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    // デバッグ用の toString メソッド
    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", memo='" + memo + '\'' +
                ", startTime=" + startTime + '\'' +
                ", endTime=" + endTime + '\'' +
                ", color='" + color + '\'' +
                ", repeat='" + repeat + '\'' +
                '}';
    }
}
