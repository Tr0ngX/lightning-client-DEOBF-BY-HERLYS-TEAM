package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SModuleTogglePacket extends PbPacket {
    public static final int ID = 3;
    private final String moduleName;
    private final boolean enabled;

    public C2SModuleTogglePacket(String arg0, boolean arg1) {
        this.moduleName = arg0;
        this.enabled = arg1;
    }

    public C2SModuleTogglePacket(FriendlyByteBuf arg0) {
        this.moduleName = arg0.readUtf();
        this.enabled = arg0.readBoolean();
    }

    public int getPacketId() {
        return 3;
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
