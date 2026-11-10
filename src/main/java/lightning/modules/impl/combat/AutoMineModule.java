package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AutoMine", description = "Manages which blocks SpeedMine should mine to defeat your opponents.", category = Module.Category.COMBAT)
public class AutoMineModule extends Module {
    public ModeSetting priority;
    public ModeSetting logic;
    public BooleanSetting raytrace;
    public BooleanSetting upFallback;
    public ModeSetting upFallbackRotate;
    public BooleanSetting ignoreNaked;
    public BooleanSetting avoidSharing;
    public BooleanSetting terrain;
    public BooleanSetting terrainPlace;
    public BooleanSetting ironPickaxe;
    public BooleanSetting antiCrawl;
    public BooleanSetting face;
    private final List pendingTerrainPlacements;
    private AutoMineModule.TerrainPair activeTerrainPair;
    private UUID activeTerrainTarget;
    private BlockPos upComboPos;

    public AutoMineModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Leg";
        tmp0[1] = "Head";
        this.priority = new ModeSetting("Priority", "Which area of the target to prioritize mining.", "Leg", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Grim";
        tmp1[1] = "NCP";
        this.logic = new ModeSetting("Logic", "Auto mining targeting logic.", "Grim", tmp1);
        this.raytrace = new BooleanSetting("Raytrace", "Only mines blocks that are in line of sight (visible).", false);
        this.upFallback = new BooleanSetting("CivBreak", "Priority=Leg only: when Raytrace can't see any leg-level surround block, mines the block above it instead. Turn on AutoCrystal's own MineIgnore setting for the crystal pre-place/detonate combo -- this module only builds+mines the obsidian.", new BooleanSetting.Visibility(this.raytrace, true), false);
        var tmp2 = new String[3];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        tmp2[2] = "Grim";
        this.upFallbackRotate = new ModeSetting("Rotate", "The rotation mode when placing the Up-fallback obsidian.", new BooleanSetting.Visibility(this.upFallback, true), "Grim", tmp2);
        this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", false);
        this.avoidSharing = new BooleanSetting("AvoidSharing", "Avoids mining blocks that are part of your own surround or safety ring.", true);
        this.terrain = new BooleanSetting("Terrain", "Automatically mines terrain blocks to place obsidian when target has no crystal base or is fluid-covered.", false);
        this.terrainPlace = new BooleanSetting("TerrainPlace", "Automatically places obsidian once the terrain block is broken.", new BooleanSetting.Visibility(this.terrain, true), true);
        this.ironPickaxe = new BooleanSetting("IronPickaxe", "Uses iron pickaxe after obsidian matures to prevent obsidian item drops.", false);
        this.antiCrawl = new BooleanSetting("AntiCrawl", "While crawling, mines the block above/below your feet to stand back up instead of staying trapped.", true);
        this.face = new BooleanSetting("Face", "Mines the block stuck in your face to rescue you from face traps or phased blocks.", true);
        this.pendingTerrainPlacements = new ArrayList();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        this.upComboPos = null;
    }

    public boolean isRaytrace() {
        return this.raytrace.getValue();
    }

    public boolean isUpComboActive(BlockPos arg0) {
        if ((!(this.upFallback.getValue())) || (this.upComboPos == null)) break label_0;
        return this.upComboPos.equals(arg0);
    }

    private void resetUpCombo() {
        this.upComboPos = null;
    }

    private SpeedMineModule speedMine() {
        return ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        resetUpCombo();
    }

    public void onEnable() {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        resetUpCombo();
    }

    public void onDisable() {
        this.pendingTerrainPlacements.clear();
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        resetUpCombo();
        Object var1 = speedMine();
        if (var1 != null) {
            var1.setDoubleEngaged(false);
            var1.restoreIronSwap();
        }
    }

    private boolean isRealTerrainBase(BlockPos arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0).getBlock();
        if (var2 != Blocks.OBSIDIAN) {
            if (var2 == Blocks.BEDROCK) {
            }
        } else {
        }
        return false;
    }

    private boolean isMinableLegCandidate(BlockPos arg0, SpeedMineModule arg1) {
        Object var3 = mc.level.getBlockState(arg0);
        if (var3.canBeReplaced()) return false;
        if (var3.getBlock().defaultDestroyTime() < 0.0f) {
            if (arg1.sixB.getValue() && var3.getBlock() == Blocks.BEDROCK) {
            }
        } else {
        }
        return false;
    }

    private boolean isPhased(Player arg0) {
        return EntityUtils.isPhased(arg0);
    }

    private boolean isTargetPhasedInBedrock(Player arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return false;
        }
        Object var2 = EntityUtils.getPhasedBlocks(arg0).iterator();
        label_2:
        if (var2.hasNext()) {
            Object var3 = ((BlockPos) var2.next());
            if (!(mc.level.getBlockState(var3).is(Blocks.BEDROCK))) {
                break label_2;
            }
            return true;
        }
        return false;
    }

    private BlockPos getGrimBedrockHeadTarget(Player arg0, SpeedMineModule arg1) {
        if ((arg0 == null) || (mc.level == null)) {
            return null;
        }
        if (!(this.logic.getValue().equalsIgnoreCase("Grim"))) return null;
        if (!(isTargetPhasedInBedrock(arg0))) return null;
        int var4 = Mth.floor(arg0.getY());
        if (!(arg0.isVisuallyCrawling())) {
            if (arg0.getBoundingBox().getYsize() <= 1.0) {
            }
        } else {
        }
        int var5 = (var4 + 2);
        Object var6 = arg1.getPrimary();
        if (var6 != null && var6.isInstantMine() && var6.getPosition().getY() == var5 && arg1.isTargetSurroundPosition(var6.getPosition(), arg0)) return var6.getPosition();
        Object var7 = new ArrayList();
        Object var8 = HoleUtils.getInsidePositions(arg0).iterator();
        while (var8.hasNext()) {
            Object var9 = ((BlockPos) var8.next());
            Object var10 = new BlockPos(var9.getX(), var5, var9.getZ());
            if (var7.contains(var10)) {
                break label_8;
            }
            var7.add(var10);
        }
        var8 = arg0.blockPosition().above((var5 - var4));
        if (!(var7.contains(var8))) {
            var7.add(var8);
        }
        var tmp0 = BlockPos.containing(arg0.getX(), (arg0.getBoundingBox().maxY + 0.2), arg0.getZ());
        Object var3 = tmp0;
        if (!(var7.contains(tmp0))) {
            var7.add(var3);
        }
        var7.sort(Comparator.comparingDouble(() -> {
    double var2 = mc.player.distanceToSqr(Vec3.atCenterOf(arg0));
    if (isRaytrace() && WorldUtils.canSeeBlock(arg0)) {
        var2 = (var2 - 1000.0);
    }
    return var2;
}));
        var9 = var7.iterator();
        label_13:
        if (var9.hasNext()) {
            var10 = ((BlockPos) var9.next());
            if (arg1.isOutOfRange(var10)) break label_13;
            if (isRaytrace()) {
                if (!(WorldUtils.canSeeBlock(var10))) break label_13;
            }
            var tmp1 = mc.level.getBlockState(var10);
            Object var11 = tmp1;
            if (tmp1.canBeReplaced()) break label_13;
            var tmp2 = new BlockPos[1];
            tmp2[0] = var10;
            if ((!(WorldUtils.canBreak(tmp2))) || (var11.is(Blocks.COBWEB))) break label_13;
            if (!(arg1.isValid(var10))) {
                break label_13;
            }
            return var10;
        }
        return null;
    }

    private BlockPos getSelfFaceBlock() {
        if ((mc.player == null) || (mc.level == null)) {
            return null;
        }
        if (mc.player.isVisuallyCrawling()) return null;
        Object var1 = BlockPos.containing(mc.player.getEyePosition());
        var tmp0 = new BlockPos[1];
        tmp0[0] = var1;
        if (WorldUtils.canBreak(tmp0)) {
            var tmp1 = new BlockPos[1];
            tmp1[0] = var1;
            if (!(WorldUtils.isReplaceable(tmp1))) return var1;
        }
        Object var2 = mc.player.blockPosition().above();
        var tmp2 = new BlockPos[1];
        tmp2[0] = var2;
        if (WorldUtils.canBreak(tmp2)) {
            var tmp3 = new BlockPos[1];
            tmp3[0] = var2;
            if (!(WorldUtils.isReplaceable(tmp3))) return var2;
        }
        return null;
    }

    public boolean isProtectedTerrainBase(BlockPos arg0) {
        if ((((!(this.terrain.getValue())) || (this.activeTerrainPair == null)) || (arg0 == null)) || (!(arg0.equals(this.activeTerrainPair.basePos())))) break label_0;
        return isRealTerrainBase(arg0);
    }

    private boolean allSidesTerraformed(Player arg0, SpeedMineModule arg1) {
        Object var3 = arg0.blockPosition();
        int var4 = 0;
        Object var5 = Direction.Plane.HORIZONTAL.iterator();
        label_0:
        if (var5.hasNext()) {
            Object var6 = ((Direction) var5.next());
            Object var8 = var3.relative(var6);
            Object var9 = var8.below();
            if ((arg1.isOutOfRange(var8)) || (arg1.isOutOfRange(var9))) break label_0;
            var tmp0 = mc.level.getBlockState(var8);
            Object var7 = tmp0;
            if (tmp0.getBlock().defaultDestroyTime() >= 0.0f) break label_1;
            if (!(arg1.sixB.getValue())) break label_0;
            if (var7.getBlock() == Blocks.BEDROCK) break label_1;
            break label_0;
            label_1:
            var4 = 1;
            Object var10 = mc.level.getBlockState(var9);
            if (var10.getBlock() != Blocks.OBSIDIAN) {
                if (var10.getBlock() == Blocks.BEDROCK) {
                }
            } else {
            }
            int var11 = 0;
            if (!(!var11)) {
                break label_0;
            }
            return false;
        }
        return var4;
    }

    public AutoMineModule.TerrainPair getBestTerrainPair(Player arg0) {
        Object var2 = speedMine();
        if (arg0 != null && mc.level != null && mc.player != null) {
            if (var2 != null) break label_1;
        }
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        return null;
        label_1:
        if (!(allSidesTerraformed(arg0, var2))) break label_2;
        this.activeTerrainPair = null;
        this.activeTerrainTarget = null;
        return null;
        label_2:
        Object var3 = arg0.getUUID();
        Object var4 = HoleUtils.getTargetFeetPosition(arg0);
        if (this.activeTerrainPair != null && var3.equals(this.activeTerrainTarget)) {
            Object var6 = this.activeTerrainPair.surroundPos();
            Object var7 = this.activeTerrainPair.basePos();
            if (var2.isOutOfRange(var6)) break label_3;
            int var8 = (!(var2.isOutOfRange(var7)) ? 1 : 0);
            int var9 = (var4.distManhattan(var6) <= 2 ? 1 : 0);
            Object var10 = mc.level.getBlockState(var6);
            Object var11 = mc.level.getBlockState(var7);
            if (var10.getBlock().defaultDestroyTime() >= 0.0f) break label_8;
            if (var2.sixB.getValue()) {
                if (var10.getBlock() == Blocks.BEDROCK) break label_8;
            }
            if (var2.isMining(var6)) {
                label_8:
            } else {
            }
            int var12 = 0;
            if (var11.getBlock().defaultDestroyTime() < 0.0f && var11.getBlock() != Blocks.BEDROCK && var11.getBlock() != Blocks.OBSIDIAN) {
                if (this.pendingTerrainPlacements.contains(var7)) {
                }
            } else {
            }
            int var5 = 0;
            int var13 = 0;
            if (!(!var8) && !(!var9) && !(!var12) && !(!var5)) return this.activeTerrainPair;
        }
        var5 = new HashSet();
        if (!(this.avoidSharing.getValue())) break label_18;
        var5.addAll(HoleUtils.getFeetPositions(mc.player, true, false, true));
        var6 = HoleUtils.getInsidePositions(mc.player).iterator();
        while (var6.hasNext()) {
            var7 = ((BlockPos) var6.next());
            var8 = Direction.Plane.HORIZONTAL.iterator();
            while (var8.hasNext()) {
                var9 = ((Direction) var8.next());
                var5.add(var7.relative(var9));
            }
        }
        label_18:
        var6 = null;
        var7 = 1.7976931348623157E308;
        var9 = Direction.Plane.HORIZONTAL.iterator();
        while (var9.hasNext()) {
            var10 = ((Direction) var9.next());
            var13 = var4.relative(var10);
            Object var14 = var13.below();
            if (var2.isOutOfRange(var13)) break label_19;
            if (var2.isOutOfRange(var14)) {
                break label_19;
            }
            Object var15 = mc.level.getBlockState(var13);
            Object var16 = mc.level.getBlockState(var14);
            if (var15.getBlock().defaultDestroyTime() < 0.0f) {
                if (!(var2.isMining(var13))) break label_19;
            }
            if (var16.getBlock().defaultDestroyTime() < 0.0f && var16.getBlock() != Blocks.BEDROCK && var16.getBlock() != Blocks.OBSIDIAN && !(this.pendingTerrainPlacements.contains(var14))) {
                break label_19;
            }
            double var17 = 0.0;
            if (!(this.avoidSharing.getValue())) break label_24;
            if (!(var5.contains(var13))) {
                if (!(var5.contains(var14))) break label_24;
            }
            var17 = (var17 + 100000.0);
            label_24:
            if (var16.getBlock() != Blocks.OBSIDIAN) {
                if (var16.getBlock() == Blocks.BEDROCK) {
                }
            } else {
            }
            var12 = 0;
            int var19 = 0;
            if (!(!var12)) {
                var17 = (var17 - 20000.0);
                break label_29;
            }
            if (var16.canBeReplaced()) break label_29;
            var17 = (var17 + (((double) var16.getBlock().defaultDestroyTime()) * 10.0));
            label_29:
            if (!(var15.canBeReplaced())) {
                var17 = (var17 - 10000.0);
            }
            if (!(var2.isMining(var13))) {
                if (var2.isMining(var14)) {
                }
            } else {
            }
            var11 = 0;
            int var20 = 0;
            if (!(!var11)) {
                var17 = (var17 - 50000.0);
            }
            var17 = (var17 + mc.player.distanceToSqr(Vec3.atCenterOf(var13)));
            if ((var17 + mc.player.distanceToSqr(Vec3.atCenterOf(var13))) >= var7) {
                break label_19;
            }
            var7 = var17;
            var6 = new AutoMineModule.TerrainPair(var10, var13, var14, var17);
        }
        this.activeTerrainPair = var6;
        this.activeTerrainTarget = (var6 != null ? var3 : null);
        return var6;
    }

    public void placePendingTerrain() {
        if ((!(this.terrainPlace.getValue())) || (this.pendingTerrainPlacements.isEmpty())) {
            return;
        }
        if (mc.player == null) break label_2;
        if (mc.level == null) {
            label_2:
            return;
        }
        Object var2 = speedMine();
        if (var2 == null) return;
        if (!(var2.whileEating.getValue()) && EntityUtils.isEating()) return;
        if (var2.switchMode.getValue().equalsIgnoreCase("None")) {
        } else {
            if (!(var2.switchMode.getValue().equalsIgnoreCase("AltSwap"))) {
                if (var2.switchMode.getValue().equalsIgnoreCase("AltPickup")) {
                }
            } else {
            }
        }
        int var1 = InventoryUtils.find(0, InventoryUtils.INVENTORY_END, InventoryUtils.HOTBAR_END);
        if (var1 == -1) {
            var1 = InventoryUtils.findHardestBlock(0, 8);
        }
        if (var1 == -1) return;
        Object var3 = new ArrayList();
        Object var4 = new ArrayList(this.pendingTerrainPlacements).iterator();
        while (var4.hasNext()) {
            Object var5 = ((BlockPos) var4.next());
            if (var2.isOutOfRange(var5)) {
                var3.add(var5);
                break label_13;
            }
            Object var6 = mc.level.getBlockState(var5);
            if (var6.getBlock() == Blocks.OBSIDIAN) {
                var3.add(var5);
                var2.dropPrimaryIfInvalid((arg1) -> !(arg1.equals(arg0)));
                var2.dropSecondaryIfInvalid((arg1) -> !(arg1.equals(arg0)));
                break label_13;
            }
            if (!(var6.canBeReplaced())) break label_13;
            if (!(WorldUtils.isPlaceable(var5))) {
                break label_13;
            }
            Object var7 = WorldUtils.getDirection(var5, false);
            if (var7 == null) {
                var7 = WorldUtils.getClosestDirection(var5, true);
            }
            if (var7 == null) {
                break label_13;
            }
            int var8 = mc.player.getInventory().getSelectedSlot();
            InventoryUtils.switchSlot(var2.switchMode.getValue(), var1, var8);
            if (!(var2.rotate.getValue().equalsIgnoreCase("Packet")) && !(var2.rotate.getValue().equalsIgnoreCase("Normal"))) {
                if (var2.rotate.getValue().equalsIgnoreCase("Silent")) {
                }
            } else {
            }
            if (!(var2.render.getValue().equalsIgnoreCase("Both"))) {
                if (var2.render.getValue().equalsIgnoreCase("Fill")) {
                }
            } else {
            }
            int var9 = WorldUtils.placeBlock(InteractionHand.MAIN_HAND, 1, 0, true, true, false);
            InventoryUtils.switchBack(var2.switchMode.getValue(), var1, var8);
            if (!var9) {
                break label_13;
            }
            var3.add(var5);
            var2.dropPrimaryIfInvalid((arg1) -> !(arg1.equals(arg0)));
            var2.dropSecondaryIfInvalid((arg1) -> !(arg1.equals(arg0)));
        }
        Objects.requireNonNull(this.pendingTerrainPlacements);
        var3.forEach(this.pendingTerrainPlacements::remove);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (((PingBypassFlags.isPingBypassActive()) || (mc.player == null)) || (mc.level == null)) break label_74;
        Object var2 = speedMine();
        if ((var2 == null) || (!(var2.isToggled()))) break label_74;
        if ((!(this.antiCrawl.getValue())) || (!(mc.player.isVisuallyCrawling()))) break label_4;
        Object var3 = mc.player.blockPosition();
        var tmp0 = new BlockPos[1];
        tmp0[0] = var3.below();
        if (!(WorldUtils.canBreak(tmp0))) break label_0;
        var tmp1 = new BlockPos[1];
        tmp1[0] = var3.below();
        if (WorldUtils.isReplaceable(tmp1)) break label_0;
        var tmp2 = new BlockPos[1];
        tmp2[0] = var3.below(2);
        if ((!(WorldUtils.isReplaceable(tmp2))) || (HoleUtils.getSingleHole(var3.below(2), 1.0, false) != null)) break label_1;
        label_0:
        Object var4 = var3.above();
        break label_2;
        label_1:
        var4 = var3.below();
        label_2:
        if ((!(var2.isValid(var4))) || (var2.isOutOfRange(var4))) break label_4;
        if (var2.isMining(var4)) return;
        var2.handle(var4, 0);
        return;
        label_4:
        if (!(this.face.getValue())) break label_6;
        var3 = getSelfFaceBlock();
        if (((var3 == null) || (!(var2.isValid(var3)))) || (var2.isOutOfRange(var3))) break label_6;
        if (var2.isMining(var3)) return;
        var2.handle(var3, 0);
        return;
        label_6:
        if (tickUpFallback(var2)) break label_74;
        var3 = var2.getPrimary();
        var4 = var2.getSecondary();
        Object var5 = BlockPos.containing(mc.player.getEyePosition());
        Object var6 = mc.player.blockPosition().above();
        Object var7 = mc.player.blockPosition();
        if ((var3 == null) || (var3.getPriority())) break label_8;
        if (((var3.getPosition().equals(var5)) || (var3.getPosition().equals(var6))) || (var3.getPosition().equals(var7))) break label_7;
        if (!(var3.getPosition().equals(var7.below()))) break label_8;
        label_7:
        if (!(mc.level.getBlockState(var3.getPosition()).canBeReplaced())) break label_8;
        var2.dropPrimary();
        var3 = null;
        label_8:
        if ((var4 == null) || (var4.getPriority())) break label_10;
        if (!(var4.getPosition().equals(var5)) && !(var4.getPosition().equals(var6)) && !(var4.getPosition().equals(var7))) {
            if (!(var4.getPosition().equals(var7.below()))) break label_10;
        }
        if (!(mc.level.getBlockState(var4.getPosition()).canBeReplaced())) break label_10;
        var2.dropSecondary();
        var4 = null;
        label_10:
        Object var8 = var2.getTarget();
        if (var8 != null) {
            if (this.ignoreNaked.getValue() && EntityUtils.isNaked(var8.player())) {
            }
        } else {
        }
        Object var9 = var8;
        if (var4 != null) {
        } else {
        }
        Object var10 = null;
        if (var4 != null) {
        } else {
        }
        int var11 = 0;
        if (var3 != null && var3.getPriority() > 0) {
            var tmp3 = new BlockPos[1];
            tmp3[0] = var3.getPosition();
            if (!(WorldUtils.isReplaceable(tmp3))) break label_74;
        }
        if (var10 != null && var11 > 0) {
            var tmp4 = new BlockPos[1];
            tmp4[0] = var10;
            if (!(WorldUtils.isReplaceable(tmp4))) break label_74;
        }
        if (var3 != null && !(var3.getPriority()) && var2.sixB.getValue() && var3.getState().getBlock() == Blocks.BEDROCK && var9 != null) {
            if (var2.isTargetSurroundPosition(var3.getPosition(), var9.player())) break label_74;
        }
        if (var2.doubleMine.getValue()) {
            if (!(var2.isMineTimerReady())) return;
            if (var9 == null) {
                var2.setDoubleEngaged(false);
            }
            if (var9 == null) break label_74;
            Object var12 = new ArrayList();
            Object var13 = HoleUtils.getInsidePositions(var9.player());
            var12.addAll(var13);
            Object var14 = HoleUtils.getFeetPositions(var9.player(), true, false, true);
            var12.addAll(var14);
            Object var15 = var14.iterator();
            while (var15.hasNext()) {
                Object var16 = ((BlockPos) var15.next());
                var12.add(var16.below());
                Object var17 = Direction.Plane.HORIZONTAL.iterator();
                while (var17.hasNext()) {
                    Object var18 = ((Direction) var17.next());
                    var12.add(var16.relative(var18).below());
                }
            }
            var15 = var13.iterator();
            while (var15.hasNext()) {
                var16 = ((BlockPos) var15.next());
                var17 = Direction.Plane.HORIZONTAL.iterator();
                while (var17.hasNext()) {
                    var18 = ((Direction) var17.next());
                    var12.add(var16.relative(var18));
                    var12.add(var16.relative(var18).below());
                }
                var12.add(var16.above());
                var12.add(var16.below());
            }
            if (this.terrain.getValue()) {
            } else {
            }
            var15 = null;
            if (var15 != null) {
                var12.add(var15.surroundPos());
                var12.add(var15.basePos());
            }
            if (this.logic.getValue().equalsIgnoreCase("Grim")) {
            } else {
            }
            var16 = null;
            if (var16 != null) {
                var12.add(var16);
            }
            if (var3 != null && var3.isTerrainBase() && isRealTerrainBase(var3.getPosition())) {
                var2.dropPrimary();
                var3 = null;
            }
            var2.setDoubleEngaged(true);
            if (((var3 == null) || (var3.getPriority())) || (var3.isTerrainBase())) break label_42;
            var17 = var2.isTargetSurroundPosition(var3.getPosition(), var9.player());
            if (!var17) {
                var2.dropPrimary();
                var18 = null;
                break label_42;
            }
            if (var3.isInstantMine()) break label_42;
            var18 = var3.getPosition();
            if (!(HoleUtils.getInsidePositions(var9.player()).contains(var18))) break label_39;
            int var19 = (mc.level.getBlockState(var18).canBeReplaced() ? 1 : 0);
            if (var12.contains(var18)) {
                if (!var19) break label_42;
            }
            var2.dropPrimary();
            Object var20 = null;
            label_42:
            if (((var4 == null) || (var4.getPriority())) || (var4.isHolding())) break label_46;
            var17 = var4.getPosition();
            if (!(HoleUtils.getInsidePositions(var9.player()).contains(var17))) break label_43;
            var18 = (mc.level.getBlockState(var17).canBeReplaced() ? 1 : 0);
            if (var12.contains(var17)) {
                if (!var18) break label_46;
            }
            var2.dropSecondary();
            var19 = null;
            label_46:
            var17 = var15;
            var18 = () -> {
    if ((!(this.terrain.getValue())) || (arg0 == null)) break label_16;
    Object var4 = arg0.basePos();
    Object var5 = arg0.surroundPos();
    Object var6 = mc.level.getBlockState(var4);
    Object var7 = mc.level.getBlockState(var5);
    if (var6.getBlock() != Blocks.OBSIDIAN) {
        if (var6.getBlock() != Blocks.BEDROCK) break label_1;
    }
    break label_2;
    label_1:
    label_2:
    int var8 = 0;
    if (!var8 && this.terrainPlace.getValue() && !(this.pendingTerrainPlacements.contains(var4))) {
        this.pendingTerrainPlacements.add(var4);
    }
    if (isPhased(arg1.player())) {
        if (arg2.primaryCamping()) {
            Object var9 = arg2.getPrimary().getPosition();
            if (!(var9.equals(var4)) && !(var9.equals(var5))) return;
        }
        if (arg2.getPrimary() != null) {
            if (arg2.getSecondary() == null) {
            }
        } else {
        }
        var9 = 0;
        if (!var8 && !(var6.canBeReplaced())) {
            if ((arg2.isMining(var4)) || (!var9)) break label_12;
            arg2.handle(var4, 0);
            Object var10 = arg2.getPrimary();
            if (var10 != null && var10.getPosition().equals(var4)) {
                var10.setTerrainBase(true);
            }
            break label_12;
        }
        if ((!var8) || (var7.canBeReplaced())) break label_12;
        var10 = arg2.getPrimary();
        if (var10 == null) break label_10;
        int var11 = (var10.getPosition().equals(var5) ? 1 : 0);
        if (var11) break label_12;
        arg2.handle(var5, 0);
        Object var12 = arg2.getPrimary();
        if ((var12 == null) || (!(var12.getPosition().equals(var5)))) break label_12;
        var12.setTerrainSurround(true);
        label_12:
        break label_16;
    }
    if (!var8 && !(var6.canBeReplaced()) && !(arg2.isMining(var4))) {
        arg2.handle(var4, 0);
    }
    if ((var7.canBeReplaced()) || (arg2.isMining(var5))) break label_16;
    if ((!var8) || (!(arg2.doubleMine.getValue()))) break label_15;
    var9 = ((BlockPos) HoleUtils.getFeetPositions(arg1.player(), true, false, true).stream().filter((arg1) -> !(arg1.equals(arg0))).filter((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced())).filter((arg1) -> {
    if ((arg0.isMining(arg1)) || (arg0.isInvalid(arg1))) break label_0;
    return !(arg0.isOutOfRange(arg1));
}).min(Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(Vec3.atCenterOf(arg0)))).orElse(null));
    if (var9 == null) break label_15;
    arg2.handle(var9, 0);
    label_15:
    arg2.handle(var5, 0);
    var9 = arg2.getPrimary();
    if ((var9 == null) || (!(var9.getPosition().equals(var5)))) break label_16;
    var9.setTerrainSurround(true);
    label_16:
};
            var19 = () -> {
    Object var4 = arg0.getPrimary();
    if ((var4 == null) || (!(arg0.primaryCamping()))) break label_0;
    int var5 = (HoleUtils.getInsidePositions(arg1.player()).contains(var4.getPosition()) ? 1 : 0);
    if (!(arg0.primaryCamping())) break label_2;
    if (!var5) break label_28;
    label_2:
    if (var4 != null && arg0.sixB.getValue()) {
        if (var4.getState().getBlock() == Blocks.BEDROCK) break label_28;
    }
    Object var6 = HoleUtils.getInsidePositions(arg1.player()).stream().filter((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced())).toList();
    if (var6.size() >= 2) break label_28;
    if (this.terrain.getValue() && var4 != null && var4.isTerrainSurround()) {
        if (!(var4.isStarted())) break label_28;
    }
    if ((!(this.terrain.getValue())) || (arg2 == null)) break label_10;
    Object var7 = mc.level.getBlockState(arg2.basePos());
    if (var7.getBlock() != Blocks.OBSIDIAN) {
        if (var7.getBlock() == Blocks.BEDROCK) {
        }
    } else {
    }
    int var8 = 0;
    if (!var8) return;
    if (!(arg0.isMining(arg2.basePos()))) {
        if (!(arg0.isMining(arg2.surroundPos()))) break label_10;
    }
    if (arg0.slotsFull()) return;
    label_10:
    if (this.logic.getValue().equals("NCP")) {
        var7 = HoleUtils.getFeetPositions(arg1.player(), true, false, true).stream().filter((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced())).filter(() -> {
    if (isRaytrace()) {
        if (WorldUtils.canSeeBlock(arg0)) {
        }
    } else {
    }
    return false;
}).toList().iterator();
        while (var7.hasNext()) {
            var8 = ((BlockPos) var7.next());
            if (arg0.slotsFull()) {
                break label_14;
            }
            if (!(arg0.isMining(var8)) && !(arg0.isInvalid(var8)) && !(arg0.isOutOfRange(var8))) {
                arg0.handle(var8, 0);
            }
        }
        label_14:
        break label_28;
    }
    var7 = new HashSet();
    if ((!(this.avoidSharing.getValue())) || (mc.player == null)) break label_19;
    var7.addAll(HoleUtils.getFeetPositions(mc.player, true, false, true));
    var8 = HoleUtils.getInsidePositions(mc.player).iterator();
    while (var8.hasNext()) {
        Object var9 = ((BlockPos) var8.next());
        Object var10 = Direction.Plane.HORIZONTAL.iterator();
        while (var10.hasNext()) {
            Object var11 = ((Direction) var10.next());
            var7.add(var9.relative(var11));
        }
    }
    label_19:
    var8 = HoleUtils.getFeetPositions(arg1.player(), true, false, true).stream().filter((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced())).filter(() -> {
    if (isRaytrace()) {
        if (WorldUtils.canSeeBlock(arg0)) {
        }
    } else {
    }
    return false;
}).sorted(Comparator.comparingDouble(() -> {
    if (!(this.avoidSharing.getValue())) break label_0;
    double var5 = (arg0.contains(arg3) ? 100000.0 : 0.0);
    if (isRaytrace() && !(WorldUtils.canSeeBlock(arg3))) {
        var5 = (var5 + 500000.0);
        break label_3;
    }
    if (!(WorldUtils.canSeeBlock(arg3))) break label_3;
    var5 = (var5 - 50000.0);
    label_3:
    if (arg1.sixB.getValue() && mc.level.getBlockState(arg3).getBlock() == Blocks.BEDROCK) {
        var5 = (var5 - 200000.0);
    }
    if (this.terrain.getValue() && arg2 != null && arg3.equals(arg2.surroundPos())) {
        var5 = (var5 - 100000.0);
        break label_11;
    }
    Object var7 = arg3.below();
    Object var8 = mc.level.getBlockState(var7);
    if (var8.getBlock() != Blocks.OBSIDIAN) {
        if (var8.getBlock() == Blocks.BEDROCK) {
        }
    } else {
    }
    int var9 = 0;
    int var10 = arg1.isMining(var7);
    if (!(!var10)) {
        var5 = (var5 - 50000.0);
        break label_11;
    }
    if (!(!var9)) {
        var5 = (var5 - 20000.0);
        break label_11;
    }
    if (!(this.terrain.getValue())) break label_11;
    var5 = (var5 + 50000.0);
    label_11:
    return (var5 + mc.player.distanceToSqr(Vec3.atCenterOf(arg3)));
})).toList();
    var9 = var5;
    var10 = var8.iterator();
    while (var10.hasNext()) {
        var11 = ((BlockPos) var10.next());
        if (!var9 && arg0.slotsFull()) {
            break label_28;
        }
        if (!(arg0.isMining(var11))) {
            if (arg0.isInvalid(var11)) break label_20;
            if (arg0.isOutOfRange(var11)) {
                break label_20;
            }
            if (!(arg0.sixB.getValue())) break label_23;
            int var12 = (mc.level.getBlockState(var11).getBlock() == Blocks.BEDROCK ? 1 : 0);
            if (((!var12) || (!(arg0.doubleMine.getValue()))) || (arg0.getPrimary() != null)) break label_25;
            Object var13 = ((BlockPos) var8.stream().filter((arg1) -> !(arg1.equals(arg0))).filter((arg0) -> mc.level.getBlockState(arg0).getBlock() != Blocks.BEDROCK).filter((arg1) -> {
    if ((arg0.isMining(arg1)) || (arg0.isInvalid(arg1))) break label_0;
    return !(arg0.isOutOfRange(arg1));
}).findFirst().orElse(null));
            if (var13 == null) break label_25;
            arg0.handle(var13, 0);
            label_25:
            arg0.handle(var11, 0);
            var9 = 0;
        }
        if (!var9 && arg0.slotsFull()) {
            break label_28;
        }
    }
    label_28:
};
            var20 = () -> {
    Object var3 = arg0.getPrimary();
    if (this.terrain.getValue() && var3 != null && var3.isTerrainSurround()) {
        if (!(var3.isStarted())) break label_4;
    }
    int var4 = Mth.floor(arg1.player().getY());
    Object var5 = HoleUtils.getInsidePositions(arg1.player()).stream().filter((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced())).filter(() -> {
    if (isRaytrace()) {
        if (WorldUtils.canSeeBlock(arg0)) {
        }
    } else {
    }
    return false;
}).sorted(Comparator.comparingDouble((arg1) -> (arg1.getY() > arg0 ? 1.0 : 0.0))).toList().iterator();
    while (var5.hasNext()) {
        Object var6 = ((BlockPos) var5.next());
        if ((arg0.isInvalid(var6)) || (arg0.isOutOfRange(var6))) break label_3;
        if (arg0.phaseSlotsFull()) {
            break label_4;
        }
        arg0.handle(var6, 0);
        if (!(arg0.phaseSlotsFull())) break label_3;
        break label_4;
        label_3:
    }
    label_4:
};
            Object var21 = null;
            if (this.priority.getValue().equalsIgnoreCase("Head")) {
                var21 = getHeadTarget(var9.player(), var2);
            }
            if (var21 == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
                var21 = getGrimBedrockHeadTarget(var9.player(), var2);
            }
            if (var21 != null && !(var2.isMining(var21))) {
                var2.handle(var21, 0);
            }
            if (var21 == null) {
                var20.run();
            }
            if (var21 == null && this.terrain.getValue()) {
                var18.run();
            }
            if (var21 == null) {
                var19.run();
            }
            break label_74;
        }
        var12 = null;
        var13 = null;
        var14 = 0;
        if (var9 == null) return;
        if (((var3 == null) || (var3.getPriority())) || (var3.isTerrainBase())) break label_59;
        var15 = var2.isTargetSurroundPosition(var3.getPosition(), var9.player());
        if (!(HoleUtils.getInsidePositions(var9.player()).contains(var3.getPosition()))) break label_55;
        var16 = (mc.level.getBlockState(var3.getPosition()).canBeReplaced() ? 1 : 0);
        if (!var15) {
            var2.dropPrimary();
            var3 = null;
            break label_59;
        }
        if (!(!var16)) {
            var2.dropPrimary();
            var3 = null;
            break label_59;
        }
        if (var2.sixB.getValue() && var3.getState().getBlock() == Blocks.BEDROCK) return;
        label_59:
        if (this.priority.getValue().equalsIgnoreCase("Head")) {
            var12 = getHeadTarget(var9.player(), var2);
        }
        if (var12 == null && this.logic.getValue().equalsIgnoreCase("Grim")) {
            var13 = getGrimBedrockHeadTarget(var9.player(), var2);
            var12 = var13;
        }
        if ((var12 != null) || (!(this.terrain.getValue()))) break label_66;
        var15 = getBestTerrainPair(var9.player());
        if (var15 == null) break label_66;
        var16 = mc.level.getBlockState(var15.basePos());
        var17 = mc.level.getBlockState(var15.surroundPos());
        if (var16.getBlock() != Blocks.OBSIDIAN) {
            if (var16.getBlock() == Blocks.BEDROCK) {
            }
        } else {
        }
        var18 = 0;
        if (!var18 && !(var16.canBeReplaced())) {
            var12 = var15.basePos();
            var14 = 1;
            if ((!(this.terrainPlace.getValue())) || (this.pendingTerrainPlacements.contains(var12))) break label_66;
            this.pendingTerrainPlacements.add(var12);
            break label_66;
        }
        if (var17.canBeReplaced()) break label_66;
        var12 = var15.surroundPos();
        label_66:
        if (var12 != null) break label_69;
        var15 = Mth.floor(var9.player().getY());
        var16 = HoleUtils.getInsidePositions(var9.player()).stream().filter((arg1) -> arg1.getY() <= arg0).filter((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced())).filter(() -> {
    if (isRaytrace()) {
        if (WorldUtils.canSeeBlock(arg0)) {
        }
    } else {
    }
    return false;
}).toList();
        if (!(var16.isEmpty())) {
        } else {
        }
        var12 = var9.position();
        if (((var12 == null) || (!(isRaytrace()))) || (WorldUtils.canSeeBlock(var12))) break label_69;
        var12 = null;
        label_69:
        if (var12 == null) return;
        if (var3 != null && var12.equals(var3.getPosition())) return;
        if (var3 != null && !(var3.getPriority()) && !(var3.isTerrainBase()) && var2.isTargetSurroundPosition(var3.getPosition(), var9.player())) {
            if ((var13 == null) || (!(var12.equals(var13)))) {
                return;
            }
        }
        var2.handle(var12, 0);
        if (!var14) break label_74;
        var15 = var2.getPrimary();
        if ((var15 == null) || (!(var15.getPosition().equals(var12)))) break label_74;
        var15.setTerrainBase(true);
        label_74:
    }

    private BlockPos getHeadTarget(Player arg0, SpeedMineModule arg1) {
        if ((arg0 == null) || (mc.level == null)) {
            return null;
        }
        int var3 = Mth.floor(arg0.getY());
        int var4 = (var3 + 2);
        Object var5 = arg1.getPrimary();
        if (var5 != null && var5.isInstantMine() && var5.getPosition().getY() == var4 && arg1.isTargetSurroundPosition(var5.getPosition(), arg0)) return var5.getPosition();
        Object var6 = HoleUtils.getInsidePositions(arg0).iterator();
        label_3:
        if (var6.hasNext()) {
            Object var7 = ((BlockPos) var6.next());
            Object var8 = new BlockPos(var7.getX(), var4, var7.getZ());
            if ((!(arg1.isValid(var8))) || (arg1.isOutOfRange(var8))) break label_3;
            if (isRaytrace()) {
                if (!(WorldUtils.canSeeBlock(var8))) break label_3;
            }
            if (mc.level.getBlockState(var8).canBeReplaced()) {
                break label_3;
            }
            return var8;
        }
        var6 = arg0.blockPosition().above(2);
        if ((!(arg1.isValid(var6))) || (arg1.isOutOfRange(var6))) break label_8;
        if (isRaytrace()) {
            if (!(WorldUtils.canSeeBlock(var6))) break label_8;
        }
        if (!(mc.level.getBlockState(var6).canBeReplaced())) return var6;
        label_8:
        return null;
    }

    private boolean tickUpFallback(SpeedMineModule arg0) {
        if (this.priority.getValue().equalsIgnoreCase("Leg") && this.raytrace.getValue()) {
            if (this.upFallback.getValue()) break label_1;
        }
        resetUpCombo();
        return false;
        label_1:
        Object var3 = arg0.getTarget();
        if (var3 != null) {
        } else {
        }
        Object var2 = null;
        Object var4 = null;
        if (var2 != null) {
            if (EntityUtils.isGhost(var2)) {
            }
            resetUpCombo();
            return false;
        }
        if (!(HoleUtils.isPlayerInHole(var2))) {
            resetUpCombo();
            return false;
        }
        Object var5 = HoleUtils.getFeetPositions(var2, true, false, true);
        if (this.upComboPos == null) break label_10;
        if (!(var5.contains(this.upComboPos.below()))) {
            resetUpCombo();
            break label_10;
        }
        if (!(isPhased(var2))) {
            if (elsewhereDealsMoreDamage(arg0, var2, this.upComboPos)) {
            }
            resetUpCombo();
            return false;
        }
        return tickUpComboCycle(arg0, this.upComboPos);
        label_10:
        int var6 = var5.stream().anyMatch((arg0) -> !(mc.level.getBlockState(arg0).canBeReplaced()));
        int var7 = var5.stream().anyMatch(() -> {
    if (!(isMinableLegCandidate(arg1, arg0))) break label_0;
    return WorldUtils.canSeeBlock(arg1);
});
        if ((!var6) || (!(!var7))) {
            return false;
        }
        if (isPhased(var2)) return false;
        Object var8 = null;
        Object var9 = var5.iterator();
        label_14:
        if (!(var9.hasNext())) break label_16;
        Object var10 = ((BlockPos) var9.next());
        Object var11 = var10.above();
        if (arg0.isOutOfRange(var11)) break label_14;
        if (!(WorldUtils.canSeeBlock(var11))) {
            break label_14;
        }
        var8 = var11;
        break label_16;
        label_16:
        if (var8 == null) return false;
        this.upComboPos = var8;
        return tickUpComboCycle(arg0, var8);
    }

    private boolean elsewhereDealsMoreDamage(SpeedMineModule arg0, Player arg1, BlockPos arg2) {
        Object var4 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
        if (var4 == null) return false;
        float var5 = var4.getBestPlaceDamage();
        if (var5 <= 0.0f) return false;
        Object var6 = arg2.above();
        float var7 = DamageUtils.getCrystalDamage(arg1, PositionUtils.extrapolate(arg1, var4.extrapolation.getValue().intValue()), var6, arg2, var4.ignoreTerrain.getValue());
        return var5 > var7;
    }

    private boolean tickUpComboCycle(SpeedMineModule arg0, BlockPos arg1) {
        if (mc.level.getBlockState(arg1).canBeReplaced()) {
            placeUpComboObsidian(arg0, arg1);
            return true;
        }
        if (!(arg0.isMining(arg1)) && arg0.isValid(arg1) && !(arg0.isOutOfRange(arg1))) {
            arg0.handle(arg1, 0);
        }
        return true;
    }

    private void placeUpComboObsidian(SpeedMineModule arg0, BlockPos arg1) {
        if (!(WorldUtils.isPlaceable(arg1))) return;
        Object var3 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
        if (var3 == null) break label_1;
        break label_2;
        label_1:
        label_2:
        Object var4 = "Silent";
        int var5 = var4.equalsIgnoreCase("AltSwap");
        int var6 = InventoryUtils.find(Items.OBSIDIAN, 0, (!(!var5) ? InventoryUtils.INVENTORY_END : InventoryUtils.HOTBAR_END));
        if (var6 == -1) {
            var6 = InventoryUtils.findHardestBlock(0, 8);
        }
        if (var6 == -1) return;
        Object var7 = WorldUtils.getDirection(arg1, false);
        if (var7 == null) {
            var7 = WorldUtils.getClosestDirection(arg1, true);
        }
        if (var7 == null) return;
        int var8 = mc.player.getInventory().getSelectedSlot();
        InventoryUtils.switchSlot(var4, var6, var8);
        if (!(arg0.render.getValue().equalsIgnoreCase("Both"))) {
            if (arg0.render.getValue().equalsIgnoreCase("Fill")) {
            }
        } else {
        }
        WorldUtils.placeBlock(var7, InteractionHand.MAIN_HAND, this.upFallbackRotate.getValue(), 1, true, false);
        InventoryUtils.switchBack(var4, var6, var8);
    }


    public record TerrainPair(Direction direction, BlockPos surroundPos, BlockPos basePos, double score) {

        public TerrainPair(Direction arg0, BlockPos arg1, BlockPos arg2, double arg3) {
            this.direction = arg0;
            this.surroundPos = arg1;
            this.basePos = arg2;
            this.score = arg3;
        }

        public Direction direction() {
            return this.direction;
        }

        public BlockPos surroundPos() {
            return this.surroundPos;
        }

        public BlockPos basePos() {
            return this.basePos;
        }

        public double score() {
            return this.score;
        }

    }
}
