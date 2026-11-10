package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class S2CRenderPositionPacket extends PbPacket {
    public static final int ID = 10;
    @Nullable
    private final BlockPos position;

    public S2CRenderPositionPacket(BlockPos arg0) {
        this.position = arg0;
    }

    public S2CRenderPositionPacket(FriendlyByteBuf arg0) {
        if (arg0.readBoolean()) {
        } else {
        }
        new BlockPos(arg0.readInt(), arg0.readInt(), arg0.readInt()).position = null;
    }

    public int getPacketId() {
        return 10;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeBoolean(this.position != null);
        if (this.position != null) {
            arg0.writeInt(this.position.getX());
            arg0.writeInt(this.position.getY());
            arg0.writeInt(this.position.getZ());
        }
    }

    @Nullable
    public BlockPos getPosition() {
        return this.position;
    }

}
