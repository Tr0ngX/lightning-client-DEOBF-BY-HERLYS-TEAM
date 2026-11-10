package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.security.KeyPair;
import java.util.Random;
import java.util.UUID;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbLoginHandler implements ServerLoginPacketListener {
    private static final Logger LOGGER;
    private static final Random RANDOM;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final KeyPair keyPair;
    private final byte[] nonce;
    private PbLoginHandler.LoginState state;
    private String playerName;
    private GameProfile acceptedProfile;

    public PbLoginHandler(ProxyServer arg0, Connection arg1) {
        this.nonce = new byte[4];
        this.state = PbLoginHandler.LoginState.HELLO;
        this.proxyServer = arg0;
        this.connection = arg1;
        try {
            this.keyPair = Crypt.generateKeyPair();
        } catch (CryptException ex2) {
            Object var3 = ex2;
            throw new IllegalStateException("Failed to generate RSA keypair", var3);
        }
        RANDOM.nextBytes(this.nonce);
    }

    public void handleHello(ServerboundHelloPacket arg0) {
        if (this.state != PbLoginHandler.LoginState.HELLO) throw new IllegalStateException("Unexpected hello packet");
        this.playerName = arg0.name();
        LOGGER.info("Login attempt from {}", this.playerName);
        this.state = PbLoginHandler.LoginState.KEY;
        this.connection.send(new ClientboundHelloPacket("", this.keyPair.getPublic().getEncoded(), this.nonce, false));
    }

    public void handleKey(ServerboundKeyPacket arg0) {
        if (this.state != PbLoginHandler.LoginState.KEY) throw new IllegalStateException("Unexpected key packet");
        Object var3 = this.keyPair.getPrivate();
        if (!(arg0.isChallengeValid(this.nonce, var3))) {
            throw new IllegalStateException("Protocol error: invalid nonce!");
            try {
            }
            Object var2 = arg0.getSecretKey(var3);
        } catch (CryptException ex3) {
            Object var4 = ex3;
            throw new IllegalStateException("Protocol error", var4);
        }
        this.state = PbLoginHandler.LoginState.READY_TO_ACCEPT;
        try {
            var4 = Crypt.getCipher(2, var2);
            Object var5 = Crypt.getCipher(1, var2);
            this.connection.setEncryptionKey(var4, var5);
        } catch (CryptException ex7) {
            var4 = ex7;
            throw new IllegalStateException("Protocol error", var4);
        }
        acceptPlayer();
    }

    private void acceptPlayer() {
        var tmp0 = new GameProfile(UUIDUtil.createOfflinePlayerUUID(this.playerName), this.playerName);
        Object var1 = tmp0;
        this.acceptedProfile = tmp0;
        this.state = PbLoginHandler.LoginState.PROTOCOL_SWITCHING;
        this.connection.send(new ClientboundLoginFinishedPacket(var1, UUID.randomUUID()));
        LOGGER.info("Login success for {} ({}), waiting for LOGIN_ACKNOWLEDGED", var1.name(), var1.id());
    }

    public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket arg0) {
        if (this.state != PbLoginHandler.LoginState.PROTOCOL_SWITCHING) throw new IllegalStateException("Unexpected login acknowledgement packet");
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, new PbConfigurationHandler(this.proxyServer, this.connection, this.acceptedProfile));
        this.state = PbLoginHandler.LoginState.ACCEPTED;
        LOGGER.info("Transitioning {} to configuration phase", this.playerName);
    }

    public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket arg0) {
        disconnect(Component.literal("Unexpected query response"));
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket arg0) {
        disconnect(Component.literal("Unexpected cookie response"));
    }

    public void onDisconnect(DisconnectionDetails arg0) {
        LOGGER.info("Client {} disconnected during login: {}", (this.playerName != null ? this.playerName : "unknown"), arg0.reason());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    private void disconnect(Component arg0) {
        try {
            this.connection.send(new ClientboundLoginDisconnectPacket(arg0));
            this.connection.disconnect(arg0);
        } catch (Exception ex2) {
            Object var2 = ex2;
            LOGGER.error("Error disconnecting client", var2);
        }
    }

    public String getPlayerName() {
        return this.playerName;
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbLoginHandler.class);
        RANDOM = new Random();
    }


    enum LoginState {
        HELLO,
        KEY,
        READY_TO_ACCEPT,
        PROTOCOL_SWITCHING,
        ACCEPTED;


        private LoginState() {
        }

    }
}
