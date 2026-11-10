package lightning.pingbypass.protocol;

import net.minecraft.network.FriendlyByteBuf;

public abstract class PbPacket {
    public abstract int getPacketId();

    public abstract void write(FriendlyByteBuf arg0);

}
