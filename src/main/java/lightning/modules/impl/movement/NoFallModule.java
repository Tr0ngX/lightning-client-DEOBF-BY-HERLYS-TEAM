package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.ai.attributes.Attributes;

@RegisterModule(name = "NoFall", description = "Prevents you from taking fall damage.", category = Module.Category.MOVEMENT)
public class NoFallModule extends Module {
    public ModeSetting mode;

    public NoFallModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Packet";
        tmp0[1] = "Grim";
        this.mode = new ModeSetting("Mode", "The method that will be used in preventing fall damage.", "Packet", tmp0);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (!(isFalling())) return;
        if (this.mode.getValue().equalsIgnoreCase("Grim")) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), (mc.player.getY() + 1.0E-9), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(), false, mc.player.horizontalCollision));
            mc.player.resetFallDistance();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Packet"))) return;
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ServerboundMovePlayerPacket))) break label_3;
        Object var3 = ((ServerboundMovePlayerPacket) var2);
        if (!(isFalling())) break label_3;
        ((PlayerMoveC2SPacketAccessor) var3).setOnGround(true);
        label_3:
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    private boolean isFalling() {
        if ((mc.player.fallDistance <= mc.player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) || (mc.player.onGround())) break label_0;
        return !(mc.player.isFallFlying());
    }

}
