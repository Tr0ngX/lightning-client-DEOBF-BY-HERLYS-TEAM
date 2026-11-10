package lightning.pingbypass;

public class ProtocolTransitionTracker {
    private static volatile long lastTransitionAt;

    public static void mark() {
        lastTransitionAt = System.currentTimeMillis();
    }

    public static long lastTransitionAt() {
        return lastTransitionAt;
    }

    static {
        lastTransitionAt = 0L;
    }

}
