package lightning.pingbypass.protocol;

import net.minecraft.network.Connection;

@FunctionalInterface
public interface PbPacketHandler {
    void handle(PbPacket arg0, Connection arg1);

}
