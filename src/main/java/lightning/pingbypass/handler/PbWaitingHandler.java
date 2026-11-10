package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import lightning.mixins.accessors.DisconnectedScreenAccessor;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CErrorPacket;
import lightning.pingbypass.protocol.packets.S2CServerNamePacket;
import lightning.pingbypass.server.LobbyWorldSender;
import lightning.pingbypass.server.ProxyServer;
import lightning.pingbypass.server.S2CForwarder;
import lightning.pingbypass.server.WorldStateReplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbWaitingHandler implements ServerGamePacketListener, TickablePacketListener {
    private static final Logger LOGGER;
    private static final long KEEP_ALIVE_TIMEOUT_MS = 30000L;
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private final RegistryAccess registryManager;
    private long lastKeepAliveTime;
    private long lastKeepAliveSentTime;
    private boolean lobbyWorldSent;
    private String pendingServerIp;
    private int pendingServerPort;
    private volatile boolean connectingToServer;
    private int connectionAttemptTicks;

    public PbWaitingHandler(ProxyServer arg0, Connection arg1, GameProfile arg2, RegistryAccess arg3) {
        this.proxyServer = arg0;
        this.connection = arg1;
        this.profile = arg2;
        this.registryManager = arg3;
        this.lastKeepAliveTime = System.currentTimeMillis();
        this.lastKeepAliveSentTime = 0L;
        LOGGER.info("Waiting handler initialized for {}", arg2.name());
    }

    public void tick() {
        long var1 = System.currentTimeMillis();
        if (!this.lobbyWorldSent) {
            this.lobbyWorldSent = true;
            LobbyWorldSender.sendLobbyWorld(this.connection, this.registryManager);
        }
        if ((var1 - this.lastKeepAliveTime) <= 30000L) break label_1;
        LOGGER.warn("Client {} timed out (no keep-alive for 30s)", this.profile.name());
        this.connection.disconnect(Component.literal("Timed out"));
        label_1:
        if ((var1 - this.lastKeepAliveSentTime) >= 15000L) {
            this.lastKeepAliveSentTime = var1;
            this.connection.send(new ClientboundKeepAlivePacket(var1));
        }
        if (!this.connectingToServer) break label_10;
        this.connectionAttemptTicks = (this.connectionAttemptTicks + 1);
        Object var3 = Minecraft.getInstance();
        if (this.connectionAttemptTicks == 20) {
            sendChat("\u00a77[PingBypass] Resolving address...");
            break label_5;
        }
        if (this.connectionAttemptTicks == 40 && var3.getConnection() == null) {
            sendChat("\u00a77[PingBypass] Establishing connection...");
            break label_5;
        }
        if ((this.connectionAttemptTicks != 60) || (var3.getConnection() != null)) break label_5;
        sendChat("\u00a77[PingBypass] Encrypting...");
        label_5:
        if (var3.getConnection() != null && var3.player == null) {
            if ((this.connectionAttemptTicks % 40) != 0) break label_10;
            sendChat("\u00a77[PingBypass] Configuring...");
            break label_10;
        }
        if (var3.getConnection() != null && var3.player != null && var3.level != null) {
            if (var3.getConnection().registryAccess() != null) {
                this.connectingToServer = false;
                sendChat("\u00a7a[PingBypass] Connected! Loading world...");
                onServerConnectionEstablished(var3, this.pendingServerIp, this.pendingServerPort);
                break label_10;
            }
            if ((this.connectionAttemptTicks % 40) != 0) break label_10;
            sendChat("\u00a77[PingBypass] Loading terrain...");
            break label_10;
        }
        Object var4 = var3.gui.screen();
        if ((var4 instanceof DisconnectedScreen)) {
            Object var5 = ((DisconnectedScreen) var4);
            this.connectingToServer = false;
            Object var6 = ((DisconnectedScreenAccessor) var5).night$getDetails().reason().getString();
            var tmp0 = new Object[4];
            tmp0[0] = this.pendingServerIp;
            tmp0[1] = this.pendingServerPort;
            tmp0[2] = this.profile.name();
            tmp0[3] = var6;
            LOGGER.warn("Connection to {}:{} failed for {}: {}", tmp0);
            sendError(("Failed to connect to " + this.pendingServerIp + ":" + this.pendingServerPort + ": " + var6));
            sendChat(("\u00a7c[PingBypass] Failed to connect to " + this.pendingServerIp + ":" + this.pendingServerPort + " (" + var6 + ")"));
            break label_10;
        }
        if (this.connectionAttemptTicks <= 600) break label_10;
        this.connectingToServer = false;
        var tmp1 = new Object[3];
        tmp1[0] = this.pendingServerIp;
        tmp1[1] = this.pendingServerPort;
        tmp1[2] = this.profile.name();
        LOGGER.warn("Connection to {}:{} timed out for {}", tmp1);
        sendError(("Connection to " + this.pendingServerIp + ":" + this.pendingServerPort + " timed out"));
        sendChat("\u00a7c[PingBypass] Connection timed out");
        label_10:
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket arg0) {
        Object var2 = arg0.payload();
        if (!(PbCustomPayload.CHANNEL.equals(var2.type().id()))) return;
        if (!((var2 instanceof PbCustomPayload))) return;
        Object var3 = ((PbCustomPayload) var2);
        Object var4 = var3.toBuf();
        try {
            int var5 = var4.readVarInt();
            if (!var5) {
                Object var6 = var4.readUtf();
                int var7 = var4.readVarInt();
                handleJoinRequest(var6, var7);
            } else {
                LOGGER.warn("Unexpected packet ID {} from {} during waiting state", var5, this.profile.name());
            }
            var4.release();
        } catch (Exception ex5) {
            var5 = ex5;
            LOGGER.warn("Malformed custom payload from {}", this.profile.name(), var5);
            var4.release();
            break label_9;
            Object var8 = null;
            var4.release();
            throw var8;
        }
        label_9:
    }

    void handleJoinRequest(String arg0, int arg1) {
        var tmp0 = new Object[3];
        tmp0[0] = this.profile.name();
        tmp0[1] = arg0;
        tmp0[2] = arg1;
        LOGGER.info("Client {} requested join to {}:{}", tmp0);
        Object var3 = Minecraft.getInstance();
        if (var3.getConnection() != null && var3.player != null && var3.level != null && var3.getConnection().registryAccess() != null) {
            LOGGER.info("Proxy already connected to a server, replaying world state for {}", this.profile.name());
            sendChat("\u00a7a[PingBypass] Proxy already connected! Resuming session...");
            onServerConnectionEstablished(var3, arg0, arg1);
            return;
        }
        sendChat(("\u00a7d[PingBypass] \u00a7fConnecting to \u00a7a" + arg0 + ":" + arg1 + "\u00a7f..."));
        Object var4 = (arg0 + ":" + arg1);
        Object var5 = ServerAddress.parseString(var4);
        Object var6 = new ServerData("PingBypass Target", var4, ServerData.Type.OTHER);
        this.pendingServerIp = arg0;
        this.pendingServerPort = arg1;
        this.connectingToServer = true;
        this.connectionAttemptTicks = 0;
        var3.execute(() -> {
    try {
        LOGGER.info("Initiating connection to {} for {}", arg0, this.profile.name());
        ConnectScreen.startConnecting(new TitleScreen(), arg1, arg2, arg3, false, null);
    } catch (Exception ex2) {
        Object var5 = ex2;
        var tmp0 = new Object[3];
        tmp0[0] = arg0;
        tmp0[1] = this.profile.name();
        tmp0[2] = var5;
        LOGGER.error("Failed to initiate connection to {} for {}", tmp0);
        sendError(("Failed to connect to " + arg0 + ": " + var5.getMessage()));
        this.connectingToServer = false;
    }
});
    }

    private void onServerConnectionEstablished(Minecraft arg0, String arg1, int arg2) {
        var tmp0 = new Object[3];
        tmp0[0] = arg1;
        tmp0[1] = arg2;
        tmp0[2] = this.profile.name();
        LOGGER.info("Server connection established to {}:{} for {}", tmp0);
        try {
            Object var4 = arg0.getConnection().getConnection();
            this.proxyServer.setServerConnection(var4);
            Object var5 = arg0.level.registryAccess();
            this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var5)));
            LOGGER.info("Re-transitioned outbound encoder to real server registry for {}", this.profile.name());
            Object var6 = var5;
            Object var7 = (arg1 + ":" + arg2);
            this.connection.send(new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CServerNamePacket(var7))));
            Object var8 = new S2CForwarder(this.connection);
            var8.start();
            int var9 = WorldStateReplay.replay(this.connection, var5);
            Object var10 = new PbPlayHandler(this.proxyServer, this.connection, this.profile, var8, 0);
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var6), () -> false), var10);
            var tmp1 = new Object[3];
            tmp1[0] = this.profile.name();
            tmp1[1] = arg1;
            tmp1[2] = arg2;
            LOGGER.info("Proxy fully active for {} on {}:{} (awaiting teleport confirm)", tmp1);
        } catch (Exception ex2) {
            var4 = ex2;
            LOGGER.error("Failed to set up forwarding for {}", this.profile.name(), var4);
            sendError(("Failed to set up forwarding: " + var4.getMessage()));
        }
    }

    private void sendError(String arg0) {
        if (this.connection.isConnected()) {
            this.connection.send(new ClientboundCustomPayloadPacket(PbCustomPayload.fromPacket(new S2CErrorPacket(arg0))));
        }
    }

    private void sendChat(String arg0) {
        if (this.connection.isConnected()) {
            this.connection.send(new ClientboundSystemChatPacket(Component.literal(arg0), false));
        }
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket arg0) {
        this.lastKeepAliveTime = System.currentTimeMillis();
    }

    public void onDisconnect(DisconnectionDetails arg0) {
        LOGGER.info("Client {} disconnected during waiting state", this.profile.name());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void handleAnimate(ServerboundSwingPacket arg0) {
    }

    public void handleChat(ServerboundChatPacket arg0) {
    }

    public void handleChatCommand(ServerboundChatCommandPacket arg0) {
    }

    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket arg0) {
    }

    public void handleChatAck(ServerboundChatAckPacket arg0) {
    }

    public void handleClientCommand(ServerboundClientCommandPacket arg0) {
    }

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket arg0) {
    }

    public void handleContainerClick(ServerboundContainerClickPacket arg0) {
    }

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket arg0) {
    }

    public void handleContainerClose(ServerboundContainerClosePacket arg0) {
    }

    public void handleAttack(ServerboundAttackPacket arg0) {
    }

    public void handleInteract(ServerboundInteractPacket arg0) {
    }

    public void handleSpectatorAction(ServerboundSpectatorActionPacket arg0) {
    }

    public void handleMovePlayer(ServerboundMovePlayerPacket arg0) {
    }

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket arg0) {
    }

    public void handlePlayerAction(ServerboundPlayerActionPacket arg0) {
    }

    public void handlePlayerCommand(ServerboundPlayerCommandPacket arg0) {
    }

    public void handlePlayerInput(ServerboundPlayerInputPacket arg0) {
    }

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket arg0) {
    }

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket arg0) {
    }

    public void handleSignUpdate(ServerboundSignUpdatePacket arg0) {
    }

    public void handleUseItemOn(ServerboundUseItemOnPacket arg0) {
    }

    public void handleUseItem(ServerboundUseItemPacket arg0) {
    }

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket arg0) {
    }

    public void handlePaddleBoat(ServerboundPaddleBoatPacket arg0) {
    }

    public void handleMoveVehicle(ServerboundMoveVehiclePacket arg0) {
    }

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket arg0) {
    }

    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket arg0) {
    }

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket arg0) {
    }

    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket arg0) {
    }

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket arg0) {
    }

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket arg0) {
    }

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket arg0) {
    }

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket arg0) {
    }

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket arg0) {
    }

    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket arg0) {
    }

    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket arg0) {
    }

    public void handleRenameItem(ServerboundRenameItemPacket arg0) {
    }

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket arg0) {
    }

    public void handleSetGameRule(ServerboundSetGameRulePacket arg0) {
    }

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket arg0) {
    }

    public void handleSetTestBlock(ServerboundSetTestBlockPacket arg0) {
    }

    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket arg0) {
    }

    public void handleSelectTrade(ServerboundSelectTradePacket arg0) {
    }

    public void handleEditBook(ServerboundEditBookPacket arg0) {
    }

    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket arg0) {
    }

    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket arg0) {
    }

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket arg0) {
    }

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket arg0) {
    }

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket arg0) {
    }

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket arg0) {
    }

    public void handleChangeGameMode(ServerboundChangeGameModePacket arg0) {
    }

    public void handleLockDifficulty(ServerboundLockDifficultyPacket arg0) {
    }

    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket arg0) {
    }

    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket arg0) {
    }

    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket arg0) {
    }

    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket arg0) {
    }

    public void handleClientTickEnd(ServerboundClientTickEndPacket arg0) {
    }

    public void handleClientInformation(ServerboundClientInformationPacket arg0) {
    }

    public void handlePong(ServerboundPongPacket arg0) {
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket arg0) {
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket arg0) {
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket arg0) {
    }

    public void handlePingRequest(ServerboundPingRequestPacket arg0) {
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbWaitingHandler.class);
    }

}
