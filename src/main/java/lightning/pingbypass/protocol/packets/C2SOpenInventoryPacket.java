package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SOpenInventoryPacket extends PbPacket {
    public static final int ID = 15;
    private final boolean open;

    public C2SOpenInventoryPacket(boolean arg0) {
        this.open = arg0;
    }

    public C2SOpenInventoryPacket(FriendlyByteBuf arg0) {
        this.open = arg0.readBoolean();
    }

    public int getPacketId() {
        return 15;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeBoolean(this.open);
    }

    public boolean isOpen() {
        return this.open;
    }

}
