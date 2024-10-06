package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

public class BusyData {
    private int day0Busy;   //1日前のBusy
    private int day1Busy;   //当日のBusy

    public BusyData(int day0Busy, int day1Busy) {
        this.setDay0Busy(day0Busy);
        this.setDay1Busy(day1Busy);
    }

    public int getDay0Busy() {
        return this.day0Busy;
    }

    public void setDay0Busy(int day0Busy) {
        this.day0Busy = day0Busy;
    }

    public int getDay1Busy() {
        return this.day1Busy;
    }

    public void setDay1Busy(int day1Busy) {
        this.day1Busy = day1Busy;
    }

    public void shiftBusy(){
        this.setDay0Busy(this.getDay1Busy());
    }
}