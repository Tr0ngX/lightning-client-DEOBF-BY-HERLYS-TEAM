package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "BedAura", description = "Automatically places and detonates beds in the Nether and End.", category = Module.Category.COMBAT)
public class BedAuraModule extends Module {
    public CategorySetting targetingCategory;
    public ModeSetting targetMode;
    public NumberSetting targetRange;
    public NumberSetting maxTargets;
    public NumberSetting bedRange;
    public BooleanSetting ignoreFriends;
    public CategorySetting damageCategory;
    public NumberSetting minDamage;
    public NumberSetting maxSelfDamage;
    public NumberSetting minHealth;
    public BooleanSetting antiSuicide;
    public NumberSetting balance;
    public BooleanSetting predict;
    public CategorySetting behaviorCategory;
    public NumberSetting delay;
    public NumberSetting range;
    public NumberSetting wallsRange;
    public ModeSetting swapMode;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting airPlace;
    public BooleanSetting placeOnFeet;
    public BooleanSetting pauseOnEat;
    public CategorySetting autoCraftCategory;
    public BooleanSetting autoCraft;
    public BindSetting autoCraftBind;
    public BooleanSetting autoPlaceTable;
    public BooleanSetting autoCloseTable;
    public CategorySetting renderCategory;
    public BooleanSetting renderPreview;
    public BooleanSetting interpolate;
    public ModeSetting renderMode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private static final int RECOMPUTE_INTERVAL_TICKS = 10;
    private int recomputeTicks;
    private BlockPos currentFoot;
    private Direction currentDir;
    private Player currentTarget;
    private float currentEstimatedDamage;
    private BlockPos currentHead;
    private boolean currentAdopt;
    private long lastActionMs;
    private boolean bedPlacedThisCycle;
    private BlockPos placedBedPos;
    private BlockPos placedBedHeadPos;
    private BedAuraModule.PlacementCandidate currentPlacement;
    private final List targets;
    private long placementRenderMs;
    private static final long PLACEMENT_ANIM_MS = 350L;
    private BlockPos lastRenderedPos;
    private boolean autoCraftRunning;
    private int autoCraftTicks;
    private BlockPos placedTablePos;
    private boolean tableOpenSent;
    private boolean lastHadBeds;
    private static final int CRAFTING_RESULT_SLOT = 0;
    private static final int[] WOOL_GRID_SLOTS;
    private static final int[] PLANK_GRID_SLOTS;
    private static final int CRAFTING_CONTAINER_SIZE = 10;

    public BedAuraModule() {
        this.targetingCategory = new CategorySetting("Targeting", "Settings related to target selection.");
        var tmp0 = new String[3];
        tmp0[0] = "Closest";
        tmp0[1] = "Furthest";
        tmp0[2] = "Health";
        this.targetMode = new ModeSetting("Target", "Mode", "How targets are sorted.", new CategorySetting.Visibility(this.targetingCategory), "Closest", tmp0);
        this.targetRange = new NumberSetting("TargetRange", "TargetRange", "The range within which players are considered targets.", new CategorySetting.Visibility(this.targetingCategory), 8.0, 1.0, 16.0);
        this.maxTargets = new NumberSetting("MaxTargets", "MaxTargets", "Maximum number of targets to evaluate simultaneously.", new CategorySetting.Visibility(this.targetingCategory), 3.0, 1.0, 8.0);
        this.bedRange = new NumberSetting("BedRange", "BedRange", "Search cube radius around the target to look for placement spots.", new CategorySetting.Visibility(this.targetingCategory), 4.0, 1.0, 8.0);
        this.ignoreFriends = new BooleanSetting("IgnoreFriends", "Ignore friends when targeting.", new CategorySetting.Visibility(this.targetingCategory), true);
        this.damageCategory = new CategorySetting("Damage", "Damage calculation and safety settings.");
        this.minDamage = new NumberSetting("MinDamage", "MinDamage", "Minimum predicted damage to target.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
        this.maxSelfDamage = new NumberSetting("MaxSelfDamage", "MaxSelfDamage", "Maximum allowed damage to yourself.", new CategorySetting.Visibility(this.damageCategory), 6.0, 0.0, 36.0);
        this.minHealth = new NumberSetting("MinHealth", "MinHealth", "Do not act if your health (+absorption) is at or below this.", new CategorySetting.Visibility(this.damageCategory), 4.0, 0.0, 36.0);
        this.antiSuicide = new BooleanSetting("AntiSuicide", "Cancel if self damage drops health below MinHealth.", new CategorySetting.Visibility(this.damageCategory), true);
        this.balance = new NumberSetting("Balance", "Balance", "Minimum target to self damage ratio (0 = off).", new CategorySetting.Visibility(this.damageCategory), 0.0, 0.0, 10.0);
        this.predict = new BooleanSetting("Predict", "Extrapolate target velocity for damage estimation.", new CategorySetting.Visibility(this.damageCategory), true);
        this.behaviorCategory = new CategorySetting("Behavior", "Settings for placement and detonation behavior.");
        this.delay = new NumberSetting("Delay", "Delay", "Delay in milliseconds between actions.", new CategorySetting.Visibility(this.behaviorCategory), 100, 0, 500);
        this.range = new NumberSetting("Range", "Range", "Direct reach for placing and detonating beds.", new CategorySetting.Visibility(this.behaviorCategory), 4.5, 1.0, 6.0);
        this.wallsRange = new NumberSetting("WallsRange", "WallsRange", "Reach through walls / obstacles.", new CategorySetting.Visibility(this.behaviorCategory), 4.0, 0.0, 6.0);
        var tmp1 = new String[3];
        tmp1[0] = "Silent";
        tmp1[1] = "Normal";
        tmp1[2] = "Alt";
        this.swapMode = new ModeSetting("SwapMode", "SwapMode", "How to swap to the bed and non-bed item.", new CategorySetting.Visibility(this.behaviorCategory), "Silent", tmp1);
        this.rotate = new BooleanSetting("Rotate", "Rotate towards bed before placing and detonating.", new CategorySetting.Visibility(this.behaviorCategory), true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Enforce strict raycast line of sight on placement face.", new CategorySetting.Visibility(this.behaviorCategory), false);
        this.airPlace = new BooleanSetting("AirPlace", "Allow placing beds without a solid supporting block below.", new CategorySetting.Visibility(this.behaviorCategory), false);
        this.placeOnFeet = new BooleanSetting("PlaceOnFeet", "Allow bed head to overlap target entity hitbox for maximum damage.", new CategorySetting.Visibility(this.behaviorCategory), true);
        this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or consuming items.", new CategorySetting.Visibility(this.behaviorCategory), true);
        this.autoCraftCategory = new CategorySetting("AutoCraft", "Automated bed crafting from wool and planks.");
        this.autoCraft = new BooleanSetting("AutoCraft", "Enable automated bed crafting.", new CategorySetting.Visibility(this.autoCraftCategory), true);
        this.autoCraftBind = new BindSetting("AutoCraftBind", "Keybind to toggle AutoCraft.", new CategorySetting.Visibility(this.autoCraftCategory), -1).disableHoldModes();
        this.autoPlaceTable = new BooleanSetting("AutoPlace", "Place and open a crafting table when out of beds.", new CategorySetting.Visibility(this.autoCraftCategory), true);
        this.autoCloseTable = new BooleanSetting("AutoClose", "Close the crafting table GUI automatically when crafting is finished.", new CategorySetting.Visibility(this.autoCraftCategory), true);
        this.renderCategory = new CategorySetting("Render", "Placement preview rendering.");
        this.renderPreview = new BooleanSetting("Render", "Render preview box at target bed placement.", new CategorySetting.Visibility(this.renderCategory), true);
        this.interpolate = new BooleanSetting("Interpolate", "Smooth linear grow-in animation for preview box.", new CategorySetting.Visibility(this.renderCategory), true);
        var tmp2 = new String[3];
        tmp2[0] = "Fill";
        tmp2[1] = "Outline";
        tmp2[2] = "Both";
        this.renderMode = new ModeSetting("RenderMode", "Mode", "Render mode for preview box.", new CategorySetting.Visibility(this.renderCategory), "Both", tmp2);
        this.fillColor = new ColorSetting("FillColor", "FillColor", "Color used for fill rendering.", new CategorySetting.Visibility(this.renderCategory), ColorUtils.getDefaultFillColor());
        this.outlineColor = new ColorSetting("OutlineColor", "OutlineColor", "Color used for outline rendering.", new CategorySetting.Visibility(this.renderCategory), ColorUtils.getDefaultOutlineColor());
        this.recomputeTicks = 0;
        this.currentFoot = null;
        this.currentDir = null;
        this.currentTarget = null;
        this.currentEstimatedDamage = 0.0f;
        this.currentHead = null;
        this.currentAdopt = false;
        this.lastActionMs = 0L;
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.currentPlacement = null;
        this.targets = new ArrayList();
        this.placementRenderMs = 0L;
        this.lastRenderedPos = null;
        this.autoCraftRunning = false;
        this.autoCraftTicks = 0;
        this.placedTablePos = null;
        this.tableOpenSent = false;
        this.lastHadBeds = true;
    }

    public void onEnable() {
        clearSelection();
        this.recomputeTicks = 0;
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.autoCraftRunning = false;
        this.placedTablePos = null;
        this.tableOpenSent = false;
    }

    public void onDisable() {
        clearSelection();
        this.autoCraftRunning = false;
        this.placedTablePos = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.autoCraft.getValue() && this.autoPlaceTable.getValue()) {
            var tmp0 = (countBeds() > 0 ? 1 : 0);
            int var2 = tmp0;
            int var3 = tmp0;
            if (!var2 && !(!this.lastHadBeds)) {
                startAutoCraft();
            }
            this.lastHadBeds = var2;
        }
        if (!(!this.autoCraftRunning)) {
            handleAutoCraftTick();
        }
        runBedCycle();
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent arg0) {
        if (mc.gui.screen() != null) return;
        if (this.autoCraftBind.getValue() != -1 && arg0.getKey() == this.autoCraftBind.getValue()) {
            startAutoCraft();
            LightningClient.CHAT_MANAGER.tagged("AutoCraft triggered!", "BedAura");
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent arg0) {
        if (mc.gui.screen() != null) return;
        if (this.autoCraftBind.getValue() != -1 && arg0.getButton() == this.autoCraftBind.getValue()) {
            startAutoCraft();
            LightningClient.CHAT_MANAGER.tagged("AutoCraft triggered!", "BedAura");
        }
    }

    private void runBedCycle() {
        Object var2 = mc.player.level().dimension();
        if (var2 != Level.NETHER && var2 != Level.END) {
            clearSelection();
            return;
        }
        if ((!(this.pauseOnEat.getValue())) || (!(mc.player.isUsingItem()))) break label_2;
        if (mc.player.getUseItem().has(DataComponents.FOOD)) break label_1;
        if (!(mc.player.getUseItem().has(DataComponents.POTION_CONTENTS))) break label_2;
        label_1:
        return;
        label_2:
        float var3 = (mc.player.getHealth() + mc.player.getAbsorptionAmount());
        if (var3 <= this.minHealth.getValue().floatValue()) return;
        long var4 = System.currentTimeMillis();
        if ((var4 - this.lastActionMs) < this.delay.getValue().longValue()) return;
        if (!(!this.bedPlacedThisCycle)) {
            submitDetonate();
            return;
        }
        var tmp0 = (findBedSlot() != -1 ? 1 : 0);
        int var1 = tmp0;
        int var6 = tmp0;
        if (!var1) {
            if (this.autoCraft.getValue() && !(countBeds()) && !this.autoCraftRunning) {
                startAutoCraft();
            }
            clearSelection();
            return;
        }
        updateTargets();
        if (this.targets.isEmpty()) {
            clearSelection();
            return;
        }
        if (this.recomputeTicks <= 0) {
            this.recomputeTicks = 10;
            Object var7 = findExistingBed();
            if (var7 != null) {
                this.currentFoot = var7.pos();
                this.currentHead = findOtherBedHalf(var7.pos());
                this.currentDir = null;
                this.currentTarget = var7.target();
                this.currentAdopt = true;
                this.currentEstimatedDamage = var7.damage();
                this.bedPlacedThisCycle = true;
                this.placedBedPos = this.currentFoot;
                this.placedBedHeadPos = this.currentHead;
                this.currentPlacement = null;
                submitDetonate();
                return;
            }
            Object var8 = searchPlacement();
            if (var8 == null) {
                clearSelection();
                return;
            }
            this.currentFoot = var8.foot();
            this.currentDir = var8.dir();
            this.currentTarget = var8.target();
            this.currentEstimatedDamage = var8.estimatedDamage();
            this.currentAdopt = false;
            this.currentHead = this.currentFoot.relative(this.currentDir);
            break label_14;
        }
        this.recomputeTicks = (this.recomputeTicks - 1);
        if (((this.currentFoot == null) || (this.currentAdopt)) || (currentSelectionStillGood())) break label_14;
        this.recomputeTicks = 0;
        label_14:
        if (this.currentFoot != null && this.currentDir != null) {
            if (this.currentTarget == null) {
            }
            clearSelection();
            return;
        }
        this.currentPlacement = new BedAuraModule.PlacementCandidate(this.currentFoot, this.currentDir, this.currentEstimatedDamage);
        submitPlaceBed(this.currentFoot, this.currentDir);
    }

    private void submitPlaceBed(BlockPos arg0, Direction arg1) {
        Object var3 = computeBedHit(arg0);
        if (var3 == null) {
            clearSelection();
            this.recomputeTicks = 4;
            return;
        }
        float var4 = arg1.toYRot();
        Object var5 = RotationUtils.getRotations(var3.getLocation());
        float var6 = var5[1];
        LightningClient.ROTATION_MANAGER.silentRotate(var4, var6);
        if (executePlaceBed(arg0, var3)) {
            this.bedPlacedThisCycle = true;
            this.placedBedPos = arg0;
            this.placedBedHeadPos = arg0.relative(arg1);
            this.lastActionMs = System.currentTimeMillis();
        }
    }

    private void submitDetonate() {
        if (this.placedBedPos == null) {
            this.bedPlacedThisCycle = false;
            return;
        }
        Object var3 = this.placedBedPos;
        if ((this.placedBedHeadPos == null) || (this.currentTarget == null)) break label_1;
        float var4 = computeDamage(Vec3.atCenterOf(this.placedBedPos), this.currentTarget);
        float var5 = computeDamage(Vec3.atCenterOf(this.placedBedHeadPos), this.currentTarget);
        if (var5 <= var4) break label_1;
        var3 = this.placedBedHeadPos;
        label_1:
        var tmp0 = computeBedFace(var3);
        Object var2 = tmp0;
        if (tmp0 == null) {
            this.bedPlacedThisCycle = false;
            this.placedBedPos = null;
            this.placedBedHeadPos = null;
            this.currentPlacement = null;
            this.recomputeTicks = 4;
            return;
        }
        var4 = findDetonateSlot();
        var5 = RotationUtils.getRotations(var2.getLocation());
        LightningClient.ROTATION_MANAGER.silentRotate(var5[0], var5[1]);
        int var6 = mc.player.getInventory().getSelectedSlot();
        int var7 = 0;
        if (this.swapMode.getValue().equalsIgnoreCase("Alt")) {
        } else {
        }
        var tmp1 = this.swapMode.getValue();
        Object var1 = tmp1;
        Object var8 = tmp1;
        if (var4 != -2147483648) {
            var7 = InventoryUtils.switchSlot(var1, var4, var6);
        }
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, var2);
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        if (!var7) break label_10;
        InventoryUtils.switchBack(var1, var4, var6);
        break label_10;
        Object var9 = "AltPickup";
        if (!(!var7)) {
            InventoryUtils.switchBack(var1, var4, var6);
        }
        throw var9;
        label_10:
        this.bedPlacedThisCycle = false;
        this.placedBedPos = null;
        this.placedBedHeadPos = null;
        this.currentPlacement = null;
        this.lastActionMs = System.currentTimeMillis();
    }

    private boolean executePlaceBed(BlockPos arg0, BlockHitResult arg1) {
        int var6 = findBedSlot();
        if (var6 == -1) return false;
        if (!(this.swapMode.getValue().equalsIgnoreCase("Alt"))) break label_1;
        break label_2;
        label_1:
        label_2:
        Object var5 = this.swapMode.getValue();
        var tmp0 = mc.player.getInventory().getSelectedSlot();
        int var4 = tmp0;
        int var3 = InventoryUtils.switchSlot(var5, var6, tmp0);
        if (!var3 && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
        }
        if (!(WorldUtils.isInteractable(mc.level.getBlockState(arg1.getBlockPos())))) break label_4;
        var tmp1 = (!(mc.player.isShiftKeyDown()) ? 1 : 0);
        int var8 = tmp1;
        int var9 = tmp1;
        if (!var8) break label_8;
        if (mc.player.input != null) {
        } else {
        }
        Object var7 = new Input(false, false, false, false, false, false, false);
        Object var10 = new Input(var7.forward(), var7.backward(), var7.left(), var7.right(), var7.jump(), true, var7.sprint());
        mc.getConnection().send(new ServerboundPlayerInputPacket(var10));
        Object var11 = mc.player;
        if (!((var11 instanceof ClientPlayerEntityAccessor))) break label_8;
        Object var12 = ((ClientPlayerEntityAccessor) var11);
        var12.setLastSentInput(var10);
        try {
            label_8:
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, arg1);
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            if (!var8) {
                break label_20;
                try {
                }
                if (mc.player.input != null) {
                } else {
                }
                var7 = new Input(false, false, false, false, false, false, false);
            } catch (Throwable ex14) {
                var10 = ex14;
                if (!var8) break label_17;
                if (mc.player.input != null) {
                } else {
                }
                var11 = new Input(false, false, false, false, false, false, false);
                mc.getConnection().send(new ServerboundPlayerInputPacket(var11));
                var12 = mc.player;
                if (!((var12 instanceof ClientPlayerEntityAccessor))) break label_17;
                Object var13 = ((ClientPlayerEntityAccessor) var12);
                var13.setLastSentInput(var11);
                label_17:
                if (!(!var3)) {
                    InventoryUtils.switchBack(var5, var6, var4);
                }
                throw var10;
            }
            mc.getConnection().send(new ServerboundPlayerInputPacket(var7));
            var10 = mc.player;
            if (!((var10 instanceof ClientPlayerEntityAccessor))) break label_20;
            var11 = ((ClientPlayerEntityAccessor) var10);
            var11.setLastSentInput(var7);
            label_20:
            if (!(!var3)) {
                InventoryUtils.switchBack(var5, var6, var4);
            }
            return true;
        }
    }

    private boolean canSeePlacementFace(Vec3 arg0, Vec3 arg1, BlockPos arg2) {
        Object var4 = mc.level.clip(new ClipContext(arg0, arg1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var4.getType() == HitResult.Type.MISS) return true;
        if ((var4 instanceof BlockHitResult)) {
            Object var5 = var4;
            return var5.getBlockPos().equals(arg2);
        }
        return false;
    }

    private BlockHitResult computeBedHit(BlockPos arg0) {
        double var2 = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!(withinReach(arg0, var2))) return null;
        Object var4 = arg0.below();
        int var5 = (!(mc.level.getBlockState(var4).canBeReplaced()) ? 1 : 0);
        Object var6 = mc.player.getEyePosition();
        if (!(!var5)) {
            Object var7 = new Vec3((((double) arg0.getX()) + 0.5), ((double) arg0.getY()), (((double) arg0.getZ()) + 0.5));
            if (this.strictDirection.getValue()) {
                if ((var6.y < ((double) arg0.getY())) || (!(canSeePlacementFace(var6, var7, var4)))) {
                    return null;
                }
            }
            return new BlockHitResult(var7, Direction.UP, var4, false);
        }
        if (!(this.airPlace.getValue())) return null;
        var7 = Direction.values();
        int var8 = var7.length;
        int var9 = 0;
        while (var9 < var8) {
            Object var10 = var7[var9];
            Object var11 = arg0.relative(var10);
            if (mc.level.getBlockState(var11).canBeReplaced()) {
                break label_10;
            }
            Object var12 = var10.getOpposite();
            Object var13 = new Vec3(((((double) arg0.getX()) + 0.5) - (((double) var10.getStepX()) * 0.5)), ((((double) arg0.getY()) + 0.5) - (((double) var10.getStepY()) * 0.5)), ((((double) arg0.getZ()) + 0.5) - (((double) var10.getStepZ()) * 0.5)));
            if (this.strictDirection.getValue() && !(canSeePlacementFace(var6, var13, var11))) {
                break label_10;
            }
            return new BlockHitResult(var13, var12, var11, false);
            label_10:
            var9++;
        }
        if (this.strictDirection.getValue()) return null;
        var7 = new Vec3((((double) arg0.getX()) + 0.5), (((double) arg0.getY()) + 0.5), (((double) arg0.getZ()) + 0.5));
        return new BlockHitResult(var7, Direction.UP, arg0, false);
    }

    private BlockHitResult computeBedFace(BlockPos arg0) {
        Object var2 = mc.player.getEyePosition();
        double var3 = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        Object var5 = Vec3.atCenterOf(arg0);
        Object var6 = null;
        double var7 = 1.7976931348623157E308;
        Object var9 = null;
        double var10 = 1.7976931348623157E308;
        Object var12 = Direction.values();
        int var13 = var12.length;
        int var14 = 0;
        while (var14 < var13) {
            Object var15 = var12[var14];
            Object var21 = new Vec3(((double) var15.getStepX()), ((double) var15.getStepY()), ((double) var15.getStepZ()));
            Object var22 = var5.add(var21.scale(0.5));
            if (var2.subtract(var22).dot(var21) <= 0.0) break label_6;
            double var19 = var22.distanceTo(var2);
            if (var22.distanceTo(var2) > var3) {
                break label_6;
            }
            if (var19 < var10) {
                var10 = var19;
                var9 = new BlockHitResult(var22, var15, arg0, false);
            }
            var tmp0 = mc.level.clip(new ClipContext(var2, var22, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
            Object var18 = tmp0;
            if ((tmp0.getType() != HitResult.Type.BLOCK) || (!((var18 instanceof BlockHitResult)))) break label_4;
            Object var16 = var18;
            var tmp1 = var18.getBlockPos();
            Object var17 = tmp1;
            if (tmp1.equals(arg0)) break label_4;
            if (this.placedBedPos != null) {
                if (var17.equals(this.placedBedPos)) break label_4;
            }
            if ((this.placedBedHeadPos == null) || (!(var17.equals(this.placedBedHeadPos)))) break label_6;
            label_4:
            if (var19 >= var7) {
                break label_6;
            }
            var7 = var19;
            var6 = new BlockHitResult(var22, var15, arg0, false);
            label_6:
            var14++;
        }
        return (var6 != null ? var6 : var9);
    }

    private BedAuraModule.JointPlacement searchPlacement() {
        int var1 = ((int) Math.ceil(this.bedRange.getValue().doubleValue()));
        double var2 = this.minDamage.getValue().doubleValue();
        double var4 = this.maxSelfDamage.getValue().doubleValue();
        double var6 = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        Object var8 = null;
        double var9 = -1.0;
        Object var11 = mc.player.getEyePosition();
        double var12 = (var6 + 0.5);
        Object var14 = this.targets.iterator();
        while (var14.hasNext()) {
            Object var15 = ((Player) var14.next());
            Object var22 = var15.blockPosition();
            Object var23 = var15.getBoundingBox().getCenter();
            if (Vec3.atCenterOf(var22).distanceTo(var11) > ((((double) var1) * Math.sqrt(3.0)) + var12)) break label_0;
            var tmp0 = Math.max(-(var1), ((int) Math.floor(((var11.x - var12) - ((double) var22.getX())))));
            int var21 = tmp0;
            var tmp1 = Math.min(var1, ((int) Math.ceil(((var11.x + var12) - ((double) var22.getX())))));
            int var20 = tmp1;
            if (tmp0 > tmp1) break label_0;
            var tmp2 = Math.max(-(var1), ((int) Math.floor(((var11.y - var12) - ((double) var22.getY())))));
            int var19 = tmp2;
            var tmp3 = Math.min(var1, ((int) Math.ceil(((var11.y + var12) - ((double) var22.getY())))));
            int var18 = tmp3;
            if (tmp2 > tmp3) break label_0;
            var tmp4 = Math.max(-(var1), ((int) Math.floor(((var11.z - var12) - ((double) var22.getZ())))));
            int var17 = tmp4;
            var tmp5 = Math.min(var1, ((int) Math.ceil(((var11.z + var12) - ((double) var22.getZ())))));
            int var16 = tmp5;
            if (tmp4 <= tmp5) break label_1;
            break label_0;
            label_1:
            int var24 = var21;
            while (var24 <= var20) {
                int var25 = var19;
                while (var25 <= var18) {
                    int var26 = var17;
                    while (var26 <= var16) {
                        Object var27 = var22.offset(var24, var25, var26);
                        if (Vec3.atCenterOf(var27).distanceTo(var11) > var12) break label_17;
                        if (!(footPlaceable(var27, var6))) {
                            break label_17;
                        }
                        Object var28 = Direction.Plane.HORIZONTAL.iterator();
                        while (var28.hasNext()) {
                            Object var29 = ((Direction) var28.next());
                            Object var32 = var27.relative(var29);
                            if (!(headPlaceable(var32))) {
                                break label_6;
                            }
                            Object var33 = Vec3.atCenterOf(var27);
                            Object var34 = Vec3.atCenterOf(var32);
                            float var35 = computeDamage(var33, var15);
                            float var36 = computeDamage(var34, var15);
                            int var37 = (var36 >= var35 ? 1 : 0);
                            var tmp6 = (!(!var37) ? var34 : var33);
                            Object var31 = tmp6;
                            Object var38 = tmp6;
                            float var39 = Math.max(var35, var36);
                            if (((double) var39) < var2) break label_6;
                            var tmp7 = computeDamage(var31, mc.player);
                            float var30 = tmp7;
                            if (((double) tmp7) > var4) {
                                break label_6;
                            }
                            float var40 = (mc.player.getHealth() + mc.player.getAbsorptionAmount());
                            if (this.antiSuicide.getValue()) {
                                if ((var40 - var30) <= this.minHealth.getValue().floatValue()) break label_6;
                            }
                            if (this.balance.getValue().doubleValue() > 0.0 && var30 > 0.0f && ((double) (var39 / var30)) < this.balance.getValue().doubleValue()) {
                                break label_6;
                            }
                            double var41 = ((double) var39);
                            if (var34.distanceTo(var23) < var33.distanceTo(var23)) {
                                var41 = (var41 + 0.01);
                            }
                            if (var8 != null && var41 <= var9) {
                                break label_6;
                            }
                            var8 = new BedAuraModule.JointPlacement(var27, var29, var15, var39);
                            var9 = var41;
                        }
                        label_17:
                        var26++;
                    }
                    var25++;
                }
                var24++;
            }
        }
        return var8;
    }

    private boolean footPlaceable(BlockPos arg0, double arg1) {
        if (!(withinReach(arg0, arg1))) return false;
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        if (!(this.airPlace.getValue()) && mc.level.getBlockState(arg0.below()).canBeReplaced()) return false;
        if (this.strictDirection.getValue()) {
            if (computeBedHit(arg0) != null) {
            }
        } else {
        }
        return false;
    }

    private boolean headPlaceable(BlockPos arg0) {
        if (!(mc.level.getBlockState(arg0).canBeReplaced())) return false;
        if (this.placeOnFeet.getValue()) return true;
        return !(entityAt(arg0));
    }

    private boolean entityAt(BlockPos arg0) {
        Object var2 = new AABB(arg0);
        Object var3 = mc.level.entitiesForRendering().iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if (((var4 == mc.player) || ((var4 instanceof ItemEntity))) || (!(var4.isAlive()))) break label_0;
            if (!(var4.getBoundingBox().intersects(var2))) {
                break label_0;
            }
            return true;
        }
        return false;
    }

    private boolean currentSelectionStillGood() {
        if (((this.currentFoot == null) || (this.currentDir == null)) || (this.currentTarget == null)) {
            return false;
        }
        if ((!(this.currentTarget.isAlive())) || (this.currentTarget.getHealth() <= 0.0f)) {
            return false;
        }
        Object var2 = this.currentFoot.relative(this.currentDir);
        double var3 = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        if (!(withinReach(this.currentFoot, var3)) && !(withinReach(var2, var3))) return false;
        if (this.strictDirection.getValue() && computeBedHit(this.currentFoot) == null) return false;
        Object var5 = Vec3.atCenterOf(this.currentFoot);
        Object var6 = Vec3.atCenterOf(var2);
        float var7 = computeDamage(var5, this.currentTarget);
        var tmp0 = computeDamage(var6, this.currentTarget);
        float var1 = tmp0;
        float var8 = Math.max(var7, tmp0);
        if (((double) var8) < this.minDamage.getValue().doubleValue()) return false;
        Object var9 = (var1 >= var7 ? var6 : var5);
        float var10 = computeDamage(var9, mc.player);
        if (((double) var10) > this.maxSelfDamage.getValue().doubleValue()) return false;
        this.currentEstimatedDamage = var8;
        return true;
    }

    private BedAuraModule.ExistingBed findExistingBed() {
        double var1 = Math.max(this.range.getValue().doubleValue(), this.wallsRange.getValue().doubleValue());
        int var3 = ((int) Math.ceil(this.bedRange.getValue().doubleValue()));
        Object var4 = null;
        Object var5 = this.targets.iterator();
        while (var5.hasNext()) {
            Object var6 = ((Player) var5.next());
            Object var7 = var6.blockPosition();
            Object var8 = var6.getBoundingBox().getCenter();
            int var9 = -(var3);
            while (var9 <= var3) {
                int var10 = -(var3);
                while (var10 <= var3) {
                    int var11 = -(var3);
                    while (var11 <= var3) {
                        Object var14 = var7.offset(var9, var10, var11);
                        if (((!((mc.level.getBlockState(var14).getBlock() instanceof BedBlock))) || (Vec3.atCenterOf(var14).distanceTo(var8) > 6.0)) || (!(withinReach(var14, var1)))) break label_5;
                        var tmp0 = computeDamage(Vec3.atCenterOf(var14), mc.player);
                        float var13 = tmp0;
                        if (((double) tmp0) > this.maxSelfDamage.getValue().doubleValue()) break label_5;
                        var tmp1 = computeDamage(Vec3.atCenterOf(var14), var6);
                        float var12 = tmp1;
                        if (((double) tmp1) < this.minDamage.getValue().doubleValue()) break label_5;
                        if (var4 != null && var12 <= var4.damage()) {
                            break label_5;
                        }
                        var4 = new BedAuraModule.ExistingBed(var14, var6, var12);
                        label_5:
                        var11++;
                    }
                    var10++;
                }
                var9++;
            }
        }
        return var4;
    }

    private float computeDamage(Vec3 arg0, Player arg1) {
        if (this.predict.getValue()) {
            Object var3 = arg1.position().add(arg1.getDeltaMovement());
            Object var4 = arg1.getBoundingBox().move(var3.subtract(arg1.position()));
            return DamageUtils.getDamage(arg1, var4, arg0, 5.0f, null, false);
        }
        return DamageUtils.getDamage(arg1, arg1.getBoundingBox(), arg0, 5.0f, null, false);
    }

    private boolean withinReach(BlockPos arg0, double arg1) {
        return Vec3.atCenterOf(arg0).distanceTo(mc.player.getEyePosition()) <= (arg1 + 0.5);
    }

    private void updateTargets() {
        this.targets.clear();
        Object var1 = mc.player.getEyePosition();
        Object var2 = new ArrayList();
        Object var3 = mc.level.entitiesForRendering().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if (!((var4 instanceof Player))) break label_3;
            Object var5 = ((Player) var4);
            if (((var5 == mc.player) || (!(var5.isAlive()))) || (var5.getHealth() <= 0.0f)) break label_3;
            if (!(this.ignoreFriends.getValue())) break label_1;
            if (LightningClient.FRIEND_MANAGER.contains(var5.getName().getString())) break label_3;
            label_1:
            if (Vec3.atCenterOf(var5.blockPosition()).distanceTo(var1) > this.targetRange.getValue().doubleValue()) {
                if (((double) var5.distanceTo(mc.player)) > this.targetRange.getValue().doubleValue()) break label_3;
            }
            var2.add(var5);
            label_3:
        }
        var4 = this.targetMode.getValue();
        var5 = -1;
        switch (var4.hashCode()) {
            case -2137395588 -> { break label_6; }
            case 1575438095 -> { break label_5; }
            default -> { break label_7; }
        }
        label_5:
        if (!(var4.equals("Furthest"))) break label_7;
        var5 = 0;
        break label_7;
        label_6:
        if (!(var4.equals("Health"))) break label_7;
        var5 = 1;
        label_7:
        switch (var5) {
            case 0 -> { break label_8; }
            case 1 -> { break label_9; }
            default -> { break label_10; }
        }
        label_8:
        break label_11;
        label_9:
        break label_11;
        label_10:
        label_11:
        var3 = Comparator.comparingDouble((arg0) -> ((double) arg0.distanceTo(mc.player)));
        var2.sort(var3);
        var4 = this.maxTargets.getValue().intValue();
        var5 = var2.iterator();
        while (var5.hasNext()) {
            Object var6 = ((Player) var5.next());
            if (this.targets.size() >= var4) {
                break label_14;
            }
            this.targets.add(var6);
        }
        label_14:
    }

    private BlockPos findOtherBedHalf(BlockPos arg0) {
        Object var2 = Direction.Plane.HORIZONTAL.iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Direction) var2.next());
            Object var4 = arg0.relative(var3);
            if (!((mc.level.getBlockState(var4).getBlock() instanceof BedBlock))) {
                break label_0;
            }
            return var4;
        }
        return null;
    }

    private int findBedSlot() {
        int var1 = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Object var2 = (arg0) -> {
    if (arg0.isEmpty()) break label_0;
    return (arg0.getItem() instanceof BedItem);
};
        if (!(!var1)) {
            int var3 = 0;
            while (var3 < 36) {
                if (!(var2.test(mc.player.getInventory().getItem(var3)))) {
                } else {
                    return var3;
                }
                var3++;
            }
            return -1;
        }
        var3 = 0;
        while (var3 < 9) {
            if (!(var2.test(mc.player.getInventory().getItem(var3)))) {
            } else {
                return var3;
            }
            var3++;
        }
        return -1;
    }

    private int findDetonateSlot() {
        int var2 = this.swapMode.getValue().equalsIgnoreCase("Alt");
        Object var3 = (arg0) -> {
    if ((arg0.isEmpty()) || ((arg0.getItem() instanceof BedItem))) break label_0;
    return !((arg0.getItem() instanceof BlockItem));
};
        if (!(!var2)) {
            int var1 = 0;
            label_0:
            if (var1 >= 36) break label_7;
            if (var3.test(mc.player.getInventory().getItem(var1))) break label_1;
            break label_2;
            label_1:
            return var1;
            label_2:
            var1++;
            break label_0;
        }
        var1 = 0;
        while (var1 < 9) {
            if (!(var3.test(mc.player.getInventory().getItem(var1)))) {
            } else {
                return var1;
            }
            var1++;
        }
        label_7:
        var1 = 0;
        while (var1 < 9) {
            if (!(mc.player.getInventory().getItem(var1).isEmpty())) {
            } else {
                return var1;
            }
            var1++;
        }
        var1 = 0;
        while (var1 < 9) {
            if ((mc.player.getInventory().getItem(var1).getItem() instanceof BedItem)) {
            } else {
                return var1;
            }
            var1++;
        }
        return -2147483648;
    }

    private void clearSelection() {
        this.currentFoot = null;
        this.currentDir = null;
        this.currentTarget = null;
        this.currentEstimatedDamage = 0.0f;
        this.currentHead = null;
        this.currentAdopt = false;
        this.currentPlacement = null;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((!(this.renderPreview.getValue())) || (mc.level == null)) || (mc.player == null)) {
            return;
        }
        if (!(!this.bedPlacedThisCycle) && this.placedBedPos != null) {
            Object var4 = this.placedBedPos;
            if (this.placedBedHeadPos == null) break label_2;
            break label_3;
            label_2:
            label_3:
            var tmp0 = findOtherBedHalf(var4);
            Object var3 = tmp0;
            Object var5 = tmp0;
            if (var3 != null) {
                this.placedBedHeadPos = var3;
            }
            break label_11;
        }
        if (this.currentPlacement != null) {
            var4 = this.currentPlacement.pos();
            var3 = var4.relative(this.currentPlacement.dir());
            break label_11;
        }
        if (this.currentFoot != null) {
            var4 = this.currentFoot;
            if (this.currentHead != null) {
                break label_9;
            }
            if (this.currentDir != null) {
                break label_9;
            }
            label_9:
            var3 = findOtherBedHalf(var4);
            break label_11;
        }
        this.lastRenderedPos = null;
        return;
        label_11:
        if (!(var4.equals(this.lastRenderedPos))) {
            this.lastRenderedPos = var4;
            this.placementRenderMs = System.currentTimeMillis();
        }
        var5 = 1.0f;
        if (this.interpolate.getValue()) {
            long var6 = (System.currentTimeMillis() - this.placementRenderMs);
            float var8 = Math.min(1.0f, (((float) var6) / 350.0f));
            var5 = (0.15f + (0.85f * var8));
        }
        var6 = 0.5625;
        var8 = arg0.getMatrices();
        double var9 = ((double) var4.getX());
        double var11 = ((double) var4.getY());
        double var13 = ((double) var4.getZ());
        double var15 = (((double) var4.getX()) + 1.0);
        double var17 = (((double) var4.getY()) + 0.5625);
        double var19 = (((double) var4.getZ()) + 1.0);
        if (var3 != null) {
            var9 = ((double) Math.min(var4.getX(), var3.getX()));
            var11 = ((double) Math.min(var4.getY(), var3.getY()));
            var13 = ((double) Math.min(var4.getZ(), var3.getZ()));
            var15 = (((double) Math.max(var4.getX(), var3.getX())) + 1.0);
            var17 = (((double) Math.max(var4.getY(), var3.getY())) + 0.5625);
            var19 = (((double) Math.max(var4.getZ(), var3.getZ())) + 1.0);
        }
        double var21 = ((var9 + var15) * 0.5);
        double var23 = ((var11 + var17) * 0.5);
        double var25 = ((var13 + var19) * 0.5);
        double var27 = (((var15 - var9) * 0.5) * ((double) var5));
        double var29 = (((var17 - var11) * 0.5) * ((double) var5));
        double var31 = (((var19 - var13) * 0.5) * ((double) var5));
        Object var33 = new AABB((var21 - var27), (var23 - var29), (var25 - var31), (var21 + var27), (var23 + var29), (var25 + var31));
        Object var34 = this.fillColor.getColor();
        Object var35 = this.outlineColor.getColor();
        if (!(this.renderMode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var36 = 0;
        if (!(this.renderMode.getValue().equalsIgnoreCase("Outline"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var2 = 0;
        int var37 = 0;
        if (!(!var36)) {
            Renderer3D.renderBox(var8, var33, var34);
        }
        if (!(!var2)) {
            Renderer3D.renderBoxOutline(var8, var33, var35);
        }
    }

    private void startAutoCraft() {
        if (!(!this.autoCraftRunning)) return;
        this.autoCraftRunning = true;
        this.autoCraftTicks = 0;
        this.placedTablePos = null;
        this.tableOpenSent = false;
    }

    private void handleAutoCraftTick() {
        this.autoCraftTicks = (this.autoCraftTicks + 1);
        if (this.autoCraftTicks > 200) {
            this.autoCraftRunning = false;
            return;
        }
        int var1 = (mc.player.containerMenu instanceof CraftingMenu);
        if (!var1) {
            if (this.autoPlaceTable.getValue()) break label_1;
            this.autoCraftRunning = false;
            return;
            label_1:
            if (this.placedTablePos == null) {
                this.placedTablePos = tryPlaceCraftingTable();
                if (this.placedTablePos == null) {
                    this.autoCraftRunning = false;
                    return;
                }
                this.tableOpenSent = false;
                this.autoCraftTicks = 0;
                return;
            }
            var tmp0 = (mc.level.getBlockState(this.placedTablePos).getBlock() == Blocks.CRAFTING_TABLE ? 1 : 0);
            int var2 = tmp0;
            int var3 = tmp0;
            if (!(!var2) && !this.tableOpenSent) {
                if (this.rotate.getValue()) {
                    Object var4 = RotationUtils.getRotations(Vec3.atCenterOf(this.placedTablePos));
                    LightningClient.ROTATION_MANAGER.silentRotate(var4[0], var4[1]);
                }
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(this.placedTablePos), Direction.UP, this.placedTablePos, false));
                this.tableOpenSent = true;
                this.autoCraftTicks = 0;
                return;
            }
            if (this.autoCraftTicks > 40) {
                this.autoCraftRunning = false;
                this.placedTablePos = null;
                this.tableOpenSent = false;
            }
            return;
        }
        this.placedTablePos = null;
        this.tableOpenSent = false;
        var2 = mc.player.containerMenu;
        if (!(gridHasMaterials(var2)) && !(fillCraftingGrid(var2))) {
            if (this.autoCloseTable.getValue()) {
                mc.player.closeContainer();
            }
            this.autoCraftRunning = false;
            return;
        }
        mc.gameMode.handleContainerInput(var2.containerId, 0, 0, ContainerInput.QUICK_MOVE, mc.player);
        if (isInventoryFull()) {
            if (this.autoCloseTable.getValue()) {
                mc.player.closeContainer();
            }
            this.autoCraftRunning = false;
        }
    }

    private int countBeds() {
        int var1 = 0;
        int var2 = 0;
        while (var2 < 36) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if (!((var3.getItem() instanceof BedItem))) {
            } else {
                var1 = (var1 + var3.getCount());
            }
            var2++;
        }
        return var1;
    }

    private boolean isInventoryFull() {
        int var1 = 0;
        while (var1 < 36) {
            if (!(mc.player.getInventory().getItem(var1).isEmpty())) {
            } else {
                return false;
            }
            var1++;
        }
        return true;
    }

    private boolean gridHasMaterials(AbstractContainerMenu arg0) {
        Object var2 = WOOL_GRID_SLOTS;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            int var5 = var2[var4];
            if (isWool(arg0.getSlot(var5).getItem())) {
            } else {
                return false;
            }
            var4++;
        }
        var2 = PLANK_GRID_SLOTS;
        var3 = var2.length;
        var4 = 0;
        while (var4 < var3) {
            var5 = var2[var4];
            if (isPlanks(arg0.getSlot(var5).getItem())) {
            } else {
                return false;
            }
            var4++;
        }
        return true;
    }

    private boolean isWool(ItemStack arg0) {
        if (arg0.isEmpty()) break label_0;
        return arg0.is(ItemTags.WOOL);
    }

    private boolean isPlanks(ItemStack arg0) {
        if (arg0.isEmpty()) break label_0;
        return arg0.is(ItemTags.PLANKS);
    }

    private boolean fillCraftingGrid(AbstractContainerMenu arg0) {
        int var3 = findMenuSlot(arg0, this::isWool);
        int var4 = findMenuSlot(arg0, this::isPlanks);
        if ((var3 == -1) || (var4 == -1)) {
            return false;
        }
        Object var5 = WOOL_GRID_SLOTS;
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            int var8 = var5[var7];
            if (isWool(arg0.getSlot(var8).getItem())) {
            } else {
                int var2 = findMenuSlot(arg0, this::isWool);
                if (var2 == -1) return false;
                placeOneItem(arg0, var2, var8);
            }
            var7++;
        }
        var5 = PLANK_GRID_SLOTS;
        var6 = var5.length;
        var7 = 0;
        while (var7 < var6) {
            var8 = var5[var7];
            if (isPlanks(arg0.getSlot(var8).getItem())) {
            } else {
                var2 = findMenuSlot(arg0, this::isPlanks);
                if (var2 == -1) return false;
                placeOneItem(arg0, var2, var8);
            }
            var7++;
        }
        return true;
    }

    private void placeOneItem(AbstractContainerMenu arg0, int arg1, int arg2) {
        mc.gameMode.handleContainerInput(arg0.containerId, arg1, 1, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(arg0.containerId, arg2, 1, ContainerInput.PICKUP, mc.player);
        mc.gameMode.handleContainerInput(arg0.containerId, arg1, 0, ContainerInput.PICKUP, mc.player);
    }

    private int findMenuSlot(AbstractContainerMenu arg0, Predicate arg1) {
        int var3 = 10;
        while (var3 < arg0.slots.size()) {
            if (!(arg1.test(arg0.getSlot(var3).getItem()))) {
            } else {
                return var3;
            }
            var3++;
        }
        return -1;
    }

    private BlockPos tryPlaceCraftingTable() {
        int var1 = -1;
        int var2 = 0;
        while (var2 < 9) {
            if (mc.player.getInventory().getItem(var2).getItem() == Blocks.CRAFTING_TABLE.asItem()) break label_1;
            break label_2;
            label_1:
            var1 = var2;
            break label_3;
            label_2:
            var2++;
        }
        label_3:
        if (var1 == -1) return null;
        var2 = mc.player.blockPosition();
        Object var3 = null;
        double var4 = 1.7976931348623157E308;
        int var6 = ((int) Math.ceil(this.range.getValue().doubleValue()));
        int var7 = -(var6);
        while (var7 <= var6) {
            int var8 = -(var6);
            while (var8 <= var6) {
                int var9 = -(var6);
                while (var9 <= var6) {
                    Object var10 = var2.offset(var7, var8, var9);
                    double var11 = mc.player.distanceToSqr((((double) var10.getX()) + 0.5), (((double) var10.getY()) + 0.5), (((double) var10.getZ()) + 0.5));
                    if ((((var11 > (this.range.getValue().doubleValue() * this.range.getValue().doubleValue())) || (!(mc.level.getBlockState(var10).canBeReplaced()))) || (mc.level.getBlockState(var10.below()).canBeReplaced())) || (new AABB(var10).intersects(mc.player.getBoundingBox()))) break label_9;
                    if (var11 >= var4) {
                        break label_9;
                    }
                    var4 = var11;
                    var3 = var10;
                    label_9:
                    var9++;
                }
                var8++;
            }
            var7++;
        }
        if (var3 == null) return null;
        var7 = var3.below();
        var8 = new BlockHitResult(new Vec3((((double) var3.getX()) + 0.5), ((double) var3.getY()), (((double) var3.getZ()) + 0.5)), Direction.UP, var7, false);
        var9 = mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot("Normal", var1, var9);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, var8);
        InventoryUtils.switchBack("Normal", var1, var9);
        return var3;
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent arg0) {
        clearSelection();
        this.autoCraftRunning = false;
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent arg0) {
        clearSelection();
        this.autoCraftRunning = false;
    }

    static {
        var tmp0 = new int[3];
        tmp0[0] = 1;
        tmp0[1] = 2;
        tmp0[2] = 3;
        WOOL_GRID_SLOTS = tmp0;
        var tmp1 = new int[3];
        tmp1[0] = 4;
        tmp1[1] = 5;
        tmp1[2] = 6;
        PLANK_GRID_SLOTS = tmp1;
    }


    record ExistingBed(BlockPos pos, Player target, float damage) {

        private ExistingBed(BlockPos arg0, Player arg1, float arg2) {
            this.pos = arg0;
            this.target = arg1;
            this.damage = arg2;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Player target() {
            return this.target;
        }

        public float damage() {
            return this.damage;
        }

    }

    public record JointPlacement(BlockPos foot, Direction dir, Player target, float estimatedDamage) {

        public JointPlacement(BlockPos arg0, Direction arg1, Player arg2, float arg3) {
            this.foot = arg0;
            this.dir = arg1;
            this.target = arg2;
            this.estimatedDamage = arg3;
        }

        public BlockPos foot() {
            return this.foot;
        }

        public Direction dir() {
            return this.dir;
        }

        public Player target() {
            return this.target;
        }

        public float estimatedDamage() {
            return this.estimatedDamage;
        }

    }

    public record PlacementCandidate(BlockPos pos, Direction dir, float estimatedDamage) {

        public PlacementCandidate(BlockPos arg0, Direction arg1, float arg2) {
            this.pos = arg0;
            this.dir = arg1;
            this.estimatedDamage = arg2;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Direction dir() {
            return this.dir;
        }

        public float estimatedDamage() {
            return this.estimatedDamage;
        }

    }
}
