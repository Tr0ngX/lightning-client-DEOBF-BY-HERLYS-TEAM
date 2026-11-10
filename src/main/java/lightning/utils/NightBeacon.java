package lightning.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class NightBeacon {
    public static final int MARK = 128;
    private static final boolean ENABLED;
    private static volatile boolean broken;
    private static Constructor pktCtor;
    private static Constructor infoCtor;
    private static Method pktGetter;
    private static int modelIdx;
    private static Method sendMethod;
    private static Field accessorField;
    private static Method getEntityData;
    private static Method dataGet;
    private static boolean inited;
    private static boolean readBroken;

    public static void pre(Object arg0, Object arg1, Object arg2, boolean arg3, Object arg4) {
        if (!(!ENABLED) && !broken) {
            if (arg1 != null) break label_1;
        }
        return;
        try {
            label_1:
            if (!"ServerboundClientInformationPacket".equals(arg1.getClass().getSimpleName())) {
                return;
                try {
                }
                Object var5 = rewrite(arg1);
                if (var5 != null) {
                    if (var5 == arg1) {
                    }
                    return;
                    try {
                    }
                    if (sendMethod != null) break label_10;
                    Object var6 = arg0.getClass();
                    while (var6 != null && sendMethod == null) {
                        Object var7 = var6.getDeclaredMethods();
                        int var8 = var7.length;
                        int var9 = 0;
                        while (var9 < var8) {
                            Object var10 = var7[var9];
                            Object var11 = var10.getParameterTypes();
                            if (var10.getName().equals("send") && var11.length == 3 && var11[2] == Boolean.TYPE) {
                                var10.setAccessible(true);
                                sendMethod = var10;
                                break label_9;
                            }
                            var9++;
                        }
                        label_9:
                        var6 = var6.getSuperclass();
                    }
                    label_10:
                    if (sendMethod == null) {
                        return;
                        try {
                        }
                        var tmp0 = new Object[3];
                        tmp0[0] = var5;
                        tmp0[1] = arg2;
                        tmp0[2] = arg3;
                        sendMethod.invoke(arg0, tmp0);
                        arg4.getClass().getMethod("cancel", new Class[0]).invoke(arg4, new Object[0]);
                    } catch (Throwable ex14) {
                        var5 = ex14;
                        broken = true;
                    }
                }
            }
        }
    }

    private static Object rewrite(Object arg0) throws Exception {
        Object var1 = arg0.getClass();
        if (pktCtor == null) {
            Object var2 = var1.getRecordComponents();
            if ((var2 == null) || (var2.length != 1)) {
                throw new IllegalStateException();
            }
            pktGetter = var2[0].getAccessor();
            pktGetter.setAccessible(true);
            var tmp0 = new Class[1];
            tmp0[0] = var2[0].getType();
            pktCtor = var1.getDeclaredConstructor(tmp0);
            pktCtor.setAccessible(true);
            Object var3 = var2[0].getType();
            Object var4 = var3.getRecordComponents();
            Object var5 = new Class[var4.length];
            int var6 = 0;
            while (var6 < var4.length) {
                var5[var6] = var4[var6].getType();
                if (var4[var6].getName().toLowerCase().contains("modelcustom")) {
                    modelIdx = var6;
                }
                var6++;
            }
            if ((modelIdx < 0) || (var5[modelIdx] != Integer.TYPE)) {
                throw new IllegalStateException();
            }
            infoCtor = var3.getDeclaredConstructor(var5);
            infoCtor.setAccessible(true);
        }
        var2 = pktGetter.invoke(arg0, new Object[0]);
        var3 = var2.getClass().getRecordComponents();
        var4 = new Object[var3.length];
        var5 = 0;
        while (var5 < var3.length) {
            var6 = var3[var5].getAccessor();
            var6.setAccessible(true);
            var4[var5] = var6.invoke(var2, new Object[0]);
            var5++;
        }
        var5 = ((Integer) var4[modelIdx]).intValue();
        if ((var5 & 128) != 0) return arg0;
        var4[modelIdx] = (var5 | 128);
        var tmp1 = new Object[1];
        tmp1[0] = infoCtor.newInstance(var4);
        return pktCtor.newInstance(tmp1);
    }

    public static boolean or(Object arg0, boolean arg1) {
        if (!arg1) {
            if (isMarked(arg0)) {
            }
        } else {
        }
        return false;
    }

    public static boolean isMarked(Object arg0) {
        if (!(!ENABLED) && !readBroken) {
            if (arg0 != null) break label_1;
        }
        return false;
        try {
            label_1:
            if (inited) break label_14;
            Object var1 = arg0.getClass();
            while (var1 != null && accessorField == null) {
                Object var2 = var1.getDeclaredFields();
                int var3 = var2.length;
                int var4 = 0;
                while (var4 < var3) {
                    Object var5 = var2[var4];
                    if (Modifier.isStatic(var5.getModifiers()) && var5.getName().equals("DATA_PLAYER_MODE_CUSTOMISATION")) {
                        var5.setAccessible(true);
                        accessorField = var5;
                        break label_5;
                    }
                    var4++;
                }
                label_5:
                var1 = var1.getSuperclass();
            }
            var1 = arg0.getClass();
            while (var1 != null && getEntityData == null) {
                var2 = var1.getDeclaredMethods();
                var3 = var2.length;
                var4 = 0;
                while (var4 < var3) {
                    var5 = var2[var4];
                    if (var5.getName().equals("getEntityData") && !(var5.getParameterCount())) {
                        var5.setAccessible(true);
                        getEntityData = var5;
                        break label_10;
                    }
                    var4++;
                }
                label_10:
                var1 = var1.getSuperclass();
            }
            inited = true;
            if (accessorField != null) {
                if (getEntityData != null) break label_14;
            }
            readBroken = true;
            return false;
            try {
                label_14:
                var1 = getEntityData.invoke(arg0, new Object[0]);
                if (dataGet != null) break label_19;
                var2 = var1.getClass().getMethods();
                var3 = var2.length;
                var4 = 0;
                while (var4 < var3) {
                    var5 = var2[var4];
                    if (var5.getName().equals("get") && var5.getParameterCount() == 1) {
                        dataGet = var5;
                        break label_17;
                    }
                    var4++;
                }
                label_17:
                if (dataGet != null) break label_19;
                readBroken = true;
                return false;
                try {
                    label_19:
                    var tmp0 = new Object[1];
                    tmp0[0] = accessorField.get(null);
                    var2 = dataGet.invoke(var1, tmp0);
                    if (!((var2 instanceof Byte))) break label_20;
                    var3 = ((Byte) var2);
                    return (var3.byteValue() & 128) != 0;
                } catch (Throwable ex22) {
                    var1 = ex22;
                    readBroken = true;
                    return false;
                }
            }
        }
    }

    static {
        ENABLED = !"false".equalsIgnoreCase(System.getProperty("night.beacon", "true"));
        broken = false;
        modelIdx = -1;
    }

}
