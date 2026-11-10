package lightning.utils.system;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static double random(double arg0, double arg1) {
        return ((Math.random() * (arg0 - arg1)) + arg1);
    }

    public static double round(double arg0, int arg1) {
        int var3 = ((int) Math.pow(10.0, ((double) arg1)));
        return (((double) Math.round((arg0 * ((double) var3)))) / ((double) var3));
    }

    public static Vec3 getVec(BlockPos arg0) {
        return new Vec3(((double) arg0.getX()), ((double) arg0.getY()), ((double) arg0.getZ()));
    }

    public static AABB getBox(Vec3 arg0) {
        return new AABB(arg0.x, arg0.y, arg0.z, (arg0.x + 1.0), (arg0.y + 1.0), (arg0.z + 1.0));
    }

    public static Vec3 scale(Vec3 arg0, float arg1) {
        return arg0.scale(((double) arg1));
    }

    public static double interpolate(double arg0, double arg1, double arg2) {
        return (arg0 + ((arg1 - arg0) * arg2));
    }

    public static boolean inRange(double arg0, double arg1, double arg2) {
        if (arg0 <= (arg1 - arg2)) break label_0;
        return arg0 < (arg1 + arg2);
    }

    public static float wrapAngle(float arg0) {
        var tmp0 = (arg0 % 360.0f);
        arg0 = tmp0;
        if (tmp0 < 0.0f) {
            arg0 = (arg0 + 360.0f);
        }
        return arg0;
    }

}
