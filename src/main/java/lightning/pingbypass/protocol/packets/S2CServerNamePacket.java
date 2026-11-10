package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CServerNamePacket extends PbPacket {
    public static final int ID = 8;
    private final String serverIp;

    public S2CServerNamePacket(String arg0) {
        this.serverIp = arg0;
    }

    public S2CServerNamePacket(FriendlyByteBuf arg0) {
        this.serverIp = arg0.readUtf();
    }

    public int getPacketId() {
        return 8;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeUtf(this.serverIp);
    }

    public String getServerIp() {
        return this.serverIp;
    }

}
