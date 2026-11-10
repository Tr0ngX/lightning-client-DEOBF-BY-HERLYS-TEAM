package lightning.modules.impl.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "ESP", description = "Renders a box ESP around entities that you have selected.", category = Module.Category.VISUALS)
public class ESPModule extends Module {
    public BooleanSetting players;
    public BooleanSetting hostiles;
    public BooleanSetting animals;
    public BooleanSetting ambient;
    public BooleanSetting invisibles;
    public BooleanSetting items;
    public BooleanSetting others;
    public BooleanSetting blocks;
    public WhitelistSetting blockWhitelist;
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private List targetEntities;
    private final Map matchedBlocks;
    private final Map pendingChunks;
    private static final int SCAN_DELAY_TICKS = 5;
    private static final int MAX_CHUNK_EVALS_PER_TICK = 2;
    private static boolean chunkListenerRegistered;
    private Predicate blockPredicate;
    private String lastWhitelistSignature;
    private boolean lastBlocksEnabled;

    public ESPModule() {
        this.players = new BooleanSetting("Players", "Renders the box ESP on player entities.", true);
        this.hostiles = new BooleanSetting("Hostiles", "Renders the box ESP on hostile entities.", true);
        this.animals = new BooleanSetting("Animals", "Renders the box ESP on animal entities.", true);
        this.ambient = new BooleanSetting("Ambient", "Renders the box ESP on ambient entities.", false);
        this.invisibles = new BooleanSetting("Invisibles", "Renders the box ESP on invisible entities.", true);
        this.items = new BooleanSetting("Items", "Renders the box ESP on item entities.", true);
        this.others = new BooleanSetting("Others", "Renders the box ESP on miscellaneous entities.", false);
        this.blocks = new BooleanSetting("Block", "Renders a box ESP on whitelisted blocks (portals, ores, ...).", false);
        this.blockWhitelist = new WhitelistSetting("Whitelist", "Blocks to highlight.", new BooleanSetting.Visibility(this.blocks, true), WhitelistSetting.Type.BLOCKS);
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "Fill";
        tmp0[2] = "Outline";
        tmp0[3] = "Both";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the target entities.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.targetEntities = new ArrayList();
        this.matchedBlocks = new HashMap();
        this.pendingChunks = new HashMap();
        this.blockPredicate = (arg0) -> false;
        this.lastWhitelistSignature = "";
        this.lastBlocksEnabled = false;
        registerChunkListener();
    }

    private void registerChunkListener() {
        if (!(!chunkListenerRegistered)) return;
        chunkListenerRegistered = true;
        try {
            Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents");
            ESPModule.FabricChunkEventsHelper.register();
        } catch (Throwable ex3) {
            Object var1 = ex3;
            LightningClient.LOGGER.warn("Fabric Chunk Events API not available or optional: {}", var1.getMessage());
        }
    }

    private static long chunkKey(int arg0, int arg1) {
        return ((((long) arg0) & 4294967295L) | (((long) arg1) << 32));
    }

    private void queueChunk(int arg0, int arg1) {
        this.pendingChunks.putIfAbsent(chunkKey(arg0, arg1), 5);
    }

    public void onEnable() {
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        this.lastBlocksEnabled = this.blocks.getValue();
        if (!(this.blocks.getValue())) return;
        rebuildPredicate();
    }

    public void onDisable() {
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        this.lastBlocksEnabled = false;
    }

    private String whitelistSignature() {
        return String.join(",", this.blockWhitelist.getWhitelistIds());
    }

    private void rebuildPredicate() {
        this.lastWhitelistSignature = whitelistSignature();
        this.blockPredicate = () -> {
    if (arg0.isAir()) break label_0;
    return this.blockWhitelist.isWhitelistContains(arg0.getBlock());
};
        this.matchedBlocks.clear();
        this.pendingChunks.clear();
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        int var1 = (mc.player.blockPosition().getX() >> 4);
        int var2 = (mc.player.blockPosition().getZ() >> 4);
        int var3 = mc.options.getEffectiveRenderDistance();
        int var4 = -(var3);
        while (var4 <= var3) {
            int var5 = -(var3);
            while (var5 <= var3) {
                int var6 = (var1 + var4);
                int var7 = (var2 + var5);
                if (!(mc.level.hasChunk(var6, var7))) {
                } else {
                    queueChunk(var6, var7);
                }
                var5++;
            }
            var4++;
        }
    }

    @SubscribeEvent
    public void onBlockScanTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.blocks.getValue())) {
            if (!(!this.lastBlocksEnabled)) {
                this.lastBlocksEnabled = false;
                this.matchedBlocks.clear();
                this.pendingChunks.clear();
            }
            return;
        }
        if (!this.lastBlocksEnabled) {
            this.lastBlocksEnabled = true;
            rebuildPredicate();
            return;
        }
        if (!(whitelistSignature().equals(this.lastWhitelistSignature))) {
            rebuildPredicate();
            return;
        }
        if (this.pendingChunks.isEmpty()) return;
        int var2 = 2;
        Object var3 = this.pendingChunks.entrySet().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Map.Entry) var3.next());
            int var5 = (((Integer) var4.getValue()).intValue() - 1);
            if (var5 > 0) {
                var4.setValue(var5);
                break label_7;
            }
            if (!var2) {
                var4.setValue(1);
                break label_7;
            }
            var2--;
            var3.remove();
            long var6 = ((Long) var4.getKey()).longValue();
            int var8 = ((int) var6);
            int var9 = ((int) (var6 >>> 32));
            evaluateChunk(var8, var9, var6);
        }
    }

    private void evaluateChunk(int arg0, int arg1, long arg2) {
        if ((mc.level == null) || (!(mc.level.hasChunk(arg0, arg1)))) {
            return;
        }
        Object var5 = mc.level.getChunk(arg0, arg1);
        Object var6 = new ArrayList();
        Object var7 = var5.getSections();
        int var8 = mc.level.getMinSectionY();
        int var9 = 0;
        while (var9 < var7.length) {
            Object var10 = var7[var9];
            if ((var10 == null) || (var10.hasOnlyAir())) break label_11;
            if (!(var10.maybeHas(this.blockPredicate))) {
                break label_11;
            }
            int var11 = ((var8 + var9) << 4);
            int var12 = (arg0 << 4);
            int var13 = (arg1 << 4);
            int var14 = 0;
            while (var14 < 16) {
                int var15 = 0;
                while (var15 < 16) {
                    int var16 = 0;
                    while (var16 < 16) {
                        Object var17 = var10.getBlockState(var14, var15, var16);
                        if (!(this.blockPredicate.test(var17))) {
                        } else {
                            var6.add(new BlockPos((var12 + var14), (var11 + var15), (var13 + var16)));
                        }
                        var16++;
                    }
                    var15++;
                }
                var14++;
            }
            label_11:
            var9++;
        }
        if (var6.isEmpty()) {
            this.matchedBlocks.remove(arg2);
        } else {
            this.matchedBlocks.put(arg2, var6);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.level == null) return;
        Object var2 = new ArrayList();
        Object var3 = mc.level.entitiesForRendering().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Entity) var3.next());
            if (var4 == mc.player) break label_1;
            if (!(isValidEntity(var4))) {
                break label_1;
            }
            var2.add(var4);
        }
        this.targetEntities = var2;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (mc.level == null) return;
        if (this.targetEntities.isEmpty()) break label_5;
        Object var2 = this.targetEntities.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Entity) var2.next());
            Object var4 = EntityUtils.getRenderPos(var3, arg0.getTickDelta());
            Object var5 = new AABB((var4.x - (var3.getBoundingBox().getXsize() / 2.0)), var4.y, (var4.z - (var3.getBoundingBox().getZsize() / 2.0)), (var4.x + (var3.getBoundingBox().getXsize() / 2.0)), (var4.y + var3.getBoundingBox().getYsize()), (var4.z + (var3.getBoundingBox().getZsize() / 2.0)));
            if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
                if (this.mode.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBox(arg0.getMatrices(), var5, this.fillColor.getColor());
            }
            if (!(this.mode.getValue().equalsIgnoreCase("Outline")) && !(this.mode.getValue().equalsIgnoreCase("Both"))) {
                break label_1;
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var5, this.outlineColor.getColor());
        }
        label_5:
        renderBlockTargets(arg0);
    }

    private void renderBlockTargets(RenderWorldEvent arg0) {
        if (((mc.level == null) || (!(this.blocks.getValue()))) || (this.matchedBlocks.isEmpty())) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("None")) return;
        if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var3 = 0;
        if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var2 = 0;
        int var4 = 0;
        if (!var3 && !var2) return;
        Object var5 = this.matchedBlocks.values().iterator();
        while (var5.hasNext()) {
            Object var6 = ((List) var5.next());
            Object var7 = var6.iterator();
            while (var7.hasNext()) {
                Object var8 = ((BlockPos) var7.next());
                Object var9 = mc.level.getBlockState(var8);
                if (!(this.blockPredicate.test(var9))) {
                    break label_11;
                }
                Object var10 = var9.getShape(mc.level, var8);
                if (var10.isEmpty()) {
                } else {
                }
                Object var11 = var10.toAabbs().stream().map((arg1) -> arg1.move(arg0)).toList();
                Object var12 = var11.iterator();
                while (var12.hasNext()) {
                    Object var13 = ((AABB) var12.next());
                    if (!(!var3)) {
                        Renderer3D.renderBox(arg0.getMatrices(), var13, this.fillColor.getColor());
                    }
                    if (!var2) {
                        break label_15;
                    }
                    Renderer3D.renderBoxOutline(arg0.getMatrices(), var13, this.outlineColor.getColor());
                }
            }
        }
    }

    private boolean isValidEntity(Entity arg0) {
        if (this.players.getValue() && arg0.getType() == EntityTypes.PLAYER) return true;
        if (this.hostiles.getValue() && EntityUtils.isHostile(arg0)) return true;
        if (this.animals.getValue() && EntityUtils.isAnimal(arg0) && !(EntityUtils.isHostile(arg0))) return true;
        if (this.ambient.getValue() && arg0.getType().getCategory() == MobCategory.AMBIENT) return true;
        if (this.invisibles.getValue() && arg0.isInvisible()) return true;
        if (this.items.getValue()) {
            if ((arg0.getType() == EntityTypes.ITEM) || (arg0.getType() == EntityTypes.EXPERIENCE_BOTTLE)) {
                return true;
            }
        }
        return this.others.getValue();
    }

    static {
        chunkListenerRegistered = false;
    }


    class FabricChunkEventsHelper {
        static void register() {
            ClientChunkEvents.CHUNK_LOAD.register((arg0, arg1) -> {
    if (LightningClient.MODULE_MANAGER != null) {
    } else {
    }
    Object var2 = null;
    Object var3 = null;
    if (((var2 == null) || (!(var2.isToggled()))) || (!(var2.blocks.getValue()))) {
        return;
    }
    if (Minecraft.getInstance().level != arg0) return;
    var2.queueChunk(arg1.getPos().x(), arg1.getPos().z());
});
            ClientChunkEvents.CHUNK_UNLOAD.register((arg0, arg1) -> {
    if (LightningClient.MODULE_MANAGER != null) {
    } else {
    }
    Object var2 = null;
    Object var3 = null;
    if (var2 == null) return;
    long var4 = ESPModule.chunkKey(arg1.getPos().x(), arg1.getPos().z());
    var2.matchedBlocks.remove(var4);
    var2.pendingChunks.remove(var4);
});
        }

    }
}
