package io.github.shun.osugi.busible.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "date")
public class Date {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int year;
    private int month;
    private int day;

    // GetterとSetter
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

    // toString()メソッドのオーバーライド
    @Override
    public String toString() {
        return "Date{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}
