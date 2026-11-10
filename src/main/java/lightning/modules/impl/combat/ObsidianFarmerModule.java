package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.Timer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "ObsidianFarmer", description = "Places and mines ender chests using SpeedMine.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class ObsidianFarmerModule extends Module {
    public ModeSetting autoSwitch;
    public ModeSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting airPlace;
    public BooleanSetting crystalDestruction;
    public NumberSetting range;
    public NumberSetting mineRange;
    public NumberSetting wallsRange;
    public NumberSetting delay;
    public NumberSetting mineDelay;
    public NumberSetting retryDelay;
    public BooleanSetting requirePacketMine;
    public BooleanSetting support;
    public BooleanSetting swing;
    public BooleanSetting whileEating;
    public BooleanSetting onlyOnGround;
    public BooleanSetting render;
    private final Timer placeTimer;
    private final Timer mineTimer;
    private final Timer retryTimer;
    private BlockPos enderChestPos;
    private ObsidianFarmerModule.Phase phase;

    public ObsidianFarmerModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing and mining blocks.", "Grim", tmp0);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places and mines using directions that face you.", false);
        this.airPlace = new BooleanSetting("AirPlace", "Places blocks in the air without needing neighboring blocks.", false);
        this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Destroys any crystals that interfere with block placement.", true);
        this.range = new NumberSetting("Range", "The maximum range at which blocks will be placed.", 5.0, 1.0, 6.0);
        this.mineRange = new NumberSetting("MineRange", "The maximum range at which ender chests will be mined.", 5.0, 1.0, 8.0);
        this.wallsRange = new NumberSetting("WallsRange", "The maximum range through walls for mining.", 3.5, 1.0, 8.0);
        this.delay = new NumberSetting("Delay", "The delay in ticks between placements.", 2, 0, 20);
        this.mineDelay = new NumberSetting("MineDelay", "The delay in ticks before mining.", 1, 0, 20);
        this.retryDelay = new NumberSetting("RetryDelay", "The delay in ticks before retrying a failed mine attempt.", 1, 1, 20);
        this.requirePacketMine = new BooleanSetting("RequirePacketMine", "Only mines when SpeedMine is enabled.", true);
        this.support = new BooleanSetting("Support", "Places a supporting block under the ender chest if needed.", true);
        this.swing = new BooleanSetting("Swing", "Swings your hand when starting to break blocks.", true);
        this.whileEating = new BooleanSetting("WhileEating", "Places and mines blocks while eating.", true);
        this.onlyOnGround = new BooleanSetting("OnlyGround", "Only works while on ground.", false);
        this.render = new BooleanSetting("Render", "Whether or not to render place and mine positions.", true);
        this.placeTimer = new Timer();
        this.mineTimer = new Timer();
        this.retryTimer = new Timer();
        this.phase = ObsidianFarmerModule.Phase.Place;
    }

    public void onEnable() {
        this.enderChestPos = null;
        this.phase = ObsidianFarmerModule.Phase.Place;
        this.placeTimer.reset();
        this.mineTimer.reset();
        this.retryTimer.reset();
    }

    public void onDisable() {
        this.enderChestPos = null;
        this.phase = ObsidianFarmerModule.Phase.Place;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (getNull()) return;
        Object var2 = mc.player;
        Object var3 = mc.level;
        Object var4 = mc.gameMode;
        if (((var2 == null) || (var3 == null)) || (var4 == null)) {
            return;
        }
        if (!(this.whileEating.getValue()) && EntityUtils.isEating()) return;
        if (this.onlyOnGround.getValue() && !(var2.onGround())) return;
        updateCurrentEnderChest(var2, var3);
        if (this.enderChestPos != null && var3.getBlockState(this.enderChestPos).is(Blocks.ENDER_CHEST)) {
            this.phase = ObsidianFarmerModule.Phase.Mine;
            if (this.requirePacketMine.getValue() && !(isPacketMineActive())) return;
            targetEnderChest(var2, var3, var4, this.enderChestPos);
            return;
        }
        this.phase = ObsidianFarmerModule.Phase.Place;
        this.enderChestPos = null;
        placeEnderChest(var2, var3);
    }

    private boolean isPacketMineActive() {
        if (LightningClient.MODULE_MANAGER == null) return false;
        Object var1 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (var1 == null) break label_1;
        return var1.isToggled();
    }

    private void updateCurrentEnderChest(LocalPlayer arg0, ClientLevel arg1) {
        if (this.enderChestPos != null) {
            if (!(arg1.getBlockState(this.enderChestPos).is(Blocks.ENDER_CHEST))) {
                this.enderChestPos = null;
            }
            return;
        }
        this.enderChestPos = findExistingEnderChest(arg0, arg1);
    }

    private BlockPos findExistingEnderChest(LocalPlayer arg0, ClientLevel arg1) {
        double var3 = Math.max(this.range.getValue().doubleValue(), this.mineRange.getValue().doubleValue());
        double var5 = (var3 * var3);
        int var7 = ((int) Math.ceil(var3));
        Object var8 = arg0.blockPosition();
        Object var9 = null;
        double var10 = 1.7976931348623157E308;
        int var12 = -(var7);
        while (var12 <= var7) {
            int var13 = -(var7);
            while (var13 <= var7) {
                int var14 = -(var7);
                while (var14 <= var7) {
                    Object var17 = var8.offset(var12, var13, var14);
                    if (!(arg1.getBlockState(var17).is(Blocks.ENDER_CHEST))) break label_4;
                    double var15 = arg0.distanceToSqr(Vec3.atCenterOf(var17));
                    if (arg0.distanceToSqr(Vec3.atCenterOf(var17)) > var5) break label_4;
                    if (var15 >= var10) {
                        break label_4;
                    }
                    var10 = var15;
                    var9 = var17.immutable();
                    label_4:
                    var14++;
                }
                var13++;
            }
            var12++;
        }
        return var9;
    }

    private void placeEnderChest(LocalPlayer arg0, ClientLevel arg1) {
        if (!(this.placeTimer.hasTimeElapsed((((long) this.delay.getValue().intValue()) * 50L)))) return;
        Object var4 = findBestPlacePosition(arg0, arg1);
        if (var4 == null) return;
        int var5 = findEnderChestSlot(arg0);
        if (var5 == -1) return;
        int var6 = arg0.getInventory().getSelectedSlot();
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        int var7 = 0;
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var5, var6);
        var7 = tryPlaceAt(var4);
        if ((var7) || (!(this.support.getValue()))) break label_7;
        var tmp0 = var4.below();
        Object var9 = tmp0;
        if (!(isValidPlaceSpot(arg0, arg1, tmp0))) break label_7;
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        var tmp1 = InventoryUtils.findHardestBlock(35, 8);
        int var8 = tmp1;
        if (tmp1 == -1) break label_7;
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var8, var6);
        int var10 = tryPlaceAt(var9);
        if ((!var10) || (!(isValidPlaceSpot(arg0, arg1, var4)))) break label_7;
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var5, var6);
        var7 = tryPlaceAt(var4);
        label_7:
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var5, var6);
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        break label_11;
        Object var11 = 0;
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        throw var11;
        label_11:
        this.placeTimer.reset();
        if (!var7) return;
        if (arg1.getBlockState(var4).is(Blocks.ENDER_CHEST)) {
        } else {
        }
        var tmp2 = findExistingEnderChest(arg0, arg1);
        Object var3 = tmp2;
        var8 = tmp2;
        if (var3 == null) return;
        this.enderChestPos = var3;
        this.phase = ObsidianFarmerModule.Phase.Mine;
        this.mineTimer.reset();
        this.retryTimer.reset();
    }

    private boolean tryPlaceAt(BlockPos arg0) {
        Object var2 = WorldUtils.getDirection(arg0, this.strictDirection.getValue());
        if (var2 != null) return WorldUtils.placeBlock(arg0, var2, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        if (this.airPlace.getValue()) {
            WorldUtils.airPlaceBlock(arg0, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.render.getValue());
            return true;
        }
        return false;
    }

    private void targetEnderChest(LocalPlayer arg0, ClientLevel arg1, MultiPlayerGameMode arg2, BlockPos arg3) {
        if (!(this.mineTimer.hasTimeElapsed((((long) this.mineDelay.getValue().intValue()) * 50L)))) return;
        if (!(this.retryTimer.hasTimeElapsed((((long) this.retryDelay.getValue().intValue()) * 50L)))) return;
        if (!(canTargetEnderChest(arg0, arg1, arg3))) {
            this.enderChestPos = null;
            this.phase = ObsidianFarmerModule.Phase.Place;
            return;
        }
        Object var6 = getMineSide(arg0, arg1, arg3);
        if (this.strictDirection.getValue() && var6 == null) return;
        Object var7 = (var6 == null ? Direction.UP : var6);
        Object var8 = getHitVec(arg3, var7);
        var tmp0 = (!(this.rotate.getValue().equalsIgnoreCase("None")) ? 1 : 0);
        int var5 = tmp0;
        int var9 = tmp0;
        if (!var5) break label_9;
        Object var10 = RotationUtils.getRotations(var8);
        if (LightningClient.ROTATION_MANAGER.isBatchingRotations()) {
            LightningClient.ROTATION_MANAGER.batchRotate(var10[0], var10[1]);
            break label_9;
        }
        LightningClient.ROTATION_MANAGER.silentRotate(var10[0], var10[1]);
        label_9:
        arg2.startDestroyBlock(arg3, var7);
        if (this.swing.getValue()) {
            arg0.swing(InteractionHand.MAIN_HAND);
        }
        this.retryTimer.reset();
        this.mineTimer.reset();
    }

    private BlockPos findBestPlacePosition(LocalPlayer arg0, ClientLevel arg1) {
        Object var3 = getPlaceCandidates(arg0);
        var3.removeIf(() -> !(isValidPlaceSpot(arg0, arg1, arg2)));
        var3.sort(Comparator.comparingDouble((arg1) -> arg0.distanceToSqr(Vec3.atCenterOf(arg1))));
        if (var3.isEmpty()) {
        } else {
        }
        return ((BlockPos) var3.getFirst()).immutable();
    }

    private List getPlaceCandidates(LocalPlayer arg0) {
        Object var2 = new LinkedHashSet();
        Object var3 = arg0.blockPosition();
        Object var4 = arg0.getDirection();
        var2.add(var3.relative(var4));
        var2.add(var3.relative(var4).below());
        Object var5 = Direction.Plane.HORIZONTAL.iterator();
        while (var5.hasNext()) {
            Object var6 = ((Direction) var5.next());
            var2.add(var3.relative(var6));
        }
        var5 = Direction.Plane.HORIZONTAL.iterator();
        while (var5.hasNext()) {
            var6 = ((Direction) var5.next());
            var2.add(var3.relative(var6).below());
        }
        var2.add(var3.below());
        return new ArrayList(var2);
    }

    private boolean isValidPlaceSpot(LocalPlayer arg0, ClientLevel arg1, BlockPos arg2) {
        if (arg1.isOutsideBuildHeight(arg2)) return false;
        Object var4 = arg1.getBlockState(arg2);
        if (!(var4.isAir()) && !(var4.canBeReplaced())) return false;
        if (arg0.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg2)) > (this.range.getValue().doubleValue() * this.range.getValue().doubleValue())) return false;
        Object var5 = new AABB(arg2);
        if (arg0.getBoundingBox().intersects(var5)) return false;
        if (!(arg1.getEntitiesOfClass(Entity.class, var5, (arg0) -> !((arg0 instanceof ItemEntity))).isEmpty())) return false;
        if (!(this.airPlace.getValue())) {
            if (WorldUtils.getDirection(arg2, this.strictDirection.getValue()) != null) {
            }
        } else {
        }
        return false;
    }

    private boolean canTargetEnderChest(LocalPlayer arg0, ClientLevel arg1, BlockPos arg2) {
        if (!(arg1.getBlockState(arg2).is(Blocks.ENDER_CHEST))) return false;
        if (!(canReach(arg0, arg1, arg2, this.mineRange.getValue().doubleValue(), this.wallsRange.getValue().doubleValue()))) return false;
        if (this.strictDirection.getValue()) {
            if (getMineSide(arg0, arg1, arg2) != null) {
            }
        } else {
        }
        return false;
    }

    private boolean canReach(LocalPlayer arg0, ClientLevel arg1, BlockPos arg2, double arg3, double arg4) {
        Object var9 = arg0.getEyePosition();
        var tmp0 = Vec3.atCenterOf(arg2);
        Object var8 = tmp0;
        double var10 = var9.distanceTo(tmp0);
        if (var10 <= arg3 && hasLineOfSight(arg0, arg1, var9, var8)) return true;
        return var10 <= arg4;
    }

    private boolean hasLineOfSight(LocalPlayer arg0, ClientLevel arg1, Vec3 arg2, Vec3 arg3) {
        Object var5 = arg1.clip(new ClipContext(arg2, arg3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, arg0));
        return var5.getType() == HitResult.Type.MISS;
    }

    private Direction getMineSide(LocalPlayer arg0, ClientLevel arg1, BlockPos arg2) {
        Object var4 = arg0.getEyePosition();
        Object var5 = null;
        double var6 = 1.7976931348623157E308;
        Object var8 = Direction.values();
        int var9 = var8.length;
        int var10 = 0;
        while (var10 < var9) {
            Object var11 = var8[var10];
            Object var12 = getHitVec(arg2, var11);
            double var13 = var4.distanceTo(var12);
            if (var13 >= var6) break label_2;
            if (this.strictDirection.getValue() && !(hasLineOfSight(arg0, arg1, var4, var12))) {
                break label_2;
            }
            var6 = var13;
            var5 = var11;
            label_2:
            var10++;
        }
        return var5;
    }

    private Vec3 getHitVec(BlockPos arg0, Direction arg1) {
        return Vec3.atCenterOf(arg0).add((((double) arg1.getStepX()) * 0.5), (((double) arg1.getStepY()) * 0.5), (((double) arg1.getStepZ()) * 0.5));
    }

    private int findEnderChestSlot(LocalPlayer arg0) {
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var2 = 8;
        int var3 = arg0.getInventory().getSelectedSlot();
        if (arg0.getInventory().getItem(var3).is(Items.ENDER_CHEST)) return var3;
        return InventoryUtils.find(Items.ENDER_CHEST, 0, var2);
    }

    public String getMetaData() {
        if (this.enderChestPos != null) return (String.valueOf(ChatFormatting.GREEN) + this.phase.name() + String.valueOf(ChatFormatting.RESET));
        return "Place";
    }


    enum Phase {
        Place,
        Mine;


        private Phase() {
        }

    }
}
