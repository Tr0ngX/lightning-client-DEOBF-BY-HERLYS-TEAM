package lightning.utils.minecraft;

import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PositionUtils implements IMinecraft {
    public static BlockPos getFlooredPosition(Entity arg0) {
        if ((arg0.getY() - Math.floor(arg0.getY())) > 0.8) {
        } else {
        }
        return new BlockPos;
    }

    public static AABB getRadius(Entity arg0, double arg1) {
        return new AABB(((double) Mth.floor((arg0.getX() - arg1))), ((double) Mth.floor((arg0.getY() - arg1))), ((double) Mth.floor((arg0.getZ() - arg1))), ((double) Mth.floor((arg0.getX() + arg1))), ((double) Mth.floor((arg0.getY() + arg1))), ((double) Mth.floor((arg0.getZ() + arg1))));
    }

    public static AABB extrapolate(Player arg0, int arg1) {
        return extrapolate(arg0, arg1, true);
    }

    public static AABB extrapolate(Player arg0, int arg1, boolean arg2) {
        if (arg0 == null) return null;
        if (arg1 <= 0) break label_1;
        if (mc.level == null) {
            label_1:
            return arg0.getBoundingBox();
        }
        double var3 = (arg0.getX() - arg0.xo);
        if (!(!arg2)) {
        } else {
        }
        double var5 = 0.0;
        double var7 = (arg0.getZ() - arg0.zo);
        double var9 = 0.0;
        double var11 = 0.0;
        double var13 = 0.0;
        double var15 = 0.5;
        while (var15 <= ((double) arg1)) {
            Object var17 = new Vec3((var3 * var15), (var5 * var15), (var7 * var15));
            if (mc.level.noCollision(arg0, arg0.getBoundingBox().move(var17))) {
                var9 = (var3 * var15);
                var11 = (var5 * var15);
                var13 = (var7 * var15);
            } else {
                if (!arg2) {
                    break label_10;
                }
                Object var18 = new Vec3((var3 * var15), var11, (var7 * var15));
                if (!(mc.level.noCollision(arg0, arg0.getBoundingBox().move(var18)))) {
                    break label_10;
                }
                var9 = (var3 * var15);
                var13 = (var7 * var15);
            }
            var15 = (var15 + 0.5);
        }
        label_10:
        return arg0.getBoundingBox().move(new Vec3(var9, var11, var13));
    }

}
