package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.DamageUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PistonHelper;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "PistonKick", description = "Pushes players out of holes or solid blocks using pistons.", category = Module.Category.COMBAT)
public final class PistonKickModule extends Module {
    public static PistonKickModule INSTANCE;
    public NumberSetting delay;
    public NumberSetting pushDelay;
    public ModeSetting power;
    public BooleanSetting airPlace;
    public BooleanSetting oneTwelve;
    public NumberSetting targetRange;
    public BooleanSetting inAirTarget;
    public ModeSetting rotate;
    public BooleanSetting breakCrystal;
    public NumberSetting placeRange;
    public NumberSetting breakRange;
    public NumberSetting maxLocalDamage;
    public BooleanSetting safety;
    public ModeSetting swapAction;
    public BooleanSetting consecutive;
    public BooleanSetting merge;
    public BooleanSetting pauseOnEat;
    public BooleanSetting strictDirection;
    public BooleanSetting render;
    private BlockPos targetPhasedPos;
    private BlockPos pistonPos;
    private BlockPos redstonePos;
    private BlockPos supportPos;
    private final List supportPositions;
    private Direction pushDir;
    private Player currentTarget;
    private long lastActionMs;
    private long extendedSinceMs;
    private long leverTurnedOnMs;

    public PistonKickModule() {
        this.delay = new NumberSetting("Delay", "Delay between pushes (ticks)", 1, 0, 10);
        this.pushDelay = new NumberSetting("PushDelay", "Duration to hold piston extended before retracting (ms)", 150, 50, 500);
        var tmp0 = new String[2];
        tmp0[0] = "Redstone";
        tmp0[1] = "Lever";
        this.power = new ModeSetting("Power", "How the piston is powered.", "Redstone", tmp0);
        this.airPlace = new BooleanSetting("AirPlace", "Allow placing when not on ground.", false);
        this.oneTwelve = new BooleanSetting("1.12", "Place piston and power in the same tick.", true);
        this.targetRange = new NumberSetting("TargetRange", "Target range.", 5.0f, 1.0f, 6.0f);
        this.inAirTarget = new BooleanSetting("InAirTarget", "Target players in the air.", true);
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "Normal";
        tmp1[2] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Silently rotate when placing.", "Silent", tmp1);
        this.breakCrystal = new BooleanSetting("BreakCrystal", "Break end crystals blocking piston path.", false);
        this.placeRange = new NumberSetting("PlaceRange", "Block place range.", 5.0f, 1.0f, 6.0f);
        this.breakRange = new NumberSetting("BreakRange", "Crystal break range.", new BooleanSetting.Visibility(this.breakCrystal, true), 4.5f, 1.0f, 6.0f);
        this.maxLocalDamage = new NumberSetting("MaxLocalDamage", "Max self-damage allowed to break a crystal.", new BooleanSetting.Visibility(this.breakCrystal, true), 8.0f, 0.0f, 20.0f);
        this.safety = new BooleanSetting("Safety", "Never break a crystal that would kill you.", new BooleanSetting.Visibility(this.breakCrystal, true), true);
        this.swapAction = new ModeSetting("Swap", "How to swap to required items.", "Silent", InventoryUtils.SWITCH_MODES);
        this.consecutive = new BooleanSetting("Consecutive", "Keep targeting the same player consecutively.", true);
        this.merge = new BooleanSetting("Merge", "Burst piston and power packets together.", false);
        this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pause while eating or using items.", false);
        this.strictDirection = new BooleanSetting("StrictDirection", "Use raytrace to verify line-of-sight before placing piston/redstone (required on servers that check raytrace).", false);
        this.render = new BooleanSetting("Render", "Render placed blocks using RendersModule.", true);
        this.supportPositions = new ArrayList();
        this.lastActionMs = 0L;
        this.extendedSinceMs = 0L;
        this.leverTurnedOnMs = 0L;
        INSTANCE = this;
    }

    public static PistonKickModule getInstance() {
        return INSTANCE;
    }

    public void onEnable() {
        resetState();
    }

    public void onDisable() {
        resetState();
    }

    private void resetState() {
        this.targetPhasedPos = null;
        this.pistonPos = null;
        this.redstonePos = null;
        this.supportPos = null;
        this.supportPositions.clear();
        this.pushDir = null;
        this.currentTarget = null;
        this.extendedSinceMs = 0L;
        this.leverTurnedOnMs = 0L;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.pauseOnEat.getValue() && EntityUtils.isEating()) return;
        long var5 = System.currentTimeMillis();
        if ((var5 - this.lastActionMs) < (this.delay.getValue().longValue() * 50L)) return;
        int var7 = findPiston();
        int var8 = findPower();
        if (var7 == -1) break label_4;
        if (var8 != -1) break label_5;
        label_4:
        resetState();
        return;
        label_5:
        if (this.currentTarget == null) break label_10;
        if (this.currentTarget.isAlive() && !(this.currentTarget.isSpectator())) {
            if (mc.player.distanceToSqr(this.currentTarget) > ((double) (this.targetRange.getValue().floatValue() * this.targetRange.getValue().floatValue()))) {
            }
            resetState();
            break label_10;
        }
        Object var9 = getPhasedSolidBlocks(this.currentTarget);
        if (var9.isEmpty()) {
            resetState();
            break label_10;
        }
        if (this.targetPhasedPos != null) {
            if (var9.contains(this.targetPhasedPos)) break label_10;
        }
        this.targetPhasedPos = ((BlockPos) var9.get(0));
        label_10:
        if (this.currentTarget != null) break label_14;
        var9 = getEnemies();
        Object var10 = var9.iterator();
        label_11:
        if (!(var10.hasNext())) break label_14;
        Object var11 = ((Player) var10.next());
        if (!(this.inAirTarget.getValue())) {
            if (!(var11.onGround())) break label_11;
        }
        var tmp0 = getPhasedSolidBlocks(var11);
        Object var12 = tmp0;
        if (tmp0.isEmpty()) {
            break label_11;
        }
        this.currentTarget = var11;
        this.targetPhasedPos = ((BlockPos) var12.get(0));
        break label_14;
        label_14:
        if (this.currentTarget != null) {
            if (this.targetPhasedPos == null) {
            }
            resetState();
            return;
        }
        if (this.breakCrystal.getValue()) {
            breakNearbyCrystals(this.currentTarget);
        }
        if ((this.pistonPos == null) || (this.pushDir == null)) break label_32;
        var9 = isPistonPlacedAndFacing(this.pistonPos, this.pushDir);
        var10 = isPistonExtended(this.pistonPos);
        var11 = isPistonPowered(this.pistonPos);
        var12 = this.power.getValue().equalsIgnoreCase("Lever");
        if (!(!var10)) {
            if (this.extendedSinceMs == 0L) {
                this.extendedSinceMs = var5;
            }
            long var13 = Math.max(50L, this.pushDelay.getValue().longValue());
            if ((var5 - this.extendedSinceMs) >= Math.max(50L, this.pushDelay.getValue().longValue())) {
                if (this.leverTurnedOnMs > 0L && (var5 - this.leverTurnedOnMs) < 50L) {
                }
                this.lastActionMs = var5;
                return;
            }
            if (!(!var12) && this.redstonePos != null && isLever(this.redstonePos) && isLeverPowered(this.redstonePos)) {
                clickLever(this.redstonePos);
                this.leverTurnedOnMs = 0L;
                this.lastActionMs = var5;
                return;
            }
            if (!(!var12) && this.redstonePos != null && !(isLeverPowered(this.redstonePos)) && !(!var10)) {
                this.lastActionMs = var5;
                return;
            }
            Object var15 = getPhasedSolidBlocks(this.currentTarget);
            if (var15.isEmpty()) {
                resetState();
                return;
            }
            this.pistonPos = null;
            this.redstonePos = null;
            this.supportPos = null;
            this.pushDir = null;
            this.extendedSinceMs = 0L;
            this.leverTurnedOnMs = 0L;
            this.lastActionMs = var5;
        } else {
            this.extendedSinceMs = 0L;
        }
        if (!(!var9) && !(!var11) && !var10) {
            this.lastActionMs = var5;
            return;
        }
        var13 = this.airPlace.getValue();
        if ((var9) || (PistonHelper.canPlace(this.pistonPos, null, var13, true))) break label_32;
        if (!var12 && this.redstonePos != null && this.redstonePos.equals(this.pistonPos.below())) {
            if ((PistonHelper.canPlace(this.redstonePos, null, var13, true)) || (isPowerSource(this.redstonePos))) break label_29;
        }
        if (!(this.supportPositions.isEmpty())) {
            IF (this.supportPositions.stream().anyMatch(() -> {
    if (!(PistonHelper.canPlace(arg1, null, arg0, true))) {
        if (isSolidBlock(arg1)) {
        }
    } else {
    }
    return false;
})) break label_29;
        }
        if (this.supportPos == null) break label_30;
        if (PistonHelper.canPlace(this.supportPos, null, var13, true)) break label_29;
        if (!(isSolidBlock(this.supportPos))) break label_30;
        label_29:
        break label_31;
        label_30:
        label_31:
        int var14 = 0;
        var15 = 0;
        if (var14) break label_32;
        resetState();
        label_32:
        if (this.pistonPos != null) {
            if (this.pushDir == null) {
            }
            if (!(searchPlacement(this.currentTarget))) return;
        }
        if ((this.pistonPos == null) || (this.pushDir == null)) {
            return;
        }
        var9 = this.airPlace.getValue();
        var10 = this.power.getValue().equalsIgnoreCase("Lever");
        var11 = this.supportPositions.iterator();
        label_37:
        if (var11.hasNext()) {
            var12 = ((BlockPos) var11.next());
            if ((isSolidBlock(var12)) || (!(PistonHelper.canPlace(var12, null, var9, true)))) break label_37;
            var tmp1 = findSupportBlock();
            var13 = tmp1;
            if (tmp1 == -1) {
                break label_37;
            }
            placeBlock(var13, var12, null);
            this.lastActionMs = var5;
            if (this.oneTwelve.getValue()) break label_37;
            if (this.merge.getValue()) {
                break label_37;
            }
            return;
        }
        if ((((this.supportPos == null) || (isSolidBlock(this.supportPos))) || (this.supportPositions.contains(this.supportPos))) || (!(PistonHelper.canPlace(this.supportPos, null, var9, true)))) break label_41;
        var tmp2 = findSupportBlock();
        int var4 = tmp2;
        if (tmp2 == -1) break label_41;
        placeBlock(var4, this.supportPos, null);
        this.lastActionMs = var5;
        if (!(this.oneTwelve.getValue()) && !(this.merge.getValue())) return;
        label_41:
        if ((var10) || (this.redstonePos == null)) break label_42;
        var tmp3 = (this.redstonePos.equals(this.pistonPos.below()) ? 1 : 0);
        int var3 = tmp3;
        var11 = tmp3;
        if (!(!var3) && !(isPowerSource(this.redstonePos)) && PistonHelper.canPlace(this.redstonePos, null, var9, true)) {
            placeBlock(var8, this.redstonePos, null);
            this.lastActionMs = var5;
            if (!(this.oneTwelve.getValue()) && !(this.merge.getValue())) return;
        }
        var tmp4 = (!(isPistonPlacedAndFacing(this.pistonPos, this.pushDir)) ? 1 : 0);
        int var2 = tmp4;
        var12 = tmp4;
        if (!(!var2) && PistonHelper.canPlace(this.pistonPos, null, var9, true)) {
            placeBlock(var7, this.pistonPos, this.pushDir);
            this.lastActionMs = var5;
            if (!(this.oneTwelve.getValue()) && !(this.merge.getValue())) return;
        }
        if (this.redstonePos != null && !(isPistonPowered(this.pistonPos)) && !(isPowerSource(this.redstonePos))) {
            if (!(!var10) && !(isPistonPlacedAndFacing(this.pistonPos, this.pushDir))) {
                if (!(!var9)) {
                }
            } else {
            }
            var13 = 0;
            var14 = 0;
            if ((!var13) || (!(PistonHelper.canPlace(this.redstonePos, null, var9, true)))) break label_51;
            placeBlock(var8, this.redstonePos, null);
            this.lastActionMs = var5;
            if (!var10) break label_51;
            clickLever(this.redstonePos);
            this.leverTurnedOnMs = var5;
            label_51:
            break label_53;
        }
        if ((((((!var10) || (this.redstonePos == null)) || (!(isLever(this.redstonePos)))) || (isLeverPowered(this.redstonePos))) || (isPistonExtended(this.pistonPos))) || (this.extendedSinceMs != 0L)) break label_53;
        clickLever(this.redstonePos);
        this.leverTurnedOnMs = var5;
        this.lastActionMs = var5;
        label_53:
    }

    private boolean searchPlacement(Player arg0) {
        Object var2 = getPhasedSolidBlocks(arg0);
        if (var2.isEmpty()) return false;
        if (var2.size() >= 2) {
            Object var3 = mc.player.getDirection();
            Object var4 = var3.getAxis();
            var2.sort((arg1, arg2) -> {
    if (arg0 == Direction.Axis.Z) {
        if (arg1.getZ() == arg2.getZ()) {
        }
        if (arg0 == Direction.Axis.X && arg1.getX() != arg2.getX()) {
        }
    } else {
    }
    int var3 = 0;
    return (!(!var3) ? -1 : 1);
});
        }
        var3 = var2.iterator();
        label_2:
        if (var3.hasNext()) {
            var4 = ((BlockPos) var3.next());
            if (!(searchPlacementForBlock(arg0, var4))) {
                break label_2;
            }
            this.targetPhasedPos = var4;
            return true;
        }
        return false;
    }

    private boolean searchPlacementForBlock(Player arg0, BlockPos arg1) {
        Object var3 = new ArrayList();
        Object var4 = mc.player.getDirection();
        var3.add(var4);
        Object var5 = Direction.Plane.HORIZONTAL.iterator();
        while (var5.hasNext()) {
            Object var6 = ((Direction) var5.next());
            if (var6 != var4) break label_1;
            break label_0;
            label_1:
            var3.add(var6);
        }
        label_2:
        var5 = arg1.above();
        var6 = this.airPlace.getValue();
        int var7 = this.power.getValue().equalsIgnoreCase("Lever");
        Object var8 = var3.iterator();
        label_3:
        if (var8.hasNext()) {
            Object var9 = ((Direction) var8.next());
            if (isDestinationOpen(arg1, var9)) break label_4;
            break label_3;
            label_4:
            Object var13 = var5.relative(var9.getOpposite());
            if (mc.level.getBlockState(var5).isAir()) break label_5;
            if (!(mc.level.getBlockState(var5).canBeReplaced())) break label_3;
            label_5:
            if (inRange(var13)) break label_6;
            break label_3;
            label_6:
            int var14 = isPistonPlacedAndFacing(var13, var9);
            if (mc.level.getBlockState(var13).isAir()) break label_7;
            if (mc.level.getBlockState(var13).canBeReplaced()) {
                label_7:
            } else {
            }
            int var12 = 0;
            int var15 = 0;
            if (!var14 && !var12) {
                break label_3;
            }
            if (!var14) {
                if (PistonHelper.canPlace(var13, null, var6, true)) {
                }
            } else {
            }
            int var11 = 0;
            int var16 = 0;
            if (!var11) break label_53;
            if (isPistonPowered(var13)) {
                this.supportPos = null;
                this.pistonPos = var13;
                this.redstonePos = null;
                this.pushDir = var9;
                return true;
            }
            if (!(!var7)) {
                Object var17 = var13.below();
                if (var6) break label_15;
                int var18 = (!(isSolidBlock(var17)) ? 1 : 0);
                Object var19 = new ArrayList();
                Object var20 = var13.relative(var9.getOpposite());
                Object var21 = var20.below();
                Object var22 = new ArrayList();
                if (!(!var18)) {
                    var22.add(var17);
                }
                if (!var6 && !(isSolidBlock(var21))) {
                    var22.add(var21);
                }
                var19.add(new PistonKickModule.LeverCandidate(var20, var22));
                Object var23 = var13.relative(var9.getCounterClockWise());
                Object var24 = var23.below();
                Object var25 = new ArrayList();
                if (!(!var18)) {
                    var25.add(var17);
                }
                if (!var6 && !(isSolidBlock(var24))) {
                    var25.add(var24);
                }
                var19.add(new PistonKickModule.LeverCandidate(var23, var25));
                if (isSolidBlock(var23)) {
                    Object var26 = var23.above();
                    Object var27 = new ArrayList();
                    if (!(!var18)) {
                        var27.add(var17);
                    }
                    var19.add(new PistonKickModule.LeverCandidate(var26, var27));
                }
                var26 = var13.relative(var9.getClockWise());
                var27 = var26.below();
                Object var28 = new ArrayList();
                if (!(!var18)) {
                    var28.add(var17);
                }
                if (!var6 && !(isSolidBlock(var27))) {
                    var28.add(var27);
                }
                var19.add(new PistonKickModule.LeverCandidate(var26, var28));
                if (isSolidBlock(var26)) {
                    Object var29 = var26.above();
                    Object var30 = new ArrayList();
                    if (!(!var18)) {
                        var30.add(var17);
                    }
                    var19.add(new PistonKickModule.LeverCandidate(var29, var30));
                }
                if (isSolidBlock(var20)) {
                    var29 = var13.relative(var9.getOpposite(), 2);
                    var30 = new ArrayList();
                    if (!(!var18)) {
                        var30.add(var17);
                    }
                    var19.add(new PistonKickModule.LeverCandidate(var29, var30));
                    Object var31 = var20.above();
                    Object var32 = new ArrayList();
                    if (!(!var18)) {
                        var32.add(var17);
                    }
                    var19.add(new PistonKickModule.LeverCandidate(var31, var32));
                }
                var29 = var13.relative(var9.getOpposite()).below();
                var30 = new ArrayList();
                if (!(!var18)) {
                    var30.add(var17);
                }
                var19.add(new PistonKickModule.LeverCandidate(var29, var30));
                var31 = var19.iterator();
                label_31:
                if (var31.hasNext()) {
                    var32 = ((PistonKickModule.LeverCandidate) var31.next());
                    Object var34 = var32.leverPos;
                    if (((((var34.equals(var13.relative(var9))) || (var34.equals(var5))) || (var34.equals(var5.below()))) || (var34.equals(arg1.relative(var9)))) || (var34.equals(var5.relative(var9)))) break label_31;
                    if (!(inRange(var34))) {
                        break label_31;
                    }
                    if (!(isLever(var34)) && !(mc.level.getBlockState(var34).isAir())) {
                        if (mc.level.getBlockState(var34).canBeReplaced()) {
                        }
                    } else {
                    }
                    int var33 = 0;
                    int var35 = 0;
                    if (!var33) {
                        break label_31;
                    }
                    int var36 = 1;
                    Object var37 = var32.neededSupports.iterator();
                    label_37:
                    if (!(var37.hasNext())) break label_42;
                    Object var38 = ((BlockPos) var37.next());
                    if (!(var38.equals(var5)) && !(var38.equals(var5.below())) && !(var38.equals(arg1.relative(var9)))) {
                        if (var38.equals(var5.relative(var9))) {
                        }
                        var36 = 0;
                        break label_42;
                    }
                    if (!(inRange(var38))) {
                        var36 = 0;
                        break label_42;
                    }
                    if (isSolidBlock(var38)) break label_37;
                    if (PistonHelper.canPlace(var38, null, var6, true)) {
                        break label_37;
                    }
                    var36 = 0;
                    break label_42;
                    label_42:
                    if (!var36) {
                        break label_31;
                    }
                    this.supportPositions.clear();
                    this.supportPositions.addAll(var32.neededSupports);
                    if (var32.neededSupports.isEmpty()) {
                    } else {
                    }
                    null.supportPos = ((BlockPos) var32.neededSupports.get(0));
                    this.pistonPos = var13;
                    this.redstonePos = var34;
                    this.pushDir = var9;
                    return true;
                }
                break label_53;
            }
            var17 = findBestPowerPos(var13, var5, arg1.relative(var9), var9);
            if (var17 != null) {
                this.supportPos = null;
                this.pistonPos = var13;
                this.redstonePos = var17;
                this.pushDir = var9;
                return true;
            }
            var tmp0 = new Direction[5];
            tmp0[0] = Direction.UP;
            tmp0[1] = var9.getOpposite();
            tmp0[2] = var9.getClockWise();
            tmp0[3] = var9.getCounterClockWise();
            tmp0[4] = Direction.DOWN;
            var18 = tmp0;
            var19 = var18.length;
            var20 = 0;
            while (var20 < var19) {
                var21 = var18[var20];
                var22 = var13.relative(var21);
                if ((((((var22.equals(var13.relative(var9))) || (var22.equals(var5))) || (var22.equals(var5.below()))) || (var22.equals(arg1.relative(var9)))) || (var22.equals(var5.relative(var9)))) || (!(inRange(var22)))) break label_52;
                if (!(mc.level.getBlockState(var22).isAir())) {
                    if (!(mc.level.getBlockState(var22).canBeReplaced())) break label_52;
                }
                if (!var6 && !(PistonHelper.canPlace(var22, null, false, true))) {
                    break label_52;
                }
                this.supportPos = null;
                this.pistonPos = var13;
                this.redstonePos = var22;
                this.pushDir = var9;
                return true;
                label_52:
                var20++;
            }
            label_53:
            if ((var6) || (!var12)) break label_3;
            var tmp1 = var13.below();
            Object var10 = tmp1;
            if (!(inRange(tmp1))) {
                break label_3;
            }
            if (!(!var7)) {
                if (!(PistonHelper.canPlace(var10, null, false, true)) && !(isSolidBlock(var10))) {
                    break label_3;
                }
                var17 = new ArrayList();
                var18 = var13.relative(var9.getOpposite());
                var19 = var18.below();
                var20 = new ArrayList();
                if (!(isSolidBlock(var10))) {
                    var20.add(var10);
                }
                if (!(isSolidBlock(var19))) {
                    var20.add(var19);
                }
                var17.add(new PistonKickModule.LeverCandidate(var18, var20));
                var21 = var13.relative(var9.getCounterClockWise());
                var22 = var21.below();
                var23 = new ArrayList();
                if (!(isSolidBlock(var10))) {
                    var23.add(var10);
                }
                if (!(isSolidBlock(var22))) {
                    var23.add(var22);
                }
                var17.add(new PistonKickModule.LeverCandidate(var21, var23));
                var24 = var13.relative(var9.getClockWise());
                var25 = var24.below();
                var26 = new ArrayList();
                if (!(isSolidBlock(var10))) {
                    var26.add(var10);
                }
                if (!(isSolidBlock(var25))) {
                    var26.add(var25);
                }
                var17.add(new PistonKickModule.LeverCandidate(var24, var26));
                var27 = var17.iterator();
                label_62:
                if (var27.hasNext()) {
                    var28 = ((PistonKickModule.LeverCandidate) var27.next());
                    var30 = var28.leverPos;
                    if (((((var30.equals(var13.relative(var9))) || (var30.equals(var5))) || (var30.equals(var5.below()))) || (var30.equals(arg1.relative(var9)))) || (var30.equals(var5.relative(var9)))) break label_62;
                    if (!(inRange(var30))) {
                        break label_62;
                    }
                    if (!(isLever(var30)) && !(mc.level.getBlockState(var30).isAir())) {
                        if (mc.level.getBlockState(var30).canBeReplaced()) {
                        }
                    } else {
                    }
                    var29 = 0;
                    var31 = 0;
                    if (!var29) {
                        break label_62;
                    }
                    var32 = 1;
                    var33 = var28.neededSupports.iterator();
                    label_68:
                    if (!(var33.hasNext())) break label_72;
                    var34 = ((BlockPos) var33.next());
                    if (!(var34.equals(var5)) && !(var34.equals(var5.below())) && !(var34.equals(arg1.relative(var9)))) {
                        if (var34.equals(var5.relative(var9))) {
                        }
                        var32 = 0;
                        break label_72;
                    }
                    if (!(inRange(var34))) break label_71;
                    if (isSolidBlock(var34)) break label_68;
                    if (!(PistonHelper.canPlace(var34, null, false, true))) break label_71;
                    break label_68;
                    label_71:
                    var32 = 0;
                    break label_72;
                    label_72:
                    if (!var32) {
                        break label_62;
                    }
                    this.supportPositions.clear();
                    this.supportPositions.addAll(var28.neededSupports);
                    if (var28.neededSupports.isEmpty()) {
                    } else {
                    }
                    null.supportPos = ((BlockPos) var28.neededSupports.get(0));
                    this.pistonPos = var13;
                    this.redstonePos = var30;
                    this.pushDir = var9;
                    return true;
                }
                break label_3;
            }
            if (!(PistonHelper.canPlace(var10, null, false, true)) && !(isPowerSource(var10))) {
                break label_3;
            }
            this.supportPositions.clear();
            this.supportPos = null;
            this.pistonPos = var13;
            this.redstonePos = var10;
            this.pushDir = var9;
            return true;
        }
        return false;
    }

    private BlockPos findBestPowerPos(BlockPos arg0, BlockPos arg1, BlockPos arg2, Direction arg3) {
        Object var6 = null;
        double var7 = 1.7976931348623157E308;
        int var9 = this.airPlace.getValue();
        var tmp0 = new Direction[5];
        tmp0[0] = Direction.DOWN;
        tmp0[1] = arg3.getOpposite();
        tmp0[2] = arg3.getClockWise();
        tmp0[3] = arg3.getCounterClockWise();
        tmp0[4] = Direction.UP;
        Object var5 = tmp0;
        Object var10 = tmp0;
        int var11 = var10.length;
        int var12 = 0;
        while (var12 < var11) {
            Object var13 = var10[var12];
            Object var16 = arg0.relative(var13);
            if ((((var16.equals(arg1)) || (var16.equals(arg1.below()))) || (var16.equals(arg2))) || (var16.equals(arg2.above()))) break label_4;
            if (var16.equals(arg0.relative(arg3))) {
                break label_4;
            }
            if (isPowerSource(var16)) return var16;
            if ((!(PistonHelper.canPlace(var16, null, var9, true))) || (!(inRange(var16)))) break label_4;
            double var14 = mc.player.distanceToSqr(Vec3.atCenterOf(var16));
            if (mc.player.distanceToSqr(Vec3.atCenterOf(var16)) >= var7) {
                break label_4;
            }
            var7 = var14;
            var6 = var16;
            label_4:
            var12++;
        }
        return var6;
    }

    private void placeBlock(int arg0, BlockPos arg1, Direction arg2) {
        if ((arg0 == -1) || (mc.player == null)) {
            return;
        }
        if (this.strictDirection.getValue()) {
        } else {
        }
        var tmp0 = PistonHelper.getPlaceSide(arg1, (arg0) -> true);
        Object var4 = tmp0;
        Object var5 = tmp0;
        if (var4 != null) break label_6;
        if (this.airPlace.getValue()) {
            if (this.power.getValue().equalsIgnoreCase("Lever") && this.pistonPos != null && this.pushDir != null && arg1.equals(this.pistonPos.relative(this.pushDir.getOpposite()))) {
                var4 = this.pushDir;
                break label_6;
            }
            var4 = PistonHelper.getClickSide(arg1);
            if (var4 != null) break label_6;
            var4 = Direction.UP;
            break label_6;
        }
        return;
        label_6:
        int var6 = mc.player.getInventory().getSelectedSlot();
        doSwap(arg0, var6);
        if (!(shouldRotate())) break label_7;
        doRotate(arg1, var4);
        if (arg2 == null) break label_7;
        snapPistonFacing(arg2);
        label_7:
        PistonHelper.placeBlock(arg1, var4, false);
        restoreSlot(arg0, var6);
        if (this.render.getValue()) {
            LightningClient.RENDER_MANAGER.getRenderPositions().removeIf((arg1) -> arg1.getPos().equals(arg0));
            LightningClient.RENDER_MANAGER.getRenderPositions().add(new RenderPosition(arg1));
        }
    }

    private void snapPistonFacing(Direction arg0) {
        switch (PistonKickModule.1.$SwitchMap$net$minecraft$core$Direction[arg0.ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            case 3 -> { break label_2; }
            case 4 -> { break label_3; }
            default -> { break label_4; }
        }
        label_0:
        break label_5;
        label_1:
        break label_5;
        label_2:
        break label_5;
        label_3:
        break label_5;
        label_4:
        label_5:
        float var2 = 0.0f;
        LightningClient.ROTATION_MANAGER.silentRotate(var2, 0.0f);
    }

    private boolean isPistonPlacedAndFacing(BlockPos arg0, Direction arg1) {
        if (mc.level == null) return false;
        Object var3 = mc.level.getBlockState(arg0);
        if ((var3.getBlock() instanceof PistonBaseBlock)) return var3.getValue(PistonBaseBlock.FACING) == arg1;
        return false;
    }

    private boolean isPistonExtended(BlockPos arg0) {
        if (mc.level == null) return false;
        Object var2 = mc.level.getBlockState(arg0);
        if ((var2.getBlock() instanceof PistonBaseBlock)) return ((Boolean) var2.getValue(PistonBaseBlock.EXTENDED)).booleanValue();
        return false;
    }

    private boolean isPistonPowered(BlockPos arg0) {
        if (mc.level == null) return false;
        if (this.power.getValue().equalsIgnoreCase("Lever")) {
            Object var2 = Direction.values();
            int var3 = var2.length;
            int var4 = 0;
            while (var4 < var3) {
                Object var5 = var2[var4];
                Object var6 = arg0.relative(var5);
                if (!(isLever(var6))) break label_3;
                if (!(isLeverPowered(var6))) {
                    break label_3;
                }
                return true;
                label_3:
                var4++;
            }
            return mc.level.hasNeighborSignal(arg0);
        }
        var2 = Direction.values();
        var3 = var2.length;
        var4 = 0;
        while (var4 < var3) {
            var5 = var2[var4];
            if (!(isRedstone(arg0.relative(var5)))) {
            } else {
                return true;
            }
            var4++;
        }
        return false;
    }

    private boolean isPowerSource(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0).getBlock();
        if (this.power.getValue().equalsIgnoreCase("Lever")) return var2 == Blocks.LEVER;
        if (var2 != Blocks.REDSTONE_BLOCK) {
            if (var2 == Blocks.REDSTONE_TORCH) {
            }
        } else {
        }
        return false;
    }

    private boolean isRedstone(BlockPos arg0) {
        if (mc.level == null) return false;
        Object var2 = mc.level.getBlockState(arg0).getBlock();
        if (var2 != Blocks.REDSTONE_BLOCK) {
            if (var2 == Blocks.REDSTONE_TORCH) {
            }
        } else {
        }
        return false;
    }

    private boolean isLever(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        return mc.level.getBlockState(arg0).getBlock() == Blocks.LEVER;
    }

    private boolean isLeverPowered(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0);
        if (var2.getBlock() == Blocks.LEVER) return ((Boolean) var2.getValue(LeverBlock.POWERED)).booleanValue();
        return false;
    }

    private void clickLever(BlockPos arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = PistonHelper.getClickSide(arg0);
        if (var2 == null) {
            var2 = Direction.UP;
        }
        PistonHelper.clickBlock(arg0, var2, shouldRotate());
    }

    private boolean isDestinationOpen(BlockPos arg0, Direction arg1) {
        if (mc.level == null) return false;
        Object var3 = arg0.relative(arg1);
        Object var4 = var3.above();
        Object var5 = mc.level.getBlockState(var3);
        Object var6 = mc.level.getBlockState(var4);
        if (!(var5.isAir())) {
            if (((!(var5.canBeReplaced())) || (var5.blocksMotion())) || (isImmovable(var5))) {
                return false;
            }
        }
        if (!(var6.isAir())) {
            if (var6.canBeReplaced() && !(var6.blocksMotion()) && !(isImmovable(var6))) {
            }
        } else {
        }
        return false;
    }

    private boolean isImmovable(BlockState arg0) {
        Object var2 = arg0.getBlock();
        if (var2 != Blocks.OBSIDIAN && var2 != Blocks.BEDROCK && var2 != Blocks.CRYING_OBSIDIAN && var2 != Blocks.RESPAWN_ANCHOR && var2 != Blocks.ENDER_CHEST) {
            if (arg0.isSolid()) {
            }
        } else {
        }
        return false;
    }

    public List getPhasedSolidBlocks(Player arg0) {
        Object var2 = new ArrayList();
        if ((arg0 == null) || (mc.level == null)) {
            return var2;
        }
        Object var3 = EntityUtils.getPhasedBlocks(arg0);
        Object var4 = var3.iterator();
        while (var4.hasNext()) {
            Object var5 = ((BlockPos) var4.next());
            Object var6 = mc.level.getBlockState(var5);
            if (!(isHardPhasedBlock(var6))) {
                break label_2;
            }
            var2.add(var5);
        }
        if (!(var2.isEmpty())) break label_7;
        var4 = BlockPos.containing(arg0.getX(), (arg0.getY() + 0.1), arg0.getZ());
        if (isHardPhasedBlock(mc.level.getBlockState(var4))) {
            var2.add(var4);
            break label_7;
        }
        var5 = arg0.blockPosition();
        if (isHardPhasedBlock(mc.level.getBlockState(var5))) {
            var2.add(var5);
            break label_7;
        }
        var6 = var4.above();
        if (!(isHardPhasedBlock(mc.level.getBlockState(var6)))) break label_7;
        var2.add(var6);
        label_7:
        return var2;
    }

    public BlockPos getPhasedSolidBlock(Player arg0) {
        Object var2 = getPhasedSolidBlocks(arg0);
        if (var2.isEmpty()) {
        } else {
        }
        return ((BlockPos) var2.get(0));
    }

    private boolean isDecoyOrPassableBlock(BlockState arg0) {
        if (((arg0 == null) || (arg0.isAir())) || (arg0.canBeReplaced())) {
            return true;
        }
        Object var2 = arg0.getBlock();
        if (!(var2 instanceof WebBlock) && !(var2 instanceof ButtonBlock) && !(var2 instanceof MushroomBlock) && !(var2 instanceof LadderBlock) && !(var2 instanceof ScaffoldingBlock) && !(var2 instanceof FlowerBlock) && !(var2 instanceof FlowerPotBlock) && !(var2 instanceof TallFlowerBlock) && !(var2 instanceof VineBlock) && !(var2 instanceof TorchBlock) && !(var2 instanceof LeverBlock) && !(var2 instanceof PressurePlateBlock) && !(var2 instanceof SignBlock) && !(var2 instanceof BannerBlock) && !(var2 instanceof CarpetBlock) && !(var2 instanceof TripWireBlock) && !(var2 instanceof TripWireHookBlock) && !(var2 instanceof BaseRailBlock) && var2 != Blocks.COBWEB && var2 != Blocks.LADDER && var2 != Blocks.SCAFFOLDING && var2 != Blocks.VINE && var2 != Blocks.BROWN_MUSHROOM && var2 != Blocks.RED_MUSHROOM && var2 != Blocks.TORCH && var2 != Blocks.SOUL_TORCH) {
            if (var2 == Blocks.REDSTONE_TORCH) {
            }
        } else {
        }
        return false;
    }

    private boolean isHardPhasedBlock(BlockState arg0) {
        if (isDecoyOrPassableBlock(arg0)) return false;
        Object var2 = arg0.getBlock();
        if (((((((((((var2 == Blocks.OBSIDIAN) || (var2 == Blocks.BEDROCK)) || (var2 == Blocks.ENDER_CHEST)) || (var2 == Blocks.RESPAWN_ANCHOR)) || (var2 == Blocks.CRYING_OBSIDIAN)) || (var2 == Blocks.ANVIL)) || (var2 == Blocks.CHIPPED_ANVIL)) || (var2 == Blocks.DAMAGED_ANVIL)) || (var2 == Blocks.REINFORCED_DEEPSLATE)) || (var2 == Blocks.NETHERITE_BLOCK)) || (var2 == Blocks.ANCIENT_DEBRIS)) {
            return true;
        }
        if (!(arg0.isSolid())) break label_3;
        return arg0.blocksMotion();
    }

    private boolean inRange(BlockPos arg0) {
        double var2 = ((double) this.placeRange.getValue().floatValue());
        return mc.player.distanceToSqr(Vec3.atCenterOf(arg0)) <= (var2 * var2);
    }

    private void breakNearbyCrystals(Player arg0) {
        double var2 = ((double) this.breakRange.getValue().floatValue());
        Object var4 = arg0.getBoundingBox().inflate(var2);
        mc.level.getEntitiesOfClass(EndCrystal.class, var4, (arg1) -> {
    if (!(arg1.isAlive())) break label_0;
    return mc.player.distanceToSqr(arg1) <= (arg0 * arg0);
}).stream().min(Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(arg0))).ifPresent(() -> {
    float var2 = DamageUtils.getCrystalDamage(mc.player, null, arg0, false);
    if (var2 > this.maxLocalDamage.getValue().floatValue()) return;
    if (this.safety.getValue() && var2 >= (mc.player.getHealth() + mc.player.getAbsorptionAmount())) return;
    mc.getConnection().send(new ServerboundAttackPacket(arg0.getId()));
    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
});
    }

    private boolean shouldRotate() {
        return !(this.rotate.getValue().equalsIgnoreCase("None"));
    }

    private void doRotate(BlockPos arg0, Direction arg1) {
        Object var3 = Vec3.atCenterOf(arg0).add((((double) arg1.getStepX()) * 0.5), (((double) arg1.getStepY()) * 0.5), (((double) arg1.getStepZ()) * 0.5));
        Object var4 = RotationUtils.getRotations(var3);
        LightningClient.ROTATION_MANAGER.silentRotate(var4[0], var4[1]);
    }

    private void doSwap(int arg0, int arg1) {
        InventoryUtils.switchSlot(this.swapAction.getValue(), arg0, arg1);
    }

    private void restoreSlot(int arg0, int arg1) {
        InventoryUtils.switchBack(this.swapAction.getValue(), arg0, arg1);
    }

    private int findPower() {
        if (this.power.getValue().equalsIgnoreCase("Lever")) return findBlock(Blocks.LEVER);
        int var1 = findBlock(Blocks.REDSTONE_BLOCK);
        if (var1 != -1) return var1;
        return findBlock(Blocks.REDSTONE_TORCH);
    }

    private int findSupportBlock() {
        int var1 = findBlock(Blocks.OBSIDIAN);
        if (var1 != -1) return var1;
        var1 = findBlock(Blocks.CRYING_OBSIDIAN);
        if (var1 != -1) return var1;
        var1 = findBlock(Blocks.ENDER_CHEST);
        if (var1 != -1) return var1;
        var1 = findBlock(Blocks.NETHERITE_BLOCK);
        if (var1 != -1) return var1;
        var1 = findBlock(Blocks.COBBLESTONE);
        if (var1 != -1) return var1;
        return findPower();
    }

    private boolean isSolidBlock(BlockPos arg0) {
        if ((mc.level == null) || (arg0 == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0);
        if ((var2.isAir()) || (var2.canBeReplaced())) break label_2;
        return var2.isSolid();
    }

    private int findPiston() {
        int var1 = findBlock(Blocks.PISTON);
        if (var1 != -1) return var1;
        return findBlock(Blocks.STICKY_PISTON);
    }

    private int findBlock(Block arg0) {
        if (this.swapAction.getValue().equalsIgnoreCase("None")) {
            Object var4 = mc.player.getMainHandItem();
            if (var4.isEmpty()) break label_0;
            var tmp0 = var4.getItem();
            Object var3 = tmp0;
            if (!((tmp0 instanceof BlockItem))) break label_0;
            var tmp1 = ((BlockItem) var3);
            Object var2 = tmp1;
            if (tmp1.getBlock() == arg0) return mc.player.getInventory().getSelectedSlot();
            label_0:
            return -1;
        }
        if (!(this.swapAction.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.swapAction.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        var2 = InventoryUtils.HOTBAR_END;
        var3 = 0;
        while (var3 <= var2) {
            Object var6 = mc.player.getInventory().getItem(var3);
            if (var6.isEmpty()) break label_7;
            var tmp2 = var6.getItem();
            Object var5 = tmp2;
            if (!((tmp2 instanceof BlockItem))) break label_7;
            var tmp3 = ((BlockItem) var5);
            var4 = tmp3;
            if (tmp3.getBlock() != arg0) {
                break label_7;
            }
            return var3;
            label_7:
            var3++;
        }
        return -1;
    }

    private List getEnemies() {
        double var1 = ((double) this.targetRange.getValue().floatValue());
        return ((List) mc.level.players().stream().filter((arg0) -> arg0 != mc.player).filter((arg0) -> !(EntityUtils.isGhost(arg0))).filter((arg0) -> {
    if (arg0.isSpectator()) break label_0;
    return arg0.isAlive();
}).filter((arg0) -> !(LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString()))).filter((arg1) -> mc.player.distanceToSqr(arg1) <= (arg0 * arg0)).sorted(Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(arg0))).collect(Collectors.toList()));
    }


    record LeverCandidate(BlockPos leverPos, List neededSupports) {

        private LeverCandidate(BlockPos arg0, List arg1) {
            this.leverPos = arg0;
            this.neededSupports = arg1;
        }

        public BlockPos leverPos() {
            return this.leverPos;
        }

        public List neededSupports() {
            return this.neededSupports;
        }

    }

    class 1 {

        static {
            $SwitchMap$net$minecraft$core$Direction = new int[Direction.values().length];
            try {
                $SwitchMap$net$minecraft$core$Direction[Direction.SOUTH.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.NORTH.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.EAST.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.WEST.ordinal()] = 4;
            } catch (NoSuchFieldError ex11) {
                var0 = ex11;
            }
        }

    }
}
