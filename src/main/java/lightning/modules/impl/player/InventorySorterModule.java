package lightning.modules.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;

@RegisterModule(name = "InventorySorter", description = "Auto-sorts your inventory into the active Rekit kit's layout.", category = Module.Category.PLAYER)
public class InventorySorterModule extends Module {
    public NumberSetting delay;
    public NumberSetting actionsPerTick;
    public BooleanSetting ignoreHotbar;
    private int ticks;
    public static volatile long lastContainerActionMs;
    private int cursorWaitTicks;
    private static final int CURSOR_DUMP_AFTER_TICKS = 20;

    public InventorySorterModule() {
        this.delay = new NumberSetting("Delay", "Tick delay.", 1, 0, 10);
        this.actionsPerTick = new NumberSetting("ActionsPerTick", "Max actions per tick.", 1, 1, 5);
        this.ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when sorting.", false);
        this.ticks = 0;
        this.cursorWaitTicks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        Object var2 = ((RekitModule) LightningClient.MODULE_MANAGER.getModule(RekitModule.class));
        if (var2.getActiveKit().isEmpty()) return;
        if (mc.player.isCreative()) return;
        if ((mc.gui.screen() instanceof AbstractContainerScreen) && !(mc.gui.screen() instanceof InventoryScreen)) return;
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        this.ticks = 0;
        int var3 = 0;
        while (var3 < this.actionsPerTick.getValue().intValue() && sortTick(var2)) {
            var3++;
        }
    }

    private boolean sortTick(RekitModule arg0) {
        Object var2 = mc.player.containerMenu;
        if (var2.getCarried().isEmpty()) break label_1;
        this.cursorWaitTicks = (this.cursorWaitTicks + 1);
        if (this.cursorWaitTicks < 20) break label_0;
        this.cursorWaitTicks = 0;
        int var3 = findEmptySlot(var2);
        if (var3 == -1) break label_0;
        click(var3, 0, ContainerInput.PICKUP);
        label_0:
        return false;
        label_1:
        this.cursorWaitTicks = 0;
        var3 = new HashMap();
        int var4 = 0;
        while (var4 < 36) {
            if (this.ignoreHotbar.getValue()) {
                if (var4 <= 8) break label_5;
            }
            var tmp0 = getHandlerSlot(var4);
            int var6 = tmp0;
            var tmp1 = var2.getSlot(tmp0).getItem();
            Object var7 = tmp1;
            if (((tmp1.isEmpty()) || (isShulkerBox(var7))) || (var7.getCount() >= var7.getItem().getDefaultMaxStackSize())) break label_5;
            var tmp2 = ((RekitModule.KitItem) arg0.getActiveKit().get(var4));
            Object var5 = tmp2;
            if (isCorrectItem(var7, tmp2)) {
                break label_5;
            }
            Object var8 = BuiltInRegistries.ITEM.getKey(var7.getItem()).toString();
            ((List) var3.computeIfAbsent(var8, (arg0) -> new ArrayList())).add(var6);
            label_5:
            var4++;
        }
        var4 = var3.values().iterator();
        while (var4.hasNext()) {
            var5 = ((List) var4.next());
            if (var5.size() <= 1) {
                break label_7;
            }
            var6 = 0;
            while (var6 < var5.size()) {
                var7 = ((Integer) var5.get(var6)).intValue();
                var8 = var2.getSlot(var7).getItem();
                int var9 = (var6 + 1);
                while (var9 < var5.size()) {
                    int var10 = ((Integer) var5.get(var9)).intValue();
                    Object var11 = var2.getSlot(var10).getItem();
                    if (!(ItemStack.isSameItemSameComponents(var8, var11))) {
                    } else {
                        atomicSwap(var10, var7);
                        return true;
                    }
                    var9++;
                }
                var6++;
            }
        }
        var4 = 0;
        while (var4 < 36) {
            if (this.ignoreHotbar.getValue()) {
                if (var4 <= 8) break label_21;
            }
            var tmp3 = getHandlerSlot(var4);
            var7 = tmp3;
            var tmp4 = var2.getSlot(tmp3).getItem();
            var8 = tmp4;
            if (isShulkerBox(tmp4)) break label_21;
            var tmp5 = ((RekitModule.KitItem) arg0.getActiveKit().get(var4));
            var6 = tmp5;
            if (isCorrectItem(var8, tmp5)) break label_21;
            var tmp6 = findItemForKit(var2, arg0, var6);
            var5 = tmp6;
            if (tmp6 == -1) {
                break label_21;
            }
            var9 = getHandlerSlot(var5);
            if (var4 <= 8 && var5 >= 9) {
                click(var9, var4, ContainerInput.SWAP);
                return true;
            }
            if (var5 <= 8 && var4 >= 9) {
                click(var7, var5, ContainerInput.SWAP);
                return true;
            }
            atomicSwap(var9, var7);
            return true;
            label_21:
            var4++;
        }
        return false;
    }

    private void atomicSwap(int arg0, int arg1) {
        click(arg0, 0, ContainerInput.PICKUP);
        click(arg1, 0, ContainerInput.PICKUP);
        click(arg0, 0, ContainerInput.PICKUP);
    }

    private void click(int arg0, int arg1, ContainerInput arg2) {
        lastContainerActionMs = System.currentTimeMillis();
        mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, arg0, arg1, arg2, mc.player);
    }

    private boolean isCorrectItem(ItemStack arg0, RekitModule.KitItem arg1) {
        if (arg1 == null) return true;
        if (arg0.isEmpty()) return false;
        if ((arg1.id == null) || (arg1.id.isEmpty())) {
            return false;
        }
        Object var3 = Identifier.tryParse(arg1.id);
        if (var3 == null) return false;
        Object var4 = ((Item) BuiltInRegistries.ITEM.getValue(var3));
        if (var4 == null) break label_5;
        return arg0.getItem() == var4;
    }

    private int findItemForKit(AbstractContainerMenu arg0, RekitModule arg1, RekitModule.KitItem arg2) {
        if (((arg2 == null) || (arg2.id == null)) || (arg2.id.isEmpty())) {
            return -1;
        }
        Object var4 = Identifier.tryParse(arg2.id);
        if (var4 == null) return -1;
        Object var5 = ((Item) BuiltInRegistries.ITEM.getValue(var4));
        if (var5 == null) return -1;
        int var6 = 0;
        while (var6 < 36) {
            if (this.ignoreHotbar.getValue()) {
                if (var6 <= 8) break label_7;
            }
            var tmp0 = getHandlerSlot(var6);
            int var8 = tmp0;
            var tmp1 = arg0.getSlot(tmp0).getItem();
            Object var9 = tmp1;
            if (((tmp1.isEmpty()) || (isShulkerBox(var9))) || (var9.getItem() != var5)) break label_7;
            var tmp2 = ((RekitModule.KitItem) arg1.getActiveKit().get(var6));
            Object var7 = tmp2;
            if (isCorrectItem(var9, tmp2)) {
                break label_7;
            }
            return var6;
            label_7:
            var6++;
        }
        return -1;
    }

    private int findEmptySlot(AbstractContainerMenu arg0) {
        Object var4 = ((RekitModule) LightningClient.MODULE_MANAGER.getModule(RekitModule.class));
        int var3 = 0;
        while (var3 < 36) {
            if (this.ignoreHotbar.getValue()) {
                if (var3 <= 8) break label_3;
            }
            var tmp0 = getHandlerSlot(var3);
            int var2 = tmp0;
            if (!(arg0.getSlot(tmp0).getItem().isEmpty())) break label_3;
            if (var4.getActiveKit().get(var3) != null) {
                break label_3;
            }
            return var2;
            label_3:
            var3++;
        }
        var3 = 0;
        while (var3 < 36) {
            if (this.ignoreHotbar.getValue()) {
                if (var3 <= 8) break label_8;
            }
            var tmp1 = getHandlerSlot(var3);
            var2 = tmp1;
            if (!(arg0.getSlot(tmp1).getItem().isEmpty())) {
                break label_8;
            }
            return var2;
            label_8:
            var3++;
        }
        return -1;
    }

    private int getHandlerSlot(int arg0) {
        if (arg0 >= 0 && arg0 <= 8) return (36 + arg0);
        if (arg0 >= 9 && arg0 <= 35) return arg0;
        return -1;
    }

    private boolean isShulkerBox(ItemStack arg0) {
        if (arg0.isEmpty()) break label_0;
        var tmp0 = arg0.getItem();
        Object var3 = tmp0;
        if (!((tmp0 instanceof BlockItem))) break label_0;
        var tmp1 = ((BlockItem) var3);
        Object var2 = tmp1;
        return (tmp1.getBlock() instanceof ShulkerBoxBlock);
    }

    static {
        lastContainerActionMs = 0L;
    }

}
