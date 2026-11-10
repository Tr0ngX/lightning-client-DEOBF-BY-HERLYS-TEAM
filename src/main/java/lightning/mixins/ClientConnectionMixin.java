package lightning.mixins;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lightning.LightningClient;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.miscellaneous.NoPacketKickModule;
import lightning.pingbypass.ProtocolTransitionTracker;
import lightning.utils.NightBeacon;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ClientConnectionMixin {
    private static final Logger LOGGER;
    private static final long PROTOCOL_TRANSITION_WINDOW_MS = 1000L;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void send$HEAD(Packet arg0, ChannelFutureListener arg1, boolean arg2, CallbackInfo arg3) {
        NightBeacon.pre(this, arg0, arg1, arg2, arg3);
        if (!((arg0 instanceof ClientIntentionPacket))) break label_2;
        Object var6 = ((Connection) this);
        Object var7 = var6.getPacketListener();
        if (var7 == null) break label_0;
        var tmp0 = (var7.protocol() != ConnectionProtocol.HANDSHAKING ? 1 : 0);
        int var5 = tmp0;
        int var8 = tmp0;
        if (!var5) break label_2;
        LOGGER.error("[PB] Caught an outgoing ClientIntentionPacket on {} -- this connection is already past handshake, this send will crash it", var6.getLoggableAddress(false), new Throwable("call site"));
        label_2:
        var5 = new PacketSendEvent(arg0);
        LightningClient.EVENT_HANDLER.post(var5);
        if (var5.isCancelled()) {
            arg3.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("TAIL"), cancellable = true)
    private void send$TAIL(Packet arg0, ChannelFutureListener arg1, boolean arg2, CallbackInfo arg3) {
        LightningClient.EVENT_HANDLER.post(new PacketSendEvent.Post(arg0));
    }

    @Inject(method = "doSendPacket", at = @At("HEAD"), cancellable = true)
    private void doSendPacket$HEAD(Packet arg0, ChannelFutureListener arg1, boolean arg2, CallbackInfo arg3) {
        int var6 = ((System.currentTimeMillis() - ProtocolTransitionTracker.lastTransitionAt()) < 1000L ? 1 : 0);
        Object var7 = arg0.getClass().getPackageName();
        if (!(var7.equals("net.minecraft.network.protocol.game"))) {
            if (var7.equals("net.minecraft.network.protocol.handshake")) {
            }
        } else {
        }
        int var5 = 0;
        int var8 = 0;
        if (!(!var6) && !(!var5)) {
            LOGGER.warn("[PB] Dropped {} queued right as the connection was mid protocol-transition -- would have crashed the session", arg0.getClass().getSimpleName());
            arg3.cancel();
            return;
        }
        if ((arg0 instanceof ClientIntentionPacket)) {
            Object var10 = ((Connection) this);
            Object var11 = var10.getPacketListener();
            if (var11 == null) break label_6;
            var tmp0 = (var11.protocol() != ConnectionProtocol.HANDSHAKING ? 1 : 0);
            int var9 = tmp0;
            int var12 = tmp0;
            if (!(!var9)) return;
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void channelRead0(ChannelHandlerContext arg0, Packet arg1, CallbackInfo arg2) {
        Object var4 = new PacketReceiveEvent(arg1, ((Connection) this));
        LightningClient.EVENT_HANDLER.post(var4);
        if (!((arg1 instanceof ClientboundBundlePacket))) break label_1;
        Object var5 = ((ClientboundBundlePacket) arg1);
        Object var6 = var5.subPackets().iterator();
        while (var6.hasNext()) {
            Object var7 = ((Packet) var6.next());
            LightningClient.EVENT_HANDLER.post(new PacketReceiveEvent(var7, ((Connection) this)));
        }
        label_1:
        if (var4.isCancelled()) {
            arg2.cancel();
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
    private void disconnect(DisconnectionDetails arg0, CallbackInfo arg1) {
        Object var4 = ((Connection) this);
        if (arg0.reason() != null) {
        } else {
        }
        Object var5 = "unknown";
        if (((LightningClient.PINGBYPASS_CONFIG == null) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) || (LightningClient.PROXY_SERVER == null)) break label_2;
        var tmp0 = (var4 == LightningClient.PROXY_SERVER.getServerConnection() ? 1 : 0);
        int var3 = tmp0;
        int var6 = tmp0;
        if (!(!var3)) {
            LOGGER.warn("[PB] Proxy's connection to the real server dropped: {}", var5);
            Object var7 = ((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class));
            if (var7 != null && var7.isToggled()) {
                var7.setToggled(false);
            }
        } else {
            LOGGER.info("Connection disconnected ({}): {}", var4.getLoggableAddress(false), var5);
        }
        LightningClient.EVENT_HANDLER.post(new ClientDisconnectEvent());
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught$HEAD(ChannelHandlerContext arg0, Throwable arg1, CallbackInfo arg2) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var4 = null;
        Object var5 = null;
        if (var4 != null && var4.isToggled() && var4.shouldSuppress(arg1)) {
            var4.onExceptionCaught(arg1);
            arg2.cancel();
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger("Lightning/Disconnect");
    }

}
