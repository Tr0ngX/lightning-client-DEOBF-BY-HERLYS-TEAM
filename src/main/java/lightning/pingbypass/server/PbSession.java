package lightning.pingbypass.server;

import lightning.mixins.accessors.ConnectionAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbSession extends Connection {
    private static final Logger LOGGER;

    public PbSession() {
        super(PacketFlow.SERVERBOUND);
    }

    public void setupInboundProtocol(ProtocolInfo arg0, PacketListener arg1) {
        if (arg0.flow() != getReceiving()) throw new IllegalStateException(("Invalid inbound protocol: " + String.valueOf(arg0.id())));
        Object var3 = ((ConnectionAccessor) this);
        var3.setPacketListener(arg1);
        Object var4 = var3.getChannel();
        Object var5 = () -> {
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
        LOGGER.error("[PbSession] Failed inbound transition to {}", arg1.id(), var2);
    }
};
        if (var4.eventLoop().inEventLoop()) {
            var5.run();
            break label_4;
            try {
            }
            var4.eventLoop().submit(var5).sync();
        } catch (InterruptedException ex3) {
            Object var6 = ex3;
            Thread.currentThread().interrupt();
        }
        label_4:
    }

    public void setupOutboundProtocol(ProtocolInfo arg0) {
        if (arg0.flow() != getSending()) throw new IllegalStateException(("Invalid outbound protocol: " + String.valueOf(arg0.id())));
        Object var2 = ((ConnectionAccessor) this);
        Object var3 = var2.getChannel();
        var2.setSendLoginDisconnect(arg0.id() == ConnectionProtocol.LOGIN);
        Object var4 = () -> {
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
        LOGGER.error("[PbSession] Failed outbound transition to {}", arg1.id(), var2);
    }
};
        if (var3.eventLoop().inEventLoop()) {
            var4.run();
            break label_6;
            try {
            }
            var3.eventLoop().submit(var4).sync();
        } catch (InterruptedException ex5) {
            Object var5 = ex5;
            Thread.currentThread().interrupt();
        }
        label_6:
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbSession.class);
    }

}
