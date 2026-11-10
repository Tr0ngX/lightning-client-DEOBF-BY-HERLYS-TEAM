package lightning.events;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class EventHandler {
    private final Map listeners;
    private final Map staticListeners;
    private final Map listenerMap;

    public EventHandler() {
        this.listeners = new ConcurrentHashMap();
        this.staticListeners = new ConcurrentHashMap();
        this.listenerMap = new ConcurrentHashMap();
    }

    public void subscribe(Object arg0) {
        Object var2 = getListeners(arg0.getClass(), arg0).iterator();
        while (var2.hasNext()) {
            Object var3 = ((Listener) var2.next());
            insert(((List) this.listenerMap.computeIfAbsent(var3.getSubscriber(), new EventHandler.1(this))), var3);
        }
    }

    public void unsubscribe(Object arg0) {
        Object var2 = getListeners(arg0.getClass(), arg0).iterator();
        while (var2.hasNext()) {
            Object var3 = ((Listener) var2.next());
            Object var4 = ((List) this.listenerMap.get(var3.getSubscriber()));
            if (var4 == null) {
                break label_0;
            }
            var4.remove(var3);
        }
    }

    public void post(Event arg0) {
        Object var2 = ((List) this.listenerMap.get(arg0.getClass()));
        if (var2 == null) return;
        Object var3 = var2.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Listener) var3.next());
            var4.invoke(arg0);
        }
    }

    public boolean isListening(Class arg0) {
        Object var2 = ((List) this.listenerMap.get(arg0));
        if (var2 == null) break label_0;
        return !(var2.isEmpty());
    }

    private List getListeners(Class arg0, Object arg1) {
        Object var3 = new EventHandler.2(this, arg0, arg1);
        if (arg1 == null) return ((List) this.staticListeners.computeIfAbsent(arg0, var3));
        Object var4 = this.listeners.keySet().iterator();
        while (var4.hasNext()) {
            Object var5 = var4.next();
            if (var5 == arg1) return ((List) this.listeners.get(arg1));
        }
        var4 = ((List) var3.apply(arg1));
        this.listeners.put(arg1, var4);
        return var4;
    }

    private void processListeners(List arg0, Class arg1, Object arg2) {
        Object var4 = arg1.getDeclaredMethods();
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            if (!(isValid(var7))) {
            } else {
                arg0.add(new Listener(arg1, arg2, var7));
            }
            var6++;
        }
        if (arg1.getSuperclass() != null) {
            processListeners(arg0, arg1.getSuperclass(), arg2);
        }
    }

    private boolean isValid(Method arg0) {
        if (!(arg0.isAnnotationPresent(SubscribeEvent.class))) return false;
        if (arg0.getReturnType() != Void.TYPE) return false;
        if (arg0.getParameterCount() != 1) return false;
        return !(arg0.getParameters()[0].getType().isPrimitive());
    }

    private void insert(List arg0, Listener arg1) {
        int var3 = 0;
        while (var3 < arg0.size() && arg1.getPriority() <= ((Listener) arg0.get(var3)).getPriority()) {
            var3++;
        }
        arg0.add(var3, arg1);
    }


    class 1 implements Function {
        1(EventHandler arg0) {
            Objects.requireNonNull(arg0);
        }

        public List apply(Class arg0) {
            return new CopyOnWriteArrayList();
        }

    }

    class 2 implements Function {

        2(EventHandler arg0, Class arg1, Object arg2) {
            this.val$klass = arg1;
            this.val$object = arg2;
            Objects.requireNonNull(arg0);
            this.this$0 = arg0;
        }

        public List apply(Object arg0) {
            Object var2 = new CopyOnWriteArrayList();
            this.this$0.processListeners(var2, this.val$klass, this.val$object);
            return var2;
        }

    }
}
