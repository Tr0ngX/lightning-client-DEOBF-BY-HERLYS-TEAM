package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import java.util.concurrent.ThreadLocalRandom;
import lightning.LightningClient;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.server.LobbyWorldSender;
import lightning.pingbypass.server.ProxyServer;
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

public class PbPasswordHandler implements ServerGamePacketListener, TickablePacketListener {
    private static final Logger LOGGER;
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection connection;
    private final GameProfile profile;
    private final RegistryAccess registryManager;
    private boolean passwordRequestSent;
    private boolean lobbyWorldSent;
    private boolean clientInPlayState;
    private long lastKeepAliveTime;

    public PbPasswordHandler(ProxyServer arg0, Connection arg1, GameProfile arg2, RegistryAccess arg3) {
        this.proxyServer = arg0;
        this.connection = arg1;
        this.profile = arg2;
        this.registryManager = arg3;
        this.lastKeepAliveTime = 0L;
        LOGGER.info("Password handler initialized for {}", arg2.name());
    }

    public void tick() {
        long var1 = System.currentTimeMillis();
        if (!this.lobbyWorldSent) {
            this.lobbyWorldSent = true;
            LobbyWorldSender.sendLobbyWorld(this.connection, this.registryManager);
        }
        if ((var1 - this.lastKeepAliveTime) >= 15000L) {
            this.lastKeepAliveTime = var1;
            this.connection.send(new ClientboundKeepAlivePacket(var1));
        }
        if (!this.passwordRequestSent && !(!this.clientInPlayState)) {
            this.passwordRequestSent = true;
            this.connection.send(new ClientboundCustomPayloadPacket(PbCustomPayload.passwordRequest()));
            LOGGER.info("Sent password request to {}", this.profile.name());
        }
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket arg0) {
        Object var2 = arg0.payload();
        if (!(PbCustomPayload.CHANNEL.equals(var2.type().id()))) return;
        if (!((var2 instanceof PbCustomPayload))) return;
        Object var3 = ((PbCustomPayload) var2);
        Object var4 = var3.toBuf();
        int var5 = var4.readVarInt();
        if (var5 == 2) {
            handlePassword(var4.readUtf());
        } else {
            LOGGER.warn("Unexpected packet ID {} from {} during password verification", var5, this.profile.name());
        }
        var4.release();
        break label_7;
        Object var6 = null;
        var4.release();
        throw var6;
        label_7:
    }

    private void handlePassword(String arg0) {
        Object var2 = LightningClient.PINGBYPASS_CONFIG.getPassword();
        if (var2.equals(arg0)) {
            LOGGER.info("Password accepted for {}", this.profile.name());
            this.connection.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(RegistryAccess.EMPTY), () -> false), new PbWaitingHandler(this.proxyServer, this.connection, this.profile, this.registryManager));
        } else {
            LOGGER.warn("Wrong password from {}", this.profile.name());
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(1L, 11L));
            } catch (InterruptedException ex3) {
                Object var3 = ex3;
                Thread.currentThread().interrupt();
            }
            this.connection.disconnect(Component.literal("Wrong password"));
        }
    }

    public void onDisconnect(DisconnectionDetails arg0) {
        LOGGER.info("Client {} disconnected during password verification", this.profile.name());
    }

    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
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

    public void handleKeepAlive(ServerboundKeepAlivePacket arg0) {
        this.clientInPlayState = true;
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
        LOGGER = LoggerFactory.getLogger(PbPasswordHandler.class);
    }

}
