package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.stashfinder.StashFinderStore;
import lightning.modules.impl.visuals.stashfinder.StashWebhook;
import lightning.modules.impl.visuals.stashfinder.StructureSignature;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.graphics.Renderer3D;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "StashFinder", description = "Scans loaded chunks for loot stashes and storage animals, alerting via chat and Discord webhook.", category = Module.Category.VISUALS)
public class StashFinderModule extends Module {
    public CategorySetting containersCategory;
    public NumberSetting minChests;
    public NumberSetting minBarrels;
    public NumberSetting minShulkers;
    public NumberSetting minEnderChests;
    public NumberSetting minHoppers;
    public NumberSetting minDispensersDroppers;
    public NumberSetting minFurnaces;
    public NumberSetting minCrafters;
    public CategorySetting animalsCategory;
    public NumberSetting minDonkeys;
    public NumberSetting minLlamas;
    public NumberSetting minChestBoats;
    public CategorySetting generalCategory;
    public BooleanSetting ignoreNatural;
    public BooleanSetting chatNotify;
    public BooleanSetting renderHighlight;
    public ColorSetting highlightColor;
    public CategorySetting webhookCategory;
    public BooleanSetting sendWebhook;
    public StringSetting webhookUrl;
    public StringSetting userId;
    private final StashFinderStore store;
    private String lastWorldKey;
    private final Map pending;
    private static final int SCAN_DELAY_TICKS = 25;
    private static final int MAX_EVALS_PER_TICK = 4;
    private static final int BOOTSTRAP_RADIUS_CHUNKS = 12;
    private static boolean listenerRegistered;

    public StashFinderModule() {
        this.containersCategory = new CategorySetting("Containers", "Threshold settings for containers.");
        this.minChests = new NumberSetting("Chests", "Minimum chests required.", new CategorySetting.Visibility(this.containersCategory), 10, 1, 64);
        this.minBarrels = new NumberSetting("Barrels", "Minimum barrels required.", new CategorySetting.Visibility(this.containersCategory), 10, 1, 64);
        this.minShulkers = new NumberSetting("Shulkers", "Minimum shulker boxes required.", new CategorySetting.Visibility(this.containersCategory), 4, 1, 64);
        this.minEnderChests = new NumberSetting("EnderChests", "Minimum ender chests required.", new CategorySetting.Visibility(this.containersCategory), 2, 1, 64);
        this.minHoppers = new NumberSetting("Hoppers", "Minimum hoppers required.", new CategorySetting.Visibility(this.containersCategory), 10, 1, 64);
        this.minDispensersDroppers = new NumberSetting("Dispensers/Droppers", "Minimum dispensers or droppers required.", new CategorySetting.Visibility(this.containersCategory), 10, 1, 64);
        this.minFurnaces = new NumberSetting("Furnaces", "Minimum furnaces required.", new CategorySetting.Visibility(this.containersCategory), 10, 1, 64);
        this.minCrafters = new NumberSetting("Crafters", "Minimum crafters required.", new CategorySetting.Visibility(this.containersCategory), 4, 1, 64);
        this.animalsCategory = new CategorySetting("Animals", "Threshold settings for storage animals.");
        this.minDonkeys = new NumberSetting("Donkeys", "Minimum donkeys required.", new CategorySetting.Visibility(this.animalsCategory), 2, 1, 16);
        this.minLlamas = new NumberSetting("Llamas", "Minimum llamas required.", new CategorySetting.Visibility(this.animalsCategory), 2, 1, 16);
        this.minChestBoats = new NumberSetting("ChestBoats", "Minimum chest boats required.", new CategorySetting.Visibility(this.animalsCategory), 1, 1, 16);
        this.generalCategory = new CategorySetting("General", "General settings.");
        this.ignoreNatural = new BooleanSetting("IgnoreNatural", "Ignores naturally generated structures.", new CategorySetting.Visibility(this.generalCategory), true);
        this.chatNotify = new BooleanSetting("ChatNotify", "Notifies in chat when a stash is found.", new CategorySetting.Visibility(this.generalCategory), true);
        this.renderHighlight = new BooleanSetting("RenderHighlight", "Highlights found stash chunks in the world.", new CategorySetting.Visibility(this.generalCategory), true);
        this.highlightColor = new ColorSetting("HighlightColor", "Highlight color for stash chunks.", new CategorySetting.Visibility(this.generalCategory), new ColorSetting.Color(new Color(0, 230, 120, 100), false, false));
        this.webhookCategory = new CategorySetting("Webhook", "Discord webhook settings.");
        this.sendWebhook = new BooleanSetting("SendWebhook", "Sends stash alerts to Discord webhook.", new CategorySetting.Visibility(this.webhookCategory), true);
        this.webhookUrl = new StringSetting("WebhookURL", "Discord webhook URL.", new CategorySetting.Visibility(this.webhookCategory), "");
        this.userId = new StringSetting("UserID", "Discord User ID to ping.", new CategorySetting.Visibility(this.webhookCategory), "");
        this.store = new StashFinderStore();
        this.lastWorldKey = null;
        this.pending = new HashMap();
        registerChunkListener();
    }

    private void registerChunkListener() {
        if (!(!listenerRegistered)) return;
        listenerRegistered = true;
        try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            StashFinderModule.FabricChunkEventsHelper.register();
        } catch (Throwable ex3) {
            Object var1 = ex3;
            LightningClient.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", var1.getMessage());
        }
    }

    public void onEnable() {
        this.pending.clear();
        this.store.loadForWorld();
        this.lastWorldKey = StashFinderStore.currentWorldKey();
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        int var1 = (mc.player.blockPosition().getX() >> 4);
        int var2 = (mc.player.blockPosition().getZ() >> 4);
        int var3 = -12;
        while (var3 <= 12) {
            int var4 = -12;
            while (var4 <= 12) {
                int var7 = (var1 + var3);
                int var8 = (var2 + var4);
                if (!(mc.level.hasChunk(var7, var8))) break label_5;
                long var5 = StashFinderStore.chunkKey(var7, var8);
                if (this.store.isEvaluated(StashFinderStore.chunkKey(var7, var8))) {
                    break label_5;
                }
                this.pending.putIfAbsent(var5, 25);
                label_5:
                var4++;
            }
            var3++;
        }
    }

    public void onDisable() {
        this.store.flush();
        this.pending.clear();
    }

    private void maybeReloadStoreForWorld() {
        Object var1 = StashFinderStore.currentWorldKey();
        if (!(var1.equals(this.lastWorldKey))) {
            this.store.loadForWorld();
            this.lastWorldKey = var1;
            this.pending.clear();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        maybeReloadStoreForWorld();
        this.store.flushIfStale();
        if (this.pending.isEmpty()) return;
        int var2 = 4;
        Object var3 = this.pending.entrySet().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Map.Entry) var3.next());
            int var5 = (((Integer) var4.getValue()).intValue() - 1);
            if (var5 > 0) {
                var4.setValue(var5);
                break label_3;
            }
            if (!var2) {
                var4.setValue(1);
                break label_3;
            }
            var2--;
            var3.remove();
            long var6 = ((Long) var4.getKey()).longValue();
            int var8 = ((int) (var6 & 4294967295L));
            int var9 = ((int) (var6 >>> 32));
            evaluateChunk(var8, var9, var6);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((!(this.renderHighlight.getValue())) || (mc.level == null)) {
            return;
        }
        Object var2 = this.highlightColor.getColor();
        Object var3 = new Color(var2.getRed(), var2.getGreen(), var2.getBlue(), 255);
        Object var4 = this.store.getStashes().iterator();
        while (var4.hasNext()) {
            Object var5 = ((StashFinderStore.StashData) var4.next());
            Object var6 = new AABB(((double) var5.x()), ((double) mc.level.getMinY()), ((double) var5.z()), ((double) (var5.x() + 16)), ((double) mc.level.getMaxY()), ((double) (var5.z() + 16)));
            Renderer3D.renderBox(arg0.getMatrices(), var6, var2);
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var6, var3);
        }
    }

    public void resetCurrentWorldStore() {
        this.store.loadForWorld();
        this.store.clearAll();
        this.pending.clear();
        this.lastWorldKey = StashFinderStore.currentWorldKey();
    }

    private void evaluateChunk(int arg0, int arg1, long arg2) {
        if ((mc.level == null) || (!(mc.level.hasChunk(arg0, arg1)))) {
            return;
        }
        Object var5 = mc.level.getChunk(arg0, arg1);
        this.store.markEvaluated(arg2);
        Object var6 = countContainers(var5);
        var6.putAll(countAnimals(var5));
        if (!(meetsAnyThreshold(var6))) return;
        if (this.ignoreNatural.getValue() && StructureSignature.isNatural(mc, var5, var6)) return;
        Object var7 = new LinkedHashMap();
        Object var8 = var6.entrySet().iterator();
        while (var8.hasNext()) {
            Object var9 = ((Map.Entry) var8.next());
            if (((Integer) var9.getValue()).intValue() <= 0) {
                break label_4;
            }
            var7.put(((String) var9.getKey()), ((Integer) var9.getValue()));
        }
        var8 = var5.getPos();
        var9 = var8.getMinBlockX();
        int var10 = var8.getMinBlockZ();
        Object var11 = mc.level.dimension().identifier().toString();
        this.store.recordStash(var9, var10, var11, var7);
        if (this.chatNotify.getValue()) {
            Object var12 = new StringBuilder();
            var12.append("Stash found at X: ").append(var9).append(" Z: ").append(var10).append(" (");
            int var13 = 0;
            Object var14 = var7.entrySet().iterator();
            while (var14.hasNext()) {
                Object var15 = ((Map.Entry) var14.next());
                var13++;
                if (var13 > 0) {
                    var12.append(", ");
                }
                var12.append(((String) var15.getKey())).append(": ").append(var15.getValue());
            }
            var12.append(")");
            LightningClient.CHAT_MANAGER.tagged(var12.toString(), "StashFinder");
        }
        if (this.sendWebhook.getValue()) {
            var12 = this.webhookUrl.getValue();
            var13 = this.userId.getValue();
            StashWebhook.send(var9, var10, var11, var7, var12, var13);
        }
    }

    private boolean meetsAnyThreshold(Map arg0) {
        if (!(check(arg0, "Chests", this.minChests)) && !(check(arg0, "Barrels", this.minBarrels)) && !(check(arg0, "Shulkers", this.minShulkers)) && !(check(arg0, "Ender Chests", this.minEnderChests)) && !(check(arg0, "Hoppers", this.minHoppers)) && !(check(arg0, "Dispensers/Droppers", this.minDispensersDroppers)) && !(check(arg0, "Furnaces", this.minFurnaces)) && !(check(arg0, "Crafters", this.minCrafters)) && !(check(arg0, "Donkey", this.minDonkeys)) && !(check(arg0, "Llama", this.minLlamas))) {
            if (check(arg0, "Chest Boat", this.minChestBoats)) {
            }
        } else {
        }
        return false;
    }

    private boolean check(Map arg0, String arg1, NumberSetting arg2) {
        int var4 = arg2.getValue().intValue();
        if (var4 <= 0) return false;
        return ((Integer) arg0.getOrDefault(arg1, 0)).intValue() >= var4;
    }

    private Map countContainers(LevelChunk arg0) {
        Object var2 = new LinkedHashMap();
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;
        int var6 = 0;
        int var7 = 0;
        int var8 = 0;
        int var9 = 0;
        int var10 = 0;
        Object var11 = arg0.getBlockEntities().values().iterator();
        while (var11.hasNext()) {
            Object var12 = ((BlockEntity) var11.next());
            if ((var12 instanceof ChestBlockEntity)) break label_1;
            if ((var12 instanceof TrappedChestBlockEntity)) {
                label_1:
                var3++;
                break label_0;
            }
            if ((var12 instanceof BarrelBlockEntity)) {
                var4++;
                break label_0;
            }
            if ((var12 instanceof ShulkerBoxBlockEntity)) {
                var5++;
                break label_0;
            }
            if ((var12 instanceof EnderChestBlockEntity)) {
                var6++;
                break label_0;
            }
            if ((var12 instanceof HopperBlockEntity)) {
                var7++;
                break label_0;
            }
            if (!((var12 instanceof DispenserBlockEntity))) {
                if ((var12 instanceof DropperBlockEntity)) {
                }
                var8++;
                break label_0;
            }
            if (!(var12 instanceof FurnaceBlockEntity) && !(var12 instanceof BlastFurnaceBlockEntity)) {
                if ((var12 instanceof SmokerBlockEntity)) {
                }
                var9++;
                break label_0;
            }
            if (!((var12 instanceof CrafterBlockEntity))) {
                break label_0;
            }
            var10++;
        }
        var2.put("Chests", var3);
        var2.put("Barrels", var4);
        var2.put("Shulkers", var5);
        var2.put("Ender Chests", var6);
        var2.put("Hoppers", var7);
        var2.put("Dispensers/Droppers", var8);
        var2.put("Furnaces", var9);
        var2.put("Crafters", var10);
        return var2;
    }

    private Map countAnimals(LevelChunk arg0) {
        Object var2 = new LinkedHashMap();
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;
        if (mc.level == null) return var2;
        Object var6 = arg0.getPos();
        Object var7 = new AABB(((double) var6.getMinBlockX()), ((double) mc.level.getMinY()), ((double) var6.getMinBlockZ()), ((double) (var6.getMaxBlockX() + 1)), ((double) mc.level.getMaxY()), ((double) (var6.getMaxBlockZ() + 1)));
        Object var8 = mc.level.getEntities(((Entity) null), var7, (arg0) -> true).iterator();
        while (var8.hasNext()) {
            Object var9 = ((Entity) var8.next());
            if ((var9 instanceof Donkey)) {
                var3++;
                break label_1;
            }
            if ((var9 instanceof Llama)) {
                var4++;
                break label_1;
            }
            if (!((var9 instanceof ChestBoat))) {
                break label_1;
            }
            var5++;
        }
        var2.put("Donkey", var3);
        var2.put("Llama", var4);
        var2.put("Chest Boat", var5);
        return var2;
    }

    static {
        listenerRegistered = false;
    }


    class FabricChunkEventsHelper {
        static void register() {
            ClientChunkEvents.CHUNK_LOAD.register((arg0, arg1) -> {
    if (LightningClient.MODULE_MANAGER != null) {
    } else {
    }
    Object var2 = null;
    Object var3 = null;
    if ((var2 == null) || (!(var2.isToggled()))) {
        return;
    }
    Object var4 = Minecraft.getInstance();
    if (var4.level != arg0) return;
    long var5 = StashFinderStore.chunkKey(arg1.getPos().x(), arg1.getPos().z());
    if (var2.store.isEvaluated(var5)) return;
    var2.pending.putIfAbsent(var5, 25);
});
        }

    }
}
