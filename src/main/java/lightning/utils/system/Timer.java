package lightning.utils.system;

public class Timer {
    private long startTime;

    public Timer() {
        this.startTime = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(Number arg0) {
        return (System.currentTimeMillis() - this.startTime) >= arg0.longValue();
    }

    public long timeElapsed() {
        return (System.currentTimeMillis() - this.startTime);
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }

}
