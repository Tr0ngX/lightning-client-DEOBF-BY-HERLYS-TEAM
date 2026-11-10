package lightning.utils.system;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadExecutor {
    private static final Map RUNNING;

    public static void execute(Runnable arg0) {
        Object var2 = arg0.getClass().getEnclosingClass();
        if (var2 == null) {
            var2 = arg0.getClass();
        }
        var tmp0 = ((Thread) RUNNING.get(var2));
        Object var1 = tmp0;
        if (tmp0 != null && var1.isAlive()) return;
        Object var3 = var2;
        Object var4 = new ThreadExecutor.OneTimeThread(() -> {
    arg0.run();
    RUNNING.remove(arg1);
    break label_3;
    Object var2 = null;
    RUNNING.remove(arg1);
    throw var2;
    label_3:
});
        RUNNING.put(var3, var4);
        var4.start();
    }

    static {
        RUNNING = new ConcurrentHashMap();
    }


    class OneTimeThread extends Thread {
        private final Runnable runnable;

        public OneTimeThread(Runnable arg0) {
            this.runnable = arg0;
        }

        public void run() {
            this.runnable.run();
        }

    }
}
