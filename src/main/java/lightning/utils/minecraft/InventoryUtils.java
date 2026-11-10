package lightning.utils.minecraft;

import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lightning.LightningClient;
import lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CSlotSyncPacket;
import lightning.pingbypass.protocol.packets.S2CWindowClickPacket;
import lightning.utils.IMinecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class InventoryUtils implements IMinecraft {
    public static String[] SWITCH_MODES;
    public static String[] SWAP_MODES;
    public static int HOTBAR_START;
    public static int HOTBAR_END;
    public static int INVENTORY_START;
    public static int INVENTORY_END;
    private static final ArrayDeque STACK;
    private static final Set LONG_HOLD;
    private static final ThreadLocal PROGRAMMATIC_SWITCH;
    private static final long STALE_FRAME_MS = 30000L;

    public static void markLongHold(int arg0) {
        LONG_HOLD.add(arg0);
    }

    public static void clearLongHold(int arg0) {
        LONG_HOLD.remove(arg0);
    }

    public static boolean hasActiveSilentSwitch() {
        Object var0 = STACK;
        Object var1 = var0;
        // synchronized (var0)
        Object var2 = STACK.iterator();
        label_1:
        if (var2.hasNext()) {
            Object var3 = ((InventoryUtils.Frame) var2.next());
            if (LONG_HOLD.contains(var3.key())) {
                break label_1;
            }
            return true;
        }
        break label_8;
        Object var4 = null;
        throw var4;
        label_8:
        return false;
    }

    public static boolean isProgrammaticSwitching() {
        return ((Boolean) PROGRAMMATIC_SWITCH.get()).booleanValue();
    }

    private static int restingSlot() {
        if (mc.player == null) {
        } else {
        }
        return mc.player.getInventory().getSelectedSlot();
    }

    public static void tickPendingRestore() {
        if (mc.player == null) {
            Object var1 = STACK;
            Object var2 = var1;
            // synchronized (var1)
            STACK.clear();
            break label_4;
            Object var3 = null;
            throw var3;
            label_4:
            LONG_HOLD.clear();
            return;
        }
        if (!(mc.options.keyUse.isDown())) {
            if (mc.player.isUsingItem()) {
            }
        } else {
        }
        int var0 = 0;
        var1 = 0;
        if (!var0) {
            if (mc.options.keyAttack.isDown()) {
            }
            flushPendingRestores();
        }
        if (!(!var0)) {
            send(mc.player.getInventory().getSelectedSlot());
        }
        var2 = STACK;
        var3 = var2;
        // synchronized (var2)
        IF (!(STACK.removeIf((arg0) -> {
    if (LONG_HOLD.contains(arg0.key())) break label_0;
    return (System.currentTimeMillis() - arg0.time()) >= 30000L;
}))) break label_13;
        settle();
        label_13:
        if (!var0) {
            settle();
        }
        break label_18;
        Object var4 = 1;
        throw var4;
        label_18:
    }

    public static void flushPendingRestores() {
        Object var0 = STACK;
        Object var1 = var0;
        // synchronized (var0)
        settle();
        break label_4;
        Object var2 = null;
        throw var2;
        label_4:
    }

    public static void releaseAllBorrows() {
        Object var0 = STACK;
        Object var1 = var0;
        // synchronized (var0)
        STACK.removeIf((arg0) -> !(LONG_HOLD.contains(arg0.key())));
        settle();
        break label_4;
        Object var2 = null;
        throw var2;
        label_4:
    }

    private static void settle() {
        Object var0 = ((InventoryUtils.Frame) STACK.peek());
        if (var0 != null) {
        } else {
        }
        send(restingSlot());
    }

    private static void send(int arg0) {
        if ((arg0 < 0) || (mc.getConnection() == null)) {
            return;
        }
        if (arg0 == LightningClient.POSITION_MANAGER.getServerSlot()) return;
        int var1 = ((Boolean) PROGRAMMATIC_SWITCH.get()).booleanValue();
        PROGRAMMATIC_SWITCH.set(true);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(arg0));
        PROGRAMMATIC_SWITCH.set(var1);
        break label_6;
        Object var2 = null;
        PROGRAMMATIC_SWITCH.set(var1);
        throw var2;
        label_6:
    }

    private static void push(int arg0, int arg1) {
        Object var2 = STACK;
        Object var3 = var2;
        // synchronized (var2)
        STACK.push(new InventoryUtils.Frame(arg0, arg1, System.currentTimeMillis()));
        send(arg1);
        break label_4;
        Object var4 = null;
        throw var4;
        label_4:
    }

    private static void release(int arg0) {
        Object var1 = STACK;
        Object var2 = var1;
        // synchronized (var1)
        int var3 = 0;
        Object var4 = STACK.iterator();
        label_1:
        if (!(var4.hasNext())) break label_3;
        if (((InventoryUtils.Frame) var4.next()).key() != arg0) {
            break label_1;
        }
        var4.remove();
        var3 = 1;
        break label_3;
        label_3:
        if (!var3) return;
        Object var5 = ((InventoryUtils.Frame) STACK.peek());
        if (var5 != null) {
            send(var5.held());
            return;
        }
        settle();
        break label_11;
        Object var6 = null;
        throw var6;
        label_11:
    }

    public static boolean switchSlot(String arg0, int arg1, int arg2) {
        if (arg0.equalsIgnoreCase("None")) return true;
        if (arg1 == -1) break label_1;
        if (arg2 == -1) {
            label_1:
            return false;
        }
        PROGRAMMATIC_SWITCH.set(true);
        if (!(mc.isSameThread()) && !(arg0.equalsIgnoreCase("Silent"))) {
            if (arg1 > HOTBAR_END) {
                int var3 = 0;
                int var4 = var3;
                PROGRAMMATIC_SWITCH.set(false);
                return var4;
            }
            arg0 = "Silent";
        }
        var3 = arg0;
        var4 = -1;
        switch (var3.hashCode()) {
            case -1955878649 -> { break label_7; }
            case -1818460043 -> { break label_8; }
            case -13898395 -> { break label_9; }
            case 759863996 -> { break label_10; }
            default -> { break label_11; }
        }
        label_7:
        if (!(var3.equals("Normal"))) break label_11;
        var4 = 0;
        break label_11;
        label_8:
        if (!(var3.equals("Silent"))) break label_11;
        var4 = 1;
        break label_11;
        label_9:
        if (!(var3.equals("AltPickup"))) break label_11;
        var4 = 2;
        break label_11;
        label_10:
        if (!(var3.equals("AltSwap"))) break label_11;
        var4 = 3;
        label_11:
        switch (var4) {
            case 0 -> { break label_12; }
            case 1 -> { break label_13; }
            case 2 -> { break label_14; }
            case 3 -> { break label_14; }
            default -> { break label_18; }
        }
        label_12:
        mc.player.getInventory().setSelectedSlot(arg1);
        ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
        syncSlotToClientIfProxy(arg1);
        break label_18;
        label_13:
        push(arg1, arg1);
        break label_18;
        label_14:
        if (arg1 <= HOTBAR_END) {
            push(arg1, arg1);
            break label_18;
        }
        swap((arg0.equals("AltPickup") ? "Pickup" : "Swap"), arg1, arg2);
        push(arg1, arg2);
        label_18:
        var3 = 1;
        var4 = var3;
        PROGRAMMATIC_SWITCH.set(false);
        return var4;
        Object var5 = null;
        PROGRAMMATIC_SWITCH.set(false);
        throw var5;
    }

    public static void switchBackNormal(int arg0) {
        if ((arg0 == -1) || (arg0 == LightningClient.POSITION_MANAGER.getServerSlot())) {
            return;
        }
        PROGRAMMATIC_SWITCH.set(true);
        mc.player.getInventory().setSelectedSlot(arg0);
        ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
        syncSlotToClientIfProxy(arg0);
        PROGRAMMATIC_SWITCH.set(false);
        break label_5;
        Object var1 = null;
        PROGRAMMATIC_SWITCH.set(false);
        throw var1;
        label_5:
    }

    private static void syncSlotToClientIfProxy(int arg0) {
        if ((LightningClient.PINGBYPASS_CONFIG == null) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) {
            return;
        }
        if ((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PROXY_SERVER == null)) {
            return;
        }
        Object var1 = new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CSlotSyncPacket(arg0)));
        Object var2 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Connection) var2.next());
            if (!(var3.isConnected())) {
                break label_4;
            }
            var3.send(var1);
        }
    }

    public static void switchBack(String arg0, int arg1, int arg2) {
        if (arg0.equalsIgnoreCase("None")) return;
        if (arg2 == -1) return;
        PROGRAMMATIC_SWITCH.set(true);
        if (!(arg0.equals("AltPickup"))) {
            if (!(arg0.equals("AltSwap"))) break label_6;
        }
        if ((arg1 <= HOTBAR_END) || (!(mc.isSameThread()))) break label_6;
        swap((arg0.equals("AltPickup") ? "Pickup" : "Swap"), arg1, arg2);
        label_6:
        release(arg1);
        PROGRAMMATIC_SWITCH.set(false);
        break label_9;
        Object var3 = null;
        PROGRAMMATIC_SWITCH.set(false);
        throw var3;
        label_9:
    }

    public static void swapEquipment(int arg0, int arg1) {
        if (arg0 >= 0 && arg0 <= 8) {
            click(arg1, arg0, ContainerInput.SWAP);
            return;
        }
        click(indexToSlot(arg0), 0, ContainerInput.PICKUP);
        click(arg1, 0, ContainerInput.PICKUP);
        click(indexToSlot(arg0), 0, ContainerInput.PICKUP);
    }

    public static void swap(String arg0, int arg1, int arg2) {
        Object var3 = arg0;
        int var4 = -1;
        switch (var3.hashCode()) {
            case -1904609636 -> { break label_0; }
            case 2590131 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        if (!(var3.equals("Pickup"))) break label_2;
        var4 = 0;
        break label_2;
        label_1:
        if (!(var3.equals("Swap"))) break label_2;
        var4 = 1;
        label_2:
        switch (var4) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            default -> { break label_5; }
        }
        label_3:
        click(indexToSlot(arg1), 0, ContainerInput.PICKUP);
        click(indexToSlot(arg2), 0, ContainerInput.PICKUP);
        click(indexToSlot(arg1), 0, ContainerInput.PICKUP);
        break label_5;
        label_4:
        click(indexToSlot(arg1), arg2, ContainerInput.SWAP);
        label_5:
    }

    public static void click(int arg0, int arg1, ContainerInput arg2) {
        mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, arg0, arg1, arg2, mc.player);
        broadcastClickToClientIfProxy(arg0, arg1, arg2);
    }

    private static void broadcastClickToClientIfProxy(int arg0, int arg1, ContainerInput arg2) {
        if ((LightningClient.PINGBYPASS_CONFIG == null) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) {
            return;
        }
        if ((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PROXY_SERVER == null)) {
            return;
        }
        Object var3 = new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CWindowClickPacket(mc.player.containerMenu.containerId, arg0, arg1, arg2)));
        Object var4 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Connection) var4.next());
            if (!(var5.isConnected())) {
                break label_4;
            }
            var5.send(var3);
        }
    }

    public static int indexToSlot(int arg0) {
        if (mc.player != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if ((((var2 == null) || (mc.player.inventoryMenu == var2)) || (arg0 < 0)) || (arg0 > 35)) break label_4;
        var tmp0 = (var2.slots.size() - 36);
        int var1 = tmp0;
        if (tmp0 < 0) break label_4;
        if (arg0 <= 8) {
        } else {
        }
        return (var1 + (arg0 - 9));
        label_4:
        if (arg0 >= 0 && arg0 <= 8) return (36 + arg0);
        return arg0;
    }

    public static int find(Item arg0) {
        return find(arg0, HOTBAR_START, INVENTORY_END);
    }

    public static int findHotbar(Item arg0) {
        return find(arg0, HOTBAR_START, HOTBAR_END);
    }

    public static int findInventory(Item arg0) {
        return find(arg0, INVENTORY_START, INVENTORY_END);
    }

    public static int find(Item arg0, int arg1, int arg2) {
        int var3 = arg2;
        while (var3 >= arg1) {
            Object var4 = mc.player.getInventory().getItem(var3);
            if (var4.getItem() != arg0) {
            } else {
                return var3;
            }
            var3--;
        }
        return -1;
    }

    public static int find(Class arg0) {
        return find(arg0, HOTBAR_START, INVENTORY_END);
    }

    public static int findHotbar(Class arg0) {
        return find(arg0, HOTBAR_START, HOTBAR_END);
    }

    public static int findInventory(Class arg0) {
        return find(arg0, INVENTORY_START, INVENTORY_END);
    }

    public static int find(Class arg0, int arg1, int arg2) {
        int var3 = arg2;
        while (var3 >= arg1) {
            Object var4 = mc.player.getInventory().getItem(var3);
            if (!(var4.getItem().getClass().isAssignableFrom(arg0))) {
            } else {
                return var3;
            }
            var3--;
        }
        return -1;
    }

    public static int findInventory(Item arg0, int arg1) {
        int var2 = INVENTORY_END;
        while (var2 >= INVENTORY_START) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if (var3.getItem() != arg0) break label_2;
            if (mc.player.getInventory().getItem(var2).getCount() < arg1) {
                break label_2;
            }
            return var2;
            label_2:
            var2--;
        }
        return -1;
    }

    public static int findHardestBlock(int arg0, int arg1) {
        float var2 = -1.0f;
        int var3 = -1;
        int var4 = arg0;
        while (var4 <= arg1) {
            Object var5 = mc.player.getInventory().getItem(var4).getItem();
            if (!((var5 instanceof BlockItem))) {
                break label_4;
            }
            Object var6 = ((BlockItem) var5);
            float var7 = var6.getBlock().defaultDestroyTime();
            if (var7 == -1.0f) return var4;
            if (var7 <= var2) {
                break label_4;
            }
            var2 = var7;
            var3 = var4;
            label_4:
            var4++;
        }
        return var3;
    }

    public static int findFastestItem(BlockState arg0, int arg1, int arg2) {
        double var3 = -1.0;
        int var5 = -1;
        int var6 = arg1;
        while (var6 <= arg2) {
            double var7 = ((double) mc.player.getInventory().getItem(var6).getItem().getDestroySpeed(mc.player.getInventory().getItem(var6), arg0));
            if (var7 <= var3) {
            } else {
                var3 = var7;
                var5 = var6;
            }
            var6++;
        }
        return var5;
    }

    public static int findBestSword(int arg0, int arg1) {
        int var2 = -1;
        int var3 = -1;
        int var4 = -1;
        int var5 = -1;
        int var6 = -1;
        int var7 = -1;
        int var8 = arg1;
        while (var8 >= arg0) {
            Object var9 = mc.player.getInventory().getItem(var8);
            if (var9.getItem() == Items.NETHERITE_SWORD) {
                var2 = var8;
            }
            if (var9.getItem() == Items.DIAMOND_SWORD) {
                var3 = var8;
            }
            if (var9.getItem() == Items.IRON_SWORD) {
                var4 = var8;
            }
            if (var9.getItem() == Items.GOLDEN_SWORD) {
                var5 = var8;
            }
            if (var9.getItem() == Items.STONE_SWORD) {
                var6 = var8;
            }
            if (var9.getItem() != Items.WOODEN_SWORD) {
            } else {
                var7 = var8;
            }
            var8--;
        }
        if (var2 != -1) return var2;
        if (var3 != -1) return var3;
        if (var4 != -1) return var4;
        if (var5 != -1) return var5;
        if (var6 != -1) return var6;
        return var7;
    }

    public static int findEmptySlot(int arg0, int arg1) {
        int var2 = arg1;
        while (var2 >= arg0) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if (!(var3.isEmpty())) {
            } else {
                return var2;
            }
            var2--;
        }
        return -1;
    }

    public static boolean inInventoryScreen() {
        if (!(mc.gui.screen() instanceof InventoryScreen) && !(mc.gui.screen() instanceof CreativeModeInventoryScreen) && !(mc.gui.screen() instanceof ContainerScreen)) {
            if ((mc.gui.screen() instanceof ShulkerBoxScreen)) {
            }
        } else {
        }
        return false;
    }

    static {
        var tmp0 = new String[5];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Silent";
        tmp0[3] = "AltPickup";
        tmp0[4] = "AltSwap";
        SWITCH_MODES = tmp0;
        var tmp1 = new String[2];
        tmp1[0] = "Pickup";
        tmp1[1] = "Swap";
        SWAP_MODES = tmp1;
        HOTBAR_START = 0;
        HOTBAR_END = 8;
        INVENTORY_START = 9;
        INVENTORY_END = 35;
        STACK = new ArrayDeque();
        LONG_HOLD = ConcurrentHashMap.newKeySet();
        PROGRAMMATIC_SWITCH = ThreadLocal.withInitial(() -> false);
    }


    record Frame(int key, int held, long time) {

        private Frame(int arg0, int arg1, long arg2) {
            this.key = arg0;
            this.held = arg1;
            this.time = arg2;
        }

        public int key() {
            return this.key;
        }

        public int held() {
            return this.held;
        }

        public long time() {
            return this.time;
        }

    }
}
