package lightning.pingbypass.server;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldStateSender {
    private static final Logger LOGGER;

    public static void sendWorld(ClientLevel arg0, LocalPlayer arg1, Connection arg2) {
        LOGGER.info("Sending world state to reconnecting client...");
        try {
            if (arg0 != null && arg1 != null) {
                if (arg2 == null) {
                }
                LOGGER.warn("Cannot send world state: null parameter(s)");
                return;
                try {
                }
                WorldStateReplay.replay(arg2, arg0.registryAccess());
                LOGGER.info("World state sent successfully.");
            } catch (Exception ex5) {
                Object var3 = ex5;
                LOGGER.error("Error sending world state to client", var3);
            }
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger(WorldStateSender.class);
    }

}
