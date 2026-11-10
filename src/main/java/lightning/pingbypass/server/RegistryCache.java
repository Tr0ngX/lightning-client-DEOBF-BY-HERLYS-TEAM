package lightning.pingbypass.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryCache {
    private static final Logger LOGGER;
    private static final Executor SYNC_EXECUTOR;
    private final List registryPackets;
    private final List knownPacks;
    private LayeredRegistryAccess registries;
    private RegistryAccess registryManager;
    private boolean loaded;
    private volatile boolean loadAttempted;

    public RegistryCache() {
        this.registryPackets = new ArrayList();
        this.knownPacks = new ArrayList();
    }

    public void load() {
        LOGGER.info("Loading vanilla registries for proxy...");
        long var1 = System.currentTimeMillis();
        try {
            Object var3 = ServerPacksSource.createVanillaTrustedRepository();
            MinecraftServer.configurePackRepository(var3, WorldDataConfiguration.DEFAULT, true, true);
            Object var4 = var3.openAllSelected();
            Object var5 = new MultiPackResourceManager(PackType.SERVER_DATA, var4);
            try {
                Object var6 = RegistryLayer.createRegistryAccess();
                Object var7 = TagLoader.loadTagsForExistingRegistries(var5, var6.getLayer(RegistryLayer.STATIC));
                Object var8 = var6.getAccessForLoading(RegistryLayer.WORLDGEN);
                Object var9 = TagLoader.buildUpdatedLookups(var8, var7);
                Object var10 = ((RegistryAccess.Frozen) RegistryDataLoader.load(var5, var9, RegistryDataLoader.WORLDGEN_REGISTRIES, SYNC_EXECUTOR).join());
                Object var11 = Stream.concat(var9.stream(), var10.listRegistries()).toList();
                Object var12 = ((RegistryAccess.Frozen) RegistryDataLoader.load(var5, var11, RegistryDataLoader.DIMENSION_REGISTRIES, SYNC_EXECUTOR).join());
                var tmp0 = new RegistryAccess.Frozen[2];
                tmp0[0] = var10;
                tmp0[1] = var12;
                this.registries = var6.replaceFrom(RegistryLayer.WORLDGEN, tmp0);
                Object var13 = var7.iterator();
                while (var13.hasNext()) {
                    Object var14 = ((Registry.PendingTags) var13.next());
                    var14.apply();
                }
                this.registryManager = this.registries.compositeAccess();
                this.knownPacks.addAll(var5.listPacks().flatMap((arg0) -> arg0.knownPackInfo().stream()).toList());
                var13 = this.registries.compositeAccess().createSerializationContext(NbtOps.INSTANCE);
                RegistrySynchronization.packRegistries(var13, this.registries.getAccessFrom(RegistryLayer.WORLDGEN), Set.of(), (arg1) -> this.registryPackets.add(new ClientboundRegistryDataPacket(arg0, arg1)));
                this.registryPackets.add(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
                this.loaded = true;
                var5.close();
            } catch (Throwable ex5) {
                var6 = ex5;
                try {
                    var5.close();
                } catch (Throwable ex8) {
                    var7 = ex8;
                    var6.addSuppressed(var7);
                }
                throw var6;
            }
            var5 = (System.currentTimeMillis() - var1);
            LOGGER.info("Loaded {} registry packets in {}ms", this.registryPackets.size(), var5);
        } catch (Exception ex12) {
            var3 = ex12;
            LOGGER.error("Failed to load vanilla registries", var3);
        }
    }

    public boolean isLoaded() {
        if (!this.loaded && !this.loadAttempted) {
            this.loadAttempted = true;
            load();
        }
        return this.loaded;
    }

    public List getRegistryPackets() {
        return this.registryPackets;
    }

    public List getKnownPacks() {
        return this.knownPacks;
    }

    public LayeredRegistryAccess getRegistries() {
        return this.registries;
    }

    public RegistryAccess getRegistryManager() {
        return this.registryManager;
    }

    static {
        LOGGER = LoggerFactory.getLogger(RegistryCache.class);
        SYNC_EXECUTOR = Runnable::run;
    }

}
