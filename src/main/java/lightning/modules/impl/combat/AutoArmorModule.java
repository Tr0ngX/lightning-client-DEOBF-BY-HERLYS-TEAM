package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

@RegisterModule(name = "AutoArmor", description = "Automatically equips the best armor.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class AutoArmorModule extends Module {
    public ModeSetting health;
    public BooleanSetting elytraPriority;
    public BooleanSetting preserve;
    public NumberSetting preserveHealth;
    public NumberSetting safeRange;
    public BooleanSetting elytra;
    public BindSetting elytraBind;
    public BooleanSetting smartElytra;
    public ModeSetting headMode;
    public ModeSetting chestMode;
    public ModeSetting legsMode;
    public ModeSetting feetMode;
    private int ticks;

    public AutoArmorModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Highest";
        tmp0[1] = "Lowest";
        tmp0[2] = "Any";
        this.health = new ModeSetting("Health", "The health priority to apply.", "Highest", tmp0);
        this.elytraPriority = new BooleanSetting("ElytraPriority", "Prioritizes elytra over armor pieces.", true);
        this.preserve = new BooleanSetting("Preserve", "Preserve low health armor to avoid it from breaking.", false);
        this.preserveHealth = new NumberSetting("PreserveHealth", "The minimum health of armor to preserve it.", new BooleanSetting.Visibility(this.preserve, true), 20.0f, 10.0f, 50.0f);
        this.safeRange = new NumberSetting("SafeRange", "Range to check for nearby players before re-equipping preserved armor.", new BooleanSetting.Visibility(this.preserve, true), 0, 0, 10);
        this.elytra = new BooleanSetting("Elytra", "Equips an elytra instead of a chestplate.", false);
        this.elytraBind = new BindSetting("ElytraBind", "Keybind that toggles Elytra on/off.", 0).disableHoldModes();
        this.smartElytra = new BooleanSetting("SmartElytra", "Chooses when to enable elytra in a more convenient way.", false);
        var tmp1 = new String[2];
        tmp1[0] = "Blast";
        tmp1[1] = "Prot";
        this.headMode = new ModeSetting("HeadMode", "Which protection enchant Head armor is ranked by.", "Prot", tmp1);
        var tmp2 = new String[2];
        tmp2[0] = "Blast";
        tmp2[1] = "Prot";
        this.chestMode = new ModeSetting("ChestMode", "Which protection enchant Chest armor is ranked by.", "Prot", tmp2);
        var tmp3 = new String[2];
        tmp3[0] = "Blast";
        tmp3[1] = "Prot";
        this.legsMode = new ModeSetting("LegsMode", "Which protection enchant Legs armor is ranked by.", "Prot", tmp3);
        var tmp4 = new String[2];
        tmp4[0] = "Blast";
        tmp4[1] = "Prot";
        this.feetMode = new ModeSetting("FeetMode", "Which protection enchant Feet armor is ranked by.", "Prot", tmp4);
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onElytraBind(KeyInputEvent arg0) {
        if (((shouldRunOnProxy()) || (getNull())) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        if (this.elytraBind.getValue() && arg0.getKey() == this.elytraBind.getValue() && this.elytraBind.getMode().equals("Bind")) {
            this.elytra.setValue(!(this.elytra.getValue()));
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent arg0) {
        if (shouldRunOnProxy()) return;
        if ((getNull()) || (!(this.smartElytra.getValue()))) {
            return;
        }
        if (arg0.getKey() == 32 && !(this.elytra.getValue()) && !(mc.player.onGround()) && !(EntityUtils.isInWeb(mc.player))) {
            this.elytra.setValue(true);
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if ((!(this.smartElytra.getValue())) || (!(this.elytra.getValue()))) break label_5;
        if (mc.player.onGround()) {
            if ((mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem)) {
            }
            if (!(EntityUtils.isInWeb(mc.player))) break label_5;
        }
        this.elytra.setValue(false);
        label_5:
        if (this.ticks <= 0) {
            if (InventoryUtils.inInventoryScreen()) return;
            update(EquipmentSlot.HEAD, 5);
            if (this.elytra.getValue() && this.elytraPriority.getValue()) {
                if (mc.player.getInventory().getItem(38).getItem() != Items.ELYTRA) {
                }
                update(EquipmentSlot.CHEST, 6);
            }
            update(EquipmentSlot.LEGS, 7);
            update(EquipmentSlot.FEET, 8);
        }
        this.ticks = (this.ticks - 1);
    }

    private boolean isTierDiamondOrNetherite(ItemStack arg0) {
        Object var2 = arg0.getItem().toString().toLowerCase();
        if (!(var2.contains("diamond"))) {
            if (var2.contains("netherite")) {
            }
        } else {
        }
        return false;
    }

    private boolean isValidCombatArmor(ItemStack arg0, String arg1) {
        if (!(isArmor(arg0))) return false;
        if (!(isTierDiamondOrNetherite(arg0))) {
            if (getEnchantLevel(arg0, arg1) > 0) {
            }
        } else {
        }
        return false;
    }

    private void update(EquipmentSlot arg0, int arg1) {
        int var5 = findElytra();
        if (!(this.elytra.getValue())) break label_0;
        var tmp0 = (arg0 == EquipmentSlot.CHEST ? 1 : 0);
        int var4 = tmp0;
        int var6 = tmp0;
        if (arg0 == EquipmentSlot.HEAD) {
            break label_5;
        }
        if (arg0 == EquipmentSlot.CHEST) {
            break label_5;
        }
        int var7 = (arg0 == EquipmentSlot.LEGS ? 37 : 36);
        if (!(!var4)) {
        } else {
        }
        int var8 = findArmor(arg0);
        if (!(!var4)) {
        } else {
        }
        var tmp1 = compare(var7, var8, true, arg0);
        int var3 = tmp1;
        int var9 = tmp1;
        if (var8 != -1 && var3 != var7) {
            mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, arg1, 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, InventoryUtils.indexToSlot(var8), 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, arg1, 0, ContainerInput.PICKUP, mc.player);
            this.ticks = (2 + LightningClient.SERVER_MANAGER.getPingDelay());
        }
    }

    private int compare(int arg0, int arg1, boolean arg2, EquipmentSlot arg3) {
        if (arg1 == -1) return arg0;
        if (!(isArmor(mc.player.getInventory().getItem(arg0)))) return arg1;
        switch (AutoArmorModule.1.$SwitchMap$net$minecraft$world$entity$EquipmentSlot[arg3.ordinal()]) {
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            default -> { break label_5; }
        }
        label_2:
        break label_6;
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        Object var5 = this.feetMode.getValue();
        var5 = (var5.equalsIgnoreCase("Blast") ? "blast_protection" : "protection");
        Object var6 = mc.player.getInventory().getItem(arg0);
        Object var7 = mc.player.getInventory().getItem(arg1);
        if (getEnchantLevel(var6, var5) < getEnchantLevel(var7, var5)) return arg1;
        if (!(this.preserve.getValue())) break label_13;
        int var8 = isSafe();
        if (!(!var8)) {
            if (((getDurability(arg1) >= this.preserveHealth.getValue().floatValue()) || (getDurability(arg0) < this.preserveHealth.getValue().floatValue())) || (!(isValidCombatArmor(var7, var5)))) break label_13;
            return arg1;
        }
        if (getDurability(arg0) < this.preserveHealth.getValue().floatValue() && isValidCombatArmor(var7, var5)) return (getDurability(arg0) < getDurability(arg1) ? arg1 : arg0);
        label_13:
        if (!arg2) {
            if (this.health.getValue().equals("Highest") && getDurability(arg0) < getDurability(arg1)) return arg1;
            if (this.health.getValue().equals("Lowest") && getDurability(arg0) > getDurability(arg1)) return arg1;
        }
        return arg0;
    }

    private boolean isSafe() {
        if (this.safeRange.getValue().doubleValue() <= 0.0) return false;
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        double var1 = (this.safeRange.getValue().doubleValue() * this.safeRange.getValue().doubleValue());
        Object var3 = mc.level.players().iterator();
        label_3:
        if (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if (((var4 == mc.player) || (var4.isSpectator())) || (!(var4.isAlive()))) break label_3;
            if (LightningClient.FRIEND_MANAGER != null) {
                if (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString())) break label_3;
            }
            if (mc.player.distanceToSqr(var4) > var1) {
                break label_3;
            }
            return false;
        }
        return true;
    }

    private int compareElytra(int arg0, int arg1) {
        if (arg1 == -1) return arg0;
        if (mc.player.getInventory().getItem(arg0).getItem() != Items.ELYTRA) return arg1;
        if (this.health.getValue().equals("Highest") && getDurability(arg0) < getDurability(arg1)) return arg1;
        if (this.health.getValue().equals("Lowest") && getDurability(arg0) > getDurability(arg1)) return arg1;
        return arg0;
    }

    private int findArmor(EquipmentSlot arg0) {
        int var2 = -1;
        int var3 = InventoryUtils.HOTBAR_START;
        while (var3 <= InventoryUtils.INVENTORY_END) {
            Object var4 = mc.player.getInventory().getItem(var3);
            if (!(isArmor(var4))) break label_2;
            if (!(getSlotType(var4).equals(arg0))) {
                break label_2;
            }
            var2 = compare(var3, var2, false, arg0);
            label_2:
            var3++;
        }
        return var2;
    }

    private int findElytra() {
        int var1 = -1;
        int var2 = InventoryUtils.HOTBAR_START;
        while (var2 <= InventoryUtils.INVENTORY_END) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if (var3.getItem() != Items.ELYTRA) {
            } else {
                var1 = compareElytra(var2, var1);
            }
            var2++;
        }
        return var1;
    }

    private int getEnchantLevel(ItemStack arg0, String arg1) {
        Object var3 = arg0.getEnchantments().keySet().iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Holder) var3.next());
            if (!(var4.getRegisteredName().replace("minecraft:", "").equals(arg1))) {
                break label_0;
            }
            return arg0.getEnchantments().getLevel(var4);
        }
        return 0;
    }

    private float getDurability(int arg0) {
        Object var2 = mc.player.getInventory().getItem(arg0);
        return ((((float) (var2.getMaxDamage() - var2.getDamageValue())) * 100.0f) / ((float) var2.getMaxDamage()));
    }

    private EquipmentSlot getSlotType(ItemStack arg0) {
        if (arg0.has(DataComponents.GLIDER)) return EquipmentSlot.CHEST;
        return ((Equippable) arg0.get(DataComponents.EQUIPPABLE)).slot();
    }

    private boolean isArmor(ItemStack arg0) {
        if (arg0.getItem() == Items.ELYTRA) return false;
        int var2 = ((ItemAttributeModifiers) arg0.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)).modifiers().stream().anyMatch((arg0) -> arg0.attribute().is(Attributes.ARMOR));
        if (!var2) return false;
        Object var3 = ((Equippable) arg0.get(DataComponents.EQUIPPABLE));
        if (var3 == null) break label_2;
        return var3.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }


    class 1 {

        static {
            $SwitchMap$net$minecraft$world$entity$EquipmentSlot = new int[EquipmentSlot.values().length];
            try {
                $SwitchMap$net$minecraft$world$entity$EquipmentSlot[EquipmentSlot.HEAD.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$world$entity$EquipmentSlot[EquipmentSlot.CHEST.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$net$minecraft$world$entity$EquipmentSlot[EquipmentSlot.LEGS.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
            }
        }

    }
}
