package lightning.modules.impl.player;

import java.util.ArrayList;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name = "InventoryCleaner", description = "Auto-drops unwanted items from your inventory each tick.", category = Module.Category.PLAYER)
public class InventoryCleanerModule extends Module {
    public ModeSetting mode;
    public WhitelistSetting whitelist;
    public BooleanSetting ignoreHotbar;
    public BooleanSetting throwWorse;
    public BooleanSetting others;
    public BooleanSetting ignoreCustomName;
    public NumberSetting delay;
    public NumberSetting actionsPerTick;
    private int ticks;

    public InventoryCleanerModule() {
        var tmp0 = new String[3];
        tmp0[0] = "WhiteList";
        tmp0[1] = "BlackList";
        tmp0[2] = "All";
        this.mode = new ModeSetting("Mode", "WhiteList = drop items IN list. BlackList = drop items NOT in list. All = drop everything.", "WhiteList", tmp0);
        this.whitelist = new WhitelistSetting("Whitelist", "Items this mode's WhiteList/BlackList compares against.", WhitelistSetting.Type.ITEMS);
        this.ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Skip hotbar slots (0-8) when cleaning.", true);
        this.throwWorse = new BooleanSetting("ThrowWorse", "Drop lower-tier duplicate tools/armor of the same type (e.g. diamond+iron pickaxe -> drop iron).", true);
        this.others = new BooleanSetting("Others", "Also drop from other open GUIs like Chest, Shulker, EnderChest.", false);
        this.ignoreCustomName = new BooleanSetting("IgnoreCustomName", "(With Others on) skip acting on custom-titled containers such as a shop GUI, except shulker boxes. Does not affect individual items.", false);
        this.delay = new NumberSetting("Delay", "Tick delay between drop passes.", 1, 0, 20);
        this.actionsPerTick = new NumberSetting("ActionsPerTick", "Max items to drop per pass.", 5, 1, 20);
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gameMode == null)) {
            return;
        }
        if (mc.player.isCreative()) return;
        if (!(mc.player.containerMenu.getCarried().isEmpty())) return;
        long var4 = System.currentTimeMillis();
        if (((var4 - RekitModule.lastContainerActionMs) < 200L) || ((var4 - InventorySorterModule.lastContainerActionMs) < 200L)) {
            return;
        }
        if (!((mc.gui.screen() instanceof AbstractContainerScreen))) break label_6;
        var tmp0 = (!((mc.gui.screen() instanceof InventoryScreen)) ? 1 : 0);
        int var3 = tmp0;
        int var6 = tmp0;
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        this.ticks = 0;
        int var7 = mc.player.containerMenu.containerId;
        int var8 = 0;
        int var9 = this.actionsPerTick.getValue().intValue();
        int var10 = 0;
        while (var10 < 36 && var8 < var9) {
            if (this.ignoreHotbar.getValue()) {
                if (var10 <= 8) break label_12;
            }
            var tmp1 = mc.player.getInventory().getItem(var10);
            Object var12 = tmp1;
            if ((tmp1.isEmpty()) || (!(shouldDrop(var12)))) break label_12;
            var tmp2 = invToHandlerSlot(var10);
            int var11 = tmp2;
            if (tmp2 < 0) {
                break label_12;
            }
            throwSlot(var7, var11);
            var8++;
            label_12:
            var10++;
        }
        if (this.throwWorse.getValue() && var8 < var9) {
            var8 = (var8 + runThrowWorsePass(var7, (var9 - var8)));
        }
        if ((!(this.ignoreCustomName.getValue())) || (!(hasCustomContainerTitle()))) break label_15;
        var tmp3 = (!((mc.player.containerMenu instanceof ShulkerBoxMenu)) ? 1 : 0);
        int var2 = tmp3;
        var10 = tmp3;
        if ((((!var3) || (!(this.others.getValue()))) || (var2)) || (var8 >= var9)) break label_20;
        var11 = mc.player.containerMenu.slots.size();
        var12 = (var11 - 36);
        int var13 = 0;
        while (var13 < var12 && var8 < var9) {
            Object var14 = ((Slot) mc.player.containerMenu.slots.get(var13)).getItem();
            if (var14.isEmpty()) break label_19;
            if (!(shouldDrop(var14))) {
                break label_19;
            }
            throwSlot(var7, var13);
            var8++;
            label_19:
            var13++;
        }
        label_20:
    }

    private void throwSlot(int arg0, int arg1) {
        mc.gameMode.handleContainerInput(arg0, arg1, 1, ContainerInput.THROW, mc.player);
    }

    private boolean hasCustomContainerTitle() {
        Object var1 = mc.gui.screen();
        if (!((var1 instanceof AbstractContainerScreen))) return false;
        Object var2 = ((AbstractContainerScreen) var1);
        Object var3 = var2.getTitle();
        if (var3 == null) return false;
        return !((var3.getContents() instanceof TranslatableContents));
    }

    private boolean shouldDrop(ItemStack arg0) {
        int var2 = this.whitelist.isWhitelistContains(arg0.getItem());
        Object var3 = this.mode.getValue();
        int var4 = -1;
        switch (var3.hashCode()) {
            case -958939875 -> { break label_1; }
            case 65921 -> { break label_0; }
            default -> { break label_2; }
        }
        label_0:
        if (!(var3.equals("All"))) break label_2;
        var4 = 0;
        break label_2;
        label_1:
        if (!(var3.equals("BlackList"))) break label_2;
        var4 = 1;
        label_2:
        switch (var4) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            default -> { break label_6; }
        }
        label_3:
        break label_7;
        label_4:
        if (!var2) {
            break label_7;
        }
        break label_7;
        label_6:
        label_7:
        return var2;
    }

    private int runThrowWorsePass(int arg0, int arg1) {
        var tmp0 = new String[9];
        tmp0[0] = "_pickaxe";
        tmp0[1] = "_axe";
        tmp0[2] = "_shovel";
        tmp0[3] = "_hoe";
        tmp0[4] = "_sword";
        tmp0[5] = "_helmet";
        tmp0[6] = "_chestplate";
        tmp0[7] = "_leggings";
        tmp0[8] = "_boots";
        Object var3 = tmp0;
        int var4 = arg1;
        Object var5 = var3;
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            Object var8 = var5[var7];
            if (var4 > 0) break label_1;
            break label_12;
            label_1:
            Object var9 = new ArrayList();
            int var10 = 0;
            while (var10 < 36) {
                if (this.ignoreHotbar.getValue()) {
                    if (var10 <= 8) break label_5;
                }
                var tmp1 = mc.player.getInventory().getItem(var10);
                Object var12 = tmp1;
                if (tmp1.isEmpty()) break label_5;
                var tmp2 = BuiltInRegistries.ITEM.getKey(var12.getItem()).toString();
                Object var11 = tmp2;
                if (!(tmp2.endsWith(var8))) {
                    break label_5;
                }
                var tmp3 = new int[2];
                tmp3[0] = var10;
                tmp3[1] = var12.getMaxDamage();
                var9.add(tmp3);
                label_5:
                var10++;
            }
            if (var9.size() < 2) {
                break label_11;
            }
            var10 = var9.stream().mapToInt((arg0) -> arg0[1]).max().orElse(0);
            var11 = var9.iterator();
            while (var11.hasNext()) {
                var12 = ((int[]) var11.next());
                if (var4 <= 0) {
                    break label_11;
                }
                if (var12[1] >= var10) break label_8;
                var tmp4 = invToHandlerSlot(var12[0]);
                int var13 = tmp4;
                if (tmp4 < 0) {
                    break label_8;
                }
                throwSlot(arg0, var13);
                var4--;
            }
            label_11:
            var7++;
        }
        label_12:
        return (arg1 - var4);
    }

    private int invToHandlerSlot(int arg0) {
        Object var2 = mc.player.getInventory();
        Object var3 = mc.player.containerMenu.slots.iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Slot) var3.next());
            if (var4.container != var2) break label_0;
            if (var4.getContainerSlot() != arg0) {
                break label_0;
            }
            return var4.index;
        }
        return -1;
    }

}
