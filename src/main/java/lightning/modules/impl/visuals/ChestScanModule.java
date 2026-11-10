package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.chestscan.ChestScanChain;
import lightning.modules.impl.visuals.chestscan.ChestScanStore;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@RegisterModule(name = "ChestScan", description = "Highlights opened chests by contents (empty/partial/full), with optional hopper-chain inference.", category = Module.Category.VISUALS)
public class ChestScanModule extends Module {
    public NumberSetting scanRadius;
    public BooleanSetting hopperChain;
    public ModeSetting renderMode;
    public CategorySetting emptyCategory;
    public ColorSetting emptyFillColor;
    public ColorSetting emptyOutlineColor;
    public CategorySetting partialCategory;
    public ColorSetting partialFillColor;
    public ColorSetting partialOutlineColor;
    public CategorySetting fullCategory;
    public ColorSetting fullFillColor;
    public ColorSetting fullOutlineColor;
    private final ChestScanStore store;
    private String lastWorldKey;
    private BlockPos lastLookedAtChestPos;
    private boolean wasChestMenuOpenLastTick;
    private BlockPos openChestPos;
    private ChestScanStore.ChestStatus lastSnapshotStatus;
    private int chainTicks;
    private Set lastInferredEmpty;
    private final Map missingSinceMs;
    private static final long PRUNE_GRACE_MS = 1500L;

    public ChestScanModule() {
        this.scanRadius = new NumberSetting("ScanRadius", "How far (in blocks) to render tracked chests and consider hopper chains.", 64.0, 8.0, 128.0);
        this.hopperChain = new BooleanSetting("HopperChain", "Smart mode to check chests linked to the bottom chest by hoppers.", false);
        var tmp0 = new String[3];
        tmp0[0] = "Both";
        tmp0[1] = "Fill";
        tmp0[2] = "Outline";
        this.renderMode = new ModeSetting("Mode", "Render mode for chests.", "Both", tmp0);
        this.emptyCategory = new CategorySetting("Empty", "Color for empty chests.");
        this.emptyFillColor = new ColorSetting("EmptyFillColor", "Fill", "Fill color for empty chests.", new CategorySetting.Visibility(this.emptyCategory), new ColorSetting.Color(new Color(0, 200, 0, 120), false, false));
        this.emptyOutlineColor = new ColorSetting("EmptyOutlineColor", "Outline", "Outline color for empty chests.", new CategorySetting.Visibility(this.emptyCategory), new ColorSetting.Color(new Color(0, 200, 0, 255), false, false));
        this.partialCategory = new CategorySetting("Partial", "Color for partially filled chests.");
        this.partialFillColor = new ColorSetting("PartialFillColor", "Fill", "Fill color for partially filled chests.", new CategorySetting.Visibility(this.partialCategory), new ColorSetting.Color(new Color(230, 200, 0, 120), false, false));
        this.partialOutlineColor = new ColorSetting("PartialOutlineColor", "Outline", "Outline color for partially filled chests.", new CategorySetting.Visibility(this.partialCategory), new ColorSetting.Color(new Color(230, 200, 0, 255), false, false));
        this.fullCategory = new CategorySetting("Full", "Color for full chests.");
        this.fullFillColor = new ColorSetting("FullFillColor", "Fill", "Fill color for full chests.", new CategorySetting.Visibility(this.fullCategory), new ColorSetting.Color(new Color(220, 0, 0, 120), false, false));
        this.fullOutlineColor = new ColorSetting("FullOutlineColor", "Outline", "Outline color for full chests.", new CategorySetting.Visibility(this.fullCategory), new ColorSetting.Color(new Color(220, 0, 0, 255), false, false));
        this.store = new ChestScanStore();
        this.lastWorldKey = null;
        this.lastLookedAtChestPos = null;
        this.wasChestMenuOpenLastTick = false;
        this.openChestPos = null;
        this.lastSnapshotStatus = null;
        this.chainTicks = 0;
        this.lastInferredEmpty = Collections.emptySet();
        this.missingSinceMs = new HashMap();
    }

    public ChestScanStore getStore() {
        return this.store;
    }

    public void onEnable() {
        this.store.loadForWorld();
        this.lastWorldKey = ChestScanStore.currentWorldKey();
    }

    public void onDisable() {
        if (this.openChestPos != null && this.lastSnapshotStatus != null) {
            finalizeChestState(mc, this.openChestPos, this.lastSnapshotStatus);
        }
        this.lastWorldKey = null;
        this.lastLookedAtChestPos = null;
        this.wasChestMenuOpenLastTick = false;
        this.openChestPos = null;
        this.lastSnapshotStatus = null;
        this.missingSinceMs.clear();
    }

    private void maybeReloadStoreForWorld() {
        Object var1 = ChestScanStore.currentWorldKey();
        if (!(var1.equals(this.lastWorldKey))) {
            this.store.loadForWorld();
            this.lastWorldKey = var1;
            this.missingSinceMs.clear();
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        maybeReloadStoreForWorld();
        int var2 = (mc.player.containerMenu instanceof ChestMenu);
        if (!var2 && mc.gui.screen() == null) {
            this.lastLookedAtChestPos = resolveLookedAtChestPos(mc);
        }
        if (!(!var2) && !this.wasChestMenuOpenLastTick) {
            this.openChestPos = this.lastLookedAtChestPos;
            this.lastSnapshotStatus = null;
        }
        if (!(!var2)) {
            Object var3 = ((ChestMenu) mc.player.containerMenu);
            this.lastSnapshotStatus = computeStatus(var3.getContainer());
        }
        if (!var2 && !(!this.wasChestMenuOpenLastTick)) {
            finalizeChestState(mc, this.openChestPos, this.lastSnapshotStatus);
            this.openChestPos = null;
            this.lastSnapshotStatus = null;
        }
        this.wasChestMenuOpenLastTick = var2;
        tickChainRecompute(mc);
    }

    private void tickChainRecompute(Minecraft arg0) {
        if (!(this.hopperChain.getValue())) {
            this.lastInferredEmpty = Collections.emptySet();
            return;
        }
        this.chainTicks = (this.chainTicks + 1);
        if (this.chainTicks < 20) return;
        this.chainTicks = 0;
        Object var2 = arg0.player.blockPosition();
        int var3 = this.scanRadius.getValue().intValue();
        Object var4 = new HashSet(this.store.positions());
        Object var5 = ChestScanChain.findEdges(arg0.level, var4, var2, var3);
        Object var6 = new HashMap();
        Object var7 = var4.iterator();
        while (var7.hasNext()) {
            Object var8 = ((BlockPos) var7.next());
            var6.put(var8, this.store.get(var8));
        }
        this.lastInferredEmpty = ChestScanChain.inferEmpty(var5, var6);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        double var2 = this.scanRadius.getValue().doubleValue();
        double var4 = (var2 * var2);
        Object var6 = mc.player.blockPosition();
        if (this.renderMode.getValue().equalsIgnoreCase("Fill")) break label_2;
        if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            label_2:
        } else {
        }
        int var7 = 0;
        if (!(this.renderMode.getValue().equalsIgnoreCase("Outline"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        int var8 = 0;
        Object var9 = new HashSet();
        Object var10 = new ArrayList(this.store.positions()).iterator();
        while (var10.hasNext()) {
            Object var11 = ((BlockPos) var10.next());
            if (var6.distSqr(var11) > var4) break label_8;
            if (!(mc.level.hasChunk((var11.getX() >> 4), (var11.getZ() >> 4)))) {
                break label_8;
            }
            if (!((mc.level.getBlockState(var11).getBlock() instanceof ChestBlock))) {
                long var13 = System.currentTimeMillis();
                Object var15 = ((Long) this.missingSinceMs.putIfAbsent(var11, var13));
                if (var15 == null) break label_8;
                if ((var13 - var15.longValue()) < 1500L) {
                    break label_8;
                }
                this.store.remove(var11);
                this.missingSinceMs.remove(var11);
                break label_8;
            }
            this.missingSinceMs.remove(var11);
            if (var9.contains(var11)) break label_8;
            var tmp0 = this.store.get(var11);
            Object var12 = tmp0;
            if (tmp0 == null) {
                break label_8;
            }
            var13 = getChestBox(var11, var9);
            Object var14 = getFillColor(var12);
            var15 = getOutlineColor(var12);
            if (!(!var7)) {
                Renderer3D.renderBox(arg0.getMatrices(), var13, var14);
            }
            if (!var8) {
                break label_8;
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var13, var15);
        }
        if (!(this.hopperChain.getValue())) break label_20;
        var10 = this.emptyFillColor.getColor();
        var11 = this.emptyOutlineColor.getColor();
        var12 = this.lastInferredEmpty.iterator();
        while (var12.hasNext()) {
            var13 = ((BlockPos) var12.next());
            if ((((this.store.get(var13) != null) || (var6.distSqr(var13) > var4)) || (!(mc.level.hasChunk((var13.getX() >> 4), (var13.getZ() >> 4))))) || (var9.contains(var13))) break label_16;
            var tmp1 = mc.level.getBlockState(var13);
            var14 = tmp1;
            if (!((tmp1.getBlock() instanceof ChestBlock))) {
                break label_16;
            }
            var15 = getChestBox(var13, var9);
            if (!(!var7)) {
                Renderer3D.renderBox(arg0.getMatrices(), var15, var10);
            }
            if (!var8) {
                break label_16;
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var15, var11);
        }
        label_20:
    }

    private AABB getChestBox(BlockPos arg0, Set arg1) {
        arg1.add(arg0);
        Object var3 = mc.level.getBlockState(arg0);
        if (((!((var3.getBlock() instanceof ChestBlock))) || (!(var3.hasProperty(ChestBlock.TYPE)))) || (var3.getValue(ChestBlock.TYPE) == ChestType.SINGLE)) break label_2;
        Object var4 = ((Direction) var3.getValue(ChestBlock.FACING));
        Object var5 = ((ChestType) var3.getValue(ChestBlock.TYPE));
        if (var5 == ChestType.LEFT) {
        } else {
        }
        Object var6 = var4.getCounterClockWise();
        Object var7 = arg0.relative(var6);
        Object var8 = mc.level.getBlockState(var7);
        if (((!((var8.getBlock() instanceof ChestBlock))) || (!(var8.hasProperty(ChestBlock.TYPE)))) || (var8.getValue(ChestBlock.TYPE) == ChestType.SINGLE)) break label_2;
        arg1.add(var7);
        return new AABB(((double) Math.min(arg0.getX(), var7.getX())), ((double) Math.min(arg0.getY(), var7.getY())), ((double) Math.min(arg0.getZ(), var7.getZ())), (((double) Math.max(arg0.getX(), var7.getX())) + 1.0), (((double) Math.max(arg0.getY(), var7.getY())) + 1.0), (((double) Math.max(arg0.getZ(), var7.getZ())) + 1.0));
        label_2:
        return new AABB(arg0);
    }

    private Color getFillColor(ChestScanStore.ChestStatus arg0) {
        switch (ChestScanModule.1.$SwitchMap$night$modules$impl$visuals$chestscan$ChestScanStore$ChestStatus[arg0.ordinal()]) {
            case 1 -> { break label_1; }
            case 2 -> { break label_2; }
            case 3 -> { break label_3; }
            default -> { break label_0; }
        }
        label_0:
        throw new MatchException(null, null);
        label_1:
        break label_4;
        label_2:
        break label_4;
        label_3:
        label_4:
        return this.fullFillColor.getColor();
    }

    private Color getOutlineColor(ChestScanStore.ChestStatus arg0) {
        switch (ChestScanModule.1.$SwitchMap$night$modules$impl$visuals$chestscan$ChestScanStore$ChestStatus[arg0.ordinal()]) {
            case 1 -> { break label_1; }
            case 2 -> { break label_2; }
            case 3 -> { break label_3; }
            default -> { break label_0; }
        }
        label_0:
        throw new MatchException(null, null);
        label_1:
        break label_4;
        label_2:
        break label_4;
        label_3:
        label_4:
        return this.fullOutlineColor.getColor();
    }

    private BlockPos resolveLookedAtChestPos(Minecraft arg0) {
        Object var3 = arg0.hitResult;
        if ((var3 instanceof BlockHitResult)) {
            var tmp0 = ((BlockHitResult) var3);
            Object var2 = tmp0;
            if (tmp0.getType() != HitResult.Type.BLOCK) {
            }
            return null;
        }
        Object var4 = var2.getBlockPos();
        return ((arg0.level.getBlockState(var4).getBlock() instanceof ChestBlock) ? var4 : null);
    }

    private ChestScanStore.ChestStatus computeStatus(Container arg0) {
        int var2 = arg0.getContainerSize();
        if (!var2) return ChestScanStore.ChestStatus.EMPTY;
        int var3 = 0;
        int var4 = 0;
        while (var4 < var2) {
            if (arg0.getItem(var4).isEmpty()) {
            } else {
                var3++;
            }
            var4++;
        }
        if (!var3) return ChestScanStore.ChestStatus.EMPTY;
        return (var3 == var2 ? ChestScanStore.ChestStatus.FULL : ChestScanStore.ChestStatus.PARTIAL);
    }

    private void finalizeChestState(Minecraft arg0, BlockPos arg1, ChestScanStore.ChestStatus arg2) {
        if ((arg1 == null) || (arg2 == null)) {
            return;
        }
        this.store.put(arg1, arg2);
        if ((arg0 == null) || (arg0.level == null)) {
            return;
        }
        Object var4 = arg0.level.getBlockState(arg1);
        if (((!((var4.getBlock() instanceof ChestBlock))) || (!(var4.hasProperty(ChestBlock.TYPE)))) || (var4.getValue(ChestBlock.TYPE) == ChestType.SINGLE)) break label_6;
        Object var5 = ((Direction) var4.getValue(ChestBlock.FACING));
        Object var6 = ((ChestType) var4.getValue(ChestBlock.TYPE));
        if (var6 == ChestType.LEFT) {
        } else {
        }
        Object var7 = var5.getCounterClockWise();
        Object var8 = arg1.relative(var7);
        if (!((arg0.level.getBlockState(var8).getBlock() instanceof ChestBlock))) break label_6;
        this.store.put(var8, arg2);
        label_6:
    }


    class 1 {

        static {
            $SwitchMap$night$modules$impl$visuals$chestscan$ChestScanStore$ChestStatus = new int[ChestScanStore.ChestStatus.values().length];
            try {
                $SwitchMap$night$modules$impl$visuals$chestscan$ChestScanStore$ChestStatus[ChestScanStore.ChestStatus.EMPTY.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$modules$impl$visuals$chestscan$ChestScanStore$ChestStatus[ChestScanStore.ChestStatus.PARTIAL.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$night$modules$impl$visuals$chestscan$ChestScanStore$ChestStatus[ChestScanStore.ChestStatus.FULL.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
            }
        }

    }
}
