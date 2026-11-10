package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.EarlyTickHooks;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AirStuck", description = "Suspends movement in the air by freezing movement packets.", category = Module.Category.MOVEMENT)
public class AirStuckModule extends Module {
    public final BooleanSetting onlyWhenFalling;
    private Vec3 savedVelocity;
    private final Runnable earlyTickCallback;

    public AirStuckModule() {
        this.onlyWhenFalling = new BooleanSetting("Only When Falling", "Only captures velocity while falling.", false);
        this.savedVelocity = Vec3.ZERO;
        this.earlyTickCallback = this::onEarlyTick;
    }

    public void onEnable() {
        EarlyTickHooks.register(this.earlyTickCallback);
    }

    public void onDisable() {
        EarlyTickHooks.unregister(this.earlyTickCallback);
        if (!(getNull()) && !(this.savedVelocity.equals(Vec3.ZERO))) {
            mc.player.setDeltaMovement(this.savedVelocity);
        }
        this.savedVelocity = Vec3.ZERO;
    }

    private void onEarlyTick() {
        if ((!(isToggled())) || (getNull())) {
            return;
        }
        if (this.onlyWhenFalling.getValue()) {
            if (mc.player.fallDistance <= 0.0) break label_3;
        }
        if ((!(this.savedVelocity.equals(Vec3.ZERO))) || (mc.player.isSprinting())) break label_3;
        this.savedVelocity = mc.player.getDeltaMovement();
        label_3:
    }

    private boolean isActive() {
        if ((!(isToggled())) || (getNull())) break label_0;
        return !(this.savedVelocity.equals(Vec3.ZERO));
    }

    @SubscribeEvent(priority = 2147483647)
    public void onPacketSend(PacketSendEvent arg0) {
        if (!(isActive())) return;
        Object var2 = arg0.getPacket();
        if (!(var2 instanceof ServerboundClientTickEndPacket) && !(var2 instanceof ServerboundPlayerCommandPacket)) {
            if ((var2 instanceof ServerboundMovePlayerPacket)) {
            }
            arg0.setCancelled(true);
        }
    }

    private static AirStuckModule instance() {
        if (LightningClient.MODULE_MANAGER == null) {
        } else {
        }
        return ((AirStuckModule) LightningClient.MODULE_MANAGER.getModule(AirStuckModule.class));
    }

    public static Vec3 movementDisplacement(Vec3 arg0) {
        Object var1 = instance();
        if (var1 != null && var1.isActive()) {
        } else {
        }
        return arg0;
    }

    public static void localPlayerConstructed() {
        Object var0 = instance();
        if (var0 != null && var0.isToggled()) {
            var0.setToggled(false, false);
        }
    }

}
