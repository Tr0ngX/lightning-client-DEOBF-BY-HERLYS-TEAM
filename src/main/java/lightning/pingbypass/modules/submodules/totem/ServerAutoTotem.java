package lightning.pingbypass.modules.submodules.totem;

import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.modules.impl.combat.SuicideModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.modules.PbModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ServerAutoTotem extends PbModule implements IMinecraft {
    public ModeSetting item;
    public NumberSetting health;
    public BooleanSetting elytraCheck;
    public NumberSetting fallDistance;
    public BooleanSetting useGapple;
    public BooleanSetting gappleSword;
    public BooleanSetting gapplePickaxe;
    public BooleanSetting gappleTotem;
    public BooleanSetting lethalOverride;
    public BooleanSetting version117;
    public BooleanSetting smartMine;
    public BooleanSetting antiMace;
    public NumberSetting maceRange;
    private int totemCount;
    private int gappleStagedSlot;

    public ServerAutoTotem() {
        super("AutoTotem");
        var tmp0 = new String[3];
        tmp0[0] = "Totem";
        tmp0[1] = "Crystal";
        tmp0[2] = "Gapple";
        this.item = new ModeSetting("Item", "The item that will be placed in your offhand slot when safety conditions are met.", "Totem", tmp0);
        this.health = new NumberSetting("Health", "The health at which a totem will be prioritized.", 16, 0, 36);
        this.elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
        this.fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", 20.0f, 0.0f, 80.0f);
        this.useGapple = new BooleanSetting("UseGapple", "Switches to a golden apple in your offhand based on main hand items when health is safe.", true);
        this.gappleSword = new BooleanSetting("Sword", "Switches to a gapple when holding a sword or axe in main hand.", true);
        this.gapplePickaxe = new BooleanSetting("Pickaxe", "Switches to a gapple when holding a pickaxe in main hand.", true);
        this.gappleTotem = new BooleanSetting("Totem", "Switches to a gapple when holding a totem in main hand.", false);
        this.lethalOverride = new BooleanSetting("LethalOverride", "Overrides any necessity for a totem when gappling.", false);
        this.version117 = new BooleanSetting("1.17+", "1.17+ uses the SWAP inventory action, while having it off uses inventory clicks.", true);
        this.smartMine = new BooleanSetting("SmartMine", "Switches to a crystal whenever you start mining and a totem when you aren't mining.", false);
        this.antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
        this.maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", 12.0f, 0.0f, 24.0f);
        this.totemCount = 0;
        this.gappleStagedSlot = -1;
    }

    public void onEnable() {
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    public void onDisable() {
        LightningClient.EVENT_HANDLER.unsubscribe(this);
        this.gappleStagedSlot = -1;
    }

    public List getSettings() {
        var tmp0 = new Setting[13];
        tmp0[0] = this.item;
        tmp0[1] = this.health;
        tmp0[2] = this.elytraCheck;
        tmp0[3] = this.fallDistance;
        tmp0[4] = this.useGapple;
        tmp0[5] = this.gappleSword;
        tmp0[6] = this.gapplePickaxe;
        tmp0[7] = this.gappleTotem;
        tmp0[8] = this.lethalOverride;
        tmp0[9] = this.version117;
        tmp0[10] = this.smartMine;
        tmp0[11] = this.antiMace;
        tmp0[12] = this.maceRange;
        return List.of(tmp0);
    }

    @SubscribeEvent(priority = 2147483647)
    public void onPlayerPop(PlayerPopEvent arg0) {
        if (arg0.getPlayer() == mc.player && !(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            tick();
        }
    }

    public void tick() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        this.totemCount = mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
        if (!(mc.gui.screen() instanceof InventoryScreen) && (mc.gui.screen() instanceof AbstractContainerScreen)) return;
        Object var2 = getItem();
        if (var2 == null) return;
        if (var2 == Items.TOTEM_OF_UNDYING && ((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled() && ((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).offhandOverride.getValue()) {
            if (mc.player.getOffhandItem().isEmpty()) return;
            int var1 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            break label_15;
        }
        if (mc.player.getOffhandItem().getItem() == var2) return;
        if (this.gappleStagedSlot != -1 && var2 == Items.TOTEM_OF_UNDYING) {
            if (mc.player.getInventory().getItem(this.gappleStagedSlot).getItem() == Items.TOTEM_OF_UNDYING) {
                var1 = this.gappleStagedSlot;
                break label_8;
            }
            var1 = InventoryUtils.findInventory(var2);
            if (var1 != -1) break label_8;
            var1 = InventoryUtils.find(var2);
            label_8:
            this.gappleStagedSlot = -1;
            break label_13;
        }
        if (var2 != Items.ENCHANTED_GOLDEN_APPLE) {
            if (var2 == Items.GOLDEN_APPLE) {
            }
            var1 = InventoryUtils.findHotbar(var2);
            if (var1 == -1) {
                var1 = InventoryUtils.findInventory(var2);
            }
            if (var1 == -1) break label_13;
            this.gappleStagedSlot = var1;
            break label_13;
        }
        var1 = InventoryUtils.findInventory(var2);
        if (var1 != -1) break label_13;
        var1 = InventoryUtils.find(var2);
        label_13:
        if (var1 != -1) break label_15;
        if (var2 == Items.TOTEM_OF_UNDYING) {
            var1 = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            break label_15;
        }
        return;
        label_15:
        if (var1 == -1) return;
        if (this.version117.getValue()) {
            InventoryUtils.swap("Swap", var1, 40);
        } else {
            InventoryUtils.swap("Pickup", var1, 45);
        }
    }

    private Item getItem() {
        int var2 = hasItem(Items.TOTEM_OF_UNDYING);
        if (hasItem(Items.ENCHANTED_GOLDEN_APPLE)) {
            break label_2;
        }
        Object var3 = (hasItem(Items.GOLDEN_APPLE) ? Items.GOLDEN_APPLE : null);
        int var4 = (var3 != null ? 1 : 0);
        if (!(mc.player.getMainHandItem().is(ItemTags.SWORDS))) {
            if ((mc.player.getMainHandItem().getItem() instanceof AxeItem)) {
            }
        } else {
        }
        int var5 = 0;
        int var6 = mc.player.getMainHandItem().is(ItemTags.PICKAXES);
        int var7 = mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING);
        if (this.gappleSword.getValue()) {
            if (var5) break label_10;
        }
        if (this.gapplePickaxe.getValue()) {
            if (var6) break label_10;
        }
        if (this.gappleTotem.getValue() && !(!var7)) {
            label_10:
        } else {
        }
        int var1 = 0;
        int var8 = 0;
        if (this.useGapple.getValue() && mc.options.keyUse.isDown() && !(!var1) && !(!var4)) {
            if (((this.lethalOverride.getValue()) || (!(needsTotem()))) || (!var2)) {
                return var3;
            }
        }
        if (!(hasItem(Items.TOTEM_OF_UNDYING))) break label_18;
        if (needsTotem()) return Items.TOTEM_OF_UNDYING;
        if ((!(this.item.getValue().equalsIgnoreCase("Crystal"))) || (!(this.smartMine.getValue()))) break label_18;
        var tmp0 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        Object var9 = tmp0;
        if (tmp0.getPrimary() != null) {
            if (var9.getPrimary().isMining()) break label_18;
        }
        if ((var9.getSecondary() == null) || (!(var9.getSecondary().isMining()))) {
            return Items.TOTEM_OF_UNDYING;
        }
        label_18:
        var9 = this.item.getValue();
        int var10 = -1;
        switch (var9.hashCode()) {
            case -1582753002 -> { break label_19; }
            case 2125698931 -> { break label_20; }
            default -> { break label_21; }
        }
        label_19:
        if (!(var9.equals("Crystal"))) break label_21;
        var10 = 0;
        break label_21;
        label_20:
        if (!(var9.equals("Gapple"))) break label_21;
        var10 = 1;
        label_21:
        switch (var10) {
            case 0 -> { break label_22; }
            case 1 -> { break label_24; }
            default -> { break label_26; }
        }
        label_22:
        if (!(hasItem(Items.END_CRYSTAL))) return Items.TOTEM_OF_UNDYING;
        return Items.END_CRYSTAL;
        label_24:
        if (!var4) return Items.TOTEM_OF_UNDYING;
        return var3;
        label_26:
        return Items.TOTEM_OF_UNDYING;
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
        if (InventoryUtils.find(arg0) == -1) {
            if (mc.player.getOffhandItem().getItem() == arg0) {
            }
        } else {
        }
        return false;
    }

    public String getMetaData() {
        return String.valueOf(this.totemCount);
    }

}
