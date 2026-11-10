package lightning.modules.impl.player;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.MainhandModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@RegisterModule(name = "Replenish", description = "Automatically replenishes stacks in your hotbar with new ones when they meet a specified threshold.", category = Module.Category.PLAYER)
public class ReplenishModule extends Module {
    public ModeSetting switchMode;
    public NumberSetting threshold;
    public NumberSetting minimumCount;
    public BooleanSetting unstackable;
    public BooleanSetting inventory;
    private int ticks;
    private final Item[] hotbarItems;
    private final int[] hotbarCounts;

    public ReplenishModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Pickup";
        tmp0[1] = "Swap";
        tmp0[2] = "Quick";
        this.switchMode = new ModeSetting("Switch", "The mode that will be used for switching items.", "Swap", tmp0);
        this.threshold = new NumberSetting("Threshold", "The minimum amount of items in a stack before that stack is replaced.", 12, 1, 64);
        this.minimumCount = new NumberSetting("MinimumCount", "The minimum amount of items that should be in the new stack.", 48, 1, 64);
        this.unstackable = new BooleanSetting("Unstackable", "Replenishes items that cannot be stacked (like Totems).", false);
        this.inventory = new BooleanSetting("Inventory", "Allows replenishing items while an inventory, container, or custom GUI is open.", false);
        this.hotbarItems = new Item[9];
        this.hotbarCounts = new int[9];
    }

    public void onEnable() {
        if (mc.player == null) return;
        int var1 = 0;
        while (var1 <= 8) {
            Object var2 = mc.player.getInventory().getItem(var1);
            this.hotbarItems[var1] = var2.getItem();
            this.hotbarCounts[var1] = var2.getCount();
            var1++;
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.inventory.getValue()) break label_8;
        if (mc.gui == null) break label_2;
        if (mc.gui.screen() == null) {
            label_2:
            if (mc.player.containerMenu != null && mc.player.containerMenu != mc.player.inventoryMenu) {
            }
        } else {
        }
        int var2 = 0;
        int var3 = 0;
        if (!var2) break label_8;
        int var4 = 0;
        while (var4 <= 8) {
            Object var5 = mc.player.getInventory().getItem(var4);
            this.hotbarItems[var4] = var5.getItem();
            this.hotbarCounts[var4] = var5.getCount();
            var4++;
        }
        return;
        label_8:
        if (mc.player.containerMenu != null && mc.player.containerMenu != mc.player.inventoryMenu && mc.player.containerMenu.slots.size() < 36) return;
        if (this.ticks > 0) break label_24;
        var2 = 0;
        while (var2 <= 8) {
            var5 = mc.player.getInventory().getItem(var2);
            Object var6 = var5.getItem();
            int var7 = var5.getCount();
            Object var8 = this.hotbarItems[var2];
            int var9 = this.hotbarCounts[var2];
            this.hotbarItems[var2] = var6;
            this.hotbarCounts[var2] = var7;
            if ((var8 == null) || (var8 == Items.AIR)) break label_23;
            if (var6 == var8 && var7 >= var9) {
                break label_23;
            }
            Object var10 = (var6 == Items.AIR ? var8 : var6);
            if (var6 == Items.AIR) {
            } else {
            }
            var4 = var5;
            Object var11 = var5;
            if (!(var4.isStackable())) {
                if (!(this.unstackable.getValue())) break label_23;
            }
            if (var10 == Items.TOTEM_OF_UNDYING) {
                if (((MainhandModule) LightningClient.MODULE_MANAGER.getModule(MainhandModule.class)).legitOwnsTotems()) break label_23;
            }
            if (!(var4.isStackable())) {
                if (var7 > 0) {
                    break label_23;
                }
                if (var7 > ((int) ((this.threshold.getValue().floatValue() / 64.0f) * ((float) var4.getMaxStackSize())))) break label_23;
            }
            var tmp0 = InventoryUtils.findInventory(var10, ((int) ((((float) this.minimumCount.getValue().intValue()) / 64.0f) * ((float) var4.getMaxStackSize()))));
            var3 = tmp0;
            if (tmp0 == -1) {
                break label_23;
            }
            if (this.switchMode.getValue().equalsIgnoreCase("Quick")) {
                mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, InventoryUtils.indexToSlot(var3), 0, ContainerInput.QUICK_MOVE, mc.player);
            } else {
                InventoryUtils.swap(this.switchMode.getValue(), var3, var2);
            }
            this.ticks = (2 + LightningClient.SERVER_MANAGER.getPingDelay());
            label_23:
            var2++;
        }
        label_24:
        this.ticks = (this.ticks - 1);
    }

    public String getMetaData() {
        return String.valueOf(this.threshold.getValue().intValue());
    }

}
