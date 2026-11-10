package lightning.pingbypass.server;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.handler.PbPlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.GameProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferRehook {
    private static final Logger LOGGER;

    public TransferRehook() {
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((((LightningClient.PINGBYPASS_CONFIG == null) || (!(LightningClient.PINGBYPASS_CONFIG.isServer()))) || (!PingBypassFlags.proxyForwardingActive)) || (LightningClient.PROXY_SERVER == null)) break label_5;
        Object var2 = Minecraft.getInstance();
        if (((var2.getConnection() == null) || (var2.player == null)) || (var2.level == null)) break label_5;
        Object var3 = var2.getConnection().getConnection();
        Object var4 = LightningClient.PROXY_SERVER.getServerConnection();
        if ((var3 == var4) || (!(var3.isConnected()))) break label_5;
        LOGGER.warn("[PB] Proxy's own server connection changed underneath us (transfer/backend switch) -- rewiring {} connected client(s)", LightningClient.PROXY_SERVER.getConnections().size());
        LightningClient.PROXY_SERVER.setServerConnection(var3);
        Object var5 = var2.level.registryAccess();
        Object var6 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var6.hasNext()) {
            Object var7 = ((Connection) var6.next());
            if (!(var7.isConnected())) break label_4;
            Object var9 = var7.getPacketListener();
            if (!((var9 instanceof PbPlayHandler))) break label_4;
            Object var8 = ((PbPlayHandler) var9);
            var9 = var8.getProfile();
            try {
                var7.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var5)));
                Object var10 = new S2CForwarder(var7);
                var10.start();
                int var11 = WorldStateReplay.replay(var7, var5);
                Object var12 = new PbPlayHandler(LightningClient.PROXY_SERVER, var7, var9, var10, var11);
                var7.setupInboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(var5), () -> false), var12);
                LOGGER.info("[PB] Rewired {} onto the new server connection after transfer", var9.name());
            } catch (Exception ex3) {
                var10 = ex3;
                LOGGER.error("[PB] Failed to rewire {} after transfer", var9.name(), var10);
            }
            label_4:
        }
        label_5:
    }

    static {
        LOGGER = LoggerFactory.getLogger(TransferRehook.class);
    }

}
