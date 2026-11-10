package lightning.pingbypass.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lightning.pingbypass.protocol.packets.C2SJoinPacket;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.C2SPasswordPacket;
import lightning.pingbypass.protocol.packets.C2SSettingChangePacket;
import lightning.pingbypass.protocol.packets.C2SStayPacket;
import lightning.pingbypass.protocol.packets.S2CErrorPacket;
import lightning.pingbypass.protocol.packets.S2CModuleStatePacket;
import lightning.pingbypass.protocol.packets.S2CPasswordRequestPacket;
import lightning.pingbypass.protocol.packets.S2CServerNamePacket;
import lightning.pingbypass.protocol.packets.S2CSettingStatePacket;
import lightning.pingbypass.server.ProxyServer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbProtocolHandler {
    private static final Logger LOGGER;
    private final Map factories;
    private final Map handlers;

    public PbProtocolHandler() {
        this.factories = new HashMap();
        this.handlers = new HashMap();
        registerFactories();
    }

    public void register(int arg0, Function arg1, PbPacketHandler arg2) {
        this.factories.put(arg0, arg1);
        this.handlers.put(arg0, arg2);
    }

    public void registerFactory(int arg0, Function arg1) {
        this.factories.put(arg0, arg1);
    }

    public void registerHandler(int arg0, PbPacketHandler arg1) {
        this.handlers.put(arg0, arg1);
    }

    public void handle(FriendlyByteBuf arg0, Connection arg1) {
        try {
            int var3 = arg0.readVarInt();
        } catch (Exception ex2) {
            Object var4 = ex2;
            LOGGER.warn("[PbProtocol] Failed to read packet ID from buffer", var4);
            return;
        }
        var4 = ((Function) this.factories.get(var3));
        if (var4 == null) {
            LOGGER.warn("[PbProtocol] Unknown packet ID: {}", var3);
            break label_11;
            try {
            }
            Object var5 = ((PbPacket) var4.apply(arg0));
        } catch (Exception ex6) {
            Object var6 = ex6;
            LOGGER.warn("[PbProtocol] Failed to deserialize packet ID {}", var3, var6);
            return;
        }
        var6 = ((PbPacketHandler) this.handlers.get(var3));
        if (var6 == null) {
            LOGGER.warn("[PbProtocol] No handler registered for packet ID: {}", var3);
            break label_11;
            try {
            }
            var6.handle(var5, arg1);
        } catch (Exception ex10) {
            Object var7 = ex10;
            LOGGER.warn("[PbProtocol] Error handling packet ID {}", var3, var7);
        }
        label_11:
    }

    private void registerFactories() {
        registerFactory(0, C2SJoinPacket::new);
        registerFactory(1, S2CPasswordRequestPacket::new);
        registerFactory(2, C2SPasswordPacket::new);
        registerFactory(3, C2SModuleTogglePacket::new);
        registerFactory(4, C2SSettingChangePacket::new);
        registerFactory(5, S2CModuleStatePacket::new);
        registerFactory(6, S2CSettingStatePacket::new);
        registerFactory(7, S2CErrorPacket::new);
        registerFactory(8, S2CServerNamePacket::new);
        registerFactory(9, C2SStayPacket::new);
    }

    public void registerStayHandler(ProxyServer arg0) {
        register(9, C2SStayPacket::new, (arg1, arg2) -> {
    arg0.setStayConnected(arg1.isStay());
    LOGGER.info("[PbProtocol] Stay Connected set to {} via protocol handler", arg1.isStay());
});
    }

    static {
        LOGGER = LoggerFactory.getLogger(PbProtocolHandler.class);
    }

}
