package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.KeyboardTickEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.system.MathUtils;
import lombok.Generated;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

@RegisterModule(name = "Freecam", description = "Allows you to move your camera anywhere you want without restriction.", category = Module.Category.VISUALS)
public class FreecamModule extends Module implements IFreecamModule {
    public NumberSetting horizontalSpeed;
    public NumberSetting verticalSpeed;
    public BooleanSetting rotate;
    public BooleanSetting shift;
    private float freeYaw;
    private float freePitch;
    private float prevFreeYaw;
    private float prevFreePitch;
    private double freeX;
    private double freeY;
    private double freeZ;
    private double prevFreeX;
    private double prevFreeY;
    private double prevFreeZ;
    private CameraType prevCameraType;

    public FreecamModule() {
        this.horizontalSpeed = new NumberSetting("HorizontalSpeed", "The speed at which your camera will move horizontally.", 1.0f, 0.1f, 3.0f);
        this.verticalSpeed = new NumberSetting("VerticalSpeed", "The speed at which your camera will move vertically.", 0.5f, 0.1f, 3.0f);
        this.rotate = new BooleanSetting("Rotate", "Rotates your real player model along with the camera.", false);
        this.shift = new BooleanSetting("Shift", "Allows moving down with shift even when a GUI is open.", false);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        this.prevFreeYaw = this.freeYaw;
        this.prevFreePitch = this.freePitch;
        if (this.rotate.getValue()) {
            this.freeYaw = mc.player.getYRot();
            this.freePitch = mc.player.getXRot();
        }
    }

    public void onMouseTurn(double arg0, double arg1) {
        float var5 = (((float) arg1) * 0.15f);
        float var6 = (((float) arg0) * 0.15f);
        this.freePitch = Mth.clamp((this.freePitch + var5), -90.0f, 90.0f);
        this.freeYaw = (this.freeYaw + var6);
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent arg0) {
        if (mc.player == null) return;
        this.prevFreeX = this.freeX;
        this.prevFreeY = this.freeY;
        this.prevFreeZ = this.freeZ;
        float var2 = arg0.getMovementForward();
        float var3 = arg0.getMovementSideways();
        double var4 = this.horizontalSpeed.getValue().doubleValue();
        if (var2 == 0.0f) {
            if (var3 != 0.0f) {
            }
            float var6 = ((float) Math.sqrt(((double) ((var2 * var2) + (var3 * var3)))));
            if (var6 > 1.0E-4f && Math.abs((var6 - 1.0f)) > 1.0E-4f) {
                var2 = (var2 / var6);
                var3 = (var3 / var6);
            }
            double var7 = Math.cos(Math.toRadians(((double) (this.freeYaw + 90.0f))));
            double var9 = Math.sin(Math.toRadians(((double) (this.freeYaw + 90.0f))));
            double var11 = (((((double) var2) * var4) * var7) + ((((double) var3) * var4) * var9));
            double var13 = (((((double) var2) * var4) * var9) - ((((double) var3) * var4) * var7));
            this.freeX = (this.freeX + var11);
            this.freeZ = (this.freeZ + var13);
        }
        if (mc.options.keyJump.isDown()) {
            this.freeY = (this.freeY + this.verticalSpeed.getValue().doubleValue());
        }
        if (!(mc.options.keyShift.isDown())) break label_6;
        if (!(this.shift.getValue())) {
            if (mc.gui.screen() != null) break label_6;
        }
        this.freeY = (this.freeY - this.verticalSpeed.getValue().doubleValue());
        label_6:
        arg0.setMovementForward(0.0f);
        arg0.setMovementSideways(0.0f);
        arg0.setCancelled(true);
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
            return;
        }
        mc.smartCull = false;
        this.prevCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        this.prevFreeYaw = mc.player.getYRot();
        this.freeYaw = mc.player.getYRot();
        this.prevFreePitch = mc.player.getXRot();
        this.freePitch = mc.player.getXRot();
        this.prevFreeX = mc.player.getX();
        null.freeX = this;
        this.prevFreeY = (mc.player.getY() + ((double) mc.player.getEyeHeight(mc.player.getPose())));
        null.freeY = this;
        this.prevFreeZ = mc.player.getZ();
        null.freeZ = this;
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if ((arg0.getPlayer() == null) || (mc.player == null)) {
            return;
        }
        setToggled(false);
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientConnectEvent arg0) {
        setToggled(false);
    }

    public void onDisable() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        mc.smartCull = true;
        if (this.prevCameraType != null) {
            mc.options.setCameraType(this.prevCameraType);
        }
    }

    public boolean isRotate() {
        return this.rotate.getValue();
    }

    public float getFreeYaw() {
        return ((float) MathUtils.interpolate(((double) this.prevFreeYaw), ((double) this.freeYaw), ((double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true))));
    }

    public float getFreePitch() {
        return ((float) MathUtils.interpolate(((double) this.prevFreePitch), ((double) this.freePitch), ((double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true))));
    }

    public double getFreeX() {
        return MathUtils.interpolate(this.prevFreeX, this.freeX, ((double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)));
    }

    public double getFreeY() {
        return MathUtils.interpolate(this.prevFreeY, this.freeY, ((double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)));
    }

    public double getFreeZ() {
        return MathUtils.interpolate(this.prevFreeZ, this.freeZ, ((double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)));
    }

    @Generated
    public NumberSetting getHorizontalSpeed() {
        return this.horizontalSpeed;
    }

    @Generated
    public NumberSetting getVerticalSpeed() {
        return this.verticalSpeed;
    }

    @Generated
    public BooleanSetting getRotate() {
        return this.rotate;
    }

    @Generated
    public BooleanSetting getShift() {
        return this.shift;
    }

    @Generated
    public float getPrevFreeYaw() {
        return this.prevFreeYaw;
    }

    @Generated
    public float getPrevFreePitch() {
        return this.prevFreePitch;
    }

    @Generated
    public double getPrevFreeX() {
        return this.prevFreeX;
    }

    @Generated
    public double getPrevFreeY() {
        return this.prevFreeY;
    }

    @Generated
    public double getPrevFreeZ() {
        return this.prevFreeZ;
    }

    @Generated
    public CameraType getPrevCameraType() {
        return this.prevCameraType;
    }

    @Generated
    public void setHorizontalSpeed(NumberSetting arg0) {
        this.horizontalSpeed = arg0;
    }

    @Generated
    public void setVerticalSpeed(NumberSetting arg0) {
        this.verticalSpeed = arg0;
    }

    @Generated
    public void setRotate(BooleanSetting arg0) {
        this.rotate = arg0;
    }

    @Generated
    public void setShift(BooleanSetting arg0) {
        this.shift = arg0;
    }

    @Generated
    public void setFreeYaw(float arg0) {
        this.freeYaw = arg0;
    }

    @Generated
    public void setFreePitch(float arg0) {
        this.freePitch = arg0;
    }

    @Generated
    public void setPrevFreeYaw(float arg0) {
        this.prevFreeYaw = arg0;
    }

    @Generated
    public void setPrevFreePitch(float arg0) {
        this.prevFreePitch = arg0;
    }

    @Generated
    public void setFreeX(double arg0) {
        this.freeX = arg0;
    }

    @Generated
    public void setFreeY(double arg0) {
        this.freeY = arg0;
    }

    @Generated
    public void setFreeZ(double arg0) {
        this.freeZ = arg0;
    }

    @Generated
    public void setPrevFreeX(double arg0) {
        this.prevFreeX = arg0;
    }

    @Generated
    public void setPrevFreeY(double arg0) {
        this.prevFreeY = arg0;
    }

    @Generated
    public void setPrevFreeZ(double arg0) {
        this.prevFreeZ = arg0;
    }

    @Generated
    public void setPrevCameraType(CameraType arg0) {
        this.prevCameraType = arg0;
    }

}
