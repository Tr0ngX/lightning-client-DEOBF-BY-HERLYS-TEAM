package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SPasswordPacket extends PbPacket {
    public static final int ID = 2;
    private final String password;

    public C2SPasswordPacket(String arg0) {
        this.password = arg0;
    }

    public C2SPasswordPacket(FriendlyByteBuf arg0) {
        this.password = arg0.readUtf();
    }

    public int getPacketId() {
        return 2;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeUtf(this.password);
    }

    public String getPassword() {
        return this.password;
    }

}
