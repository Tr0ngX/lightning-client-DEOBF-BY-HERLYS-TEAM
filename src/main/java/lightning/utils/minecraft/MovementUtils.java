package lightning.utils.minecraft;

import lightning.LightningClient;
import lightning.events.impl.PlayerMoveEvent;
import lightning.mixins.accessors.Vec3dAccessor;
import lightning.modules.api.ISprintModule;
import lightning.utils.IMinecraft;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class MovementUtils implements IMinecraft {
    public static double DEFAULT_SPEED;

    public static Vector2d forward(double arg0) {
        float var3 = mc.player.input.getMoveVector().y;
        float var4 = mc.player.input.getMoveVector().x;
        float var5 = mc.player.getYRot();
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var2 = tmp0;
        if ((tmp0 == null) || (!(var2.isGrimCompensating()))) break label_0;
        var5 = var2.getGrimYaw();
        label_0:
        if (var3 == 0.0f && var4 == 0.0f) return new Vector2d(0.0, 0.0);
        float var6 = ((float) Math.sqrt(((double) ((var3 * var3) + (var4 * var4)))));
        if (var6 > 1.0E-4f && Math.abs((var6 - 1.0f)) > 1.0E-4f) {
            var3 = (var3 / var6);
            var4 = (var4 / var6);
        }
        double var7 = Math.cos(Math.toRadians(((double) (var5 + 90.0f))));
        double var9 = Math.sin(Math.toRadians(((double) (var5 + 90.0f))));
        return new Vector2d((((((double) var3) * arg0) * var7) + ((((double) var4) * arg0) * var9)), (((((double) var3) * arg0) * var9) - ((((double) var4) * arg0) * var7)));
    }

    public static double[] straightForward(double arg0) {
        float var3 = mc.player.getYRot();
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint"));
        Object var2 = tmp0;
        if ((tmp0 == null) || (!(var2.isGrimCompensating()))) break label_0;
        var3 = var2.getGrimYaw();
        label_0:
        var tmp1 = new double[2];
        tmp1[0] = (arg0 * Math.cos(Math.toRadians(((double) (var3 + 90.0f)))));
        tmp1[1] = (arg0 * Math.sin(Math.toRadians(((double) (var3 + 90.0f)))));
        return tmp1;
    }

    public static double getPotionSpeed(double arg0) {
        if (mc.player.hasEffect(MobEffects.SPEED)) {
            arg0 = (arg0 * (1.0 + (0.2 * ((double) (mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1)))));
        }
        if (mc.player.hasEffect(MobEffects.SLOWNESS)) {
            arg0 = (arg0 / (1.0 + (0.2 * ((double) (mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier() + 1)))));
        }
        return arg0;
    }

    public static double getPotionJump(double arg0) {
        if (mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            arg0 = (arg0 + ((double) (((float) (mc.player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1)) * 0.1f)));
        }
        return arg0;
    }

    public static boolean isMoving() {
        if (mc.player == null) return false;
        if ((((mc.options.keyUp.isDown()) || (mc.options.keyDown.isDown())) || (mc.options.keyLeft.isDown())) || (mc.options.keyRight.isDown())) break label_2;
        if (mc.player.input != null) {
            if ((mc.player.input.getMoveVector().x != 0.0f) || (mc.player.input.getMoveVector().y != 0.0f)) break label_2;
        }
        if (mc.player.xxa != 0.0f) break label_2;
        if (mc.player.zza != 0.0f) {
            label_2:
        } else {
        }
        return false;
    }

    public static void moveTowards(PlayerMoveEvent arg0, Vec3 arg1, double arg2) {
        double var4 = Math.toRadians(((double) RotationUtils.getRotations(arg1)[0]));
        double var6 = (-(Math.sin(var4)) * arg2);
        double var8 = (Math.cos(var4) * arg2);
        var tmp0 = new double[2];
        tmp0[0] = (arg1.x - mc.player.getX());
        tmp0[1] = (arg1.z - mc.player.getZ());
        Object var10 = tmp0;
        if (Math.abs(var6) < Math.abs(var10[0])) {
        } else {
        }
        new Vec3.setMovement(new Vec3);
        if (Math.abs(var8) < Math.abs(var10[1])) {
        } else {
        }
        new Vec3.setMovement(new Vec3);
        ((Vec3dAccessor) mc.player.getDeltaMovement()).setX(0.0);
        ((Vec3dAccessor) mc.player.getDeltaMovement()).setZ(0.0);
        arg0.setCancelled(true);
    }

    static {
        DEFAULT_SPEED = 0.2873;
    }

}
