package lightning.pingbypass.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PbCustomPayload(byte[] data) implements CustomPacketPayload {
    public static final Identifier CHANNEL;
    public static final CustomPacketPayload.Type ID;
    public static final int C2S_JOIN = 0;
    public static final int S2C_PASSWORD_REQUEST = 1;
    public static final int C2S_PASSWORD = 2;
    public static final int S2C_ERROR = 7;
    public static final StreamCodec CODEC;

    public PbCustomPayload(byte[] arg0) {
        this.data = arg0;
    }

    public CustomPacketPayload.Type type() {
        return ID;
    }

    public FriendlyByteBuf toBuf() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.data));
    }

    public static PbCustomPayload fromPacket(PbPacket arg0) {
        Object var1 = new FriendlyByteBuf(Unpooled.buffer());
        var1.writeVarInt(arg0.getPacketId());
        arg0.write(var1);
        Object var2 = new byte[var1.readableBytes()];
        var1.readBytes(var2);
        var1.release();
        return new PbCustomPayload(var2);
    }

    public static ServerboundCustomPayloadPacket createC2SPacket(PbPacket arg0) {
        return new ServerboundCustomPayloadPacket(fromPacket(arg0));
    }

    public static PbCustomPayload passwordRequest() {
        Object var0 = new FriendlyByteBuf(Unpooled.buffer());
        var0.writeVarInt(1);
        Object var1 = new byte[var0.readableBytes()];
        var0.readBytes(var1);
        var0.release();
        return new PbCustomPayload(var1);
    }

    public static PbCustomPayload error(String arg0) {
        Object var1 = new FriendlyByteBuf(Unpooled.buffer());
        var1.writeVarInt(7);
        var1.writeUtf(arg0);
        Object var2 = new byte[var1.readableBytes()];
        var1.readBytes(var2);
        var1.release();
        return new PbCustomPayload(var2);
    }

    public byte[] data() {
        return this.data;
    }

    static {
        CHANNEL = Identifier.fromNamespaceAndPath("lightning", "pingbypass");
        ID = new CustomPacketPayload.Type(CHANNEL);
        CODEC = new PbCustomPayload.1();
    }


    class 1 implements StreamCodec {
        public PbCustomPayload decode(RegistryFriendlyByteBuf arg0) {
            Object var2 = new byte[arg0.readableBytes()];
            arg0.readBytes(var2);
            return new PbCustomPayload(var2);
        }

        public void encode(RegistryFriendlyByteBuf arg0, PbCustomPayload arg1) {
            arg0.writeBytes(arg1.data());
        }

    }
}
