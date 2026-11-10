package lightning.events;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import lightning.LightningClient;
import lombok.Generated;

public class Listener {
    private static Method lookupMethod;
    private final Class subscriber;
    private Consumer consumer;
    private final int priority;

    public Listener(Class arg0, Object arg1, Method arg2) {
        this.subscriber = arg2.getParameters()[0].getType();
        this.priority = ((SubscribeEvent) arg2.getAnnotation(SubscribeEvent.class)).priority();
        try {
            Object var4 = MethodType.methodType(Void.TYPE, arg2.getParameters()[0].getType());
            Object var5 = MethodHandles.lookup().findVirtual(arg0, arg2.getName(), var4);
            Object var6 = MethodType.methodType(Consumer.class, arg0);
            this.consumer = LambdaMetafactory.metafactory(MethodHandles.lookup(), "accept", var6, MethodType.methodType(Void.TYPE, Object.class), var5, var4).getTarget().invoke(arg1);
        } catch (Throwable ex2) {
            var4 = ex2;
            LightningClient.LOGGER.error(("Fast event dispatch failed for " + arg0.getName() + "." + arg2.getName() + ", falling back to reflection"), var4);
            if (!(arg2.canAccess(arg1))) {
                arg2.setAccessible(true);
            }
            this.consumer = new Listener.ReflectiveFallbackConsumer(arg2, arg1);
        }
    }

    public void invoke(Object arg0) {
        this.consumer.accept(arg0);
    }

    @Generated
    public Class getSubscriber() {
        return this.subscriber;
    }

    @Generated
    public Consumer getConsumer() {
        return this.consumer;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }


    final class ReflectiveFallbackConsumer implements Consumer {
        private final Method method;
        private final Object target;

        private ReflectiveFallbackConsumer(Method arg0, Object arg1) {
            this.method = arg0;
            this.target = arg1;
        }

        public void accept(Object arg0) {
            try {
                var tmp0 = new Object[1];
                tmp0[0] = arg0;
                this.method.invoke(this.target, tmp0);
            } catch (Exception ex2) {
                Object var2 = ex2;
                LightningClient.LOGGER.error("The Event System threw an exception!", var2);
            }
        }

    }
}
