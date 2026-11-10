package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "PhaseESP", description = "Renders ESP on blocks which have a safe base.", category = Module.Category.VISUALS)
public class PhaseESPModule extends Module {
    public ModeSetting fill;
    public NumberSetting fillHeight;
    public ModeSetting outline;
    public NumberSetting outlineHeight;
    public CategorySetting bedrockCategory;
    public BooleanSetting bedrock;
    public ColorSetting bedrockFillColor;
    public ColorSetting bedrockOutlineColor;
    public CategorySetting obsidianCategory;
    public BooleanSetting obsidian;
    public ColorSetting obsidianFillColor;
    public ColorSetting obsidianOutlineColor;
    public CategorySetting airCategory;
    public BooleanSetting air;
    public ColorSetting airFillColor;
    public ColorSetting airOutlineColor;
    private ArrayList phaseBlocks;

    public PhaseESPModule() {
        var tmp0 = new String[2];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        this.fill = new ModeSetting("Fill", "The mode for the fill rendering on the phase blocks.", "Normal", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Normal";
        this.fillHeight = new NumberSetting("FillHeight", "The height of the fill rendering on the phase blocks.", new ModeSetting.Visibility(this.fill, tmp1), 1.0, -2.0, 2.0);
        var tmp2 = new String[2];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        this.outline = new ModeSetting("Outline", "The mode for the outline rendering on the phase blocks.", "Normal", tmp2);
        var tmp3 = new String[1];
        tmp3[0] = "Normal";
        this.outlineHeight = new NumberSetting("OutlineHeight", "The height of the outline rendering on the phase blocks.", new ModeSetting.Visibility(this.outline, tmp3), 1.0, -2.0, 2.0);
        this.bedrockCategory = new CategorySetting("Bedrock", "The bedrock base category.");
        this.bedrock = new BooleanSetting("Bedrock", "Enabled", "Renders blocks which have a bedrock base.", new CategorySetting.Visibility(this.bedrockCategory), true);
        this.bedrockFillColor = new ColorSetting("BedrockFillColor", "Fill", "The color for the fill rendering of bedrock base blocks.", new CategorySetting.Visibility(this.bedrockCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.bedrockOutlineColor = new ColorSetting("BedrockOutlineColor", "Outline", "The color for the outline rendering of bedrock base blocks.", new CategorySetting.Visibility(this.bedrockCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.obsidianCategory = new CategorySetting("Obsidian", "The obsidian base category.");
        this.obsidian = new BooleanSetting("Obsidian", "Enabled", "Renders blocks which have a obsidian base.", new CategorySetting.Visibility(this.obsidianCategory), true);
        this.obsidianFillColor = new ColorSetting("ObsidianFillColor", "Fill", "The color for the fill rendering of obsidian base blocks.", new CategorySetting.Visibility(this.obsidianCategory), new ColorSetting.Color(new Color(255, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.obsidianOutlineColor = new ColorSetting("ObsidianOutlineColor", "Outline", "The color for the outline rendering of obsidian base blocks.", new CategorySetting.Visibility(this.obsidianCategory), new ColorSetting.Color(new Color(255, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.airCategory = new CategorySetting("Air", "The air base category.");
        this.air = new BooleanSetting("Air", "Enabled", "Renders blocks which have a air base.", new CategorySetting.Visibility(this.airCategory), true);
        this.airFillColor = new ColorSetting("AirFillColor", "Fill", "The color for the fill rendering of air base blocks.", new CategorySetting.Visibility(this.airCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.airOutlineColor = new ColorSetting("AirOutlineColor", "Outline", "The color for the outline rendering of air base blocks.", new CategorySetting.Visibility(this.airCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.phaseBlocks = new ArrayList();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        Object var2 = new ArrayList();
        Object var3 = PositionUtils.getFlooredPosition(mc.player);
        Object var4 = Direction.values();
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            if (!(var7.getAxis().isHorizontal())) break label_4;
            var tmp0 = var3.relative(var7);
            Object var9 = tmp0;
            if (mc.level.getBlockState(tmp0).getBlock() != Blocks.BEDROCK) {
                if (mc.level.getBlockState(var9).getBlock() != Blocks.OBSIDIAN) break label_4;
            }
            var tmp1 = getPhaseType(var9);
            Object var8 = tmp1;
            if (tmp1 == null) {
                break label_4;
            }
            var2.add(new PhaseESPModule.PhaseBlock(new AABB(var9), var8));
            label_4:
            var6++;
        }
        this.phaseBlocks.clear();
        this.phaseBlocks.addAll(var2);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((getNull()) || (this.phaseBlocks.isEmpty())) {
            return;
        }
        Object var2 = this.phaseBlocks.iterator();
        while (var2.hasNext()) {
            Object var3 = ((PhaseESPModule.PhaseBlock) var2.next());
            Object var4 = new AABB(var3.box().minX, var3.box().minY, var3.box().minZ, var3.box().maxX, (var3.box().minY + this.fillHeight.getValue().doubleValue()), var3.box().maxZ);
            Object var5 = new AABB(var3.box().minX, var3.box().minY, var3.box().minZ, var3.box().maxX, (var3.box().minY + this.outlineHeight.getValue().doubleValue()), var3.box().maxZ);
            if (this.fill.getValue().equalsIgnoreCase("Normal")) {
                Renderer3D.renderBox(arg0.getMatrices(), var4, getFillColor(var3.type()));
            }
            if (!(this.outline.getValue().equalsIgnoreCase("Normal"))) {
                break label_2;
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var5, getOutlineColor(var3.type()));
        }
    }

    private Color getFillColor(PhaseESPModule.PhaseType arg0) {
        switch (arg0.ordinal()) {
            case 0 -> { break label_0; }
            case 1 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        break label_3;
        label_1:
        break label_3;
        label_2:
        label_3:
        return this.airFillColor.getColor();
    }

    private Color getOutlineColor(PhaseESPModule.PhaseType arg0) {
        switch (arg0.ordinal()) {
            case 0 -> { break label_0; }
            case 1 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        break label_3;
        label_1:
        break label_3;
        label_2:
        label_3:
        return this.airOutlineColor.getColor();
    }

    private PhaseESPModule.PhaseType getPhaseType(BlockPos arg0) {
        if (mc.level.getBlockState(arg0.below()).getBlock() == Blocks.BEDROCK && this.bedrock.getValue()) return PhaseESPModule.PhaseType.BEDROCK;
        if (mc.level.getBlockState(arg0.below()).getBlock() == Blocks.OBSIDIAN && this.obsidian.getValue()) return PhaseESPModule.PhaseType.OBSIDIAN;
        if (mc.level.getBlockState(arg0.below()).getBlock() == Blocks.AIR && this.air.getValue()) return PhaseESPModule.PhaseType.AIR;
        return null;
    }


    public record PhaseBlock(AABB box, PhaseESPModule.PhaseType type) {

        public PhaseBlock(AABB arg0, PhaseESPModule.PhaseType arg1) {
            this.box = arg0;
            this.type = arg1;
        }

        public AABB box() {
            return this.box;
        }

        public PhaseESPModule.PhaseType type() {
            return this.type;
        }

    }

    enum PhaseType {
        BEDROCK,
        OBSIDIAN,
        AIR;


        private PhaseType() {
        }

    }
}
