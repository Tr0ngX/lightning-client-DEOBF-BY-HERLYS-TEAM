package lightning.pingbypass.protocol.packets;

import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class S2CSettingStatePacket extends PbPacket {
    public static final int ID = 6;
    private final String moduleName;
    private final String settingName;
    private final String value;

    public S2CSettingStatePacket(String arg0, String arg1, String arg2) {
        this.moduleName = arg0;
        this.settingName = arg1;
        this.value = arg2;
    }

    public S2CSettingStatePacket(FriendlyByteBuf arg0) {
        this.moduleName = arg0.readUtf();
        this.settingName = arg0.readUtf();
        this.value = arg0.readUtf();
    }

    public int getPacketId() {
        return 6;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeUtf(this.moduleName);
        arg0.writeUtf(this.settingName);
        arg0.writeUtf(this.value);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getSettingName() {
        return this.settingName;
    }

    public String getValue() {
        return this.value;
    }

}
