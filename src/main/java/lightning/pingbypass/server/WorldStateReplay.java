package lightning.pingbypass.server;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lightning.mixins.accessors.PlayerListS2CPacketAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldStateReplay {
    private static final Logger LOGGER;
    private static final Random RANDOM;

    public static int replay(Connection arg0, RegistryAccess arg1) {
        Object var2 = Minecraft.getInstance();
        Object var3 = var2.player;
        Object var4 = var2.level;
        Object var5 = var2.getConnection();
        if (var3 != null && var4 != null && var5 != null) {
            LOGGER.info("Replaying world state to client...");
            long var6 = System.currentTimeMillis();
            int var8 = -(Math.abs(RANDOM.nextInt()));
            if (!var8) {
                var8 = -1;
            }
            if (var2.gameMode != null) {
            } else {
            }
            Object var9 = GameType.SURVIVAL;
            if (var2.gameMode != null) {
            } else {
            }
            Object var10 = null;
            Object var11 = var5.levels();
            Object var12 = createSpawnInfo(var4, var9, var10, var3, arg1);
            send(arg0, new ClientboundLoginPacket(var3.getId(), var4.getLevelData().isHardcore(), var11, 1, 16, 16, false, true, false, var12, false, false));
            send(arg0, new ClientboundRespawnPacket(var12, 0));
            send(arg0, new ClientboundChangeDifficultyPacket(var4.getLevelData().getDifficulty(), var4.getLevelData().isDifficultyLocked()));
            send(arg0, new ClientboundPlayerAbilitiesPacket(var3.getAbilities()));
            send(arg0, new ClientboundSetHeldSlotPacket(var3.getInventory().getSelectedSlot()));
            sendPlayerInfo(arg0, var5);
            sendLevelInfo(arg0, var4);
            send(arg0, new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0.0f));
            sendChunks(arg0, var3, var4);
            send(arg0, new ClientboundSetHealthPacket(var3.getHealth(), var3.getFoodData().getFoodLevel(), var3.getFoodData().getSaturationLevel()));
            send(arg0, new ClientboundSetExperiencePacket(var3.experienceProgress, var3.totalExperience, var3.experienceLevel));
            send(arg0, new ClientboundContainerSetContentPacket(var3.inventoryMenu.containerId, var3.inventoryMenu.incrementStateId(), var3.inventoryMenu.getItems(), var3.inventoryMenu.getCarried()));
            sendEntities(arg0, var4, var3);
            send(arg0, new ClientboundPlayerPositionPacket(var8, new PositionMoveRotation(var3.position(), Vec3.ZERO, var3.getYRot(), var3.getXRot()), Set.of()));
            send(arg0, new ClientboundSetEntityMotionPacket(var3));
            long var13 = (System.currentTimeMillis() - var6);
            LOGGER.info("Level state replay completed in {}ms (teleportId={})", var13, var8);
            return var8;
        }
        LOGGER.error("Cannot replay world state: player/world/handler is null");
        return 0;
    }

    private static void sendLevelInfo(Connection arg0, ClientLevel arg1) {
        send(arg0, new ClientboundInitializeBorderPacket(arg1.getWorldBorder()));
        send(arg0, new ClientboundSetTimePacket(arg1.getLevelData().getGameTime(), Map.of()));
        send(arg0, new ClientboundSetDefaultSpawnPositionPacket(arg1.getLevelData().getRespawnData()));
        if (arg1.isRaining()) {
            send(arg0, new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0f));
            send(arg0, new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, arg1.getRainLevel(1.0f)));
            send(arg0, new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, arg1.getThunderLevel(1.0f)));
        }
    }

    private static void sendPlayerInfo(Connection arg0, ClientPacketListener arg1) {
        Object var2 = arg1.getOnlinePlayers();
        if (!(var2.isEmpty())) {
            Object var3 = new ArrayList();
            Object var4 = var2.iterator();
            while (var4.hasNext()) {
                Object var5 = ((PlayerInfo) var4.next());
                var3.add(new ClientboundPlayerInfoUpdatePacket.Entry(var5.getProfile().id(), var5.getProfile(), arg1.getListedOnlinePlayers().contains(var5), var5.getLatency(), var5.getGameMode(), var5.getTabListDisplayName(), false, 0, null));
            }
            var4 = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
            var5 = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), Collections.emptyList());
            ((PlayerListS2CPacketAccessor) var5).setActions(var4);
            ((PlayerListS2CPacketAccessor) var5).setEntries(var3);
            send(arg0, var5);
            LOGGER.info("Sent {} player list entries to client", var3.size());
        }
    }

    private static CommonPlayerSpawnInfo createSpawnInfo(ClientLevel arg0, GameType arg1, GameType arg2, LocalPlayer arg3, RegistryAccess arg4) {
        Object var5 = ((ResourceKey) arg0.dimensionTypeRegistration().unwrapKey().orElseThrow());
        Object var6 = arg4.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(var5);
        return new CommonPlayerSpawnInfo(var6, arg0.dimension(), 0L, arg1, arg2, arg0.isDebug(), false, arg3.getLastDeathLocation(), arg3.getPortalCooldown(), arg0.getSeaLevel());
    }

    private static void sendEntities(Connection arg0, ClientLevel arg1, LocalPlayer arg2) {
        int var3 = 0;
        Object var4 = arg1.entitiesForRendering().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Entity) var4.next());
            if (var5 == arg2) break label_9;
            try {
                send(arg0, new ClientboundAddEntityPacket(var5.getId(), var5.getUUID(), var5.getX(), var5.getY(), var5.getZ(), var5.getXRot(), var5.getYRot(), var5.getType(), 0, var5.getDeltaMovement(), ((double) var5.getYHeadRot())));
                Object var6 = var5.getEntityData().getNonDefaultValues();
                if (var6 != null && !(var6.isEmpty())) {
                    send(arg0, new ClientboundSetEntityDataPacket(var5.getId(), var6));
                }
                send(arg0, new ClientboundSetEntityMotionPacket(var5));
                if (!((var5 instanceof LivingEntity))) break label_6;
                Object var7 = ((LivingEntity) var5);
                send(arg0, new ClientboundRotateHeadPacket(var5, ((byte) ((int) ((var7.getYHeadRot() * 256.0f) / 360.0f)))));
                Object var8 = new ArrayList();
                Object var9 = EquipmentSlot.values();
                int var10 = var9.length;
                int var11 = 0;
                while (var11 < var10) {
                    Object var12 = var9[var11];
                    Object var13 = var7.getItemBySlot(var12);
                    if (!(var13.isEmpty())) {
                        var8.add(new Pair(var12, var13));
                    }
                    var11++;
                }
                if (var8.isEmpty()) break label_6;
                send(arg0, new ClientboundSetEquipmentPacket(var5.getId(), var8));
                label_6:
                var3++;
            } catch (Exception ex8) {
                var6 = ex8;
                var tmp0 = new Object[3];
                tmp0[0] = var5.getType().getDescriptionId();
                tmp0[1] = var5.getId();
                tmp0[2] = var6;
                LOGGER.warn("Failed to send entity {} (ID: {})", tmp0);
            }
            label_9:
        }
        LOGGER.info("Sent {} entities to client", var3);
    }

    private static void sendChunks(Connection arg0, LocalPlayer arg1, ClientLevel arg2) {
        int var3 = arg1.chunkPosition().x();
        int var4 = arg1.chunkPosition().z();
        send(arg0, new ClientboundSetChunkCacheCenterPacket(var3, var4));
        int var5 = Math.min(((Integer) Minecraft.getInstance().options.renderDistance().get()).intValue(), 8);
        int var6 = 0;
        int var7 = (var4 - var5);
        while (var7 <= (var4 + var5)) {
            int var8 = (var3 - var5);
            while (var8 <= (var3 + var5)) {
                Object var9 = arg2.getChunkSource().getChunk(var8, var7, ChunkStatus.FULL, false);
                if (var9 == null) {
                } else {
                    send(arg0, new ClientboundLevelChunkWithLightPacket(var9, arg2.getChunkSource().getLightEngine(), null, null));
                    var6++;
                }
                var8++;
            }
            var7++;
        }
        LOGGER.info("Sent {} chunks to client", var6);
    }

    private static void send(Connection arg0, Packet arg1) {
        if (arg0.isConnected()) {
            arg0.send(arg1);
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger(WorldStateReplay.class);
        RANDOM = new Random();
    }

}
