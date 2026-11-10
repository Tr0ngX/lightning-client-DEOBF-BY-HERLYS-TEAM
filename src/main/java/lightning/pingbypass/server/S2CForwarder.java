package lightning.pingbypass.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.ProtocolTransitionTracker;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2CForwarder {
    private static final Logger LOGGER;
    private final Connection clientConnection;
    private final List queuedPackets;
    private final AtomicBoolean locked;
    private volatile boolean active;

    public S2CForwarder(Connection arg0) {
        this.queuedPackets = new CopyOnWriteArrayList();
        this.locked = new AtomicBoolean(false);
        this.clientConnection = arg0;
        this.active = false;
    }

    public void start() {
        this.active = true;
        PingBypassFlags.proxyForwardingActive = true;
        LightningClient.EVENT_HANDLER.subscribe(this);
        LOGGER.info("S2C forwarder started");
    }

    public void stop() {
        this.active = false;
        PingBypassFlags.proxyForwardingActive = false;
        LightningClient.EVENT_HANDLER.unsubscribe(this);
        LOGGER.info("S2C forwarder stopped");
    }

    public void lock() {
        Object var1 = this.locked;
        Object var2 = var1;
        // synchronized (var1)
        this.locked.set(true);
        break label_4;
        Object var3 = null;
        throw var3;
        label_4:
    }

    public void unlockAndFlush() {
        Object var1 = this.locked;
        Object var2 = var1;
        // synchronized (var1)
        Object var3 = this.queuedPackets.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Packet) var3.next());
            sendToClient(var4);
        }
        this.queuedPackets.clear();
        this.locked.set(false);
        break label_6;
        Object var5 = null;
        throw var5;
        label_6:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((!this.active) || (!(this.clientConnection.isConnected()))) {
            return;
        }
        if (LightningClient.PROXY_SERVER == null) break label_2;
        break label_3;
        label_2:
        label_3:
        Object var3 = null;
        Object var4 = null;
        if ((var3 == null) || (arg0.getConnection() != var3)) {
            return;
        }
        Object var5 = arg0.getPacket();
        if (((var5 instanceof ClientboundKeepAlivePacket)) || ((var5 instanceof ClientboundPingPacket))) {
            return;
        }
        if (!((var5 instanceof ClientboundTransferPacket))) {
            if ((var5 instanceof ClientboundStartConfigurationPacket)) {
            }
            LOGGER.info("Suppressed {} from being forwarded to client (proxy handles it internally)", var5.getClass().getSimpleName());
            ProtocolTransitionTracker.mark();
            return;
        }
        if (LightningClient.PROXY_SERVER != null && LightningClient.PROXY_SERVER.getServerConnection() != null) {
        } else {
        }
        Object var2 = null;
        Object var6 = null;
        if (var2 != null && var2.protocol() != ConnectionProtocol.PLAY) {
            LOGGER.info("Suppressed {} while proxy's server connection is mid-reconfigure (protocol={})", var5.getClass().getSimpleName(), var2.protocol());
            ProtocolTransitionTracker.mark();
            return;
        }
        if ((var5 instanceof ClientboundBundlePacket)) return;
        Object var7 = this.locked;
        Object var8 = var7;
        // synchronized (var7)
        if (this.locked.get()) {
            this.queuedPackets.add(var5);
        } else {
            sendToClient(var5);
        }
        break label_20;
        Object var9 = LightningClient.PROXY_SERVER.getServerConnection().getPacketListener();
        throw var9;
        label_20:
    }

    private void sendToClient(Packet arg0) {
        if (this.clientConnection.isConnected()) {
            this.clientConnection.send(arg0);
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger(S2CForwarder.class);
    }

}
