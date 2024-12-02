package io.github.shun.osugi.busible.entity;
// 必要なインポート文
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "date") // テーブル名を指定
public class Date {
    @PrimaryKey(autoGenerate = true) // 主キーを設定
    private int id;

    private int year;

    private int month;

    private int day;

    // 必要なGetterとSetter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}


