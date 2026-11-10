package lightning.modules.impl.player;

import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public final class AutoShopSync {
    private static final int CONFIRM_SLOT = 23;
    private static final long MAX_WAIT_NANOS = 1000000000L;
    private static Object lastMenu;
    private static int lastStateId;
    private static boolean armed;
    private static long armedAt;
    private static long lastActive;
    private static final long GRACE_NANOS = 1500000000L;

    private static int stateId(AbstractContainerMenu arg0) {
        try {
            return arg0.getStateId();
        } catch (Throwable ex2) {
            Object var1 = ex2;
            return 0;
        }
    }

    public static void click(int arg0, int arg1, ContainerInput arg2) {
        lastActive = System.nanoTime();
        try {
            Object var3 = Minecraft.getInstance();
            if (arg0 != 23 && var3 != null && var3.player != null && var3.player.containerMenu != null) {
                lastMenu = var3.player.containerMenu;
                lastStateId = stateId(var3.player.containerMenu);
                armedAt = System.nanoTime();
                armed = true;
            } else {
                armed = false;
            }
        } catch (Throwable ex9) {
            var3 = ex9;
            armed = false;
        }
        InventoryUtils.click(arg0, arg1, arg2);
    }

    public static int delay(Number arg0) {
        int var1 = arg0.intValue();
        lastActive = System.nanoTime();
        if (!armed) {
            return var1;
            try {
            }
            Object var2 = Minecraft.getInstance();
            if (var2 != null) {
                if (var2.player == null) {
                }
                armed = false;
                return var1;
                try {
                }
                Object var3 = var2.player.containerMenu;
                if (var3 != lastMenu) break label_12;
                if (var3 != null) {
                    if (stateId(var3) != lastStateId) break label_12;
                }
                if ((System.nanoTime() - armedAt) > 1000000000L) {
                    label_12:
                    armed = false;
                    return var1;
                    try {
                    }
                    return 2147483647;
                } catch (Throwable ex17) {
                    var2 = ex17;
                    armed = false;
                    return var1;
                }
            }
        }
    }

    public static AutoShopModule.ScreenDecision classify(AutoShopModule arg0, Screen arg1) {
        Object var2 = arg0.classifyScreen(arg1);
        if (var2 == AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED) {
            return var2;
            try {
            }
            if (lastActive != 0L && arg0.silent.getValue() && (arg1 instanceof AbstractContainerScreen) && !(arg1 instanceof InventoryScreen) && (System.nanoTime() - lastActive) < 1500000000L) {
                Object var3 = Minecraft.getInstance();
                if (var3 != null && var3.player != null) {
                    var3.player.closeContainer();
                }
                return AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED;
            }
        } catch (Throwable ex12) {
            var3 = ex12;
        }
        return var2;
    }

}
