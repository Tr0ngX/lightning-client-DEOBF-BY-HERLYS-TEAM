package lightning.utils.rotations;

import lightning.utils.IMinecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class RotationUtils implements IMinecraft {
    public static float[] getRotations(Entity arg0) {
        return getRotations(arg0.getX(), (arg0.getY() + (((double) arg0.getEyeHeight(arg0.getPose())) / 2.0)), arg0.getZ());
    }

    public static float[] getRotations(Vec3 arg0) {
        return getRotations(arg0.x, arg0.y, arg0.z);
    }

    public static float[] getRotations(double arg0, double arg1, double arg2) {
        return getRotations(mc.player, arg0, arg1, arg2);
    }

    public static float[] getRotations(Entity arg0, double arg1, double arg2, double arg3) {
        Object var7 = arg0.position().add(0.0, ((double) arg0.getEyeHeight(arg0.getPose())), 0.0);
        double var8 = (arg1 - var7.x);
        double var10 = ((arg2 - var7.y) * -1.0);
        double var12 = (arg3 - var7.z);
        double var14 = ((double) Mth.sqrt(((float) ((var8 * var8) + (var12 * var12)))));
        float var16 = ((float) Mth.wrapDegrees((Math.toDegrees(Math.atan2(var12, var8)) - 90.0)));
        float var17 = ((float) Mth.clamp(Mth.wrapDegrees(Math.toDegrees(Math.atan2(var10, var14))), -90.0, 90.0));
        var tmp0 = new float[2];
        tmp0[0] = (var16 + ((((float) Math.random()) - 0.5f) * 4.0f));
        tmp0[1] = (var17 + ((((float) Math.random()) - 0.5f) * 4.0f));
        return tmp0;
    }

    public static double getYRotToVec(Entity arg0, Vec3 arg1) {
        double var2 = (arg1.x - arg0.getX());
        double var4 = (arg1.z - arg0.getZ());
        return Mth.wrapDegrees((Math.toDegrees(Math.atan2(var4, var2)) - 90.0));
    }

    public static double getXRotToVec(Entity arg0, Vec3 arg1) {
        double var2 = (arg1.x - arg0.getX());
        double var4 = (arg1.y - (arg0.getY() + ((double) arg0.getEyeHeight())));
        double var6 = (arg1.z - arg0.getZ());
        double var8 = Math.sqrt(((var2 * var2) + (var6 * var6)));
        return Mth.clamp(Mth.wrapDegrees(-(Math.toDegrees(Math.atan2(var4, var8)))), -90.0, 90.0);
    }

    public static float[] getExactRotations(Entity arg0, Vec3 arg1) {
        var tmp0 = new float[2];
        tmp0[0] = ((float) getYRotToVec(arg0, arg1));
        tmp0[1] = ((float) getXRotToVec(arg0, arg1));
        return tmp0;
    }

    public static float getYRotToVec(Vec3 arg0, Vec3 arg1) {
        double var2 = (arg1.x - arg0.x);
        double var4 = (arg1.z - arg0.z);
        return ((float) Mth.wrapDegrees((Math.toDegrees(Math.atan2(var4, var2)) - 90.0)));
    }

    public static float getXRotToVec(Vec3 arg0, Vec3 arg1) {
        double var2 = (arg1.x - arg0.x);
        double var4 = (arg1.y - arg0.y);
        double var6 = (arg1.z - arg0.z);
        return ((float) -(Math.toDegrees(Math.atan2(var4, Math.sqrt(((var2 * var2) + (var6 * var6)))))));
    }

    public static Vec3 getClosestPointToEye(Vec3 arg0, AABB arg1) {
        double var2 = arg0.x;
        double var4 = arg0.y;
        double var6 = arg0.z;
        double var8 = 0.0625;
        double var10 = 1.0E-9;
        if (arg0.x < arg1.minX) {
            var2 = arg1.minX;
            break label_1;
        }
        if (arg0.x <= arg1.maxX) break label_1;
        var2 = arg1.maxX;
        label_1:
        if (arg0.y < arg1.minY) {
            var4 = arg1.minY;
            break label_3;
        }
        if (arg0.y <= arg1.maxY) break label_3;
        var4 = arg1.maxY;
        label_3:
        if (arg0.z < arg1.minZ) {
            var6 = arg1.minZ;
            break label_5;
        }
        if (arg0.z <= arg1.maxZ) break label_5;
        var6 = arg1.maxZ;
        label_5:
        if (Math.abs((var2 - arg1.minX)) < 1.0E-9) {
            var2 = Math.min((arg1.minX + 0.0625), (arg1.maxX - 1.0E-9));
            break label_7;
        }
        if (Math.abs((var2 - arg1.maxX)) >= 1.0E-9) break label_7;
        var2 = Math.max((arg1.maxX - 0.0625), (arg1.minX + 1.0E-9));
        label_7:
        if (Math.abs((var6 - arg1.minZ)) < 1.0E-9) {
            var6 = Math.min((arg1.minZ + 0.0625), (arg1.maxZ - 1.0E-9));
            break label_9;
        }
        if (Math.abs((var6 - arg1.maxZ)) >= 1.0E-9) break label_9;
        var6 = Math.max((arg1.maxZ - 0.0625), (arg1.minZ + 1.0E-9));
        label_9:
        return new Vec3(var2, var4, var6);
    }

    public static Vec3 getClampClosestPoint(Vec3 arg0, AABB arg1) {
        double var2 = Mth.clamp(arg0.x, arg1.minX, arg1.maxX);
        double var4 = Mth.clamp(arg0.y, arg1.minY, arg1.maxY);
        double var6 = Mth.clamp(arg0.z, arg1.minZ, arg1.maxZ);
        return new Vec3(var2, var4, var6);
    }

    public static Vec3 getLookVectorFromYRotXRot(float arg0, float arg1) {
        float var2 = (arg1 * 0.017453292f);
        float var3 = (-(arg0) * 0.017453292f);
        float var4 = Mth.cos(((double) var3));
        float var5 = Mth.sin(((double) var3));
        float var6 = Mth.cos(((double) var2));
        float var7 = Mth.sin(((double) var2));
        return new Vec3(((double) (var5 * var6)), ((double) -(var7)), ((double) (var4 * var6)));
    }

    public static EntityHitResult raycastTarget(Vec3 arg0, Entity arg1, double arg2, float arg3, float arg4) {
        Object var6 = getLookVectorFromYRotXRot(arg3, arg4);
        Object var7 = arg0.add(var6.scale(arg2));
        float var8 = arg1.getPickRadius();
        Object var9 = arg1.getBoundingBox().inflate(((double) Math.max(var8, 0.05f)));
        if (var9.clip(arg0, var7).isPresent()) return new EntityHitResult(arg1);
        return null;
    }

}
