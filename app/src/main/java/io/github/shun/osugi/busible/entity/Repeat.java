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
    private int repeat; //繰り返し



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

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

}
