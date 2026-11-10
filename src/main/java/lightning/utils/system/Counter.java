package lightning.utils.system;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Counter {
    private final ConcurrentLinkedQueue count;

    public Counter() {
        this.count = new ConcurrentLinkedQueue();
    }

    public void increment() {
        this.count.add((System.currentTimeMillis() + 1000L));
    }

    public int getCount() {
        this.count.removeIf((arg0) -> arg0.longValue() < System.currentTimeMillis());
        return this.count.size();
    }

    public void reset() {
        this.count.clear();
    }

}
