package lightning.pingbypass.handler;

import lightning.pingbypass.server.ProxyServer;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbHandshakeHandler implements ServerHandshakePacketListener {
    private static final Logger LOGGER;
    private static final int PROTOCOL_VERSION = 775;
    private final ProxyServer proxyServer;
    private final Connection connection;

    public PbHandshakeHandler(ProxyServer arg0, Connection arg1) {
        this.proxyServer = arg0;
        this.connection = arg1;
    }

    public void handleIntention(ClientIntentionPacket arg0) {
        switch (PbHandshakeHandler.1.$SwitchMap$net$minecraft$network$protocol$handshake$ClientIntent[arg0.intention().ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        handleLogin(arg0);
        break label_3;
        label_1:
        handleStatus();
        break label_3;
        label_2:
        throw new UnsupportedOperationException(("Invalid intention " + String.valueOf(arg0.intention())));
        label_3:
    }

    private void handleLogin(ClientIntentionPacket arg0) {
        this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        if (arg0.protocolVersion() != 775) {
            if (arg0.protocolVersion() < 775) {
                var tmp0 = new Object[1];
                tmp0[0] = "26.1.2";
                Object var2 = Component.translatable("multiplayer.disconnect.outdated_client", tmp0);
            } else {
                var tmp1 = new Object[1];
                tmp1[0] = "26.1.2";
                var2 = Component.translatable("multiplayer.disconnect.incompatible", tmp1);
            }
            this.connection.send(new ClientboundLoginDisconnectPacket(var2));
            this.connection.disconnect(var2);
            LOGGER.info("Rejected connection from {}: wrong protocol version {}", this.connection.getLoggableAddress(false), arg0.protocolVersion());
            break label_4;
        }
        if (hasActivePlayConnection()) {
            var2 = Component.literal("This PingBypass server is already in use!");
            this.connection.send(new ClientboundLoginDisconnectPacket(var2));
            this.connection.disconnect(var2);
            LOGGER.info("Rejected connection from {}: another client is already connected", this.connection.getLoggableAddress(false));
            break label_4;
        }
        this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new PbLoginHandler(this.proxyServer, this.connection));
        LOGGER.info("Client {} starting login", this.connection.getLoggableAddress(false));
        label_4:
    }

    private void handleStatus() {
        this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
        this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new PbStatusHandler(this.proxyServer, this.connection));
        LOGGER.debug("Received STATUS handshake, serving proxy status");
    }

    private boolean hasActivePlayConnection() {
        Object var1 = this.proxyServer.getConnections().iterator();
        label_0:
        if (var1.hasNext()) {
            Object var2 = ((Connection) var1.next());
            if (((var2 == this.connection) || (!(var2.isConnected()))) || (var2.getPacketListener() == null)) break label_0;
            if (var2.getPacketListener().protocol() != ConnectionProtocol.PLAY) {
                break label_0;
            }
            return true;
        }
        return false;
    }

    public void onDisconnect(DisconnectionDetails arg0) {
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbHandshakeHandler.class);
    }


    class 1 {

        static {
            $SwitchMap$net$minecraft$network$protocol$handshake$ClientIntent = new int[ClientIntent.values().length];
            try {
                $SwitchMap$net$minecraft$network$protocol$handshake$ClientIntent[ClientIntent.LOGIN.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$network$protocol$handshake$ClientIntent[ClientIntent.STATUS.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
            }
        }

    }
}
