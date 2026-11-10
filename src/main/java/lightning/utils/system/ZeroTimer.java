package lightning.utils.system;

public class ZeroTimer {
    private long startTime;

    public ZeroTimer() {
        this.startTime = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long arg0) {
        if (this.startTime == 0L) return true;
        return (System.currentTimeMillis() - this.startTime) >= arg0;
    }

    public void zero() {
        this.startTime = 0L;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }

}
