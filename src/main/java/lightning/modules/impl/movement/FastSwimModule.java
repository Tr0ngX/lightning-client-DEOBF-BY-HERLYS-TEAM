package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.MovementUtils;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "FastSwim", description = "Modifies your swim speed in water and lava.", category = Module.Category.MOVEMENT)
public class FastSwimModule extends Module {
    private static final double DEFAULT_VERTICAL_SPEED = 0.04;
    public NumberSetting hWater;
    public NumberSetting vWater;
    public NumberSetting hLava;
    public NumberSetting vLava;

    public FastSwimModule() {
        this.hWater = new NumberSetting("H-Water", "Horizontal swim speed multiplier in water.", 1.0f, 0.1f, 20.0f);
        this.vWater = new NumberSetting("V-Water", "Vertical (up/down) swim speed multiplier in water.", 1.0f, 0.1f, 20.0f);
        this.hLava = new NumberSetting("H-Lava", "Horizontal swim speed multiplier in lava.", 1.0f, 0.1f, 20.0f);
        this.vLava = new NumberSetting("V-Lava", "Vertical (up/down) swim speed multiplier in lava.", 1.0f, 0.1f, 20.0f);
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        if (mc.player == null) return;
        if (mc.player.onGround()) return;
        Object var4 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var4.isToggled() && var4.mode.getValue().equalsIgnoreCase("Control") && mc.player.isFallFlying()) return;
        if (mc.player.isInLava()) {
            float var3 = this.hLava.getValue().floatValue();
            float var2 = this.vLava.getValue().floatValue();
            break label_5;
        }
        if (mc.player.isInWater()) {
            var3 = this.hWater.getValue().floatValue();
            var2 = this.vWater.getValue().floatValue();
            break label_5;
        }
        return;
        label_5:
        Object var5 = MovementUtils.forward((MovementUtils.DEFAULT_SPEED * ((double) var3)));
        double var6 = arg0.getMovement().y;
        if (mc.options.keyJump.isDown()) {
            var6 = (0.04 * ((double) var2));
            break label_7;
        }
        if (!(mc.options.keyShift.isDown())) break label_7;
        var6 = (-0.04 * ((double) var2));
        label_7:
        arg0.setMovement(new Vec3(var5.x, var6, var5.y));
        arg0.setCancelled(true);
    }

}
