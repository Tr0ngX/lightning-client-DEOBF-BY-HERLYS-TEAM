package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "ReverseStep", description = "Makes it so that you fall down instantly at a specified height.", category = Module.Category.MOVEMENT)
public class ReverseStepModule extends Module {
    public NumberSetting height;

    public ReverseStepModule() {
        this.height = new NumberSetting("Height", "The maximum height at which instant falling will be applied to.", 3.0, 0.0, 12.0);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (!(LightningClient.SERVER_MANAGER.getSetbackTimer().hasTimeElapsed(300L))) return;
        if (((((((mc.player.isPassenger()) || (mc.player.isFallFlying())) || (mc.player.onClimbable())) || (mc.player.isInLava())) || (mc.player.isInWater())) || (mc.player.input.keyPresses.jump())) || (mc.player.input.keyPresses.shift())) {
            return;
        }
        if (mc.player.onGround() && nearBlock(this.height.getValue().doubleValue())) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -(this.height.getValue().doubleValue()), mc.player.getDeltaMovement().z);
        }
    }

    public String getMetaData() {
        return String.valueOf(this.height.getValue().floatValue());
    }

    private boolean nearBlock(double arg0) {
        double var3 = 0.0;
        while (var3 < (arg0 + 0.5)) {
            if (mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(0.0, -(var3), 0.0))) {
            } else {
                return true;
            }
            var3 = (var3 + 0.01);
        }
        return false;
    }

}
