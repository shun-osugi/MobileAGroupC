package io.github.shun.osugi.busible.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "repeat_exclusion",
        foreignKeys = @ForeignKey(
                entity = Repeat.class,
                parentColumns = "id",
                childColumns = "repeatId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "repeatId")
)
public class RepeatExclusion {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int repeatId; // Repeat テーブルの外部キー
    private String date; // 除外日（例: "2025-02-21"）

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRepeatId() {
        return repeatId;
    }

    public void setRepeatId(int repeatId) {
        this.repeatId = repeatId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
