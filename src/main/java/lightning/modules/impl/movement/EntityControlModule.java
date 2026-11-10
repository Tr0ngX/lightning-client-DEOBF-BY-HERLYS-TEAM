package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.MovementUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "EntityControl", description = "Allows you to control and fly with ridden entities.", category = Module.Category.MOVEMENT)
public class EntityControlModule extends Module {
    public static EntityControlModule INSTANCE;
    public BooleanSetting fly;
    public NumberSetting speed;
    public NumberSetting verticalSpeed;
    public NumberSetting fallSpeed;
    public BooleanSetting control;
    public BooleanSetting noSync;
    private Entity controlledVehicle;

    public EntityControlModule() {
        this.fly = new BooleanSetting("Fly", "Allows you to fly with your vehicle.", true);
        this.speed = new NumberSetting("Speed", "Horizontal flying speed.", 5.0, 0.1, 50.0);
        this.verticalSpeed = new NumberSetting("VerticalSpeed", "Vertical flying speed.", 6.0, 0.0, 20.0);
        this.fallSpeed = new NumberSetting("FallSpeed", "Glide down speed when no vertical key is pressed.", 0.1, 0.0, 50.0);
        this.control = new BooleanSetting("Control", "Steer entities even without a saddle.", true);
        this.noSync = new BooleanSetting("NoSync", "Cancels server vehicle sync packets to prevent rubberbanding.", false);
        INSTANCE = this;
    }

    public static EntityControlModule getInstance() {
        return INSTANCE;
    }

    public void onDisable() {
        releaseVehicleGravity();
    }

    private void releaseVehicleGravity() {
        if (this.controlledVehicle != null) {
            this.controlledVehicle.setNoGravity(false);
            this.controlledVehicle = null;
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (!(getNull())) {
            if (this.fly.getValue()) break label_1;
        }
        releaseVehicleGravity();
        return;
        label_1:
        Object var4 = mc.player.getVehicle();
        if (var4 == null) {
            releaseVehicleGravity();
            return;
        }
        if (this.controlledVehicle != null && this.controlledVehicle != var4) {
            releaseVehicleGravity();
        }
        this.controlledVehicle = var4;
        var4.setNoGravity(true);
        var4.setYRot(mc.player.getYRot());
        Object var5 = MovementUtils.forward(this.speed.getValue().doubleValue());
        double var6 = var5.x;
        double var8 = var5.y;
        if (!((mc.gui.screen() instanceof ChatScreen))) {
            if (mc.gui.screen() != null) {
            }
            double var2 = (-(this.fallSpeed.getValue().doubleValue()) / 20.0);
        } else {
            int var11 = mc.options.keyJump.isDown();
            if (!(mc.options.keySprint.isDown())) {
                if (mc.options.keyShift.isDown()) {
                }
            } else {
            }
            int var10 = 0;
            int var12 = 0;
            if (!(!var11)) {
                if (!(!var10)) {
                    break label_12;
                }
                break label_12;
            }
            if (!(!var10)) {
                break label_12;
            }
            label_12:
            var2 = (-(this.fallSpeed.getValue().doubleValue()) / 20.0);
        }
        var4.setDeltaMovement(new Vec3(var6, var2, var8));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (getNull()) return;
        if ((arg0.getPacket() instanceof ClientboundMoveVehiclePacket) && this.noSync.getValue()) {
            arg0.cancel();
        }
    }

}
