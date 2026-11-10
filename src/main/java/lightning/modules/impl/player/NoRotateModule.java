package lightning.modules.impl.player;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.mixins.accessors.PlayerPositionAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.minecraft.PositionUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.Relative;

@RegisterModule(name = "NoRotate", description = "Prevents the server from forcing rotations on you.", category = Module.Category.PLAYER)
public class NoRotateModule extends Module {
    public BooleanSetting inBlocks;
    public BooleanSetting spoof;

    public NoRotateModule() {
        this.inBlocks = new BooleanSetting("InBlocks", "Whether or not to stop rotations whenever inside of a block.", false);
        this.spoof = new BooleanSetting("Spoof", "Sends rotation packets once you have been rubberbanded.", false);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.inBlocks.getValue()) && !(mc.level.getBlockState(PositionUtils.getFlooredPosition(mc.player)).canBeReplaced())) return;
        Object var2 = arg0.getPacket();
        if ((var2 instanceof ClientboundPlayerPositionPacket)) {
            Object var3 = ((ClientboundPlayerPositionPacket) var2);
            if (this.spoof.getValue()) {
                LightningClient.ROTATION_MANAGER.packetRotate(var3.change().yRot(), var3.change().xRot());
                LightningClient.ROTATION_MANAGER.packetRotate(mc.player.getYRot(), mc.player.getXRot());
            }
            ((PlayerPositionAccessor) var3.change()).setYaw(mc.player.getYRot());
            ((PlayerPositionAccessor) var3.change()).setPitch(mc.player.getXRot());
            var3.relatives().remove(Relative.X_ROT);
            var3.relatives().remove(Relative.Y_ROT);
        }
    }

}
