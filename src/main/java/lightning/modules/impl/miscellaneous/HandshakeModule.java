package lightning.modules.impl.miscellaneous;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.mixins.accessors.CustomPayloadC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.StringSetting;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;

@RegisterModule(name = "Handshake", description = "Spoofs your client handshake to make the server think that you are playing on a different client.", category = Module.Category.MISCELLANEOUS)
public class HandshakeModule extends Module {
    public StringSetting brand;

    public HandshakeModule() {
        this.brand = new StringSetting("Brand", "The brand that the server will think you are playing on.", "vanilla");
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) break label_1;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundCustomPayloadPacket))) break label_1;
        Object var2 = ((ServerboundCustomPayloadPacket) var3);
        if (!(var2.payload().type().id().equals(BrandPayload.TYPE.id()))) return;
        ((CustomPayloadC2SPacketAccessor) var2).setPayload(new BrandPayload(this.brand.getValue()));
        label_1:
    }

}
