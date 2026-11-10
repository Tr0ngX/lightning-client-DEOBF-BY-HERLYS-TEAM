package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.awt.Color;
import lightning.LightningClient;
import lightning.events.impl.AttackBlockEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.modules.SyncModule;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SFriendSyncPacket;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.C2SOpenInventoryPacket;
import lightning.pingbypass.protocol.packets.C2SSettingChangePacket;
import lightning.pingbypass.server.ProxyServer;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.pingbypass.server.S2CForwarder;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.HashedStack;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
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
import net.minecraft.world.InteractionHand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbPlayHandler implements ServerGamePacketListener, TickablePacketListener {
    private static final Logger LOGGER;
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection clientConnection;
    private final GameProfile profile;
    private final S2CForwarder s2cForwarder;
    private boolean wasOnGround;
    private long lastKeepAliveSent;

    public PbPlayHandler(ProxyServer arg0, Connection arg1, GameProfile arg2, S2CForwarder arg3, int arg4) {
        this.wasOnGround = true;
        this.proxyServer = arg0;
        this.clientConnection = arg1;
        this.profile = arg2;
        this.s2cForwarder = arg3;
        LOGGER.info("Play handler initialized for {} \u2014 dumb pipe mode", arg2.name());
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public void tick() {
        long var1 = System.currentTimeMillis();
        if ((var1 - this.lastKeepAliveSent) >= 15000L) {
            this.lastKeepAliveSent = var1;
            this.clientConnection.send(new ClientboundKeepAlivePacket(var1));
        }
    }

    public void onDisconnect(DisconnectionDetails arg0) {
        LOGGER.info("Client {} disconnected: {}", this.profile.name(), arg0.reason());
        this.s2cForwarder.stop();
        Object var2 = Minecraft.getInstance();
        var2.execute(() -> {
    if (arg0.level != null) {
        arg0.level.disconnect(Component.literal("Client disconnected"));
    }
    if (arg0.getConnection() != null) {
        arg0.getConnection().getConnection().disconnect(Component.literal("Client disconnected"));
    }
});
    }

    public boolean isAcceptingMessages() {
        return this.clientConnection.isConnected();
    }

    private void syncSlotForInteract() {
        InventoryUtils.flushPendingRestores();
        Object var2 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (((var2 == null) || (!(var2.isToggled()))) || (!(var2.isRunningOnProxy()))) break label_4;
        Object var1 = Minecraft.getInstance();
        if (var1.player == null) break label_4;
        Object var3 = var2.interactSyncLock;
        Object var4 = var3;
        // synchronized (var3)
        int var5 = var1.player.getInventory().getSelectedSlot();
        forward(new ServerboundSetCarriedItemPacket(var5));
        var2.setInteractPaused(true);
        return;
        Object var6 = null;
        throw var6;
        label_4:
        var1 = Minecraft.getInstance();
        if (var1.player == null) return;
        var3 = var1.player.getInventory().getSelectedSlot();
        if (var3 != LightningClient.POSITION_MANAGER.getServerSlot()) {
            forward(new ServerboundSetCarriedItemPacket(var3));
        }
    }

    private void forward(Packet arg0) {
        Object var2 = this.proxyServer.getServerConnection();
        if (var2 != null && var2.isConnected()) {
            ProxyServerTickListener.allowSend(() -> arg0.send(arg1));
        }
    }

    public void handleMovePlayer(ServerboundMovePlayerPacket arg0) {
        Minecraft.getInstance().execute(() -> handleMovePlayer0(arg0));
    }

    private void handleMovePlayer0(ServerboundMovePlayerPacket arg0) {
        Object var2 = Minecraft.getInstance();
        if (var2.player != null) {
            double var3 = var2.player.getY();
            if (arg0.hasPosition()) {
                var2.player.setPos(arg0.getX(var2.player.getX()), arg0.getY(var2.player.getY()), arg0.getZ(var2.player.getZ()));
                double var5 = var2.player.getY();
                if (var5 < var3 && !(arg0.isOnGround())) {
                    var2.player.fallDistance = (var2.player.fallDistance + ((double) ((float) (var3 - var5))));
                    break label_1;
                }
                if (!(arg0.isOnGround())) break label_1;
                var2.player.fallDistance = 0.0;
                label_1:
                if (var5 > var3 && !(arg0.isOnGround()) && !(!this.wasOnGround)) {
                    LightningClient.EVENT_HANDLER.post(new PlayerJumpEvent());
                }
                this.wasOnGround = arg0.isOnGround();
            }
            if (arg0.hasRotation()) {
                var2.player.setYRot(arg0.getYRot(var2.player.getYRot()));
                var2.player.setXRot(arg0.getXRot(var2.player.getXRot()));
            }
            var2.player.setOnGround(arg0.isOnGround());
            var2.player.horizontalCollision = arg0.horizontalCollision();
            ProxyServerTickListener.allowSend(() -> ((ClientPlayerEntityAccessor) arg0.player).invokeSendMovementPackets());
        }
        PingBypassFlags.clientOnGround = arg0.isOnGround();
        PingBypassFlags.clientHorizontalCollision = arg0.horizontalCollision();
    }

    public void handleUseItemOn(ServerboundUseItemOnPacket arg0) {
        syncSlotForInteract();
        mirrorStartUsingItem(arg0.getHand());
        forward(arg0);
    }

    public void handleUseItem(ServerboundUseItemPacket arg0) {
        syncSlotForInteract();
        mirrorStartUsingItem(arg0.getHand());
        forward(arg0);
    }

    private void mirrorStartUsingItem(InteractionHand arg0) {
        Object var2 = Minecraft.getInstance();
        if (var2.player == null) return;
        Object var3 = var2.player.getItemInHand(arg0);
        if (var3.getUseDuration(var2.player) > 0) {
            var2.player.startUsingItem(arg0);
        }
    }

    public void handleInteract(ServerboundInteractPacket arg0) {
        forward(arg0);
    }

    public void handlePlayerAction(ServerboundPlayerActionPacket arg0) {
        if (arg0.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
            Object var4 = Minecraft.getInstance();
            if (var4.player != null) {
                var4.player.stopUsingItem();
            }
            var tmp0 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
            Object var3 = tmp0;
            if (tmp0 != null && var3.isInteractPaused()) {
                var3.setInteractPaused(false);
            }
            forward(arg0);
            return;
        }
        if (arg0.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) break label_3;
        var tmp1 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        Object var2 = tmp1;
        if ((tmp1 == null) || (!(var2.isToggled()))) break label_3;
        var3 = Minecraft.getInstance();
        if (var3.player == null) break label_3;
        var3.execute(() -> {
    Object var2 = new AttackBlockEvent(arg0.getPos(), arg0.getDirection());
    LightningClient.EVENT_HANDLER.post(var2);
    if (!(var2.isCancelled())) {
        forward(arg0);
    }
});
        return;
        label_3:
        forward(arg0);
    }

    public void handleAnimate(ServerboundSwingPacket arg0) {
        forward(arg0);
    }

    public void handleContainerClick(ServerboundContainerClickPacket arg0) {
        Object var2 = Minecraft.getInstance();
        if (var2.player != null) {
            var2.execute(() -> {
    try {
        if (arg0.containerId() == arg1.player.containerMenu.containerId) {
        } else {
        }
        Object var3 = arg1.player.inventoryMenu;
        var3.clicked(arg0.slotNum(), arg0.buttonNum(), arg0.containerInput(), arg1.player);
        Object var4 = new ServerboundContainerClickPacket(arg0.containerId(), var3.getStateId(), arg0.slotNum(), arg0.buttonNum(), arg0.containerInput(), Int2ObjectMaps.emptyMap(), HashedStack.EMPTY);
        forward(var4);
    } catch (Exception ex4) {
        var3 = ex4;
    }
});
        }
    }

    public void handleContainerClose(ServerboundContainerClosePacket arg0) {
        forward(arg0);
    }

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket arg0) {
        Object var3 = Minecraft.getInstance();
        Object var4 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (((var4 == null) || (!(var4.isToggled()))) || (!(var4.isRunningOnProxy()))) break label_1;
        if (var4.getPrimary() == null) {
            if (var4.getSecondary() == null) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        int var2 = 0;
        int var5 = 0;
        if (var3.player != null) {
            var3.player.getInventory().setSelectedSlot(arg0.getSlot());
        }
        if ((var2) || (InventoryUtils.hasActiveSilentSwitch())) {
            return;
        }
        forward(arg0);
    }

    public void handleClientCommand(ServerboundClientCommandPacket arg0) {
        forward(arg0);
    }

    public void handleChat(ServerboundChatPacket arg0) {
        forward(arg0);
    }

    public void handleChatCommand(ServerboundChatCommandPacket arg0) {
        forward(arg0);
    }

    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket arg0) {
        forward(arg0);
    }

    public void handleChatAck(ServerboundChatAckPacket arg0) {
        forward(arg0);
    }

    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket arg0) {
        forward(arg0);
    }

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket arg0) {
        forward(arg0);
    }

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket arg0) {
        forward(arg0);
    }

    public void handleSpectatorAction(ServerboundSpectatorActionPacket arg0) {
        forward(arg0);
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket arg0) {
        forward(arg0);
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket arg0) {
        forward(arg0);
    }

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket arg0) {
        forward(arg0);
    }

    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket arg0) {
        forward(arg0);
    }

    public void handleClientTickEnd(ServerboundClientTickEndPacket arg0) {
        forward(arg0);
    }

    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket arg0) {
        forward(arg0);
    }

    public void handleClientInformation(ServerboundClientInformationPacket arg0) {
        forward(arg0);
    }

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket arg0) {
        forward(arg0);
    }

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket arg0) {
        forward(arg0);
    }

    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket arg0) {
        forward(arg0);
    }

    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket arg0) {
        forward(arg0);
    }

    public void handleEditBook(ServerboundEditBookPacket arg0) {
        forward(arg0);
    }

    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket arg0) {
        forward(arg0);
    }

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket arg0) {
        forward(arg0);
    }

    public void handleLockDifficulty(ServerboundLockDifficultyPacket arg0) {
        forward(arg0);
    }

    public void handleMoveVehicle(ServerboundMoveVehiclePacket arg0) {
        forward(arg0);
    }

    public void handlePaddleBoat(ServerboundPaddleBoatPacket arg0) {
        forward(arg0);
    }

    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket arg0) {
        forward(arg0);
    }

    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket arg0) {
        forward(arg0);
    }

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket arg0) {
        forward(arg0);
    }

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket arg0) {
        forward(arg0);
    }

    public void handlePlayerCommand(ServerboundPlayerCommandPacket arg0) {
        Object var2 = Minecraft.getInstance();
        if (var2.player == null) break label_2;
        switch (PbPlayHandler.1.$SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action[arg0.getAction().ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        var2.player.setSprinting(true);
        break label_2;
        label_1:
        var2.player.setSprinting(false);
        label_2:
        forward(arg0);
    }

    public void handlePlayerInput(ServerboundPlayerInputPacket arg0) {
        Object var2 = Minecraft.getInstance();
        if (var2.player != null) {
            var2.player.setShiftKeyDown(arg0.input().shift());
        }
        forward(arg0);
    }

    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket arg0) {
        forward(arg0);
    }

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket arg0) {
        forward(arg0);
    }

    public void handleRenameItem(ServerboundRenameItemPacket arg0) {
        forward(arg0);
    }

    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket arg0) {
        forward(arg0);
    }

    public void handleSelectTrade(ServerboundSelectTradePacket arg0) {
        forward(arg0);
    }

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket arg0) {
        forward(arg0);
    }

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket arg0) {
        forward(arg0);
    }

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket arg0) {
        forward(arg0);
    }

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket arg0) {
        forward(arg0);
    }

    public void handleSetGameRule(ServerboundSetGameRulePacket arg0) {
        forward(arg0);
    }

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket arg0) {
        forward(arg0);
    }

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket arg0) {
        forward(arg0);
    }

    public void handleSetTestBlock(ServerboundSetTestBlockPacket arg0) {
        forward(arg0);
    }

    public void handleSignUpdate(ServerboundSignUpdatePacket arg0) {
        forward(arg0);
    }

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket arg0) {
        forward(arg0);
    }

    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket arg0) {
        forward(arg0);
    }

    public void handleChangeGameMode(ServerboundChangeGameModePacket arg0) {
        forward(arg0);
    }

    public void handleAttack(ServerboundAttackPacket arg0) {
        forward(arg0);
    }

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket arg0) {
        forward(arg0);
    }

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket arg0) {
        forward(arg0);
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket arg0) {
        forward(arg0);
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket arg0) {
    }

    public void handlePong(ServerboundPongPacket arg0) {
        forward(arg0);
    }

    public void handlePingRequest(ServerboundPingRequestPacket arg0) {
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket arg0) {
        Object var2 = arg0.payload();
        if ((var2 instanceof PbCustomPayload)) {
            Object var3 = ((PbCustomPayload) var2);
            handlePbPayload(var3);
            return;
        }
        forward(arg0);
    }

    private void handlePbPayload(PbCustomPayload arg0) {
        Object var2 = arg0.toBuf();
        try {
            int var3 = var2.readVarInt();
            switch (var3) {
                case 3 -> { break label_1; }
                case 4 -> { break label_7; }
                case 14 -> { break label_13; }
                case 15 -> { break label_11; }
                default -> { break label_15; }
            }
            label_1:
            Object var4 = new C2SModuleTogglePacket(var2);
            Object var5 = LightningClient.PB_MODULE_MANAGER.getModule(var4.getModuleName());
            if (var5 != null) {
                Minecraft.getInstance().execute(() -> {
    SyncModule.applyToggle(arg0, arg1.isEnabled());
    LOGGER.info("[PB] PbModule {} toggled to {}", arg1.getModuleName(), arg1.isEnabled());
});
                var2.release();
                return;
                try {
                }
                Object var6 = LightningClient.MODULE_MANAGER.getModule(var4.getModuleName());
                if (var6 == null) {
                    var2.release();
                    return;
                    try {
                    }
                    Minecraft.getInstance().execute(() -> {
    arg0.setToggled(arg1.isEnabled(), false);
    LOGGER.info("[PB] Module {} toggled to {}", arg1.getModuleName(), arg1.isEnabled());
});
                    var2.release();
                    return;
                    try {
                        label_7:
                        var4 = new C2SSettingChangePacket(var2);
                        var5 = LightningClient.PB_MODULE_MANAGER.getModule(var4.getModuleName());
                        if (var5 != null) {
                            Minecraft.getInstance().execute(() -> SyncModule.applySetting(arg0, arg1.getSettingName(), arg1.getValue()));
                            var2.release();
                            return;
                            try {
                            }
                            handleSettingChange(var4);
                            var2.release();
                            return;
                            try {
                                label_11:
                                var4 = new C2SOpenInventoryPacket(var2);
                                Minecraft.getInstance().execute(() -> {
    Object var1 = Minecraft.getInstance();
    if (var1.player == null) return;
    if (arg0.isOpen()) {
        var1.gui.setScreen(new InventoryScreen(var1.player));
        break label_2;
    }
    Object var3 = var1.gui.screen();
    if (!((var3 instanceof InventoryScreen))) break label_2;
    var tmp0 = ((InventoryScreen) var3);
    Object var2 = tmp0;
    if (tmp0.getMenu() != var1.player.inventoryMenu) break label_2;
    var1.gui.setScreen(null);
    label_2:
});
                                var2.release();
                                return;
                                try {
                                    label_13:
                                    var4 = new C2SFriendSyncPacket(var2);
                                    Minecraft.getInstance().execute(() -> {
    LightningClient.FRIEND_MANAGER.clear();
    Object var1 = arg0.getFriends().iterator();
    while (var1.hasNext()) {
        Object var2 = ((String) var1.next());
        LightningClient.FRIEND_MANAGER.add(var2);
    }
    LOGGER.info("[PB] Synced {} friend(s) from client", arg0.getFriends().size());
});
                                    var2.release();
                                    return;
                                    try {
                                        label_15:
                                        LOGGER.debug("[PB] Unknown packet ID: {}", var3);
                                        var2.release();
                                        return;
                                    } catch (Exception ex17) {
                                        var3 = ex17;
                                        LOGGER.warn("[PB] Failed to handle payload", var3);
                                        var2.release();
                                        return;
                                    }
                                    Object var7 = null;
                                    var2.release();
                                    throw var7;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleSettingChange(C2SSettingChangePacket arg0) {
        Object var2 = LightningClient.MODULE_MANAGER.getModule(arg0.getModuleName());
        if (var2 == null) return;
        Object var3 = var2.getSetting(arg0.getSettingName());
        if (var3 == null) return;
        Minecraft.getInstance().execute(() -> {
    try {
        Object var2 = arg0.getValue();
        if (!((arg1 instanceof BooleanSetting))) break label_1;
        Object var3 = ((BooleanSetting) arg1);
        var3.setValue(Boolean.parseBoolean(var2));
        break label_11;
        label_1:
        if ((arg1 instanceof NumberSetting)) {
            var3 = ((NumberSetting) arg1);
            switch (PbPlayHandler.1.$SwitchMap$night$settings$impl$NumberSetting$Type[var3.getType().ordinal()]) {
                case 1 -> { break label_2; }
                case 2 -> { break label_3; }
                case 3 -> { break label_4; }
                case 4 -> { break label_5; }
                default -> { break label_6; }
            }
            label_2:
            var3.setValue(Integer.parseInt(var2));
            break label_6;
            label_3:
            var3.setValue(Long.parseLong(var2));
            break label_6;
            label_4:
            var3.setValue(Float.parseFloat(var2));
            break label_6;
            label_5:
            var3.setValue(Double.parseDouble(var2));
            label_6:
            break label_11;
        }
        if ((arg1 instanceof ModeSetting)) {
            var3 = ((ModeSetting) arg1);
            var3.setValue(var2);
            break label_11;
        }
        if ((arg1 instanceof StringSetting)) {
            var3 = ((StringSetting) arg1);
            var3.setValue(var2);
            break label_11;
        }
        if (!((arg1 instanceof ColorSetting))) break label_11;
        var3 = ((ColorSetting) arg1);
        Object var4 = var2.split(",");
        if (var4.length < 4) break label_11;
        var3.setColor(new Color(Integer.parseInt(var4[0]), Integer.parseInt(var4[1]), Integer.parseInt(var4[2]), Integer.parseInt(var4[3])));
        if (var4.length >= 5) {
            var3.setSync(Boolean.parseBoolean(var4[4]));
        }
        if (var4.length < 6) break label_11;
        var3.setRainbow(Boolean.parseBoolean(var4[5]));
        label_11:
        var tmp0 = new Object[3];
        tmp0[0] = arg0.getModuleName();
        tmp0[1] = arg0.getSettingName();
        tmp0[2] = var2;
        LOGGER.info("[PB] Setting {}.{} = {}", tmp0);
    } catch (Exception ex13) {
        var2 = ex13;
        var tmp1 = new Object[3];
        tmp1[0] = arg0.getModuleName();
        tmp1[1] = arg0.getSettingName();
        tmp1[2] = var2;
        LOGGER.warn("[PB] Failed to apply setting {}.{}", tmp1);
    }
});
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbPlayHandler.class);
    }


    class 1 {

        static {
            $SwitchMap$night$settings$impl$NumberSetting$Type = new int[NumberSetting.Type.values().length];
            try {
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.INTEGER.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.LONG.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
                try {
                }
                $SwitchMap$night$settings$impl$NumberSetting$Type[NumberSetting.Type.DOUBLE.ordinal()] = 4;
            } catch (NoSuchFieldError ex11) {
                var0 = ex11;
            }
            $SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action = new int[ServerboundPlayerCommandPacket.Action.values().length];
            try {
                $SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action[ServerboundPlayerCommandPacket.Action.START_SPRINTING.ordinal()] = 1;
            } catch (NoSuchFieldError ex15) {
                var0 = ex15;
                try {
                }
                $SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action[ServerboundPlayerCommandPacket.Action.STOP_SPRINTING.ordinal()] = 2;
            } catch (NoSuchFieldError ex18) {
                var0 = ex18;
            }
        }

    }
}
