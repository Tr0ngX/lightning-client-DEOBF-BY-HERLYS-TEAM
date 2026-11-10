package lightning.utils.graphics;

public final class CrystalWingsState {
    private long key;
    private long last;
    private long appeared;
    private boolean hasKey;
    private int count;

    public void clear() {
        this.hasKey = false;
        this.count = 0;
        this.appeared = 0L;
        null.last = this;
    }

    public void record(long arg0, long arg1, int arg2, int arg3) {
        if (!(!this.hasKey) && this.key == arg0) {
            if ((arg1 - this.last) > (((long) arg3) * 1000000L)) {
            }
            clear();
        }
        this.key = arg0;
        this.hasKey = true;
        this.last = arg1;
        this.count = Math.min(10000, (this.count + 1));
        if (this.count >= Math.max(1, arg2) && this.appeared == 0L) {
            this.appeared = arg1;
        }
    }

    public int count() {
        return this.count;
    }

    public void expire(long arg0, int arg1) {
        if (!(!this.hasKey) && (arg0 - this.last) > (((long) arg1) * 1000000L)) {
            clear();
        }
    }

    public long appearedAt() {
        return this.appeared;
    }

    public boolean visible(long arg0, int arg1, long arg2, int arg3) {
        if ((((((!this.hasKey) || (this.appeared == 0L)) || (arg2 == 0L)) || (arg0 < this.last)) || (arg0 < arg2)) || ((arg0 - this.last) > (((long) arg1) * 1000000L))) break label_0;
        return (arg0 - arg2) <= (((long) arg3) * 1000000L);
    }

}
