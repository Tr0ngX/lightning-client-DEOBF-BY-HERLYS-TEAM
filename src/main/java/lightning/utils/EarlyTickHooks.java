package lightning.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EarlyTickHooks {
    private static final List callbacks;

    public static void register(Runnable arg0) {
        callbacks.add(arg0);
    }

    public static void unregister(Runnable arg0) {
        callbacks.remove(arg0);
    }

    public static void dispatch() {
        Object var0 = callbacks.iterator();
        while (var0.hasNext()) {
            Object var1 = ((Runnable) var0.next());
            var1.run();
        }
    }

    static {
        callbacks = new CopyOnWriteArrayList();
    }

}
