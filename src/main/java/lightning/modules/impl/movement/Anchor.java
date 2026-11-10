package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.mixins.accessors.ClientInputAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.HoleUtils;
import lightning.utils.minecraft.PositionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;

@RegisterModule(name = "Anchor", description = "Pulls you toward your nearest hole when looking down.", category = Module.Category.MOVEMENT)
public class Anchor extends Module {
    public NumberSetting pitch;
    public BooleanSetting doubles;
    public BooleanSetting stopMotion;
    public BooleanSetting fastFall;
    public NumberSetting speed;
    public BooleanSetting checkAlreadyInHole;
    public BooleanSetting checkTerrainHole;
    public BooleanSetting checkBurrowed;

    public Anchor() {
        this.pitch = new NumberSetting("Pitch", "Minimum pitch angle to activate anchoring.", 60, 0, 90);
        this.doubles = new BooleanSetting("Doubles", "Include 2x1 holes when anchoring.", true);
        this.stopMotion = new BooleanSetting("StopMotion", "Stop horizontal movement while anchoring.", true);
        this.fastFall = new BooleanSetting("FastFall", "Accelerate fall speed while anchoring.", true);
        this.speed = new NumberSetting("FallSpeed", "Speed multiplier for falling.", 1.0f, 1.0f, 5.0f);
        this.checkAlreadyInHole = new BooleanSetting("AlreadyInHole", "Don't anchor if already in a hole.", true);
        this.checkTerrainHole = new BooleanSetting("CheckTerrainHole", "Don't anchor toward terrain holes.", false);
        this.checkBurrowed = new BooleanSetting("CheckBurrowed", "Don't anchor if burrowed.", true);
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        if (((getNull()) || (mc.player == null)) || (mc.level == null)) {
            return;
        }
        if (this.checkAlreadyInHole.getValue() && isAlreadyInHole()) return;
        if (this.checkBurrowed.getValue() && isBurrowed()) return;
        float var2 = mc.player.getXRot();
        if (var2 < this.pitch.getValue().floatValue()) return;
        Object var3 = PositionUtils.getFlooredPosition(mc.player);
        if (mc.player.isFallFlying()) return;
        int var4 = 5;
        int var5 = 0;
        while (var5 <= var4) {
            Object var6 = var3.below((var5 + 1));
            if (mc.level.getBlockState(var6).isAir()) return;
            Object var7 = HoleUtils.getSingleHole(var6, 1.0);
            if (var7 != null) {
                if (this.checkTerrainHole.getValue() && isTerrainHole(var7)) {
                    break label_13;
                }
                applyAnchorLogic(arg0, var7);
                return;
            }
            if (!(this.doubles.getValue())) break label_11;
            var tmp0 = HoleUtils.getDoubleHole(var6, 1.0);
            var7 = tmp0;
            if (tmp0 == null) break label_11;
            if (this.checkTerrainHole.getValue() && isTerrainHole(var7)) {
                break label_13;
            }
            applyAnchorLogic(arg0, var7);
            return;
            label_11:
            var7 = HoleUtils.getQuadHole(var6, 1.0);
            if (var7 == null) break label_13;
            if (this.checkTerrainHole.getValue() && isTerrainHole(var7)) {
                break label_13;
            }
            applyAnchorLogic(arg0, var7);
            return;
            label_13:
            var5++;
        }
    }

    private void applyAnchorLogic(PlayerMoveEvent arg0, HoleUtils.Hole arg1) {
        if (mc.player == null) return;
        Object var4 = arg1.box().getCenter();
        if (this.stopMotion.getValue()) {
            Object var3 = mc.player.getDeltaMovement();
            if (var3 != null) {
                mc.player.setDeltaMovement(0.0, var3.y, 0.0);
            }
            ((ClientInputAccessor) mc.player.input).setMoveVector(Vec2.ZERO);
        }
        if (!(this.fastFall.getValue())) break label_3;
        var tmp0 = mc.player.getDeltaMovement();
        var3 = tmp0;
        if ((tmp0 == null) || (var3.y <= -0.1)) break label_3;
        double var5 = (this.speed.getValue().doubleValue() / 10.0);
        mc.player.setDeltaMovement(var3.x, -(var5), var3.z);
        label_3:
        var5 = (var4.x - mc.player.getX());
        double var7 = (var4.z - mc.player.getZ());
        arg0.setX((var5 / 2.0));
        arg0.setZ((var7 / 2.0));
    }

    private boolean isAlreadyInHole() {
        return HoleUtils.isPlayerInHole(mc.player);
    }

    private boolean isBurrowed() {
        Object var1 = PositionUtils.getFlooredPosition(mc.player);
        var tmp0 = new Direction[4];
        tmp0[0] = Direction.NORTH;
        tmp0[1] = Direction.SOUTH;
        tmp0[2] = Direction.EAST;
        tmp0[3] = Direction.WEST;
        Object var2 = tmp0;
        int var3 = 0;
        Object var4 = var2;
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            Object var8 = var1.relative(var7);
            if (mc.level.getBlockState(var8).isAir()) break label_2;
            if (mc.level.getBlockState(var8).canBeReplaced()) {
                break label_2;
            }
            var3++;
            label_2:
            var6++;
        }
        return var3 == 4;
    }

    private boolean isTerrainHole(HoleUtils.Hole arg0) {
        if (arg0 == null) return false;
        if (arg0.safety() != HoleUtils.HoleSafety.UNSAFE) {
            if (arg0.safety() == HoleUtils.HoleSafety.SAFE) {
            }
        } else {
        }
        return false;
    }

}
