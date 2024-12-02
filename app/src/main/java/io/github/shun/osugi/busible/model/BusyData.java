package io.github.shun.osugi.busible.model;

public class BusyData {
    private int day0Busy;   //1日前のBusy
    private int day1Busy;   //当日のBusy
    private boolean gray;

    public BusyData() {
        this.setDay0Busy(0);
        this.setDay1Busy(0);
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

    public void setGray(boolean gray){ this.gray = gray; }
    public boolean getGray(){ return this.gray; }
}