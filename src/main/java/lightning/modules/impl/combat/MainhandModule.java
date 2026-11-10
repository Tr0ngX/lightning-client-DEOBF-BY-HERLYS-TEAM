package lightning.modules.impl.combat;

import java.util.ArrayDeque;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

@RegisterModule(name = "Mainhand", description = "Automatically manages totems and golden apples in your mainhand.", category = Module.Category.COMBAT)
public class MainhandModule extends Module {
    public NumberSetting slot;
    public NumberSetting delay;
    public ModeSetting refillMode;
    public NumberSetting popsBeforeRefill;
    public BooleanSetting useGapple;
    public BooleanSetting lethalOverride;
    public NumberSetting health;
    public BooleanSetting elytraCheck;
    public NumberSetting fallDistance;
    public BooleanSetting antiMace;
    public NumberSetting maceRange;
    private long pendingRefillTime;
    private static final int POP_WINDOW_UPDATES = 8;
    private static final int MAX_POP_CANDIDATES = 32;
    private final ArrayDeque popCandidates;
    private final boolean[] observedTotems;
    private int updateSequence;
    private int confirmedPopDebt;
    private int refillSource;
    private int refillTarget;
    private int refillAttemptUpdate;
    private String observedMode;
    private Object observedPlayer;
    private Object observedWorld;
    private String pendingReason;
    private boolean eatingHotbar;
    private int previousHotbarSlot;
    private boolean eatingInventory;
    private int swappedInvSlot;
    private int swappedHotbarSlot;
    public static volatile boolean hidingLegitInventory;

    public MainhandModule() {
        this.slot = new NumberSetting("Slot", "The hotbar slot (1-9) to keep a totem in.", 1, 1, 9);
        this.delay = new NumberSetting("Delay", "Delay (ms) before refilling a totem to hotbar.", 0, 0, 2000);
        var tmp0 = new String[2];
        tmp0[0] = "Immediate";
        tmp0[1] = "After Pops";
        this.refillMode = new ModeSetting("RefillMode", "Refill Mode", "When to refill the configured hotbar slot.", "Immediate", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "After Pops";
        this.popsBeforeRefill = new NumberSetting("PopsBeforeRefill", "Pops Before Refill", "Confirmed main-hand Totem pops per inventory refill.", new ModeSetting.Visibility(this.refillMode, tmp1), 4, 1, 9);
        this.useGapple = new BooleanSetting("UseGapple", "Switches to golden apple when right clicking while holding a totem in mainhand.", true);
        this.lethalOverride = new BooleanSetting("LethalOverride", "Allows eating a golden apple regardless of health when no enemies are in visual range.", new BooleanSetting.Visibility(this.useGapple, true), false);
        this.health = new NumberSetting("Health", "The health at which a totem will be prioritized.", 16, 0, 36);
        this.elytraCheck = new BooleanSetting("ElytraCheck", "Prioritizes a totem whenever you're wearing an elytra.", true);
        this.fallDistance = new NumberSetting("FallDistance", "The fall distance at which the module will prioritize a totem.", 20.0f, 0.0f, 80.0f);
        this.antiMace = new BooleanSetting("AntiMace", "Switches to a totem if a player near you is trying to smash attack you with a mace.", false);
        this.maceRange = new NumberSetting("MaceRange", "The distance at which an enemy has to be in with a mace in order to swap to a totem.", new BooleanSetting.Visibility(this.antiMace, true), 12.0f, 0.0f, 24.0f);
        this.pendingRefillTime = 0L;
        this.popCandidates = new ArrayDeque();
        this.observedTotems = new boolean[9];
        this.refillSource = -1;
        this.refillTarget = -1;
        this.eatingHotbar = false;
        this.previousHotbarSlot = -1;
        this.eatingInventory = false;
        this.swappedInvSlot = -1;
        this.swappedHotbarSlot = -1;
    }

    public void onEnable() {
        this.pendingRefillTime = 0L;
        resetAfterPops("enable");
        this.observedMode = this.refillMode.getValue();
    }

    private boolean afterPops() {
        return "After Pops".equals(this.refillMode.getValue());
    }

    private void resetAfterPops(String arg0) {
        if (this.popCandidates.isEmpty() && !this.confirmedPopDebt && this.pendingRefillTime == 0L) {
            if (this.refillSource != -1) {
            }
            LightningClient.LOGGER.info("[Mainhand] RESET reason={}", arg0);
        }
        this.popCandidates.clear();
        this.confirmedPopDebt = 0;
        this.pendingRefillTime = 0L;
        this.refillSource = -1;
        this.refillTarget = -1;
        this.pendingReason = null;
        this.updateSequence = 0;
        int var2 = 0;
        while (var2 < this.observedTotems.length) {
            this.observedTotems[var2] = 0;
            var2++;
        }
    }

    private void observeModeAndSession() {
        Object var1 = this.refillMode.getValue();
        if (this.observedMode != null) {
            if (!(this.observedMode.equals(var1))) {
            }
            resetAfterPops("mode_change");
            this.observedMode = var1;
            LightningClient.LOGGER.info("[Mainhand] MODE mode={}", var1);
        }
        if (mc.player != null && mc.level != null) {
            if (mc.player.isDeadOrDying()) {
            }
            resetAfterPops("invalid_session");
            this.observedPlayer = mc.player;
            this.observedWorld = mc.level;
            return;
        }
        if (this.observedPlayer == mc.player) {
            if (this.observedWorld != mc.level) {
            }
            resetAfterPops("session_change");
            this.observedPlayer = mc.player;
            this.observedWorld = mc.level;
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        resetAfterPops("disconnect");
        this.observedPlayer = null;
        this.observedWorld = null;
    }

    @SubscribeEvent(priority = 2147483647)
    public void onPlayerPop(PlayerPopEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if ((arg0.getPlayer() != mc.player) || (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            return;
        }
        if (!(isToggled())) return;
        mc.execute(() -> {
    if ((mc.player == null) || (mc.level == null)) {
        return;
    }
    observeModeAndSession();
    if (afterPops()) {
        if (mc.player.isDeadOrDying()) return;
        int var1 = mc.player.getInventory().getSelectedSlot();
        if (var1 >= 0 && var1 <= 8 && mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
            if (!(mc.player.getInventory().getItem(var1).is(Items.TOTEM_OF_UNDYING))) {
            }
            LightningClient.LOGGER.info("[Mainhand] POP_REJECTED reason=offhand_or_no_mainhand");
            return;
        }
        if (this.popCandidates.size() == 32) {
            this.popCandidates.removeFirst();
        }
        this.popCandidates.addLast(new MainhandModule.PopCandidate(var1, this.updateSequence));
        this.observedTotems[var1] = 1;
        LightningClient.LOGGER.info("[Mainhand] POP_CANDIDATE slot={} age=0", (var1 + 1));
        return;
    }
    var1 = (this.slot.getValue().intValue() - 1);
    long var2 = this.delay.getValue().longValue();
    if (var2 <= 0L) {
        refill(var1);
    } else {
        this.pendingRefillTime = (System.currentTimeMillis() + var2);
    }
});
    }

    private void refill(int arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) return;
        Object var2 = mc.gui.screen();
        if ((var2 instanceof AbstractContainerScreen) && !(var2 instanceof InventoryScreen) && !(mc.player.containerMenu.getCarried().isEmpty())) return;
        if (mc.player.getInventory().getItem(arg0).getItem() == Items.TOTEM_OF_UNDYING) return;
        int var3 = findRefillTotem(arg0);
        if (var3 != -1) {
            InventoryUtils.swap("Swap", var3, arg0);
        }
    }

    public boolean legitOwnsTotems() {
        return isToggled();
    }

    @SubscribeEvent(priority = 2147483647)
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        observeModeAndSession();
        if (afterPops() && mc.player != null && mc.level != null && !(mc.player.isDeadOrDying())) {
            observeAfterPops();
        }
        updateMainhand();
    }

    private void observeAfterPops() {
        this.updateSequence = (this.updateSequence + 1);
        Object var1 = new boolean[9];
        int var2 = 0;
        while (var2 < var1.length) {
            var1[var2] = mc.player.getInventory().getItem(var2).is(Items.TOTEM_OF_UNDYING);
            var2++;
        }
        var2 = this.popCandidates.iterator();
        while (var2.hasNext()) {
            Object var3 = ((MainhandModule.PopCandidate) var2.next());
            if ((this.updateSequence - var3.createdUpdate) > 8) {
                var2.remove();
                LightningClient.LOGGER.info("[Mainhand] POP_REJECTED reason=timeout slot={}", (var3.slot + 1));
                break label_4;
            }
            if ((!(this.observedTotems[var3.slot])) || (var1[var3.slot])) break label_4;
            var2.remove();
            this.confirmedPopDebt = (this.confirmedPopDebt + 1);
            var tmp0 = new Object[3];
            tmp0[0] = (var3.slot + 1);
            tmp0[1] = this.confirmedPopDebt;
            tmp0[2] = this.popsBeforeRefill.getValue().intValue();
            LightningClient.LOGGER.info("[Mainhand] POP_CONFIRMED slot={} debt={} threshold={}", tmp0);
            this.observedTotems[var3.slot] = 0;
            label_4:
        }
        System.arraycopy(var1, 0, this.observedTotems, 0, var1.length);
    }

    private void logPending(String arg0) {
        if (!(arg0.equals(this.pendingReason))) {
            LightningClient.LOGGER.info("[Mainhand] REFILL_PENDING reason={}", arg0);
            this.pendingReason = arg0;
        }
    }

    private void updateAfterPopsRefill(int arg0, long arg1) {
        int var4 = this.popsBeforeRefill.getValue().intValue();
        if (this.refillSource != -1) {
            int var5 = mc.player.getInventory().getItem(this.refillTarget).is(Items.TOTEM_OF_UNDYING);
            int var6 = (!(mc.player.getInventory().getItem(this.refillSource).is(Items.TOTEM_OF_UNDYING)) ? 1 : 0);
            if (!(!var5) && !(!var6)) {
                this.confirmedPopDebt = Math.max(0, (this.confirmedPopDebt - var4));
                var tmp0 = new Object[3];
                tmp0[0] = this.refillSource;
                tmp0[1] = (this.refillTarget + 1);
                tmp0[2] = this.confirmedPopDebt;
                LightningClient.LOGGER.info("[Mainhand] REFILL_SUCCESS sourceSlot={} targetSlot={} remainingDebt={}", tmp0);
                this.refillSource = -1;
                this.refillTarget = -1;
                this.pendingRefillTime = 0L;
                this.pendingReason = null;
                return;
            }
            if ((this.updateSequence - this.refillAttemptUpdate) <= 8) return;
            this.refillSource = -1;
            this.refillTarget = -1;
            this.pendingRefillTime = 0L;
            logPending("swap_not_observed");
            return;
        }
        if (this.confirmedPopDebt < var4) {
            this.pendingRefillTime = 0L;
            this.pendingReason = null;
            return;
        }
        if (mc.player.getInventory().getItem(arg0).is(Items.TOTEM_OF_UNDYING)) {
            this.pendingRefillTime = 0L;
            logPending("target_still_totem");
            return;
        }
        if (this.pendingRefillTime != 0L) break label_7;
        var5 = this.delay.getValue().longValue();
        this.pendingRefillTime = (arg1 + Math.max(1L, var5));
        var tmp1 = new Object[3];
        tmp1[0] = this.confirmedPopDebt;
        tmp1[1] = var4;
        tmp1[2] = (arg0 + 1);
        LightningClient.LOGGER.info("[Mainhand] THRESHOLD_REACHED debt={} threshold={} targetSlot={}", tmp1);
        if (var5 <= 0L) break label_7;
        LightningClient.LOGGER.info("[Mainhand] REFILL_DELAY ms={}", var5);
        label_7:
        if ((arg1 < this.pendingRefillTime) || (PingBypassFlags.isPingBypassActive())) {
            return;
        }
        var5 = InventoryUtils.find(Items.TOTEM_OF_UNDYING, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
        if (var5 == -1) {
            logPending("no_inventory_totem");
            return;
        }
        this.pendingReason = null;
        this.refillSource = var5;
        this.refillTarget = arg0;
        this.refillAttemptUpdate = this.updateSequence;
        InventoryUtils.swap("Swap", var5, arg0);
    }

    private void updateMainhand() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (PingBypassFlags.isPingBypassActive()) return;
        Object var4 = mc.gui.screen();
        if (!((var4 instanceof AbstractContainerScreen))) break label_3;
        var tmp0 = (!((var4 instanceof InventoryScreen)) ? 1 : 0);
        int var3 = tmp0;
        int var5 = tmp0;
        if (!(!var3) && !(mc.player.containerMenu.getCarried().isEmpty())) return;
        if (!(mc.options.keyUse.isDown())) {
            if (mc.player.isUsingItem()) {
            }
        } else {
        }
        int var2 = 0;
        int var6 = 0;
        if (!(!this.eatingHotbar)) {
            if (!var2) {
                if (this.previousHotbarSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
                    ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
                }
                this.eatingHotbar = false;
                this.previousHotbarSlot = -1;
            }
            return;
        }
        if (!(!this.eatingInventory)) {
            if (!var2) {
                if (this.swappedInvSlot != -1 && this.swappedHotbarSlot != -1) {
                    InventoryUtils.swap("Swap", this.swappedInvSlot, this.swappedHotbarSlot);
                }
                this.eatingInventory = false;
                this.swappedInvSlot = -1;
                this.swappedHotbarSlot = -1;
            }
            return;
        }
        if (needsTotem()) {
            if (this.lethalOverride.getValue() && !(hasEnemyInVisualRange())) {
            }
        } else {
        }
        int var1 = 0;
        int var7 = 0;
        if ((((!(this.useGapple.getValue())) || (!(mc.options.keyUse.isDown()))) || (!(mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)))) || (!var1)) break label_19;
        int var8 = findGappleHotbar();
        if (var8 != -1) {
            this.previousHotbarSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(var8);
            ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
            this.eatingHotbar = true;
            return;
        }
        int var9 = findGappleInventory();
        if (var9 == -1) break label_19;
        this.swappedInvSlot = var9;
        this.swappedHotbarSlot = mc.player.getInventory().getSelectedSlot();
        InventoryUtils.swap("Swap", var9, this.swappedHotbarSlot);
        this.eatingInventory = true;
        return;
        label_19:
        var8 = (this.slot.getValue().intValue() - 1);
        var9 = System.currentTimeMillis();
        if (afterPops()) {
            updateAfterPopsRefill(var8, var9);
            break label_25;
        }
        if (this.pendingRefillTime > 0L) {
            if (var9 < this.pendingRefillTime) break label_25;
            this.pendingRefillTime = 0L;
            refill(var8);
            break label_25;
        }
        var tmp1 = (mc.player.getInventory().getItem(var8).getItem() != Items.TOTEM_OF_UNDYING ? 1 : 0);
        int var11 = tmp1;
        int var12 = tmp1;
        if (!var11) break label_25;
        long var13 = this.delay.getValue().longValue();
        if (var13 <= 0L) {
            refill(var8);
            break label_25;
        }
        this.pendingRefillTime = (var9 + var13);
        label_25:
        if (!(needsTotem())) break label_30;
        var12 = mc.player.getInventory().getSelectedSlot();
        var tmp2 = (mc.player.getInventory().getItem(var8).getItem() == Items.TOTEM_OF_UNDYING ? 1 : 0);
        var11 = tmp2;
        var13 = tmp2;
        if (var12 == var8) {
            if (var11) break label_30;
        }
        if (!(!var11)) {
            mc.player.getInventory().setSelectedSlot(var8);
            ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
            break label_30;
        }
        int var14 = InventoryUtils.find(Items.TOTEM_OF_UNDYING, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
        if ((var14 == -1) || (var12 == var14)) break label_30;
        mc.player.getInventory().setSelectedSlot(var14);
        ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
        label_30:
    }

    private int findRefillTotem(int arg0) {
        int var2 = InventoryUtils.findInventory(Items.TOTEM_OF_UNDYING);
        if (var2 != -1) return var2;
        int var3 = InventoryUtils.HOTBAR_START;
        while (var3 <= InventoryUtils.HOTBAR_END) {
            if (var3 == arg0) break label_3;
            if (mc.player.getInventory().getItem(var3).getItem() != Items.TOTEM_OF_UNDYING) {
                break label_3;
            }
            return var3;
            label_3:
            var3++;
        }
        return -1;
    }

    private int findGappleHotbar() {
        int var1 = InventoryUtils.find(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
        if (var1 != -1) return var1;
        return InventoryUtils.find(Items.GOLDEN_APPLE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
    }

    private int findGappleInventory() {
        int var1 = InventoryUtils.find(Items.ENCHANTED_GOLDEN_APPLE, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
        if (var1 != -1) return var1;
        return InventoryUtils.find(Items.GOLDEN_APPLE, InventoryUtils.INVENTORY_START, InventoryUtils.INVENTORY_END);
    }

    public boolean shouldLockSlot() {
        if (!(isToggled())) return false;
        if (mc.player == null) return false;
        if ((this.eatingHotbar) || (!(!this.eatingInventory))) {
            return false;
        }
        if (InventoryUtils.find(Items.TOTEM_OF_UNDYING) == -1) return false;
        return needsTotem();
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

    private boolean hasEnemyInVisualRange() {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var1 = null;
        return mc.level.players().stream().anyMatch((arg1) -> {
    if (((arg1 == mc.player) || (arg1.isDeadOrDying())) || (EntityUtils.isGhost(arg1))) break label_1;
    if (arg0 != null && arg0.isToggled()) {
        if (arg1 == arg0.getPlayer()) break label_1;
    }
    return !(LightningClient.FRIEND_MANAGER.contains(arg1.getName().getString()));
});
    }

    public void onDisable() {
        if (!(!this.eatingHotbar) && mc.player != null && this.previousHotbarSlot != -1) {
            mc.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
            ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
        }
        if (!(!this.eatingInventory) && mc.player != null && this.swappedInvSlot != -1 && this.swappedHotbarSlot != -1) {
            InventoryUtils.swap("Swap", this.swappedInvSlot, this.swappedHotbarSlot);
        }
        this.eatingHotbar = false;
        this.previousHotbarSlot = -1;
        this.eatingInventory = false;
        this.swappedInvSlot = -1;
        this.swappedHotbarSlot = -1;
        this.pendingRefillTime = 0L;
        resetAfterPops("disable");
        this.observedMode = null;
        this.observedPlayer = null;
        this.observedWorld = null;
    }

    public String getMetaData() {
        return ("Slot " + this.slot.getValue().intValue());
    }

    static {
        hidingLegitInventory = false;
    }


    final class PopCandidate {
        private final int slot;
        private final int createdUpdate;

        private PopCandidate(int arg0, int arg1) {
            this.slot = arg0;
            this.createdUpdate = arg1;
        }

    }
}
