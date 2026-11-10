package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SStayPacket extends PbPacket {
    public static final int ID = 9;
    private final boolean stay;

    public C2SStayPacket(boolean arg0) {
        this.stay = arg0;
    }

    public C2SStayPacket(FriendlyByteBuf arg0) {
        this.stay = arg0.readBoolean();
    }

    public int getPacketId() {
        return 9;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeBoolean(this.stay);
    }

    public boolean isStay() {
        return this.stay;
    }

}
