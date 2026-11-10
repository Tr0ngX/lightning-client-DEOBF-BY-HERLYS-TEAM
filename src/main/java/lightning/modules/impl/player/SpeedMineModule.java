package lightning.modules.impl.player;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackBlockEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.DestroyBlockEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.mixins.accessors.ClientWorldAccessor;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.combat.AutoMineModule;
import lightning.modules.impl.combat.AutoTrapModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CMiningStatePacket;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "SpeedMine", description = "Automatically mines blocks at a faster speed using packets.", category = Module.Category.PLAYER)
public class SpeedMineModule extends Module {
    public ModeSetting switchMode;
    public NumberSetting range;
    public NumberSetting speed;
    public BooleanSetting farReach;
    public BooleanSetting sixB;
    public ModeSetting rotate;
    public BooleanSetting switchReset;
    public BooleanSetting doubleMine;
    public BooleanSetting shift;
    public ModeSetting await;
    public ModeSetting rebreak;
    public NumberSetting instantDelay;
    public NumberSetting instantTimeout;
    public BooleanSetting async;
    public CategorySetting onGroundCheckCategory;
    public BooleanSetting ncpCheck;
    public BooleanSetting airCheck;
    public BooleanSetting liquidCheck;
    public BooleanSetting grim;
    public BooleanSetting whileEating;
    public ModeSetting whitelistMode;
    public WhitelistSetting whitelist;
    public CategorySetting renderCategory;
    public ModeSetting render;
    public ModeSetting animation;
    public ModeSetting color;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    public ModeSetting instantRender;
    public ColorSetting instantColor;
    private SpeedMineModule.Action primary;
    private SpeedMineModule.Secondary secondary;
    private static final int SECONDARY_TIMEOUT = 10;
    private int secondaryHoldSlot;
    private int secondaryOriginalSlot;
    private static final int SECONDARY_MAX_TICKS = 60;
    private boolean doubleEngaged;
    private boolean primaryPaired;
    private boolean handlingSwitchReset;
    private final Timer instantTimer;
    private static final long INSTANT_ACK_STALL_MS = 200L;
    private final Timer mineTimer;
    private double delayBalance;
    private long lastStopMs;
    private static final int STOP_COOLDOWN_TICKS = 6;
    private int stopCooldown;
    private volatile boolean interactPaused;
    private volatile long interactPausedAt;
    private boolean needsRestart;
    public final Object interactSyncLock;
    private static final long INTERACT_PAUSE_TIMEOUT_MS = 750L;
    public volatile BlockPos proxyPrimaryPos;
    public volatile float proxyPrimaryProgress;
    public volatile BlockPos proxySecondaryPos;
    public volatile float proxySecondaryProgress;
    private volatile float prevProxyPrimaryProgress;
    private volatile long proxyPrimaryUpdateTime;
    private volatile long proxyPrimaryUpdateInterval;
    private volatile float prevProxySecondaryProgress;
    private volatile long proxySecondaryUpdateTime;
    private volatile long proxySecondaryUpdateInterval;
    private static final int GRIM_DECOY_Y_OFFSET = 2000;

    public SpeedMineModule() {
        this.switchMode = new ModeSetting("Switch", "The mode that will be used for automatically switching to the fastest item.", "Silent", InventoryUtils.SWITCH_MODES);
        this.range = new NumberSetting("Range", "The maximum distance at which blocks will be mined.", 6.0, 0.0, 8.0);
        this.speed = new NumberSetting("Speed", "The speed at which the module will mine blocks.", 0.7, 0.7, 1.0);
        this.farReach = new BooleanSetting("FarReach", "Bypass for Duration 0.7 on Grim. Try this if normal AutoMine doesn't work.", false);
        this.sixB = new BooleanSetting("6b", "Allows mining bedrock after 40 ticks and enables instant-mining when broken.", false);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Automatically rotates to the block when mining it.", "Normal", tmp0);
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "AltSwap";
        tmp1[2] = "AltPickup";
        this.switchReset = new BooleanSetting("SwitchReset", "Resets the mining when switching slots.", new ModeSetting.Visibility(this.switchMode, tmp1), true);
        this.doubleMine = new BooleanSetting("Double", "Allows the mining of 2 blocks at the same time.", false);
        this.shift = new BooleanSetting("Shift", "Selects the block behind according to crosshair as secondary when clicking while holding shift.", new BooleanSetting.Visibility(this.doubleMine, true), true);
        var tmp2 = new String[3];
        tmp2[0] = "Off";
        tmp2[1] = "Dynamic";
        tmp2[2] = "Always";
        this.await = new ModeSetting("Await", "Wait for blocks to break before swapping back. Off: don't wait. Dynamic: wait only while the secondary isn't close to done yet. Always: always wait. Works around real NCP's FastBreak check on some Paper servers (cancels doubleMine's secondary otherwise) -- costs mining throughput while waiting.", new BooleanSetting.Visibility(this.doubleMine, true), "Off", tmp2);
        var tmp3 = new String[3];
        tmp3[0] = "None";
        tmp3[1] = "Fast";
        tmp3[2] = "Instant";
        this.rebreak = new ModeSetting("Rebreak", "Automatically re-mines blocks once they have been replaced.", "None", tmp3);
        var tmp4 = new String[2];
        tmp4[0] = "Fast";
        tmp4[1] = "Instant";
        this.instantDelay = new NumberSetting("InstantDelay", "The amount of time that has to pass before instantly mining blocks.", new ModeSetting.Visibility(this.rebreak, tmp4), 0, 0, 20);
        var tmp5 = new String[2];
        tmp5[0] = "Fast";
        tmp5[1] = "Instant";
        this.instantTimeout = new NumberSetting("InstantTimeout", "The amount of time that cancel instantly mine while no block to mine.", new ModeSetting.Visibility(this.rebreak, tmp5), 60, 0, 100);
        var tmp6 = new String[2];
        tmp6[0] = "Fast";
        tmp6[1] = "Instant";
        this.async = new BooleanSetting("Async", "Keeps instantly re-firing even while the target position is currently air, instead of waiting for it to solidify.", new ModeSetting.Visibility(this.rebreak, tmp6), false);
        this.onGroundCheckCategory = new CategorySetting("OnGroundCheck", "Claims onGround on outgoing movement packets while mining, bypassing the not-on-the-ground mining speed penalty.");
        this.ncpCheck = new BooleanSetting("NCP", "Enables the onGround claim below. Turn off for servers that don't penalize air/liquid mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
        this.airCheck = new BooleanSetting("AirCheck", "Claims onGround while airborne and mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
        this.liquidCheck = new BooleanSetting("LiquidCheck", "Claims onGround while in liquid and mining.", new CategorySetting.Visibility(this.onGroundCheckCategory), false);
        this.grim = new BooleanSetting("Grim", "Adds a bypass catered to the Grim anticheat.", false);
        this.whileEating = new BooleanSetting("WhileEating", "Mines blocks while eating.", true);
        var tmp7 = new String[3];
        tmp7[0] = "All";
        tmp7[1] = "WhiteList";
        tmp7[2] = "BlackList";
        this.whitelistMode = new ModeSetting("Mode", "All = mine every block. WhiteList = mine only listed blocks. BlackList = mine every block except listed.", "All", tmp7);
        this.whitelist = new WhitelistSetting("List", "Blocks the WhiteList/BlackList mode compares against.", WhitelistSetting.Type.BLOCKS);
        this.renderCategory = new CategorySetting("Render", "The category containing all settings related to rendering.");
        var tmp8 = new String[4];
        tmp8[0] = "None";
        tmp8[1] = "Fill";
        tmp8[2] = "Outline";
        tmp8[3] = "Both";
        this.render = new ModeSetting("Render", "Mode", "The rendering that will be applied to the blocks highlighted.", new CategorySetting.Visibility(this.renderCategory), "Both", tmp8);
        var tmp9 = new String[3];
        tmp9[0] = "Fill";
        tmp9[1] = "Outline";
        tmp9[2] = "Both";
        var tmp10 = new String[3];
        tmp10[0] = "None";
        tmp10[1] = "Expand";
        tmp10[2] = "Rise";
        this.animation = new ModeSetting("Animation", "The animation that will be used when rendering the block mining progress.", new ModeSetting.Visibility(this.render, tmp9), "Expand", tmp10);
        var tmp11 = new String[3];
        tmp11[0] = "Fill";
        tmp11[1] = "Outline";
        tmp11[2] = "Both";
        var tmp12 = new String[3];
        tmp12[0] = "Static";
        tmp12[1] = "Smooth";
        tmp12[2] = "Custom";
        this.color = new ModeSetting("Color", "The color that will be used when rendering the block mining.", new ModeSetting.Visibility(this.render, tmp11), "Smooth", tmp12);
        var tmp13 = new String[2];
        tmp13[0] = "Fill";
        tmp13[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.render, tmp13), ColorUtils.getDefaultFillColor());
        var tmp14 = new String[2];
        tmp14[0] = "Outline";
        tmp14[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.render, tmp14), ColorUtils.getDefaultOutlineColor());
        var tmp15 = new String[3];
        tmp15[0] = "None";
        tmp15[1] = "Default";
        tmp15[2] = "Custom";
        this.instantRender = new ModeSetting("InstantRender", "Instant", "The color that will be used for rendering instantly mined blocks.", new CategorySetting.Visibility(this.renderCategory), "None", tmp15);
        var tmp16 = new String[1];
        tmp16[0] = "Custom";
        this.instantColor = new ColorSetting("InstantColor", "The custom color used for instantly mined blocks.", new ModeSetting.Visibility(this.instantRender, tmp16), new ColorSetting.Color(new Color(148, 0, 211), false, false));
        this.primary = null;
        this.secondary = null;
        this.secondaryHoldSlot = -1;
        this.secondaryOriginalSlot = -1;
        this.doubleEngaged = false;
        this.primaryPaired = false;
        this.handlingSwitchReset = false;
        this.instantTimer = new Timer();
        this.mineTimer = new Timer();
        this.delayBalance = 0.0;
        this.lastStopMs = 0L;
        this.stopCooldown = 0;
        this.interactPaused = false;
        this.interactPausedAt = 0L;
        this.needsRestart = false;
        this.interactSyncLock = new Object();
        this.proxyPrimaryPos = null;
        this.proxyPrimaryProgress = 0.0f;
        this.proxySecondaryPos = null;
        this.proxySecondaryProgress = 0.0f;
        this.prevProxyPrimaryProgress = 0.0f;
        this.proxyPrimaryUpdateTime = 0L;
        this.proxyPrimaryUpdateInterval = 50L;
        this.prevProxySecondaryProgress = 0.0f;
        this.proxySecondaryUpdateTime = 0L;
        this.proxySecondaryUpdateInterval = 50L;
    }

    public SpeedMineModule.Action getPrimary() {
        return this.primary;
    }

    public SpeedMineModule.Secondary getSecondary() {
        return this.secondary;
    }

    public void setDoubleEngaged(boolean arg0) {
        this.doubleEngaged = arg0;
    }

    private boolean hasSecondarySlot() {
        return this.secondary != null;
    }

    private boolean canStartNow() {
        if (this.whileEating.getValue()) break label_1;
        if (this.interactPaused) break label_0;
        if (mc.player == null) break label_1;
        if (mc.player.isUsingItem()) break label_0;
        if (!(EntityUtils.isEating())) break label_1;
        label_0:
        return false;
        label_1:
        if (!(this.farReach.getValue())) return true;
        if (this.stopCooldown) break label_3;
        return canBegin();
    }

    private boolean canBegin() {
        long var1 = (System.currentTimeMillis() - this.lastStopMs);
        if (var1 >= 275L) return true;
        double var3 = ((double) ((300L - var1) * ((long) (this.farReach.getValue() ? 2 : 1))));
        return (this.delayBalance + var3) <= 900.0;
    }

    private void trackStarts(int arg0) {
        long var2 = (System.currentTimeMillis() - this.lastStopMs);
        int var4 = 0;
        while (var4 < arg0) {
            if (var2 >= 275L) {
                this.delayBalance = (this.delayBalance * 0.9);
            } else {
                this.delayBalance = (this.delayBalance + ((double) (300L - var2)));
            }
            var4++;
        }
        this.delayBalance = Mth.clamp(this.delayBalance, -1000.0, 1000.0);
    }

    private void markStop() {
        markStop(false);
    }

    private void markStop(boolean arg0) {
        this.lastStopMs = System.currentTimeMillis();
        if (!(!arg0)) {
            this.stopCooldown = 6;
        }
    }

    public BlockPos getMiningPosition() {
        if (this.primary != null && this.primary.isMining()) {
        } else {
        }
        return null;
    }

    public boolean isInteractPaused() {
        return this.interactPaused;
    }

    public void setInteractPaused(boolean arg0) {
        this.interactPaused = arg0;
        if (!(!arg0)) {
            this.interactPausedAt = System.currentTimeMillis();
        } else {
            this.needsRestart = true;
        }
    }

    public void updateProxyMiningState(BlockPos arg0, float arg1, BlockPos arg2, float arg3) {
        long var5 = System.currentTimeMillis();
        if (arg0 == null) {
            if (this.proxyPrimaryPos != null) {
                break label_3;
            }
            break label_3;
        }
        int var7 = (!(arg0.equals(this.proxyPrimaryPos)) ? 1 : 0);
        this.prevProxyPrimaryProgress = (!(!var7) ? arg1 : this.proxyPrimaryProgress);
        this.proxyPrimaryUpdateInterval = Mth.clamp((var5 - this.proxyPrimaryUpdateTime), 1L, 500L);
        this.proxyPrimaryUpdateTime = var5;
        this.proxyPrimaryPos = arg0;
        this.proxyPrimaryProgress = arg1;
        if (arg2 == null) {
            if (this.proxySecondaryPos != null) {
                break label_9;
            }
            break label_9;
        }
        int var8 = (!(arg2.equals(this.proxySecondaryPos)) ? 1 : 0);
        this.prevProxySecondaryProgress = (!(!var8) ? arg3 : this.proxySecondaryProgress);
        this.proxySecondaryUpdateInterval = Mth.clamp((var5 - this.proxySecondaryUpdateTime), 1L, 500L);
        this.proxySecondaryUpdateTime = var5;
        this.proxySecondaryPos = arg2;
        this.proxySecondaryProgress = arg3;
    }

    private float interpolatedProgress(float arg0, float arg1, long arg2, long arg3) {
        float var7 = Mth.clamp((((float) (System.currentTimeMillis() - arg2)) / ((float) arg3)), 0.0f, 1.0f);
        return (arg0 + ((arg1 - arg0) * var7));
    }

    public boolean handle(BlockPos arg0, int arg1) {
        if (!(canHandle(arg0))) return false;
        if (this.primary == null) break label_1;
        if (!(this.primary.getPosition().equals(arg0))) {
            label_1:
            if (this.secondary != null && this.secondary.getPosition().equals(arg0)) {
            }
            return true;
        }
        if (this.primary != null && arg1 <= 0 && this.sixB.getValue() && this.primary.isBedrockOrigin() && arg1 <= this.primary.getPriority()) return false;
        if (!(this.doubleMine.getValue())) break label_6;
        if (!this.doubleEngaged) {
            if (arg1 <= 0) break label_6;
        }
        break label_7;
        label_6:
        label_7:
        int var3 = 0;
        int var4 = 0;
        if (!(!var3)) {
            if (this.secondary != null) {
                if (this.await.getValue().equalsIgnoreCase("Always")) return false;
                if (this.await.getValue().equalsIgnoreCase("Dynamic") && !(this.secondary.isHolding())) return false;
            }
            if (this.secondary == null && this.primary != null) {
            } else {
            }
            Object var5 = null;
            Object var6 = null;
            if (var5 != null) {
                this.secondary = var5;
                break label_13;
            }
            if (this.primary == null) break label_13;
            this.primary.cancel();
            label_13:
            this.primary = new SpeedMineModule.Action(this, this, arg0, arg1);
            this.primaryPaired = hasSecondarySlot();
        } else {
            if (this.primary != null) {
                this.primary.cancel();
            }
            this.primary = new SpeedMineModule.Action(this, this, arg0, arg1);
            this.primaryPaired = false;
        }
        return true;
    }

    public boolean fillSecondary(BlockPos arg0, int arg1) {
        if (this.primary != null && this.primary.isTerrainBase()) return false;
        if (!(canHandle(arg0))) return false;
        if (this.sixB.getValue() && mc.level.getBlockState(arg0).getBlock() == Blocks.BEDROCK) return false;
        if (this.secondary != null) return this.secondary.getPosition().equals(arg0);
        if (this.primary != null && this.primary.getPosition().equals(arg0)) return true;
        Object var3 = new SpeedMineModule.Action(this, this, arg0, arg1);
        Object var4 = var3.demote();
        if (var4 == null) return false;
        this.secondary = var4;
        return true;
    }

    public boolean isMineTimerReady() {
        return this.mineTimer.hasTimeElapsed(50L);
    }

    public void dropPrimaryIfInvalid(Predicate arg0) {
        if ((this.primary == null) || (this.primary.getPriority())) {
            return;
        }
        if (!(arg0.test(this.primary.getPosition()))) {
            dropPrimary();
        }
    }

    public void dropSecondaryIfInvalid(Predicate arg0) {
        if ((this.secondary == null) || (this.secondary.getPriority())) {
            return;
        }
        if (!(arg0.test(this.secondary.getPosition()))) {
            dropSecondary();
        }
    }

    public void dropPrimary() {
        if (this.primary == null) return;
        this.primary.cancel();
        this.primary = null;
        this.primaryPaired = false;
    }

    public void dropSecondary() {
        if (this.secondary == null) return;
        this.secondary.release();
        this.secondary = null;
    }

    public boolean primaryDigging() {
        if (this.primary == null) break label_0;
        return !(mc.level.getBlockState(this.primary.getPosition()).canBeReplaced());
    }

    public boolean primaryCamping() {
        if ((this.primary == null) || (!(this.primary.isInstantMine()))) {
            return false;
        }
        if (!(mc.level.getBlockState(this.primary.getPosition()).canBeReplaced()) && !(this.sixB.getValue())) return false;
        Object var1 = getTarget();
        if (var1 == null) break label_3;
        return isTargetSurroundPosition(this.primary.getPosition(), var1.player());
    }

    public boolean slotsFull() {
        if (this.primary != null && this.sixB.getValue() && this.primary.isBedrockOrigin()) return true;
        if (!(primaryDigging())) {
            if (!(primaryCamping())) break label_3;
        }
        if (!(!this.doubleEngaged) && !(hasSecondarySlot())) {
            if (!this.primaryPaired) break label_3;
        }
        break label_4;
        label_3:
        label_4:
        return false;
    }

    public boolean phaseSlotsFull() {
        if (!(primaryDigging())) break label_1;
        if (!(!this.doubleEngaged) && !(hasSecondarySlot())) {
            if (!this.primaryPaired) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        return false;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (isDeferringToProxy()) return;
        if (mc.player == null) break label_1;
        if (mc.level == null) {
            label_1:
            return;
        }
        if (this.stopCooldown > 0) {
            this.stopCooldown = (this.stopCooldown - 1);
        }
        if (!(this.doubleMine.getValue())) {
            this.doubleEngaged = false;
        }
        if (this.secondary != null && !(this.doubleMine.getValue())) {
            this.secondary.release();
            this.secondary = null;
            break label_6;
        }
        if ((this.secondary == null) || (!(this.secondary.process()))) break label_6;
        this.secondary = null;
        label_6:
        if (this.primary != null && this.primary.process()) {
            this.primary = null;
            this.primaryPaired = false;
        }
        if (isProxyActive()) {
            syncMiningStateToClient();
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null) {
            var2.placePendingTerrain();
        }
        if (this.primary != null && var2 != null && var2.isProtectedTerrainBase(this.primary.getPosition())) {
            this.primary.cancel();
            this.primary = null;
            this.primaryPaired = false;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (isDeferringToProxy()) {
            renderProxyState(arg0.getMatrices());
            return;
        }
        if (this.doubleMine.getValue() && this.secondary != null) {
            this.secondary.render(arg0.getMatrices());
        }
        if (this.primary != null) {
            this.primary.render(arg0.getMatrices());
        }
    }

    @SubscribeEvent
    public void night$onBlockUpdate(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundBlockUpdatePacket))) return;
        Object var3 = ((ClientboundBlockUpdatePacket) var2);
        Object var4 = var3.getPos();
        if (this.primary != null && var4.equals(this.primary.getPosition())) {
            this.primary.markBroken();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent.Post arg0) {
        if (isDeferringToProxy()) return;
        if (mc.player == null) break label_1;
        if (mc.level == null) {
            label_1:
            return;
        }
        if (!(!this.handlingSwitchReset)) return;
        if ((!((arg0.getPacket() instanceof ServerboundSetCarriedItemPacket))) || (!(this.switchReset.getValue()))) break label_9;
        if (!(this.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
            if (!(this.switchMode.getValue().equalsIgnoreCase("AltPickup"))) break label_9;
        }
        this.handlingSwitchReset = true;
        if (this.primary != null) {
            this.primary.cancel();
            this.primary.tryStart();
        }
        this.handlingSwitchReset = false;
        break label_9;
        Object var4 = null;
        this.handlingSwitchReset = false;
        throw var4;
        label_9:
        var tmp0 = arg0.getPacket();
        Object var3 = tmp0;
        if ((tmp0 instanceof ServerboundPlayerActionPacket)) {
            var tmp1 = ((ServerboundPlayerActionPacket) var3);
            Object var2 = tmp1;
            if (tmp1.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && var2.getPos().getY() > 2000) return;
        }
        var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundMovePlayerPacket))) break label_16;
        var4 = ((ServerboundMovePlayerPacket) var3);
        if ((!(this.ncpCheck.getValue())) || (this.primary == null)) break label_16;
        if ((!(this.airCheck.getValue())) || (mc.player.onGround())) break label_11;
        int var6 = (!(mc.player.isInWater()) ? 1 : 0);
        if (!(this.liquidCheck.getValue())) break label_13;
        var tmp2 = (mc.player.isInWater() ? 1 : 0);
        int var5 = tmp2;
        int var7 = tmp2;
        if (!var6) {
            if (!var5) break label_16;
        }
        ((PlayerMoveC2SPacketAccessor) var4).setOnGround(true);
        label_16:
    }

    @SubscribeEvent
    public void onAttackBlock(AttackBlockEvent arg0) {
        if (isDeferringToProxy()) return;
        if (mc.player == null) break label_1;
        if (mc.level == null) {
            label_1:
            return;
        }
        Object var2 = arg0.getPosition();
        if ((!(this.doubleMine.getValue())) || (!(this.shift.getValue()))) break label_10;
        if (!(mc.options.keyShift.isDown())) {
            if (!(mc.player.isShiftKeyDown())) break label_10;
        }
        Object var3 = mc.player.getLookAngle();
        double var4 = Math.abs(var3.x);
        double var6 = Math.abs(var3.y);
        double var8 = Math.abs(var3.z);
        if (var6 > var4 && var6 > var8) {
            if (var3.y > 0.0) {
                break label_9;
            }
            break label_9;
        }
        if (var4 > var8) {
            if (var3.x > 0.0) {
                break label_9;
            }
            break label_9;
        }
        Object var10 = (var3.z > 0.0 ? Direction.SOUTH : Direction.NORTH);
        Object var11 = var2.relative(var10);
        if (((!(isValid(var11))) || (isOutOfRange(var11))) || (mc.level.getBlockState(var11).canBeReplaced())) break label_10;
        handle(var11, 1);
        label_10:
        if (handle(var2, 1)) {
            arg0.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        this.primary = null;
        this.secondary = null;
        this.doubleEngaged = false;
        this.primaryPaired = false;
    }

    public void onEnable() {
        this.doubleEngaged = false;
        this.primaryPaired = false;
    }

    public void onDisable() {
        this.doubleEngaged = false;
        this.primaryPaired = false;
        if (isDeferringToProxy()) return;
        restoreIronSwap();
        if (mc.player != null) {
            if (mc.level == null) {
            }
            this.primary = null;
            this.secondary = null;
            return;
        }
        if (this.secondary != null) {
            this.secondary.release();
        }
        this.secondary = null;
        if (this.primary != null) {
            this.primary.cancel();
            this.primary = null;
        }
        setInteractPaused(false);
    }

    public void restoreIronSwap() {
        if (this.primary != null) {
            this.primary.restoreIronSwap();
        }
        if (mc.player == null) break label_2;
        var tmp0 = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
        int var1 = tmp0;
        if (tmp0 == -1) break label_2;
        int var2 = InventoryUtils.findFastestItem(Blocks.OBSIDIAN.defaultBlockState(), (InventoryUtils.HOTBAR_END + 1), InventoryUtils.INVENTORY_END);
        if (var2 == -1) {
            var2 = InventoryUtils.findEmptySlot((InventoryUtils.HOTBAR_END + 1), InventoryUtils.INVENTORY_END);
        }
        if ((var2 == -1) || (var2 <= InventoryUtils.HOTBAR_END)) break label_2;
        InventoryUtils.swap("Swap", var2, var1);
        label_2:
    }

    public String getMetaData() {
        if (this.primary == null) {
        } else {
        }
        Object var1 = new DecimalFormat("0.0").format(((double) (this.primary.getProgress() / this.primary.getSpeed())));
        if (this.secondary != null) {
            if (!(this.doubleMine.getValue())) {
            }
        } else {
        }
        Object var2 = (", " + new DecimalFormat("0.0").format(((double) (this.secondary.getProgress() / this.secondary.getSpeed()))));
        return (var1 + var2);
    }

    private boolean canHandle(BlockPos arg0) {
        if ((mc.gameMode.getPlayerMode() == GameType.CREATIVE) || (mc.gameMode.getPlayerMode() == GameType.SPECTATOR)) {
            return false;
        }
        if (mc.level.getBlockState(arg0).getBlock().defaultDestroyTime() == -1.0f) {
            if ((!(this.sixB.getValue())) || (mc.level.getBlockState(arg0).getBlock() != Blocks.BEDROCK)) {
                return false;
            }
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var4 = null;
        if (var3 != null && var3.isProtectedTerrainBase(arg0)) return false;
        int var5 = this.whitelist.isWhitelistContains(mc.level.getBlockState(arg0).getBlock());
        Object var6 = this.whitelistMode.getValue();
        int var7 = -1;
        switch (var6.hashCode()) {
            case -958939875 -> { break label_8; }
            case 349164615 -> { break label_7; }
            default -> { break label_9; }
        }
        label_7:
        if (!(var6.equals("WhiteList"))) break label_9;
        var7 = 0;
        break label_9;
        label_8:
        if (!(var6.equals("BlackList"))) break label_9;
        var7 = 1;
        label_9:
        switch (var7) {
            case 0 -> { break label_10; }
            case 1 -> { break label_11; }
            default -> { break label_13; }
        }
        label_10:
        break label_14;
        label_11:
        if (!var5) {
            break label_14;
        }
        break label_14;
        label_13:
        label_14:
        int var2 = 1;
        if (!var2) return false;
        return mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0)) <= Mth.square(this.range.getValue().doubleValue());
    }

    public boolean isInvalid(BlockPos arg0) {
        if (!(isValid(arg0))) return true;
        return isMining(arg0);
    }

    public boolean isValid(BlockPos arg0) {
        if (arg0 == null) return false;
        if (mc.level.getBlockState(arg0).getBlock().defaultDestroyTime() == -1.0f) {
            if ((!(this.sixB.getValue())) || (mc.level.getBlockState(arg0).getBlock() != Blocks.BEDROCK)) {
                return false;
            }
        }
        return !(mc.level.getBlockState(arg0).getBlock().equals(Blocks.COBWEB));
    }

    public boolean isMining(BlockPos arg0) {
        if (arg0 == null) return true;
        if (this.primary != null && this.primary.getPosition().equals(arg0)) return true;
        if (this.secondary == null) break label_2;
        return this.secondary.getPosition().equals(arg0);
    }

    public boolean isTargetSurroundPosition(BlockPos arg0, Player arg1) {
        if (((arg0 == null) || (arg1 == null)) || (mc.level == null)) {
            return false;
        }
        Object var4 = arg1.getBoundingBox();
        var tmp0 = Mth.floor(arg1.getY());
        int var3 = tmp0;
        int var5 = tmp0;
        while (var5 <= (var3 + 2)) {
            int var6 = Mth.floor(var4.minX);
            while (var6 < Mth.ceil(var4.maxX)) {
                int var7 = Mth.floor(var4.minZ);
                while (var7 < Mth.ceil(var4.maxZ)) {
                    Object var8 = new BlockPos(var6, var5, var7);
                    if (arg0.equals(var8)) return true;
                    if (var5 > (var3 + 1)) {
                        break label_9;
                    }
                    Object var9 = Direction.Plane.HORIZONTAL.iterator();
                    label_7:
                    if (!(var9.hasNext())) break label_9;
                    Object var10 = ((Direction) var9.next());
                    if (!(arg0.equals(var8.relative(var10)))) {
                        break label_7;
                    }
                    return true;
                    label_9:
                    var7++;
                }
                var6++;
            }
            var5++;
        }
        return false;
    }

    public boolean isOutOfRange(BlockPos arg0) {
        if (arg0 == null) return true;
        return mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0)) > Mth.square(this.range.getValue().doubleValue());
    }

    public SpeedMineModule.Target getTarget() {
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var1 = null;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = new ArrayList(mc.level.players());
        if ((var1 == null) || (!(var1.isToggled()))) break label_6;
        Object var4 = var1.getGhosts().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Player) var4.next());
            if (var5 == null) break label_4;
            if (var3.contains(var5)) {
                break label_4;
            }
            var3.add(var5);
        }
        label_6:
        var4 = null;
        var5 = var3.iterator();
        while (var5.hasNext()) {
            Object var6 = ((Player) var5.next());
            if (var6 == mc.player) break label_7;
            if (var2 != null) {
                if (var2.isGhost(var6)) break label_7;
            }
            var tmp0 = EntityUtils.isGhost(var6);
            int var8 = tmp0;
            if (!tmp0) {
                if ((!(var6.isAlive())) || (var6.getHealth() <= 0.0f)) break label_7;
            }
            if (mc.player.distanceToSqr(var6) > Mth.square((this.range.getValue().doubleValue() + 2.0))) break label_7;
            if (var1 != null && var1.isGhost(var6)) {
                var tmp1 = var1.getSpot(((RemotePlayer) var6));
                Object var7 = tmp1;
                if ((tmp1 == null) || (!(LightningClient.FRIEND_MANAGER.contains(var7.data.name)))) break label_11;
                break label_7;
            }
            if (!(LightningClient.FRIEND_MANAGER.contains(var6.getName().getString()))) break label_11;
            break label_7;
            label_11:
            Object var9 = getPositions(var6);
            Object var10 = getTargetPosition(var9);
            if (this.doubleMine.getValue()) break label_12;
            if (var9.isEmpty()) break label_7;
            if (var10 != null) break label_12;
            break label_7;
            label_12:
            if (var4 == null) {
                var4 = new SpeedMineModule.Target(var6, var9, var10);
                break label_7;
            }
            int var11 = EntityUtils.isGhost(var4.player());
            if (var11 != var8) {
                if (!var11) {
                    break label_7;
                }
                var4 = new SpeedMineModule.Target(var6, var9, var10);
                break label_7;
            }
            if (mc.player.distanceToSqr(var6) >= mc.player.distanceToSqr(var4.player())) {
                break label_7;
            }
            var4 = new SpeedMineModule.Target(var6, var9, var10);
        }
        return var4;
    }

    private BlockPos getTargetPosition(List arg0) {
        Object var2 = null;
        double var3 = 0.0;
        Object var5 = arg0.iterator();
        while (var5.hasNext()) {
            Object var6 = ((SpeedMineModule.Position) var5.next());
            if (!(this.doubleMine.getValue())) break label_1;
            if (!(var6.feetPosition())) break label_0;
            label_1:
            if (!(isValidPosition(var6.position()))) break label_0;
            if (HoleUtils.isPlayerInHole(mc.player) && HoleUtils.getFeetPositions(mc.player, true, false, true).contains(var6.position())) {
                break label_0;
            }
            double var7 = 0.0;
            if (this.sixB.getValue() && mc.level.getBlockState(var6.position()).getBlock() == Blocks.BEDROCK) {
                if (var6.feetPosition()) {
                    var7 = (var7 + 1000.0);
                } else {
                    var7 = (var7 - 1000.0);
                }
                var7 = (var7 - 1000.0);
            }
            if (var6.feetPosition()) {
                var7 = (var7 + 5.0);
                if (mc.level.getBlockState(var6.position()).getBlock() == Blocks.ENDER_CHEST) {
                    var7 = (var7 + 0.95);
                    break label_7;
                }
                if (!(WorldUtils.isCrystalPlaceable(var6.position().offset(0, 1, 0)))) break label_7;
                var7 = (var7 + 0.35);
                label_7:
                if (!(hasCityPosition(var6.position()))) break label_12;
                var7 = (var7 + 0.6);
                break label_12;
            }
            if (mc.level.getBlockState(var6.position()).getBlock() == Blocks.ENDER_CHEST) {
                var7 = (var7 - 2.0);
                break label_11;
            }
            if (WorldUtils.isCrystalPlaceable(var6.position().offset(0, 1, 0))) {
                var7 = (var7 + 0.75);
                break label_11;
            }
            var7 = (var7 - 2.0);
            label_11:
            var7 = (var7 - 2.0);
            label_12:
            if (var7 < var3) {
                break label_0;
            }
            var2 = var6.position();
            var3 = var7;
        }
        return var2;
    }

    private List getPositions(Player arg0) {
        Object var2 = new ArrayList();
        Object var3 = HoleUtils.getFeetPositions(arg0, true, false, true).iterator();
        while (var3.hasNext()) {
            Object var4 = ((BlockPos) var3.next());
            var2.add(new SpeedMineModule.Position(var4, true));
            if (this.doubleMine.getValue()) {
                break label_0;
            }
            var2.add(new SpeedMineModule.Position(var4.offset(0, 1, 0), false));
        }
        if (!(this.doubleMine.getValue())) {
            var2.add(new SpeedMineModule.Position(arg0.blockPosition().offset(0, 2, 0), false));
        }
        return var2;
    }

    private boolean isValidPosition(BlockPos arg0) {
        if (mc.level.getBlockState(arg0).canBeReplaced()) return false;
        if (mc.level.getBlockState(arg0).getBlock().defaultDestroyTime() == -1.0f) {
            if ((!(this.sixB.getValue())) || (mc.level.getBlockState(arg0).getBlock() != Blocks.BEDROCK)) {
                return false;
            }
        }
        return !(isOutOfRange(arg0));
    }

    private boolean hasCityPosition(BlockPos arg0) {
        Object var2 = Direction.Plane.HORIZONTAL.iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Direction) var2.next());
            Object var4 = arg0.relative(var3);
            if (!(WorldUtils.isPlaceable(var4))) {
                break label_0;
            }
            return true;
        }
        return false;
    }

    private boolean isProxyActive() {
        if (((!PingBypassFlags.proxyForwardingActive) || (LightningClient.PINGBYPASS_CONFIG == null)) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) break label_0;
        return LightningClient.PROXY_SERVER != null;
    }

    private boolean isDeferringToProxy() {
        return PingBypassFlags.isPingBypassActive();
    }

    private void syncMiningStateToClient() {
        if (this.primary != null) {
        } else {
        }
        if (this.primary != null) {
        } else {
        }
        if (this.secondary != null) {
        } else {
        }
        if (this.secondary != null) {
        } else {
        }
        Object var1 = this.primary.getPosition();
        Object var2 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Connection) var2.next());
            if (!(var3.isConnected())) {
                break label_10;
            }
            var3.send(var1);
        }
    }

    private void renderProxyState(PoseStack arg0) {
        if (this.proxyPrimaryPos != null) {
            float var2 = interpolatedProgress(this.prevProxyPrimaryProgress, this.proxyPrimaryProgress, this.proxyPrimaryUpdateTime, this.proxyPrimaryUpdateInterval);
            renderProxyBlock(arg0, this.proxyPrimaryPos, var2);
        }
        if (this.proxySecondaryPos != null && this.doubleMine.getValue()) {
            var2 = interpolatedProgress(this.prevProxySecondaryProgress, this.proxySecondaryProgress, this.proxySecondaryUpdateTime, this.proxySecondaryUpdateInterval);
            renderProxyBlock(arg0, this.proxySecondaryPos, var2);
        }
    }

    private void renderProxyBlock(PoseStack arg0, BlockPos arg1, float arg2) {
        if (mc.level.getBlockState(arg1).canBeReplaced()) return;
        Object var4 = new AABB(arg1);
        if (!(this.animation.getValue().equalsIgnoreCase("Expand"))) break label_1;
        var4 = new AABB(arg1).deflate(0.5).inflate(Mth.clamp((((double) arg2) / 2.0), 0.0, 0.5));
        label_1:
        if (this.animation.getValue().equalsIgnoreCase("Rise")) {
            var4 = new AABB(((double) arg1.getX()), ((double) arg1.getY()), ((double) arg1.getZ()), (((double) arg1.getX()) + 1.0), ((double) (((float) arg1.getY()) + arg2)), (((double) arg1.getZ()) + 1.0));
        }
        Object var5 = this.fillColor.getColor();
        Object var6 = this.outlineColor.getColor();
        if (this.color.getValue().equalsIgnoreCase("Static")) {
            if (((double) arg2) >= 0.9) {
            } else {
            }
            var5 = new Color(255, 0, 0, this.fillColor.getAlpha());
            if (((double) arg2) >= 0.9) {
            } else {
            }
            var6 = new Color(255, 0, 0, this.outlineColor.getAlpha());
            break label_8;
        }
        if (!(this.color.getValue().equalsIgnoreCase("Smooth"))) break label_8;
        var5 = new Color((255 - ((int) (Mth.clamp(arg2, 0.0f, 1.0f) * 255.0f))), ((int) (Mth.clamp(arg2, 0.0f, 1.0f) * 255.0f)), 0, this.fillColor.getAlpha());
        var6 = new Color((255 - ((int) (Mth.clamp(arg2, 0.0f, 1.0f) * 255.0f))), ((int) (Mth.clamp(arg2, 0.0f, 1.0f) * 255.0f)), 0, this.outlineColor.getAlpha());
        label_8:
        if (!(this.render.getValue().equalsIgnoreCase("Fill"))) {
            if (this.render.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBox(arg0, var4, var5);
        }
        if (!(this.render.getValue().equalsIgnoreCase("Outline"))) {
            if (this.render.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBoxOutline(arg0, var4, var6);
        }
    }

    private void serverSend(Packet arg0) {
        if (!(isProxyActive())) break label_0;
        var tmp0 = LightningClient.PROXY_SERVER.getServerConnection();
        Object var2 = tmp0;
        if ((tmp0 == null) || (!(var2.isConnected()))) break label_0;
        ProxyServerTickListener.allowSend(() -> arg0.send(arg1));
        return;
        label_0:
        mc.getConnection().send(arg0);
    }

    private void serverSendSequenced(IntFunction arg0) {
        if (!(isProxyActive())) break label_8;
        Object var2 = ((ClientWorldAccessor) mc.level).invokeGetPendingUpdateManager().startPredicting();
        try {
            serverSend(((Packet) arg0.apply(var2.currentSequence())));
            if (var2 == null) break label_7;
            var2.close();
        } catch (Throwable ex2) {
            Object var3 = ex2;
            if (var2 == null) break label_6;
            try {
                var2.close();
            } catch (Throwable ex5) {
                Object var4 = ex5;
                var3.addSuppressed(var4);
            }
            label_6:
            throw var3;
        }
        label_7:
        break label_9;
        label_8:
        NetworkUtils.sendSequencedPacket((arg1) -> {
    Object var2 = ((Packet) arg0.apply(arg1));
    return var2;
});
        label_9:
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action arg0, BlockPos arg1, Direction arg2) {
        sendRawPlayerAction(arg0, arg1, arg2, false);
    }

    private void sendRawPlayerAction(ServerboundPlayerActionPacket.Action arg0, BlockPos arg1, Direction arg2, boolean arg3) {
        if (arg0 == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            markStop(arg3);
        }
        Object var5 = ((ClientWorldAccessor) mc.level).invokeGetPendingUpdateManager().startPredicting();
        try {
            serverSend(new ServerboundPlayerActionPacket(arg0, arg1, arg2, var5.currentSequence()));
            if (var5 == null) break label_8;
            var5.close();
        } catch (Throwable ex3) {
            Object var6 = ex3;
            if (var5 == null) break label_7;
            try {
                var5.close();
            } catch (Throwable ex6) {
                Object var7 = ex6;
                var6.addSuppressed(var7);
            }
            label_7:
            throw var6;
        }
        label_8:
    }


    public class Secondary {
        private final BlockPos position;
        private final int priority;
        private BlockState state;
        private float progress;
        private float prevProgress;
        private int ticks;
        private boolean holding;
        private int holdTicks;
        final SpeedMineModule this$0;

        private Secondary(SpeedMineModule arg0, SpeedMineModule arg1, BlockPos arg2, int arg3, BlockState arg4, float arg5) {
            Objects.requireNonNull(arg0);
            Object var7 = arg1;
            Objects.requireNonNull(var7);
            this.this$0 = var7;
            this.position = arg2;
            this.priority = arg3;
            this.state = arg4;
            this.progress = arg5;
            this.prevProgress = arg5;
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public int getPriority() {
            return this.priority;
        }

        public boolean isMining() {
            return true;
        }

        public boolean isHolding() {
            return this.holding;
        }

        public float getSpeed() {
            return 1.0f;
        }

        public float getProgress() {
            return Mth.clamp(this.progress, 0.0f, 1.0f);
        }

        public boolean process() {
            if (this.this$0.isOutOfRange(this.position)) {
                release();
                return true;
            }
            if (IMinecraft.mc.player == null) break label_2;
            if (IMinecraft.mc.player.isUsingItem()) break label_1;
            if (!(EntityUtils.isEating())) break label_2;
            label_1:
            break label_3;
            label_2:
            label_3:
            int var4 = 0;
            int var5 = 0;
            if (!(!this.this$0.interactPaused) && !var4 && (System.currentTimeMillis() - this.this$0.interactPausedAt) >= 750L) {
                this.this$0.setInteractPaused(false);
            }
            var tmp0 = IMinecraft.mc.level.getBlockState(this.position);
            Object var3 = tmp0;
            if (tmp0.canBeReplaced()) {
                LightningClient.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
                this.this$0.mineTimer.reset();
                release();
                return true;
            }
            this.state = var3;
            int var6 = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (var6 == -1) {
                var6 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            var tmp1 = (WorldUtils.getMineSpeed(this.state, var6) / LightningClient.WORLD_MANAGER.getTimerMultiplier());
            float var2 = tmp1;
            if (tmp1 <= 0.0f) {
                release();
                return true;
            }
            this.ticks = (this.ticks + 1);
            this.prevProgress = this.progress;
            this.progress = (this.progress + var2);
            if (!(this.this$0.whileEating.getValue()) && !(!var4)) {
                if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                }
            } else {
            }
            int var1 = 0;
            int var7 = 0;
            if (!this.holding && (this.progress + var2) >= 1.0f && !(!var1)) {
                hold(var6);
            }
            if ((!this.holding) || (this.this$0.farReach.getValue())) break label_12;
            this.holdTicks = (this.holdTicks + 1);
            if ((this.holdTicks + 1) < 3) break label_12;
            release();
            return true;
            label_12:
            if (this.progress >= (1.0f + (var2 * 10.0f))) {
                release();
                return true;
            }
            if (this.ticks > 60) {
                release();
                return true;
            }
            return false;
        }

        private void hold(int arg0) {
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) return;
            int var2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            this.holding = true;
            if (var2 == arg0) return;
            this.this$0.secondaryOriginalSlot = var2;
            this.this$0.secondaryHoldSlot = arg0;
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(arg0));
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), arg0, var2);
            }
        }

        private void release() {
            if (!this.holding) return;
            this.holding = false;
            if (this.this$0.secondaryHoldSlot == -1) return;
            int var1 = this.this$0.secondaryOriginalSlot;
            int var2 = this.this$0.secondaryHoldSlot;
            this.this$0.secondaryHoldSlot = -1;
            this.this$0.secondaryOriginalSlot = -1;
            if (IMinecraft.mc.player == null) return;
            if (this.this$0.switchMode.getValue().equalsIgnoreCase("Normal") && IMinecraft.mc.player.getInventory().getSelectedSlot() == var1) return;
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var1));
            } else {
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), var2, var1);
            }
        }

        public void render(PoseStack arg0) {
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) return;
            double var2 = ((double) Mth.clamp(Mth.lerp(IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), this.prevProgress, this.progress), 0.0f, 1.0f));
            Object var4 = new AABB(this.position);
            if (!(this.this$0.animation.getValue().equalsIgnoreCase("Expand"))) break label_1;
            var4 = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((var2 / 2.0), 0.0, 0.5));
            label_1:
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                var4 = new AABB(((double) this.position.getX()), ((double) this.position.getY()), ((double) this.position.getZ()), (((double) this.position.getX()) + 1.0), (((double) this.position.getY()) + var2), (((double) this.position.getZ()) + 1.0));
            }
            Object var5 = this.this$0.fillColor.getColor();
            Object var6 = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                if (var2 >= 0.9) {
                } else {
                }
                var5 = new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                if (var2 >= 0.9) {
                } else {
                }
                var6 = new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
                break label_8;
            }
            if (!(this.this$0.color.getValue().equalsIgnoreCase("Smooth"))) break label_8;
            var5 = new Color((255 - ((int) (var2 * 255.0))), ((int) (var2 * 255.0)), 0, this.this$0.fillColor.getAlpha());
            var6 = new Color((255 - ((int) (var2 * 255.0))), ((int) (var2 * 255.0)), 0, this.this$0.outlineColor.getAlpha());
            label_8:
            if (!(this.this$0.render.getValue().equalsIgnoreCase("Fill"))) {
                if (this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBox(arg0, var4, var5);
            }
            if (!(this.this$0.render.getValue().equalsIgnoreCase("Outline"))) {
                if (this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBoxOutline(arg0, var4, var6);
            }
        }

        @Generated
        public BlockState getState() {
            return this.state;
        }

        @Generated
        public float getPrevProgress() {
            return this.prevProgress;
        }

        @Generated
        public int getTicks() {
            return this.ticks;
        }

        @Generated
        public int getHoldTicks() {
            return this.holdTicks;
        }

    }

    public record Target(Player player, List feetPositions, BlockPos position) {

        public Target(Player arg0, List arg1, BlockPos arg2) {
            this.player = arg0;
            this.feetPositions = arg1;
            this.position = arg2;
        }

        public Player player() {
            return this.player;
        }

        public List feetPositions() {
            return this.feetPositions;
        }

        public BlockPos position() {
            return this.position;
        }

    }

    public record Position(BlockPos position, boolean feetPosition) {

        public Position(BlockPos arg0, boolean arg1) {
            this.position = arg0;
            this.feetPosition = arg1;
        }

        public BlockPos position() {
            return this.position;
        }

        public boolean feetPosition() {
            return this.feetPosition;
        }

    }

    public class Action {
        private final BlockPos position;
        private BlockState state;
        private final int priority;
        private float progress;
        private float prevProgress;
        private int attempts;
        private int ticks;
        private boolean mining;
        private long stallTime;
        private boolean instantMine;
        private int startSlot;
        private boolean started;
        private int brokenCount;
        private int lastBrokenCount;
        private final Timer instantRemineResetTimer;
        private float ghostIronProgress;
        private boolean ironSwapped;
        private int swappedIronInventorySlot;
        private int swappedIronHotbarSlot;
        private boolean terrainBase;
        private boolean terrainSurround;
        private final boolean bedrockOrigin;
        final SpeedMineModule this$0;

        public Action(SpeedMineModule arg0, SpeedMineModule arg1, BlockPos arg2, int arg3) {
            Objects.requireNonNull(arg0);
            this.this$0$ = arg0;
            Object var5 = arg1;
            Objects.requireNonNull(var5);
            this.this$0 = var5;
            this.startSlot = -1;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer = new Timer();
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.position = arg2;
            this.state = IMinecraft.mc.level.getBlockState(arg2);
            this.bedrockOrigin = this.state.getBlock() == Blocks.BEDROCK;
            this.priority = arg3;
            tryStart();
        }

        public BlockPos getPosition() {
            return this.position;
        }

        public BlockState getState() {
            return this.state;
        }

        public float getProgress() {
            return this.progress;
        }

        public int getTicks() {
            return this.ticks;
        }

        public boolean isMining() {
            return this.mining;
        }

        public boolean isInstantMine() {
            return this.instantMine;
        }

        public int getPriority() {
            return this.priority;
        }

        public boolean isTerrainBase() {
            return this.terrainBase;
        }

        public void setTerrainBase(boolean arg0) {
            this.terrainBase = arg0;
        }

        public boolean isTerrainSurround() {
            return this.terrainSurround;
        }

        public void setTerrainSurround(boolean arg0) {
            this.terrainSurround = arg0;
        }

        public boolean isStarted() {
            return this.started;
        }

        private int getBreakSlot(BlockState arg0, int arg1) {
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var4 = null;
            Object var5 = null;
            if (var4 != null && var4.isToggled() && var4.ironPickaxe.getValue() && !(!this.ironSwapped)) {
                var tmp0 = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                int var3 = tmp0;
                if (tmp0 != -1) return var3;
            }
            return arg1;
        }

        private long asyncFireDelayMs() {
            long var3 = (this.this$0.instantDelay.getValue().longValue() * 50L);
            if (this.position == null) return var3;
            Object var5 = this.this$0.getTarget();
            if (var5 == null) break label_1;
            if (var5.player() == null) {
                label_1:
                return var3;
            }
            if (this.position.getY() != (Mth.floor(var5.player().getY()) + 2)) break label_3;
            var tmp0 = (this.this$0.isTargetSurroundPosition(this.position, var5.player()) ? 1 : 0);
            int var2 = tmp0;
            int var6 = tmp0;
            if (!var2) return var3;
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var7 = null;
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var8 = null;
            if (var7 == null) break label_10;
            int var9 = (var7.isToggled() ? 1 : 0);
            if ((var8 == null) || (!(var8.isToggled()))) break label_12;
            var tmp1 = (var8.priority.getValue().equalsIgnoreCase("Head") ? 1 : 0);
            int var1 = tmp1;
            int var10 = tmp1;
            if (!(!var9) && !(!var1)) return Math.max(var3, ((long) LightningClient.SERVER_MANAGER.getPing()));
            return var3;
        }

        private void updateIronGhost() {
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var3 = null;
            if ((var3 == null) || (!(var3.isToggled()))) break label_2;
            var tmp0 = (var3.ironPickaxe.getValue() ? 1 : 0);
            int var2 = tmp0;
            int var4 = tmp0;
            if ((!var2) || (this.ironSwapped)) break label_6;
            var tmp1 = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
            int var1 = tmp1;
            if (((tmp1 == -1) || (this.state == null)) || (this.state.isAir())) break label_6;
            float var5 = (WorldUtils.getMineSpeed(this.state, var1) / LightningClient.WORLD_MANAGER.getTimerMultiplier());
            this.ghostIronProgress = Mth.clamp((this.ghostIronProgress + var5), 0.0f, getSpeed());
            if (this.ghostIronProgress < getSpeed()) break label_6;
            int var6 = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (var6 == -1) {
                var6 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            if (var1 > InventoryUtils.HOTBAR_END) {
                InventoryUtils.swap("Swap", var1, var6);
                this.ironSwapped = true;
                this.swappedIronInventorySlot = var1;
                this.swappedIronHotbarSlot = var6;
                break label_6;
            }
            if (var1 == -1) break label_6;
            this.ironSwapped = true;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = var1;
            label_6:
        }

        public void restoreIronSwap() {
            if (!(!this.ironSwapped)) {
                if (this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END && this.swappedIronHotbarSlot >= 0 && this.swappedIronHotbarSlot <= InventoryUtils.HOTBAR_END) {
                    if ((IMinecraft.mc.player == null) || (!(IMinecraft.mc.player.getInventory().getItem(this.swappedIronHotbarSlot).is(Items.IRON_PICKAXE)))) break label_6;
                    InventoryUtils.swap("Swap", this.swappedIronInventorySlot, this.swappedIronHotbarSlot);
                    break label_6;
                }
                if (IMinecraft.mc.player == null) break label_6;
                var tmp0 = InventoryUtils.find(Items.IRON_PICKAXE, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
                int var1 = tmp0;
                if (tmp0 == -1) break label_6;
                if (this.state != null && !(this.state.isAir())) {
                } else {
                }
                Object var3 = Blocks.OBSIDIAN.defaultBlockState();
                if (this.swappedIronInventorySlot > InventoryUtils.HOTBAR_END) {
                } else {
                }
                var tmp1 = InventoryUtils.findFastestItem(var3, (InventoryUtils.HOTBAR_END + 1), InventoryUtils.INVENTORY_END);
                int var2 = tmp1;
                int var4 = tmp1;
                if (var2 == -1) {
                    var2 = InventoryUtils.findEmptySlot((InventoryUtils.HOTBAR_END + 1), InventoryUtils.INVENTORY_END);
                }
                if ((var2 == -1) || (var2 <= InventoryUtils.HOTBAR_END)) break label_6;
                InventoryUtils.swap("Swap", var2, var1);
                label_6:
                this.ironSwapped = false;
                this.swappedIronInventorySlot = -1;
                this.swappedIronHotbarSlot = -1;
                this.ghostIronProgress = 0.0f;
            }
        }

        private SpeedMineModule.Secondary demote() {
            restoreIronSwap();
            if (!this.started) return null;
            Object var1 = IMinecraft.mc.level.getBlockState(this.position);
            if (var1.canBeReplaced()) return null;
            if (this.this$0.sixB.getValue() && var1.getBlock() == Blocks.BEDROCK) return null;
            if (!this.instantMine && this.progress < getSpeed()) {
                Object var3 = WorldUtils.getClosestDirection(this.position, true);
                if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                } else {
                    if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
                        if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                        }
                    } else {
                    }
                }
                int var2 = InventoryUtils.findFastestItem(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
                if (var2 == -1) {
                    var2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                fireBreakBurst(var3, var2, true);
            }
            return new SpeedMineModule.Secondary(this.this$0$, this.this$0, this.position, this.priority, var1, this.progress);
        }

        private void markBroken() {
            this.brokenCount = (this.brokenCount + 1);
        }

        private boolean ackStalled() {
            if (this.brokenCount != this.lastBrokenCount) {
                this.instantRemineResetTimer.reset();
                this.lastBrokenCount = this.brokenCount;
            }
            return this.instantRemineResetTimer.hasTimeElapsed(200L);
        }

        private boolean instantAckStalled() {
            if (!(IMinecraft.mc.level.getBlockState(this.position).canBeReplaced())) break label_0;
            return ackStalled();
        }

        private void tryStart() {
            Object var1 = this.this$0.interactSyncLock;
            Object var2 = var1;
            // synchronized (var1)
            if (!(this.this$0.canStartNow())) return;
            start();
            break label_6;
            Object var3 = null;
            throw var3;
            label_6:
        }

        public boolean process() {
            if (this.this$0.isOutOfRange(this.position)) {
                cancel();
                return true;
            }
            updateIronGhost();
            if (IMinecraft.mc.player == null) break label_2;
            if (IMinecraft.mc.player.isUsingItem()) break label_1;
            if (!(EntityUtils.isEating())) break label_2;
            label_1:
            break label_3;
            label_2:
            label_3:
            int var1 = 0;
            int var2 = 0;
            if (((!this.this$0.interactPaused) || (var1)) || ((System.currentTimeMillis() - this.this$0.interactPausedAt) < 750L)) break label_4;
            this.this$0.setInteractPaused(false);
            label_4:
            if ((!this.this$0.needsRestart) || (!(this.this$0.isProxyActive()))) break label_5;
            this.this$0.needsRestart = false;
            this.started = false;
            label_5:
            if (!this.started) {
                if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) return true;
                tryStart();
                return false;
            }
            if (!(this.this$0.farReach.getValue())) return legacyProcess();
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                if (!this.brokenCount) {
                    markBroken();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    this.instantMine = true;
                    return false;
                }
                if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                    this.progress = 0.0f;
                    this.prevProgress = 0.0f;
                }
                if (!(!this.instantMine)) {
                    if (this.this$0.async.getValue()) {
                        if (!(this.this$0.whileEating.getValue())) {
                            if (EntityUtils.isEating()) break label_19;
                        }
                        if ((isAsyncFireBlockedByCombo()) || (!(this.this$0.instantTimer.hasTimeElapsed(asyncFireDelayMs())))) break label_19;
                        Object var5 = WorldUtils.getClosestDirection(this.position, true);
                        if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                        } else {
                            if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
                                if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                                }
                            } else {
                            }
                        }
                        int var4 = InventoryUtils.findFastestItem(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
                        if (var4 == -1) {
                            var4 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                        }
                        fireBreakBurst(var5, getBreakSlot(this.state, var4), false, false);
                        this.this$0.instantTimer.reset();
                        this.attempts = (this.attempts + 1);
                        label_19:
                        return false;
                    }
                    if (instantAckStalled()) return false;
                    var4 = this.this$0.instantTimeout.getValue().longValue();
                    if (var4 <= 0L) break label_22;
                    var tmp0 = (this.this$0.instantTimer.hasTimeElapsed((var4 * 50L)) ? 1 : 0);
                    int var3 = tmp0;
                    int var6 = tmp0;
                    if (!var3) return false;
                }
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend(new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
                }
                cancel();
                return true;
            }
            var3 = WorldUtils.getClosestDirection(this.position, true);
            var4 = IMinecraft.mc.level.getBlockState(this.position);
            if (!(var4.canBeReplaced()) && var4.getBlock() != this.state.getBlock()) {
                this.state = var4;
            }
            if (!(!this.mining)) {
                this.ticks = (this.ticks + 1);
                if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                } else {
                    if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
                        if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                        }
                    } else {
                    }
                }
                int var7 = InventoryUtils.findFastestItem(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
                if (var7 == -1) {
                    var7 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                } else {
                }
                var tmp1 = (WorldUtils.getMineSpeed(this.state, var7) / LightningClient.WORLD_MANAGER.getTimerMultiplier());
                var6 = tmp1;
                float var8 = tmp1;
                if (!(!this.instantMine)) {
                    this.prevProgress = getSpeed();
                    this.progress = getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((this.progress + var6), 0.0f, getSpeed());
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                    if (this.this$0.isProxyActive()) {
                        this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    } else {
                        IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                    }
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if ((!(this.this$0.rotate.getValue().equalsIgnoreCase("Normal"))) || ((this.progress + (var6 * 2.0f)) < getSpeed())) break label_42;
                Object var9 = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, var3));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend(new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), var9[0], var9[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    break label_42;
                }
                LightningClient.ROTATION_MANAGER.legacyRotate(var9, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                label_42:
                if (IMinecraft.mc.player == null) break label_44;
                if (!(IMinecraft.mc.player.isUsingItem())) {
                    if (!(EntityUtils.isEating())) break label_44;
                }
                break label_45;
                label_44:
                label_45:
                var9 = 0;
                if (!this.this$0.interactPaused) {
                    if (!(this.this$0.whileEating.getValue()) && !(!var9)) {
                    }
                } else {
                }
                var5 = 0;
                int var10 = 0;
                if (this.progress >= getSpeed() && !(var4.canBeReplaced()) && !var5) {
                    if (!(!this.instantMine) && !this.brokenCount && ackStalled()) {
                        start();
                        return false;
                    }
                    if (!(!this.instantMine) && !(this.this$0.async.getValue())) {
                        if (!(this.this$0.instantTimer.hasTimeElapsed((this.this$0.instantDelay.getValue().longValue() * 50L)))) break label_58;
                    }
                    fireBreakBurst(var3, getBreakSlot(this.state, var7), false, !this.instantMine);
                    if (!this.instantMine) {
                        this.this$0.mineTimer.reset();
                    }
                    this.attempts = (this.attempts + 1);
                    if (this.this$0.rebreak.getValue().equalsIgnoreCase("None")) {
                        if (this.this$0.sixB.getValue()) {
                        }
                        if (!(!this.terrainBase)) {
                        }
                        this.mining = false;
                        this.stallTime = System.currentTimeMillis();
                        break label_58;
                    }
                    this.instantMine = true;
                    if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                        this.progress = 0.0f;
                        this.prevProgress = 0.0f;
                    }
                    this.this$0.instantTimer.reset();
                    this.instantRemineResetTimer.reset();
                    label_58:
                    return false;
                }
                break label_62;
            }
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) break label_62;
            if (!(!this.attempts)) {
                if ((System.currentTimeMillis() - this.stallTime) < 150L) break label_62;
            }
            tryStart();
            label_62:
            return false;
        }

        private boolean legacyProcess() {
            updateIronGhost();
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) {
                if (!this.brokenCount) {
                    markBroken();
                }
                if ((!(this.this$0.sixB.getValue())) || (this.state.getBlock() != Blocks.BEDROCK)) break label_1;
                this.instantMine = true;
                return false;
                label_1:
                if (!(!this.instantMine)) {
                    if (this.this$0.async.getValue()) {
                        if (this.this$0.whileEating.getValue()) break label_2;
                        if (EntityUtils.isEating()) break label_9;
                        label_2:
                        if ((isAsyncFireBlockedByCombo()) || (!(this.this$0.instantTimer.hasTimeElapsed(asyncFireDelayMs())))) break label_9;
                        Object var3 = WorldUtils.getClosestDirection(this.position, true);
                        if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("None"))) break label_3;
                        break label_7;
                        label_3:
                        if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap")) break label_4;
                        if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                            label_4:
                        } else {
                        }
                        label_7:
                        int var2 = InventoryUtils.findFastestItem(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
                        if (var2 == -1) {
                            var2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                        }
                        legacyFireBreakBurst(var3, getBreakSlot(this.state, var2));
                        this.this$0.instantTimer.reset();
                        this.attempts = (this.attempts + 1);
                        label_9:
                        return false;
                    }
                    if (instantAckStalled()) return false;
                    var2 = this.this$0.instantTimeout.getValue().longValue();
                    if (var2 <= 0L) break label_12;
                    var tmp0 = (this.this$0.instantTimer.hasTimeElapsed((var2 * 50L)) ? 1 : 0);
                    int var1 = tmp0;
                    int var4 = tmp0;
                    if (!var1) return false;
                }
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend(new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
                }
                cancel();
                return true;
            }
            var1 = WorldUtils.getClosestDirection(this.position, true);
            var2 = IMinecraft.mc.level.getBlockState(this.position);
            if (!(var2.canBeReplaced()) && var2.getBlock() != this.state.getBlock()) {
                this.state = var2;
            }
            if (!(!this.mining)) {
                this.ticks = (this.ticks + 1);
                if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                } else {
                    if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
                        if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                        }
                    } else {
                    }
                }
                var4 = InventoryUtils.findFastestItem(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
                if (var4 == -1) {
                    var4 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                }
                if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                } else {
                }
                var tmp1 = (WorldUtils.getMineSpeed(this.state, var4) / LightningClient.WORLD_MANAGER.getTimerMultiplier());
                var3 = tmp1;
                float var5 = tmp1;
                if (!(!this.instantMine)) {
                    this.prevProgress = getSpeed();
                    this.progress = getSpeed();
                } else {
                    this.prevProgress = this.progress;
                    this.progress = Mth.clamp((this.progress + var3), 0.0f, getSpeed());
                }
                if ((!(this.this$0.sixB.getValue())) || (this.state.getBlock() != Blocks.BEDROCK)) break label_31;
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                } else {
                    IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
                if (IMinecraft.mc.player != null && IMinecraft.mc.player.isUsingItem()) {
                    if (IMinecraft.mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND) break label_31;
                }
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                label_31:
                if ((!(this.this$0.rotate.getValue().equalsIgnoreCase("Normal"))) || ((this.progress + (var3 * 2.0f)) < getSpeed())) break label_33;
                Object var6 = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, var1));
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend(new ServerboundMovePlayerPacket.PosRot(IMinecraft.mc.player.getX(), IMinecraft.mc.player.getY(), IMinecraft.mc.player.getZ(), var6[0], var6[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                    break label_33;
                }
                LightningClient.ROTATION_MANAGER.legacyRotate(var6, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this.this$0));
                label_33:
                if ((this.progress < getSpeed()) || (var2.canBeReplaced())) break label_42;
                if (!(this.this$0.whileEating.getValue())) {
                    if (EntityUtils.isEating()) break label_42;
                }
                if (!(!this.instantMine) && !this.brokenCount && ackStalled()) {
                    start();
                    return false;
                }
                if (!(!this.instantMine) && !(this.this$0.async.getValue())) {
                    if (!(this.this$0.instantTimer.hasTimeElapsed((this.this$0.instantDelay.getValue().longValue() * 50L)))) break label_41;
                }
                legacyFireBreakBurst(var1, getBreakSlot(this.state, var4));
                this.attempts = (this.attempts + 1);
                if (this.this$0.rebreak.getValue().equalsIgnoreCase("None")) {
                    if (this.this$0.sixB.getValue()) {
                    }
                    if (!(!this.terrainBase)) {
                    }
                    this.mining = false;
                    this.stallTime = System.currentTimeMillis();
                    break label_41;
                }
                this.instantMine = true;
                if (this.this$0.rebreak.getValue().equalsIgnoreCase("Fast")) {
                    this.progress = 0.0f;
                    this.prevProgress = 0.0f;
                }
                this.this$0.instantTimer.reset();
                this.instantRemineResetTimer.reset();
                label_41:
                return false;
                label_42:
                break label_45;
            }
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced()) break label_45;
            if (!(!this.attempts)) {
                if ((System.currentTimeMillis() - this.stallTime) < 150L) break label_45;
            }
            start();
            label_45:
            return false;
        }

        private boolean isHeadCrystalComboPending() {
            if (LightningClient.MODULE_MANAGER != null) {
                break label_1;
            }
            label_1:
            Object var2 = null;
            Object var3 = null;
            if ((var2 == null) || (!(var2.isToggled()))) {
                return false;
            }
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var1 = null;
            Object var4 = null;
            if ((var1 == null) || (!(var1.isToggled()))) {
                return false;
            }
            Object var5 = this.this$0.getTarget();
            if ((var5 == null) || (var5.player() == null)) {
                return false;
            }
            int var6 = Mth.floor(var5.player().getY());
            if ((this.position.getY() < (var6 + 2)) || (Math.abs(((((double) this.position.getX()) + 0.5) - var5.player().getX())) >= 1.3)) break label_10;
            return Math.abs(((((double) this.position.getZ()) + 0.5) - var5.player().getZ())) < 1.3;
        }

        private boolean isCivBreakComboPending() {
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var1 = null;
            if ((var1 == null) || (!(var1.isToggled()))) break label_2;
            return var1.isUpComboActive(this.position);
        }

        private boolean isAsyncFireBlockedByCombo() {
            if (!(isHeadCrystalComboPending())) {
                if (isCivBreakComboPending()) {
                }
            } else {
            }
            return false;
        }

        private void checkAndPlaceHeadCrystal(BlockPos arg0) {
            if (((IMinecraft.mc.player == null) || (IMinecraft.mc.level == null)) || (IMinecraft.mc.getConnection() == null)) {
                return;
            }
            if (!this.instantMine) return;
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var5 = null;
            Object var6 = null;
            if ((var5 == null) || (!(var5.isToggled()))) {
                return;
            }
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var4 = null;
            Object var7 = null;
            if ((var4 == null) || (!(var4.isToggled()))) {
                return;
            }
            Object var8 = IMinecraft.mc.level.getBlockState(arg0);
            if (var8.getBlock() != Blocks.OBSIDIAN) return;
            Object var9 = this.this$0.getTarget();
            if ((var9 == null) || (var9.player() == null)) {
                return;
            }
            int var10 = Mth.floor(var9.player().getY());
            if ((arg0.getY() < (var10 + 2)) || (Math.abs(((((double) arg0.getX()) + 0.5) - var9.player().getX())) >= 1.3)) break label_14;
            var tmp0 = (Math.abs(((((double) arg0.getZ()) + 0.5) - var9.player().getZ())) < 1.3 ? 1 : 0);
            int var3 = tmp0;
            int var11 = tmp0;
            if (!var3) return;
            Object var12 = arg0.above();
            if (!(WorldUtils.isCrystalPlaceable(var12))) return;
            int var13 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
            var tmp1 = (IMinecraft.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? 1 : 0);
            int var2 = tmp1;
            int var14 = tmp1;
            if (!var2 && var13 == -1) return;
            Object var15 = (!(!var2) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            Object var16 = new Vec3((((double) arg0.getX()) + 0.5), (((double) arg0.getY()) + 1.0), (((double) arg0.getZ()) + 0.5));
            Object var17 = new BlockHitResult(var16, Direction.UP, arg0, false);
            if (this.this$0.isProxyActive()) {
                if (!(!var2)) {
                    this.this$0.serverSendSequenced((arg2) -> new ServerboundUseItemOnPacket(arg0, arg1, arg2));
                    this.this$0.serverSend(new ServerboundSwingPacket(var15));
                    break label_26;
                }
                int var18 = IMinecraft.mc.player.getInventory().getSelectedSlot();
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var13));
                this.this$0.serverSendSequenced((arg2) -> new ServerboundUseItemOnPacket(arg0, arg1, arg2));
                this.this$0.serverSend(new ServerboundSwingPacket(var15));
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var18));
                break label_26;
            }
            if (!(!var2)) {
                NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemOnPacket(arg0, arg1, arg2));
                IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(var15));
                break label_26;
            }
            var18 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            InventoryUtils.switchSlot("Silent", var13, var18);
            NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemOnPacket(arg0, arg1, arg2));
            IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(var15));
            InventoryUtils.switchBack("Silent", var13, var18);
            label_26:
        }

        private void legacyFireBreakBurst(Direction arg0, int arg1) {
            checkAndPlaceHeadCrystal(this.position);
            LightningClient.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            if (!(this.this$0.rotate.getValue().equalsIgnoreCase("Silent"))) break label_1;
            Object var3 = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, arg0));
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend(new ServerboundMovePlayerPacket.Rot(var3[0], var3[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
                break label_1;
            }
            LightningClient.ROTATION_MANAGER.wireRotate("Silent", var3);
            label_1:
            var3 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int var5 = (this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : arg1);
                if (var5 == -1) break label_4;
                var tmp0 = (var5 != var3 ? 1 : 0);
                int var4 = tmp0;
                int var6 = tmp0;
                if (!(!var4)) {
                    this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var5));
                }
                stopDestroyBlock(this.position, arg0, true);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), arg0));
                }
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (!(!var4)) {
                    this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var3));
                }
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), arg1, var3);
                stopDestroyBlock(this.position, arg0, true);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), arg0));
                }
                IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), arg1, var3);
            }
        }

        private void fireBreakBurst(Direction arg0, int arg1, boolean arg2) {
            fireBreakBurst(arg0, arg1, arg2, !arg2);
        }

        private void fireBreakBurst(Direction arg0, int arg1, boolean arg2, boolean arg3) {
            if (!arg2) {
                checkAndPlaceHeadCrystal(this.position);
                LightningClient.EVENT_HANDLER.post(new DestroyBlockEvent(this.position));
            }
            if (!(this.this$0.rotate.getValue().equalsIgnoreCase("Silent"))) break label_2;
            Object var5 = RotationUtils.getRotations(WorldUtils.getHitVector(this.position, arg0));
            if (!(this.this$0.isProxyActive())) break label_1;
            this.this$0.serverSend(new ServerboundMovePlayerPacket.Rot(var5[0], var5[1], IMinecraft.mc.player.onGround(), IMinecraft.mc.player.horizontalCollision));
            break label_2;
            label_1:
            LightningClient.ROTATION_MANAGER.wireRotate("Silent", var5);
            label_2:
            var5 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            if (this.this$0.isProxyActive()) {
                int var7 = (this.startSlot != -1 ? this.startSlot : var5);
                var tmp0 = (this.this$0.switchMode.getValue().equalsIgnoreCase("None") ? -1 : arg1);
                int var6 = tmp0;
                int var8 = tmp0;
                if (var6 == -1) break label_7;
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var6));
                if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("Normal"))) break label_7;
                IMinecraft.mc.player.getInventory().setSelectedSlot(var6);
                label_7:
                stopDestroyBlock(this.position, arg0, !arg2, arg3);
                if (this.this$0.grim.getValue()) {
                    this.this$0.serverSend(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), arg0));
                }
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                if (var6 == -1) break label_12;
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var7));
                if (!(this.this$0.switchMode.getValue().equalsIgnoreCase("Normal"))) break label_12;
                IMinecraft.mc.player.getInventory().setSelectedSlot(var7);
                label_12:
            } else {
                InventoryUtils.switchSlot(this.this$0.switchMode.getValue(), arg1, var5);
                stopDestroyBlock(this.position, arg0, !arg2, arg3);
                if (this.this$0.grim.getValue()) {
                    IMinecraft.mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position.above(500), arg0));
                }
                IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                if (IMinecraft.mc.level.getBlockState(this.position).getBlock() == Blocks.BEDROCK) {
                    IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
                }
                InventoryUtils.switchBack(this.this$0.switchMode.getValue(), arg1, var5);
            }
        }

        private void stopDestroyBlock(BlockPos arg0, Direction arg1, boolean arg2) {
            stopDestroyBlock(arg0, arg1, arg2, false);
        }

        private void stopDestroyBlock(BlockPos arg0, Direction arg1, boolean arg2, boolean arg3) {
            Object var5 = ((ClientWorldAccessor) IMinecraft.mc.level).invokeGetPendingUpdateManager().startPredicting();
            try {
                Object var6 = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, arg0, arg1, var5.currentSequence());
                this.this$0.markStop(arg3);
                if (this.this$0.isProxyActive()) {
                    this.this$0.serverSend(var6);
                } else {
                    IMinecraft.mc.getConnection().send(var6);
                }
                if (var5 == null) break label_8;
                var5.close();
            } catch (Throwable ex3) {
                var6 = ex3;
                if (var5 == null) break label_7;
                try {
                    var5.close();
                } catch (Throwable ex6) {
                    Object var7 = ex6;
                    var6.addSuppressed(var7);
                }
                label_7:
                throw var6;
            }
            label_8:
        }

        public void render(PoseStack arg0) {
            if (IMinecraft.mc.level.getBlockState(this.position).canBeReplaced() && !this.instantMine) return;
            Object var4 = new AABB(this.position);
            if (!(IMinecraft.mc.level.getBlockState(this.position).canBeReplaced())) break label_1;
            int var5 = (!(!this.instantMine) ? 1 : 0);
            if (!(!var5)) {
            } else {
            }
            double var2 = ((double) Mth.clamp(Mth.lerp(IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (this.prevProgress / getSpeed()), (this.progress / getSpeed())), 0.0f, 1.0f));
            double var6 = ((double) Mth.clamp(Mth.lerp(IMinecraft.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), (this.prevProgress / getSpeed()), (this.progress / getSpeed())), 0.0f, 1.0f));
            if (this.this$0.animation.getValue().equalsIgnoreCase("Expand")) {
                var4 = new AABB(this.position).deflate(0.5).inflate(Mth.clamp((var2 / 2.0), 0.0, 0.5));
            }
            if (this.this$0.animation.getValue().equalsIgnoreCase("Rise")) {
                var4 = new AABB(((double) this.position.getX()), ((double) this.position.getY()), ((double) this.position.getZ()), (((double) this.position.getX()) + 1.0), (((double) this.position.getY()) + var2), (((double) this.position.getZ()) + 1.0));
            }
            Object var8 = this.this$0.fillColor.getColor();
            Object var9 = this.this$0.outlineColor.getColor();
            if (this.this$0.color.getValue().equalsIgnoreCase("Static")) {
                if (var2 >= 0.9) {
                } else {
                }
                var8 = new Color(255, 0, 0, this.this$0.fillColor.getAlpha());
                if (var2 >= 0.9) {
                } else {
                }
                var9 = new Color(255, 0, 0, this.this$0.outlineColor.getAlpha());
                break label_12;
            }
            if (!(this.this$0.color.getValue().equalsIgnoreCase("Smooth"))) break label_12;
            var8 = new Color((255 - ((int) (Mth.clamp(var2, 0.0, 1.0) * 255.0))), ((int) (Mth.clamp(var2, 0.0, 1.0) * 255.0)), 0, this.this$0.fillColor.getAlpha());
            var9 = new Color((255 - ((int) (Mth.clamp(var2, 0.0, 1.0) * 255.0))), ((int) (Mth.clamp(var2, 0.0, 1.0) * 255.0)), 0, this.this$0.outlineColor.getAlpha());
            label_12:
            if (var2 >= ((double) getSpeed()) && !(!this.instantMine) && this.this$0.instantRender.getValue().equalsIgnoreCase("Custom")) {
                var8 = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.fillColor.getAlpha());
                var9 = ColorUtils.getColor(this.this$0.instantColor.getColor(), this.this$0.outlineColor.getAlpha());
            }
            if (!(this.this$0.render.getValue().equalsIgnoreCase("Fill"))) {
                if (this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBox(arg0, var4, var8);
            }
            if (!(this.this$0.render.getValue().equalsIgnoreCase("Outline"))) {
                if (this.this$0.render.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBoxOutline(arg0, var4, var9);
            }
        }

        public void start() {
            if (!(this.this$0.farReach.getValue())) {
                legacyStart();
                return;
            }
            this.this$0.trackStarts((this.this$0.farReach.getValue() ? 2 : 1));
            Object var1 = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                this.startSlot = IMinecraft.mc.player.getInventory().getSelectedSlot();
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, var1);
                    Object var2 = grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, var2, WorldUtils.getClosestDirection(var2, true));
                } else {
                    this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, arg0, arg1));
                }
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.farReach.getValue()) {
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, var1);
                    var2 = grimDecoyPos();
                    this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, var2, WorldUtils.getClosestDirection(var2, true));
                } else {
                    NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, arg0, arg1));
                }
                IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            if (this.state.getBlock() == Blocks.BEDROCK) {
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.ticks = 0;
            this.mining = true;
            this.instantMine = false;
            this.started = true;
            this.brokenCount = 0;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer.reset();
        }

        private void legacyStart() {
            Object var1 = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                if (this.this$0.switchMode.getValue().equalsIgnoreCase("None")) {
                } else {
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltSwap")) break label_1;
                    if (this.this$0.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                        label_1:
                    } else {
                    }
                }
                int var3 = InventoryUtils.findFastestItem(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
                if (var3 == -1) break label_5;
                var tmp0 = (var3 != IMinecraft.mc.player.getInventory().getSelectedSlot() ? 1 : 0);
                int var2 = tmp0;
                int var4 = tmp0;
                if (!(!var2)) {
                    this.this$0.serverSend(new ServerboundSetCarriedItemPacket(var3));
                }
                if (this.this$0.doubleMine.getValue()) {
                    this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, arg0, arg1));
                    this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, arg0, arg1));
                    this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, arg0, arg1));
                } else {
                    this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, arg0, arg1));
                }
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            } else {
                if (this.this$0.doubleMine.getValue()) {
                    NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, arg0, arg1));
                    NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, arg0, arg1));
                    NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.position, arg0, arg1));
                } else {
                    NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.position, arg0, arg1));
                }
                IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            if (this.state.getBlock() == Blocks.BEDROCK) {
                IMinecraft.mc.player.swing(InteractionHand.MAIN_HAND);
            }
            restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.ticks = 0;
            this.mining = true;
            this.instantMine = false;
            this.started = true;
            this.brokenCount = 0;
            this.lastBrokenCount = -1;
            this.instantRemineResetTimer.reset();
        }

        public void cancel() {
            if (!(this.this$0.farReach.getValue())) {
                legacyCancel();
                return;
            }
            if ((this.this$0.doubleMine.getValue()) || (!this.started)) break label_3;
            if (this.this$0.farReach.getValue()) {
                this.this$0.sendRawPlayerAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true));
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                break label_3;
            }
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), arg0));
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                break label_3;
            }
            NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, WorldUtils.getClosestDirection(this.position, true), arg0));
            IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            label_3:
            if (this.this$0.isProxyActive() && this.startSlot != -1) {
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(this.startSlot));
                IMinecraft.mc.player.getInventory().setSelectedSlot(this.startSlot);
                this.startSlot = -1;
            }
            restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.mining = false;
            this.started = false;
            this.instantMine = false;
        }

        private void legacyCancel() {
            if (this.this$0.doubleMine.getValue()) break label_1;
            Object var1 = WorldUtils.getClosestDirection(this.position, true);
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSendSequenced(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, arg0, arg1));
                this.this$0.serverSend(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                break label_1;
            }
            NetworkUtils.sendSequencedPacket(() -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.position, arg0, arg1));
            IMinecraft.mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            label_1:
            if (this.this$0.isProxyActive()) {
                this.this$0.serverSend(new ServerboundSetCarriedItemPacket(IMinecraft.mc.player.getInventory().getSelectedSlot()));
            }
            restoreIronSwap();
            this.progress = 0.0f;
            this.prevProgress = 0.0f;
            this.ghostIronProgress = 0.0f;
            this.ironSwapped = false;
            this.swappedIronInventorySlot = -1;
            this.swappedIronHotbarSlot = -1;
            this.attempts = 0;
            this.mining = false;
            this.started = false;
            this.instantMine = false;
        }

        private float getSpeed() {
            return this.this$0.speed.getValue().floatValue();
        }

        private BlockPos grimDecoyPos() {
            return this.position.below(2000);
        }

        public int getTicksRemaining() {
            if (!this.mining) return 2147483647;
            if (this.this$0.sixB.getValue() && this.state.getBlock() == Blocks.BEDROCK) {
                float var2 = 0.025f;
                return Math.max(0, Math.round(((getSpeed() - this.progress) / var2)));
            }
            var2 = InventoryUtils.findFastestItem(this.state, InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
            if (var2 == -1) {
                var2 = IMinecraft.mc.player.getInventory().getSelectedSlot();
            }
            var tmp0 = (WorldUtils.getMineSpeed(this.state, var2) / LightningClient.WORLD_MANAGER.getTimerMultiplier());
            float var1 = tmp0;
            if (tmp0 <= 0.0f) return 2147483647;
            return Math.max(0, Math.round(((getSpeed() - this.progress) / var1)));
        }

        @Generated
        public float getPrevProgress() {
            return this.prevProgress;
        }

        @Generated
        public int getAttempts() {
            return this.attempts;
        }

        @Generated
        public long getStallTime() {
            return this.stallTime;
        }

        @Generated
        public int getStartSlot() {
            return this.startSlot;
        }

        @Generated
        public int getBrokenCount() {
            return this.brokenCount;
        }

        @Generated
        public int getLastBrokenCount() {
            return this.lastBrokenCount;
        }

        @Generated
        public Timer getInstantRemineResetTimer() {
            return this.instantRemineResetTimer;
        }

        @Generated
        public float getGhostIronProgress() {
            return this.ghostIronProgress;
        }

        @Generated
        public boolean isIronSwapped() {
            return this.ironSwapped;
        }

        @Generated
        public int getSwappedIronInventorySlot() {
            return this.swappedIronInventorySlot;
        }

        @Generated
        public int getSwappedIronHotbarSlot() {
            return this.swappedIronHotbarSlot;
        }

        @Generated
        public boolean isBedrockOrigin() {
            return this.bedrockOrigin;
        }

        @Generated
        public void setProgress(float arg0) {
            this.progress = arg0;
        }

    }
}
