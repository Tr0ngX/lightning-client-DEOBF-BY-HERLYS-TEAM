package lightning.pingbypass.modules.submodules.crystal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.DestroyBlockEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.modules.impl.combat.CrystalPlacementHelper;
import lightning.modules.impl.combat.SuicideModule;
import lightning.modules.impl.movement.PhaseModule;
import lightning.modules.impl.player.KeyActionModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.modules.PbModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.NetworkUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Counter;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ServerAutoCrystal extends PbModule implements IMinecraft {
    public BooleanSetting attack;
    public NumberSetting attackSpeed;
    public NumberSetting attackRange;
    public NumberSetting attackWallsRange;
    public ModeSetting antiWeakness;
    public BooleanSetting instant;
    public BooleanSetting inhibit;
    public BooleanSetting place;
    public NumberSetting placeSpeed;
    public NumberSetting placeRange;
    public NumberSetting placeWallsRange;
    public ModeSetting placements;
    public BooleanSetting blockDestruction;
    public ModeSetting autoSwitch;
    public BooleanSetting swapBack;
    public NumberSetting swapDelay;
    public ModeSetting sequential;
    public ModeSetting rotate;
    public ModeSetting swing;
    public BooleanSetting yawStep;
    public NumberSetting yawStepThreshold;
    public BooleanSetting raytrace;
    public NumberSetting extrapolation;
    public NumberSetting enemyRange;
    public BooleanSetting ignoreNaked;
    public BooleanSetting chestBreak;
    public BooleanSetting pauseOnPearl;
    public BooleanSetting pauseOnXP;
    public BooleanSetting gameLoop;
    public NumberSetting loopDelay;
    public ModeSetting whileEating;
    public BooleanSetting godSync;
    public NumberSetting predictions;
    public NumberSetting offset;
    public ModeSetting godSwing;
    public BooleanSetting fast;
    public BooleanSetting antiKick;
    public NumberSetting kickThreshold;
    public ModeSetting facePlaceMode;
    public ModeSetting facePlaceSpeed;
    public NumberSetting facePlaceDelay;
    public BooleanSetting healthPlace;
    public NumberSetting health;
    public BooleanSetting armorPlace;
    public NumberSetting percentage;
    public NumberSetting minimumDamage;
    public NumberSetting maximumSelfDamage;
    public NumberSetting lethalMultiplier;
    public BooleanSetting antiSuicide;
    public BooleanSetting ignoreTerrain;
    private final ExecutorService executor;
    private volatile Future pendingCalc;
    private Runnable attackRunnable;
    private Runnable placeRunnable;
    private final Map attackedCrystals;
    private final Map placedCrystals;
    private final Map countedCrystals;
    private final Timer attackTimer;
    private final Timer placeTimer;
    private final Timer facePlaceTimer;
    private final Timer loopTimer;
    private final Timer swapTimer;
    private int lastSelectedSlot;
    private long totalPlaces;
    private long totalAttacks;
    private boolean sequenceAttack;
    private boolean sequencePlace;
    private boolean attackedSequentially;
    private boolean placedSequentially;
    private Player target;
    private EndCrystal attackTarget;
    private ServerAutoCrystal.PlaceTarget placeTarget;
    private ServerAutoCrystal.PlaceTarget mineTarget;
    private String calculationTime;
    private int calculationCount;
    private String calculationDamage;
    private final Counter crystalCounter;
    private int crystalsPerSecond;
    private int highestID;
    private int kickTicks;
    private int savedSlot;
    private static final float STICKY_EPSILON = 0.5f;
    private long lastPearlThrowTime;
    private long lastXpThrowTime;

    public ServerAutoCrystal() {
        super("AutoCrystal");
        this.attack = new BooleanSetting("Attack", "Automatically attacks crystals that are deemed safe.", true);
        this.attackSpeed = new NumberSetting("AttackSpeed", "The speed at which crystals will be attacked.", 20.0f, 0.1f, 20.0f);
        this.attackRange = new NumberSetting("AttackRange", "The maximum distance at which crystals will be attacked.", 4.5, 0.0, 8.0);
        this.attackWallsRange = new NumberSetting("AttackWallsRange", "The maximum distance at which crystals will be attacked through walls.", 4.5, 0.0, 8.0);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Silent";
        this.antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", "None", tmp0);
        this.instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", true);
        this.inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", true);
        this.place = new BooleanSetting("Place", "Automatically places crystals on positions that are deemed safe and lethal enough.", true);
        this.placeSpeed = new NumberSetting("PlaceSpeed", "The speed at which crystals will be placed.", 20.0f, 0.1f, 20.0f);
        this.placeRange = new NumberSetting("PlaceRange", "The maximum distance at which positions will be placed on.", 4.5, 0.0, 8.0);
        this.placeWallsRange = new NumberSetting("PlaceWallsRange", "The maximum distance at which positions will be placed on through walls.", 4.5, 0.0, 8.0);
        var tmp1 = new String[2];
        tmp1[0] = "Native";
        tmp1[1] = "Protocol";
        this.placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", "Native", tmp1);
        this.blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", true);
        var tmp2 = new String[4];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        tmp2[2] = "Silent";
        tmp2[3] = "AltSwap";
        this.autoSwitch = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", "None", tmp2);
        this.swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were holding before the module started switching to crystals.", false);
        this.swapDelay = new NumberSetting("SwapDelay", "The delay in ticks after swapping before placing or attacking crystals.", 0, 0, 20);
        var tmp3 = new String[3];
        tmp3[0] = "None";
        tmp3[1] = "Strict";
        tmp3[2] = "Strong";
        this.sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", "Strong", tmp3);
        var tmp4 = new String[4];
        tmp4[0] = "None";
        tmp4[1] = "Normal";
        tmp4[2] = "Packet";
        tmp4[3] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", "Normal", tmp4);
        var tmp5 = new String[6];
        tmp5[0] = "Default";
        tmp5[1] = "None";
        tmp5[2] = "Packet";
        tmp5[3] = "Mainhand";
        tmp5[4] = "Offhand";
        tmp5[5] = "Both";
        this.swing = new ModeSetting("Swing", "The hand that will be used for swinging.", "Default", tmp5);
        this.yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", false);
        this.yawStepThreshold = new NumberSetting("YawStepThreshold", "The threshold in order for yaw to be modified.", 75, 1, 180);
        this.raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", false);
        this.extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", 0, 0, 20);
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", 10.0, 0.0, 24.0);
        this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", false);
        this.chestBreak = new BooleanSetting("ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", false);
        this.pauseOnPearl = new BooleanSetting("PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", true);
        this.pauseOnXP = new BooleanSetting("PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", true);
        this.gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", false);
        this.loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", 50, 0, 1000);
        var tmp6 = new String[4];
        tmp6[0] = "None";
        tmp6[1] = "Attack";
        tmp6[2] = "Place";
        tmp6[3] = "Both";
        this.whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", "Both", tmp6);
        this.godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", false);
        this.predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", 10, 1, 20);
        this.offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", 0, 0, 2);
        var tmp7 = new String[3];
        tmp7[0] = "None";
        tmp7[1] = "Normal";
        tmp7[2] = "Strict";
        this.godSwing = new ModeSetting("GodSwing", "The swinging that will be done for each predicted attack.", "Normal", tmp7);
        this.fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", false);
        this.antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", false);
        this.kickThreshold = new NumberSetting("KickThreshold", "The tick threshold for the kick prevention.", 5, 1, 10);
        var tmp8 = new String[3];
        tmp8[0] = "None";
        tmp8[1] = "Dynamic";
        tmp8[2] = "Always";
        this.facePlaceMode = new ModeSetting("FaceplaceMode", "The checks that will be done in order to faceplace.", "Dynamic", tmp8);
        var tmp9 = new String[2];
        tmp9[0] = "Normal";
        tmp9[1] = "Custom";
        this.facePlaceSpeed = new ModeSetting("FaceplaceSpeed", "The speed that players will be faceplaced at.", "Normal", tmp9);
        this.facePlaceDelay = new NumberSetting("FaceplaceDelay", "The ticks that have to be waited for before faceplacing again.", 11, 0, 20);
        this.healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", true);
        this.health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", 8.0f, 0.0f, 36.0f);
        this.armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", true);
        this.percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", 10, 1, 100);
        this.minimumDamage = new NumberSetting("MinimumDamage", "The minimum damage that has to be dealt to enemies.", 6.0, 0.0, 36.0);
        this.maximumSelfDamage = new NumberSetting("MaximumSelfDamage", "The maximum damage that can be dealt to you by crystals.", 10.0, 0.0, 36.0);
        this.lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", 1.5f, 0.0f, 4.0f);
        this.antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", true);
        this.ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", true);
        this.executor = Executors.newSingleThreadExecutor();
        this.pendingCalc = null;
        this.attackRunnable = null;
        this.placeRunnable = null;
        this.attackedCrystals = new ConcurrentHashMap();
        this.placedCrystals = new ConcurrentHashMap();
        this.countedCrystals = new ConcurrentHashMap();
        this.attackTimer = new Timer();
        this.placeTimer = new Timer();
        this.facePlaceTimer = new Timer();
        this.loopTimer = new Timer();
        this.swapTimer = new Timer();
        this.lastSelectedSlot = -1;
        this.totalPlaces = 0L;
        this.totalAttacks = 0L;
        this.sequenceAttack = false;
        this.sequencePlace = true;
        this.attackedSequentially = false;
        this.placedSequentially = false;
        this.target = null;
        this.attackTarget = null;
        this.placeTarget = null;
        this.mineTarget = null;
        this.calculationTime = "0.00ms";
        this.calculationCount = 0;
        this.calculationDamage = "0.00";
        this.crystalCounter = new Counter();
        this.crystalsPerSecond = 0;
        this.highestID = -100000;
        this.kickTicks = 0;
        this.savedSlot = -1;
        this.lastPearlThrowTime = 0L;
        this.lastXpThrowTime = 0L;
    }

    public void onEnable() {
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    public void onDisable() {
        LightningClient.EVENT_HANDLER.unsubscribe(this);
        if (this.pendingCalc != null) {
            this.pendingCalc.cancel(false);
            this.pendingCalc = null;
        }
        if (this.savedSlot != -1) {
            InventoryUtils.switchBackNormal(this.savedSlot);
            this.savedSlot = -1;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        LightningClient.RENDER_MANAGER.setRenderPosition(null);
        this.attackedCrystals.clear();
        this.placedCrystals.clear();
        this.countedCrystals.clear();
        this.attackedSequentially = false;
        this.placedSequentially = false;
        this.target = null;
        this.placeTarget = null;
        this.mineTarget = null;
        this.calculationTime = "0.00ms";
        this.calculationCount = 0;
        this.calculationDamage = "0.00";
        this.crystalCounter.reset();
        this.highestID = -100000;
    }

    public List getSettings() {
        var tmp0 = new Setting[47];
        tmp0[0] = this.attack;
        tmp0[1] = this.attackSpeed;
        tmp0[2] = this.attackRange;
        tmp0[3] = this.attackWallsRange;
        tmp0[4] = this.antiWeakness;
        tmp0[5] = this.instant;
        tmp0[6] = this.inhibit;
        tmp0[7] = this.place;
        tmp0[8] = this.placeSpeed;
        tmp0[9] = this.placeRange;
        tmp0[10] = this.placeWallsRange;
        tmp0[11] = this.placements;
        tmp0[12] = this.blockDestruction;
        tmp0[13] = this.autoSwitch;
        tmp0[14] = this.swapBack;
        tmp0[15] = this.swapDelay;
        tmp0[16] = this.sequential;
        tmp0[17] = this.rotate;
        tmp0[18] = this.swing;
        tmp0[19] = this.yawStep;
        tmp0[20] = this.yawStepThreshold;
        tmp0[21] = this.raytrace;
        tmp0[22] = this.extrapolation;
        tmp0[23] = this.enemyRange;
        tmp0[24] = this.chestBreak;
        tmp0[25] = this.gameLoop;
        tmp0[26] = this.loopDelay;
        tmp0[27] = this.whileEating;
        tmp0[28] = this.godSync;
        tmp0[29] = this.predictions;
        tmp0[30] = this.offset;
        tmp0[31] = this.godSwing;
        tmp0[32] = this.fast;
        tmp0[33] = this.antiKick;
        tmp0[34] = this.kickThreshold;
        tmp0[35] = this.facePlaceMode;
        tmp0[36] = this.facePlaceSpeed;
        tmp0[37] = this.facePlaceDelay;
        tmp0[38] = this.healthPlace;
        tmp0[39] = this.health;
        tmp0[40] = this.armorPlace;
        tmp0[41] = this.percentage;
        tmp0[42] = this.minimumDamage;
        tmp0[43] = this.maximumSelfDamage;
        tmp0[44] = this.lethalMultiplier;
        tmp0[45] = this.antiSuicide;
        tmp0[46] = this.ignoreTerrain;
        return List.of(tmp0);
    }

    private boolean isDead() {
        if ((mc.player == null) || (mc.level == null)) {
            return true;
        }
        if (((!(mc.player.isAlive())) || (mc.player.isDeadOrDying())) || (mc.player.getHealth() <= 0.0f)) {
            return true;
        }
        return (mc.gui.screen() instanceof DeathScreen);
    }

    public void tick() {
        if (isDead()) {
            this.attackRunnable = null;
            this.placeRunnable = null;
            this.target = null;
            this.placeTarget = null;
            this.attackTarget = null;
            this.mineTarget = null;
            this.placedCrystals.clear();
            this.attackedCrystals.clear();
            this.countedCrystals.clear();
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        int var1 = mc.player.getInventory().getSelectedSlot();
        if (this.lastSelectedSlot != -1 && this.lastSelectedSlot != var1) {
            this.swapTimer.reset();
        }
        this.lastSelectedSlot = var1;
        long var2 = 50L;
        this.attackedCrystals.entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), arg0));
        long var4 = (Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), 500L) + ((long) ((20.0f - this.attackSpeed.getValue().floatValue()) * 50.0f)));
        this.placedCrystals.entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > arg0);
        this.countedCrystals.entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), arg0));
        this.crystalsPerSecond = this.crystalCounter.getCount();
        Object var6 = () -> {
    long var1 = System.nanoTime();
    this.attackTarget = calculateCrystals();
    this.placeTarget = calculatePlacements(null);
    long var3 = (System.nanoTime() - var1);
    this.calculationTime = (new DecimalFormat("0.00").format((((double) var3) / 1000000.0)) + "ms");
    if (this.placeTarget == null) {
    } else {
    }
    0.calculationCount = this.placeTarget.getCalculations();
    if (this.placeTarget == null) {
    } else {
    }
    "0.00".calculationDamage = new DecimalFormat("0.00").format(((double) this.placeTarget.getDamage()));
    if (this.placeTarget == null) {
    } else {
    }
    null.target = this.placeTarget.getPlayer();
    Object var5 = this.placeTarget.getPlayer();
    if (!(this.blockDestruction.getValue())) break label_7;
    Object var6 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
    Object var7 = null;
    if (var6.getPrimary() != null && var6.getPrimary().isMining()) {
        var7 = var6.getPrimary().getPosition();
    }
    if (var7 == null) break label_7;
    this.mineTarget = calculatePlacements(var7);
    label_7:
};
        if (this.pendingCalc != null) {
            if (this.pendingCalc.isDone()) {
            }
            this.pendingCalc = this.executor.submit(var6);
        }
        if (this.gameLoop.getValue()) {
            if (!(this.loopTimer.hasTimeElapsed(this.loopDelay.getValue().longValue()))) return;
            this.loopTimer.reset();
        }
        run();
        if (this.attackRunnable != null) {
            this.attackRunnable.run();
            this.attackRunnable = null;
        }
        if (this.placeRunnable != null) {
            this.placeRunnable.run();
            this.placeRunnable = null;
        }
    }

    private void run() {
        if (((isDead()) || (isPearlPaused())) || (isXpPaused())) {
            return;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        if (this.sequential.getValue().equalsIgnoreCase("None")) {
            if (!(!this.sequenceAttack)) {
                this.sequenceAttack = false;
                this.sequencePlace = true;
                attackCrystals();
                return;
            }
            if (!this.sequencePlace) break label_5;
            this.sequenceAttack = true;
            this.sequencePlace = false;
            placeCrystals(false);
            break label_5;
        }
        if (this.attack.getValue()) {
            attackCrystals();
        }
        if (!(this.place.getValue())) break label_5;
        placeCrystals(false);
        label_5:
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        if ((((!(isToggled())) || (isDead())) || (getPlayers().isEmpty())) || (shouldPause("Attack"))) {
            return;
        }
        if ((!(this.attack.getValue())) || (!(this.instant.getValue()))) {
            return;
        }
        if (!(this.attackTimer.hasTimeElapsed((1000.0f - (this.attackSpeed.getValue().floatValue() * 50.0f))))) return;
        Object var2 = arg0.getEntity();
        if (!((var2 instanceof EndCrystal))) return;
        Object var3 = ((EndCrystal) var2);
        if (this.inhibit.getValue() && this.attackedCrystals.containsKey(var3.getId())) return;
        if (!(this.placedCrystals.containsKey(var3.blockPosition().below()))) return;
        if (var3.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) return;
        if (!(mc.level.getWorldBorder().isWithinBounds(var3.blockPosition()))) return;
        if (!(WorldUtils.canSee(var3))) {
            if ((this.raytrace.getValue()) || (var3.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue()))) {
                return;
            }
        }
        if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(var3.blockPosition())));
        }
        attack(var3);
        this.attackedSequentially = true;
        if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
            placeCrystals(true);
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent arg0) {
        if ((((!(isToggled())) || (isDead())) || (getPlayers().isEmpty())) || (shouldPause("Place"))) {
            return;
        }
        this.kickTicks = 0;
        if (!(this.blockDestruction.getValue())) return;
        if (!(this.placeTimer.hasTimeElapsed((1000.0f - (this.placeSpeed.getValue().floatValue() * 50.0f))))) return;
        Object var5 = arg0.getPosition();
        if (var5 == null) return;
        int var6 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int var7 = mc.player.getInventory().getSelectedSlot();
        int var8 = 0;
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None")) && var6 == -1 && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) return;
        if (this.mineTarget == null) {
        } else {
        }
        var tmp0 = this.mineTarget.clone();
        Object var4 = tmp0;
        Object var9 = tmp0;
        if (var4 != null) {
            if (var4.getPosition() != null && !(var5.equals(var4.getException()))) {
            }
            var4 = calculatePlacements(var5);
        }
        if (var4 != null && var4.getPosition() != null) {
            if (var4.getDamage() <= 0.0f) {
            }
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        Object var10 = var4.getPosition();
        if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var10)) > Mth.square(this.placeRange.getValue().doubleValue())) {
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        LightningClient.RENDER_MANAGER.setRenderPosition(var10);
        if (!(WorldUtils.canSeeBlock(var10))) {
            if ((this.raytrace.getValue()) || (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var10)) > Mth.square(this.placeWallsRange.getValue().doubleValue()))) {
                return;
            }
        }
        Object var11 = null;
        Object var12 = mc.level.getEntities(((Entity) null), new AABB(var10.above()), (arg0) -> true).iterator();
        label_15:
        if (!(var12.hasNext())) break label_17;
        Object var13 = ((Entity) var12.next());
        if (!((var13 instanceof EndCrystal))) {
            break label_15;
        }
        var tmp1 = ((EndCrystal) var13);
        Object var14 = tmp1;
        var11 = tmp1;
        break label_17;
        label_17:
        if (var11 != null) {
            if (isAtActiveUnbrokenMinePos(var11)) {
                LightningClient.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            if (!(this.attack.getValue())) {
                LightningClient.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
                LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(var11));
            }
            attack(var11);
            return;
        }
        var12 = CrystalPlacementHelper.getVisiblePlacement(var10);
        if (var12 != null && var12.hitVec != null) {
        } else {
        }
        var tmp2 = Vec3.atCenterOf(var10).add(0.0, 0.5, 0.0);
        Object var3 = tmp2;
        var13 = tmp2;
        if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(var3));
        }
        var14 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (!(var14.switchReset.getValue())) break label_26;
        if (!(var14.switchMode.getValue().equalsIgnoreCase("Normal")) && !(var14.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
            if (!(var14.switchMode.getValue().equalsIgnoreCase("AltPickup"))) break label_26;
        }
        break label_27;
        label_26:
        label_27:
        int var2 = 0;
        int var15 = 0;
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None")) && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (!var2 && this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = var7;
            }
            if (!(!var2)) {
            } else {
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), var6, var7);
            var8 = 1;
        }
        place(var10);
        if (!(!var8)) {
            if (!(!var2)) {
            } else {
            }
            InventoryUtils.switchBack(this.autoSwitch.getValue(), var6, var7);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundAddEntityPacket))) break label_4;
        Object var4 = ((ClientboundAddEntityPacket) var2);
        if (var4.getId() > this.highestID) {
            this.highestID = var4.getId();
        }
        var tmp0 = BlockPos.containing(var4.getX(), var4.getY(), var4.getZ()).offset(0, -1, 0);
        Object var3 = tmp0;
        if (!(this.countedCrystals.containsKey(tmp0))) break label_4;
        if (this.facePlaceTimer.hasTimeElapsed((this.facePlaceDelay.getValue().longValue() * 50L))) {
            this.facePlaceTimer.reset();
        }
        this.countedCrystals.remove(var3);
        this.crystalCounter.increment();
        this.crystalsPerSecond = this.crystalCounter.getCount();
        label_4:
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        this.kickTicks = 0;
        this.attackRunnable = null;
        this.placeRunnable = null;
        this.target = null;
        this.placeTarget = null;
        this.attackTarget = null;
        this.mineTarget = null;
        this.placedCrystals.clear();
        this.attackedCrystals.clear();
        this.countedCrystals.clear();
        LightningClient.RENDER_MANAGER.setRenderPosition(null);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((arg0.getPacket() instanceof ServerboundSetCarriedItemPacket)) {
            this.swapTimer.reset();
        }
        if ((!((arg0.getPacket() instanceof ServerboundUseItemPacket))) || (mc.player == null)) break label_4;
        if (mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            if (mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            }
            this.lastPearlThrowTime = System.currentTimeMillis();
            break label_4;
        }
        if (mc.player.getMainHandItem().getItem() != Items.EXPERIENCE_BOTTLE) {
            if (mc.player.getOffhandItem().getItem() != Items.EXPERIENCE_BOTTLE) break label_4;
        }
        this.lastXpThrowTime = System.currentTimeMillis();
        label_4:
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent arg0) {
        this.highestID = -100000;
    }

    public String getMetaData() {
        return (this.calculationTime + ", " + this.calculationCount + ", " + this.calculationDamage + ", " + this.crystalsPerSecond);
    }

    private void attackCrystals() {
        if ((isDead()) || (shouldPause("Attack"))) {
            return;
        }
        if (!(getPlayers().isEmpty())) break label_2;
        this.attackTarget = null;
        return;
        label_2:
        Object var2 = null;
        Object var3 = this.placeTarget;
        if (((var3 == null) || (var3.getPosition() != null)) || (var3.obstructions == null)) break label_3;
        int var4 = (!(var3.obstructions.isEmpty()) ? 1 : 0);
        if (!(!var4)) {
        } else {
        }
        Object var5 = mc.level.entitiesForRendering().iterator();
        label_7:
        if (!(var5.hasNext())) break label_11;
        Object var6 = ((Entity) var5.next());
        if (!((var6 instanceof EndCrystal))) break label_7;
        var tmp0 = ((EndCrystal) var6);
        Object var7 = tmp0;
        if (!(tmp0.isAlive())) break label_7;
        if (this.inhibit.getValue()) {
            if (this.attackedCrystals.containsKey(var6.getId())) break label_7;
        }
        if (!var4) {
            if (!(this.placedCrystals.containsKey(var7.blockPosition().below()))) break label_7;
        }
        if ((var7.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) || (!(mc.level.getWorldBorder().isWithinBounds(var7.blockPosition())))) break label_7;
        if (WorldUtils.canSee(var7)) break label_10;
        if (this.raytrace.getValue()) break label_7;
        if (var7.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= Mth.square(this.attackWallsRange.getValue().doubleValue())) break label_10;
        break label_7;
        label_10:
        var2 = var7;
        break label_11;
        label_11:
        var tmp1 = (var2 == null ? this.attackTarget : var2);
        Object var1 = tmp1;
        var5 = tmp1;
        if (var1 == null) return;
        if (this.swapDelay.getValue().intValue() > 0 && !(this.swapTimer.hasTimeElapsed((this.swapDelay.getValue().longValue() * 50L)))) return;
        if (this.attackTimer.hasTimeElapsed((1000.0f - (this.attackSpeed.getValue().floatValue() * 50.0f)))) {
            if (!(!this.attackedSequentially)) {
            }
            if (!(!this.attackedSequentially)) {
                this.attackedSequentially = false;
            }
            return;
        }
        var6 = mc.level.getEntity(var1.getId());
        var7 = null;
        if (var6 == null) {
            var7 = "entity-gone";
            break label_26;
        }
        if (!((var6 instanceof EndCrystal))) {
            var7 = "not-end-crystal";
            break label_26;
        }
        if (!(((EndCrystal) var6).isAlive())) {
            var7 = "dead";
            break label_26;
        }
        if (this.inhibit.getValue() && this.attackedCrystals.containsKey(var6.getId())) {
            var7 = "inhibit";
            break label_26;
        }
        if (var6.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) {
            var7 = "range";
            break label_26;
        }
        if (!(mc.level.getWorldBorder().isWithinBounds(var6.blockPosition()))) {
            var7 = "border";
            break label_26;
        }
        if (WorldUtils.canSee(var6)) break label_26;
        if (!(this.raytrace.getValue())) {
            if (var6.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= Mth.square(this.attackWallsRange.getValue().doubleValue())) break label_26;
        }
        var7 = "cannot-see";
        label_26:
        if (var7 != null) return;
        this.attackRunnable = () -> {
    if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
        LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(arg0.blockPosition())));
    }
    attack(arg0);
};
    }

    private void placeCrystals(boolean arg0) {
        if ((isDead()) || (shouldPause("Place"))) {
            return;
        }
        if (this.placeTarget != null) break label_2;
        break label_3;
        label_2:
        label_3:
        var tmp0 = this.placeTarget.clone();
        Object var2 = tmp0;
        Object var3 = tmp0;
        if (var2 != null) {
            if (var2.getPosition() == null) {
            }
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        if (var2.getPlayer() != null && var2.getPlayer().isAlive() && !(var2.getPlayer().isDeadOrDying())) {
            if (var2.getPlayer().getHealth() <= 0.0f) {
            }
            this.placeTarget = null;
            this.target = null;
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        int var4 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        int var5 = mc.player.getInventory().getSelectedSlot();
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None")) && var4 == -1 && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) return;
        Object var6 = var2.getPosition();
        if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var6)) > Mth.square(this.placeRange.getValue().doubleValue())) {
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        LightningClient.RENDER_MANAGER.setRenderPosition(var6);
        if (!(mc.level.getWorldBorder().isWithinBounds(var6))) return;
        if (mc.level.getBlockState(var6).getBlock() != Blocks.OBSIDIAN && mc.level.getBlockState(var6).getBlock() != Blocks.BEDROCK) return;
        if ((!(mc.level.getBlockState(var6.offset(0, 1, 0)).isAir())) || (this.placements.getValue().equalsIgnoreCase("Protocol") && !(mc.level.getBlockState(var6.offset(0, 2, 0)).isAir()))) {
            return;
        }
        if (!(WorldUtils.canSeeBlock(var6))) {
            if ((this.raytrace.getValue()) || (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var6)) > Mth.square(this.placeWallsRange.getValue().doubleValue()))) {
                return;
            }
        }
        IF (!(mc.level.getEntities(((Entity) null), new AABB(var6.offset(0, 1, 0)), (arg0) -> true).stream().anyMatch((arg0) -> {
    if ((!(arg0.isAlive())) || ((arg0 instanceof ExperienceOrb))) break label_0;
    return !((arg0 instanceof EndCrystal));
}))) break label_16;
        return;
        label_16:
        if (this.swapDelay.getValue().intValue() > 0 && !(this.swapTimer.hasTimeElapsed((this.swapDelay.getValue().longValue() * 50L)))) return;
        if (!(this.placeTimer.hasTimeElapsed((1000.0f - (this.placeSpeed.getValue().floatValue() * 50.0f))))) return;
        if (!arg0 && !(!this.placedSequentially)) {
            this.placedSequentially = false;
            return;
        }
        this.placeRunnable = () -> {
    int var5 = 0;
    Object var6 = CrystalPlacementHelper.getVisiblePlacement(arg0);
    if (var6 != null && var6.hitVec != null) {
        break label_1;
    }
    label_1:
    var tmp0 = Vec3.atCenterOf(arg0).add(0.0, 0.5, 0.0);
    Object var4 = tmp0;
    Object var7 = tmp0;
    if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
        LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(var4));
    }
    if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
            this.savedSlot = arg1;
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), arg2, arg1);
        var5 = 1;
    }
    place(arg0);
    if (!(!var5)) {
        InventoryUtils.switchBack(this.autoSwitch.getValue(), arg2, arg1);
    }
    if (this.godSync.getValue()) {
        if (this.antiKick.getValue()) {
            if (!(mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) && !(mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem) && !(((KeyActionModule) LightningClient.MODULE_MANAGER.getModule(KeyActionModule.class)).isXpActive())) {
            }
        } else {
        }
        int var8 = 0;
        int var9 = 0;
        if (this.antiKick.getValue()) {
            if (this.kickTicks <= this.kickThreshold.getValue().intValue()) break label_18;
        }
        if (!var8) break label_18;
        if (this.fast.getValue()) break label_12;
        Object var10 = mc.level.entitiesForRendering().iterator();
        while (var10.hasNext()) {
            Object var11 = ((Entity) var10.next());
            if (var11.getId() <= this.highestID) {
                break label_10;
            }
            this.highestID = var11.getId();
        }
        label_12:
        var10 = (1 - this.offset.getValue().intValue());
        while (var10 < this.predictions.getValue().intValue()) {
            var11 = mc.level.getEntity(this.highestID);
            if (var11 != null && !(var11 instanceof EndCrystal)) {
            } else {
                int var12 = (this.highestID + var10);
                mc.getConnection().send(new ServerboundAttackPacket(var12));
                if (this.godSwing.getValue().equals("Strict")) {
                    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
                this.attackedCrystals.put(var12, System.currentTimeMillis());
            }
            var10++;
        }
        if (!(this.godSwing.getValue().equals("Normal"))) break label_18;
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        label_18:
        this.kickTicks = (this.kickTicks + 1);
    }
};
        if (!(!arg0)) {
            this.placeRunnable.run();
            this.placeRunnable = null;
            this.placedSequentially = true;
        }
    }

    private EndCrystal calculateCrystals() {
        if (!(this.attack.getValue())) return null;
        if (shouldPause("Attack")) return null;
        Object var1 = getPlayers();
        if (var1.isEmpty()) return null;
        Object var2 = null;
        float var3 = 0.0f;
        Object var4 = mc.level.entitiesForRendering().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Entity) var4.next());
            if (!((var5 instanceof EndCrystal))) break label_3;
            var tmp0 = ((EndCrystal) var5);
            Object var7 = tmp0;
            if (!(tmp0.isAlive())) break label_3;
            if (this.inhibit.getValue()) {
                if (this.attackedCrystals.containsKey(var5.getId())) break label_3;
            }
            if ((var7.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) || (!(mc.level.getWorldBorder().isWithinBounds(var7.blockPosition())))) break label_3;
            if (!(WorldUtils.canSee(var7))) {
                if ((this.raytrace.getValue()) || (var7.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue()))) break label_3;
            }
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_6;
            var tmp1 = DamageUtils.getCrystalDamage(mc.player, null, var7, this.ignoreTerrain.getValue());
            float var6 = tmp1;
            if (tmp1 > this.maximumSelfDamage.getValue().floatValue()) break label_3;
            if ((!(this.antiSuicide.getValue())) || (var6 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_6;
            break label_3;
            label_6:
            int var8 = 0;
            Object var9 = var1.iterator();
            label_7:
            if (!(var9.hasNext())) break label_11;
            Object var10 = ((Player) var9.next());
            float var11 = DamageUtils.getCrystalDamage(var10, PositionUtils.extrapolate(var10, this.extrapolation.getValue().intValue()), var7, this.ignoreTerrain.getValue());
            if (var11 < getMinimumDamage(var10, this.minimumDamage.getValue().floatValue()) && var11 < (var10.getHealth() + var10.getAbsorptionAmount())) {
                if ((var11 * (1.0f + this.lethalMultiplier.getValue().floatValue())) < (var10.getHealth() + var10.getAbsorptionAmount())) break label_7;
            }
            if (var11 <= var3 && var11 <= (var10.getHealth() + var10.getAbsorptionAmount())) {
                break label_7;
            }
            var2 = var7;
            var3 = var11;
            if (var11 <= (var10.getHealth() + var10.getAbsorptionAmount())) {
                break label_7;
            }
            var8 = 1;
            break label_11;
            label_11:
            if (var8) break label_12;
        }
        label_12:
        return var2;
    }

    private ServerAutoCrystal.PlaceTarget calculatePlacements(BlockPos arg0) {
        if (!(this.place.getValue())) return null;
        if (shouldPause("Place")) break label_2;
        if (this.autoSwitch.getValue().equalsIgnoreCase("None")) break label_1;
        if (InventoryUtils.findHotbar(Items.END_CRYSTAL) != -1) break label_3;
        label_1:
        if ((mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL) || (mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL)) break label_3;
        label_2:
        return null;
        label_3:
        Object var2 = getPlayers();
        if (var2.isEmpty()) return null;
        Object var3 = null;
        Object var4 = null;
        Object var5 = new ArrayList();
        float var6 = 0.0f;
        if (this.placeTarget == null) {
        } else {
        }
        Object var7 = this.placeTarget.getPosition();
        int var8 = 0;
        int var9 = 0;
        while (var9 < LightningClient.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue()))) {
            Object var11 = mc.player.blockPosition().offset(LightningClient.WORLD_MANAGER.getOffset(var9));
            if ((mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var11)) > Mth.square(this.placeRange.getValue().doubleValue())) || (!(mc.level.getWorldBorder().isWithinBounds(var11)))) break label_21;
            if (mc.level.getBlockState(var11).getBlock() != Blocks.OBSIDIAN) {
                if (mc.level.getBlockState(var11).getBlock() != Blocks.BEDROCK) break label_21;
            }
            if (!(mc.level.getBlockState(var11.offset(0, 1, 0)).isAir())) break label_21;
            if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
                if (!(mc.level.getBlockState(var11.offset(0, 2, 0)).isAir())) break label_21;
            }
            if (!(WorldUtils.canSeeBlock(var11))) {
                if ((this.raytrace.getValue()) || (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var11)) > Mth.square(this.placeWallsRange.getValue().doubleValue()))) break label_21;
            }
            IF (!(mc.level.getEntities(((Entity) null), new AABB(var11.offset(0, 1, 0)), (arg0) -> true).stream().anyMatch((arg0) -> {
    if ((!(arg0.isAlive())) || ((arg0 instanceof ExperienceOrb))) break label_0;
    return !((arg0 instanceof EndCrystal));
}))) break label_11;
            break label_21;
            label_11:
            Object var12 = mc.level.getEntities(((Entity) null), new AABB(var11.offset(0, 1, 0)), (arg0) -> true).stream().filter(() -> {
    if (!((arg0 instanceof EndCrystal))) break label_1;
    var tmp0 = ((EndCrystal) arg0);
    Object var2 = tmp0;
    if (this.placedCrystals.containsKey(tmp0.blockPosition().below())) {
        if (var2.tickCount < ((20 - this.attackSpeed.getValue().intValue()) + 15)) break label_1;
    }
    break label_2;
    label_1:
    label_2:
    return false;
}).toList();
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_12;
            var tmp0 = DamageUtils.getCrystalDamage(mc.player, null, var11, arg0, this.ignoreTerrain.getValue());
            float var10 = tmp0;
            if (tmp0 > this.maximumSelfDamage.getValue().floatValue()) break label_21;
            if ((!(this.antiSuicide.getValue())) || (var10 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_12;
            break label_21;
            label_12:
            int var13 = 0;
            Object var14 = var2.iterator();
            label_13:
            if (!(var14.hasNext())) break label_20;
            Object var15 = ((Player) var14.next());
            var8++;
            float var16 = DamageUtils.getCrystalDamage(var15, PositionUtils.extrapolate(var15, this.extrapolation.getValue().intValue()), var11, arg0, this.ignoreTerrain.getValue());
            if (var16 < getMinimumDamage(var15, this.minimumDamage.getValue().floatValue()) && var16 < (var15.getHealth() + var15.getAbsorptionAmount()) && (var16 * (1.0f + this.lethalMultiplier.getValue().floatValue())) < (var15.getHealth() + var15.getAbsorptionAmount())) {
                break label_13;
            }
            if (arg0 == null && !(var12.isEmpty())) {
                var5.add(((Entity) var12.getFirst()));
                break label_20;
            }
            float var17 = (var16 + (var11.equals(var7) ? 0.5f : 0.0f));
            if (var17 <= var6 && var16 <= (var15.getHealth() + var15.getAbsorptionAmount())) {
                break label_13;
            }
            var3 = var11;
            var4 = var15;
            var6 = var17;
            if (var16 <= (var15.getHealth() + var15.getAbsorptionAmount())) {
                break label_13;
            }
            var13 = 1;
            break label_20;
            label_20:
            if (!var13) break label_21;
            break label_22;
            label_21:
            var9++;
        }
        label_22:
        if (var3 == null) return new ServerAutoCrystal.PlaceTarget(null, null, var5, null, 0.0f, var8);
        return new ServerAutoCrystal.PlaceTarget(var3, var4, var5, arg0, var6, var8);
    }

    private float[] calculateRotations(Vec3 arg0) {
        Object var2 = RotationUtils.getRotations(arg0);
        if (this.yawStep.getValue()) {
            float var3 = (LightningClient.ROTATION_MANAGER.getServerYaw() - var2[0]);
            if (Math.abs(var3) > 180.0f) {
                var3 = (var3 + (var3 > 0.0f ? -360.0f : 360.0f));
            }
            float var4 = (((float) (var3 > 0.0f ? -1 : 1)) * this.yawStepThreshold.getValue().floatValue());
            if (Math.abs(var3) > this.yawStepThreshold.getValue().floatValue()) {
            } else {
            }
            float var5 = var2[0];
            var2[0] = var5;
        }
        return var2;
    }

    private boolean isEnemyAtMinePos(BlockPos arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return false;
        }
        Object var2 = new AABB(arg0).inflate(1.5, 2.0, 1.5);
        Object var3 = mc.level.players().iterator();
        label_2:
        if (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if (((var4.equals(mc.player)) || (EntityUtils.isGhost(var4))) || (!(var4.isAlive()))) break label_2;
            if (LightningClient.FRIEND_MANAGER != null) {
                if (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString())) break label_2;
            }
            if (!(var4.getBoundingBox().intersects(var2))) {
                break label_2;
            }
            return true;
        }
        return false;
    }

    private boolean isCrystalNearMinePos(EndCrystal arg0, BlockPos arg1) {
        if ((arg0 == null) || (arg1 == null)) {
            return false;
        }
        Object var3 = arg0.blockPosition().below();
        if (Math.abs((var3.getX() - arg1.getX())) <= 2 && Math.abs((var3.getZ() - arg1.getZ())) <= 2 && Math.abs((var3.getY() - arg1.getY())) <= 2) return true;
        return arg0.getBoundingBox().distanceToSqr(Vec3.atCenterOf(arg1)) <= 9.0;
    }

    private boolean isAtActiveUnbrokenMinePos(EndCrystal arg0) {
        if (((arg0 == null) || (mc.level == null)) || (mc.player == null)) {
            return false;
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var4 = null;
        Object var5 = null;
        if ((var4 == null) || (!(var4.isToggled()))) {
            return false;
        }
        Object var6 = var4.getPrimary();
        if ((var6 == null) || (var6.getPosition() == null)) break label_9;
        var tmp0 = var6.getPosition();
        Object var3 = tmp0;
        if (!(isCrystalNearMinePos(arg0, tmp0))) break label_9;
        if (this.attack.getValue()) {
            if (!(isEnemyAtMinePos(var3))) {
            }
        } else {
        }
        return false;
        label_9:
        Object var7 = var4.getSecondary();
        if ((var7 == null) || (var7.getPosition() == null)) break label_13;
        var tmp1 = var7.getPosition();
        Object var2 = tmp1;
        if (!(isCrystalNearMinePos(arg0, tmp1))) break label_13;
        if (this.attack.getValue()) {
            if (!(isEnemyAtMinePos(var2))) {
            }
        } else {
        }
        return false;
        label_13:
        return false;
    }

    private void attack(EndCrystal arg0) {
        if (((arg0 == null) || (!(arg0.isAlive()))) || (arg0.isRemoved())) {
            return;
        }
        if (isAtActiveUnbrokenMinePos(arg0)) return;
        if (!(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            float var3 = DamageUtils.getCrystalDamage(mc.player, null, arg0, this.ignoreTerrain.getValue());
            if (var3 > this.maximumSelfDamage.getValue().floatValue()) return;
            if (this.antiSuicide.getValue() && var3 > (mc.player.getHealth() + mc.player.getAbsorptionAmount())) return;
        }
        var3 = mc.player.getInventory().getSelectedSlot();
        int var4 = -1;
        if ((this.antiWeakness.getValue().equalsIgnoreCase("None")) || (!(mc.player.hasEffect(MobEffects.WEAKNESS)))) break label_5;
        var tmp0 = InventoryUtils.findBestSword(InventoryUtils.HOTBAR_START, InventoryUtils.HOTBAR_END);
        int var2 = tmp0;
        if (tmp0 == -1) break label_5;
        InventoryUtils.switchSlot(this.antiWeakness.getValue(), var2, var3);
        var4 = var2;
        label_5:
        mc.getConnection().send(new ServerboundAttackPacket(arg0.getId()));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (var4 != -1) {
            InventoryUtils.switchBack(this.antiWeakness.getValue(), var4, var3);
        }
        this.attackedCrystals.put(arg0.getId(), System.currentTimeMillis());
        this.attackTimer.reset();
        this.totalAttacks = (this.totalAttacks + 1L);
    }

    private void place(BlockPos arg0) {
        Object var2 = (mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        Object var3 = CrystalPlacementHelper.getVisiblePlacement(arg0);
        NetworkUtils.sendSequencedPacket((arg3) -> new ServerboundUseItemOnPacket(arg0, new BlockHitResult(arg1.hitVec, arg1.direction, arg2, false), arg3));
        Object var4 = this.swing.getValue();
        int var5 = -1;
        switch (var4.hashCode()) {
            case -1911998296 -> { break label_3; }
            case -1085510111 -> { break label_2; }
            case 2076577 -> { break label_6; }
            case 56796616 -> { break label_4; }
            case 115914302 -> { break label_5; }
            default -> { break label_7; }
        }
        label_2:
        if (!(var4.equals("Default"))) break label_7;
        var5 = 0;
        break label_7;
        label_3:
        if (!(var4.equals("Packet"))) break label_7;
        var5 = 1;
        break label_7;
        label_4:
        if (!(var4.equals("Mainhand"))) break label_7;
        var5 = 2;
        break label_7;
        label_5:
        if (!(var4.equals("Offhand"))) break label_7;
        var5 = 3;
        break label_7;
        label_6:
        if (!(var4.equals("Both"))) break label_7;
        var5 = 4;
        label_7:
        switch (var5) {
            case 0 -> { break label_8; }
            case 1 -> { break label_9; }
            case 2 -> { break label_10; }
            case 3 -> { break label_11; }
            case 4 -> { break label_12; }
            default -> { break label_13; }
        }
        label_8:
        mc.player.swing(var2);
        break label_13;
        label_9:
        mc.getConnection().send(new ServerboundSwingPacket(var2));
        break label_13;
        label_10:
        mc.player.swing(InteractionHand.MAIN_HAND);
        break label_13;
        label_11:
        mc.player.swing(InteractionHand.OFF_HAND);
        break label_13;
        label_12:
        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.OFF_HAND);
        label_13:
        this.placedCrystals.put(arg0, System.currentTimeMillis());
        this.countedCrystals.put(arg0, System.currentTimeMillis());
        this.placeTimer.reset();
        this.totalPlaces = (this.totalPlaces + 1L);
    }

    private List getPlayers() {
        Object var1 = new ArrayList();
        if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) {
            var1.add(mc.player);
            return var1;
        }
        Object var2 = mc.level.players().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Player) var2.next());
            if ((((var3 == mc.player) || (EntityUtils.isGhost(var3))) || (!(var3.isAlive()))) || (mc.player.distanceToSqr(var3) > Mth.square(this.enemyRange.getValue().doubleValue()))) break label_1;
            if (LightningClient.FRIEND_MANAGER.contains(var3.getName().getString())) {
                break label_1;
            }
            if (var3.getMainHandItem().getItem() != Items.END_CRYSTAL) {
                if (var3.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                }
            } else {
            }
            int var4 = 0;
            int var5 = 0;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(var3) && !var4) {
                break label_1;
            }
            var1.add(var3);
        }
        return var1;
    }

    private boolean isPearlPaused() {
        if (!(this.pauseOnPearl.getValue())) return false;
        if (LightningClient.MODULE_MANAGER == null) break label_1;
        break label_2;
        label_1:
        label_2:
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled()) return true;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var1 = null;
        Object var4 = null;
        if (var1 != null && var1.isPearlActive()) return true;
        if ((System.currentTimeMillis() - this.lastPearlThrowTime) < 250L) return true;
        if (mc.player == null) break label_12;
        if (mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            if (mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            }
        } else {
        }
        int var5 = 0;
        int var6 = 0;
        if (!var5) break label_12;
        if ((mc.options.keyUse.isDown()) || (mc.player.isUsingItem())) {
            return true;
        }
        label_12:
        return false;
    }

    private boolean isXpPaused() {
        if (!(this.pauseOnXP.getValue())) return false;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var1 = null;
        Object var2 = null;
        if (var1 != null && var1.isXpActive()) return true;
        if ((System.currentTimeMillis() - this.lastXpThrowTime) < 250L) return true;
        if (mc.player == null) break label_9;
        if (mc.player.getMainHandItem().getItem() != Items.EXPERIENCE_BOTTLE) {
            if (mc.player.getOffhandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
            }
        } else {
        }
        int var3 = 0;
        int var4 = 0;
        if (!var3) break label_9;
        if ((mc.options.keyUse.isDown()) || (mc.player.isUsingItem())) {
            return true;
        }
        label_9:
        return false;
    }

    private boolean shouldPause(String arg0) {
        if (isPearlPaused()) return true;
        if (isXpPaused()) return true;
        if (this.whileEating.getValue().equalsIgnoreCase("None")) break label_3;
        if (arg0.equalsIgnoreCase("Attack")) {
            if (this.whileEating.getValue().equalsIgnoreCase("Place")) break label_3;
        }
        if (arg0.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack")) {
            label_3:
        } else {
        }
        int var2 = 0;
        if (!var2) break label_6;
        return mc.player.isUsingItem();
    }

    private float getMinimumDamage(Player arg0, float arg1) {
        if (arg0 == null) return arg1;
        if (!(this.chestBreak.getValue())) break label_1;
        IF (!(mc.level.getEntities(((Entity) null), new AABB(arg0.blockPosition()).inflate(1.0), (arg0) -> true).stream().anyMatch(() -> {
    if (!((arg0 instanceof ItemEntity))) break label_0;
    var tmp0 = ((ItemEntity) arg0);
    Object var2 = tmp0;
    if ((tmp0.getItem().getItem() != Items.OBSIDIAN) || (var2.getItem().getCount() < 8)) break label_0;
    return var2.tickCount <= ((2 + LightningClient.SERVER_MANAGER.getPingDelay()) + (20 - this.placeSpeed.getValue().intValue()));
}))) break label_1;
        IF (mc.level.getEntities(((Entity) null), new AABB(mc.player.blockPosition()).inflate(1.0), (arg0) -> true).stream().anyMatch(() -> {
    if (!((arg0 instanceof ItemEntity))) break label_0;
    var tmp0 = ((ItemEntity) arg0);
    Object var2 = tmp0;
    if ((tmp0.getItem().getItem() != Items.OBSIDIAN) || (var2.getItem().getCount() < 8)) break label_0;
    return var2.tickCount <= ((2 + LightningClient.SERVER_MANAGER.getPingDelay()) + (20 - this.placeSpeed.getValue().intValue()));
})) break label_1;
        return 2.0f;
        label_1:
        if (this.facePlaceMode.getValue().equalsIgnoreCase("None")) return arg1;
        if (!(this.facePlaceSpeed.getValue().equalsIgnoreCase("Normal"))) {
            if (!(this.facePlaceTimer.hasTimeElapsed((this.facePlaceDelay.getValue().longValue() * 50L)))) break label_9;
        }
        if (this.facePlaceMode.getValue().equalsIgnoreCase("Always")) return Math.min(arg1, 2.0f);
        if (this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic") && this.healthPlace.getValue() && (arg0.getHealth() + arg0.getAbsorptionAmount()) <= this.health.getValue().floatValue()) return Math.min(arg1, 2.0f);
        if ((!(this.facePlaceMode.getValue().equalsIgnoreCase("Dynamic"))) || (!(this.armorPlace.getValue()))) break label_9;
        var tmp0 = new EquipmentSlot[4];
        tmp0[0] = EquipmentSlot.FEET;
        tmp0[1] = EquipmentSlot.LEGS;
        tmp0[2] = EquipmentSlot.CHEST;
        tmp0[3] = EquipmentSlot.HEAD;
        Object var3 = tmp0;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Object var6 = var3[var5];
            Object var7 = arg0.getItemBySlot(var6);
            if ((var7.isEmpty()) || (var7.get(DataComponents.EQUIPPABLE) == null)) break label_8;
            if (Math.round(((((double) (var7.getMaxDamage() - var7.getDamageValue())) * 100.0) / ((double) var7.getMaxDamage()))) > ((long) this.percentage.getValue().intValue())) {
                break label_8;
            }
            return Math.min(arg1, 2.0f);
            label_8:
            var5++;
        }
        label_9:
        return arg1;
    }

    @Generated
    public Player getTarget() {
        return this.target;
    }

    @Generated
    public String getCalculationDamage() {
        return this.calculationDamage;
    }


    public class PlaceTarget {
        private BlockPos position;
        private Player player;
        private List obstructions;
        private BlockPos exception;
        private float damage;
        private int calculations;

        public ServerAutoCrystal.PlaceTarget clone() {
            return new ServerAutoCrystal.PlaceTarget(this.position, this.player, this.obstructions, this.exception, this.damage, this.calculations);
        }

        @Generated
        public BlockPos getPosition() {
            return this.position;
        }

        @Generated
        public Player getPlayer() {
            return this.player;
        }

        @Generated
        public List getObstructions() {
            return this.obstructions;
        }

        @Generated
        public BlockPos getException() {
            return this.exception;
        }

        @Generated
        public float getDamage() {
            return this.damage;
        }

        @Generated
        public int getCalculations() {
            return this.calculations;
        }

        @Generated
        public PlaceTarget(BlockPos arg0, Player arg1, List arg2, BlockPos arg3, float arg4, int arg5) {
            this.position = arg0;
            this.player = arg1;
            this.obstructions = arg2;
            this.exception = arg3;
            this.damage = arg4;
            this.calculations = arg5;
        }

    }
}
