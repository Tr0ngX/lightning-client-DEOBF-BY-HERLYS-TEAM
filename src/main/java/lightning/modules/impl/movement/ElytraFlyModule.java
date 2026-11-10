package lightning.modules.impl.movement;

import java.util.ArrayDeque;
import java.util.Deque;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.EventInput;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerTravelEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.mixins.accessors.EntityAccessor;
import lightning.mixins.accessors.EntityFlagAccessor;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.combat.AutoMaceModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.BaritoneUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "ElytraFly", description = "Allows you to fly using an elytra without fireworks.", category = Module.Category.MOVEMENT)
public class ElytraFlyModule extends Module {
    public ModeSetting mode;
    public NumberSetting horizontal;
    public NumberSetting vertical;
    public BooleanSetting moveVertically;
    public BooleanSetting bounceTakeoff;
    public BooleanSetting bounceAutoPitch;
    public NumberSetting bouncePitch;
    public BooleanSetting bounceMinimizePackets;
    public NumberSetting bounceFlagPause;
    public BooleanSetting bounceFakeLag;
    public BooleanSetting bouncePassObstacles;
    public NumberSetting bounceObstacleLookAhead;
    public BooleanSetting bounceStandUpToPass;
    public BooleanSetting bouncePutOnElytra;
    public BooleanSetting bounceBypass;
    public NumberSetting bounceBypassTicks;
    public ModeSetting crSteerMode;
    public NumberSetting crTurnSpeed;
    public NumberSetting crFireworkDelay;
    public ModeSetting crFlipFlopMode;
    public BooleanSetting crInventory;
    public BooleanSetting crGrimSwap;
    public NumberSetting crGrimDelay;
    public BooleanSetting muteElytra;
    private boolean freeLookInitialized;
    private float freeYaw;
    private float freePitch;
    private boolean grimStill;
    private int grimStillTicks;
    private float grimFlipBaseYaw;
    private boolean grimFlipToggle;
    private long grimFireworkUsedAtMs;
    private double grimLastFireworkDurationSeconds;
    private static final long CR_DOUBLE_JUMP_WINDOW_MS = 300L;
    private boolean crLastJumpDown;
    private long crLastJumpPressTime;
    private boolean crLaunchPending;
    private boolean crGliding;
    private int crGrimTicks;
    private int crTicksSinceGlide;
    private static final int GLIDE_MIN_GAP_TICKS = 2;
    private int lastGlideStartTick;
    private final Timer crLastSwapTimer;
    private float pitch;
    private boolean bounceDoJump;
    private boolean bounceJumpThisTick;
    private boolean bouncePendingResend;
    private boolean bounceTakeoffPending;
    private int bounceFlagPauseTicksLeft;
    private boolean bouncePrevGliding;
    private double bounceStartY;
    private double bounceLastGroundY;
    private final Deque bounceSendPacketQueue;
    private final Deque bouncePingPacketQueue;
    private boolean bounceWasFlyingForLog;
    private boolean bouncePrevJumpKeyDown;
    public static volatile boolean bouncePitchOverrideActive;
    public static volatile float bounceSavedCameraPitch;
    private boolean isFlushingBouncePackets;
    private volatile int bounceBypassTicksLeft;
    private Vec3 bounceSpeedPrevPos;
    private Vec3 bounceSpeedCurrPos;
    private double bouncePrevSpeedSample;
    private Vec3 bounceObstacleStartPos;
    private Vec3 bounceObstaclePassingToPos;
    private static final double OBSTACLE_MIN_HEIGHT = 0.063;
    private static final boolean OBSTACLE_HEAD_HITTERS = true;
    private static final double OBSTACLE_ACCEPTABLE_OFFSET = 2.0;
    private static final double OBSTACLE_DIRECTION_STEP = 45.0;
    private int grimParkedSlot;
    private ItemStack grimParkedDisplaced;
    private ItemStack grimHiddenElytra;

    public ElytraFlyModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Control";
        tmp0[1] = "ControlRocket";
        tmp0[2] = "Bounce";
        this.mode = new ModeSetting("Mode", "The mode that will be used for elytra flying.", "ControlRocket", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Control";
        this.horizontal = new NumberSetting("Horizontal", "The speed at which you will be flying horizontally.", new ModeSetting.Visibility(this.mode, tmp1), 2.0f, 0.1f, 10.0f);
        var tmp2 = new String[1];
        tmp2[0] = "Control";
        this.vertical = new NumberSetting("Vertical", "The speed at which you will be flying vertically.", new ModeSetting.Visibility(this.mode, tmp2), 1.0f, 0.1f, 10.0f);
        var tmp3 = new String[1];
        tmp3[0] = "Control";
        this.moveVertically = new BooleanSetting("MoveVertically", "Whether or not to allow for vertical movement.", new ModeSetting.Visibility(this.mode, tmp3), true);
        var tmp4 = new String[1];
        tmp4[0] = "Bounce";
        this.bounceTakeoff = new BooleanSetting("Takeoff", "Automatically jumps and initiates gliding when grounded with an elytra equipped.", new ModeSetting.Visibility(this.mode, tmp4), true);
        var tmp5 = new String[1];
        tmp5[0] = "Bounce";
        this.bounceAutoPitch = new BooleanSetting("AutoPitch", "Automatically pitches the player's rotation down while gliding to bounce at faster speeds.", new ModeSetting.Visibility(this.mode, tmp5), true);
        var tmp6 = new String[1];
        tmp6[0] = "Bounce";
        this.bouncePitch = new NumberSetting("Pitch", "The pitch angle to look down when auto pitch is active.", new ModeSetting.Visibility(this.mode, tmp6), 72.0f, 0.0f, 90.0f);
        var tmp7 = new String[1];
        tmp7[0] = "Bounce";
        this.bounceMinimizePackets = new BooleanSetting("MinimizePackets", "Send the bare minimum to start flying, nothing extra.", new ModeSetting.Visibility(this.mode, tmp7), true);
        var tmp8 = new String[1];
        tmp8[0] = "Bounce";
        this.bounceFlagPause = new NumberSetting("FlagPause", "How long (in ticks) to pause if the server flags you for a movement check.", new ModeSetting.Visibility(this.mode, tmp8), 5.0f, 0.0f, 100.0f);
        var tmp9 = new String[1];
        tmp9[0] = "Bounce";
        this.bounceFakeLag = new BooleanSetting("FakeLag", "Emulates the player lagging to allow flying in 1x2 tunnels.", new ModeSetting.Visibility(this.mode, tmp9), true);
        var tmp10 = new String[1];
        tmp10[0] = "Bounce";
        this.bouncePassObstacles = new BooleanSetting("PassObstacles", "Automatically paths around obstacles in the flight line using Baritone.", new ModeSetting.Visibility(this.mode, tmp10), false);
        var tmp11 = new String[1];
        tmp11[0] = "Bounce";
        this.bounceObstacleLookAhead = new NumberSetting("ObstacleLookAhead", "Blocks to look ahead for obstacles / step by when pathing around one.", new ModeSetting.Visibility(this.mode, tmp11), 8.0f, 0.0f, 50.0f);
        var tmp12 = new String[1];
        tmp12[0] = "Bounce";
        this.bounceStandUpToPass = new BooleanSetting("StandUpToPass", "Stops gliding (stands up) while Baritone navigates around an obstacle, then automatically takes off again once clear.", new ModeSetting.Visibility(this.mode, tmp12), true);
        var tmp13 = new String[1];
        tmp13[0] = "Bounce";
        this.bouncePutOnElytra = new BooleanSetting("PutOnElytra", "Safety net: if the elytra ends up off the chest slot, immediately equip it back.", new ModeSetting.Visibility(this.mode, tmp13), true);
        var tmp14 = new String[1];
        tmp14[0] = "Bounce";
        this.bounceBypass = new BooleanSetting("Bypass", "Bypass for 2b and other servers (may be)", new ModeSetting.Visibility(this.mode, tmp14), false);
        this.bounceBypassTicks = new NumberSetting("BypassTicks", "How many ticks before/after the recast to blink.", new BooleanSetting.Visibility(this.bounceBypass, true), 3.0f, 0.0f, 20.0f);
        var tmp15 = new String[1];
        tmp15[0] = "ControlRocket";
        var tmp16 = new String[2];
        tmp16[0] = "Key";
        tmp16[1] = "Mouse";
        this.crSteerMode = new ModeSetting("Control", "Steering mode: Key (WASD flight + FreeLook camera) or Mouse (fly where camera looks).", new ModeSetting.Visibility(this.mode, tmp15), "Key", tmp16);
        var tmp17 = new String[1];
        tmp17[0] = "ControlRocket";
        this.crTurnSpeed = new NumberSetting("TurnSpeed", "Maximum rotation change per tick.", new ModeSetting.Visibility(this.mode, tmp17), 25.0f, 1.0f, 180.0f);
        var tmp18 = new String[1];
        tmp18[0] = "ControlRocket";
        this.crFireworkDelay = new NumberSetting("FireworksDelay", "Delay between every fire", new ModeSetting.Visibility(this.mode, tmp18), 0, 0, 3);
        var tmp19 = new String[1];
        tmp19[0] = "ControlRocket";
        var tmp20 = new String[3];
        tmp20[0] = "Full";
        tmp20[1] = "WithFirework";
        tmp20[2] = "None";
        this.crFlipFlopMode = new ModeSetting("FlipFlopMode", "How to hold position when not pressing movement keys.", new ModeSetting.Visibility(this.mode, tmp19), "Full", tmp20);
        var tmp21 = new String[1];
        tmp21[0] = "ControlRocket";
        this.crInventory = new BooleanSetting("Inventory", "Allows using fireworks from anywhere in the inventory, not just the hotbar.", new ModeSetting.Visibility(this.mode, tmp21), true);
        var tmp22 = new String[1];
        tmp22[0] = "ControlRocket";
        this.crGrimSwap = new BooleanSetting("GrimV3", "Cycles the elytra out of the chest slot so it never loses durability.", new ModeSetting.Visibility(this.mode, tmp22), true);
        var tmp23 = new String[1];
        tmp23[0] = "ControlRocket";
        this.crGrimDelay = new NumberSetting("GrimDelay", "Gliding ticks between each GrimV3 swap-out.", new ModeSetting.Visibility(this.mode, tmp23), 10.0f, 1.0f, 18.0f);
        var tmp24 = new String[1];
        tmp24[0] = "ControlRocket";
        this.muteElytra = new BooleanSetting("MuteElytra", "Mutes the armor-equip sound when ControlRocket swaps between elytra and chestplate.", new ModeSetting.Visibility(this.mode, tmp24), true);
        this.grimLastFireworkDurationSeconds = -1.0;
        this.crTicksSinceGlide = 99;
        this.lastGlideStartTick = -1000;
        this.crLastSwapTimer = new Timer();
        this.bounceDoJump = false;
        this.bounceJumpThisTick = false;
        this.bouncePendingResend = false;
        this.bounceTakeoffPending = false;
        this.bounceFlagPauseTicksLeft = 0;
        this.bouncePrevGliding = false;
        this.bounceStartY = 0.0;
        this.bounceLastGroundY = 0.0;
        this.bounceSendPacketQueue = new ArrayDeque();
        this.bouncePingPacketQueue = new ArrayDeque();
        this.bounceWasFlyingForLog = false;
        this.bouncePrevJumpKeyDown = false;
        this.isFlushingBouncePackets = false;
        this.bounceBypassTicksLeft = 0;
        this.bounceSpeedPrevPos = Vec3.ZERO;
        this.bounceSpeedCurrPos = Vec3.ZERO;
        this.bouncePrevSpeedSample = -1.0;
        this.bounceObstacleStartPos = Vec3.ZERO;
        this.bounceObstaclePassingToPos = null;
        this.grimParkedSlot = -1;
        this.grimParkedDisplaced = ItemStack.EMPTY;
        this.grimHiddenElytra = null;
    }

    public float getFreeYaw() {
        return this.freeYaw;
    }

    public float getFreePitch() {
        return this.freePitch;
    }

    public boolean isFreeLookActive() {
        if ((mc.player == null) || (!this.freeLookInitialized)) {
            return false;
        }
        if ((!(isToggled())) || (!(this.mode.getValue().equalsIgnoreCase("ControlRocket")))) {
            return false;
        }
        if (!(this.crSteerMode.getValue().equalsIgnoreCase("Key"))) break label_5;
        if (!(mc.player.isFallFlying())) {
            if (!(shouldPinFallFlying())) break label_5;
        }
        break label_6;
        label_5:
        label_6:
        return false;
    }

    public boolean isFreeLookInitialized() {
        return this.freeLookInitialized;
    }

    public void resetFreeLook() {
        this.freeLookInitialized = false;
    }

    public void onMouseTurn(double arg0, double arg1) {
        if (!this.freeLookInitialized && mc.player != null) {
            this.freeYaw = mc.player.getYRot();
            this.freePitch = mc.player.getXRot();
            this.freeLookInitialized = true;
        }
        this.freeYaw = (this.freeYaw + ((float) (arg0 * 0.15)));
        this.freePitch = Mth.clamp((this.freePitch + ((float) (arg1 * 0.15))), -90.0f, 90.0f);
    }

    public boolean isBounceActive() {
        if (!(isToggled())) break label_0;
        return this.mode.getValue().equalsIgnoreCase("Bounce");
    }

    public boolean isBounceAutoPitch() {
        if (((!(isBounceActive())) || (!(this.bounceAutoPitch.getValue()))) || (!(isGliding()))) break label_0;
        return !(isFlightPaused());
    }

    public float getBouncePitch() {
        return this.bouncePitch.getValue().floatValue();
    }

    public boolean isBounceJumpActive() {
        if ((!(isToggled())) || (!(this.mode.getValue().equalsIgnoreCase("Bounce")))) break label_1;
        if (!this.bounceDoJump) {
            if (!this.bounceJumpThisTick) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        return false;
    }

    public void resetBounceJump() {
        this.bounceDoJump = false;
        this.bounceJumpThisTick = false;
    }

    public boolean isBouncePitchOverrideActive() {
        return false;
    }

    public float getBounceSavedCameraPitch() {
        return 0.0f;
    }

    public void onEnable() {
        if (mc.player == null) return;
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            resetBounceState();
            this.bounceStartY = mc.player.getY();
            this.bounceLastGroundY = mc.player.getY();
            bounceSavedCameraPitch = mc.player.getXRot();
            this.bounceSpeedPrevPos = mc.player.position();
            this.bounceSpeedCurrPos = mc.player.position();
            this.bounceObstacleStartPos = mc.player.position();
            this.bounceObstaclePassingToPos = null;
            return;
        }
        resetGrimControl();
        if (!(this.mode.getValue().equalsIgnoreCase("ControlRocket"))) return;
        if (this.crGrimSwap.getValue()) {
            if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;
            int var1 = findChestplateSlot();
            if (var1 != -1) {
                InventoryUtils.swapEquipment(var1, 6);
            }
            return;
        }
        if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) return;
        var1 = InventoryUtils.find(Items.ELYTRA);
        if (var1 == -1) {
            LightningClient.CHAT_MANAGER.tagged("No elytra found in your inventory.", getName());
            setToggled(false);
            return;
        }
        InventoryUtils.swapEquipment(var1, 6);
    }

    public void onDisable() {
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            resetBounceState();
            if (this.bounceObstaclePassingToPos != null) {
                cancelObstaclePath();
            }
            this.bounceObstaclePassingToPos = null;
            flushBouncePackets();
            bouncePitchOverrideActive = false;
            if (mc.player != null && mc.player.onGround()) {
                if (mc.player.getPose() != Pose.FALL_FLYING) {
                    if (mc.player.getPose() == Pose.SWIMMING) {
                    }
                    mc.player.setPose(Pose.STANDING);
                }
                ((EntityFlagAccessor) mc.player).invokeSetSharedFlag(EntityFlagAccessor.getFlagFallFlying(), false);
            }
            return;
        }
        if (mc.player != null && !(!this.freeLookInitialized)) {
            mc.player.setYRot(this.freeYaw);
            mc.player.setXRot(this.freePitch);
        }
        resetGrimControl();
        this.crGrimTicks = 0;
        this.crLaunchPending = false;
        this.crGliding = false;
        this.grimParkedSlot = -1;
        this.grimHiddenElytra = null;
        this.grimParkedDisplaced = ItemStack.EMPTY;
        if (mc.player != null && mc.player.onGround()) {
            if (mc.player.getPose() != Pose.FALL_FLYING) {
                if (mc.player.getPose() == Pose.SWIMMING) {
                }
                mc.player.setPose(Pose.STANDING);
            }
            ((EntityAccessor) mc.player).invokeSetSharedFlag(7, false);
        }
        if (mc.player == null) return;
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlyingSpeed(0.05f);
    }

    @SubscribeEvent
    public void onInput(EventInput arg0) {
        if ((!(isToggled())) || (!(this.mode.getValue().equalsIgnoreCase("Bounce")))) {
            return;
        }
        if (!(isGliding()) && !this.bounceJumpThisTick) return;
        this.bounceJumpThisTick = false;
        if (isFlightPaused()) return;
        arg0.jumping = true;
    }

    private void resetGrimControl() {
        this.grimStill = false;
        this.grimStillTicks = 0;
        this.grimFireworkUsedAtMs = 0L;
        this.grimLastFireworkDurationSeconds = -1.0;
        this.freeLookInitialized = false;
    }

    @SubscribeEvent
    public void onPlayerTravel(PlayerTravelEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (!(mc.player.isFallFlying()))) {
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Control"))) break label_5;
        arg0.setCancelled(true);
        if (mc.player.input.getMoveVector().y == 0.0f && mc.player.input.getMoveVector().x == 0.0f) {
            mc.player.setDeltaMovement(new Vec3(0.0, mc.player.getDeltaMovement().y, 0.0));
        } else {
            this.pitch = 12.0f;
            double var2 = Math.cos(Math.toRadians(((double) (mc.player.getYRot() + 90.0f))));
            double var4 = Math.sin(Math.toRadians(((double) (mc.player.getYRot() + 90.0f))));
            mc.player.setDeltaMovement(new Vec3((((((double) mc.player.input.getMoveVector().y) * this.horizontal.getValue().doubleValue()) * var2) + ((((double) mc.player.input.getMoveVector().x) * this.horizontal.getValue().doubleValue()) * var4)), mc.player.getDeltaMovement().y, (((((double) mc.player.input.getMoveVector().y) * this.horizontal.getValue().doubleValue()) * var4) - ((((double) mc.player.input.getMoveVector().x) * this.horizontal.getValue().doubleValue()) * var2))));
        }
        mc.player.setDeltaMovement(new Vec3(mc.player.getDeltaMovement().x, 0.0, mc.player.getDeltaMovement().z));
        var2 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam")).isToggled();
        if ((!(this.moveVertically.getValue())) || (var2)) break label_5;
        if (mc.options.keyJump.isDown()) {
            mc.player.setDeltaMovement(new Vec3(mc.player.getDeltaMovement().x, this.vertical.getValue().doubleValue(), mc.player.getDeltaMovement().z));
            this.pitch = -51.0f;
            break label_5;
        }
        if (!(mc.options.keyShift.isDown())) break label_5;
        mc.player.setDeltaMovement(new Vec3(mc.player.getDeltaMovement().x, -(this.vertical.getValue().doubleValue()), mc.player.getDeltaMovement().z));
        this.pitch = 0.0f;
        label_5:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Bounce"))) break label_9;
        Object var5 = arg0.getPacket();
        if ((var5 instanceof ClientboundPingPacket)) {
            Object var7 = ((ClientboundPingPacket) var5);
            if (this.bounceFakeLag.getValue() && isGlidingMasked()) {
                if (!(isInDipWindow())) {
                }
                if (this.bounceBypass.getValue() && this.bounceBypassTicksLeft > 0) {
                }
            } else {
            }
            int var6 = 0;
            int var8 = 0;
            if (!(!var6)) {
                this.bouncePingPacketQueue.add(var7);
                arg0.setCancelled(true);
            }
            return;
        }
        if (!((arg0.getPacket() instanceof ClientboundPlayerPositionPacket))) break label_9;
        this.bounceFlagPauseTicksLeft = this.bounceFlagPause.getValue().intValue();
        this.bounceDoJump = false;
        this.bouncePendingResend = false;
        this.bounceTakeoffPending = false;
        if (isBounceDebugOn()) {
            debugLog(("FLAG PAUSE: server corrected position, holding for " + this.bounceFlagPauseTicksLeft + " ticks"));
        }
        if ((!(this.bouncePassObstacles.getValue())) || (!(BaritoneUtils.isAvailable()))) break label_9;
        var6 = getSnappedObstacleDir();
        var7 = findClosestPointOnObstacleLine(mc.player.position(), var6);
        mc.execute(() -> {
    if (mc.player == null) return;
    Object var3 = arg0.add(arg1.scale(this.bounceObstacleLookAhead.getValue().doubleValue()));
    pathToValidObstaclePoint(var3, arg1, false);
});
        label_9:
        var5 = isAutoMaceFakeFlyActive();
        if (this.mode.getValue().equalsIgnoreCase("ControlRocket")) {
            if (!(this.muteElytra.getValue())) {
            }
            if (!var5) break label_12;
        }
        if (this.crLastSwapTimer.hasTimeElapsed(500)) break label_12;
        var tmp0 = arg0.getPacket();
        Object var4 = tmp0;
        if (!((tmp0 instanceof ClientboundSoundPacket))) break label_12;
        var tmp1 = ((ClientboundSoundPacket) var4);
        Object var3 = tmp1;
        if (!(((SoundEvent) tmp1.getSound().value()).location().getPath().startsWith("item.armor.equip"))) break label_12;
        arg0.setCancelled(true);
        label_12:
        if (!(this.mode.getValue().equalsIgnoreCase("Control"))) break label_15;
        var tmp2 = arg0.getPacket();
        var4 = tmp2;
        if (!((tmp2 instanceof ServerboundMovePlayerPacket))) break label_15;
        var tmp3 = ((ServerboundMovePlayerPacket) var4);
        Object var2 = tmp3;
        if ((!(tmp3.hasRotation())) || (!(mc.player.isFallFlying()))) break label_15;
        if (mc.options.keyLeft.isDown()) {
            ((PlayerMoveC2SPacketAccessor) var2).setYaw((var2.getYRot(0.0f) - 90.0f));
        }
        if (mc.options.keyRight.isDown()) {
            ((PlayerMoveC2SPacketAccessor) var2).setYaw((var2.getYRot(0.0f) + 90.0f));
        }
        ((PlayerMoveC2SPacketAccessor) var2).setPitch(this.pitch);
        label_15:
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (mc.player == null) return;
        if (!(this.mode.getValue().equalsIgnoreCase("Bounce"))) return;
        if (!(!this.isFlushingBouncePackets)) return;
        if (arg0.getPacket() == null) return;
        if (!(arg0.getPacket().getClass().getSimpleName().startsWith("Serverbound"))) return;
        if ((!(this.bounceFakeLag.getValue())) || (!(isGlidingMasked()))) break label_5;
        int var3 = (isInDipWindow() ? 1 : 0);
        if ((!(this.bounceBypass.getValue())) || (this.bounceBypassTicksLeft <= 0)) break label_8;
        if (!((arg0.getPacket() instanceof ServerboundMovePlayerPacket))) {
            if (!((arg0.getPacket() instanceof ServerboundPongPacket))) break label_8;
        }
        break label_9;
        label_8:
        label_9:
        int var4 = 0;
        if (!var3) {
            if (!(!var4)) {
            }
        } else {
        }
        int var2 = 0;
        int var5 = 0;
        if (!(!var2)) {
            this.bounceSendPacketQueue.add(arg0.getPacket());
            arg0.setCancelled(true);
            return;
        }
        flushBouncePackets();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Bounce")) {
            if (this.bouncePutOnElytra.getValue()) {
                onPutOnElytraTick();
            }
            tickBounceMode();
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("ControlRocket"))) return;
        handleControlRocket();
    }

    private void handleControlRocket() {
        if ((this.crGrimSwap.getValue()) || (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)) break label_1;
        int var1 = InventoryUtils.find(Items.ELYTRA);
        if (var1 == -1) break label_0;
        InventoryUtils.swapEquipment(var1, 6);
        if (mc.player.onGround()) break label_0;
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        label_0:
        return;
        label_1:
        var1 = mc.options.keyJump.isDown();
        if (!(!var1) && !this.crLastJumpDown) {
            if (!(this.crSteerMode.getValue().equalsIgnoreCase("Key"))) {
                if ((System.currentTimeMillis() - this.crLastJumpPressTime) <= 300L) {
                }
            } else {
            }
            int var2 = 0;
            int var3 = 0;
            if (!(!var2) && !(mc.player.isFallFlying())) {
                this.crLaunchPending = true;
                Object var4 = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(var4.x, Math.max(var4.y, 0.45), var4.z);
            }
            this.crLastJumpPressTime = System.currentTimeMillis();
        }
        this.crLastJumpDown = var1;
        if (mc.player.onGround() && !this.crLaunchPending) {
            var2 = this.crGliding;
            this.crGliding = false;
            this.crGrimTicks = 0;
            if (!(!var2) && !(!this.freeLookInitialized)) {
                mc.player.setYRot(this.freeYaw);
                mc.player.setXRot(this.freePitch);
            }
            resetGrimControl();
            if (mc.player.getPose() != Pose.FALL_FLYING) {
                if (mc.player.getPose() == Pose.SWIMMING) {
                }
                mc.player.setPose(Pose.STANDING);
            }
            ((EntityAccessor) mc.player).invokeSetSharedFlag(7, false);
        }
        if (mc.player.isFallFlying()) {
            this.crLaunchPending = false;
            break label_13;
        }
        if (mc.player.onGround()) break label_13;
        if (!this.crLaunchPending) {
            if (mc.player.getDeltaMovement().y >= 0.0) break label_13;
        }
        sendGlideStart();
        this.crLaunchPending = false;
        label_13:
        grimSwapCycle();
        if (!(mc.player.isFallFlying())) {
            resetGrimControl();
            return;
        }
        tickGrimControl();
    }

    private void tickGrimControl() {
        if (this.crSteerMode.getValue().equalsIgnoreCase("Key")) {
            if (!this.freeLookInitialized) {
                this.freeYaw = mc.player.getYRot();
                this.freePitch = mc.player.getXRot();
                this.freeLookInitialized = true;
            }
            Object var1 = grimMovementInput();
            int var2 = this.grimStill;
            this.grimStill = var1.lengthSqr() < 1.0E-4;
            int var3 = (var1.lengthSqr() < 1.0E-4 ? 1 : 0);
            if (!(!this.grimStill) && !var2) {
                this.grimFlipBaseYaw = mc.player.getYRot();
                this.grimFlipToggle = false;
            }
            int var4 = 1;
            if (!(!this.grimStill)) {
                int var5 = hasFireworkInInventory();
                Object var6 = this.crFlipFlopMode.getValue();
                int var7 = -1;
                switch (var6.hashCode()) {
                    case 2201263 -> { break label_4; }
                    case 2433880 -> { break label_6; }
                    case 2123429197 -> { break label_5; }
                    default -> { break label_7; }
                }
                label_4:
                if (!(var6.equals("Full"))) break label_7;
                var7 = 0;
                break label_7;
                label_5:
                if (!(var6.equals("WithFirework"))) break label_7;
                var7 = 1;
                break label_7;
                label_6:
                if (!(var6.equals("None"))) break label_7;
                var7 = 2;
                label_7:
                switch (var7) {
                    case 0 -> { break label_8; }
                    case 1 -> { break label_9; }
                    case 2 -> { break label_10; }
                    default -> { break label_11; }
                }
                label_8:
                break label_12;
                label_9:
                break label_12;
                label_10:
                break label_12;
                label_11:
                label_12:
                var4 = 1;
            }
            if (!(!this.grimStill)) {
            } else {
            }
            (this.grimStillTicks + 1).grimStillTicks = 0;
            var5 = 0;
            if (!(!this.grimStill)) {
                if (!(!var4)) {
                }
                tryUseGrimFirework();
            }
            if (!(!this.grimStill)) {
                if (!var4) break label_24;
                if (!(!this.grimFlipToggle)) {
                } else {
                }
                var6 = this.grimFlipBaseYaw;
                applyGrimRotation(var6, 0.0f);
                this.grimFlipToggle = !this.grimFlipToggle;
                break label_24;
            }
            if (!(!var2) && !(hasFireworkInInventory())) {
                mc.player.setDeltaMovement(Vec3.ZERO);
            }
            var6 = mc.player.getEyePosition();
            var7 = RotationUtils.getRotations((var6.x + var1.x), (var6.y + var1.y), (var6.z + var1.z));
            applyGrimRotation(var7[0], var7[1]);
            label_24:
        } else {
            this.freeLookInitialized = false;
            var1 = mc.player.getYRot();
            var2 = mc.player.getXRot();
            applyGrimRotation(var1, var2);
            tryUseGrimFirework();
        }
    }

    private Vec3 grimMovementInput() {
        if (!(!this.freeLookInitialized)) {
        } else {
        }
        float var1 = mc.player.getYRot();
        Object var2 = Vec3.ZERO;
        if (mc.options.keyUp.isDown()) {
            var2 = var2.add(fromYaw(var1));
        }
        if (mc.options.keyDown.isDown()) {
            var2 = var2.add(fromYaw((var1 + 180.0f)));
        }
        if (mc.options.keyLeft.isDown()) {
            var2 = var2.add(fromYaw((var1 - 90.0f)));
        }
        if (mc.options.keyRight.isDown()) {
            var2 = var2.add(fromYaw((var1 + 90.0f)));
        }
        if (mc.options.keyJump.isDown()) {
            var2 = var2.add(0.0, 1.0, 0.0);
        }
        if (mc.options.keyShift.isDown()) {
            var2 = var2.add(0.0, -1.0, 0.0);
        }
        return var2;
    }

    private static Vec3 fromYaw(float arg0) {
        double var1 = Math.toRadians(((double) arg0));
        return new Vec3(-(Math.sin(var1)), 0.0, Math.cos(var1));
    }

    private void applyGrimRotation(float arg0, float arg1) {
        float var3 = this.crTurnSpeed.getValue().floatValue();
        float var4 = mc.player.getYRot();
        float var5 = mc.player.getXRot();
        float var6 = Mth.clamp(Mth.wrapDegrees((arg0 - var4)), -(var3), var3);
        float var7 = Mth.clamp((arg1 - var5), -(var3), var3);
        mc.player.setYRot((var4 + var6));
        mc.player.setXRot((var5 + var7));
        mc.player.setYBodyRot((var4 + var6));
        mc.player.setYHeadRot((var4 + var6));
    }

    private boolean hasFireworkInInventory() {
        if (mc.player == null) return false;
        if (mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET)) break label_1;
        if (mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
            label_1:
            return true;
        }
        int var1 = 0;
        while (var1 <= 8) {
            if (!(mc.player.getInventory().getItem(var1).is(Items.FIREWORK_ROCKET))) {
            } else {
                return true;
            }
            var1++;
        }
        if (!(this.crInventory.getValue())) break label_10;
        var1 = 9;
        while (var1 <= 35) {
            if (!(mc.player.getInventory().getItem(var1).is(Items.FIREWORK_ROCKET))) {
            } else {
                return true;
            }
            var1++;
        }
        label_10:
        return false;
    }

    private int findFireworkSlot() {
        if (mc.player == null) return -1;
        if (mc.player.getMainHandItem().is(Items.FIREWORK_ROCKET)) return mc.player.getInventory().getSelectedSlot();
        int var1 = 0;
        while (var1 <= 8) {
            if (!(mc.player.getInventory().getItem(var1).is(Items.FIREWORK_ROCKET))) {
            } else {
                return var1;
            }
            var1++;
        }
        if (!(this.crInventory.getValue())) break label_9;
        var1 = 9;
        while (var1 <= 35) {
            if (!(mc.player.getInventory().getItem(var1).is(Items.FIREWORK_ROCKET))) {
            } else {
                return var1;
            }
            var1++;
        }
        label_9:
        return -1;
    }

    public void resetFireworkCooldown() {
        this.grimFireworkUsedAtMs = 0L;
        this.grimLastFireworkDurationSeconds = -1.0;
    }

    public void tryUseGrimFirework() {
        if (mc.player == null) return;
        tryUseGrimFirework(mc.player.getYRot(), mc.player.getXRot());
    }

    public void tryUseGrimFirework(float arg0, float arg1) {
        if ((mc.player == null) || (mc.getConnection() == null)) {
            return;
        }
        if (mc.player.isUsingItem() && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND) return;
        double var5 = (((double) (System.currentTimeMillis() - this.grimFireworkUsedAtMs)) / 1000.0);
        if (this.grimLastFireworkDurationSeconds < 0.0) {
        } else {
        }
        double var3 = (this.grimLastFireworkDurationSeconds + ((double) this.crFireworkDelay.getValue().floatValue()));
        double var7 = (this.grimLastFireworkDurationSeconds + ((double) this.crFireworkDelay.getValue().floatValue()));
        if (this.grimFireworkUsedAtMs != 0L && var5 < var3 && fireworkBoostActive()) return;
        if (mc.player.getOffhandItem().is(Items.FIREWORK_ROCKET)) {
            Object var9 = mc.player.getOffhandItem();
            Object var10 = ((Fireworks) var9.get(DataComponents.FIREWORKS));
            if (var10 != null) {
            } else {
            }
            int var11 = 1;
            this.grimLastFireworkDurationSeconds = ((((double) var11) * 0.5) + 0.5);
            if (!(armServerGlide())) return;
            NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, arg2, arg0, arg1));
            this.grimFireworkUsedAtMs = System.currentTimeMillis();
            return;
        }
        var9 = findFireworkSlot();
        if (var9 == -1) return;
        var10 = mc.player.getInventory().getItem(var9);
        var11 = ((Fireworks) var10.get(DataComponents.FIREWORKS));
        if (var11 != null) {
        } else {
        }
        int var12 = 1;
        this.grimLastFireworkDurationSeconds = ((((double) var12) * 0.5) + 0.5);
        if (var9 > 8 && !(this.crInventory.getValue())) return;
        if (!(armServerGlide())) return;
        int var13 = mc.player.getInventory().getSelectedSlot();
        if (var9 >= 0 && var9 <= 8) {
            if (!(InventoryUtils.switchSlot("Silent", var9, var13))) break label_16;
            NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg2, arg0, arg1));
            InventoryUtils.switchBack("Silent", var9, var13);
            this.grimFireworkUsedAtMs = System.currentTimeMillis();
            break label_16;
        }
        if (!(this.crInventory.getValue())) break label_16;
        int var14 = mc.player.getInventory().getSelectedSlot();
        InventoryUtils.swap("Swap", var9, var14);
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, arg2, arg0, arg1));
        InventoryUtils.swap("Swap", var9, var14);
        this.grimFireworkUsedAtMs = System.currentTimeMillis();
        label_16:
    }

    private boolean fireworkBoostActive() {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        return !(mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(2.0), (arg0) -> (arg0 instanceof FireworkRocketEntity)).isEmpty());
    }

    private boolean armServerGlide() {
        if (this.crGrimSwap.getValue() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return sendGlideStart();
        return true;
    }

    private boolean isAutoMaceFakeFlyActive() {
        Object var1 = ((AutoMaceModule) LightningClient.MODULE_MANAGER.getModule(AutoMaceModule.class));
        if ((var1 == null) || (!(var1.isToggled()))) break label_0;
        return var1.fakeFly.getValue();
    }

    private void grimSwapCycle() {
        if ((!(this.crGrimSwap.getValue())) || (mc.player.onGround())) {
            return;
        }
        this.crGrimTicks = (this.crGrimTicks + 1);
        if (this.crGrimTicks < this.crGrimDelay.getValue().intValue()) return;
        this.crGrimTicks = 0;
        sendGlideStart();
    }

    public boolean sendGlideStart() {
        return sendGlideStart(false);
    }

    public boolean sendGlideStart(boolean arg0) {
        this.crGliding = true;
        if (mc.player == null) return false;
        if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            return true;
        }
        int var2 = mc.player.tickCount;
        if (var2 >= this.lastGlideStartTick && (var2 - this.lastGlideStartTick) < 2) return false;
        int var3 = InventoryUtils.find(Items.ELYTRA);
        if (var3 == -1) return false;
        this.lastGlideStartTick = var2;
        Object var4 = mc.player.getInventory().getItem(var3).copy();
        Object var5 = mc.player.getItemBySlot(EquipmentSlot.CHEST).copy();
        this.grimParkedSlot = var3;
        this.grimHiddenElytra = var4;
        this.grimParkedDisplaced = var5;
        this.crLastSwapTimer.reset();
        InventoryUtils.swapEquipment(var3, 6);
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        InventoryUtils.swapEquipment(var3, 6);
        this.grimParkedSlot = -1;
        this.grimHiddenElytra = null;
        this.grimParkedDisplaced = ItemStack.EMPTY;
        return true;
    }

    public boolean shouldPinFallFlying() {
        if (((((!(isToggled())) || (!(this.mode.getValue().equalsIgnoreCase("ControlRocket")))) || (!(this.crGrimSwap.getValue()))) || (!this.crGliding)) || (mc.player == null)) break label_0;
        return !(mc.player.onGround());
    }

    private int findChestplateSlot() {
        int var1 = InventoryUtils.HOTBAR_START;
        while (var1 <= InventoryUtils.INVENTORY_END) {
            Object var3 = mc.player.getInventory().getItem(var1);
            if (var3.getItem() == Items.ELYTRA) break label_2;
            var tmp0 = ((Equippable) var3.get(DataComponents.EQUIPPABLE));
            Object var2 = tmp0;
            if (tmp0 == null) break label_2;
            if (var2.slot() != EquipmentSlot.CHEST) {
                break label_2;
            }
            return var1;
            label_2:
            var1++;
        }
        return -1;
    }

    public int getGrimParkedSlot() {
        return this.grimParkedSlot;
    }

    public ItemStack getGrimParkedDisplaced() {
        return this.grimParkedDisplaced;
    }

    public ItemStack getGrimHiddenElytra() {
        return this.grimHiddenElytra;
    }

    @SubscribeEvent
    public void onUpdateMovementPost(UpdateMovementEvent.Post arg0) {
        if ((mc.player == null) || (mc.getConnection() == null)) {
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Bounce"))) return;
        this.bounceSpeedPrevPos = this.bounceSpeedCurrPos;
        this.bounceSpeedCurrPos = mc.player.position();
        if (isBounceDebugOn() && this.bouncePrevSpeedSample >= 0.0) {
            double var2 = bounceHorizontalSpeedometer();
            double var4 = (this.bouncePrevSpeedSample - var2);
            int var6 = mc.player.horizontalCollision;
            if (!var6 && this.bouncePrevSpeedSample > 5.0 && var4 > (this.bouncePrevSpeedSample * 0.25)) {
                var tmp0 = new Object[11];
                tmp0[0] = (this.bouncePrevSpeedSample * 3.6);
                tmp0[1] = (var2 * 3.6);
                tmp0[2] = ((var4 / this.bouncePrevSpeedSample) * 100.0);
                tmp0[3] = mc.player.onGround();
                tmp0[4] = var6;
                tmp0[5] = isGliding();
                tmp0[6] = mc.player.isFallFlying();
                tmp0[7] = this.bounceFlagPauseTicksLeft;
                tmp0[8] = mc.player.getX();
                tmp0[9] = mc.player.getY();
                tmp0[10] = mc.player.getZ();
                debugLog(String.format("SPEED DROP: %.1f -> %.1f km/h (-%.0f%%) onGround=%b horizontalCollision=%b gliding=%b fallFlying=%b flagPauseTicksLeft=%d pos=%.2f,%.2f,%.2f", tmp0));
            }
            this.bouncePrevSpeedSample = var2;
        } else {
            if (isBounceDebugOn()) {
            } else {
            }
            bounceHorizontalSpeedometer().bouncePrevSpeedSample = -1.0;
        }
    }

    private void tickBounceMode() {
        if (((mc.player == null) || (mc.level == null)) || (mc.getConnection() == null)) {
            return;
        }
        if (!(canGlideNow())) {
            resetBounceState();
            flushBouncePackets();
            return;
        }
        if (!(this.bounceBypass.getValue())) break label_4;
        if (this.bounceBypassTicksLeft > 0) {
            this.bounceBypassTicksLeft = (this.bounceBypassTicksLeft - 1);
        }
        if (!(isNearingBounceLanding())) break label_4;
        this.bounceBypassTicksLeft = Math.max(this.bounceBypassTicksLeft, this.bounceBypassTicks.getValue().intValue());
        label_4:
        if (handlePassingObstacles()) {
            this.bounceDoJump = false;
            return;
        }
        if (this.bounceFlagPauseTicksLeft > 0) {
            this.bounceFlagPauseTicksLeft = (this.bounceFlagPauseTicksLeft - 1);
            return;
        }
        if (isFlightPaused()) return;
        if (!(isGliding())) {
            if ((!(this.bounceTakeoff.getValue())) || (!(canTakeoff()))) break label_10;
            if (canStartGliding()) {
                startFly();
                break label_10;
            }
            double var1 = Math.toRadians(((double) mc.player.getYRot()));
            double var3 = -(Math.cos(var1));
            double var5 = -(Math.sin(var1));
            Object var7 = mc.player.getDeltaMovement();
            double var8 = Math.abs(((var7.x * var3) + (var7.z * var5)));
            if (var8 >= 0.001) return;
            this.bounceJumpThisTick = true;
            label_10:
            return;
        }
        var1 = ((EntityFlagAccessor) mc.player).invokeGetSharedFlag(EntityFlagAccessor.getFlagFallFlying());
        if (this.bounceMinimizePackets.getValue() && !(!var1)) return;
        startFly();
    }

    public void startFly() {
        if ((mc.player == null) || (mc.getConnection() == null)) {
            return;
        }
        int var1 = ((EntityFlagAccessor) mc.player).invokeGetSharedFlag(EntityFlagAccessor.getFlagFallFlying());
        ((EntityFlagAccessor) mc.player).invokeSetSharedFlag(EntityFlagAccessor.getFlagFallFlying(), true);
        mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        if (this.bounceBypass.getValue()) {
            this.bounceBypassTicksLeft = this.bounceBypassTicks.getValue().intValue();
        }
        if (isBounceDebugOn()) {
            double var2 = bounceHorizontalSpeedometer();
            var tmp0 = new Object[5];
            tmp0[0] = var2;
            tmp0[1] = (var2 * 3.6);
            tmp0[2] = mc.player.onGround();
            tmp0[3] = var1;
            tmp0[4] = this.bounceFlagPauseTicksLeft;
            debugLog(String.format("START FLY: %.2f m/s (%.1f km/h) onGround=%b rawFlying=%b flagPauseTicksLeft=%d", tmp0));
        }
    }

    private void onPutOnElytraTick() {
        if (!(this.bouncePutOnElytra.getValue())) return;
        if (((mc.gameMode == null) || (mc.player == null)) || (mc.getConnection() == null)) {
            return;
        }
        if (mc.player.containerMenu != mc.player.inventoryMenu) break label_3;
        var tmp0 = mc.player.inventoryMenu.getCarried();
        Object var2 = tmp0;
        if (tmp0.getItem() != Items.ELYTRA) break label_3;
        resolveCarriedElytra();
        return;
        label_3:
        if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) break label_4;
        var tmp1 = findElytraInInventory();
        int var1 = tmp1;
        if (tmp1 == -1) break label_4;
        swapIntoChestSlot(var1);
        label_4:
    }

    private void resolveCarriedElytra() {
        int var1 = mc.player.inventoryMenu.containerId;
        mc.gameMode.handleContainerInput(var1, 6, 0, ContainerInput.PICKUP, mc.player);
        if (mc.player.inventoryMenu.getCarried().getItem() != Items.ELYTRA) return;
        int var2 = findEmptyInventorySlot();
        if (var2 != -1) {
            mc.gameMode.handleContainerInput(var1, var2, 0, ContainerInput.PICKUP, mc.player);
            return;
        }
        int var3 = findAnyInventorySlot();
        if (var3 != -1) {
            mc.gameMode.handleContainerInput(var1, var3, 0, ContainerInput.PICKUP, mc.player);
        }
    }

    private void swapIntoChestSlot(int arg0) {
        int var2 = mc.player.inventoryMenu.containerId;
        mc.gameMode.handleContainerInput(var2, arg0, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(var2, 6, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(var2, arg0, 0, ContainerInput.PICKUP, mc.player);
    }

    private int findElytraInInventory() {
        int var1 = 9;
        while (var1 <= 44) {
            if (mc.player.inventoryMenu.getSlot(var1).getItem().getItem() != Items.ELYTRA) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

    private int findEmptyInventorySlot() {
        int var1 = 9;
        while (var1 <= 44) {
            if (!(mc.player.inventoryMenu.getSlot(var1).getItem().isEmpty())) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

    private int findAnyInventorySlot() {
        int var1 = 9;
        while (var1 <= 44) {
            if (mc.player.inventoryMenu.getSlot(var1).getItem().isEmpty()) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

    public boolean isGliding() {
        if (mc.player == null) return false;
        int var1 = ((EntityFlagAccessor) mc.player).invokeGetSharedFlag(EntityFlagAccessor.getFlagFallFlying());
        if (!(!this.bouncePrevGliding) && !(isFlightPaused())) return true;
        this.bouncePrevGliding = var1;
        return var1;
    }

    public boolean isGlidingMasked() {
        return isGliding();
    }

    public boolean isFlightPaused() {
        if (this.bounceFlagPauseTicksLeft <= 0) {
            if (BaritoneUtils.isActive()) {
            }
        } else {
        }
        return false;
    }

    public boolean canTakeoff() {
        if (mc.player == null) return false;
        if (!(mc.player.onGround())) {
            if (isGliding()) break label_2;
        }
        if (((((mc.player.getAbilities().flying) || (mc.player.onClimbable())) || (mc.player.isInWater())) || (mc.player.isPassenger())) || (mc.player.hasEffect(MobEffects.LEVITATION))) break label_2;
        return canGlideItem();
    }

    public boolean canStartGliding() {
        if (mc.player == null) return false;
        if (((((isGliding()) || (mc.player.onGround())) || (mc.player.onClimbable())) || (mc.player.isInWater())) || (mc.player.hasEffect(MobEffects.LEVITATION))) break label_1;
        return canGlideItem();
    }

    public boolean canGlideNow() {
        return canGlideItem();
    }

    private boolean canGlideItem() {
        if (mc.player == null) return false;
        Object var1 = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        return LivingEntity.canGlideUsing(var1, EquipmentSlot.CHEST);
    }

    private Vec3 getSnappedObstacleDir() {
        if (mc.player == null) return new Vec3(0.0, 0.0, 1.0);
        Object var1 = mc.player.position().subtract(this.bounceObstacleStartPos);
        if (var1.lengthSqr() > 1.0E-12) {
        } else {
        }
        Object var2 = new Vec3(0.0, 0.0, 1.0);
        double var3 = var2.x;
        double var5 = var2.z;
        double var7 = Math.toDegrees(Math.atan2(var5, var3));
        double var9 = (((var7 % 360.0) + 360.0) % 360.0);
        double var11 = (((double) Math.round((var9 / 45.0))) * 45.0);
        double var13 = Math.toRadians((((var11 % 360.0) + 360.0) % 360.0));
        double var15 = Math.sqrt(((var3 * var3) + (var5 * var5)));
        return new Vec3((Math.cos(var13) * var15), 0.0, (Math.sin(var13) * var15));
    }

    private Vec3 findClosestPointOnObstacleLine(Vec3 arg0, Vec3 arg1) {
        Object var3 = arg0.subtract(this.bounceObstacleStartPos);
        double var4 = arg1.dot(arg1);
        if (var4 > 1.0E-9) {
        } else {
        }
        double var6 = 0.0;
        return this.bounceObstacleStartPos.add(arg1.scale(var6));
    }

    private boolean obstacleRayCastObstructed(Vec3 arg0, Vec3 arg1) {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        double var3 = this.bounceObstacleLookAhead.getValue().doubleValue();
        if (var3 <= 0.0) return false;
        if (arg1.length() > 1.0E-6) {
        } else {
        }
        Object var5 = new Vec3(0.0, 0.0, 1.0);
        Object var6 = arg0.add(var5.scale(var3));
        Object var7 = mc.level.clip(new ClipContext(arg0, var6, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return var7.getType() == HitResult.Type.BLOCK;
    }

    private boolean isObstacleObstructed(Vec3 arg0, Vec3 arg1) {
        if (mc.level == null) return false;
        Object var3 = BlockPos.containing(arg0.x, arg0.y, arg0.z).below();
        if (!(mc.level.getBlockState(var3).isFaceSturdy(mc.level, var3, Direction.UP))) return true;
        if (obstacleRayCastObstructed(arg0.add(0.0, 0.063, 0.0), arg1)) return true;
        if (obstacleRayCastObstructed(arg0.add(0.0, 1.01, 0.0), arg1)) return true;
        return obstacleRayCastObstructed(arg0.add(0.0, 1.99, 0.0), arg1);
    }

    private void pathToValidObstaclePoint(Vec3 arg0, Vec3 arg1, boolean arg2) {
        double var4 = Math.max(1.0, this.bounceObstacleLookAhead.getValue().doubleValue());
        if (arg1.length() > 1.0E-6) {
        } else {
        }
        Object var6 = new Vec3(0.0, 0.0, 1.0);
        int var7 = (!arg2 ? 1 : 0);
        Object var8 = arg0;
        int var9 = 0;
        label_4:
        if (!var7) {
            if (!(isObstacleObstructed(var8, var6))) break label_6;
        }
        var9++;
        if (var9 >= 200) break label_6;
        var8 = var8.add(var6.scale(var4));
        var7 = 0;
        break label_4;
        label_6:
        passObstacleTo(var8);
    }

    private void passObstacleTo(Vec3 arg0) {
        this.bounceObstaclePassingToPos = arg0;
        if (this.bounceStandUpToPass.getValue()) {
            forceStandUpForObstacle();
            try {
            }
            Object var2 = BaritoneUtils.getPrimaryBaritone();
            if (var2 == null) {
                return;
                try {
                }
                Object var3 = BlockPos.containing(arg0.x, arg0.y, arg0.z);
                Object var4 = Class.forName("baritone.api.pathing.goals.Goal");
                var tmp0 = new Class[1];
                tmp0[0] = BlockPos.class;
                var tmp1 = new Object[1];
                tmp1[0] = var3;
                Object var5 = Class.forName("baritone.api.pathing.goals.GoalGetToBlock").getConstructor(tmp0).newInstance(tmp1);
                Object var6 = var2.getClass().getMethod("getCustomGoalProcess", new Class[0]).invoke(var2, new Object[0]);
                var tmp2 = new Class[1];
                tmp2[0] = var4;
                var tmp3 = new Object[1];
                tmp3[0] = var5;
                var6.getClass().getMethod("setGoalAndPath", tmp2).invoke(var6, tmp3);
                if (isBounceDebugOn()) {
                    debugLog(("OBSTACLE PASS: goal set to " + String.valueOf(var3)));
                }
            } catch (Throwable ex4) {
                var2 = ex4;
                if (!(isBounceDebugOn())) {
                    break label_6;
                }
                debugLog(("OBSTACLE PASS FAILED: " + String.valueOf(var2)));
            }
            label_6:
        }
    }

    private void forceStandUpForObstacle() {
        if ((mc.player == null) || (!(mc.player.isFallFlying()))) {
            return;
        }
        if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;
        int var1 = findEmptyInventorySlot();
        if (var1 == -1) {
            ((EntityFlagAccessor) mc.player).invokeSetSharedFlag(EntityFlagAccessor.getFlagFallFlying(), false);
            if (isBounceDebugOn()) {
                debugLog("STAND-UP-TO-PASS: inventory full, fell back to flag-only clear");
            }
            return;
        }
        int var2 = mc.player.inventoryMenu.containerId;
        mc.gameMode.handleContainerInput(var2, 6, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(var2, var1, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(var2, var1, 0, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(var2, 6, 0, ContainerInput.PICKUP, mc.player);
        if (isBounceDebugOn()) {
            debugLog("STAND-UP-TO-PASS: unequipped/re-equipped elytra for obstacle passing");
        }
    }

    private void cancelObstaclePath() {
        try {
            Object var1 = BaritoneUtils.getPrimaryBaritone();
            if (var1 == null) {
                return;
                try {
                }
                Object var2 = var1.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(var1, new Object[0]);
                var2.getClass().getMethod("cancelEverything", new Class[0]).invoke(var2, new Object[0]);
            } catch (Throwable ex4) {
                var1 = ex4;
            }
        }
    }

    private boolean handlePassingObstacles() {
        if (mc.player == null) return false;
        if (!(this.bouncePassObstacles.getValue())) return false;
        if (!(BaritoneUtils.isAvailable())) return false;
        if (!(BaritoneUtils.isActive())) {
            this.bounceObstaclePassingToPos = null;
        }
        Object var2 = mc.player.position();
        double var3 = Math.sqrt((Math.pow((var2.x - this.bounceObstacleStartPos.x), 2.0) + Math.pow((var2.z - this.bounceObstacleStartPos.z), 2.0)));
        if (var3 <= 0.1) return false;
        Object var5 = getSnappedObstacleDir();
        Object var6 = findClosestPointOnObstacleLine(var2, var5);
        if (this.bounceObstaclePassingToPos != null) {
            if (this.bounceStandUpToPass.getValue()) {
                forceStandUpForObstacle();
            }
            if (isObstacleObstructed(this.bounceObstaclePassingToPos, var5)) {
                pathToValidObstaclePoint(this.bounceObstaclePassingToPos, var5, false);
            }
            return true;
        }
        if (!(mc.player.onGround())) return false;
        var tmp0 = (bounceHorizontalSpeedometer() < 0.01 ? 1 : 0);
        int var1 = tmp0;
        int var7 = tmp0;
        if (isGliding() && !(!var1)) {
            pathToValidObstaclePoint(var6, var5, false);
            return true;
        }
        Object var8 = new Vec3(var2.x, var6.y, var2.z);
        double var9 = (var8.distanceTo(var6) + Math.min(0.0, (var2.y - var6.y)));
        if (var9 > 2.0) {
            pathToValidObstaclePoint(var6, var5, true);
            return true;
        }
        if (isObstacleObstructed(var8, var5)) {
            pathToValidObstaclePoint(var6, var5, false);
            return true;
        }
        return BaritoneUtils.isActive();
    }

    private double bounceHorizontalSpeedometer() {
        Object var1 = this.bounceSpeedCurrPos.subtract(this.bounceSpeedPrevPos);
        return (Math.sqrt(((var1.x * var1.x) + (var1.z * var1.z))) * 20.0);
    }

    private boolean isInDipWindow() {
        if (mc.player == null) return false;
        return (mc.player.getY() - this.bounceStartY) < 0.163;
    }

    private boolean isNearingBounceLanding() {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        Object var2 = mc.player.getDeltaMovement();
        if (var2.y >= -0.01) return false;
        Object var3 = mc.player.position();
        var tmp0 = var3.add(0.0, -8.0, 0.0);
        Object var1 = tmp0;
        Object var4 = mc.level.clip(new ClipContext(var3, tmp0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var4.getType() != HitResult.Type.BLOCK) return false;
        double var5 = (var3.y - var4.getLocation().y);
        double var7 = (var5 / Math.abs(var2.y));
        return var7 <= this.bounceBypassTicks.getValue().doubleValue();
    }

    private double getBounceHorizontalSpeed() {
        if (mc.player == null) return 0.0;
        Object var1 = mc.player.getDeltaMovement();
        return (Math.sqrt(((var1.x * var1.x) + (var1.z * var1.z))) * 20.0);
    }

    private void flushBouncePackets() {
        if (!(!this.isFlushingBouncePackets)) return;
        this.isFlushingBouncePackets = true;
        if (mc.getConnection() == null) {
            this.bounceSendPacketQueue.clear();
            this.bouncePingPacketQueue.clear();
            this.isFlushingBouncePackets = false;
            return;
        }
        if (this.bounceSendPacketQueue.isEmpty()) break label_6;
        Object var1 = mc.getConnection().getConnection();
        if (var1 != null) {
            if (var1.isConnected()) break label_5;
        }
        this.bounceSendPacketQueue.clear();
        break label_6;
        label_5:
        var tmp0 = ((Packet) this.bounceSendPacketQueue.poll());
        Object var2 = tmp0;
        if (tmp0 == null) break label_6;
        var1.send(var2, null, true);
        break label_5;
        label_6:
        if (this.bouncePingPacketQueue.isEmpty()) break label_8;
        label_7:
        var tmp1 = ((ClientboundPingPacket) this.bouncePingPacketQueue.poll());
        var1 = tmp1;
        if (tmp1 == null) break label_8;
        var1.handle(mc.getConnection());
        break label_7;
        label_8:
        this.isFlushingBouncePackets = false;
        break label_10;
        Object var3 = null;
        this.isFlushingBouncePackets = false;
        throw var3;
        label_10:
    }

    private void resetBounceState() {
        this.bounceDoJump = false;
        this.bounceJumpThisTick = false;
        this.bouncePendingResend = false;
        this.bounceTakeoffPending = false;
        this.bounceFlagPauseTicksLeft = 0;
        this.bouncePrevGliding = false;
        this.bouncePrevJumpKeyDown = false;
        this.bounceWasFlyingForLog = false;
        this.bounceBypassTicksLeft = 0;
    }

    private void debugLog(String arg0) {
        if (mc.player != null) {
            LightningClient.CHAT_MANAGER.tagged(arg0, "EBounce+");
        }
    }

    private boolean isBounceDebugOn() {
        Object var1 = ((ElytraBounceDebugModule) LightningClient.MODULE_MANAGER.getModule(ElytraBounceDebugModule.class));
        if (var1 == null) break label_0;
        return var1.isToggled();
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    static {
        bouncePitchOverrideActive = false;
        bounceSavedCameraPitch = 0.0f;
    }

}
