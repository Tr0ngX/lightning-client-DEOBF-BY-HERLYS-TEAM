package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.MultiTaskModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@RegisterModule(name = "AutoTotem", description = "Automatically puts a specified item in your offhand slot.", category = Module.Category.COMBAT)
public class AutoTotemModule extends Module {
    public ModeSetting item;
    public NumberSetting health;
    public BooleanSetting elytraCheck;
    public NumberSetting fallDistance;
    public BooleanSetting useGapple;
    public BooleanSetting gappleSword;
    public BooleanSetting gapplePickaxe;
    public BooleanSetting gappleTotem;
    public BooleanSetting lethalOverride;
    public BooleanSetting noTotemGap;
    public BooleanSetting version117;
    public BooleanSetting smartMine;
    public BooleanSetting antiMace;
    public NumberSetting maceRange;
    public BooleanSetting debug;
    private int totemCount;
    private int gappleStagedSlot;
    private boolean wasAlive;
    private String lastReason;

    public AutoTotemModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Totem";
        tmp0[1] = "Crystal";
        tmp0[2] = "Gapple";
        this.item = new ModeSetting("Item", "The item that will be placed in your offhand slot when safety conditions are met.", "Totem", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Crystal";
        tmp1[1] = "Gapple";
        this.health = new NumberSetting("Health", "The health at which a totem will be prioritized.", new ModeSetting.Visibility(this.item, tmp1), 16, 0, 36);
        this.elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
        this.fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", 20.0f, 0.0f, 80.0f);
        this.useGapple = new BooleanSetting("UseGapple", "Switches to a golden apple in your offhand based on main hand items when health is safe.", true);
        this.gappleSword = new BooleanSetting("Sword", "Switches to a gapple when holding a sword or axe in main hand.", new BooleanSetting.Visibility(this.useGapple, true), true);
        this.gapplePickaxe = new BooleanSetting("Pickaxe", "Switches to a gapple when holding a pickaxe in main hand.", new BooleanSetting.Visibility(this.useGapple, true), true);
        this.gappleTotem = new BooleanSetting("Totem", "Switches to a gapple when holding a totem in main hand.", new BooleanSetting.Visibility(this.useGapple, true), false);
        this.lethalOverride = new BooleanSetting("LethalOverride", "Overrides any necessity for a totem when gappling.", new BooleanSetting.Visibility(this.useGapple, true), false);
        this.noTotemGap = new BooleanSetting("NoTotemGap", "Swap your gap into offhand when no totems left", false);
        this.version117 = new BooleanSetting("1.17+", "1.17+ uses the SWAP inventory action, while having it off uses inventory clicks.", true);
        var tmp2 = new String[1];
        tmp2[0] = "Crystal";
        this.smartMine = new BooleanSetting("SmartMine", "Switches to a crystal whenever you start mining and a totem when you aren't mining.", new ModeSetting.Visibility(this.item, tmp2), false);
        this.antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
        this.maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", new BooleanSetting.Visibility(this.antiMace, true), 12.0f, 0.0f, 24.0f);
        this.debug = new BooleanSetting("Debug", "Logs why you died and why the totem swap failed at that moment.", false);
        this.totemCount = 0;
        this.gappleStagedSlot = -1;
        this.wasAlive = true;
        this.lastReason = "";
    }

    public void onDisable() {
        this.gappleStagedSlot = -1;
    }

    @SubscribeEvent(priority = 2147483647)
    public void onPlayerPop(PlayerPopEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if (arg0.getPlayer() == mc.player && !(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            if (this.debug.getValue()) {
                log(("TOTEM POPPED (survived). health+absorb=" + (mc.player.getHealth() + mc.player.getAbsorptionAmount()) + ", totemsLeft=" + this.totemCount + ", offhandNow=" + itemName(mc.player.getOffhandItem().getItem())));
            }
            updateTotem();
        }
    }

    @SubscribeEvent(priority = 2147483647)
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        updateTotem();
    }

    private void updateTotem() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(PingBypassFlags.isPingBypassActive())) break label_2;
        reason("skip: PingBypass active");
        return;
        label_2:
        Object var3 = mc.gui.screen();
        int var4 = (var3 instanceof InventoryScreen);
        int var5 = (var3 instanceof AbstractContainerScreen);
        if ((!var5) || (mc.player.containerMenu.getCarried().isEmpty())) break label_3;
        reason("skip: cursor is holding an item in an open container");
        return;
        label_3:
        Object var6 = getItem();
        if (var6 == null) {
            reason("skip: getItem() returned null (no valid target item)");
            return;
        }
        if (mc.player.containerMenu.getCarried().isEmpty()) break label_9;
        Object var7 = mc.player.containerMenu.getCarried();
        if (var7.getItem() == var6) {
            InventoryUtils.click(45, 0, ContainerInput.PICKUP);
            if ((var4) || (mc.player.containerMenu.getCarried().isEmpty())) break label_5;
            var tmp0 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            int var8 = tmp0;
            if (tmp0 == -1) break label_5;
            InventoryUtils.click(InventoryUtils.indexToSlot(var8), 0, ContainerInput.PICKUP);
            label_5:
            if (this.debug.getValue()) {
                log(("RECOVERED stuck cursor -> placed " + itemName(var6) + " into offhand"));
            }
            return;
        }
        if (var4) break label_9;
        var8 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
        if (var8 != -1) {
            InventoryUtils.click(InventoryUtils.indexToSlot(var8), 0, ContainerInput.PICKUP);
        }
        if (mc.player.containerMenu.getCarried().isEmpty()) break label_9;
        reason("skip: cursor is holding an item and could not clear it");
        return;
        label_9:
        if (var6 == Items.TOTEM_OF_UNDYING && ((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled() && ((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).offhandOverride.getValue()) {
            if (mc.player.getOffhandItem().isEmpty()) {
                reason("skip: Suicide offhandOverride, offhand already empty");
                return;
            }
            int var2 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            if (var2 != -1) break label_21;
            reason("Suicide override: no empty slot to move offhand item into");
            break label_21;
        }
        if (mc.player.getOffhandItem().getItem() == var6) {
            reason(("ok: offhand already holds target (" + itemName(var6) + ")"));
            return;
        }
        if (this.gappleStagedSlot != -1 && var6 == Items.TOTEM_OF_UNDYING) {
            if (mc.player.getInventory().getItem(this.gappleStagedSlot).getItem() == Items.TOTEM_OF_UNDYING) {
                var2 = this.gappleStagedSlot;
                break label_14;
            }
            var2 = InventoryUtils.findInventory(var6);
            if (var2 != -1) break label_14;
            var2 = InventoryUtils.find(var6);
            label_14:
            this.gappleStagedSlot = -1;
            break label_19;
        }
        if (var6 != Items.ENCHANTED_GOLDEN_APPLE) {
            if (var6 == Items.GOLDEN_APPLE) {
            }
            var2 = InventoryUtils.findHotbar(var6);
            if (var2 == -1) {
                var2 = InventoryUtils.findInventory(var6);
            }
            if (var2 == -1) break label_19;
            this.gappleStagedSlot = var2;
            break label_19;
        }
        var2 = InventoryUtils.findInventory(var6);
        if (var2 != -1) break label_19;
        var2 = InventoryUtils.find(var6);
        label_19:
        if (var2 != -1) break label_21;
        if (var6 == Items.TOTEM_OF_UNDYING && hasItem(Items.TOTEM_OF_UNDYING)) {
            var2 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            if (var2 != -1) break label_21;
            reason("FAIL: have a totem but no empty slot to stage the swap");
            break label_21;
        }
        reason(("FAIL: target " + itemName(var6) + " NOT FOUND in inventory (none left)"));
        return;
        label_21:
        if (var2 == -1) {
            reason("FAIL: no usable slot resolved (see previous reason)");
            return;
        }
        var7 = ((MultiTaskModule) LightningClient.MODULE_MANAGER.getModule(MultiTaskModule.class));
        if ((var7 == null) || (!(var7.isToggled()))) break label_23;
        var tmp1 = (var7.autoTotem.getValue() ? 1 : 0);
        int var1 = tmp1;
        var8 = tmp1;
        if (!var1 && mc.player.isUsingItem()) {
            mc.gameMode.releaseUsingItem(mc.player);
        }
        var tmp2 = ((AutoShopModule) LightningClient.MODULE_MANAGER.getModule(AutoShopModule.class));
        if (tmp2.antiGhostTotem.getValue() && tmp2.isRunning()) {
            tmp2.abort("AntiGhostTotem: shop cancelled so the totem can be swapped now.");
        } else {
        }
        if (this.version117.getValue()) {
            InventoryUtils.swap("Swap", var2, 40);
            break label_29;
        }
        InventoryUtils.swap("Pickup", var2, 45);
        if ((var4) || (mc.player.containerMenu.getCarried().isEmpty())) break label_29;
        var tmp3 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
        int var9 = tmp3;
        if (tmp3 == -1) break label_29;
        InventoryUtils.click(InventoryUtils.indexToSlot(var9), 0, ContainerInput.PICKUP);
        label_29:
        if (this.debug.getValue()) {
            log(("SWAP -> offhand: " + itemName(var6) + " (from slot " + var2 + ") [mode=" + (this.version117.getValue() ? "1.17+ (Swap)" : "Clicks (Pickup)") + "]"));
        }
        this.lastReason = "";
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(mc.player.containerMenu.getCarried().isEmpty()) && mc.player.containerMenu.getCarried().getItem() == Items.TOTEM_OF_UNDYING) {
        } else {
        }
        mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING).totemCount = (mc.player.containerMenu.getCarried().getCount() + 0);
        if (mc.player.getHealth() <= 0.0f) break label_5;
        var tmp0 = (!(mc.player.isDeadOrDying()) ? 1 : 0);
        int var2 = tmp0;
        int var3 = tmp0;
        if (this.debug.getValue() && !(!this.wasAlive) && !var2) {
            logDeath();
        }
        this.wasAlive = var2;
    }

    private void logDeath() {
        try {
            Object var1 = mc.player.getCombatTracker().getDeathMessage().getString();
        } catch (Throwable ex2) {
            Object var2 = ex2;
            var1 = "(unknown)";
        }
        var2 = mc.player.getOffhandItem();
        int var3 = (var2.getItem() == Items.TOTEM_OF_UNDYING ? 1 : 0);
        log("================ DEATH ================");
        log(("Cause: " + var1));
        if (var2.isEmpty()) {
        } else {
        }
        "EMPTY".log(("Offhand at death: " + itemName(var2.getItem()) + (!(!var3) ? " (totem WAS present!)" : "")));
        log(("Totems in inventory at death: " + this.totemCount));
        log(("Health+Absorb at death: " + (mc.player.getHealth() + mc.player.getAbsorptionAmount())));
        log(("needsTotem()=" + needsTotem()));
        log(("Item mode=" + this.item.getValue() + ", target now=" + itemName(getItem())));
        log(("Last swap reason: " + (this.lastReason.isEmpty() ? "(none)" : this.lastReason)));
        if (!(!var3)) {
            log("=> A totem WAS in offhand but you still died (one-shot exceeding totem heal, or totem pop not registered before death).");
            break label_14;
        }
        if (!this.totemCount) {
            log("=> No totems left in inventory -- nothing to place.");
            break label_14;
        }
        log("=> Totems available but offhand didn't have one -- see 'Last swap reason' above for why the swap didn't happen.");
        label_14:
        log("=======================================");
    }

    private Item getItem() {
        int var3 = hasItem(Items.TOTEM_OF_UNDYING);
        if (hasItem(Items.ENCHANTED_GOLDEN_APPLE)) {
            break label_2;
        }
        Object var4 = (hasItem(Items.GOLDEN_APPLE) ? Items.GOLDEN_APPLE : null);
        int var5 = (var4 != null ? 1 : 0);
        if (!(mc.player.getMainHandItem().is(ItemTags.SWORDS))) {
            if ((mc.player.getMainHandItem().getItem() instanceof AxeItem)) {
            }
        } else {
        }
        int var6 = 0;
        int var7 = mc.player.getMainHandItem().is(ItemTags.PICKAXES);
        int var8 = mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING);
        if (this.gappleSword.getValue()) {
            if (var6) break label_10;
        }
        if (this.gapplePickaxe.getValue()) {
            if (var7) break label_10;
        }
        if (this.gappleTotem.getValue() && !(!var8)) {
            label_10:
        } else {
        }
        int var2 = 0;
        int var9 = 0;
        if (this.useGapple.getValue() && mc.options.keyUse.isDown() && !(!var2) && !(!var5)) {
            if (((this.lethalOverride.getValue()) || (!(needsTotem()))) || (!var3)) {
                return var4;
            }
        }
        if (needsTotem()) {
            if (!(!var3)) return Items.TOTEM_OF_UNDYING;
            if (this.noTotemGap.getValue() && !(!var5)) return var4;
        }
        if (((!var3) || (!(this.item.getValue().equalsIgnoreCase("Crystal")))) || (!(this.smartMine.getValue()))) break label_19;
        var tmp0 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        Object var1 = tmp0;
        if (tmp0.getPrimary() != null) {
            if (var1.getPrimary().isMining()) break label_19;
        }
        if ((var1.getSecondary() == null) || (!(var1.getSecondary().isMining()))) {
            return Items.TOTEM_OF_UNDYING;
        }
        label_19:
        Object var10 = this.item.getValue();
        int var11 = -1;
        switch (var10.hashCode()) {
            case -1582753002 -> { break label_20; }
            case 2125698931 -> { break label_21; }
            default -> { break label_22; }
        }
        label_20:
        if (!(var10.equals("Crystal"))) break label_22;
        var11 = 0;
        break label_22;
        label_21:
        if (!(var10.equals("Gapple"))) break label_22;
        var11 = 1;
        label_22:
        switch (var11) {
            case 0 -> { break label_23; }
            case 1 -> { break label_25; }
            default -> { break label_27; }
        }
        label_23:
        if (!(hasItem(Items.END_CRYSTAL))) {
            break label_29;
        }
        return Items.END_CRYSTAL;
        label_25:
        if (!var5) {
            break label_29;
        }
        return var4;
        label_27:
        if (!var3) {
            break label_29;
        }
        return Items.TOTEM_OF_UNDYING;
        label_29:
        if (!(!var3)) return Items.TOTEM_OF_UNDYING;
        if (this.noTotemGap.getValue() && !(!var5)) return var4;
        return mc.player.getOffhandItem().getItem();
    }

    private boolean needsTotem() {
        if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled() && ((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).offhandOverride.getValue()) return false;
        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= this.health.getValue().floatValue()) return true;
        if (mc.player.fallDistance > ((double) this.fallDistance.getValue().floatValue())) return true;
        if (this.elytraCheck.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) return true;
        if (!(this.antiMace.getValue())) break label_4;
        return mc.level.players().stream().anyMatch(() -> {
    if ((((arg0 == mc.player) || (LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString()))) || (mc.player.distanceToSqr(arg0) > ((double) Mth.square(this.maceRange.getValue().floatValue())))) || (arg0.fallDistance < 1.5)) break label_0;
    return arg0.getMainHandItem().getItem().equals(Items.MACE);
});
    }

    private boolean hasItem(Item arg0) {
        if (InventoryUtils.find(arg0) == -1 && mc.player.getOffhandItem().getItem() != arg0) {
            if (!(mc.player.containerMenu.getCarried().isEmpty()) && mc.player.containerMenu.getCarried().getItem() == arg0) {
            }
        } else {
        }
        return false;
    }

    private void reason(String arg0) {
        if (!(this.debug.getValue())) return;
        if (arg0.equals(this.lastReason)) return;
        this.lastReason = arg0;
        LightningClient.CHAT_MANAGER.tagged(arg0, getName());
    }

    private void log(String arg0) {
        LightningClient.CHAT_MANAGER.tagged(arg0, getName());
    }

    private String itemName(Item arg0) {
        if ((arg0 == null) || (arg0 == Items.AIR)) {
            return "none";
        }
        return BuiltInRegistries.ITEM.getKey(arg0).getPath();
    }

    public String getMetaData() {
        return String.valueOf(this.totemCount);
    }

}
