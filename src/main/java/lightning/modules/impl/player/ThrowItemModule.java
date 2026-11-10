package lightning.modules.impl.player;

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
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

@RegisterModule(name = "ThrowItem", description = "Throw out items that arent belong to your loaded kit. Useful when you need to loot gears during combat", category = Module.Category.PLAYER)
public class ThrowItemModule extends Module {
    public NumberSetting delay;
    public NumberSetting actionsPerTick;
    public BooleanSetting autoDisable;
    public BooleanSetting keepKitItems;
    public BooleanSetting minimumCount;
    public NumberSetting minCount;
    public BooleanSetting ignoreHotbar;
    public BooleanSetting onlyInventory;
    private int ticks;

    public ThrowItemModule() {
        this.delay = new NumberSetting("Delay", "Tick delay between throw actions.", 1, 0, 20);
        this.actionsPerTick = new NumberSetting("ActionsPerTick", "Maximum items to drop per pass.", 3, 1, 20);
        this.autoDisable = new BooleanSetting("AutoDisable", "Automatically disables the module when no items are left to throw.", false);
        this.keepKitItems = new BooleanSetting("KeepKitItems", "Keep items that belong to the loaded kit even if in a different slot.", true);
        this.minimumCount = new BooleanSetting("MinimumCount", "Keep matching items only if their stack count reaches minimum count.", false);
        this.minCount = new NumberSetting("MinCount", "The minimum stack count to keep an item.", new BooleanSetting.Visibility(this.minimumCount, true), 16, 1, 64);
        this.ignoreHotbar = new BooleanSetting("IgnoreHotbar", "Do not throw items from the hotbar (slots 0-8).", false);
        this.onlyInventory = new BooleanSetting("OnlyInventory", "Only throw items when in regular gameplay or InventoryScreen (not in Chests/Shulkers).", true);
        this.ticks = 0;
    }

    public void onEnable() {
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gameMode == null)) {
            return;
        }
        if (mc.player.isCreative()) break label_2;
        if (mc.player.isSpectator()) {
            label_2:
            return;
        }
        if (!(mc.player.containerMenu.getCarried().isEmpty())) return;
        Object var3 = ((RekitModule) LightningClient.MODULE_MANAGER.getModule(RekitModule.class));
        if (var3 == null) return;
        Object var4 = var3.getActiveKit();
        if ((var4 == null) || (var4.isEmpty())) {
            return;
        }
        if ((var3.isAutoActive()) || ((System.currentTimeMillis() - RekitModule.lastContainerActionMs) < 200L)) {
            return;
        }
        if (mc.gui.screen() != null && !(mc.gui.screen() instanceof InventoryScreen)) return;
        if (!((mc.gui.screen() instanceof AbstractContainerScreen))) break label_11;
        var tmp0 = (!((mc.gui.screen() instanceof InventoryScreen)) ? 1 : 0);
        int var2 = tmp0;
        int var5 = tmp0;
        if (this.onlyInventory.getValue() && !(!var2)) return;
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        this.ticks = 0;
        int var6 = mc.player.containerMenu.containerId;
        int var7 = 0;
        int var8 = this.actionsPerTick.getValue().intValue();
        int var9 = 0;
        int var10 = 0;
        while (var10 < 36) {
            if (this.ignoreHotbar.getValue()) {
                if (var10 <= 8) break label_26;
            }
            var tmp1 = mc.player.getInventory().getItem(var10);
            Object var12 = tmp1;
            if (tmp1.isEmpty()) {
                break label_26;
            }
            Object var13 = ((RekitModule.KitItem) var4.get(var10));
            Object var14 = BuiltInRegistries.ITEM.getKey(var12.getItem()).toString();
            int var15 = 0;
            if (var13 != null && var13.id != null && var14.equalsIgnoreCase(var13.id)) {
                var15 = 1;
                break label_21;
            }
            if (!(this.keepKitItems.getValue())) break label_21;
            Object var16 = var4.values().iterator();
            label_19:
            if (!(var16.hasNext())) break label_21;
            Object var17 = ((RekitModule.KitItem) var16.next());
            if ((var17 == null) || (var17.id == null)) break label_19;
            if (!(var14.equalsIgnoreCase(var17.id))) {
                break label_19;
            }
            var15 = 1;
            break label_21;
            label_21:
            var16 = 0;
            if (!var15) {
                var16 = 1;
                break label_23;
            }
            if (((!(this.minimumCount.getValue())) || (var12.getMaxStackSize() <= 1)) || (var12.getCount() >= this.minCount.getValue().intValue())) break label_23;
            var16 = 1;
            label_23:
            if (!var16) {
                break label_26;
            }
            var9++;
            if (var7 >= var8) break label_26;
            var tmp2 = invToHandlerSlot(var10);
            int var11 = tmp2;
            if (tmp2 < 0) {
                break label_26;
            }
            throwSlot(var6, var11);
            var7++;
            label_26:
            var10++;
        }
        if (!var9 && this.autoDisable.getValue()) {
            setToggled(false);
        }
    }

    private void throwSlot(int arg0, int arg1) {
        mc.gameMode.handleContainerInput(arg0, arg1, 1, ContainerInput.THROW, mc.player);
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
