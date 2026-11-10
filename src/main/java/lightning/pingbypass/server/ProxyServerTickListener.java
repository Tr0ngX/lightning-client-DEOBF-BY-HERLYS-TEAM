package lightning.pingbypass.server;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.TickEvent;
import lightning.pingbypass.PingBypassFlags;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerTickListener {
    private static final Logger LOGGER;
    private final ProxyServer proxyServer;
    private static final ThreadLocal ALLOW_SEND;

    public ProxyServerTickListener(ProxyServer arg0) {
        this.proxyServer = arg0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        LightningClient.PB_MODULE_MANAGER.tick();
        if (this.proxyServer.isAlive()) {
            this.proxyServer.tick();
        }
    }

    public static void allowSend(Runnable arg0) {
        ALLOW_SEND.set(true);
        arg0.run();
        ALLOW_SEND.set(false);
        break label_3;
        Object var1 = null;
        ALLOW_SEND.set(false);
        throw var1;
        label_3:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (!PingBypassFlags.proxyForwardingActive) return;
        Object var2 = arg0.getPacket();
        if ((var2 instanceof ClientboundPlayerPositionPacket)) {
            Object var3 = ((ClientboundPlayerPositionPacket) var2);
            var tmp0 = new Object[4];
            tmp0[0] = var3.id();
            tmp0[1] = var3.change().position();
            tmp0[2] = var3.relatives();
            tmp0[3] = PingBypassFlags.clientOnGround;
            LOGGER.info("[PB] Real server sent teleport/correction id={} pos={} rel={} onGround(from client, stale on ghost until next move)={}", tmp0);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        Object var2 = arg0.getPacket();
        if ((var2 instanceof ServerboundAcceptTeleportationPacket)) {
            Object var3 = ((ServerboundAcceptTeleportationPacket) var2);
            LOGGER.info("[PB] AcceptTeleportation id={} isRealClientConfirm(allowSend)={}", var3.getId(), ALLOW_SEND.get());
        }
        if (!PingBypassFlags.proxyForwardingActive) return;
        if (((Boolean) ALLOW_SEND.get()).booleanValue()) return;
        var3 = arg0.getPacket();
        if (!(var3 instanceof ServerboundClientTickEndPacket) && !(var3 instanceof ServerboundPlayerInputPacket) && !(var3 instanceof ServerboundMoveVehiclePacket) && !(var3 instanceof ServerboundPaddleBoatPacket) && !(var3 instanceof ServerboundClientInformationPacket) && !(var3 instanceof ServerboundPlayerAbilitiesPacket) && !(var3 instanceof ServerboundContainerClosePacket) && !(var3 instanceof ServerboundPlayerLoadedPacket) && !(var3 instanceof ServerboundMovePlayerPacket) && !(var3 instanceof ServerboundAcceptTeleportationPacket)) {
            if ((var3 instanceof ServerboundPlayerCommandPacket)) {
            }
            arg0.setCancelled(true);
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger("Lightning/Rubberband");
        ALLOW_SEND = ThreadLocal.withInitial(() -> false);
    }

}
