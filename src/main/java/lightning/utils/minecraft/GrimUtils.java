package lightning.utils.minecraft;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class GrimUtils {
    public static double[] getPossibleEyeHeights(Player arg0) {
        float var2 = ((float) arg0.getAttributeValue(Attributes.SCALE));
        double var3 = (1.62 * ((double) var2));
        double var5 = (1.27 * ((double) var2));
        double var7 = (0.4 * ((double) var2));
        Object var9 = arg0.getPose();
        switch (GrimUtils.1.$SwitchMap$net$minecraft$world$entity$Pose[var9.ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_0; }
            case 3 -> { break label_0; }
            case 4 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        Object var10 = new double[3];
        var10[0] = var7;
        var10[1] = var3;
        Object var1 = var10;
        var10[2] = var5;
        break label_3;
        label_1:
        var10 = new double[3];
        var10[0] = var5;
        var10[1] = var3;
        var1 = var10;
        var10[2] = var7;
        break label_3;
        label_2:
        var10 = new double[3];
        var10[0] = var3;
        var10[1] = var5;
        var1 = var10;
        var10[2] = var7;
        label_3:
        return var1;
    }

    public static Vec3[] getPossibleEyePositions(Player arg0) {
        Object var1 = arg0.position();
        Object var2 = getPossibleEyeHeights(arg0);
        Object var3 = new Vec3[var2.length];
        int var4 = 0;
        while (var4 < var2.length) {
            var3[var4] = var1.add(0.0, var2[var4], 0.0);
            var4++;
        }
        return var3;
    }


    class 1 {

        static {
            $SwitchMap$net$minecraft$world$entity$Pose = new int[Pose.values().length];
            try {
                $SwitchMap$net$minecraft$world$entity$Pose[Pose.FALL_FLYING.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$world$entity$Pose[Pose.SPIN_ATTACK.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$net$minecraft$world$entity$Pose[Pose.SWIMMING.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
                try {
                }
                $SwitchMap$net$minecraft$world$entity$Pose[Pose.CROUCHING.ordinal()] = 4;
            } catch (NoSuchFieldError ex11) {
                var0 = ex11;
            }
        }

    }
}
