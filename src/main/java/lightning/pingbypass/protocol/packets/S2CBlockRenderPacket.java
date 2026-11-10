package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class S2CBlockRenderPacket extends PbPacket {
    public static final int ID = 11;
    private final BlockPos position;

    public S2CBlockRenderPacket(BlockPos arg0) {
        this.position = arg0;
    }

    public S2CBlockRenderPacket(FriendlyByteBuf arg0) {
        this.position = new BlockPos(arg0.readInt(), arg0.readInt(), arg0.readInt());
    }

    public int getPacketId() {
        return 11;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeInt(this.position.getX());
        arg0.writeInt(this.position.getY());
        arg0.writeInt(this.position.getZ());
    }

    public BlockPos getPosition() {
        return this.position;
    }

}
