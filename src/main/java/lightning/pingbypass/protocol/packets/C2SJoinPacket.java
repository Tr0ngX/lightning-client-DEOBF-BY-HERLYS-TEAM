package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SJoinPacket extends PbPacket {
    public static final int ID = 0;
    private final String serverIp;
    private final int serverPort;

    public C2SJoinPacket(String arg0, int arg1) {
        this.serverIp = arg0;
        this.serverPort = arg1;
    }

    public C2SJoinPacket(FriendlyByteBuf arg0) {
        this.serverIp = arg0.readUtf();
        this.serverPort = arg0.readVarInt();
    }

    public int getPacketId() {
        return 0;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeUtf(this.serverIp);
        arg0.writeVarInt(this.serverPort);
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public int getServerPort() {
        return this.serverPort;
    }

}
