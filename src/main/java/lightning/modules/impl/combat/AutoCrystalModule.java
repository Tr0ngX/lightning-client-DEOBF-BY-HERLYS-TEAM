package lightning.modules.impl.combat;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.DestroyBlockEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.GameLoopEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.movement.PhaseModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.modules.impl.player.KeyActionModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.EspShader;
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
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AutoCrystal", description = "Automatically places and attacks crystals to annihilate your opponents.", category = Module.Category.COMBAT)
public class AutoCrystalModule extends Module {
    public CategorySetting attackCategory;
    public BooleanSetting attack;
    public NumberSetting attackSpeed;
    public NumberSetting attackRange;
    public NumberSetting attackWallsRange;
    public ModeSetting antiWeakness;
    public BooleanSetting instant;
    public BooleanSetting inhibit;
    public CategorySetting placeCategory;
    public BooleanSetting place;
    public NumberSetting placeSpeed;
    public NumberSetting placeRange;
    public NumberSetting placeWallsRange;
    public ModeSetting placements;
    public BooleanSetting blockDestruction;
    public ModeSetting autoSwitch;
    public BooleanSetting swapBack;
    public NumberSetting swapDelay;
    public CategorySetting miscellaneousCategory;
    public ModeSetting sequential;
    public ModeSetting rotate;
    public ModeSetting swing;
    public BooleanSetting yawStep;
    public NumberSetting yawStepThreshold;
    public BooleanSetting raytrace;
    public BooleanSetting losDebug;
    public ModeSetting targetMode;
    public NumberSetting extrapolation;
    public NumberSetting enemyRange;
    public BooleanSetting ignoreNaked;
    public BooleanSetting chestBreak;
    public BooleanSetting mineIgnore;
    public NumberSetting mineIgnoreTicks;
    public BooleanSetting asynchronous;
    public BooleanSetting pauseOnSecondaryMine;
    public BooleanSetting awaitMine;
    public NumberSetting awaitMineTicks;
    public BooleanSetting pauseOnPearl;
    public BooleanSetting pauseOnXP;
    public BooleanSetting gameLoop;
    public NumberSetting loopDelay;
    public ModeSetting whileEating;
    public CategorySetting predictionCategory;
    public BooleanSetting godSync;
    public NumberSetting predictions;
    public NumberSetting offset;
    public ModeSetting godSwing;
    public BooleanSetting fast;
    public BooleanSetting antiKick;
    public NumberSetting kickThreshold;
    public CategorySetting facePlaceCategory;
    public ModeSetting facePlaceMode;
    public ModeSetting facePlaceSpeed;
    public NumberSetting facePlaceDelay;
    public BooleanSetting healthPlace;
    public NumberSetting health;
    public BooleanSetting armorPlace;
    public NumberSetting percentage;
    public CategorySetting basePlaceCategory;
    public BooleanSetting basePlace;
    public ModeSetting basePlaceSwitch;
    public BooleanSetting basePlaceAirPlace;
    public BooleanSetting basePlaceStrictDirection;
    public NumberSetting basePlaceMinDamage;
    public NumberSetting basePlaceRatio;
    public NumberSetting basePlaceRadius;
    public NumberSetting basePlaceRange;
    public NumberSetting basePlaceDelay;
    public BooleanSetting basePlaceRotate;
    public CategorySetting damageCategory;
    public NumberSetting minimumDamage;
    public NumberSetting maximumSelfDamage;
    public NumberSetting lethalMultiplier;
    public BooleanSetting antiSuicide;
    public BooleanSetting ignoreTerrain;
    public CategorySetting renderCategory;
    public ModeSetting animationMode;
    public ModeSetting mode;
    public NumberSetting duration;
    public NumberSetting slideSmoothness;
    public ModeSetting renderMode;
    public ColorSetting fillColorUp;
    public ColorSetting fillColorDown;
    public ColorSetting outlineColorUp;
    public ColorSetting outlineColorDown;
    public BooleanSetting renderDamage;
    public BooleanSetting icon;
    public NumberSetting iconScale;
    public NumberSetting iconRadius;
    public ColorSetting iconColor;
    private static final String[] SHADER_MODES;
    private static final String[] SHADER_ACTIVE_MODES;
    public ModeSetting shader;
    public NumberSetting shaderSpeed;
    public NumberSetting shaderOpacity;
    public BooleanSetting shaderDistanceScaling;
    public NumberSetting shaderStep;
    public ColorSetting shaderColor1;
    public ColorSetting shaderColor2;
    public ColorSetting shaderColor3;
    public ColorSetting shaderColor4;
    public ColorSetting shaderGlowColor;
    private final ExecutorService executor;
    private volatile Future pendingCalc;
    private volatile boolean asyncLoopActive;
    private volatile Set latestReservedPlacements;
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
    private final Timer losDebugTimer;
    private int lastSelectedSlot;
    private long totalPlaces;
    private long totalAttacks;
    private boolean sequenceAttack;
    private boolean sequencePlace;
    private boolean attackedSequentially;
    private boolean placedSequentially;
    private Player target;
    private EndCrystal attackTarget;
    private AutoCrystalModule.PlaceTarget placeTarget;
    private AutoCrystalModule.PlaceTarget mineTarget;
    private AutoCrystalModule.PlaceTarget mineTargetSecondary;
    private BlockPos mineIgnoreMinedPos;
    private BlockPos mineIgnorePlacedPos;
    private String calculationTime;
    private int calculationCount;
    private String calculationDamage;
    private final Counter crystalCounter;
    private int crystalsPerSecond;
    private int highestID;
    private int kickTicks;
    private volatile List cachedPlayers;
    private volatile long lastPlayerCacheTime;
    private static final long PLAYER_CACHE_DURATION = 50L;
    private int basePlaceTicks;
    private int savedSlot;
    private static final float SECONDARY_PAUSE_PROGRESS = 0.85f;
    private long lastPearlThrowTime;
    private long lastXpThrowTime;

    public AutoCrystalModule() {
        this.attackCategory = new CategorySetting("Attack", "The category for settings related to attacking crystals.");
        this.attack = new BooleanSetting("Attack", "Enabled", "Automatically attacks crystals that are deemed safe.", new CategorySetting.Visibility(this.attackCategory), true);
        this.attackSpeed = new NumberSetting("AttackSpeed", "Speed", "The speed at which crystals will be attacked.", new CategorySetting.Visibility(this.attackCategory), 20.0f, 0.1f, 20.0f);
        this.attackRange = new NumberSetting("AttackRange", "Range", "The maximum distance at which crystals will be attacked.", new CategorySetting.Visibility(this.attackCategory), 4.5, 0.0, 8.0);
        this.attackWallsRange = new NumberSetting("AttackWallsRange", "WallsRange", "The maximum distance at which crystals will be attacked through walls.", new CategorySetting.Visibility(this.attackCategory), 4.5, 0.0, 8.0);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Silent";
        this.antiWeakness = new ModeSetting("AntiWeakness", "Allows you to attack crystals when weaknessed.", new CategorySetting.Visibility(this.attackCategory), "None", tmp0);
        this.instant = new BooleanSetting("Instant", "Instantly attacks crystals once they spawn.", new CategorySetting.Visibility(this.attackCategory), true);
        this.inhibit = new BooleanSetting("Inhibit", "Prevents excessive attacks on crystals by blacklisting crystals when attacking them.", new CategorySetting.Visibility(this.attackCategory), true);
        this.placeCategory = new CategorySetting("Place", "The category for settings related to placing crystals.");
        this.place = new BooleanSetting("Place", "Enabled", "Automatically places crystals on positions that are deemed safe and lethal enough.", new CategorySetting.Visibility(this.placeCategory), true);
        this.placeSpeed = new NumberSetting("PlaceSpeed", "Speed", "The speed at which crystals will be placed.", new CategorySetting.Visibility(this.placeCategory), 10.0f, 0.1f, 20.0f);
        this.placeRange = new NumberSetting("PlaceRange", "Range", "The maximum distance at which positions will be placed on.", new CategorySetting.Visibility(this.placeCategory), 4.5, 0.0, 8.0);
        this.placeWallsRange = new NumberSetting("PlaceWallsRange", "WallsRange", "The maximum distance at which positions will be placed on through walls.", new CategorySetting.Visibility(this.placeCategory), 4.5, 0.0, 8.0);
        var tmp1 = new String[2];
        tmp1[0] = "Native";
        tmp1[1] = "Protocol";
        this.placements = new ModeSetting("Placements", "The version of the game that will be used for crystal placement calculations.", new CategorySetting.Visibility(this.placeCategory), "Native", tmp1);
        this.blockDestruction = new BooleanSetting("BlockDestruction", "Places crystals on top of mined blocks in order to damage opponents.", new CategorySetting.Visibility(this.placeCategory), true);
        var tmp2 = new String[4];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        tmp2[2] = "Silent";
        tmp2[3] = "AltSwap";
        this.autoSwitch = new ModeSetting("Switch", "Automatically switches to a crystal if you aren't currently holding one.", new CategorySetting.Visibility(this.placeCategory), "None", tmp2);
        var tmp3 = new String[1];
        tmp3[0] = "Normal";
        this.swapBack = new BooleanSetting("SwapBack", "Switches back to the item you were holding before the module started switching to crystals, once the module is disabled.", new ModeSetting.Visibility(this.autoSwitch, tmp3), false);
        this.swapDelay = new NumberSetting("SwapDelay", "Delay", "The delay in ticks after swapping before placing or attacking crystals.", new CategorySetting.Visibility(this.placeCategory), 0, 0, 20);
        this.miscellaneousCategory = new CategorySetting("Miscellaneous", "The category for all miscellaneous settings.");
        var tmp4 = new String[3];
        tmp4[0] = "None";
        tmp4[1] = "Strict";
        tmp4[2] = "Strong";
        this.sequential = new ModeSetting("Sequential", "The sequence that the module's processes will be run in.", new CategorySetting.Visibility(this.miscellaneousCategory), "Strong", tmp4);
        var tmp5 = new String[4];
        tmp5[0] = "None";
        tmp5[1] = "Normal";
        tmp5[2] = "Packet";
        tmp5[3] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Automatically rotates to the crystal whenever attacking or placing.", new CategorySetting.Visibility(this.miscellaneousCategory), "Normal", tmp5);
        var tmp6 = new String[6];
        tmp6[0] = "Default";
        tmp6[1] = "None";
        tmp6[2] = "Packet";
        tmp6[3] = "Mainhand";
        tmp6[4] = "Offhand";
        tmp6[5] = "Both";
        this.swing = new ModeSetting("Swing", "The hand that will be used for swinging.", new CategorySetting.Visibility(this.miscellaneousCategory), "Default", tmp6);
        this.yawStep = new BooleanSetting("YawStep", "Performs your rotations over multiple ticks.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.yawStepThreshold = new NumberSetting("YawStepThreshold", "Threshold", "The threshold in order for yaw to be modified.", new BooleanSetting.Visibility(this.yawStep, true), 75, 1, 180);
        this.raytrace = new BooleanSetting("Raytrace", "Avoids attacking or placing any crystals through walls.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.losDebug = new BooleanSetting("LosDebug", "Debug", "Prints once a second why the block you are aiming at passes/fails the place gates (LOS steps + damage).", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        var tmp7 = new String[4];
        tmp7[0] = "All";
        tmp7[1] = "Nearest";
        tmp7[2] = "Farthest";
        tmp7[3] = "Health";
        this.targetMode = new ModeSetting("Target", "Which player to target when multiple are in range -- narrowing this to one collapses the per-position player scan from O(players) to O(1), the actual fix for multitarget FPS drops.", new CategorySetting.Visibility(this.miscellaneousCategory), "All", tmp7);
        this.extrapolation = new NumberSetting("Extrapolation", "Extrapolates the target's position to calculate positions ahead of time.", new CategorySetting.Visibility(this.miscellaneousCategory), 0, 0, 20);
        this.enemyRange = new NumberSetting("EnemyRange", "The maximum distance at which enemies can be at.", new CategorySetting.Visibility(this.miscellaneousCategory), 10.0, 0.0, 24.0);
        this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignores naked players (even if wearing only elytra), but still targets them if they are holding an End Crystal.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.chestBreak = new BooleanSetting("ChestBreak", "Prevents other players from getting obsidian from ender chests by destroying the dropped items.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.mineIgnore = new BooleanSetting("MineIgnore", "Pre-places a crystal on the block SpeedMine is about to break, and detonates it the instant that block is gone.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.mineIgnoreTicks = new NumberSetting("MineIgnoreTicks", "Tick", "How many ticks before the block breaks to place the crystal.", new BooleanSetting.Visibility(this.mineIgnore, true), 3, 0, 10);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
        this.pauseOnSecondaryMine = new BooleanSetting("PauseOnSecondaryMine", "Pauses AutoCrystal for one tick while SpeedMine's secondary block is about to break, so it finishes first.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
        this.awaitMine = new BooleanSetting("AwaitMine", "Pauses AutoCrystal when AutoMine selects a target block so AutoMine can lock and mine properly.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.awaitMineTicks = new NumberSetting("AwaitMineTicks", "Ticks", "The number of ticks to wait for AutoMine to lock target.", new BooleanSetting.Visibility(this.awaitMine, true), 2, 1, 10);
        this.pauseOnPearl = new BooleanSetting("PauseOnPearl", "Pauses AutoCrystal when Phase is active, KeyAction pearl is used, or when throwing an ender pearl manually.", new CategorySetting.Visibility(this.miscellaneousCategory), true);
        this.pauseOnXP = new BooleanSetting("PauseOnXP", "Pauses AutoCrystal when throwing experience bottles manually or when KeyAction XP is active.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.gameLoop = new BooleanSetting("GameLoop", "Runs the module on loop instead of ticks.", new CategorySetting.Visibility(this.miscellaneousCategory), false);
        this.loopDelay = new NumberSetting("LoopDelay", "The delay that has to be waited out before running the module again.", new BooleanSetting.Visibility(this.gameLoop, true), 50, 0, 1000);
        var tmp8 = new String[4];
        tmp8[0] = "None";
        tmp8[1] = "Attack";
        tmp8[2] = "Place";
        tmp8[3] = "Both";
        this.whileEating = new ModeSetting("WhileEating", "Places and attacks crystal while eating or using items.", new CategorySetting.Visibility(this.miscellaneousCategory), "Both", tmp8);
        this.predictionCategory = new CategorySetting("Prediction", "The category for settings related to attack prediction.");
        this.godSync = new BooleanSetting("GodSync", "Makes the attacking way faster by predicting entity IDs.", new CategorySetting.Visibility(this.predictionCategory), false);
        this.predictions = new NumberSetting("Predictions", "The amount of predictions that will be done after placing.", new CategorySetting.Visibility(this.predictionCategory), 10, 1, 20);
        this.offset = new NumberSetting("Offset", "The amount that the last entity ID should be offset by.", new CategorySetting.Visibility(this.predictionCategory), 0, 0, 2);
        var tmp9 = new String[3];
        tmp9[0] = "None";
        tmp9[1] = "Normal";
        tmp9[2] = "Strict";
        this.godSwing = new ModeSetting("GodSwing", "Swing", "The swinging that will be done for each predicted attack.", new CategorySetting.Visibility(this.predictionCategory), "Normal", tmp9);
        this.fast = new BooleanSetting("Fast", "Improves the speed of the prediction calculations at the cost of stability.", new CategorySetting.Visibility(this.predictionCategory), false);
        this.antiKick = new BooleanSetting("AntiKick", "Prevents you from getting kicked by attacking invalid entity IDs.", new CategorySetting.Visibility(this.predictionCategory), false);
        this.kickThreshold = new NumberSetting("KickThreshold", "Threshold", "The tick threshold for the kick prevention.", new BooleanSetting.Visibility(this.antiKick, true), 5, 1, 10);
        this.facePlaceCategory = new CategorySetting("Faceplace", "The category for settings relating to faceplacing.");
        var tmp10 = new String[3];
        tmp10[0] = "None";
        tmp10[1] = "Dynamic";
        tmp10[2] = "Always";
        this.facePlaceMode = new ModeSetting("FaceplaceMode", "Mode", "The checks that will be done in order to faceplace.", new CategorySetting.Visibility(this.facePlaceCategory), "Dynamic", tmp10);
        var tmp11 = new String[2];
        tmp11[0] = "Normal";
        tmp11[1] = "Custom";
        this.facePlaceSpeed = new ModeSetting("FaceplaceSpeed", "Speed", "The speed that players will be faceplaced at.", new CategorySetting.Visibility(this.facePlaceCategory), "Normal", tmp11);
        var tmp12 = new String[1];
        tmp12[0] = "Custom";
        this.facePlaceDelay = new NumberSetting("FaceplaceDelay", "Delay", "The ticks that have to be waited for before faceplacing again.", new ModeSetting.Visibility(this.facePlaceSpeed, tmp12), 11, 0, 20);
        var tmp13 = new String[1];
        tmp13[0] = "Dynamic";
        this.healthPlace = new BooleanSetting("HealthPlace", "Whether or not to faceplace when the target's health is low.", new ModeSetting.Visibility(this.facePlaceMode, tmp13), true);
        this.health = new NumberSetting("Health", "The health that the target needs to be at in order for the module to start faceplacing.", new BooleanSetting.Visibility(this.healthPlace, true), 8.0f, 0.0f, 36.0f);
        var tmp14 = new String[1];
        tmp14[0] = "Dynamic";
        this.armorPlace = new BooleanSetting("ArmorPlace", "Whether or not to faceplace when the target's armor is low on durability.", new ModeSetting.Visibility(this.facePlaceMode, tmp14), true);
        this.percentage = new NumberSetting("Percentage", "The percentage that one of the target's armor pieces need to be at in order to start faceplacing.", new BooleanSetting.Visibility(this.armorPlace, true), 10, 1, 100);
        this.basePlaceCategory = new CategorySetting("BasePlace", "The category for settings related to placing base obsidian blocks for elevated crystal attacks.");
        this.basePlace = new BooleanSetting("BasePlace", "Enabled", "Automatically places obsidian base blocks under/next to targets.", new CategorySetting.Visibility(this.basePlaceCategory), true);
        var tmp15 = new String[4];
        tmp15[0] = "None";
        tmp15[1] = "Normal";
        tmp15[2] = "Silent";
        tmp15[3] = "AltSwap";
        this.basePlaceSwitch = new ModeSetting("BaseSwitch", "Switch", "Switch mode for placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), "Silent", tmp15);
        this.basePlaceAirPlace = new BooleanSetting("BaseAirPlace", "AirPlace", "Allows placing base blocks in the air without neighboring blocks.", new CategorySetting.Visibility(this.basePlaceCategory), false);
        this.basePlaceStrictDirection = new BooleanSetting("BaseStrictDirection", "StrictDirection", "Only places using directions that face you.", new CategorySetting.Visibility(this.basePlaceCategory), false);
        this.basePlaceMinDamage = new NumberSetting("BaseDamage", "Min Damage", "The minimum damage a crystal on the base must deal to the target.", new CategorySetting.Visibility(this.basePlaceCategory), 6.0, 0.0, 36.0);
        this.basePlaceRatio = new NumberSetting("BaseRatio", "Ratio", "Damage ratio required over existing standard crystal placement to trigger baseplace.", new CategorySetting.Visibility(this.basePlaceCategory), 1.5, 1.0, 3.0);
        this.basePlaceRadius = new NumberSetting("BaseRadius", "Scan Radius", "The horizontal radius around the target to scan for base placements.", new CategorySetting.Visibility(this.basePlaceCategory), 3.0, 1.0, 6.0);
        this.basePlaceRange = new NumberSetting("BaseRange", "Range", "The maximum distance at which base blocks will be placed.", new CategorySetting.Visibility(this.basePlaceCategory), 4.5, 0.0, 6.0);
        this.basePlaceDelay = new NumberSetting("BaseDelay", "Delay", "Delay in ticks between placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), 0, 0, 10);
        this.basePlaceRotate = new BooleanSetting("BaseRotate", "Rotate", "Rotates when placing base blocks.", new CategorySetting.Visibility(this.basePlaceCategory), false);
        this.damageCategory = new CategorySetting("Damage", "The category for settings related to damage calculations.");
        this.minimumDamage = new NumberSetting("MinimumDamage", "Minimum", "The minimum damage that has to be dealt to enemies.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
        this.maximumSelfDamage = new NumberSetting("MaximumSelfDamage", "MaximumSelf", "The maximum damage that can be dealt to you by crystals.", new CategorySetting.Visibility(this.damageCategory), 10.0, 0.0, 36.0);
        this.lethalMultiplier = new NumberSetting("LethalMultiplier", "The amount of crystals that the target has to be killed by in order to ignore minimum damage.", new CategorySetting.Visibility(this.damageCategory), 1.5f, 0.0f, 4.0f);
        this.antiSuicide = new BooleanSetting("AntiSuicide", "Prevents crystals from accidentally killing you when you're low on health.", new CategorySetting.Visibility(this.damageCategory), true);
        this.ignoreTerrain = new BooleanSetting("IgnoreTerrain", "Ignores terrain that can be destroyed when calculating damage.", new CategorySetting.Visibility(this.damageCategory), true);
        this.renderCategory = new CategorySetting("Render", "Contains all of the settings relating to position rendering.");
        var tmp16 = new String[2];
        tmp16[0] = "Static";
        tmp16[1] = "Slide";
        this.animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", new CategorySetting.Visibility(this.renderCategory), "Static", tmp16);
        var tmp17 = new String[2];
        tmp17[0] = "Fade";
        tmp17[1] = "Shrink";
        this.mode = new ModeSetting("Mode", "The mode for the auto crystal render.", new CategorySetting.Visibility(this.renderCategory), "Fade", tmp17);
        this.duration = new NumberSetting("Duration", "The duration for the place render.", new CategorySetting.Visibility(this.renderCategory), 300, 0, 1000);
        this.slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while place position is changing.", new CategorySetting.Visibility(this.renderCategory), 1, 0, 20);
        var tmp18 = new String[4];
        tmp18[0] = "None";
        tmp18[1] = "Fill";
        tmp18[2] = "Outline";
        tmp18[3] = "Both";
        this.renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the target position.", new CategorySetting.Visibility(this.renderCategory), "Both", tmp18);
        var tmp19 = new String[2];
        tmp19[0] = "Fill";
        tmp19[1] = "Both";
        this.fillColorUp = new ColorSetting("FillColorUp", "The color that will be used for the fill gradiant upper part rendering.", new ModeSetting.Visibility(this.renderMode, tmp19), ColorUtils.getDefaultFillColor());
        var tmp20 = new String[2];
        tmp20[0] = "Fill";
        tmp20[1] = "Both";
        this.fillColorDown = new ColorSetting("FillColorDown", "The color that will be used for the fill gradiant lower part rendering.", new ModeSetting.Visibility(this.renderMode, tmp20), ColorUtils.getDefaultFillColor());
        var tmp21 = new String[2];
        tmp21[0] = "Outline";
        tmp21[1] = "Both";
        this.outlineColorUp = new ColorSetting("OutlineColorUp", "The color that will be used for the outline gradiant upper part rendering.", new ModeSetting.Visibility(this.renderMode, tmp21), ColorUtils.getDefaultOutlineColor());
        var tmp22 = new String[2];
        tmp22[0] = "Outline";
        tmp22[1] = "Both";
        this.outlineColorDown = new ColorSetting("OutlineColorDown", "The color that will be used for the outline gradiant lower part rendering.", new ModeSetting.Visibility(this.renderMode, tmp22), ColorUtils.getDefaultOutlineColor());
        this.renderDamage = new BooleanSetting("RenderDamage", "Damage", "Renders the damage that the position will do to the opponent.", new CategorySetting.Visibility(this.renderCategory), false);
        this.icon = new BooleanSetting("Icon", "Renders a customizable crystal icon on the rendered position.", new CategorySetting.Visibility(this.renderCategory), false);
        this.iconScale = new NumberSetting("IconScale", "The scaling that will be applied to the crystal icon rendering.", new BooleanSetting.Visibility(this.icon, true), 3, 1, 5);
        this.iconRadius = new NumberSetting("IconRadius", "The difference between the outer circle and the inner circle.", new BooleanSetting.Visibility(this.icon, true), 2.0f, 0.0f, 5.0f);
        this.iconColor = new ColorSetting("IconColor", "The color that will be used for the crystal icon rendering.", new BooleanSetting.Visibility(this.icon, true), ColorUtils.getDefaultColor());
        this.shader = new ModeSetting("Shader", "The animated shader that will be drawn on the target position instead of a flat color.", new CategorySetting.Visibility(this.renderCategory), "None", SHADER_MODES);
        this.shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "The speed at which the shader animates.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 1.0f, 0.1f, 10.0f);
        this.shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "The opacity of the shader rendering.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 100, 0, 100);
        this.shaderDistanceScaling = new BooleanSetting("ShaderDistanceScaling", "DistanceScaling", "Scales the shader pattern by your distance to the position.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), false);
        var tmp23 = new String[1];
        tmp23[0] = "Gradient";
        this.shaderStep = new NumberSetting("ShaderStep", "Step", "The size of the gradient bands.", new ModeSetting.Visibility(this.shader, tmp23), 50.0f, 0.1f, 200.0f);
        var tmp24 = new String[1];
        tmp24[0] = "Gradient";
        this.shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, tmp24), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
        var tmp25 = new String[1];
        tmp25[0] = "Gradient";
        this.shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, tmp25), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        var tmp26 = new String[1];
        tmp26[0] = "Gradient";
        this.shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, tmp26), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
        var tmp27 = new String[1];
        tmp27[0] = "Gradient";
        this.shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, tmp27), new ColorSetting.Color(new Color(0, 0, 255, 255), false, false));
        var tmp28 = new String[1];
        tmp28[0] = "Glowing";
        this.shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The color that the glow shader will be tinted with.", new ModeSetting.Visibility(this.shader, tmp28), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
        this.executor = Executors.newSingleThreadExecutor();
        this.pendingCalc = null;
        this.asyncLoopActive = false;
        this.latestReservedPlacements = Set.of();
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
        this.losDebugTimer = new Timer();
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
        this.mineTargetSecondary = null;
        this.mineIgnoreMinedPos = null;
        this.mineIgnorePlacedPos = null;
        this.calculationTime = "0.00ms";
        this.calculationCount = 0;
        this.calculationDamage = "0.00";
        this.crystalCounter = new Counter();
        this.crystalsPerSecond = 0;
        this.highestID = -100000;
        this.kickTicks = 0;
        this.cachedPlayers = Collections.emptyList();
        this.lastPlayerCacheTime = 0L;
        this.basePlaceTicks = 0;
        this.savedSlot = -1;
        this.lastPearlThrowTime = 0L;
        this.lastXpThrowTime = 0L;
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

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        Object var2 = ((AutoShopModule) LightningClient.MODULE_MANAGER.getModule(AutoShopModule.class));
        if (var2 != null && var2.isRunning()) {
        }
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
        int var3 = mc.player.getInventory().getSelectedSlot();
        if (this.lastSelectedSlot != -1 && this.lastSelectedSlot != var3) {
            this.swapTimer.reset();
        }
        this.lastSelectedSlot = var3;
        long var4 = 50L;
        this.attackedCrystals.entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), arg0));
        long var6 = (Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), 500L) + ((long) ((20.0f - this.attackSpeed.getValue().floatValue()) * 50.0f)));
        this.placedCrystals.entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > arg0);
        this.countedCrystals.entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), arg0));
        this.crystalsPerSecond = this.crystalCounter.getCount();
        if (this.losDebug.getValue() && this.losDebugTimer.hasTimeElapsed(1000L)) {
            this.losDebugTimer.reset();
            losDebug();
        }
        if (this.gameLoop.getValue()) return;
        run();
    }

    private void losDebug() {
        Object var1 = mc.player.getEyePosition();
        Object var2 = mc.level.clip(new ClipContext(var1, var1.add(mc.player.getViewVector(1.0f).scale(8.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        if (!((var2 instanceof BlockHitResult))) break label_27;
        Object var3 = ((BlockHitResult) var2);
        if (var2.getType() != HitResult.Type.BLOCK) break label_27;
        Object var4 = var3.getBlockPos();
        Object var5 = mc.level.clip(new ClipContext(var1, Vec3.atCenterOf(var4), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var5.getType() != HitResult.Type.MISS) {
            if (!((var5 instanceof BlockHitResult))) break label_1;
            Object var7 = ((BlockHitResult) var5);
            if (!(var7.getBlockPos().equals(var4))) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        int var6 = 0;
        var7 = mc.level.clip(new ClipContext(var1, new Vec3((((double) var4.getX()) + 0.5), (((double) var4.getY()) + 1.0), (((double) var4.getZ()) + 0.5)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var7.getType() != HitResult.Type.MISS) {
            if (!((var7 instanceof BlockHitResult))) break label_4;
            Object var9 = ((BlockHitResult) var7);
            if (!(var9.getBlockPos().equals(var4))) break label_4;
        }
        break label_5;
        label_4:
        label_5:
        int var8 = 0;
        var9 = CrystalPlacementHelper.isPlacementVisible(var4);
        if (!((var5 instanceof BlockHitResult))) break label_6;
        Object var11 = ((BlockHitResult) var5);
        if (var5.getType() != HitResult.Type.BLOCK) break label_6;
        break label_7;
        label_6:
        label_7:
        Object var10 = "-";
        var11 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var4, null, this.ignoreTerrain.getValue());
        Object var12 = this.target;
        if (var12 == null) {
        } else {
        }
        float var13 = DamageUtils.getCrystalDamage(var12, PositionUtils.extrapolate(var12, this.extrapolation.getValue().intValue()), var4, null, this.ignoreTerrain.getValue());
        if (this.placeTarget == null) {
        } else {
        }
        Object var14 = this.placeTarget.getPosition();
        var tmp0 = new Object[1];
        tmp0[0] = Math.sqrt(var1.distanceToSqr(Vec3.atCenterOf(var4)));
        if (mc.level.getBlockState(var4).getBlock() != Blocks.OBSIDIAN) {
            if (mc.level.getBlockState(var4).getBlock() == Blocks.BEDROCK) {
            }
        } else {
        }
        var tmp1 = new Object[1];
        tmp1[0] = var11;
        var tmp2 = new Object[1];
        tmp2[0] = var13;
        if (var14 == null) {
        } else {
        }
        var6.tagged((var8 + " los=" + var9 + "/" + var10 + "/" + String.format("%.2f", tmp0) + " blockedBy=" + 1 + " dist=" + 0 + " base=" + mc.level.getBlockState(var4.above()).isAir() + " air=" + String.format("%.1f", tmp1) + " self=" + String.format("%.1f", tmp2) + " dmg=" + "none" + " picked=" + var14.toShortString() + " calc=" + this.calculationTime), getName());
        Object var15 = null;
        double var16 = 1.7976931348623157E308;
        Object var18 = mc.level.entitiesForRendering().iterator();
        while (var18.hasNext()) {
            Object var19 = ((Entity) var18.next());
            if (!((var19 instanceof EndCrystal))) break label_18;
            Object var20 = ((EndCrystal) var19);
            if (!(var20.isAlive())) break label_18;
            double var21 = var20.getBoundingBox().distanceToSqr(var1);
            if (var21 >= var16) break label_18;
            var16 = var21;
            var15 = var20;
            label_18:
        }
        if (var15 == null) break label_27;
        var18 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var15, this.ignoreTerrain.getValue());
        var19 = ((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled();
        var20 = (var18 > this.maximumSelfDamage.getValue().floatValue() ? 1 : 0);
        if (!(this.antiSuicide.getValue())) break label_22;
        var21 = (var18 > (mc.player.getHealth() + mc.player.getAbsorptionAmount()) ? 1 : 0);
        var tmp3 = new Object[1];
        tmp3[0] = var18;
        if (!var19) {
            if (!var20 && !var21) {
            }
        } else {
        }
        var15.blockPosition().toShortString().tagged(("nearestCrystal=" + String.format("%.1f", tmp3) + " attackSelf=" + this.maximumSelfDamage.getValue().floatValue() + " maxSelf=" + var19 + " suicideOn=" + var20 + " overMax=" + this.antiSuicide.getValue() + " antiSuicide=" + var21 + " overHp=" + 1 + " gatePasses=" + 0), getName());
        label_27:
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if (isDead()) return;
        mineIgnoreTick();
        this.latestReservedPlacements = Set.copyOf(LightningClient.WORLD_MANAGER.getReservedPlacements());
        if (this.asynchronous.getValue()) {
            if (!(!this.asyncLoopActive) && this.pendingCalc != null && !(this.pendingCalc.isDone())) {
                if (!(this.pendingCalc.isCancelled())) break label_4;
            }
            this.asyncLoopActive = true;
            this.pendingCalc = this.executor.submit(this::asyncLoopStep);
            break label_4;
        }
        this.asyncLoopActive = false;
        runCalculation(this.latestReservedPlacements, false);
        label_4:
    }

    private void asyncLoopStep() {
        if (!(!this.asyncLoopActive) && this.asynchronous.getValue()) {
            if (isToggled()) break label_1;
        }
        this.asyncLoopActive = false;
        return;
        try {
            label_1:
            if ((mc.player == null) || (mc.level == null)) break label_2;
            runCalculation(this.latestReservedPlacements, true);
            label_2:
            if (((!this.asyncLoopActive) || (!(this.asynchronous.getValue()))) || (!(isToggled()))) break label_7;
            try {
                Thread.sleep(5L);
                this.pendingCalc = this.executor.submit(this::asyncLoopStep);
            } catch (InterruptedException ex5) {
                Object var1 = ex5;
                Thread.currentThread().interrupt();
                this.asyncLoopActive = false;
            } catch (Throwable ex6) {
                var1 = ex6;
                this.asyncLoopActive = false;
                break label_21;
                label_7:
                this.asyncLoopActive = false;
            } catch (Throwable ex8) {
                var1 = ex8;
                if (((!this.asyncLoopActive) || (!(this.asynchronous.getValue()))) || (!(isToggled()))) break label_13;
                try {
                    Thread.sleep(5L);
                    this.pendingCalc = this.executor.submit(this::asyncLoopStep);
                } catch (InterruptedException ex11) {
                    var1 = ex11;
                    Thread.currentThread().interrupt();
                    this.asyncLoopActive = false;
                } catch (Throwable ex12) {
                    var1 = ex12;
                    this.asyncLoopActive = false;
                    break label_21;
                    label_13:
                    this.asyncLoopActive = false;
                    break label_21;
                    Object var2 = null;
                    if (((!this.asyncLoopActive) || (!(this.asynchronous.getValue()))) || (!(isToggled()))) break label_19;
                    try {
                        Thread.sleep(5L);
                        this.pendingCalc = this.executor.submit(this::asyncLoopStep);
                    } catch (InterruptedException ex17) {
                        Object var3 = ex17;
                        Thread.currentThread().interrupt();
                        this.asyncLoopActive = false;
                    } catch (Throwable ex18) {
                        var3 = ex18;
                        this.asyncLoopActive = false;
                        break label_20;
                        label_19:
                        this.asyncLoopActive = false;
                    }
                }
                label_20:
                throw var2;
            }
        }
        }
        }
        }
        label_21:
    }

    private void runCalculation(Set arg0, boolean arg1) {
        long var3 = System.nanoTime();
        try {
            this.attackTarget = calculateCrystals();
            this.placeTarget = calculatePlacements(null, arg0);
        } catch (Throwable ex2) {
            Object var5 = ex2;
            return;
        }
        var5 = (System.nanoTime() - var3);
        this.calculationTime = (new DecimalFormat("0.00").format((((double) var5) / 1000000.0)) + "ms");
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
        Object var7 = this.placeTarget.getPlayer();
        if ((!(this.blockDestruction.getValue())) || (!arg1)) break label_14;
        Object var8 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        Object var9 = null;
        if (var8.getPrimary() != null && var8.getPrimary().isMining()) {
            var9 = var8.getPrimary().getPosition();
        }
        if (var9 != null) {
            this.mineTarget = calculatePlacements(var9, arg0);
        }
        Object var10 = null;
        if (var8.farReach.getValue()) {
            if ((var8.getSecondary() == null) || (!(var8.getSecondary().isMining()))) break label_13;
            var10 = var8.getSecondary().getPosition();
            break label_13;
        }
        if ((var8.getSecondary() == null) || (!(var8.getSecondary().isMining()))) break label_13;
        var10 = var8.getSecondary().getPosition();
        label_13:
        if (var10 == null) break label_14;
        this.mineTargetSecondary = calculatePlacements(var10, arg0);
        label_14:
    }

    @SubscribeEvent
    public void onGameLoop(GameLoopEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if (isDead()) return;
        if (!(this.gameLoop.getValue())) return;
        if (!(this.loopTimer.hasTimeElapsed(this.loopDelay.getValue().longValue()))) return;
        this.loopTimer.reset();
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
        if (isDead()) return;
        if (pauseForSecondaryMine()) break label_1;
        if (pauseForAutoMine()) {
            label_1:
            return;
        }
        if ((isPearlPaused()) || (isXpPaused())) {
            return;
        }
        this.attackRunnable = null;
        this.placeRunnable = null;
        if (!(this.basePlace.getValue())) break label_6;
        if (this.basePlaceTicks < this.basePlaceDelay.getValue().intValue()) {
            this.basePlaceTicks = (this.basePlaceTicks + 1);
            break label_6;
        }
        if (!(executeBasePlace())) break label_6;
        this.basePlaceTicks = 0;
        label_6:
        if (this.sequential.getValue().equalsIgnoreCase("None")) {
            if (!(!this.sequenceAttack)) {
                this.sequenceAttack = false;
                this.sequencePlace = true;
                attackCrystals();
                return;
            }
            if (!this.sequencePlace) break label_10;
            this.sequenceAttack = true;
            this.sequencePlace = false;
            placeCrystals(false);
            break label_10;
        }
        if (this.attack.getValue()) {
            attackCrystals();
        }
        if (!(this.place.getValue())) break label_10;
        placeCrystals(false);
        label_10:
    }

    private boolean pauseForSecondaryMine() {
        if (!(this.pauseOnSecondaryMine.getValue())) return false;
        Object var1 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (var1 == null) return false;
        if (var1.farReach.getValue()) {
            Object var2 = var1.getSecondary();
            if ((var2 == null) || (!(var2.isMining()))) break label_2;
            return var2.getProgress() >= 0.85f;
        }
        var2 = var1.getSecondary();
        if ((var2 == null) || (!(var2.isMining()))) break label_5;
        return var2.getProgress() >= 0.85f;
    }

    private boolean pauseForAutoMine() {
        if (!(this.awaitMine.getValue())) return false;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var1 = null;
        Object var2 = null;
        if (var1 == null) return false;
        Object var3 = var1.getPrimary();
        if ((var3 == null) || (!(var3.isMining()))) break label_4;
        return var3.getTicks() < this.awaitMineTicks.getValue().intValue();
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

    @SubscribeEvent
    public void onUpdateMovement$POST(UpdateMovementEvent.Post arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if (isDead()) return;
        if (this.attackRunnable != null) {
            this.attackRunnable.run();
        }
        if (this.placeRunnable != null) {
            this.placeRunnable.run();
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if (isDead()) break label_1;
        if (!(getPlayers().isEmpty())) break label_2;
        label_1:
        return;
        label_2:
        if (shouldPause("Attack")) return;
        if ((!(this.attack.getValue())) || (!(this.instant.getValue()))) {
            return;
        }
        if (!(this.attackTimer.hasTimeElapsed((1000.0f - (this.attackSpeed.getValue().floatValue() * 50.0f))))) return;
        Object var3 = arg0.getEntity();
        if (!((var3 instanceof EndCrystal))) return;
        Object var4 = ((EndCrystal) var3);
        if (this.inhibit.getValue() && this.attackedCrystals.containsKey(var4.getId())) return;
        if (isAtActiveUnbrokenMinePos(var4)) return;
        Object var5 = var4.blockPosition().below();
        if (!(this.placedCrystals.containsKey(var5))) {
            var5 = BlockPos.containing(var4.getX(), (var4.getY() - 0.5), var4.getZ());
        }
        if (!(this.placedCrystals.containsKey(var5))) return;
        if (var4.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) return;
        if (!(mc.level.getWorldBorder().isWithinBounds(var4.blockPosition()))) return;
        if (!(this.raytrace.getValue())) {
            if (var4.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue())) {
            }
        } else {
        }
        int var2 = 0;
        int var6 = 0;
        if (!(!var2) && !(WorldUtils.canSee(var4))) return;
        if (!(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            float var7 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var4, this.ignoreTerrain.getValue());
            if (var7 > this.maximumSelfDamage.getValue().floatValue()) return;
            if (this.antiSuicide.getValue() && var7 > (mc.player.getHealth() + mc.player.getAbsorptionAmount())) return;
        }
        var7 = RotationUtils.getRotations(Vec3.atCenterOf(var4.blockPosition()));
        if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
            if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
            }
            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), var7);
        }
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            LightningClient.ROTATION_MANAGER.legacyRotate(calculateRotations(Vec3.atCenterOf(var4.blockPosition())), LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        attack(var4);
        this.attackedSequentially = true;
        if (this.sequential.getValue().equalsIgnoreCase("Strong")) {
            placeCrystals(true);
            this.placeRunnable = null;
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if (isDead()) break label_1;
        if (!(getPlayers().isEmpty())) break label_2;
        label_1:
        return;
        label_2:
        if (shouldPause("Place")) return;
        this.kickTicks = 0;
        if (((!(this.mineIgnore.getValue())) || (arg0.getPosition() == null)) || (!(arg0.getPosition().equals(this.mineIgnoreMinedPos)))) break label_4;
        mineIgnoreDetonate();
        label_4:
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
        Object var9 = this.mineTarget.clone();
        if (this.mineTargetSecondary == null) {
        } else {
        }
        var tmp0 = this.mineTargetSecondary.clone();
        Object var4 = tmp0;
        Object var10 = tmp0;
        if (var9 != null && var9.getPosition() != null && var5.equals(var9.getException())) {
            Object var3 = var9;
            break label_18;
        }
        if (var4 != null && var4.getPosition() != null && var5.equals(var4.getException())) {
            var3 = var4;
            break label_18;
        }
        if (!(this.asynchronous.getValue())) {
            var3 = calculatePlacements(var5);
            break label_18;
        }
        var tmp1 = (var9 != null ? var9 : var4);
        var3 = tmp1;
        Object var11 = tmp1;
        label_18:
        if (var3 != null && var3.getPosition() != null) {
            if (var3.getDamage() <= 0.0f) {
            }
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        var11 = var3.getPosition();
        if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var11)) > Mth.square(this.placeRange.getValue().doubleValue())) {
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            return;
        }
        LightningClient.RENDER_MANAGER.setRenderPosition(var11);
        if (!(this.raytrace.getValue())) {
            if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var11)) > Mth.square(this.placeWallsRange.getValue().doubleValue())) {
            }
        } else {
        }
        int var2 = 0;
        int var12 = 0;
        if (!(!var2) && !(WorldUtils.canSeeBlock(var11))) return;
        Object var13 = null;
        Object var14 = mc.level.getEntities(((Entity) null), new AABB(var11.above()), (arg0) -> true).iterator();
        label_26:
        if (!(var14.hasNext())) break label_28;
        Object var15 = ((Entity) var14.next());
        if (!((var15 instanceof EndCrystal))) {
            break label_26;
        }
        var tmp2 = ((EndCrystal) var15);
        Object var16 = tmp2;
        var13 = tmp2;
        break label_28;
        label_28:
        if (var13 != null) {
            if (isAtActiveUnbrokenMinePos(var13)) {
                LightningClient.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            if (!(this.attack.getValue())) {
                LightningClient.RENDER_MANAGER.setRenderPosition(null);
                return;
            }
            var14 = RotationUtils.getRotations(var13);
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                LightningClient.ROTATION_MANAGER.legacyRotate(var14, this, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                }
                LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), var14);
            }
            attack(var13);
            return;
        }
        var14 = CrystalPlacementHelper.getVisiblePlacement(var11);
        if (var14 != null && var14.hitVec != null) {
        } else {
        }
        var15 = Vec3.atCenterOf(var11).add(0.0, 0.5, 0.0);
        var16 = calculateRotations(var15);
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            LightningClient.ROTATION_MANAGER.legacyRotate(var16, this, (LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this) + 1));
        }
        if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
            if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
            }
            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), var16);
        }
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None")) && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                this.savedSlot = var7;
            }
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), var6, var7);
            var8 = 1;
        }
        place(var11);
        if (!(!var8)) {
            InventoryUtils.switchBack(this.autoSwitch.getValue(), var6, var7);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundAddEntityPacket))) break label_5;
        Object var4 = ((ClientboundAddEntityPacket) var2);
        if (var4.getId() > this.highestID) {
            this.highestID = var4.getId();
        }
        var tmp0 = BlockPos.containing(var4.getX(), var4.getY(), var4.getZ()).offset(0, -1, 0);
        Object var3 = tmp0;
        if (!(this.countedCrystals.containsKey(tmp0))) break label_5;
        if (this.facePlaceTimer.hasTimeElapsed((this.facePlaceDelay.getValue().longValue() * 50L))) {
            this.facePlaceTimer.reset();
        }
        this.countedCrystals.remove(var3);
        this.crystalCounter.increment();
        this.crystalsPerSecond = this.crystalCounter.getCount();
        label_5:
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
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
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
    public void onClientConnect(ClientConnectEvent arg0) {
        if (PingBypassFlags.isPingBypassActive()) return;
        this.highestID = -100000;
    }

    public void onDisable() {
        this.asyncLoopActive = false;
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
        this.cachedPlayers.clear();
        this.lastPlayerCacheTime = 0L;
    }

    public String getMetaData() {
        return (this.calculationTime + ", " + this.calculationCount + ", " + this.calculationDamage + ", " + this.crystalsPerSecond);
    }

    public boolean hasLiveCrystalNear(BlockPos arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return false;
        }
        return !(mc.level.getEntities(((Entity) null), new AABB(arg0).inflate(1.0), (arg0) -> {
    if (!((arg0 instanceof EndCrystal))) break label_0;
    return arg0.isAlive();
}).isEmpty());
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

    private void attackCrystals() {
        if (isDead()) return;
        if (shouldPause("Attack")) return;
        if (!(getPlayers().isEmpty())) break label_2;
        this.attackTarget = null;
        return;
        label_2:
        Object var1 = null;
        int var2 = 0;
        Object var3 = this.placeTarget;
        if ((((var3 == null) || (var3.getPosition() != null)) || (var3.getObstructions() == null)) || (var3.getObstructions().isEmpty())) break label_10;
        Object var4 = var3.getObstructions().iterator();
        label_3:
        if (!(var4.hasNext())) break label_10;
        Object var5 = ((Entity) var4.next());
        if (!((var5 instanceof EndCrystal))) break label_3;
        var tmp0 = ((EndCrystal) var5);
        Object var8 = tmp0;
        if (((!(tmp0.isAlive())) || (isAtActiveUnbrokenMinePos(var8))) || (var8.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue()))) break label_3;
        if (mc.level.getWorldBorder().isWithinBounds(var8.blockPosition())) break label_4;
        break label_3;
        label_4:
        if (!(this.raytrace.getValue())) {
            if (var8.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue())) {
            }
        } else {
        }
        int var7 = 0;
        int var9 = 0;
        if (!(!var7)) {
            if (!(WorldUtils.canSee(var8))) break label_3;
        }
        if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_9;
        var tmp1 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var8, this.ignoreTerrain.getValue());
        float var6 = tmp1;
        if (tmp1 > this.maximumSelfDamage.getValue().floatValue()) break label_3;
        if ((!(this.antiSuicide.getValue())) || (var6 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_9;
        break label_3;
        label_9:
        var1 = var8;
        var2 = 1;
        break label_10;
        label_10:
        if (var1 == null) {
            var1 = this.attackTarget;
        }
        if (var1 != null) break label_25;
        var4 = -1.0f;
        var5 = selectTargets(getPlayers());
        var6 = mc.level.entitiesForRendering().iterator();
        while (var6.hasNext()) {
            var7 = ((Entity) var6.next());
            if (!((var7 instanceof EndCrystal))) break label_12;
            var tmp2 = ((EndCrystal) var7);
            Object var10 = tmp2;
            if ((!(tmp2.isAlive())) || (isAtActiveUnbrokenMinePos(var10))) break label_12;
            if (this.inhibit.getValue() && this.attackedCrystals.containsKey(var10.getId()) && (System.currentTimeMillis() - ((Long) this.attackedCrystals.get(var10.getId())).longValue()) < Math.max((((long) LightningClient.SERVER_MANAGER.getPing()) * 2L), 100L)) {
                break label_12;
            }
            Object var11 = var10.blockPosition().below();
            if (!(this.placedCrystals.containsKey(var11))) {
                var11 = BlockPos.containing(var10.getX(), (var10.getY() - 0.5), var10.getZ());
            }
            if ((!(this.placedCrystals.containsKey(var11))) || (var10.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue()))) break label_12;
            if (!(mc.level.getWorldBorder().isWithinBounds(var10.blockPosition()))) {
                break label_12;
            }
            if (!(this.raytrace.getValue())) {
                if (var10.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue())) {
                }
            } else {
            }
            var9 = 0;
            int var12 = 0;
            if (!(!var9)) {
                if (!(WorldUtils.canSee(var10))) break label_12;
            }
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_20;
            var tmp3 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var10, this.ignoreTerrain.getValue());
            var8 = tmp3;
            if (tmp3 > this.maximumSelfDamage.getValue().floatValue()) break label_12;
            if ((!(this.antiSuicide.getValue())) || (var8 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_20;
            break label_12;
            label_20:
            float var13 = 0.0f;
            Object var14 = var5.iterator();
            while (var14.hasNext()) {
                Object var15 = ((Player) var14.next());
                float var16 = DamageUtils.getCrystalDamage(var15, PositionUtils.extrapolate(var15, this.extrapolation.getValue().intValue()), var10, this.ignoreTerrain.getValue());
                if (var16 <= var13) {
                    break label_21;
                }
                var13 = var16;
            }
            if (var13 <= var4) {
                break label_12;
            }
            var4 = var13;
            var1 = var10;
            var2 = 1;
        }
        label_25:
        if (var1 == null) return;
        var4 = var1;
        if (isAtActiveUnbrokenMinePos(var4)) return;
        if (!(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            var5 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var4, this.ignoreTerrain.getValue());
            if (var5 > this.maximumSelfDamage.getValue().floatValue()) return;
            if (this.antiSuicide.getValue() && var5 > (mc.player.getHealth() + mc.player.getAbsorptionAmount())) return;
        }
        var5 = calculateRotations(Vec3.atCenterOf(var4.blockPosition()));
        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
            LightningClient.ROTATION_MANAGER.legacyRotate(var5, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        if (this.swapDelay.getValue().intValue() > 0 && !(this.swapTimer.hasTimeElapsed((this.swapDelay.getValue().longValue() * 50L)))) return;
        if (!(this.attackTimer.hasTimeElapsed((1000.0f - (this.attackSpeed.getValue().floatValue() * 50.0f))))) return;
        var6 = mc.level.getEntity(var4.getId());
        var7 = null;
        if (var6 == null) {
            var7 = "entity-gone";
            break label_42;
        }
        if (!((var6 instanceof EndCrystal))) {
            var7 = "not-end-crystal";
            break label_42;
        }
        if (!(((EndCrystal) var6).isAlive())) {
            var7 = "dead";
            break label_42;
        }
        if (this.inhibit.getValue() && !var2 && this.attackedCrystals.containsKey(var6.getId())) {
            var7 = "inhibit";
            break label_42;
        }
        if (var6.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) {
            var7 = "range";
            break label_42;
        }
        if (!(mc.level.getWorldBorder().isWithinBounds(var6.blockPosition()))) {
            var7 = "border";
            break label_42;
        }
        if (!(this.raytrace.getValue())) {
            if (var6.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue())) {
            }
        } else {
        }
        var8 = 0;
        var9 = 0;
        if ((!var8) || (WorldUtils.canSee(var6))) break label_42;
        var7 = "cannot-see";
        label_42:
        if (var7 != null) return;
        this.attackRunnable = () -> {
    if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
        }
        LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(arg0.blockPosition())));
    }
    attack(arg0);
};
    }

    private void placeCrystals(boolean arg0) {
        if ((isDead()) || (shouldPause("Place"))) break label_52;
        if (this.placeTarget == null) {
            break label_1;
        }
        label_1:
        Object var2 = this.placeTarget.clone();
        if (var2 != null && var2.getPosition() != null) {
            if (var2.getPlayer() != null && var2.getPlayer().isAlive() && !(var2.getPlayer().isDeadOrDying()) && var2.getPlayer().getHealth() > 0.0f) {
                int var3 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
                int var4 = mc.player.getInventory().getSelectedSlot();
                if (((this.autoSwitch.getValue().equalsIgnoreCase("None")) || (var3 != -1)) || (mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL)) break label_2;
                if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) break label_49;
                label_2:
                Object var5 = var2.getPosition();
                if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var5)) <= Mth.square(this.placeRange.getValue().doubleValue())) break label_3;
                LightningClient.RENDER_MANAGER.setRenderPosition(null);
                break label_49;
                label_3:
                LightningClient.RENDER_MANAGER.setRenderPosition(var5);
                if (!(mc.level.getWorldBorder().isWithinBounds(var5))) break label_49;
                if (mc.level.getBlockState(var5).getBlock() == Blocks.OBSIDIAN) break label_4;
                if (mc.level.getBlockState(var5).getBlock() != Blocks.BEDROCK) break label_49;
                label_4:
                if (!(mc.level.getBlockState(var5.offset(0, 1, 0)).isAir())) break label_49;
                if (!(this.placements.getValue().equalsIgnoreCase("Protocol"))) break label_5;
                if (!(mc.level.getBlockState(var5.offset(0, 2, 0)).isAir())) break label_49;
                label_5:
                if (!(this.raytrace.getValue())) {
                    if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var5)) > Mth.square(this.placeWallsRange.getValue().doubleValue())) {
                    }
                } else {
                }
                int var6 = 0;
                if (!(!var6)) {
                    if (!(WorldUtils.canSeeBlock(var5))) break label_49;
                }
                IF (mc.level.getEntities(((Entity) null), getCrystalPlacementBox(var5), (arg0) -> true).stream().anyMatch((arg0) -> {
    if ((!(arg0.isAlive())) || ((arg0 instanceof ExperienceOrb))) break label_0;
    return !((arg0 instanceof EndCrystal));
})) break label_49;
                Object var7 = null;
                Object var8 = mc.level.getEntities(((Entity) null), getCrystalPlacementBox(var5), (arg0) -> {
    if (!((arg0 instanceof EndCrystal))) break label_0;
    return arg0.isAlive();
}).iterator();
                if (var8.hasNext()) {
                    Object var9 = ((Entity) var8.next());
                    var7 = ((EndCrystal) var9);
                }
                if (var7 != null) {
                    if ((!(this.attack.getValue())) || (isAtActiveUnbrokenMinePos(var7))) break label_49;
                    if (!(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
                        var9 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var7, this.ignoreTerrain.getValue());
                        if (var9 > this.maximumSelfDamage.getValue().floatValue()) break label_15;
                        if (this.antiSuicide.getValue()) {
                            if (var9 > (mc.player.getHealth() + mc.player.getAbsorptionAmount())) break label_15;
                        }
                        Object var10 = calculateRotations(Vec3.atCenterOf(var7.blockPosition()));
                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                            LightningClient.ROTATION_MANAGER.legacyRotate(var10, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                        }
                        if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                            if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                            }
                            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(var7.blockPosition())));
                        }
                        attack(var7);
                        label_15:
                        break label_49;
                    }
                    var9 = calculateRotations(Vec3.atCenterOf(var7.blockPosition()));
                    if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                        LightningClient.ROTATION_MANAGER.legacyRotate(var9, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                    }
                    if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                        if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                        }
                        LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(var7.blockPosition())));
                    }
                    attack(var7);
                    break label_49;
                }
                var9 = mc.level.entitiesForRendering().iterator();
                while (var9.hasNext()) {
                    var10 = ((Entity) var9.next());
                    if (!((var10 instanceof EndCrystal))) break label_42;
                    Object var11 = ((EndCrystal) var10);
                    if ((!(var11.isAlive())) || (isAtActiveUnbrokenMinePos(var11))) break label_42;
                    Object var12 = var11.blockPosition().below();
                    if (!(this.placedCrystals.containsKey(var12))) {
                        var12 = BlockPos.containing(var11.getX(), (var11.getY() - 0.5), var11.getZ());
                    }
                    if (!(this.placedCrystals.containsKey(var12))) break label_42;
                    double var13 = var11.getBoundingBox().distanceToSqr(mc.player.getEyePosition());
                    int var15 = (var13 <= Mth.square(this.attackRange.getValue().doubleValue()) ? 1 : 0);
                    if (!(this.raytrace.getValue())) {
                        if (var13 > Mth.square(this.attackWallsRange.getValue().doubleValue())) {
                        }
                    } else {
                    }
                    int var16 = 0;
                    if (!var15) break label_29;
                    if (!(!var16)) {
                        if (!(WorldUtils.canSee(var11))) break label_29;
                    }
                    break label_30;
                    label_29:
                    label_30:
                    int var17 = 0;
                    if (!(!var17)) {
                        if (!(this.attack.getValue())) break label_40;
                        if (!(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
                            float var18 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var11, this.ignoreTerrain.getValue());
                            if (var18 > this.maximumSelfDamage.getValue().floatValue()) break label_35;
                            if (this.antiSuicide.getValue()) {
                                if (var18 > (mc.player.getHealth() + mc.player.getAbsorptionAmount())) break label_35;
                            }
                            Object var19 = calculateRotations(Vec3.atCenterOf(var11.blockPosition()));
                            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                                LightningClient.ROTATION_MANAGER.legacyRotate(var19, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                            }
                            if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                                }
                                LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(var11.blockPosition())));
                            }
                            attack(var11);
                            label_35:
                            break label_40;
                        }
                        var18 = calculateRotations(Vec3.atCenterOf(var11.blockPosition()));
                        if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                            LightningClient.ROTATION_MANAGER.legacyRotate(var18, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                        }
                        if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                            if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                            }
                            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(var11.blockPosition())));
                        }
                        attack(var11);
                        label_40:
                        return;
                    }
                    if (var12.equals(var5)) return;
                    label_42:
                }
                var9 = CrystalPlacementHelper.getVisiblePlacement(var5);
                if (var9 != null && var9.hitVec != null) {
                } else {
                }
                var10 = Vec3.atCenterOf(var5).add(0.0, 0.5, 0.0);
                var11 = calculateRotations(var10);
                if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                    LightningClient.ROTATION_MANAGER.legacyRotate(var11, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                }
                if (this.swapDelay.getValue().intValue() > 0) {
                    if (!(this.swapTimer.hasTimeElapsed((this.swapDelay.getValue().longValue() * 50L)))) break label_49;
                }
                if (!(this.placeTimer.hasTimeElapsed((1000.0f - (this.placeSpeed.getValue().floatValue() * 50.0f))))) break label_49;
                if (!arg0 && !(!this.placedSequentially)) {
                    this.placedSequentially = false;
                    break label_49;
                }
                this.placeRunnable = () -> {
    int var5 = 0;
    if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
        if (!(this.rotate.getValue().equalsIgnoreCase("Silent"))) break label_1;
    }
    LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(arg0));
    label_1:
    if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
        if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
            this.savedSlot = arg1;
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), arg2, arg1);
        var5 = 1;
    }
    place(arg3);
    if (!(!var5)) {
        InventoryUtils.switchBack(this.autoSwitch.getValue(), arg2, arg1);
    }
    if (this.godSync.getValue()) {
        if (this.antiKick.getValue()) {
            if (!(mc.player.getMainHandItem().getItem() instanceof ExperienceBottleItem) && !(mc.player.getOffhandItem().getItem() instanceof ExperienceBottleItem) && !(((KeyActionModule) LightningClient.MODULE_MANAGER.getModule(KeyActionModule.class)).isXpActive())) {
            }
        } else {
        }
        int var6 = 0;
        if (this.antiKick.getValue()) {
            if (this.kickTicks <= this.kickThreshold.getValue().intValue()) break label_17;
        }
        if (!var6) break label_17;
        if (this.fast.getValue()) break label_11;
        Object var7 = mc.level.entitiesForRendering().iterator();
        while (var7.hasNext()) {
            Object var8 = ((Entity) var7.next());
            if (var8.getId() > this.highestID) {
                this.highestID = var8.getId();
            }
        }
        label_11:
        var7 = (1 - this.offset.getValue().intValue());
        while (var7 < this.predictions.getValue().intValue()) {
            var8 = mc.level.getEntity(this.highestID);
            if (var8 != null) {
                if ((var8 instanceof EndCrystal)) {
                }
                int var9 = (this.highestID + var7);
                mc.getConnection().send(new ServerboundAttackPacket(var9));
                if (this.godSwing.getValue().equals("Strict")) {
                    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
                this.attackedCrystals.put(var9, System.currentTimeMillis());
            }
            var7++;
        }
        if (!(this.godSwing.getValue().equals("Normal"))) break label_17;
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        label_17:
        this.kickTicks = (this.kickTicks + 1);
    }
};
                if (!arg0) break label_49;
                this.placeRunnable.run();
                this.placeRunnable = null;
                this.placedSequentially = true;
                label_49:
                break label_52;
            }
            this.placeTarget = null;
            this.target = null;
            LightningClient.RENDER_MANAGER.setRenderPosition(null);
            break label_52;
        }
        LightningClient.RENDER_MANAGER.setRenderPosition(null);
        label_52:
    }

    private EndCrystal calculateCrystals() {
        if (!(this.attack.getValue())) return null;
        if (shouldPause("Attack")) return null;
        Object var1 = selectTargets(getPlayers());
        if (var1.isEmpty()) return null;
        Object var2 = null;
        float var3 = 0.0f;
        Object var4 = mc.level.entitiesForRendering().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Entity) var4.next());
            if (!((var5 instanceof EndCrystal))) break label_3;
            var tmp0 = ((EndCrystal) var5);
            Object var8 = tmp0;
            if ((!(tmp0.isAlive())) || (isAtActiveUnbrokenMinePos(var8))) break label_3;
            if (this.inhibit.getValue()) {
                if (this.attackedCrystals.containsKey(var5.getId())) break label_3;
            }
            if (var8.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackRange.getValue().doubleValue())) break label_3;
            if (!(mc.level.getWorldBorder().isWithinBounds(var8.blockPosition()))) {
                break label_3;
            }
            if (!(this.raytrace.getValue())) {
                if (var8.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > Mth.square(this.attackWallsRange.getValue().doubleValue())) {
                }
            } else {
            }
            int var7 = 0;
            int var9 = 0;
            if (!(!var7)) {
                if (!(WorldUtils.canSee(var8))) break label_3;
            }
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_10;
            var tmp1 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var8, this.ignoreTerrain.getValue());
            float var6 = tmp1;
            if (tmp1 > this.maximumSelfDamage.getValue().floatValue()) break label_3;
            if ((!(this.antiSuicide.getValue())) || (var6 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_10;
            break label_3;
            label_10:
            int var10 = 0;
            Object var11 = var1.iterator();
            label_11:
            if (!(var11.hasNext())) break label_15;
            Object var12 = ((Player) var11.next());
            float var13 = DamageUtils.getCrystalDamage(var12, PositionUtils.extrapolate(var12, this.extrapolation.getValue().intValue()), var8, this.ignoreTerrain.getValue());
            if (var13 < getMinimumDamage(var12, this.minimumDamage.getValue().floatValue()) && var13 < (var12.getHealth() + var12.getAbsorptionAmount())) {
                if ((var13 * (1.0f + this.lethalMultiplier.getValue().floatValue())) < (var12.getHealth() + var12.getAbsorptionAmount())) break label_11;
            }
            if (var13 <= var3 && var13 <= (var12.getHealth() + var12.getAbsorptionAmount())) {
                break label_11;
            }
            var2 = var8;
            var3 = var13;
            if (var13 <= (var12.getHealth() + var12.getAbsorptionAmount())) {
                break label_11;
            }
            var10 = 1;
            break label_15;
            label_15:
            if (var10) break label_16;
        }
        label_16:
        return var2;
    }

    private AutoCrystalModule.PlaceTarget calculatePlacements(BlockPos arg0) {
        return calculatePlacements(arg0, LightningClient.WORLD_MANAGER.getReservedPlacements());
    }

    private AutoCrystalModule.PlaceTarget calculatePlacements(BlockPos arg0, Set arg1) {
        if (!(this.place.getValue())) return null;
        if (shouldPause("Place")) break label_35;
        if (this.autoSwitch.getValue().equalsIgnoreCase("None")) break label_1;
        if (InventoryUtils.findHotbar(Items.END_CRYSTAL) != -1) break label_2;
        label_1:
        if (mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL) break label_2;
        if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) break label_35;
        label_2:
        Object var3 = selectTargets(getPlayers());
        if (var3.isEmpty()) return null;
        Object var4 = null;
        Object var5 = null;
        float var6 = 0.0f;
        float var7 = 0.0f;
        if (this.placeTarget == null) {
        } else {
        }
        Object var8 = this.placeTarget.getPosition();
        float var9 = 0.5f;
        Object var10 = new ArrayList();
        int var11 = 0;
        int var12 = 0;
        while (var12 < LightningClient.WORLD_MANAGER.getRadius(Math.max(this.placeRange.getValue().doubleValue(), this.placeWallsRange.getValue().doubleValue()))) {
            Object var13 = mc.player.blockPosition().offset(LightningClient.WORLD_MANAGER.getOffset(var12));
            if ((mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var13)) > Mth.square(this.placeRange.getValue().doubleValue())) || (!(mc.level.getWorldBorder().isWithinBounds(var13)))) break label_31;
            if (mc.level.getBlockState(var13).getBlock() != Blocks.OBSIDIAN) {
                if (mc.level.getBlockState(var13).getBlock() != Blocks.BEDROCK) break label_31;
            }
            if (!(mc.level.getBlockState(var13.offset(0, 1, 0)).isAir())) break label_31;
            if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
                if (!(mc.level.getBlockState(var13.offset(0, 2, 0)).isAir())) break label_31;
            }
            if (!(this.raytrace.getValue())) {
                if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var13)) > Mth.square(this.placeWallsRange.getValue().doubleValue())) {
                }
            } else {
            }
            int var14 = 0;
            if (!(!var14)) {
                if (!(WorldUtils.canSeeBlock(var13))) break label_31;
            }
            Object var15 = new AABB(((double) (var13.getX() - 1)), ((double) (var13.getY() + 1)), ((double) (var13.getZ() - 1)), ((double) (var13.getX() + 2)), ((double) (var13.getY() + 3)), ((double) (var13.getZ() + 2)));
            if (arg1.stream().anyMatch((arg1) -> arg0.intersects(new AABB(arg1)))) break label_31;
            Object var16 = mc.level.getEntities(((Entity) null), getCrystalPlacementBox(var13), (arg0) -> {
    if (!(arg0.isAlive())) break label_0;
    return !((arg0 instanceof ExperienceOrb));
});
            int var17 = 0;
            Object var18 = new ArrayList();
            Object var19 = var16.iterator();
            while (var19.hasNext()) {
                Object var20 = ((Entity) var19.next());
                if ((var20 instanceof EndCrystal)) {
                    Object var21 = ((EndCrystal) var20);
                } else {
                    var17 = 1;
                    break label_20;
                }
                if (isAtActiveUnbrokenMinePos(var21)) {
                    var17 = 1;
                    break label_20;
                }
                Object var22 = var21.blockPosition().below();
                if (!(this.placedCrystals.containsKey(var22))) {
                    var22 = BlockPos.containing(var21.getX(), (var21.getY() - 0.5), var21.getZ());
                }
                if (this.placedCrystals.containsKey(var22)) {
                    if (var21.tickCount >= ((20 - this.attackSpeed.getValue().intValue()) + 15)) {
                    }
                    var18.add(var21);
                }
            }
            label_20:
            if (var17) break label_31;
            var19 = Mth.square(10.0);
            var21 = 0;
            var22 = var3.iterator();
            while (var22.hasNext()) {
                Object var23 = ((Player) var22.next());
                if (var23.distanceToSqr((((double) var13.getX()) + 0.5), (((double) var13.getY()) + 1.0), (((double) var13.getZ()) + 0.5)) <= var19) {
                    var21 = 1;
                    break label_23;
                }
            }
            label_23:
            if (!var21) break label_31;
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_24;
            var22 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var13, arg0, this.ignoreTerrain.getValue());
            if (var22 > this.maximumSelfDamage.getValue().floatValue()) break label_31;
            if ((!(this.antiSuicide.getValue())) || (var22 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_24;
            break label_31;
            label_24:
            var22 = var3.iterator();
            while (var22.hasNext()) {
                var23 = ((Player) var22.next());
                var11++;
                float var24 = DamageUtils.getCrystalDamage(var23, PositionUtils.extrapolate(var23, this.extrapolation.getValue().intValue()), var13, arg0, this.ignoreTerrain.getValue());
                if (var24 < getMinimumDamage(var23, this.minimumDamage.getValue().floatValue()) && var24 < (var23.getHealth() + var23.getAbsorptionAmount())) {
                    if ((var24 * (1.0f + this.lethalMultiplier.getValue().floatValue())) < (var23.getHealth() + var23.getAbsorptionAmount())) break label_30;
                }
                if (var24 <= 0.0f) break label_30;
                if (arg0 == null && !(var18.isEmpty())) {
                    var10.add(((Entity) var18.getFirst()));
                    break label_31;
                }
                float var25 = (var24 + (var13.equals(var8) ? 0.5f : 0.0f));
                if (var25 <= var6) break label_30;
                var4 = var13;
                var5 = var23;
                var6 = var25;
                var7 = var24;
                label_30:
            }
            label_31:
            var12++;
        }
        if (var4 == null) {
        } else {
        }
        return new AutoCrystalModule.PlaceTarget(var4, var5, var10, arg0, var7, var11);
        label_35:
        return null;
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

    private void attack(EndCrystal arg0) {
        if (((arg0 == null) || (!(arg0.isAlive()))) || (arg0.isRemoved())) {
            return;
        }
        if (isAtActiveUnbrokenMinePos(arg0)) return;
        if (!(((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled())) {
            float var3 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), arg0, this.ignoreTerrain.getValue());
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

    private void mineIgnoreTick() {
        if (!(this.mineIgnore.getValue())) {
            this.mineIgnoreMinedPos = null;
            this.mineIgnorePlacedPos = null;
            return;
        }
        Object var1 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class)).getPrimary();
        if (var1 != null) {
            if (!(var1.isMining())) {
            }
            this.mineIgnoreMinedPos = null;
            this.mineIgnorePlacedPos = null;
            return;
        }
        Object var2 = var1.getPosition();
        if (!(var2.equals(this.mineIgnoreMinedPos))) {
            this.mineIgnoreMinedPos = var2;
            this.mineIgnorePlacedPos = null;
        }
        if (this.mineIgnorePlacedPos != null) return;
        if (var1.getTicksRemaining() > this.mineIgnoreTicks.getValue().intValue()) return;
        mineIgnoreTryPlace(var2);
    }

    private void mineIgnoreTryPlace(BlockPos arg0) {
        if (shouldPause("Place")) return;
        if (!(this.placeTimer.hasTimeElapsed((1000.0f - (this.placeSpeed.getValue().floatValue() * 50.0f))))) return;
        Object var2 = selectTargets(getPlayers());
        if (var2.isEmpty()) return;
        int var3 = 0;
        Object var4 = var2.iterator();
        label_3:
        if (!(var4.hasNext())) break label_6;
        Object var5 = ((Player) var4.next());
        if (var5.equals(mc.player)) {
            break label_3;
        }
        Object var6 = var5.blockPosition();
        if ((Math.abs((arg0.getX() - var6.getX())) > 1) || (Math.abs((arg0.getZ() - var6.getZ())) > 1)) break label_3;
        if (Math.abs((arg0.getY() - var6.getY())) > 1) {
            break label_3;
        }
        var3 = 1;
        break label_6;
        label_6:
        if (!var3) return;
        var4 = new ArrayList();
        var4.add(arg0.above());
        var5 = Direction.Plane.HORIZONTAL.iterator();
        while (var5.hasNext()) {
            var6 = ((Direction) var5.next());
            var4.add(arg0.relative(var6).above());
        }
        var5 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
        var6 = mc.player.getInventory().getSelectedSlot();
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None")) && var5 == -1 && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) return;
        Object var7 = var4.iterator();
        label_11:
        if (var7.hasNext()) {
            Object var8 = ((BlockPos) var7.next());
            Object var12 = var8.below();
            if (mc.level.getBlockState(var12).getBlock() != Blocks.OBSIDIAN) {
                if (mc.level.getBlockState(var12).getBlock() != Blocks.BEDROCK) break label_11;
            }
            if ((!(mc.level.getBlockState(var8).isAir())) || (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var8)) > Mth.square(this.placeRange.getValue().doubleValue()))) break label_11;
            if (!(mc.level.getWorldBorder().isWithinBounds(var8))) {
                break label_11;
            }
            if (!(this.raytrace.getValue())) {
                if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(var8)) > Mth.square(this.placeWallsRange.getValue().doubleValue())) {
                }
            } else {
            }
            int var11 = 0;
            int var13 = 0;
            if (!(!var11)) {
                if (!(WorldUtils.canSeeBlock(var12))) break label_11;
            }
            IF (!(mc.level.getEntities(((Entity) null), new AABB(var8), (arg0) -> true).stream().anyMatch((arg0) -> {
    if ((!(arg0.isAlive())) || ((arg0 instanceof ExperienceOrb))) break label_0;
    return !((arg0 instanceof EndCrystal));
}))) break label_18;
            break label_11;
            label_18:
            Object var14 = new AABB(((double) (var8.getX() - 1)), ((double) var8.getY()), ((double) (var8.getZ() - 1)), ((double) (var8.getX() + 2)), ((double) (var8.getY() + 2)), ((double) (var8.getZ() + 2)));
            if (LightningClient.WORLD_MANAGER.getReservedPlacements().stream().anyMatch((arg1) -> arg0.intersects(new AABB(arg1)))) break label_11;
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_19;
            var tmp0 = DamageUtils.getCrystalDamage(mc.player, null, var8, null, this.ignoreTerrain.getValue());
            float var10 = tmp0;
            if (tmp0 > this.maximumSelfDamage.getValue().floatValue()) break label_11;
            if ((!(this.antiSuicide.getValue())) || (var10 <= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))) break label_19;
            break label_11;
            label_19:
            int var15 = 0;
            Object var16 = var2.iterator();
            label_20:
            if (!(var16.hasNext())) break label_22;
            Object var17 = ((Player) var16.next());
            if (var17.equals(mc.player)) break label_20;
            var tmp1 = DamageUtils.getCrystalDamage(var17, PositionUtils.extrapolate(var17, this.extrapolation.getValue().intValue()), var8, arg0, this.ignoreTerrain.getValue());
            float var18 = tmp1;
            if (tmp1 < getMinimumDamage(var17, this.minimumDamage.getValue().floatValue())) {
                break label_20;
            }
            var15 = 1;
            break label_22;
            label_22:
            if (!var15) {
                break label_11;
            }
            var16 = CrystalPlacementHelper.getVisiblePlacement(var8);
            if (var16 != null && var16.hitVec != null) {
            } else {
            }
            var tmp2 = Vec3.atCenterOf(var8).add(0.0, 0.5, 0.0);
            Object var9 = tmp2;
            var17 = tmp2;
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                LightningClient.ROTATION_MANAGER.legacyRotate(calculateRotations(var9), LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                }
                LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(var9));
            }
            var18 = 0;
            if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
                if (this.autoSwitch.getValue().equalsIgnoreCase("Normal") && this.swapBack.getValue() && this.savedSlot == -1) {
                    this.savedSlot = var6;
                }
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), var5, var6);
                var18 = 1;
            }
            place(var8);
            if (!(!var18)) {
                InventoryUtils.switchBack(this.autoSwitch.getValue(), var5, var6);
            }
            this.mineIgnorePlacedPos = var8;
            return;
        }
    }

    private void mineIgnoreDetonate() {
        if (shouldPause("Attack")) return;
        Object var1 = this.mineIgnorePlacedPos;
        this.mineIgnoreMinedPos = null;
        this.mineIgnorePlacedPos = null;
        if (var1 == null) return;
        if (!(this.attack.getValue())) return;
        Object var2 = mc.level.entitiesForRendering().iterator();
        label_3:
        if (var2.hasNext()) {
            Object var3 = ((Entity) var2.next());
            if (!((var3 instanceof EndCrystal))) break label_3;
            var tmp0 = ((EndCrystal) var3);
            Object var4 = tmp0;
            if (!(tmp0.isAlive())) break label_3;
            if (!(var4.blockPosition().below().equals(var1))) {
                break label_3;
            }
            if (this.rotate.getValue().equalsIgnoreCase("Normal")) {
                LightningClient.ROTATION_MANAGER.legacyRotate(calculateRotations(Vec3.atCenterOf(var4.blockPosition())), LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
            }
            if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                }
                LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(Vec3.atCenterOf(var4.blockPosition())));
            }
            attack(var4);
            return;
        }
    }

    private List getPlayers() {
        if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) return List.of(mc.player);
        long var1 = System.currentTimeMillis();
        if ((var1 - this.lastPlayerCacheTime) < 50L) return this.cachedPlayers;
        Object var3 = new ArrayList();
        Object var4 = mc.level.players().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Player) var4.next());
            if ((((var5 == mc.player) || (EntityUtils.isGhost(var5))) || (!(var5.isAlive()))) || (mc.player.distanceToSqr(var5) > Mth.square(this.enemyRange.getValue().doubleValue()))) break label_2;
            if (LightningClient.FRIEND_MANAGER.contains(var5.getName().getString())) {
                break label_2;
            }
            if (var5.getMainHandItem().getItem() != Items.END_CRYSTAL) {
                if (var5.getOffhandItem().getItem() == Items.END_CRYSTAL) {
                }
            } else {
            }
            int var6 = 0;
            int var7 = 0;
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(var5) && !var6) {
                break label_2;
            }
            var3.add(var5);
        }
        this.cachedPlayers = var3;
        this.lastPlayerCacheTime = var1;
        return var3;
    }

    private List selectTargets(List arg0) {
        if ((arg0.size() <= 1) || (this.targetMode.getValue().equalsIgnoreCase("All"))) {
            return arg0;
        }
        Object var2 = null;
        double var3 = 0.0;
        Object var5 = arg0.iterator();
        while (var5.hasNext()) {
            Object var6 = ((Player) var5.next());
            Object var9 = this.targetMode.getValue();
            int var10 = -1;
            switch (var9.hashCode()) {
                case -804534210 -> { break label_3; }
                case 1005233659 -> { break label_4; }
                default -> { break label_5; }
            }
            label_3:
            if (!(var9.equals("Nearest"))) break label_5;
            var10 = 0;
            break label_5;
            label_4:
            if (!(var9.equals("Farthest"))) break label_5;
            var10 = 1;
            label_5:
            switch (var10) {
                case 0 -> { break label_6; }
                case 1 -> { break label_7; }
                default -> { break label_8; }
            }
            label_6:
            double var7 = -(mc.player.distanceToSqr(var6));
            break label_9;
            label_7:
            var7 = mc.player.distanceToSqr(var6);
            break label_9;
            label_8:
            var7 = ((double) -((var6.getHealth() + var6.getAbsorptionAmount())));
            double var11 = ((double) -((var6.getHealth() + var6.getAbsorptionAmount())));
            label_9:
            if (var2 != null && var7 <= var3) {
                break label_2;
            }
            var2 = var6;
            var3 = var7;
        }
        return List.of(var2);
    }

    private boolean shouldPause(String arg0) {
        if (isPearlPaused()) return true;
        if (isXpPaused()) return true;
        if (this.awaitMine.getValue() && pauseForAutoMine()) return true;
        if (this.whileEating.getValue().equalsIgnoreCase("None")) break label_4;
        if (arg0.equalsIgnoreCase("Attack")) {
            if (this.whileEating.getValue().equalsIgnoreCase("Place")) break label_4;
        }
        if (arg0.equalsIgnoreCase("Place") && this.whileEating.getValue().equalsIgnoreCase("Attack")) {
            label_4:
        } else {
        }
        int var2 = 0;
        if (!var2) break label_7;
        return EntityUtils.isEating();
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

    private boolean executeBasePlace() {
        if (isDead()) return false;
        if (!(this.basePlace.getValue())) return false;
        if (shouldPause("Place")) return false;
        Object var4 = this.target;
        if (var4 != null) break label_5;
        double var5 = 1.7976931348623157E308;
        Object var7 = mc.level.players().iterator();
        while (var7.hasNext()) {
            Object var8 = ((Player) var7.next());
            if ((((EntityUtils.isGhost(var8)) || (var8 == mc.player)) || (!(var8.isAlive()))) || (LightningClient.FRIEND_MANAGER.contains(var8.getName().getString()))) break label_3;
            double var9 = mc.player.distanceToSqr(var8);
            if (mc.player.distanceToSqr(var8) > Mth.square(this.enemyRange.getValue().doubleValue())) break label_3;
            if (var9 < var5) break label_4;
            break label_3;
            label_4:
            var5 = var9;
            var4 = var8;
        }
        label_5:
        if (var4 == null) return false;
        var5 = this.placeTarget;
        if (var5 != null && var5.getPosition() != null) {
        } else {
        }
        float var3 = 0.0f;
        float var6 = 0.0f;
        if (var3 >= (var4.getHealth() + var4.getAbsorptionAmount())) return false;
        var7 = PositionUtils.getFlooredPosition(var4);
        var8 = this.basePlaceRadius.getValue().doubleValue();
        int var10 = ((int) Math.ceil(var8));
        int var11 = (var7.getY() - 1);
        int var12 = (var7.getY() - 3);
        Object var13 = new LinkedHashSet();
        int var14 = var11;
        while (var14 >= var12) {
            int var15 = -(var10);
            while (var15 <= var10) {
                int var16 = -(var10);
                while (var16 <= var10) {
                    if (((double) ((var15 * var15) + (var16 * var16))) > (var8 * var8)) {
                    } else {
                        var13.add(new BlockPos((var7.getX() + var15), var14, (var7.getZ() + var16)));
                    }
                    var16++;
                }
                var15++;
            }
            var14--;
        }
        var14 = null;
        var15 = 0.0f;
        var16 = 1.7976931348623157E308;
        Object var18 = var13.iterator();
        while (var18.hasNext()) {
            Object var19 = ((BlockPos) var18.next());
            if (!(isBaseCandidateValid(var19))) break label_18;
            var tmp0 = mc.level.getBlockState(var19);
            Object var24 = tmp0;
            if ((tmp0.getBlock() == Blocks.OBSIDIAN) || (var24.getBlock() == Blocks.BEDROCK)) break label_18;
            if (((SuicideModule) LightningClient.MODULE_MANAGER.getModule(SuicideModule.class)).isToggled()) break label_19;
            var tmp1 = DamageUtils.getCrystalDamage(mc.player, PositionUtils.extrapolate(mc.player, this.extrapolation.getValue().intValue()), var19, null, this.ignoreTerrain.getValue());
            float var23 = tmp1;
            if (tmp1 > this.maximumSelfDamage.getValue().floatValue()) break label_18;
            if (!(this.antiSuicide.getValue())) break label_19;
            if (var23 > (mc.player.getHealth() + mc.player.getAbsorptionAmount())) break label_18;
            label_19:
            var tmp2 = DamageUtils.getCrystalDamage(var4, PositionUtils.extrapolate(var4, this.extrapolation.getValue().intValue()), var19, null, this.ignoreTerrain.getValue());
            float var22 = tmp2;
            if (tmp2 < this.basePlaceMinDamage.getValue().floatValue()) break label_18;
            if (var22 < getMinimumDamage(var4, this.minimumDamage.getValue().floatValue()) && var22 < (var4.getHealth() + var4.getAbsorptionAmount())) {
                if ((var22 * (1.0f + this.lethalMultiplier.getValue().floatValue())) < (var4.getHealth() + var4.getAbsorptionAmount())) break label_18;
            }
            if (var3 > 0.0f) {
                if (var22 < (var3 * this.basePlaceRatio.getValue().floatValue())) break label_18;
            }
            double var20 = (((((double) -(var22)) * 10.0) + mc.player.distanceToSqr(Vec3.atCenterOf(var19))) + var4.distanceToSqr(Vec3.atCenterOf(var19)));
            if ((((((double) -(var22)) * 10.0) + mc.player.distanceToSqr(Vec3.atCenterOf(var19))) + var4.distanceToSqr(Vec3.atCenterOf(var19))) >= var16) {
                break label_18;
            }
            var16 = var20;
            var14 = var19;
            var15 = var22;
        }
        if (var14 == null) return false;
        if (this.basePlaceSwitch.getValue().equalsIgnoreCase("None")) {
        } else {
            if (!(this.basePlaceSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
                if (this.basePlaceSwitch.getValue().equalsIgnoreCase("AltPickup")) {
                }
            } else {
            }
        }
        int var2 = InventoryUtils.find(0, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
        if (var2 == -1) return false;
        var18 = WorldUtils.getDirection(var14, this.basePlaceStrictDirection.getValue());
        if (var18 == null && !(this.basePlaceAirPlace.getValue())) return false;
        var19 = mc.player.getInventory().getSelectedSlot();
        if (!(InventoryUtils.switchSlot(this.basePlaceSwitch.getValue(), var2, var19))) return false;
        if (var18 != null) {
            if (!(this.renderMode.getValue().equalsIgnoreCase("Both"))) {
                if (this.renderMode.getValue().equalsIgnoreCase("Fill")) {
                }
            } else {
            }
            int var1 = WorldUtils.placeBlock(var18, InteractionHand.MAIN_HAND, (this.basePlaceRotate.getValue() ? "Normal" : "None"), 1, true, false);
        } else {
            if (!(this.renderMode.getValue().equalsIgnoreCase("Both"))) {
                if (this.renderMode.getValue().equalsIgnoreCase("Fill")) {
                }
            } else {
            }
            WorldUtils.airPlaceBlock(InteractionHand.MAIN_HAND, (this.basePlaceRotate.getValue() ? "Normal" : "None"), 1, false);
            var1 = 1;
        }
        InventoryUtils.switchBack(this.basePlaceSwitch.getValue(), var2, var19);
        if (!(!var1)) {
            this.placeTarget = new AutoCrystalModule.PlaceTarget(var14, var4, Collections.emptyList(), null, var15, 1);
            if (!(this.place.getValue())) break label_55;
            int var21 = InventoryUtils.findHotbar(Items.END_CRYSTAL);
            var22 = mc.player.getInventory().getSelectedSlot();
            if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL) {
                if (var21 != -1 && !(this.autoSwitch.getValue().equalsIgnoreCase("None"))) {
                }
            } else {
            }
            var20 = 0;
            var23 = 0;
            if (!var20) break label_55;
            Object var25 = CrystalPlacementHelper.getVisiblePlacement(var14);
            if (var25 != null && var25.hitVec != null) {
            } else {
            }
            var tmp3 = Vec3.atCenterOf(var14).add(0.0, 0.5, 0.0);
            var24 = tmp3;
            Object var26 = tmp3;
            if (!(this.rotate.getValue().equalsIgnoreCase("Normal"))) {
                if (this.basePlaceRotate.getValue()) {
                }
                LightningClient.ROTATION_MANAGER.legacyRotate(calculateRotations(var24), LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                break label_53;
            }
            if (!(this.rotate.getValue().equalsIgnoreCase("Packet"))) {
                if (!(this.rotate.getValue().equalsIgnoreCase("Silent"))) break label_53;
            }
            LightningClient.ROTATION_MANAGER.wireRotate(this.rotate.getValue(), RotationUtils.getRotations(var24));
            label_53:
            int var27 = 0;
            if (mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL && var21 != -1) {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), var21, var22);
                var27 = 1;
            }
            place(var14);
            if (!var27) break label_55;
            InventoryUtils.switchBack(this.autoSwitch.getValue(), var21, var22);
            label_55:
            LightningClient.RENDER_MANAGER.setRenderPosition(var14);
        }
        return var1;
    }

    private boolean isBaseCandidateValid(BlockPos arg0) {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        if (mc.level.isOutsideBuildHeight(arg0)) return false;
        if (mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) > Mth.square(this.basePlaceRange.getValue().doubleValue())) return false;
        if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0)) > Mth.square(this.placeRange.getValue().doubleValue())) return false;
        if (!(this.raytrace.getValue())) {
            if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0)) > Mth.square(this.placeWallsRange.getValue().doubleValue())) {
            }
        } else {
        }
        int var3 = 0;
        int var4 = 0;
        if (!(!var3) && !(WorldUtils.canSeeBlock(arg0))) return false;
        Object var5 = mc.level.getBlockState(arg0);
        if (!(var5.canBeReplaced())) return false;
        if (!(WorldUtils.isPlaceable(arg0))) return false;
        if (!(this.basePlaceAirPlace.getValue()) && WorldUtils.getDirection(arg0, this.basePlaceStrictDirection.getValue()) == null) return false;
        Object var6 = arg0.above();
        Object var7 = mc.level.getBlockState(var6);
        if ((!(var7.canBeReplaced())) || (!(mc.level.getFluidState(var6).isEmpty()))) {
            return false;
        }
        if (this.placements.getValue().equalsIgnoreCase("Protocol")) {
            var tmp0 = mc.level.getBlockState(arg0.above(2));
            Object var2 = tmp0;
            if (!(tmp0.canBeReplaced())) return false;
        }
        return mc.level.getEntities(((Entity) null), getCrystalPlacementBox(arg0), (arg0) -> {
    if ((!(arg0.isAlive())) || ((arg0 instanceof ExperienceOrb))) break label_0;
    return !((arg0 instanceof EndCrystal));
}).isEmpty();
    }

    private AABB getCrystalPlacementBox(BlockPos arg0) {
        return new AABB(arg0.offset(0, 1, 0));
    }

    public float getBestPlaceDamage() {
        if (this.placeTarget == null) {
        } else {
        }
        return this.placeTarget.getDamage();
    }

    @Generated
    public Player getTarget() {
        return this.target;
    }

    @Generated
    public String getCalculationDamage() {
        return this.calculationDamage;
    }

    static {
        SHADER_MODES = EspShader.MODES;
        SHADER_ACTIVE_MODES = ((String[]) Arrays.copyOfRange(SHADER_MODES, 1, SHADER_MODES.length));
    }


    public class PlaceTarget {
        private BlockPos position;
        private Player player;
        private List obstructions;
        private BlockPos exception;
        private float damage;
        private int calculations;

        public AutoCrystalModule.PlaceTarget clone() {
            return new AutoCrystalModule.PlaceTarget(this.position, this.player, this.obstructions, this.exception, this.damage, this.calculations);
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
