package lightning.utils.minecraft;

import java.util.Optional;

public final class BaritoneUtils {
    public static Object getPrimaryBaritone() {
        try {
            Object var0 = Class.forName("baritone.api.BaritoneAPI");
            Object var1 = var0.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
            return var1.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(var1, new Object[0]);
        } catch (Throwable ex2) {
            var0 = ex2;
            return null;
        }
    }

    public static boolean isAvailable() {
        return getPrimaryBaritone() != null;
    }

    public static boolean isActive() {
        Object var0 = getPrimaryBaritone();
        if (var0 == null) {
            return false;
            try {
            }
            Object var1 = var0.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(var0, new Object[0]);
            if (((Boolean) var1.getClass().getMethod("isPathing", new Class[0]).invoke(var1, new Object[0])).booleanValue()) return true;
        } catch (Throwable ex3) {
            var1 = ex3;
            try {
            }
            var1 = var0.getClass().getMethod("getCustomGoalProcess", new Class[0]).invoke(var0, new Object[0]);
            if (((Boolean) var1.getClass().getMethod("isActive", new Class[0]).invoke(var1, new Object[0])).booleanValue()) return true;
        } catch (Throwable ex7) {
            var1 = ex7;
            try {
            }
            var1 = var0.getClass().getMethod("getElytraProcess", new Class[0]).invoke(var0, new Object[0]);
            if (var1 != null && ((Boolean) var1.getClass().getMethod("isActive", new Class[0]).invoke(var1, new Object[0])).booleanValue()) return true;
        } catch (Throwable ex11) {
            var1 = ex11;
            try {
            }
            Object var2 = var0.getClass().getMethod("getPathingControlManager", new Class[0]).invoke(var0, new Object[0]);
            Object var3 = ((Optional) var2.getClass().getMethod("mostRecentInControl", new Class[0]).invoke(var2, new Object[0]));
            if (var3.isPresent()) {
                var tmp0 = var3.get();
                var1 = tmp0;
                if (((Boolean) tmp0.getClass().getMethod("isActive", new Class[0]).invoke(var1, new Object[0])).booleanValue()) return true;
            }
        } catch (Throwable ex15) {
            var1 = ex15;
        }
        return false;
    }

}
