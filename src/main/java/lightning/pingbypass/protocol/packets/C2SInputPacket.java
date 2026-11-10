package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SInputPacket extends PbPacket {
    public static final int ID = 10;
    public static final int TYPE_MOUSE = 0;
    public static final int TYPE_KEY = 1;
    public static final int ACTION_PRESS = 1;
    public static final int ACTION_RELEASE = 0;
    private final int inputType;
    private final int button;
    private final int action;

    public C2SInputPacket(int arg0, int arg1, int arg2) {
        this.inputType = arg0;
        this.button = arg1;
        this.action = arg2;
    }

    public C2SInputPacket(FriendlyByteBuf arg0) {
        this.inputType = arg0.readVarInt();
        this.button = arg0.readVarInt();
        this.action = arg0.readVarInt();
    }

    public int getPacketId() {
        return 10;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeVarInt(this.inputType);
        arg0.writeVarInt(this.button);
        arg0.writeVarInt(this.action);
    }

    public int getInputType() {
        return this.inputType;
    }

    public int getButton() {
        return this.button;
    }

    public int getAction() {
        return this.action;
    }

}
