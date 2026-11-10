package lightning.modules.impl.player;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.SprintModule;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@RegisterModule(name = "KeyAction", description = "Keybinds for throwing fireworks, pearls and experience bottles.", category = Module.Category.PLAYER, persistent = true, proxyEnhanced = true)
public class KeyActionModule extends Module {
    public CategorySetting fireworkCategory;
    public BindSetting fireworkBind;
    public ModeSetting fireworkSwitch;
    public CategorySetting pearlsCategory;
    public BindSetting pearlsBind;
    public ModeSetting pearlsSwitch;
    public BooleanSetting pearlsRotate;
    public CategorySetting xpCategory;
    public BindSetting xpBind;
    public ModeSetting xpSwitch;
    public BooleanSetting xpRotate;
    public NumberSetting xpDelay;
    public NumberSetting xpRepeat;
    public ModeSetting xpAntiWaste;
    public BooleanSetting xpItemDisable;
    private boolean xpActive;
    private int xpTicks;
    private long lastPearlTime;
    public BooleanSetting xpFullRepair;

    public KeyActionModule() {
        this.fireworkCategory = new CategorySetting("FireWork", "Keybind and settings for throwing a firework.");
        this.fireworkBind = new BindSetting("FireWorkBind", "Bind", "The key that throws a firework. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.", new CategorySetting.Visibility(this.fireworkCategory), 0);
        this.fireworkSwitch = new ModeSetting("FireWorkSwitch", "Switch", "The mode that will be used for automatically switching to fireworks.", new CategorySetting.Visibility(this.fireworkCategory), "Silent", InventoryUtils.SWITCH_MODES);
        this.pearlsCategory = new CategorySetting("Pearls", "Keybind and settings for throwing an ender pearl.");
        this.pearlsBind = new BindSetting("PearlsBind", "Bind", "The key that throws an ender pearl. Hold/ReverseHold repeat it every tick the bind is (or isn't) held.", new CategorySetting.Visibility(this.pearlsCategory), 0);
        this.pearlsSwitch = new ModeSetting("PearlsSwitch", "Switch", "The mode that will be used for automatically switching to pearls.", new CategorySetting.Visibility(this.pearlsCategory), "Silent", InventoryUtils.SWITCH_MODES);
        this.pearlsRotate = new BooleanSetting("PearlsRotate", "Rotate", "Sends a packet rotation right before throwing the pearl.", new CategorySetting.Visibility(this.pearlsCategory), true);
        this.xpCategory = new CategorySetting("XP", "Keybind and settings for throwing experience bottles.");
        this.xpBind = new BindSetting("XPBind", "Bind", "The key that starts/stops throwing experience bottles. Hold/ReverseHold keep the loop matched to whether the bind is (or isn't) held instead of toggling it.", new CategorySetting.Visibility(this.xpCategory), 0);
        this.xpSwitch = new ModeSetting("XPSwitch", "Switch", "The mode that will be used for automatically switching to experience bottles.", new CategorySetting.Visibility(this.xpCategory), "Silent", InventoryUtils.SWITCH_MODES);
        this.xpRotate = new BooleanSetting("XPRotate", "Rotate", "Rotates down to your feet when throwing experience bottles.", new CategorySetting.Visibility(this.xpCategory), true);
        this.xpDelay = new NumberSetting("XPDelay", "Delay", "The delay in ticks between throwing experience bottles.", new CategorySetting.Visibility(this.xpCategory), 1, 0, 20);
        this.xpRepeat = new NumberSetting("XPRepeat", "Repeat", "Allows you to throw a lot more XP bottles at once.", new CategorySetting.Visibility(this.xpCategory), 1, 1, 15);
        this.xpFullRepair = new BooleanSetting("XPFullRepair", "FullRepair", "One press repairs all armor to 100%: throws as many bottles as needed per burst and stops automatically when armor is full.", new CategorySetting.Visibility(this.xpCategory), true);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Avoid";
        tmp0[2] = "Disable";
        this.xpAntiWaste = new ModeSetting("XPAntiWaste", "AntiWaste", "How wasting of experience bottles should be prevented.", new CategorySetting.Visibility(this.xpCategory), "Avoid", tmp0);
        this.xpItemDisable = new BooleanSetting("XPItemDisable", "ItemDisable", "Automatically stops the XP loop when you run out of XP.", new CategorySetting.Visibility(this.xpCategory), true);
        this.xpActive = false;
        this.xpTicks = 0;
        this.lastPearlTime = 0L;
    }

    public boolean isXpActive() {
        return this.xpActive;
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent arg0) {
        if (((shouldRunOnProxy()) || (getNull())) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        if (this.fireworkBind.getValue() && arg0.getKey() == this.fireworkBind.getValue() && this.fireworkBind.getMode().equals("Bind")) {
            throwFirework();
        }
        if (this.pearlsBind.getValue() && arg0.getKey() == this.pearlsBind.getValue() && this.pearlsBind.getMode().equals("Bind")) {
            throwPearl();
        }
        if (this.xpBind.getValue() && arg0.getKey() == this.xpBind.getValue() && this.xpBind.getMode().equals("Bind")) {
            this.xpActive = !this.xpActive;
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent arg0) {
        if (((shouldRunOnProxy()) || (getNull())) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        int var2 = (-(arg0.getButton()) - 1);
        if (this.fireworkBind.getValue() && var2 == this.fireworkBind.getValue() && this.fireworkBind.getMode().equals("Bind")) {
            throwFirework();
        }
        if (this.pearlsBind.getValue() && var2 == this.pearlsBind.getValue() && this.pearlsBind.getMode().equals("Bind")) {
            throwPearl();
        }
        if (this.xpBind.getValue() && var2 == this.xpBind.getValue() && this.xpBind.getMode().equals("Bind")) {
            this.xpActive = !this.xpActive;
        }
    }

    private void throwFirework() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.fireworkSwitch.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.FIREWORK_ROCKET) {
            LightningClient.CHAT_MANAGER.tagged("You are currently not holding any fireworks.", getName());
            return;
        }
        if (mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.FIREWORK_ROCKET))) return;
        if (!(this.fireworkSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.fireworkSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var1 = InventoryUtils.find(0, 35, 8);
        int var2 = mc.player.getInventory().getSelectedSlot();
        if (var1 == -1) {
            LightningClient.CHAT_MANAGER.tagged("No fireworks could be found in your hotbar.", getName());
            return;
        }
        InventoryUtils.switchSlot(this.fireworkSwitch.getValue(), var1, var2);
        NetworkUtils.sendSequencedPacket((arg0) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg0, mc.player.getYRot(), mc.player.getXRot()));
        InventoryUtils.switchBack(this.fireworkSwitch.getValue(), var1, var2);
    }

    public boolean isPearlActive() {
        return (System.currentTimeMillis() - this.lastPearlTime) < 100L;
    }

    private void throwPearl() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.pearlsSwitch.getValue().equalsIgnoreCase("None") && mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            LightningClient.CHAT_MANAGER.tagged("You are currently not holding any pearls.", getName());
            return;
        }
        if (mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.ENDER_PEARL))) return;
        this.lastPearlTime = System.currentTimeMillis();
        Object var1 = ((MultiTaskModule) LightningClient.MODULE_MANAGER.getModule(MultiTaskModule.class));
        if (((var1 == null) || (!(var1.isToggled()))) || (!(var1.pearl.getValue()))) break label_4;
        int var2 = (mc.player.isUsingItem() ? 1 : 0);
        if (!(!var2)) {
        } else {
        }
        Object var3 = this.pearlsSwitch.getValue();
        if (!(var3.equalsIgnoreCase("AltSwap"))) {
            if (var3.equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var4 = InventoryUtils.find(0, 35, 8);
        int var5 = mc.player.getInventory().getSelectedSlot();
        if (var4 == -1) {
            LightningClient.CHAT_MANAGER.tagged("No pearls could be found in your hotbar.", getName());
            return;
        }
        if (this.pearlsRotate.getValue()) {
            LightningClient.ROTATION_MANAGER.packetRotate(mc.player.getYRot(), mc.player.getXRot());
        }
        InventoryUtils.switchSlot(var3, var4, var5);
        NetworkUtils.sendSequencedPacket((arg0) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg0, mc.player.getYRot(), mc.player.getXRot()), this::serverSend);
        serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(var3, var4, var5);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (((shouldRunOnProxy()) || (getNull())) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        checkHoldBind(this.fireworkBind, this::throwFirework);
        checkHoldBind(this.pearlsBind, this::throwPearl);
        if (this.xpBind.getValue() && !(this.xpBind.getMode().equals("Bind"))) {
            int var2 = KeyboardUtils.isBindDown(this.xpBind.getValue());
            this.xpActive = this.xpBind.getMode().equals("Hold") == var2;
        }
        tickXp();
    }

    private void checkHoldBind(BindSetting arg0, Runnable arg1) {
        if ((!(arg0.getValue())) || (arg0.getMode().equals("Bind"))) {
            return;
        }
        int var3 = KeyboardUtils.isBindDown(arg0.getValue());
        if (arg0.getMode().equals("Hold") == var3) {
            arg1.run();
        }
    }

    private void tickXp() {
        if (!this.xpActive) return;
        if (shouldRunOnProxy()) return;
        if (mc.player == null) break label_2;
        if (mc.level == null) {
            label_2:
            return;
        }
        if (this.xpSwitch.getValue().equalsIgnoreCase("None") && !(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            LightningClient.CHAT_MANAGER.tagged("You are currently not holding any experience bottles.", getName());
            this.xpActive = false;
            return;
        }
        if (this.xpTicks < xpDelayInt()) {
            this.xpTicks = (this.xpTicks + 1);
            return;
        }
        if (!(needsExperience()) && !(xpAwValue().equals("None"))) {
            if (xpAwValue().equals("Disable")) {
                this.xpActive = false;
            }
            return;
        }
        if (!(this.xpSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.xpSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var3 = InventoryUtils.find(0, 35, 8);
        int var4 = mc.player.getInventory().getSelectedSlot();
        if (var3 == -1) {
            LightningClient.CHAT_MANAGER.tagged("No experience bottles could be found in your hotbar.", getName());
            this.xpActive = false;
            return;
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var5 = null;
        if (var2 != null && var2.isToggled() && var2.isGrimCompensating() && var2.getPendingYaw() != null) {
            break label_16;
        }
        if (LightningClient.ROTATION_MANAGER != null && LightningClient.ROTATION_MANAGER.getRotation() != null) {
            break label_16;
        }
        label_16:
        float var6 = mc.player.getYRot();
        if (this.xpRotate.getValue()) {
        } else {
        }
        var tmp0 = mc.player.getXRot();
        float var1 = tmp0;
        float var7 = tmp0;
        if (this.xpRotate.getValue() && LightningClient.ROTATION_MANAGER != null) {
            LightningClient.ROTATION_MANAGER.silentRotate(var6, var1);
        }
        InventoryUtils.switchSlot(this.xpSwitch.getValue(), var3, var4);
        int var8 = 0;
        while (var8 < xpBurst()) {
            NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg2, arg0, arg1), this::serverSend);
            var8++;
        }
        serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        InventoryUtils.switchBack(this.xpSwitch.getValue(), var3, var4);
        this.xpTicks = 0;
    }

    private void serverSend(Packet arg0) {
        if ((!(isRunningOnProxy())) || (LightningClient.PROXY_SERVER == null)) break label_0;
        var tmp0 = LightningClient.PROXY_SERVER.getServerConnection();
        Object var2 = tmp0;
        if ((tmp0 == null) || (!(var2.isConnected()))) break label_0;
        ProxyServerTickListener.allowSend(() -> arg0.send(arg1));
        return;
        label_0:
        mc.getConnection().send(arg0);
    }

    private boolean needsExperience() {
        var tmp0 = new EquipmentSlot[4];
        tmp0[0] = EquipmentSlot.FEET;
        tmp0[1] = EquipmentSlot.LEGS;
        tmp0[2] = EquipmentSlot.CHEST;
        tmp0[3] = EquipmentSlot.HEAD;
        Object var1 = tmp0;
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Object var4 = var1[var3];
            Object var5 = mc.player.getItemBySlot(var4);
            if (var5.isEmpty()) break label_3;
            if (!(var5.is(ItemTags.FOOT_ARMOR)) && !(var5.is(ItemTags.LEG_ARMOR)) && !(var5.is(ItemTags.CHEST_ARMOR))) {
                if (!(var5.is(ItemTags.HEAD_ARMOR))) break label_3;
            }
            if (((float) Math.round(((((float) (var5.getMaxDamage() - var5.getDamageValue())) * 100.0f) / ((float) var5.getMaxDamage())))) >= 100.0f) {
                break label_3;
            }
            return true;
            label_3:
            var3++;
        }
        return false;
    }

    public void onDisable() {
        this.xpActive = false;
        this.xpTicks = 0;
    }

    private int xpDelayInt() {
        int var1 = this.xpDelay.getValue().intValue();
        if (this.xpFullRepair.getValue()) {
            var1 = Math.max(var1, 5);
        }
        return var1;
    }

    private String xpAwValue() {
        if (this.xpFullRepair.getValue()) return "Disable";
        return this.xpAntiWaste.getValue();
    }

    private int xpMissing() {
        return (((mc.player.getItemBySlot(EquipmentSlot.FEET).getDamageValue() + mc.player.getItemBySlot(EquipmentSlot.LEGS).getDamageValue()) + mc.player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()) + mc.player.getItemBySlot(EquipmentSlot.HEAD).getDamageValue());
    }

    private int xpBurst() {
        if (!(this.xpFullRepair.getValue())) return this.xpRepeat.getValue().intValue();
        return Math.min(Math.max(((xpMissing() + 15) / 16), 1), 32);
    }

}
