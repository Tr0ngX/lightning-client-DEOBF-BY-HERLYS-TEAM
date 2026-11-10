package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

@RegisterModule(name = "XCarry", description = "Allows you to carry items in your crafting slots.", category = Module.Category.PLAYER)
public class XCarryModule extends Module {
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (mc.player == null) return;
        if ((arg0.getPacket() instanceof ServerboundContainerClosePacket)) {
            arg0.setCancelled(true);
        }
    }

    public void onDisable() {
        if (mc.player == null) return;
        mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
    }

}
