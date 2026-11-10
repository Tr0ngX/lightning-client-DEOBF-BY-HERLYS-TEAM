package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class S2CMiningStatePacket extends PbPacket {
    public static final int ID = 12;
    private final boolean hasPrimary;
    @Nullable
    private final BlockPos primaryPos;
    private final float primaryProgress;
    private final boolean hasSecondary;
    @Nullable
    private final BlockPos secondaryPos;
    private final float secondaryProgress;

    public S2CMiningStatePacket(BlockPos arg0, float arg1, BlockPos arg2, float arg3) {
        this.hasPrimary = arg0 != null;
        this.primaryPos = arg0;
        this.primaryProgress = arg1;
        this.hasSecondary = arg2 != null;
        this.secondaryPos = arg2;
        this.secondaryProgress = arg3;
    }

    public S2CMiningStatePacket(FriendlyByteBuf arg0) {
        this.hasPrimary = arg0.readBoolean();
        if (!(!this.hasPrimary)) {
            this.primaryPos = new BlockPos(arg0.readInt(), arg0.readInt(), arg0.readInt());
            this.primaryProgress = arg0.readFloat();
        } else {
            this.primaryPos = null;
            this.primaryProgress = 0.0f;
        }
        this.hasSecondary = arg0.readBoolean();
        if (!(!this.hasSecondary)) {
            this.secondaryPos = new BlockPos(arg0.readInt(), arg0.readInt(), arg0.readInt());
            this.secondaryProgress = arg0.readFloat();
        } else {
            this.secondaryPos = null;
            this.secondaryProgress = 0.0f;
        }
    }

    public int getPacketId() {
        return 12;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeBoolean(this.hasPrimary);
        if (!(!this.hasPrimary)) {
            arg0.writeInt(this.primaryPos.getX());
            arg0.writeInt(this.primaryPos.getY());
            arg0.writeInt(this.primaryPos.getZ());
            arg0.writeFloat(this.primaryProgress);
        }
        arg0.writeBoolean(this.hasSecondary);
        if (!(!this.hasSecondary)) {
            arg0.writeInt(this.secondaryPos.getX());
            arg0.writeInt(this.secondaryPos.getY());
            arg0.writeInt(this.secondaryPos.getZ());
            arg0.writeFloat(this.secondaryProgress);
        }
    }

    public boolean hasPrimary() {
        return this.hasPrimary;
    }

    @Nullable
    public BlockPos getPrimaryPos() {
        return this.primaryPos;
    }

    public float getPrimaryProgress() {
        return this.primaryProgress;
    }

    public boolean hasSecondary() {
        return this.hasSecondary;
    }

    @Nullable
    public BlockPos getSecondaryPos() {
        return this.secondaryPos;
    }

    public float getSecondaryProgress() {
        return this.secondaryProgress;
    }

}
