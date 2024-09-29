package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;

public class CalendarCell {
    private int day0Busy;
    private int day1Busy;

    public CalendarCell(int day0Busy, int day1Busy) {
        this.setday0Busy(day0Busy);   //1日前のBusy
        this.setday1Busy(day1Busy);   //当日のBusy
    }

    public int getday0Busy() {
        return this.day0Busy;
    }

    public void setday0Busy(int day0Busy) {
        this.day0Busy = day0Busy;
    }

    public int getday1Busy() {
        return this.day1Busy;
    }

    public void setday1Busy(int day1Busy) {
        this.day1Busy = day1Busy;
    }

    public void shiftBusy(){
        this.setday0Busy(this.getday1Busy());
    }
}

