package lightning.mixins;

import lightning.mixins.accessors.ConnectionAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionSafeReconfigureMixin {
    private static final Logger LOGGER;

    @Inject(method = "setupOutboundProtocol", at = @At("HEAD"), cancellable = true)
    private void setupOutboundProtocol$safe(ProtocolInfo arg0, CallbackInfo arg1) {
        Object var3 = ((Connection) this);
        if (arg0.flow() != var3.getSending()) throw new IllegalStateException(("Invalid outbound protocol: " + String.valueOf(arg0.id())));
        Object var4 = ((ConnectionAccessor) this);
        Object var5 = var4.getChannel();
        var4.setSendLoginDisconnect(arg0.id() == ConnectionProtocol.LOGIN);
        Object var6 = () -> {
    try {
        Object var2 = arg0.pipeline();
        if (var2.get("fabric:splitter") != null) {
            var2.remove("fabric:splitter");
        }
        Object var3 = new PacketEncoder(arg1);
        if (var2.get("encoder") != null) {
            var2.replace("encoder", "encoder", var3);
            break label_4;
        }
        if (var2.get("outbound_config") != null) {
            var2.replace("outbound_config", "encoder", var3);
            break label_4;
        }
        var2.addAfter("prepender", "encoder", var3);
        label_4:
        Object var4 = arg1.bundlerInfo();
        if (var4 == null) break label_6;
        Object var5 = new PacketBundleUnpacker(var4);
        if (var2.get("unbundler") != null) {
            var2.replace("unbundler", "unbundler", var5);
            break label_6;
        }
        var2.addAfter("encoder", "unbundler", var5);
        label_6:
    } catch (Exception ex7) {
        var2 = ex7;
        LOGGER.error("[PB] Failed safe outbound transition to {}", arg1.id(), var2);
    }
};
        if (var5.eventLoop().inEventLoop()) {
            var6.run();
            break label_6;
            try {
            }
            var5.eventLoop().submit(var6).sync();
        } catch (InterruptedException ex5) {
            Object var7 = ex5;
            Thread.currentThread().interrupt();
        }
        label_6:
        arg1.cancel();
    }

    @Inject(method = "setupInboundProtocol", at = @At("HEAD"), cancellable = true)
    private void setupInboundProtocol$safe(ProtocolInfo arg0, PacketListener arg1, CallbackInfo arg2) {
        Object var4 = ((Connection) this);
        if (arg0.flow() != var4.getReceiving()) throw new IllegalStateException(("Invalid inbound protocol: " + String.valueOf(arg0.id())));
        Object var5 = ((ConnectionAccessor) this);
        var5.setPacketListener(arg1);
        Object var6 = var5.getChannel();
        Object var7 = () -> {
    try {
        Object var2 = arg0.pipeline();
        if (var2.get("fabric:merger") != null) {
            var2.remove("fabric:merger");
        }
        Object var3 = new PacketDecoder(arg1);
        if (var2.get("decoder") != null) {
            var2.replace("decoder", "decoder", var3);
            break label_3;
        }
        if (var2.get("inbound_config") == null) break label_3;
        var2.replace("inbound_config", "decoder", var3);
        label_3:
        arg0.config().setAutoRead(true);
        Object var4 = arg1.bundlerInfo();
        if (var4 == null) break label_5;
        Object var5 = new PacketBundlePacker(var4);
        if (var2.get("bundler") != null) {
            var2.replace("bundler", "bundler", var5);
            break label_5;
        }
        var2.addAfter("decoder", "bundler", var5);
        label_5:
    } catch (Exception ex6) {
        var2 = ex6;
        LOGGER.error("[PB] Failed safe inbound transition to {}", arg1.id(), var2);
    }
};
        if (var6.eventLoop().inEventLoop()) {
            var7.run();
            break label_4;
            try {
            }
            var6.eventLoop().submit(var7).sync();
        } catch (InterruptedException ex3) {
            Object var8 = ex3;
            Thread.currentThread().interrupt();
        }
        label_4:
        arg2.cancel();
    }

    static {
        LOGGER = LoggerFactory.getLogger("Lightning/SafeReconfigure");
    }

}
