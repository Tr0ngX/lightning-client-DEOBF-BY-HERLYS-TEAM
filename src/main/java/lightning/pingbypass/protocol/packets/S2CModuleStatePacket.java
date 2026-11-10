package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CModuleStatePacket extends PbPacket {
    public static final int ID = 5;
    private final String moduleName;
    private final boolean enabled;

    public S2CModuleStatePacket(String arg0, boolean arg1) {
        this.moduleName = arg0;
        this.enabled = arg1;
    }

    public S2CModuleStatePacket(FriendlyByteBuf arg0) {
        this.moduleName = arg0.readUtf();
        this.enabled = arg0.readBoolean();
    }

    public int getPacketId() {
        return 5;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeUtf(this.moduleName);
        arg0.writeBoolean(this.enabled);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

}
