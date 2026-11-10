package lightning.pingbypass.handler;

import java.util.Optional;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbStatusHandler implements ServerStatusPacketListener {
    private static final Logger LOGGER;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private boolean responseSent;

    public PbStatusHandler(ProxyServer arg0, Connection arg1) {
        this.proxyServer = arg0;
        this.connection = arg1;
    }

    public void handleStatusRequest(ServerboundStatusRequestPacket arg0) {
        if (!(!this.responseSent)) {
            this.connection.disconnect(Component.literal("Status already sent"));
            return;
        }
        this.responseSent = true;
        Object var2 = buildDescription();
        Object var3 = new ServerStatus(var2, Optional.empty(), Optional.empty(), Optional.empty(), false);
        this.connection.send(new ClientboundStatusResponsePacket(var3));
    }

    public void handlePingRequest(ServerboundPingRequestPacket arg0) {
        this.connection.send(new ClientboundPongResponsePacket(arg0.getTime()));
        this.connection.disconnect(Component.literal("Ping done"));
    }

    public void onDisconnect(DisconnectionDetails arg0) {
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    private Component buildDescription() {
        Object var1 = Minecraft.getInstance();
        Object var2 = new StringBuilder();
        var2.append("\u00a7dLightning PingBypass\u00a7r\n");
        if (var1.getConnection() != null && var1.player != null && var1.level != null) {
            Object var3 = var1.getConnection().serverBrand();
            var2.append("\u00a7aConnected\u00a7r");
            if (this.proxyServer.getServerConnection() != null) {
                if (var1.getCurrentServer() != null) {
                } else {
                }
                var1.getCurrentServer().ip.append("unknown");
            }
        } else {
            var2.append("\u00a77Idle\u00a7r \u2014 not connected to any server");
        }
        return Component.literal(var2.toString());
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbStatusHandler.class);
    }

}
