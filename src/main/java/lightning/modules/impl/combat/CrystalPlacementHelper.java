package lightning.modules.impl.combat;

import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CrystalPlacementHelper implements IMinecraft {
    public static CrystalPlacementHelper.PlacementResult getVisiblePlacement(BlockPos arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return new CrystalPlacementHelper.PlacementResult(Direction.UP, Vec3.atCenterOf(arg0).add(0.0, 0.5, 0.0), false);
        }
        Object var1 = mc.player.getEyePosition();
        var tmp0 = new Direction[6];
        tmp0[0] = Direction.UP;
        tmp0[1] = Direction.SOUTH;
        tmp0[2] = Direction.NORTH;
        tmp0[3] = Direction.EAST;
        tmp0[4] = Direction.WEST;
        tmp0[5] = Direction.DOWN;
        Object var2 = tmp0;
        Object var3 = null;
        double var4 = 1.7976931348623157E308;
        var tmp1 = new double[][8];
        var tmp2 = new double[2];
        tmp2[0] = 0.0;
        tmp2[1] = 0.5;
        tmp1[0] = tmp2;
        var tmp3 = new double[2];
        tmp3[0] = 1.0;
        tmp3[1] = 0.5;
        tmp1[1] = tmp3;
        var tmp4 = new double[2];
        tmp4[0] = 0.5;
        tmp4[1] = 0.0;
        tmp1[2] = tmp4;
        var tmp5 = new double[2];
        tmp5[0] = 0.5;
        tmp5[1] = 1.0;
        tmp1[3] = tmp5;
        var tmp6 = new double[2];
        tmp6[0] = 0.0;
        tmp6[1] = 0.0;
        tmp1[4] = tmp6;
        var tmp7 = new double[2];
        tmp7[0] = 1.0;
        tmp7[1] = 0.0;
        tmp1[5] = tmp7;
        var tmp8 = new double[2];
        tmp8[0] = 0.0;
        tmp8[1] = 1.0;
        tmp1[6] = tmp8;
        var tmp9 = new double[2];
        tmp9[0] = 1.0;
        tmp9[1] = 1.0;
        tmp1[7] = tmp9;
        Object var6 = tmp1;
        int var7 = 10;
        Object var8 = var2;
        int var9 = var8.length;
        int var10 = 0;
        while (var10 < var9) {
            Object var11 = var8[var10];
            Object var12 = arg0.relative(var11);
            if (var11 != Direction.UP && !(mc.level.getBlockState(var12).canBeReplaced()) && mc.level.getBlockState(var12).isSolidRender()) {
                break label_12;
            }
            Object var13 = testFacePoint(arg0, var11, var1, 0.5, 0.5);
            if (var13 != null) {
                double var14 = var1.distanceToSqr(var13.hitVec);
                if (var14 >= var4) {
                    break label_12;
                }
                var4 = var14;
                var3 = var13;
                break label_12;
            }
            var14 = var6;
            int var15 = var14.length;
            int var16 = 0;
            while (var16 < var15) {
                Object var17 = var14[var16];
                int var18 = 1;
                while (var18 <= var7) {
                    double var19 = (1.0 - (1.0 / ((double) (1 << var18))));
                    double var21 = (0.5 + ((var17[0] - 0.5) * var19));
                    double var23 = (0.5 + ((var17[1] - 0.5) * var19));
                    Object var25 = testFacePoint(arg0, var11, var1, var21, var23);
                    if (var25 == null) {
                    } else {
                        double var26 = var1.distanceToSqr(var25.hitVec);
                        if (var26 >= var4) {
                            break label_11;
                        }
                        var4 = var26;
                        var3 = var25;
                        break label_11;
                    }
                    var18++;
                }
                label_11:
                var16++;
            }
            label_12:
            var10++;
        }
        if (var3 != null) return var3;
        var8 = null;
        var9 = 1.7976931348623157E308;
        var11 = var2;
        var12 = var11.length;
        var13 = 0;
        while (var13 < var12) {
            var14 = var11[var13];
            var18 = arg0.relative(var14);
            var19 = mc.level.getBlockState(var18);
            if (!(var19.canBeReplaced())) break label_17;
            var tmp10 = getSurfacePoint(arg0, var14, 0.5, 0.5);
            var15 = tmp10;
            var16 = var1.distanceToSqr(tmp10);
            if (var1.distanceToSqr(tmp10) >= var9) {
                break label_17;
            }
            var9 = var16;
            var8 = var14;
            label_17:
            var13++;
        }
        if (var8 != null) return new CrystalPlacementHelper.PlacementResult(var8, getSurfacePoint(arg0, var8, 0.5, 0.5), false);
        return new CrystalPlacementHelper.PlacementResult(Direction.UP, Vec3.atCenterOf(arg0).add(0.0, 0.5, 0.0), false);
    }

    private static CrystalPlacementHelper.PlacementResult testFacePoint(BlockPos arg0, Direction arg1, Vec3 arg2, double arg3, double arg4) {
        Object var7 = getPointOnFace(arg0, arg1, arg3, arg4);
        Object var8 = mc.level.clip(new ClipContext(arg2, var7, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var8 == null) return null;
        if (var8.getType() == HitResult.Type.MISS) return new CrystalPlacementHelper.PlacementResult(arg1, getSurfacePoint(arg0, arg1, arg3, arg4), true);
        if (var8.getType() == HitResult.Type.BLOCK && var8.getBlockPos().equals(arg0)) return new CrystalPlacementHelper.PlacementResult(var8.getDirection(), var8.getLocation(), true);
        return null;
    }

    public static boolean isPlacementVisible(BlockPos arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        Object var1 = getVisiblePlacement(arg0);
        if (var1 == null) break label_2;
        return !(!var1.visible);
    }

    public static Vec3 getPointOnFace(BlockPos arg0, Direction arg1, double arg2, double arg3) {
        double var6 = ((double) arg0.getX());
        double var8 = ((double) arg0.getY());
        double var10 = ((double) arg0.getZ());
        switch (CrystalPlacementHelper.1.$SwitchMap$net$minecraft$core$Direction[arg1.ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            case 3 -> { break label_2; }
            case 4 -> { break label_3; }
            case 5 -> { break label_4; }
            case 6 -> { break label_5; }
            default -> { break label_6; }
        }
        label_0:
        return new Vec3((var6 + arg2), (var8 + 0.999), (var10 + arg3));
        label_1:
        return new Vec3((var6 + arg2), (var8 + 0.001), (var10 + arg3));
        label_2:
        return new Vec3((var6 + arg2), (var8 + arg3), (var10 + 0.999));
        label_3:
        return new Vec3((var6 + arg2), (var8 + arg3), (var10 + 0.001));
        label_4:
        return new Vec3((var6 + 0.999), (var8 + arg2), (var10 + arg3));
        label_5:
        return new Vec3((var6 + 0.001), (var8 + arg2), (var10 + arg3));
        label_6:
        return new Vec3((var6 + 0.5), (var8 + 0.5), (var10 + 0.5));
    }

    public static Vec3 getSurfacePoint(BlockPos arg0, Direction arg1, double arg2, double arg3) {
        double var6 = ((double) arg0.getX());
        double var8 = ((double) arg0.getY());
        double var10 = ((double) arg0.getZ());
        switch (CrystalPlacementHelper.1.$SwitchMap$net$minecraft$core$Direction[arg1.ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            case 3 -> { break label_2; }
            case 4 -> { break label_3; }
            case 5 -> { break label_4; }
            case 6 -> { break label_5; }
            default -> { break label_6; }
        }
        label_0:
        return new Vec3((var6 + arg2), (var8 + 1.0), (var10 + arg3));
        label_1:
        return new Vec3((var6 + arg2), (var8 + 0.0), (var10 + arg3));
        label_2:
        return new Vec3((var6 + arg2), (var8 + arg3), (var10 + 1.0));
        label_3:
        return new Vec3((var6 + arg2), (var8 + arg3), (var10 + 0.0));
        label_4:
        return new Vec3((var6 + 1.0), (var8 + arg2), (var10 + arg3));
        label_5:
        return new Vec3((var6 + 0.0), (var8 + arg2), (var10 + arg3));
        label_6:
        return new Vec3((var6 + 0.5), (var8 + 0.5), (var10 + 0.5));
    }


    public class PlacementResult {
        public Direction direction;
        public Vec3 hitVec;
        public boolean visible;

        public PlacementResult(Direction arg0, Vec3 arg1) {
            this(arg0, arg1, true);
        }

        public PlacementResult(Direction arg0, Vec3 arg1, boolean arg2) {
            this.direction = arg0;
            this.hitVec = arg1;
            this.visible = arg2;
        }

    }

    class 1 {

        static {
            $SwitchMap$net$minecraft$core$Direction = new int[Direction.values().length];
            try {
                $SwitchMap$net$minecraft$core$Direction[Direction.UP.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.SOUTH.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.NORTH.ordinal()] = 4;
            } catch (NoSuchFieldError ex11) {
                var0 = ex11;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.EAST.ordinal()] = 5;
            } catch (NoSuchFieldError ex14) {
                var0 = ex14;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.WEST.ordinal()] = 6;
            } catch (NoSuchFieldError ex17) {
                var0 = ex17;
            }
        }

    }
}
