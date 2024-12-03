package io.github.shun.osugi.busible.model;

public class BusyData {
    private int busy;
    private int defaultBusy;
    private boolean gray;

    public BusyData() {
        this.setBusy(0);
        this.setDefaultBusy(0);
        this.setGray(true);
    }

    public void setDefaultBusy(int defaultBusy) {
        this.defaultBusy += defaultBusy;
    }
    public int getDefaultBusy() {
        return  this.defaultBusy;
    }

    public void setBusy(int busy) {
        this.busy += busy;
        if (this.busy > 7){
            this.busy = 7;
        }
    }
    public int getBusy() {
        return this.busy;
    }

    public void setGray(boolean gray){ this.gray = gray; }
    public boolean getGray(){ return this.gray; }
}