package lightning.mixins;

import io.netty.channel.ChannelHandlerContext;
import lightning.pingbypass.PingBypassFlags;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolSwapHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtocolSwapHandler.class)
public interface NetworkStateTransitionHandlerMixin {
    @Inject(method = "handleInboundTerminalPacket", at = @At("HEAD"), cancellable = true)
    private static void handleInboundTerminalPacket(ChannelHandlerContext arg0, Packet arg1, CallbackInfo arg2) {
        if (!PingBypassFlags.suppressEncoderErrors) return;
        Object var4 = arg0.pipeline().get("packet_handler");
        if (!((var4 instanceof Connection))) break label_1;
        var tmp0 = ((Connection) var4);
        Object var3 = tmp0;
        if (tmp0.getReceiving() != PacketFlow.CLIENTBOUND) break label_1;
        arg2.cancel();
        label_1:
    }

    @Inject(method = "handleOutboundTerminalPacket", at = @At("HEAD"), cancellable = true)
    private static void handleOutboundTerminalPacket(ChannelHandlerContext arg0, Packet arg1, CallbackInfo arg2) {
        if (!PingBypassFlags.suppressEncoderErrors) return;
        Object var4 = arg0.pipeline().get("packet_handler");
        if (!((var4 instanceof Connection))) break label_1;
        var tmp0 = ((Connection) var4);
        Object var3 = tmp0;
        if (tmp0.getReceiving() != PacketFlow.CLIENTBOUND) break label_1;
        arg2.cancel();
        label_1:
    }

}
