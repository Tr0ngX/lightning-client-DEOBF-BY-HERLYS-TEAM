package lightning.utils.minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lightning.LightningClient;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HoleUtils implements IMinecraft {
    private static final Vec3i[] holeOffsets;
    private static final Vec3i[] singleOffsets;
    private static final Vec3i[] doubleXOffsets;
    private static final Vec3i[] doubleZOffsets;
    private static final Vec3i[] quadOffsets;

    public static boolean isPlayerInHole(Player arg0) {
        return getFeetPositions(arg0, true, true, false).stream().noneMatch((arg0) -> mc.level.getBlockState(arg0).canBeReplaced());
    }

    public static List getInsidePositions(Entity arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return Collections.emptyList();
        }
        Object var3 = new ArrayList();
        Object var4 = arg0.getBoundingBox();
        if (((arg0.isSwimming()) || (arg0.isVisuallySwimming())) || (arg0.isVisuallyCrawling())) break label_3;
        if (!((arg0 instanceof LivingEntity))) break label_2;
        var tmp0 = ((LivingEntity) arg0);
        Object var1 = tmp0;
        if (tmp0.isFallFlying()) break label_3;
        label_2:
        if ((arg0.getPose() == Pose.SWIMMING) || (arg0.getPose() == Pose.FALL_FLYING)) break label_3;
        if ((var4.maxY - var4.minY) <= 1.0) {
            label_3:
        } else {
        }
        int var2 = 0;
        int var5 = 0;
        if (!(!var2)) {
            float var6 = arg0.getYRot();
            double var7 = Math.toRadians(((double) var6));
            double var9 = -(Math.sin(var7));
            double var11 = Math.cos(var7);
            double var13 = arg0.getX();
            double var15 = arg0.getY();
            double var17 = arg0.getZ();
            double var19 = -0.9;
            while (var19 <= 0.9) {
                double var21 = -0.3;
                while (var21 <= 0.3) {
                    double var23 = ((var13 + (var9 * var19)) - (var11 * var21));
                    double var25 = ((var17 + (var11 * var19)) + (var9 * var21));
                    Object var27 = new BlockPos(((int) Math.floor(var23)), ((int) Math.floor(var15)), ((int) Math.floor(var25)));
                    if (var3.contains(var27)) {
                    } else {
                        var3.add(var27);
                    }
                    var21 = (var21 + 0.3);
                }
                var19 = (var19 + 0.2);
            }
            var19 = ((int) Math.floor(var4.minX));
            int var20 = ((int) Math.ceil(var4.maxX));
            var21 = ((int) Math.floor(var4.minY));
            int var22 = ((int) Math.ceil(var4.maxY));
            var23 = ((int) Math.floor(var4.minZ));
            int var24 = ((int) Math.ceil(var4.maxZ));
            var25 = var19;
            while (var25 < var20) {
                int var26 = var21;
                while (var26 < var22) {
                    var27 = var23;
                    while (var27 < var24) {
                        Object var28 = new BlockPos(var25, var26, var27);
                        if (var3.contains(var28)) {
                        } else {
                            var3.add(var28);
                        }
                        var27++;
                    }
                    var26++;
                }
                var25++;
            }
            break label_28;
        }
        var6 = ((int) Math.floor(var4.minX));
        var7 = ((int) Math.ceil(var4.maxX));
        int var8 = ((int) Math.floor(var4.minY));
        var9 = ((int) Math.floor(var4.minZ));
        int var10 = ((int) Math.ceil(var4.maxZ));
        var11 = ((int) Math.ceil(var4.maxY));
        int var12 = var6;
        while (var12 < var7) {
            var13 = var8;
            while (var13 < var11) {
                int var14 = var9;
                while (var14 < var10) {
                    var15 = new BlockPos(var12, var13, var14);
                    if (var3.contains(var15)) {
                    } else {
                        var3.add(var15);
                    }
                    var14++;
                }
                var13++;
            }
            var12++;
        }
        label_28:
        if (var3.isEmpty()) {
            var3.add(PositionUtils.getFlooredPosition(arg0));
        }
        return var3;
    }

    public static BlockPos getTargetFeetPosition(Player arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return BlockPos.ZERO;
        }
        Object var1 = PositionUtils.getFlooredPosition(arg0);
        if (!(mc.level.getBlockState(var1.below()).canBeReplaced())) break label_5;
        int var2 = 2;
        while (var2 <= 3) {
            Object var3 = var1.below(var2);
            if (mc.level.getBlockState(var3).canBeReplaced()) {
            } else {
                return var3.above();
            }
            var2++;
        }
        label_5:
        return var1;
    }

    public static HashSet getFeetPositions(Player arg0, boolean arg1, boolean arg2, boolean arg3) {
        return getFeetPositions(arg0, arg1, arg2, arg2, arg3);
    }

    public static HashSet getFeetPositions(Player arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4) {
        Object var5 = new HashSet();
        Object var6 = new HashSet();
        Object var7 = ((HitboxDesyncModule) LightningClient.MODULE_MANAGER.getModule(HitboxDesyncModule.class));
        Object var8 = arg0.getBoundingBox();
        double var9 = (var8.minY - Math.floor(var8.minY));
        if (var9 > 0.8) {
            break label_1;
        }
        label_1:
        int var11 = ((int) Math.floor(var8.minY));
        int var12 = ((int) Math.floor(var8.minX));
        int var13 = ((int) Math.floor(var8.maxX));
        int var14 = ((int) Math.floor(var8.minZ));
        int var15 = ((int) Math.floor(var8.maxZ));
        Object var16 = new BlockPos(((int) Math.floor(arg0.getX())), var11, ((int) Math.floor(arg0.getZ())));
        if (((mc.level == null) || (arg0.onGround())) || (!(mc.level.getBlockState(var16.below()).canBeReplaced()))) break label_4;
        int var17 = 2;
        while (var17 <= 3) {
            Object var18 = var16.below(var17);
            if (mc.level.getBlockState(var18).canBeReplaced()) break label_3;
            var11 = var18.above().getY();
            break label_4;
            label_3:
            var17++;
        }
        label_4:
        var17 = PositionUtils.getFlooredPosition(arg0);
        var6.add(new BlockPos(var17.getX(), var11, var17.getZ()));
        var18 = var12;
        while (var18 <= var13) {
            int var19 = var14;
            while (var19 <= var15) {
                if (var8.intersects(new AABB(((double) var18), ((double) var11), ((double) var19), ((double) (var18 + 1)), ((double) (var11 + 1)), ((double) (var19 + 1))))) {
                    var6.add(new BlockPos(var18, var11, var19));
                }
                var19++;
            }
            var18++;
        }
        if ((!arg1) || (mc.level == null)) break label_22;
        var18 = Set.copyOf(var6).iterator();
        while (var18.hasNext()) {
            var19 = ((BlockPos) var18.next());
            Object var20 = Direction.values();
            int var21 = var20.length;
            int var22 = 0;
            while (var22 < var21) {
                Object var23 = var20[var22];
                if (var23.getAxis().isVertical()) break label_20;
                Object var24 = var19.relative(var23);
                if (!(mc.level.getBlockState(var24).canBeReplaced())) break label_20;
                Object var25 = WorldUtils.getCollisions(var24);
                if (var25.isEmpty()) break label_20;
                Object var26 = var25.iterator();
                while (var26.hasNext()) {
                    Object var27 = ((Player) var26.next());
                    if (var27 == arg0) break label_19;
                    if (var27 == mc.player) {
                        if (arg0 == mc.player) break label_19;
                    }
                    if (!(!arg4)) {
                        if (var27 != arg0) break label_19;
                    }
                    if (arg0 == mc.player) {
                        if (!(LightningClient.FRIEND_MANAGER.contains(var27.getName().getString()))) break label_19;
                    }
                    if (Math.abs((var27.getY() - arg0.getY())) > 1.0) break label_19;
                    Object var28 = var27.getBoundingBox();
                    int var29 = ((int) Math.floor((var28.minX + 0.001)));
                    int var30 = ((int) Math.floor((var28.maxX - 0.001)));
                    int var31 = ((int) Math.floor((var28.minZ + 0.001)));
                    int var32 = ((int) Math.floor((var28.maxZ - 0.001)));
                    int var33 = var29;
                    while (var33 <= var30) {
                        int var34 = var31;
                        while (var34 <= var32) {
                            var6.add(new BlockPos(var33, var11, var34));
                            var34++;
                        }
                        var33++;
                    }
                    label_19:
                }
                label_20:
                var22++;
            }
        }
        label_22:
        var18 = var6.iterator();
        while (var18.hasNext()) {
            var19 = ((BlockPos) var18.next());
            if (!(!arg2) && !(!arg3)) {
                var5.add(var19.below());
            }
            var20 = Direction.values();
            var21 = var20.length;
            var22 = 0;
            while (var22 < var21) {
                var23 = var20[var22];
                if (!(var23.getAxis().isHorizontal())) break label_26;
                var24 = var19.relative(var23);
                if (var6.contains(var24)) break label_26;
                var5.add(var24);
                if (((!arg2) || (mc.level == null)) || (!(mc.level.getBlockState(var24).canBeReplaced()))) break label_26;
                var5.add(var24.below());
                label_26:
                var22++;
            }
        }
        var18 = var11;
        var5.removeIf((arg2) -> {
    if (arg2.getY() != arg0) break label_0;
    return arg1.getBoundingBox().intersects(new AABB(arg2));
});
        if (arg0 == mc.player && var7.isToggled() && var7.close.getValue()) {
            var19 = new ArrayList();
            var20 = Vec3.atCenterOf(mc.player.blockPosition());
            var21 = ((var20.x - mc.player.getX()) > 0.0 ? 1 : 0);
            var22 = ((var20.z - mc.player.getZ()) > 0.0 ? 1 : 0);
            if (!(!var21) && !(!var22)) {
                var19.add(new BlockPos(mc.player.blockPosition().offset(-1, 0, 0)));
                var19.add(new BlockPos(mc.player.blockPosition().offset(0, 0, -1)));
            }
            if (!var21 && !(!var22)) {
                var19.add(new BlockPos(mc.player.blockPosition().offset(1, 0, 0)));
                var19.add(new BlockPos(mc.player.blockPosition().offset(0, 0, -1)));
            }
            if (!(!var21) && !var22) {
                var19.add(new BlockPos(mc.player.blockPosition().offset(-1, 0, 0)));
                var19.add(new BlockPos(mc.player.blockPosition().offset(0, 0, 1)));
            }
            if (!var21 && !var22) {
                var19.add(new BlockPos(mc.player.blockPosition().offset(1, 0, 0)));
                var19.add(new BlockPos(mc.player.blockPosition().offset(0, 0, 1)));
            }
            Objects.requireNonNull(var19);
            var5.removeIf(var19::contains);
        }
        return var5;
    }

    public static List getTrapPositions(Player arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5) {
        return getTrapPositions(arg0, arg1, arg2, arg3, arg4, arg5, false, false);
    }

    public static List getTrapPositions(Player arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5, boolean arg6) {
        return getTrapPositions(arg0, null, arg1, arg2, arg3, arg4, arg5, false, arg6);
    }

    public static List getTrapPositions(Player arg0, BlockPos arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5, boolean arg6, boolean arg7) {
        return getTrapPositions(arg0, arg1, arg2, arg3, arg4, arg5, arg6, false, arg7);
    }

    public static List getTrapPositions(Player arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5, boolean arg6, boolean arg7) {
        return getTrapPositions(arg0, null, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public static List getTrapPositions(Player arg0, BlockPos arg1, boolean arg2, boolean arg3, boolean arg4, boolean arg5, boolean arg6, boolean arg7, boolean arg8) {
        Object var9 = new LinkedHashSet();
        if (arg1 != null && !(arg0.onGround())) {
            Object var10 = new HashSet();
            var10.add(arg1);
            break label_1;
        }
        var10 = getOccupiedFloorCells(arg0);
        label_1:
        Object var11 = var10.iterator();
        while (var11.hasNext()) {
            Object var12 = ((BlockPos) var11.next());
            Object var13 = null;
            Object var14 = Direction.values();
            int var15 = var14.length;
            int var16 = 0;
            while (var16 < var15) {
                Object var17 = var14[var16];
                if ((!(var17.getAxis().isHorizontal())) || (var10.contains(var12.relative(var17)))) break label_5;
                if (var13 == null) {
                    var13 = var17;
                }
                if (WorldUtils.getDirection(var12.relative(var17).above(), arg6) == null) break label_5;
                var13 = var17;
                break label_6;
                label_5:
                var16++;
            }
            label_6:
            if (var13 == null) {
                var13 = Direction.EAST;
            }
            if (!(!arg4)) {
                addWallOffset(var9, var10, var12, new Vec3i(1, 2, 0));
                addWallOffset(var9, var10, var12, new Vec3i(-1, 2, 0));
                addWallOffset(var9, var10, var12, new Vec3i(0, 2, 1));
                addWallOffset(var9, var10, var12, new Vec3i(0, 2, -1));
            }
            if (!(!arg5)) {
                var9.add(var12.offset(0, 3, 0));
            }
            if (!arg7) break label_12;
            var14 = Direction.values();
            var15 = var14.length;
            var16 = 0;
            while (var16 < var15) {
                var17 = var14[var16];
                if (var17.getAxis().isHorizontal() && !(var10.contains(var12.relative(var17)))) {
                    var9.add(var12.relative(var17));
                }
                var16++;
            }
            label_12:
            if (!(!arg2)) {
                var14 = var12.offset(0, 2, 0);
                if (mc.level != null && !(mc.level.getBlockState(var14).canBeReplaced())) {
                    var9.add(var14);
                    break label_15;
                }
                if (!arg8 && WorldUtils.getDirection(var14, arg6) == null) {
                    var9.add(var12.relative(var13).above());
                    var9.add(var12.relative(var13).above(2));
                    var9.add(var14);
                    break label_15;
                }
                var9.add(var14);
                label_15:
                break label_22;
            }
            var14 = Direction.values();
            var15 = var14.length;
            var16 = 0;
            while (var16 < var15) {
                var17 = var14[var16];
                if (var17.getAxis().isHorizontal() && !(var10.contains(var12.relative(var17)))) {
                    var9.add(var12.relative(var17).above());
                }
                var16++;
            }
            if (!arg3) break label_22;
            var14 = var12.offset(0, 2, 0);
            if (arg8) break label_21;
            if (mc.level != null) {
                if (!(mc.level.getBlockState(var14).canBeReplaced())) break label_21;
            }
            var9.add(var12.relative(var13).above(2));
            label_21:
            var9.add(var14);
            label_22:
        }
        return new ArrayList(var9);
    }

    private static void addWallOffset(Set arg0, HashSet arg1, BlockPos arg2, Vec3i arg3) {
        if (!(arg3.getX())) {
            if (arg3.getZ()) {
            }
            var tmp0 = arg2.offset(arg3.getX(), 0, arg3.getZ());
            Object var4 = tmp0;
            if (arg1.contains(tmp0)) return;
        }
        arg0.add(arg2.offset(arg3));
    }

    private static HashSet getOccupiedFloorCells(Player arg0) {
        Object var1 = new HashSet();
        Object var2 = PositionUtils.getFlooredPosition(arg0);
        var1.add(var2);
        Object var3 = Direction.values();
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Object var6 = var3[var5];
            if (var6.getAxis().isVertical()) {
                break label_6;
            }
            Object var7 = arg0.getBoundingBox();
            Object var8 = var2.relative(var6);
            if (((var7.minX >= ((double) (var8.getX() + 1))) || (var7.maxX <= ((double) var8.getX()))) || (var7.minZ >= ((double) (var8.getZ() + 1)))) break label_6;
            if (var7.maxZ <= ((double) var8.getZ())) {
                break label_6;
            }
            int var9 = ((int) Math.floor(var7.minX));
            while (((double) var9) < Math.ceil(var7.maxX)) {
                int var10 = ((int) Math.floor(var7.minZ));
                while (((double) var10) < Math.ceil(var7.maxZ)) {
                    var1.add(new BlockPos(var9, var2.getY(), var10));
                    var10++;
                }
                var9++;
            }
            label_6:
            var5++;
        }
        return var1;
    }

    private static HoleUtils.HoleSafety classifyWalls(BlockPos arg0, Vec3i[] arg1) {
        int var2 = 1;
        int var3 = 1;
        Object var4 = arg1;
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            Object var9 = arg0.offset(var7);
            Object var10 = mc.level.getBlockState(var9).getBlock();
            int var11 = var10.equals(Blocks.BEDROCK);
            if (!(var10.equals(Blocks.OBSIDIAN)) && !(var10.equals(Blocks.RESPAWN_ANCHOR))) {
                if (var10.equals(Blocks.ENDER_CHEST)) {
                }
            } else {
            }
            int var8 = 0;
            int var12 = 0;
            if (!var11 && !var8) {
                var3 = 0;
                if ((var10 instanceof ShulkerBoxBlock)) break label_7;
                if (!(mc.level.getBlockState(var9).getCollisionShape(mc.level, var9).isEmpty())) {
                    break label_7;
                }
                return null;
            }
            if (!(!var11)) {
                break label_7;
            }
            var2 = 0;
            label_7:
            var6++;
        }
        if (!var3) return HoleUtils.HoleSafety.NONE;
        return (!(!var2) ? HoleUtils.HoleSafety.SAFE : HoleUtils.HoleSafety.UNSAFE);
    }

    public static HoleUtils.Hole getSingleHole(BlockPos arg0, double arg1) {
        return getSingleHole(arg0, arg1, true);
    }

    public static HoleUtils.Hole getSingleHole(BlockPos arg0, double arg1, boolean arg2) {
        if (!(mc.level.getBlockState(arg0).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above()).getBlock().equals(Blocks.AIR)) && !(!arg2)) return null;
        if (!(mc.level.getBlockState(arg0.above().above()).getBlock().equals(Blocks.AIR)) && !(!arg2)) return null;
        Object var4 = classifyWalls(arg0, singleOffsets);
        if (var4 == null) return null;
        return new HoleUtils.Hole(new AABB(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()), ((double) (arg0.getX() + 1)), (((double) arg0.getY()) + arg1), ((double) (arg0.getZ() + 1))), HoleUtils.HoleType.SINGLE, var4);
    }

    public static HoleUtils.Hole getDoubleHole(BlockPos arg0, double arg1) {
        if (!(mc.level.getBlockState(arg0).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above().above()).getBlock().equals(Blocks.AIR))) return null;
        if ((!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR))) || (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR)))) break label_3;
        int var5 = (mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR) ? 1 : 0);
        if ((!(mc.level.getBlockState(arg0.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR))) || (!(mc.level.getBlockState(arg0.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR)))) break label_5;
        var tmp0 = (mc.level.getBlockState(arg0.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR) ? 1 : 0);
        int var4 = tmp0;
        int var6 = tmp0;
        if (!var5 && !var4) return null;
        Object var7 = null;
        Object var8 = null;
        if (!var5) break label_8;
        var tmp1 = classifyWalls(arg0, doubleXOffsets);
        Object var3 = tmp1;
        if (tmp1 == null) break label_8;
        var7 = new AABB(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()), ((double) (arg0.getX() + 2)), (((double) arg0.getY()) + arg1), ((double) (arg0.getZ() + 1)));
        var8 = var3;
        label_8:
        if ((!var4) || (var7 != null)) break label_9;
        var tmp2 = classifyWalls(arg0, doubleZOffsets);
        var3 = tmp2;
        if (tmp2 == null) break label_9;
        var7 = new AABB(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()), ((double) (arg0.getX() + 1)), (((double) arg0.getY()) + arg1), ((double) (arg0.getZ() + 2)));
        var8 = var3;
        label_9:
        if (var7 == null) return null;
        return new HoleUtils.Hole(var7, HoleUtils.HoleType.DOUBLE, var8);
    }

    public static HoleUtils.Hole getQuadHole(BlockPos arg0, double arg1) {
        if (!(mc.level.getBlockState(arg0).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 0))).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(0, 0, 1))).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 1))).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above().above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 0)).above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 0)).above().above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(0, 0, 1)).above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(0, 0, 1)).above().above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 1)).above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.offset(new Vec3i(1, 0, 1)).above().above()).getBlock().equals(Blocks.AIR))) return null;
        Object var3 = classifyWalls(arg0, quadOffsets);
        if (var3 == null) return null;
        return new HoleUtils.Hole(new AABB(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()), ((double) (arg0.getX() + 2)), (((double) arg0.getY()) + arg1), ((double) (arg0.getZ() + 2))), HoleUtils.HoleType.QUAD, var3);
    }

    public static HoleUtils.Hole getPartialHole(BlockPos arg0, double arg1) {
        if (mc.level == null) return null;
        if (!(mc.level.getBlockState(arg0).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above()).getBlock().equals(Blocks.AIR))) return null;
        if (!(mc.level.getBlockState(arg0.above().above()).getBlock().equals(Blocks.AIR))) return null;
        if (getSingleHole(arg0, arg1) != null) return null;
        if (getDoubleHole(arg0, arg1) != null) return null;
        if (getQuadHole(arg0, arg1) != null) return null;
        if (mc.level.getBlockState(arg0.below()).canBeReplaced()) return null;
        int var3 = 0;
        Object var4 = null;
        Object var5 = Direction.values();
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            Object var8 = var5[var7];
            if (!(var8.getAxis().isHorizontal())) {
                break label_11;
            }
            Object var9 = arg0.relative(var8);
            if (!(mc.level.getBlockState(var9).canBeReplaced()) && !(mc.level.getBlockState(var9).getCollisionShape(mc.level, var9).isEmpty())) {
                var3++;
                break label_11;
            }
            var4 = var9;
            label_11:
            var7++;
        }
        if (var3 != 3) return null;
        if ((var4 == null) || (mc.level.getBlockState(var4.below()).canBeReplaced())) {
            return null;
        }
        return new HoleUtils.Hole(new AABB(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()), ((double) (arg0.getX() + 1)), (((double) arg0.getY()) + arg1), ((double) (arg0.getZ() + 1))), HoleUtils.HoleType.PARTIAL, HoleUtils.HoleSafety.PARTIAL);
    }

    static {
        var tmp0 = new Vec3i[5];
        tmp0[0] = new Vec3i(0, -1, 0);
        tmp0[1] = new Vec3i(1, 0, 0);
        tmp0[2] = new Vec3i(-1, 0, 0);
        tmp0[3] = new Vec3i(0, 0, 1);
        tmp0[4] = new Vec3i(0, 0, -1);
        holeOffsets = tmp0;
        var tmp1 = new Vec3i[5];
        tmp1[0] = new Vec3i(-1, 0, 0);
        tmp1[1] = new Vec3i(1, 0, 0);
        tmp1[2] = new Vec3i(0, 0, -1);
        tmp1[3] = new Vec3i(0, 0, 1);
        tmp1[4] = new Vec3i(0, -1, 0);
        singleOffsets = tmp1;
        var tmp2 = new Vec3i[8];
        tmp2[0] = new Vec3i(-1, 0, 0);
        tmp2[1] = new Vec3i(0, 0, -1);
        tmp2[2] = new Vec3i(0, 0, 1);
        tmp2[3] = new Vec3i(0, -1, 0);
        tmp2[4] = new Vec3i(2, 0, 0);
        tmp2[5] = new Vec3i(1, 0, -1);
        tmp2[6] = new Vec3i(1, 0, 1);
        tmp2[7] = new Vec3i(1, -1, 0);
        doubleXOffsets = tmp2;
        var tmp3 = new Vec3i[8];
        tmp3[0] = new Vec3i(0, 0, -1);
        tmp3[1] = new Vec3i(-1, 0, 0);
        tmp3[2] = new Vec3i(1, 0, 0);
        tmp3[3] = new Vec3i(0, -1, 0);
        tmp3[4] = new Vec3i(0, 0, 2);
        tmp3[5] = new Vec3i(-1, 0, 1);
        tmp3[6] = new Vec3i(1, 0, 1);
        tmp3[7] = new Vec3i(0, -1, 1);
        doubleZOffsets = tmp3;
        var tmp4 = new Vec3i[12];
        tmp4[0] = new Vec3i(-1, 0, 0);
        tmp4[1] = new Vec3i(0, 0, -1);
        tmp4[2] = new Vec3i(0, -1, 0);
        tmp4[3] = new Vec3i(2, 0, 0);
        tmp4[4] = new Vec3i(1, 0, -1);
        tmp4[5] = new Vec3i(1, -1, 0);
        tmp4[6] = new Vec3i(-1, 0, 1);
        tmp4[7] = new Vec3i(0, 0, 2);
        tmp4[8] = new Vec3i(0, -1, 1);
        tmp4[9] = new Vec3i(2, 0, 1);
        tmp4[10] = new Vec3i(1, 0, 2);
        tmp4[11] = new Vec3i(1, -1, 1);
        quadOffsets = tmp4;
    }


    public enum HoleType {
        SINGLE,
        DOUBLE,
        QUAD,
        PARTIAL;


        private HoleType() {
        }

    }

    public enum HoleSafety {
        SAFE,
        UNSAFE,
        PARTIAL,
        NONE;


        private HoleSafety() {
        }

    }

    public record Hole(AABB box, HoleUtils.HoleType type, HoleUtils.HoleSafety safety) {

        public Hole(AABB arg0, HoleUtils.HoleType arg1, HoleUtils.HoleSafety arg2) {
            this.box = arg0;
            this.type = arg1;
            this.safety = arg2;
        }

        public AABB box() {
            return this.box;
        }

        public HoleUtils.HoleType type() {
            return this.type;
        }

        public HoleUtils.HoleSafety safety() {
            return this.safety;
        }

    }
}
