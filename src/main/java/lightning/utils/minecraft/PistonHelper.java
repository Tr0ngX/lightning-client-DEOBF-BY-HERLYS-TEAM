package lightning.utils.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import lightning.LightningClient;
import lightning.utils.IMinecraft;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PistonHelper implements IMinecraft {
    public static final CopyOnWriteArrayList placeList;

    public static boolean isGrimDirection(BlockPos arg0, Direction arg1) {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        Object var2 = getCombinedBox(arg0);
        Object var3 = new AABB(mc.player.getX(), (mc.player.getY() + 0.4), mc.player.getZ(), mc.player.getX(), (mc.player.getY() + 1.62), mc.player.getZ()).inflate(2.0E-4);
        if (isIntersected(var3, var2)) return true;
        switch (PistonHelper.1.$SwitchMap$net$minecraft$core$Direction[arg1.ordinal()]) {
            case 1 -> { break label_4; }
            case 2 -> { break label_6; }
            case 3 -> { break label_8; }
            case 4 -> { break label_10; }
            case 5 -> { break label_12; }
            case 6 -> { break label_14; }
            default -> { break label_3; }
        }
        label_3:
        throw new MatchException(null, null);
        label_4:
        if (var3.minZ <= var2.minZ) {
            break label_16;
        }
        break label_16;
        label_6:
        if (var3.maxZ >= var2.maxZ) {
            break label_16;
        }
        break label_16;
        label_8:
        if (var3.maxX >= var2.maxX) {
            break label_16;
        }
        break label_16;
        label_10:
        if (var3.minX <= var2.minX) {
            break label_16;
        }
        break label_16;
        label_12:
        if (var3.maxY >= var2.maxY) {
            break label_16;
        }
        break label_16;
        label_14:
        return var3.minY <= var2.minY;
    }

    public static Direction getClickSideStrict(BlockPos arg0) {
        if (mc.player == null) return null;
        Object var1 = null;
        double var2 = 1.7976931348623157E308;
        Object var4 = Direction.values();
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            if (!(isGrimDirection(arg0, var7))) break label_3;
            double var8 = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0.relative(var7)));
            if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0.relative(var7))) >= var2) {
                break label_3;
            }
            var2 = var8;
            var1 = var7;
            label_3:
            var6++;
        }
        return var1;
    }

    public static Direction getClickSide(BlockPos arg0) {
        if (mc.player == null) return null;
        double var3 = 1.7976931348623157E308;
        Object var5 = null;
        Object var6 = Direction.values();
        int var7 = var6.length;
        int var8 = 0;
        while (var8 < var7) {
            Object var9 = var6[var8];
            if (!(canSee(arg0, var9))) break label_3;
            double var1 = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0.relative(var9)));
            if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0.relative(var9))) >= var3) {
                break label_3;
            }
            var3 = var1;
            var5 = var9;
            label_3:
            var8++;
        }
        if (var5 != null) return var5;
        var3 = 1.7976931348623157E308;
        var5 = Direction.UP;
        var6 = Direction.values();
        var7 = var6.length;
        var8 = 0;
        while (var8 < var7) {
            var9 = var6[var8];
            if (!(isGrimDirection(arg0, var9))) break label_8;
            var1 = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0.relative(var9)));
            if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(arg0.relative(var9))) >= var3) {
                break label_8;
            }
            var3 = var1;
            var5 = var9;
            label_8:
            var8++;
        }
        return var5;
    }

    public static Direction getPlaceSide(BlockPos arg0, Predicate arg1) {
        return getPlaceSide(arg0, arg1, false);
    }

    public static Direction getPlaceSide(BlockPos arg0, Predicate arg1, boolean arg2) {
        if (((arg0 == null) || (mc.player == null)) || (mc.level == null)) {
            return null;
        }
        double var8 = 1.7976931348623157E308;
        Object var10 = null;
        Object var11 = Direction.values();
        int var12 = var11.length;
        int var13 = 0;
        while (var13 < var12) {
            Object var14 = var11[var13];
            if (arg1 != null && !(arg1.test(var14))) {
                break label_6;
            }
            Object var7 = arg0.relative(var14);
            Object var6 = mc.level.getBlockState(var7);
            if ((!(canClick(var7, arg2))) || (var6.canBeReplaced())) break label_6;
            if (!(isGrimDirection(var7, var14.getOpposite()))) {
                break label_6;
            }
            Object var5 = Vec3.atCenterOf(arg0).add((((double) var14.getStepX()) * 0.5), (((double) var14.getStepY()) * 0.5), (((double) var14.getStepZ()) * 0.5));
            double var3 = mc.player.getEyePosition().distanceToSqr(var5);
            if (var3 >= var8) {
                break label_6;
            }
            var8 = var3;
            var10 = var14;
            label_6:
            var13++;
        }
        if (var10 != null) return var10;
        var11 = Direction.values();
        var12 = var11.length;
        var13 = 0;
        while (var13 < var12) {
            var14 = var11[var13];
            if (arg1 != null && !(arg1.test(var14))) {
                break label_13;
            }
            var7 = arg0.relative(var14);
            var6 = mc.level.getBlockState(var7);
            if (!(canClick(var7, arg2))) break label_13;
            if (var6.canBeReplaced()) {
                break label_13;
            }
            var5 = Vec3.atCenterOf(arg0).add((((double) var14.getStepX()) * 0.5), (((double) var14.getStepY()) * 0.5), (((double) var14.getStepZ()) * 0.5));
            var3 = mc.player.getEyePosition().distanceToSqr(var5);
            if (var3 >= var8) {
                break label_13;
            }
            var8 = var3;
            var10 = var14;
            label_13:
            var13++;
        }
        return var10;
    }

    public static List getPlaceSides(BlockPos arg0, Predicate arg1) {
        return getPlaceSides(arg0, arg1, false);
    }

    public static List getPlaceSides(BlockPos arg0, Predicate arg1, boolean arg2) {
        Object var3 = new ArrayList();
        if ((arg0 == null) || (mc.level == null)) {
            return var3;
        }
        Object var4 = Direction.values();
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            if (arg1 != null && !(arg1.test(var7))) {
                break label_5;
            }
            Object var8 = arg0.relative(var7);
            Object var9 = mc.level.getBlockState(var8);
            if ((!(canClick(var8, arg2))) || (var9.canBeReplaced())) break label_5;
            if (!(isGrimDirection(var8, var7.getOpposite()))) {
                break label_5;
            }
            var3.add(var7);
            label_5:
            var6++;
        }
        return var3;
    }

    public static boolean canClick(BlockPos arg0) {
        return canClick(arg0, false);
    }

    public static boolean canClick(BlockPos arg0, boolean arg1) {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        Object var2 = mc.level.getBlockState(arg0);
        int var3 = WorldUtils.isInteractable(var2);
        if (!(var2.isSolid())) break label_3;
        if (!(!var3) && !(mc.player.isShiftKeyDown())) {
            if (!arg1) break label_3;
        }
        break label_4;
        label_3:
        label_4:
        return false;
    }

    public static boolean canReplace(BlockPos arg0) {
        if ((((mc.level == null) || (arg0 == null)) || (arg0.getY() >= 320)) || (arg0.getY() < -64)) {
            return false;
        }
        return mc.level.getBlockState(arg0).canBeReplaced();
    }

    public static boolean canPlace(BlockPos arg0) {
        return canPlace(arg0, null, false, false);
    }

    public static boolean canPlace(BlockPos arg0, Predicate arg1) {
        return canPlace(arg0, arg1, false, false);
    }

    public static boolean canPlace(BlockPos arg0, Predicate arg1, boolean arg2) {
        return canPlace(arg0, arg1, arg2, false);
    }

    public static boolean canPlace(BlockPos arg0, Predicate arg1, boolean arg2, boolean arg3) {
        if (!(canReplace(arg0))) return false;
        if (hasEntity(arg0, arg3)) return false;
        if (!(!arg2)) return true;
        return getPlaceSide(arg0, arg1) != null;
    }

    public static boolean canPlaceCrystal(BlockPos arg0) {
        return canPlaceCrystal(arg0, false);
    }

    public static boolean canPlaceCrystal(BlockPos arg0, boolean arg1) {
        if (mc.level == null) return false;
        if (!(mc.level.getBlockState(arg0).isAir())) return false;
        Object var2 = arg0.above();
        if (!(mc.level.getBlockState(var2).isAir())) return false;
        if ((hasEntityBlockCrystal(arg0, false)) || (hasEntityBlockCrystal(var2, false))) {
            return false;
        }
        Object var3 = arg0.below();
        Object var4 = mc.level.getBlockState(var3).getBlock();
        if (var4 != Blocks.OBSIDIAN && var4 != Blocks.BEDROCK) return false;
        if (!(!arg1)) return true;
        return getClickSide(var3) != null;
    }

    public static boolean canPlaceCrystalIgnoreHeadroom(BlockPos arg0, boolean arg1) {
        if (mc.level == null) return false;
        if (!(mc.level.getBlockState(arg0).isAir())) return false;
        if (hasEntityBlockCrystal(arg0, false)) return false;
        Object var2 = arg0.below();
        Object var3 = mc.level.getBlockState(var2).getBlock();
        if (var3 != Blocks.OBSIDIAN && var3 != Blocks.BEDROCK) return false;
        if (!(!arg1)) return true;
        return getClickSide(var2) != null;
    }

    public static boolean hasEntity(BlockPos arg0, boolean arg1) {
        if (mc.level == null) return false;
        Object var2 = new AABB(arg0).deflate(0.01);
        Object var3 = mc.level.getEntitiesOfClass(Entity.class, var2).iterator();
        label_1:
        if (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if ((((!(var4.isAlive())) || ((var4 instanceof ItemEntity))) || ((var4 instanceof ExperienceOrb))) || ((var4 instanceof Projectile))) break label_1;
            if (!(!arg1) && (var4 instanceof EndCrystal)) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    public static boolean hasEntityBlockCrystal(BlockPos arg0, boolean arg1) {
        if (mc.level == null) return false;
        Object var2 = new AABB(arg0);
        Object var3 = mc.level.getEntitiesOfClass(Entity.class, var2).iterator();
        label_1:
        if (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if (!(var4.isAlive())) break label_1;
            if (!(!arg1) && (var4 instanceof EndCrystal)) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    public static boolean hasCrystal(BlockPos arg0) {
        if (mc.level == null) return false;
        Object var1 = new AABB(arg0);
        Object var2 = mc.level.getEntitiesOfClass(EndCrystal.class, var1).iterator();
        label_1:
        if (var2.hasNext()) {
            Object var3 = ((EndCrystal) var2.next());
            if (!(var3.isAlive())) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    public static boolean hasCrystalAt(BlockPos arg0) {
        if (mc.level == null) return false;
        Object var1 = new AABB(arg0);
        Object var2 = mc.level.getEntitiesOfClass(EndCrystal.class, var1).iterator();
        label_1:
        if (var2.hasNext()) {
            Object var3 = ((EndCrystal) var2.next());
            if (!(var3.isAlive())) break label_1;
            if (!(var3.blockPosition().equals(arg0))) {
                break label_1;
            }
            return true;
        }
        return false;
    }

    public static void placeBlockDirect(BlockPos arg0, Direction arg1) {
        if ((mc.player == null) || (mc.getConnection() == null)) {
            return;
        }
        Object var2 = arg0.relative(arg1);
        Object var3 = new Vec3(((((double) var2.getX()) + 0.5) + (((double) arg1.getOpposite().getStepX()) * 0.5)), ((((double) var2.getY()) + 0.5) + (((double) arg1.getOpposite().getStepY()) * 0.5)), ((((double) var2.getZ()) + 0.5) + (((double) arg1.getOpposite().getStepZ()) * 0.5)));
        Object var4 = new BlockHitResult(var3, arg1.getOpposite(), var2, false);
        NetworkUtils.sendSequencedPacket((arg1) -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, arg0, arg1));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(arg0);
    }

    public static void clickBlock(BlockPos arg0, Direction arg1, boolean arg2) {
        if ((mc.player == null) || (mc.getConnection() == null)) {
            return;
        }
        Object var3 = new Vec3(((((double) arg0.getX()) + 0.5) + (((double) arg1.getStepX()) * 0.5)), ((((double) arg0.getY()) + 0.5) + (((double) arg1.getStepY()) * 0.5)), ((((double) arg0.getZ()) + 0.5) + (((double) arg1.getStepZ()) * 0.5)));
        if (!(!arg2)) {
            Object var4 = RotationUtils.getRotations(var3);
            LightningClient.ROTATION_MANAGER.silentRotate(var4[0], var4[1]);
        }
        var4 = new BlockHitResult(var3, arg1, arg0, false);
        NetworkUtils.sendSequencedPacket((arg1) -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, arg0, arg1));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        placeList.add(arg0);
    }

    public static void placeBlock(BlockPos arg0, Direction arg1, boolean arg2) {
        clickBlock(arg0.relative(arg1), arg1.getOpposite(), arg2);
    }

    public static boolean canSee(BlockPos arg0, Direction arg1) {
        if ((mc.level == null) || (mc.player == null)) {
            return false;
        }
        Object var2 = Vec3.atCenterOf(arg0).add((((double) arg1.getStepX()) * 0.5), (((double) arg1.getStepY()) * 0.5), (((double) arg1.getStepZ()) * 0.5));
        Object var3 = mc.level.clip(new ClipContext(mc.player.getEyePosition(), var2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (var3 != null) {
            if (var3.getType() == HitResult.Type.MISS) {
            }
        } else {
        }
        return false;
    }

    public static Vec3 getClosestPointToBox(Vec3 arg0, AABB arg1) {
        return new Vec3(Mth.clamp(arg0.x, arg1.minX, arg1.maxX), Mth.clamp(arg0.y, arg1.minY, arg1.maxY), Mth.clamp(arg0.z, arg1.minZ, arg1.maxZ));
    }

    private static AABB getCombinedBox(BlockPos arg0) {
        if (mc.level == null) return new AABB(arg0);
        Object var1 = new AABB(arg0);
        Object var2 = mc.level.getBlockState(arg0).getCollisionShape(mc.level, arg0).toAabbs().iterator();
        while (var2.hasNext()) {
            Object var3 = ((AABB) var2.next());
            Object var4 = var3.move(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()));
            double var5 = Math.max(var4.minX, var1.minX);
            double var7 = Math.max(var4.minY, var1.minY);
            double var9 = Math.max(var4.minZ, var1.minZ);
            double var11 = Math.min(var4.maxX, var1.maxX);
            double var13 = Math.min(var4.maxY, var1.maxY);
            double var15 = Math.min(var4.maxZ, var1.maxZ);
            var1 = new AABB(var5, var7, var9, var11, var13, var15);
        }
        return var1;
    }

    private static boolean isIntersected(AABB arg0, AABB arg1) {
        if ((((((arg1.maxX - 1.0E-7) <= arg0.minX) || ((arg1.minX + 1.0E-7) >= arg0.maxX)) || ((arg1.maxY - 1.0E-7) <= arg0.minY)) || ((arg1.minY + 1.0E-7) >= arg0.maxY)) || ((arg1.maxZ - 1.0E-7) <= arg0.minZ)) break label_0;
        return (arg1.minZ + 1.0E-7) < arg0.maxZ;
    }

    static {
        placeList = new CopyOnWriteArrayList();
    }


    class 1 {

        static {
            $SwitchMap$net$minecraft$core$Direction = new int[Direction.values().length];
            try {
                $SwitchMap$net$minecraft$core$Direction[Direction.NORTH.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.SOUTH.ordinal()] = 2;
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
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.UP.ordinal()] = 5;
            } catch (NoSuchFieldError ex14) {
                var0 = ex14;
                try {
                }
                $SwitchMap$net$minecraft$core$Direction[Direction.DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError ex17) {
                var0 = ex17;
            }
        }

    }
}
