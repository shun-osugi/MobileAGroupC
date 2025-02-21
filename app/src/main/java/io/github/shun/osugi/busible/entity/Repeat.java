package io.github.shun.osugi.busible.entity;

// 必要なインポート文
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "repeat",
        foreignKeys = {
                @ForeignKey(
                        entity = Date.class,
                        parentColumns = "id",
                        childColumns = "dateId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Schedule.class,
                        parentColumns = "id",
                        childColumns = "scheduleId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"dateId"}),  // インデックス追加
                @Index(value = {"scheduleId"})  // インデックス追加
        }
)
public class Repeat {
    @PrimaryKey(autoGenerate = true) // 主キーを自動生成
    private int id; // 一意のID

    private int dateId; // 外部キー(日付)
    private int scheduleId; // 外部キー(予定)
    private String repeat; //繰り返し
    private int week;//週
    private int DoW;//曜日(Day of Week の略)



    // Getter と Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDateId() {
        return dateId;
    }

    public void setDateId(int dateId) {
        this.dateId = dateId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getDoW() {
        return DoW;
    }

    public void setDoW(int DoW) {
        this.DoW = DoW;
    }

}
