package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerInput;

public class S2CWindowClickPacket extends PbPacket {
    public static final int ID = 16;
    private final int containerId;
    private final int slotNum;
    private final int buttonNum;
    private final String containerInput;

    public S2CWindowClickPacket(int arg0, int arg1, int arg2, ContainerInput arg3) {
        this.containerId = arg0;
        this.slotNum = arg1;
        this.buttonNum = arg2;
        this.containerInput = arg3.name();
    }

    public S2CWindowClickPacket(FriendlyByteBuf arg0) {
        this.containerId = arg0.readVarInt();
        this.slotNum = arg0.readVarInt();
        this.buttonNum = arg0.readVarInt();
        this.containerInput = arg0.readUtf();
    }

    public int getPacketId() {
        return 16;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeVarInt(this.containerId);
        arg0.writeVarInt(this.slotNum);
        arg0.writeVarInt(this.buttonNum);
        arg0.writeUtf(this.containerInput);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlotNum() {
        return this.slotNum;
    }

    public int getButtonNum() {
        return this.buttonNum;
    }

    public ContainerInput getContainerInput() {
        return ContainerInput.valueOf(this.containerInput);
    }

}
