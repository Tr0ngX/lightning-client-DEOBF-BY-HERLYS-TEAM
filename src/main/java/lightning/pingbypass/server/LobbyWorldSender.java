package lightning.pingbypass.server;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LobbyWorldSender {
    private static final Logger LOGGER;

    public static void sendLobbyWorld(Connection arg0, RegistryAccess arg1) {
        LOGGER.info("Sending lobby world to client...");
        try {
            Object var2 = arg1.lookupOrThrow(Registries.DIMENSION_TYPE);
            Object var3 = var2.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            var tmp0 = new ResourceKey[3];
            tmp0[0] = Level.OVERWORLD;
            tmp0[1] = Level.NETHER;
            tmp0[2] = Level.END;
            Object var4 = Sets.newHashSet(tmp0);
            Object var5 = new CommonPlayerSpawnInfo(var3, Level.OVERWORLD, 0L, GameType.SPECTATOR, null, false, true, Optional.empty(), 0, 63);
            send(arg0, new ClientboundLoginPacket(1337, false, var4, 1, 16, 16, false, true, false, var5, false, false));
            send(arg0, new ClientboundPlayerAbilitiesPacket(createLobbyAbilities()));
            send(arg0, new ClientboundSetHeldSlotPacket(0));
            send(arg0, new ClientboundPlayerPositionPacket(1, new PositionMoveRotation(new Vec3(0.0, 240.0, 0.0), Vec3.ZERO, 0.0f, 0.0f), Set.of()));
            send(arg0, new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0f));
            LOGGER.info("Lobby world sent successfully.");
        } catch (Exception ex2) {
            var2 = ex2;
            LOGGER.error("Failed to send lobby world", var2);
        }
    }

    private static void send(Connection arg0, Packet arg1) {
        if (arg0.isConnected()) {
            arg0.send(arg1);
        }
    }

    private static Abilities createLobbyAbilities() {
        Object var0 = new Abilities();
        var0.mayfly = true;
        var0.flying = true;
        var0.invulnerable = true;
        return var0;
    }

    static {
        LOGGER = LoggerFactory.getLogger(LobbyWorldSender.class);
    }

}
