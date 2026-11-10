package lightning.managers;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.util.Arrays;
import java.util.UUID;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerConnectEvent;
import lightning.events.impl.PlayerDisconnectEvent;
import lightning.events.impl.ServerConnectEvent;
import lightning.modules.impl.miscellaneous.FastLatencyModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.utils.IMinecraft;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class ServerManager implements IMinecraft {
    private final Timer setbackTimer;
    private final Timer responseTimer;
    private final float[] tickRates;
    private int nextIndex;
    private long lastUpdate;
    private long timeJoined;
    private Pair lastConnection;

    public ServerManager() {
        this.setbackTimer = new Timer();
        this.responseTimer = new Timer();
        this.tickRates = new float[20];
        this.nextIndex = 0;
        this.lastUpdate = -1L;
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        this.responseTimer.reset();
        if ((arg0.getPacket() instanceof ClientboundPlayerPositionPacket)) {
            this.setbackTimer.reset();
        }
        if ((arg0.getPacket() instanceof ClientboundSetTimePacket)) {
            this.tickRates[this.nextIndex] = Math.clamp((20.0f / (((float) (System.currentTimeMillis() - this.lastUpdate)) / 1000.0f)), 0.0f, 20.0f);
            this.nextIndex = ((this.nextIndex + 1) % this.tickRates.length);
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent arg0) {
        Arrays.fill(this.tickRates, 0.0f);
        this.nextIndex = 0;
        this.timeJoined = System.currentTimeMillis();
        this.lastUpdate = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void handleConnections(PacketReceiveEvent arg0) {
        if (mc.level == null) return;
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundPlayerInfoUpdatePacket))) {
        } else {
            Object var3 = ((ClientboundPlayerInfoUpdatePacket) var2);
            if (!(var3.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER))) {
                break label_7;
            }
            Object var4 = var3.newEntries().iterator();
            while (var4.hasNext()) {
                Object var5 = ((ClientboundPlayerInfoUpdatePacket.Entry) var4.next());
                LightningClient.EVENT_HANDLER.post(new PlayerConnectEvent(var5.profile().id()));
            }
            break label_7;
        }
        var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundPlayerInfoRemovePacket))) break label_7;
        var3 = ((ClientboundPlayerInfoRemovePacket) var2);
        var4 = var3.profileIds().iterator();
        while (var4.hasNext()) {
            var5 = ((UUID) var4.next());
            LightningClient.EVENT_HANDLER.post(new PlayerDisconnectEvent(var5));
        }
        label_7:
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent arg0) {
        this.lastConnection = new ObjectObjectImmutablePair(arg0.getAddress(), arg0.getInfo());
    }

    public float getTickRate() {
        if (mc.player == null) return 0.0f;
        if ((System.currentTimeMillis() - this.timeJoined) < 4000L) return 20.0f;
        int var1 = 0;
        float var2 = 0.0f;
        Object var3 = this.tickRates;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            float var6 = var3[var5];
            if (var6 <= 0.0f) {
            } else {
                var2 = (var2 + var6);
                var1++;
            }
            var5++;
        }
        return (var2 / ((float) var1));
    }

    public int getPingDelay() {
        return ((int) (((float) getPing()) / 25.0f));
    }

    public int getPing() {
        if (((FastLatencyModule) LightningClient.MODULE_MANAGER.getModule(FastLatencyModule.class)).isToggled()) return ((FastLatencyModule) LightningClient.MODULE_MANAGER.getModule(FastLatencyModule.class)).getLatency();
        if ((mc.getConnection() == null) || (mc.player == null)) {
            return 0;
        }
        Object var1 = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (var1 != null) return var1.getLatency();
        if (!PingBypassFlags.proxyForwardingActive) break label_9;
        Object var2 = mc.getConnection().getOnlinePlayers().iterator();
        label_4:
        if (var2.hasNext()) {
            Object var3 = ((PlayerInfo) var2.next());
            if (!(var3.getProfile().name().equals(mc.player.getName().getString()))) {
                break label_4;
            }
            return var3.getLatency();
        }
        var2 = mc.getConnection().getOnlinePlayers().iterator();
        label_7:
        if (!(var2.hasNext())) break label_9;
        var3 = ((PlayerInfo) var2.next());
        if (var3.getLatency() <= 0) {
            break label_7;
        }
        return var3.getLatency();
        label_9:
        return 0;
    }

    public String getServerBrand() {
        if (((mc.getCurrentServer() == null) || (mc.getConnection() == null)) || (mc.getConnection().serverBrand() == null)) {
            return "Vanilla";
        }
        return mc.getConnection().serverBrand();
    }

    public String getServer() {
        if (mc.hasSingleplayerServer()) {
        } else {
        }
        return ServerAddress.parseString(mc.getCurrentServer().ip).getHost();
    }

    @Generated
    public Timer getSetbackTimer() {
        return this.setbackTimer;
    }

    @Generated
    public Timer getResponseTimer() {
        return this.responseTimer;
    }

    @Generated
    public float[] getTickRates() {
        return this.tickRates;
    }

    @Generated
    public int getNextIndex() {
        return this.nextIndex;
    }

    @Generated
    public long getLastUpdate() {
        return this.lastUpdate;
    }

    @Generated
    public long getTimeJoined() {
        return this.timeJoined;
    }

    @Generated
    public Pair getLastConnection() {
        return this.lastConnection;
    }

}
