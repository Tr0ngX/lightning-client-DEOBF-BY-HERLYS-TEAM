package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "VoidESP", description = "Highlights any non-bedrock blocks that can drop you into the void.", category = Module.Category.VISUALS)
public class VoidESPModule extends Module {
    public NumberSetting range;
    public BooleanSetting asynchronous;
    public CategorySetting fillCategory;
    public ModeSetting fill;
    public NumberSetting fillHeight;
    public ColorSetting fillColor;
    public CategorySetting outlineCategory;
    public ModeSetting outline;
    public NumberSetting outlineHeight;
    public ColorSetting outlineColor;
    public CategorySetting shaderCategory;
    public ModeSetting shader;
    public NumberSetting shaderOpacity;
    public NumberSetting shaderSpeed;
    public NumberSetting shaderStep;
    public ColorSetting shaderColor1;
    public ColorSetting shaderColor2;
    public ColorSetting shaderColor3;
    public ColorSetting shaderColor4;
    public ColorSetting shaderGlowColor;
    private final ExecutorService executor;
    private final List positions;

    public VoidESPModule() {
        this.range = new NumberSetting("Range", "The maximum range at which void blocks will be rendered.", 10, 1, 50);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.fillCategory = new CategorySetting("Fill", "The category for settings related to fill rendering.");
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Gradient";
        this.fill = new ModeSetting("Fill", "Mode", "The mode for the fill rendering on the void blocks.", new CategorySetting.Visibility(this.fillCategory), "Normal", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Normal";
        tmp1[1] = "Gradient";
        this.fillHeight = new NumberSetting("FillHeight", "Height", "The height of the fill rendering on the void blocks.", new ModeSetting.Visibility(this.fill, tmp1), 1.0, 0.0, 2.0);
        var tmp2 = new String[2];
        tmp2[0] = "Normal";
        tmp2[1] = "Gradient";
        this.fillColor = new ColorSetting("FillColor", "Color", "The color for the fill rendering on the void blocks.", new ModeSetting.Visibility(this.fill, tmp2), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.outlineCategory = new CategorySetting("Outline", "The category for settings related to outline rendering.");
        var tmp3 = new String[3];
        tmp3[0] = "None";
        tmp3[1] = "Normal";
        tmp3[2] = "Gradient";
        this.outline = new ModeSetting("Outline", "Mode", "The mode for the outline rendering on the void blocks.", new CategorySetting.Visibility(this.outlineCategory), "Normal", tmp3);
        var tmp4 = new String[2];
        tmp4[0] = "Normal";
        tmp4[1] = "Gradient";
        this.outlineHeight = new NumberSetting("OutlineHeight", "Height", "The height of the outline rendering on the void blocks.", new ModeSetting.Visibility(this.outline, tmp4), 1.0, 0.0, 2.0);
        var tmp5 = new String[2];
        tmp5[0] = "Normal";
        tmp5[1] = "Gradient";
        this.outlineColor = new ColorSetting("OutlineColor", "Color", "The color for the outline rendering on the void blocks.", new ModeSetting.Visibility(this.outline, tmp5), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.shaderCategory = new CategorySetting("Shader", "The category for settings related to shader rendering.");
        this.shader = new ModeSetting("Shader", "Mode", "The animated shader that will be rendered on the void blocks.", new CategorySetting.Visibility(this.shaderCategory), "None", EspShader.MODES);
        this.shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "Void block shader opacity.", new ModeSetting.Visibility(this.shader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
        this.shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "Void block shader animation speed.", new ModeSetting.Visibility(this.shader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
        var tmp6 = new String[1];
        tmp6[0] = "Gradient";
        this.shaderStep = new NumberSetting("ShaderStep", "Step", "Void block gradient step size.", new ModeSetting.Visibility(this.shader, tmp6), 50.0, 0.1, 200.0);
        var tmp7 = new String[1];
        tmp7[0] = "Gradient";
        this.shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, tmp7), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        var tmp8 = new String[1];
        tmp8[0] = "Gradient";
        this.shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, tmp8), new ColorSetting.Color(new Color(180, 0, 255, 255), false, false));
        var tmp9 = new String[1];
        tmp9[0] = "Gradient";
        this.shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, tmp9), new ColorSetting.Color(new Color(255, 0, 100, 255), false, false));
        var tmp10 = new String[1];
        tmp10[0] = "Gradient";
        this.shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, tmp10), new ColorSetting.Color(new Color(100, 0, 255, 255), false, false));
        var tmp11 = new String[1];
        tmp11[0] = "Glowing";
        this.shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The glow tint color for void blocks.", new ModeSetting.Visibility(this.shader, tmp11), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        this.executor = Executors.newSingleThreadExecutor();
        this.positions = Collections.synchronizedList(new ArrayList());
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = () -> {
    Object var1 = new ArrayList();
    int var2 = ((int) (mc.player.getX() - ((double) this.range.getValue().intValue())));
    while (((double) var2) < (mc.player.getX() + ((double) this.range.getValue().intValue()))) {
        int var3 = ((int) (mc.player.getZ() - ((double) this.range.getValue().intValue())));
        while (((double) var3) < (mc.player.getZ() + ((double) this.range.getValue().intValue()))) {
            Object var4 = BlockPos.containing(((double) var2), ((double) mc.level.getMinY()), ((double) var3));
            if (mc.level.getBlockState(var4).getBlock() != Blocks.BEDROCK && mc.level.getWorldBorder().isWithinBounds(var4)) {
                var1.add(var4);
            }
            var3++;
        }
        var2++;
    }
    var3 = this.positions;
    var4 = var3;
    // synchronized (var3)
    this.positions.clear();
    this.positions.addAll(var1);
    break label_9;
    Object var5 = null;
    throw var5;
    label_9:
};
        if (this.asynchronous.getValue()) {
            this.executor.submit(var2);
        } else {
            var2.run();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (mc.level == null) return;
        Object var2 = this.positions;
        Object var3 = var2;
        // synchronized (var2)
        if (this.positions.isEmpty()) return;
        int var4 = (!(this.shader.getValue().equalsIgnoreCase("None")) ? 1 : 0);
        Object var5 = new ArrayList();
        Object var6 = new ArrayList();
        int var7 = this.fill.getValue().equalsIgnoreCase("Normal");
        int var8 = this.fill.getValue().equalsIgnoreCase("Gradient");
        int var9 = this.outline.getValue().equalsIgnoreCase("Normal");
        int var10 = this.outline.getValue().equalsIgnoreCase("Gradient");
        Object var11 = this.positions.iterator();
        while (var11.hasNext()) {
            Object var12 = ((BlockPos) var11.next());
            Object var13 = new AABB(var12);
            Object var14 = new AABB(var13.minX, var13.minY, var13.minZ, var13.maxX, (var13.minY + this.fillHeight.getValue().doubleValue()), var13.maxZ);
            Object var15 = new AABB(var13.minX, var13.minY, var13.minZ, var13.maxX, (var13.minY + this.outlineHeight.getValue().doubleValue()), var13.maxZ);
            if (!(!var4)) {
                if (!(!var7)) {
                    Renderer3D.renderGradientBox(var5, arg0.getMatrices(), var14, this.fillColor.getColor(), this.fillColor.getColor());
                }
                if (!(!var8)) {
                    if (this.fillHeight.getValue().floatValue() < 0.0f) {
                    } else {
                    }
                    if (this.fillHeight.getValue().floatValue() < 0.0f) {
                    } else {
                    }
                    Renderer3D.renderGradientBox(var14, this.fillColor.getColor(), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), this.fillColor.getColor());
                }
                if (!(!var9)) {
                    Renderer3D.renderGradientBoxOutline(var6, arg0.getMatrices(), var15, this.outlineColor.getColor(), this.outlineColor.getColor());
                }
                if (!var10) {
                    break label_6;
                }
                if (this.outlineHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                if (this.outlineHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                Renderer3D.renderGradientBoxOutline(var15, this.outlineColor.getColor(), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), this.outlineColor.getColor());
                break label_6;
            }
            if (!(!var7)) {
                Renderer3D.renderBox(arg0.getMatrices(), var14, this.fillColor.getColor());
            }
            if (!(!var8)) {
                if (this.fillHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                if (this.fillHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                Renderer3D.renderGradientBox(this.fillColor.getColor(), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), this.fillColor.getColor());
            }
            if (!(!var9)) {
                Renderer3D.renderBoxOutline(arg0.getMatrices(), var15, this.outlineColor.getColor());
            }
            if (!var10) {
                break label_6;
            }
            if (this.outlineHeight.getValue().floatValue() < 0.0f) {
            } else {
            }
            if (this.outlineHeight.getValue().floatValue() < 0.0f) {
            } else {
            }
            Renderer3D.renderGradientBoxOutline(this.outlineColor.getColor(), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), this.outlineColor.getColor());
        }
        if (!var4) break label_34;
        if (var5.isEmpty()) {
            if (var6.isEmpty()) break label_34;
        }
        var11 = EspShader.createSettings(this.shader.getValue(), this.shaderOpacity.getValue().floatValue(), this.shaderSpeed.getValue().floatValue(), this.shaderStep.getValue().floatValue(), this.shaderColor1.getColor(), this.shaderColor2.getColor(), this.shaderColor3.getColor(), this.shaderColor4.getColor(), this.shaderGlowColor.getColor(), null);
        EspShader.draw(var5, var6, var11);
        label_34:
        break label_38;
        Object var16 = var15;
        throw var16;
        label_38:
    }

    public String getMetaData() {
        return String.valueOf(this.positions.size());
    }

}
