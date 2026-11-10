package lightning.modules.impl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.events.impl.TickEvent;
import lightning.gui.ClickGuiScreen;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.input.ClientInputForwarder;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.PbPacket;
import lightning.pingbypass.protocol.packets.C2SFriendSyncPacket;
import lightning.pingbypass.protocol.packets.C2SJoinPacket;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.C2SPasswordPacket;
import lightning.pingbypass.protocol.packets.C2SSettingChangePacket;
import lightning.pingbypass.protocol.packets.S2CBlockRenderPacket;
import lightning.pingbypass.protocol.packets.S2CErrorPacket;
import lightning.pingbypass.protocol.packets.S2CMiningStatePacket;
import lightning.pingbypass.protocol.packets.S2CModuleStatePacket;
import lightning.pingbypass.protocol.packets.S2CRenderPositionPacket;
import lightning.pingbypass.protocol.packets.S2CServerNamePacket;
import lightning.pingbypass.protocol.packets.S2CSettingStatePacket;
import lightning.pingbypass.protocol.packets.S2CSlotSyncPacket;
import lightning.pingbypass.protocol.packets.S2CWindowClickPacket;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.miscellaneous.RenderPosition;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

@RegisterModule(name = "PingBypass", description = "Connects to a PingBypass proxy server for low-latency combat.", category = Module.Category.CORE)
public class PingBypassModule extends Module {
    public final StringSetting ip;
    public final NumberSetting port;
    public final StringSetting password;
    public final StringSetting server;
    private volatile boolean joinSent;
    public volatile int proxyPing;
    private volatile String serverName;
    private final Map proxyModuleStates;
    private final Map proxySettingValues;
    private volatile Connection proxyConnection;
    private final List pendingPayloads;
    private int pendingDelay;
    private ClientInputForwarder inputForwarder;

    public PingBypassModule() {
        this.ip = new StringSetting("IP", "The IP address of the PingBypass proxy server.", "127.0.0.1");
        this.port = new NumberSetting("Port", "The port of the PingBypass proxy server.", 25565, 1, 65535);
        this.password = new StringSetting("Password", "The password for the PingBypass proxy server.", "");
        this.server = new StringSetting("Server", "The target Minecraft server to join through the proxy (e.g. mc.hypixel.net or mc.hypixel.net:25565).", "");
        this.proxyModuleStates = new ConcurrentHashMap();
        this.proxySettingValues = new ConcurrentHashMap();
        this.pendingPayloads = Collections.synchronizedList(new ArrayList());
        this.pendingDelay = 10;
    }

    public void onEnable() {
        if (LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) {
            LightningClient.CHAT_MANAGER.error("Cannot enable PingBypass on a PingBypass server!");
            setToggled(false, false);
            return;
        }
        if (mc.gui.overlay() != null) {
            LightningClient.LOGGER.info("[PingBypass] Skipping auto-connect during startup");
            return;
        }
        if (LightningClient.CONFIG_MANAGER != null && LightningClient.CONFIG_MANAGER.isLoadingConfig()) {
            LightningClient.LOGGER.info("[PingBypass] Skipping auto-connect during config load");
            return;
        }
        if ((mc.gui.screen() instanceof ClickGuiScreen)) {
            mc.gui.setScreen(null);
        }
        this.proxyModuleStates.clear();
        this.proxySettingValues.clear();
        this.serverName = null;
        this.joinSent = false;
        if (mc.level != null) {
            mc.level.disconnect(Component.literal("PingBypass enabled."));
        }
        if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect(Component.literal("PingBypass enabled."));
        }
        Object var1 = this.ip.getValue();
        int var2 = this.port.getValue().intValue();
        Object var3 = new ServerAddress(var1, var2);
        Object var4 = new ServerData("PingBypass Proxy", (var3.getHost() + ":" + var3.getPort()), ServerData.Type.OTHER);
        ConnectScreen.startConnecting(new TitleScreen(), mc, var3, var4, false, null);
    }

    public void onDisable() {
        if (mc.level != null) {
            mc.level.disconnect(Component.literal("PingBypass disabled."));
        }
        if (mc.getConnection() != null) {
            mc.getConnection().getConnection().disconnect(Component.literal("PingBypass disabled."));
        }
        this.proxyPing = 0;
        this.serverName = null;
        this.proxyConnection = null;
        PingBypassFlags.proxyForwardingActive = false;
        if (this.inputForwarder != null) {
            this.inputForwarder.stop();
            this.inputForwarder = null;
        }
        this.proxyModuleStates.clear();
        this.proxySettingValues.clear();
    }

    public String getMetaData() {
        return (this.proxyPing + "ms");
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent arg0) {
        if (isToggled() && !(!PingBypassFlags.proxyForwardingActive)) {
            mc.execute(() -> setToggled(false, false));
        }
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent arg0) {
        if ((!(isToggled())) || (!PingBypassFlags.proxyForwardingActive)) {
            return;
        }
        if (LightningClient.PINGBYPASS_CONFIG != null && LightningClient.PINGBYPASS_CONFIG.isServer()) return;
        Object var2 = arg0.getSetting();
        Object var3 = LightningClient.MODULE_MANAGER.getModules().iterator();
        label_3:
        if (var3.hasNext()) {
            Object var4 = ((Module) var3.next());
            if (!(var4.isProxyEnhanced())) break label_3;
            if (!(var4.getSettings().contains(var2))) {
                break label_3;
            }
            Object var5 = serializeSetting(var2);
            if (var5 == null) return;
            Object var6 = this.proxyConnection;
            if (var6 == null && mc.getConnection() != null) {
                var6 = mc.getConnection().getConnection();
            }
            if (var6 != null && var6.isConnected()) {
                var6.send(new ServerboundCustomPayloadPacket(PbCustomPayload.fromPacket(new C2SSettingChangePacket(var4.getName(), var2.getName(), var5))));
            }
            return;
        }
    }

    private String serializeSetting(Setting arg0) {
        if ((arg0 instanceof BooleanSetting)) {
            Object var2 = ((BooleanSetting) arg0);
            return String.valueOf(var2.getValue());
        }
        if ((arg0 instanceof NumberSetting)) {
            var2 = ((NumberSetting) arg0);
            return var2.getValue().toString();
        }
        if ((arg0 instanceof ModeSetting)) {
            var2 = ((ModeSetting) arg0);
            return var2.getValue();
        }
        if ((arg0 instanceof StringSetting)) {
            var2 = ((StringSetting) arg0);
            return var2.getValue();
        }
        if ((arg0 instanceof ColorSetting)) {
            var2 = ((ColorSetting) arg0);
            Object var3 = var2.getValue().getColor();
            return (var3.getRed() + "," + var3.getGreen() + "," + var3.getBlue() + "," + var3.getAlpha() + "," + var2.isSync() + "," + var2.isRainbow());
        }
        return null;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundCustomPayloadPacket))) return;
        Object var3 = ((ClientboundCustomPayloadPacket) var2);
        Object var4 = var3.payload();
        if (!((var4 instanceof PbCustomPayload))) return;
        Object var5 = ((PbCustomPayload) var4);
        if (arg0.getConnection() == null) break label_2;
        this.proxyConnection = arg0.getConnection();
        label_2:
        Object var6 = var5.toBuf();
        try {
            int var7 = var6.readVarInt();
            switch (var7) {
                case 1 -> { break label_4; }
                case 2 -> { break label_24; }
                case 3 -> { break label_24; }
                case 4 -> { break label_24; }
                case 5 -> { break label_6; }
                case 6 -> { break label_8; }
                case 7 -> { break label_10; }
                case 8 -> { break label_12; }
                case 9 -> { break label_24; }
                case 10 -> { break label_14; }
                case 11 -> { break label_16; }
                case 12 -> { break label_18; }
                case 13 -> { break label_20; }
                case 14 -> { break label_24; }
                case 15 -> { break label_24; }
                case 16 -> { break label_22; }
                default -> { break label_24; }
            }
            label_4:
            handlePasswordRequest();
            var6.release();
            return;
            try {
                label_6:
                handleModuleState(new S2CModuleStatePacket(var6));
                var6.release();
                return;
                try {
                    label_8:
                    handleSettingState(new S2CSettingStatePacket(var6));
                    var6.release();
                    return;
                    try {
                        label_10:
                        handleError(new S2CErrorPacket(var6));
                        var6.release();
                        return;
                        try {
                            label_12:
                            handleServerName(new S2CServerNamePacket(var6));
                            var6.release();
                            return;
                            try {
                                label_14:
                                handleRenderPosition(new S2CRenderPositionPacket(var6));
                                var6.release();
                                return;
                                try {
                                    label_16:
                                    handleBlockRender(new S2CBlockRenderPacket(var6));
                                    var6.release();
                                    return;
                                    try {
                                        label_18:
                                        handleMiningState(new S2CMiningStatePacket(var6));
                                        var6.release();
                                        return;
                                        try {
                                            label_20:
                                            handleSlotSync(new S2CSlotSyncPacket(var6));
                                            var6.release();
                                            return;
                                            try {
                                                label_22:
                                                handleWindowClick(new S2CWindowClickPacket(var6));
                                                var6.release();
                                                return;
                                                label_24:
                                                var6.release();
                                                return;
                                            } catch (Exception ex25) {
                                                var7 = ex25;
                                                LightningClient.LOGGER.warn("[PingBypass] Failed to handle S2C packet", var7);
                                                var6.release();
                                                return;
                                            }
                                            Object var8 = null;
                                            var6.release();
                                            throw var8;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handlePasswordRequest() {
        Object var1 = this.proxyConnection;
        if (var1 == null && mc.getConnection() != null) {
            var1 = mc.getConnection().getConnection();
        }
        if (var1 != null) {
            if (!(var1.isConnected())) {
            }
            LightningClient.LOGGER.warn("[PingBypass] Cannot send password response \u2014 no active connection");
            return;
        }
        sendRawCustomPayload(var1, new C2SPasswordPacket(this.password.getValue()));
        LightningClient.LOGGER.info("[PingBypass] Sent password response to proxy");
        sendJoinIfReady(var1);
    }

    private void sendJoinIfReady(Connection arg0) {
        if (!(!this.joinSent)) return;
        Object var3 = this.server.getValue();
        if ((var3 == null) || (var3.isBlank())) {
            return;
        }
        int var4 = 25565;
        if (!(var3.contains(":"))) break label_7;
        Object var5 = var3.split(":", 2);
        Object var2 = var5[0];
        try {
            var4 = Integer.parseInt(var5[1]);
        } catch (NumberFormatException ex5) {
            Object var6 = ex5;
            LightningClient.LOGGER.warn("[PingBypass] Invalid port in server setting: {}", var3);
            return;
        }
        break label_8;
        label_7:
        var2 = var3;
        label_8:
        this.joinSent = true;
        sendRawCustomPayload(arg0, new C2SJoinPacket(var2, var4));
        LightningClient.LOGGER.info("[PingBypass] Sent join request to proxy: {}:{}", var2, var4);
    }

    private void sendRawCustomPayload(Connection arg0, PbPacket arg1) {
        Object var3 = PbCustomPayload.fromPacket(arg1);
        this.pendingPayloads.add(new PingBypassModule.PendingPayload(var3, arg0));
        this.pendingDelay = 10;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (this.pendingPayloads.isEmpty()) return;
        if (this.pendingDelay > 0) {
            this.pendingDelay = (this.pendingDelay - 1);
            return;
        }
        Object var2 = this.pendingPayloads.iterator();
        while (var2.hasNext()) {
            Object var3 = ((PingBypassModule.PendingPayload) var2.next());
            if (!(var3.connection.isConnected())) {
                var2.remove();
                break label_2;
                try {
                }
                var3.connection.send(new ServerboundCustomPayloadPacket(var3.payload));
                var2.remove();
                LightningClient.LOGGER.info("[PingBypass] Sent deferred payload via vanilla networking");
            } catch (IllegalStateException ex5) {
                Object var4 = ex5;
                this.pendingDelay = 5;
            }
        }
    }

    private void handleModuleState(S2CModuleStatePacket arg0) {
        this.proxyModuleStates.put(arg0.getModuleName(), arg0.isEnabled());
        Object var2 = LightningClient.MODULE_MANAGER.getModule(arg0.getModuleName());
        if (var2 != null && var2.isToggled() != arg0.isEnabled()) {
            mc.execute(() -> arg0.setToggled(arg1.isEnabled(), false));
        }
        LightningClient.LOGGER.debug("[PingBypass] Module {} state synced to {}", arg0.getModuleName(), arg0.isEnabled());
    }

    private void handleSettingState(S2CSettingStatePacket arg0) {
        Object var2 = (arg0.getModuleName() + "." + arg0.getSettingName());
        this.proxySettingValues.put(var2, arg0.getValue());
        var tmp0 = new Object[3];
        tmp0[0] = arg0.getModuleName();
        tmp0[1] = arg0.getSettingName();
        tmp0[2] = arg0.getValue();
        LightningClient.LOGGER.debug("[PingBypass] Setting {}.{} updated to {}", tmp0);
    }

    private void handleError(S2CErrorPacket arg0) {
        LightningClient.CHAT_MANAGER.error(("[PingBypass] " + arg0.getMessage()));
    }

    private void handleServerName(S2CServerNamePacket arg0) {
        this.serverName = arg0.getServerIp();
        PingBypassFlags.proxyForwardingActive = true;
        if (this.inputForwarder == null) {
            this.inputForwarder = new ClientInputForwarder();
            this.inputForwarder.start();
        }
        syncAllSettingsToProxy();
        LightningClient.CHAT_MANAGER.message(("[PingBypass] Proxy connected to: " + arg0.getServerIp()));
    }

    public void syncAllSettingsToProxy() {
        Object var1 = this.proxyConnection;
        if (var1 == null && mc.getConnection() != null) {
            var1 = mc.getConnection().getConnection();
        }
        if ((var1 == null) || (!(var1.isConnected()))) {
            return;
        }
        int var2 = 0;
        Object var3 = LightningClient.MODULE_MANAGER.getModules().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Module) var3.next());
            if (!(var4.isProxyEnhanced())) {
                break label_3;
            }
            var1.send(new ServerboundCustomPayloadPacket(PbCustomPayload.fromPacket(new C2SModuleTogglePacket(var4.getName(), var4.isToggled()))));
            Object var5 = var4.getSettings().iterator();
            while (var5.hasNext()) {
                Object var6 = ((Setting) var5.next());
                Object var7 = serializeSetting(var6);
                if (var7 == null) {
                    break label_5;
                }
                var1.send(new ServerboundCustomPayloadPacket(PbCustomPayload.fromPacket(new C2SSettingChangePacket(var4.getName(), var6.getName(), var7))));
                var2++;
            }
        }
        LightningClient.LOGGER.info("[PingBypass] Synced {} settings to proxy", var2);
        syncFriendsToProxy();
    }

    public void syncFriendsToProxy() {
        Object var1 = this.proxyConnection;
        if (var1 == null && mc.getConnection() != null) {
            var1 = mc.getConnection().getConnection();
        }
        if ((var1 == null) || (!(var1.isConnected()))) {
            return;
        }
        var1.send(new ServerboundCustomPayloadPacket(PbCustomPayload.fromPacket(new C2SFriendSyncPacket(new ArrayList(LightningClient.FRIEND_MANAGER.getFriends())))));
    }

    private void handleRenderPosition(S2CRenderPositionPacket arg0) {
        LightningClient.RENDER_MANAGER.setRenderPosition(arg0.getPosition());
    }

    private void handleBlockRender(S2CBlockRenderPacket arg0) {
        Object var2 = new RenderPosition(arg0.getPosition());
        if (!(LightningClient.RENDER_MANAGER.renderPositions.contains(var2))) {
            LightningClient.RENDER_MANAGER.renderPositions.add(var2);
        }
    }

    private void handleMiningState(S2CMiningStatePacket arg0) {
        Object var2 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (var2 != null) {
            var2.updateProxyMiningState(arg0.getPrimaryPos(), arg0.getPrimaryProgress(), arg0.getSecondaryPos(), arg0.getSecondaryProgress());
        }
    }

    private void handleSlotSync(S2CSlotSyncPacket arg0) {
        if (mc.player != null) {
            mc.player.getInventory().setSelectedSlot(arg0.getSlot());
        }
    }

    private void handleWindowClick(S2CWindowClickPacket arg0) {
        mc.execute(() -> {
    if (mc.player != null) {
        if (mc.player.containerMenu.containerId != arg0.getContainerId()) {
        }
        return;
        try {
        }
        mc.player.containerMenu.clicked(arg0.getSlotNum(), arg0.getButtonNum(), arg0.getContainerInput(), mc.player);
    } catch (Exception ex3) {
        Object var1 = ex3;
        LightningClient.LOGGER.warn("[PingBypass] Failed to replay proxy window click", var1);
    }
});
    }

    public String getServerName() {
        return this.serverName;
    }

    public Map getProxyModuleStates() {
        return this.proxyModuleStates;
    }

    public Map getProxySettingValues() {
        return this.proxySettingValues;
    }

    public boolean isProxyModuleEnabled(String arg0) {
        return ((Boolean) this.proxyModuleStates.getOrDefault(arg0, false)).booleanValue();
    }

    public String getProxySettingValue(String arg0, String arg1) {
        return ((String) this.proxySettingValues.get((arg0 + "." + arg1)));
    }


    record PendingPayload(PbCustomPayload payload, Connection connection) {

        private PendingPayload(PbCustomPayload arg0, Connection arg1) {
            this.payload = arg0;
            this.connection = arg1;
        }

        public PbCustomPayload payload() {
            return this.payload;
        }

        public Connection connection() {
            return this.connection;
        }

    }
}
