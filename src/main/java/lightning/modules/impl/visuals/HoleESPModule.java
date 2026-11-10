package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lightning.LightningClient;
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
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.HoleUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "HoleESP", description = "Highlights all holes in a specified radius.", category = Module.Category.VISUALS)
public class HoleESPModule extends Module {
    public NumberSetting range;
    public BooleanSetting asynchronous;
    public BooleanSetting doubleHoles;
    public BooleanSetting quadHoles;
    public BooleanSetting partialHoles;
    public BooleanSetting fade;
    public ModeSetting fill;
    public NumberSetting fillHeight;
    public ModeSetting outline;
    public NumberSetting outlineHeight;
    public BooleanSetting wireframe;
    public CategorySetting safeColorsCategory;
    public ColorSetting safeFillColor;
    public ColorSetting safeOutlineColor;
    public ColorSetting safeWireframeColor;
    public ModeSetting safeShader;
    public NumberSetting safeShaderOpacity;
    public NumberSetting safeShaderSpeed;
    public NumberSetting safeShaderStep;
    public ColorSetting safeShaderColor1;
    public ColorSetting safeShaderColor2;
    public ColorSetting safeShaderColor3;
    public ColorSetting safeShaderColor4;
    public ColorSetting safeShaderGlowColor;
    public CategorySetting unsafeColorsCategory;
    public ColorSetting unsafeFillColor;
    public ColorSetting unsafeOutlineColor;
    public ColorSetting unsafeWireframeColor;
    public ModeSetting unsafeShader;
    public NumberSetting unsafeShaderOpacity;
    public NumberSetting unsafeShaderSpeed;
    public NumberSetting unsafeShaderStep;
    public ColorSetting unsafeShaderColor1;
    public ColorSetting unsafeShaderColor2;
    public ColorSetting unsafeShaderColor3;
    public ColorSetting unsafeShaderColor4;
    public ColorSetting unsafeShaderGlowColor;
    public CategorySetting partialColorsCategory;
    public ColorSetting partialFillColor;
    public ColorSetting partialOutlineColor;
    public ColorSetting partialWireframeColor;
    public ModeSetting partialShader;
    public NumberSetting partialShaderOpacity;
    public NumberSetting partialShaderSpeed;
    public NumberSetting partialShaderStep;
    public ColorSetting partialShaderColor1;
    public ColorSetting partialShaderColor2;
    public ColorSetting partialShaderColor3;
    public ColorSetting partialShaderColor4;
    public ColorSetting partialShaderGlowColor;
    private final ExecutorService executor;
    private final List holes;

    public HoleESPModule() {
        this.range = new NumberSetting("Range", "The maximum range at which holes will be rendered.", 10, 1, 50);
        this.asynchronous = new BooleanSetting("Asynchronous", "Performs calculations on separate threads.", true);
        this.doubleHoles = new BooleanSetting("DoubleHoles", "Whether or not to render the ESP on double holes.", true);
        this.quadHoles = new BooleanSetting("QuadHoles", "Whether or not to render the ESP on quad holes.", true);
        this.partialHoles = new BooleanSetting("PartialHoles", "Whether or not to render the ESP on partial (incomplete) holes.", true);
        this.fade = new BooleanSetting("Fade", "Fades the holes in and out based on your distance to them.", false);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Gradient";
        this.fill = new ModeSetting("Fill", "The mode for the fill rendering on the hole boxes.", "Normal", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Normal";
        tmp1[1] = "Gradient";
        this.fillHeight = new NumberSetting("FillHeight", "The height of the fill rendering on the holes.", new ModeSetting.Visibility(this.fill, tmp1), 1.0, -2.0, 2.0);
        var tmp2 = new String[3];
        tmp2[0] = "None";
        tmp2[1] = "Normal";
        tmp2[2] = "Gradient";
        this.outline = new ModeSetting("Outline", "The mode for the outline rendering on the hole boxes.", "Normal", tmp2);
        var tmp3 = new String[2];
        tmp3[0] = "Normal";
        tmp3[1] = "Gradient";
        this.outlineHeight = new NumberSetting("OutlineHeight", "The height of the outline rendering on the holes.", new ModeSetting.Visibility(this.outline, tmp3), 1.0, -2.0, 2.0);
        this.wireframe = new BooleanSetting("Wireframe", "Renders an X wireframe on the hole boxes.", false);
        this.safeColorsCategory = new CategorySetting("Safe", "The category that contains the settings for coloring of safe (all-bedrock) holes.");
        this.safeFillColor = new ColorSetting("SafeFillColor", "Fill", "The color for the fill rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.safeOutlineColor = new ColorSetting("SafeOutlineColor", "Outline", "The color for the outline rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.safeWireframeColor = new ColorSetting("SafeWireframeColor", "Wireframe", "The color for the wireframe rendering on safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), new ColorSetting.Color(new Color(0, 255, 0, 125), false, false));
        this.safeShader = new ModeSetting("SafeShader", "Shader", "The animated shader for safe holes.", new CategorySetting.Visibility(this.safeColorsCategory), "None", EspShader.MODES);
        this.safeShaderOpacity = new NumberSetting("SafeShaderOpacity", "Opacity", "Safe hole shader opacity.", new ModeSetting.Visibility(this.safeShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
        this.safeShaderSpeed = new NumberSetting("SafeShaderSpeed", "Speed", "Safe hole shader speed.", new ModeSetting.Visibility(this.safeShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
        var tmp4 = new String[1];
        tmp4[0] = "Gradient";
        this.safeShaderStep = new NumberSetting("SafeShaderStep", "Step", "Safe hole gradient step size.", new ModeSetting.Visibility(this.safeShader, tmp4), 50.0, 0.1, 200.0);
        var tmp5 = new String[1];
        tmp5[0] = "Gradient";
        this.safeShaderColor1 = new ColorSetting("SafeShaderColor1", "The first gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, tmp5), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
        var tmp6 = new String[1];
        tmp6[0] = "Gradient";
        this.safeShaderColor2 = new ColorSetting("SafeShaderColor2", "The second gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, tmp6), new ColorSetting.Color(new Color(0, 200, 100, 255), false, false));
        var tmp7 = new String[1];
        tmp7[0] = "Gradient";
        this.safeShaderColor3 = new ColorSetting("SafeShaderColor3", "The third gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, tmp7), new ColorSetting.Color(new Color(0, 255, 200, 255), false, false));
        var tmp8 = new String[1];
        tmp8[0] = "Gradient";
        this.safeShaderColor4 = new ColorSetting("SafeShaderColor4", "The fourth gradient color for safe holes.", new ModeSetting.Visibility(this.safeShader, tmp8), new ColorSetting.Color(new Color(100, 255, 0, 255), false, false));
        var tmp9 = new String[1];
        tmp9[0] = "Glowing";
        this.safeShaderGlowColor = new ColorSetting("SafeShaderGlowColor", "GlowColor", "The glow tint color for safe holes.", new ModeSetting.Visibility(this.safeShader, tmp9), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
        this.unsafeColorsCategory = new CategorySetting("Unsafe", "The category that contains the settings for coloring of unsafe (blast-proof but not pure bedrock) holes.");
        this.unsafeFillColor = new ColorSetting("UnsafeFillColor", "Fill", "The color for the fill rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.unsafeOutlineColor = new ColorSetting("UnsafeOutlineColor", "Outline", "The color for the outline rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.unsafeWireframeColor = new ColorSetting("UnsafeWireframeColor", "Wireframe", "The color for the wireframe rendering on unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), new ColorSetting.Color(new Color(255, 0, 0, 125), false, false));
        this.unsafeShader = new ModeSetting("UnsafeShader", "Shader", "The animated shader for unsafe holes.", new CategorySetting.Visibility(this.unsafeColorsCategory), "None", EspShader.MODES);
        this.unsafeShaderOpacity = new NumberSetting("UnsafeShaderOpacity", "Opacity", "Unsafe hole shader opacity.", new ModeSetting.Visibility(this.unsafeShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
        this.unsafeShaderSpeed = new NumberSetting("UnsafeShaderSpeed", "Speed", "Unsafe hole shader speed.", new ModeSetting.Visibility(this.unsafeShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
        var tmp10 = new String[1];
        tmp10[0] = "Gradient";
        this.unsafeShaderStep = new NumberSetting("UnsafeShaderStep", "Step", "Unsafe hole gradient step size.", new ModeSetting.Visibility(this.unsafeShader, tmp10), 50.0, 0.1, 200.0);
        var tmp11 = new String[1];
        tmp11[0] = "Gradient";
        this.unsafeShaderColor1 = new ColorSetting("UnsafeShaderColor1", "The first gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, tmp11), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        var tmp12 = new String[1];
        tmp12[0] = "Gradient";
        this.unsafeShaderColor2 = new ColorSetting("UnsafeShaderColor2", "The second gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, tmp12), new ColorSetting.Color(new Color(255, 100, 0, 255), false, false));
        var tmp13 = new String[1];
        tmp13[0] = "Gradient";
        this.unsafeShaderColor3 = new ColorSetting("UnsafeShaderColor3", "The third gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, tmp13), new ColorSetting.Color(new Color(255, 0, 100, 255), false, false));
        var tmp14 = new String[1];
        tmp14[0] = "Gradient";
        this.unsafeShaderColor4 = new ColorSetting("UnsafeShaderColor4", "The fourth gradient color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, tmp14), new ColorSetting.Color(new Color(200, 0, 0, 255), false, false));
        var tmp15 = new String[1];
        tmp15[0] = "Glowing";
        this.unsafeShaderGlowColor = new ColorSetting("UnsafeShaderGlowColor", "GlowColor", "The glow tint color for unsafe holes.", new ModeSetting.Visibility(this.unsafeShader, tmp15), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        this.partialColorsCategory = new CategorySetting("Partial", "The category that contains the settings for coloring of partial (incomplete) holes.");
        this.partialFillColor = new ColorSetting("PartialFillColor", "Fill", "The color for the fill rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.partialOutlineColor = new ColorSetting("PartialOutlineColor", "Outline", "The color for the outline rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        this.partialWireframeColor = new ColorSetting("PartialWireframeColor", "Wireframe", "The color for the wireframe rendering on partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), new ColorSetting.Color(new Color(255, 255, 255, 125), false, false));
        this.partialShader = new ModeSetting("PartialShader", "Shader", "The animated shader for partial holes.", new CategorySetting.Visibility(this.partialColorsCategory), "None", EspShader.MODES);
        this.partialShaderOpacity = new NumberSetting("PartialShaderOpacity", "Opacity", "Partial hole shader opacity.", new ModeSetting.Visibility(this.partialShader, EspShader.SHADER_ACTIVE_MODES), 100, 0, 100);
        this.partialShaderSpeed = new NumberSetting("PartialShaderSpeed", "Speed", "Partial hole shader speed.", new ModeSetting.Visibility(this.partialShader, EspShader.SHADER_ACTIVE_MODES), 1.0, 0.1, 10.0);
        var tmp16 = new String[1];
        tmp16[0] = "Gradient";
        this.partialShaderStep = new NumberSetting("PartialShaderStep", "Step", "Partial hole gradient step size.", new ModeSetting.Visibility(this.partialShader, tmp16), 50.0, 0.1, 200.0);
        var tmp17 = new String[1];
        tmp17[0] = "Gradient";
        this.partialShaderColor1 = new ColorSetting("PartialShaderColor1", "The first gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, tmp17), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
        var tmp18 = new String[1];
        tmp18[0] = "Gradient";
        this.partialShaderColor2 = new ColorSetting("PartialShaderColor2", "The second gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, tmp18), new ColorSetting.Color(new Color(200, 200, 200, 255), false, false));
        var tmp19 = new String[1];
        tmp19[0] = "Gradient";
        this.partialShaderColor3 = new ColorSetting("PartialShaderColor3", "The third gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, tmp19), new ColorSetting.Color(new Color(220, 220, 220, 255), false, false));
        var tmp20 = new String[1];
        tmp20[0] = "Gradient";
        this.partialShaderColor4 = new ColorSetting("PartialShaderColor4", "The fourth gradient color for partial holes.", new ModeSetting.Visibility(this.partialShader, tmp20), new ColorSetting.Color(new Color(180, 180, 180, 255), false, false));
        var tmp21 = new String[1];
        tmp21[0] = "Glowing";
        this.partialShaderGlowColor = new ColorSetting("PartialShaderGlowColor", "GlowColor", "The glow tint color for partial holes.", new ModeSetting.Visibility(this.partialShader, tmp21), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
        this.executor = Executors.newSingleThreadExecutor();
        this.holes = Collections.synchronizedList(new ArrayList());
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = mc.player.blockPosition();
        int var3 = LightningClient.WORLD_MANAGER.getRadius(this.range.getValue().doubleValue());
        Object var4 = () -> {
    Object var3 = new ArrayList();
    int var4 = 0;
    while (var4 < arg0) {
        Object var8 = arg1.offset(LightningClient.WORLD_MANAGER.getOffset(var4));
        Object var9 = HoleUtils.getSingleHole(var8, 0.0);
        if (var9 == null) break label_1;
        var3.add(var9);
        break label_5;
        label_1:
        if (!(this.doubleHoles.getValue())) break label_2;
        var tmp0 = HoleUtils.getDoubleHole(var8, 0.0);
        Object var7 = tmp0;
        if (tmp0 == null) break label_2;
        var3.add(var7);
        break label_5;
        label_2:
        if (!(this.quadHoles.getValue())) break label_3;
        var tmp1 = HoleUtils.getQuadHole(var8, 0.0);
        Object var6 = tmp1;
        if (tmp1 == null) break label_3;
        var3.add(var6);
        break label_5;
        label_3:
        if (!(this.partialHoles.getValue())) break label_5;
        var tmp2 = HoleUtils.getPartialHole(var8, 0.0);
        Object var5 = tmp2;
        if (tmp2 == null) {
            break label_5;
        }
        var3.add(var5);
        label_5:
        var4++;
    }
    var4 = this.holes;
    var5 = var4;
    // synchronized (var4)
    this.holes.clear();
    this.holes.addAll(var3);
    break label_11;
    Object var10 = null;
    throw var10;
    label_11:
};
        if (this.asynchronous.getValue()) {
            this.executor.submit(var4);
        } else {
            var4.run();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (mc.level == null) return;
        Object var2 = this.holes;
        Object var3 = var2;
        // synchronized (var2)
        if (this.holes.isEmpty()) return;
        int var4 = (!(this.safeShader.getValue().equalsIgnoreCase("None")) ? 1 : 0);
        int var5 = (!(this.unsafeShader.getValue().equalsIgnoreCase("None")) ? 1 : 0);
        int var6 = (!(this.partialShader.getValue().equalsIgnoreCase("None")) ? 1 : 0);
        Object var7 = new ArrayList();
        Object var8 = new ArrayList();
        Object var9 = new ArrayList();
        Object var10 = new ArrayList();
        Object var11 = new ArrayList();
        Object var12 = new ArrayList();
        int var13 = this.fill.getValue().equalsIgnoreCase("Normal");
        int var14 = this.fill.getValue().equalsIgnoreCase("Gradient");
        int var15 = this.outline.getValue().equalsIgnoreCase("Normal");
        int var16 = this.outline.getValue().equalsIgnoreCase("Gradient");
        Object var17 = this.holes.iterator();
        while (var17.hasNext()) {
            Object var18 = ((HoleUtils.Hole) var17.next());
            if (!(Renderer3D.isFrustumVisible(var18.box()))) {
                break label_10;
            }
            int var20 = (var18.safety() == HoleUtils.HoleSafety.SAFE ? 1 : 0);
            var tmp0 = (var18.safety() == HoleUtils.HoleSafety.PARTIAL ? 1 : 0);
            int var19 = tmp0;
            int var21 = tmp0;
            if (!(!var20)) {
                break label_18;
            }
            int var22 = (!(!var19) ? var6 : var5);
            Object var23 = new AABB(var18.box().minX, var18.box().minY, var18.box().minZ, var18.box().maxX, (var18.box().minY + this.fillHeight.getValue().doubleValue()), var18.box().maxZ);
            Object var24 = new AABB(var18.box().minX, var18.box().minY, var18.box().minZ, var18.box().maxX, (var18.box().minY + this.outlineHeight.getValue().doubleValue()), var18.box().maxZ);
            Object var25 = new AABB(var18.box().minX, var18.box().minY, var18.box().minZ, var18.box().maxX, var18.box().minY, var18.box().maxZ);
            Object var26 = getFillColor(var18);
            Object var27 = getOutlineColor(var18);
            Object var28 = getWireframeColor(var18);
            if (!(!var22)) {
                if (!(!var20)) {
                    break label_21;
                }
                Object var30 = (!(!var19) ? var11 : var9);
                if (!(!var20)) {
                    break label_24;
                }
                Object var29 = (!(!var19) ? var12 : var10);
                if (!(!var13)) {
                    Renderer3D.renderGradientBox(var30, arg0.getMatrices(), var23, var26, var26);
                }
                if (!(!var14)) {
                    if (this.fillHeight.getValue().floatValue() < 0.0f) {
                    } else {
                    }
                    if (this.fillHeight.getValue().floatValue() < 0.0f) {
                    } else {
                    }
                    Renderer3D.renderGradientBox(var23, var26, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), var26);
                }
                if (!(!var15)) {
                    Renderer3D.renderGradientBoxOutline(var29, arg0.getMatrices(), var24, var27, var27);
                }
                if (!(!var16)) {
                    if (this.outlineHeight.getValue().floatValue() < 0.0f) {
                    } else {
                    }
                    if (this.outlineHeight.getValue().floatValue() < 0.0f) {
                    } else {
                    }
                    Renderer3D.renderGradientBoxOutline(var24, var27, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), var27);
                }
                if (!(this.wireframe.getValue())) {
                    break label_10;
                }
                Renderer3D.renderBoxWireframe(var29, arg0.getMatrices(), var25, var28, var28);
                break label_10;
            }
            if (!(!var13)) {
                Renderer3D.renderBox(arg0.getMatrices(), var23, var26);
            }
            if (!(!var14)) {
                if (this.fillHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                if (this.fillHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                Renderer3D.renderGradientBox(var26, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), var26);
            }
            if (!(!var15)) {
                Renderer3D.renderBoxOutline(arg0.getMatrices(), var24, var27);
            }
            if (!(!var16)) {
                if (this.outlineHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                if (this.outlineHeight.getValue().floatValue() < 0.0f) {
                } else {
                }
                Renderer3D.renderGradientBoxOutline(var27, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), var27);
            }
            if (!(this.wireframe.getValue())) {
                break label_10;
            }
            Renderer3D.renderBoxWireframe(arg0.getMatrices(), var25, var28);
        }
        if (!var4) break label_54;
        if (var7.isEmpty()) {
            if (var8.isEmpty()) break label_54;
        }
        var17 = EspShader.createSettings(this.safeShader.getValue(), this.safeShaderOpacity.getValue().floatValue(), this.safeShaderSpeed.getValue().floatValue(), this.safeShaderStep.getValue().floatValue(), this.safeShaderColor1.getColor(), this.safeShaderColor2.getColor(), this.safeShaderColor3.getColor(), this.safeShaderColor4.getColor(), this.safeShaderGlowColor.getColor(), null);
        EspShader.draw(var7, var8, var17);
        label_54:
        if (!var5) break label_56;
        if (var9.isEmpty()) {
            if (var10.isEmpty()) break label_56;
        }
        var17 = EspShader.createSettings(this.unsafeShader.getValue(), this.unsafeShaderOpacity.getValue().floatValue(), this.unsafeShaderSpeed.getValue().floatValue(), this.unsafeShaderStep.getValue().floatValue(), this.unsafeShaderColor1.getColor(), this.unsafeShaderColor2.getColor(), this.unsafeShaderColor3.getColor(), this.unsafeShaderColor4.getColor(), this.unsafeShaderGlowColor.getColor(), null);
        EspShader.draw(var9, var10, var17);
        label_56:
        if (!var6) break label_58;
        if (var11.isEmpty()) {
            if (var12.isEmpty()) break label_58;
        }
        var17 = EspShader.createSettings(this.partialShader.getValue(), this.partialShaderOpacity.getValue().floatValue(), this.partialShaderSpeed.getValue().floatValue(), this.partialShaderStep.getValue().floatValue(), this.partialShaderColor1.getColor(), this.partialShaderColor2.getColor(), this.partialShaderColor3.getColor(), this.partialShaderColor4.getColor(), this.partialShaderGlowColor.getColor(), null);
        EspShader.draw(var11, var12, var17);
        label_58:
        break label_62;
        Object var31 = var24;
        throw var31;
        label_62:
    }

    public String getMetaData() {
        return String.valueOf(this.holes.size());
    }

    private Color getFillColor(HoleUtils.Hole arg0) {
        switch (HoleESPModule.1.$SwitchMap$night$utils$minecraft$HoleUtils$HoleSafety[arg0.safety().ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        Object var2 = this.safeFillColor.getColor();
        break label_3;
        label_1:
        var2 = this.partialFillColor.getColor();
        break label_3;
        label_2:
        var tmp0 = this.unsafeFillColor.getColor();
        var2 = tmp0;
        Object var3 = tmp0;
        label_3:
        if (!(this.fade.getValue())) return var2;
        return ColorUtils.getColor(var2, ((int) (((float) var2.getAlpha()) * getEasing(arg0))));
    }

    private Color getOutlineColor(HoleUtils.Hole arg0) {
        switch (HoleESPModule.1.$SwitchMap$night$utils$minecraft$HoleUtils$HoleSafety[arg0.safety().ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        Object var2 = this.safeOutlineColor.getColor();
        break label_3;
        label_1:
        var2 = this.partialOutlineColor.getColor();
        break label_3;
        label_2:
        var tmp0 = this.unsafeOutlineColor.getColor();
        var2 = tmp0;
        Object var3 = tmp0;
        label_3:
        if (!(this.fade.getValue())) return var2;
        return ColorUtils.getColor(var2, ((int) (((float) var2.getAlpha()) * getEasing(arg0))));
    }

    private Color getWireframeColor(HoleUtils.Hole arg0) {
        switch (HoleESPModule.1.$SwitchMap$night$utils$minecraft$HoleUtils$HoleSafety[arg0.safety().ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        Object var2 = this.safeWireframeColor.getColor();
        break label_3;
        label_1:
        var2 = this.partialWireframeColor.getColor();
        break label_3;
        label_2:
        var tmp0 = this.unsafeWireframeColor.getColor();
        var2 = tmp0;
        Object var3 = tmp0;
        label_3:
        if (!(this.fade.getValue())) return var2;
        return ColorUtils.getColor(var2, ((int) (((float) var2.getAlpha()) * getEasing(arg0))));
    }

    private float getEasing(HoleUtils.Hole arg0) {
        float var2 = ((float) (1.0 - Mth.clamp((Math.sqrt(mc.player.distanceToSqr(arg0.box().getCenter())) / this.range.getValue().doubleValue()), 0.0, 1.0)));
        return Easing.ease(var2, Easing.Method.EASE_OUT_CUBIC);
    }


    class 1 {

        static {
            $SwitchMap$night$utils$minecraft$HoleUtils$HoleSafety = new int[HoleUtils.HoleSafety.values().length];
            try {
                $SwitchMap$night$utils$minecraft$HoleUtils$HoleSafety[HoleUtils.HoleSafety.SAFE.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$night$utils$minecraft$HoleUtils$HoleSafety[HoleUtils.HoleSafety.PARTIAL.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
            }
        }

    }
}
