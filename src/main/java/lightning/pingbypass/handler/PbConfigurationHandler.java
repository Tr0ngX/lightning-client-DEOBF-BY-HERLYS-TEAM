package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.util.List;
import lightning.LightningClient;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.GameProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbConfigurationHandler implements ServerConfigurationPacketListener, TickablePacketListener {
    private static final Logger LOGGER;
    private static final GameProtocols.Context NO_INFINITE_MATERIALS;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private boolean knownPacksSent;

    public PbConfigurationHandler(ProxyServer arg0, Connection arg1, GameProfile arg2) {
        this.proxyServer = arg0;
        this.connection = arg1;
        this.profile = arg2;
    }

    public void tick() {
        if (!this.knownPacksSent) {
            this.knownPacksSent = true;
            Object var1 = this.proxyServer.getRegistryCache();
            if (var1.isLoaded()) {
                this.connection.send(new ClientboundSelectKnownPacks(var1.getKnownPacks()));
            } else {
                this.connection.send(new ClientboundSelectKnownPacks(List.of()));
            }
            LOGGER.info("Sent SelectKnownPacks to {}", this.profile.name());
        }
    }

    public void handleSelectKnownPacks(ServerboundSelectKnownPacks arg0) {
        LOGGER.info("Client {} responded with known packs, sending registries", this.profile.name());
        Object var2 = this.proxyServer.getRegistryCache();
        if (!(var2.isLoaded())) break label_1;
        Object var3 = var2.getRegistryPackets().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Packet) var3.next());
            this.connection.send(var4);
        }
        label_1:
        this.connection.send(ClientboundFinishConfigurationPacket.INSTANCE);
        if (var2.isLoaded()) {
        } else {
        }
        "Sent {} registry packets and FINISH_CONFIGURATION to {}".info(var2.getRegistryPackets().size(), 0, this.profile.name());
    }

    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket arg0) {
        LOGGER.info("Client {} acknowledged FINISH_CONFIGURATION, transitioning to PLAY", this.profile.name());
        Object var2 = this.proxyServer.getRegistryCache();
        if (var2.isLoaded()) {
        } else {
        }
        Object var3 = RegistryAccess.EMPTY;
        this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var3)));
        if (LightningClient.PINGBYPASS_CONFIG.hasPassword()) {
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), NO_INFINITE_MATERIALS), new PbPasswordHandler(this.proxyServer, this.connection, this.profile, var3));
            LOGGER.info("Transitioning {} to password verification", this.profile.name());
        } else {
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), NO_INFINITE_MATERIALS), new PbWaitingHandler(this.proxyServer, this.connection, this.profile, var3));
            LOGGER.info("Transitioning {} to waiting state", this.profile.name());
        }
    }

    public void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket arg0) {
    }

    public void onDisconnect(DisconnectionDetails arg0) {
        LOGGER.info("Client {} disconnected during configuration: {}", this.profile.name(), arg0.reason());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void handleClientInformation(ServerboundClientInformationPacket arg0) {
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket arg0) {
    }

    public void handlePong(ServerboundPongPacket arg0) {
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket arg0) {
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket arg0) {
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket arg0) {
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket arg0) {
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbConfigurationHandler.class);
        NO_INFINITE_MATERIALS = () -> false;
    }

}
