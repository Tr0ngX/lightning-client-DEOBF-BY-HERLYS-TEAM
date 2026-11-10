package lightning.utils.graphics;

public final class LoadingClock {
    private static final long STALL_MS = 150L;
    private static final long NEW_SESSION_GAP_MS = 3000L;
    private static final long HOLD_SMOOTH_MS = 800L;
    private static final long GIVE_UP_MS = 30000L;
    private static long last;
    private static long acc;
    private static long smooth;
    private static long sessionStart;

    public static synchronized long tick(long arg0) {
        if (last < 0L) break label_1;
        if ((arg0 - last) > 3000L) {
            label_1:
            acc = 0L;
            smooth = 0L;
            last = arg0;
            sessionStart = arg0;
            return 0L;
        }
        long var2 = (arg0 - last);
        if (var2 < 0L) {
            var2 = 0L;
        }
        last = arg0;
        if (var2 > 150L) {
            smooth = 0L;
        } else {
            acc = (acc + var2);
            smooth = (smooth + var2);
        }
        return acc;
    }

    public static synchronized long elapsed() {
        if (sessionStart >= 0L && (last - sessionStart) > 30000L) return 2305843009213693951L;
        return (smooth >= 800L ? LoadingClock.acc : 0L);
    }

    static {
        last = -1L;
        acc = 0L;
        smooth = 0L;
        sessionStart = -1L;
    }

}
