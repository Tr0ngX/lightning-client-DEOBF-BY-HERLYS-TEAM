package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.system.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "BlockHighlight", description = "Replaces the default Minecraft block highlight with a more customizable one.", category = Module.Category.VISUALS)
public class BlockHighlightModule extends Module {
    public ModeSetting animationMode;
    public ModeSetting mode;
    public NumberSetting slideSmoothness;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private BlockPos prevPosition;
    private Vec3 renderPosition;
    private long animationStart;

    public BlockHighlightModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Static";
        tmp0[1] = "Slide";
        this.animationMode = new ModeSetting("Animation", "The animation that will be applied to the rendering.", "Static", tmp0);
        var tmp1 = new String[5];
        tmp1[0] = "None";
        tmp1[1] = "Fill";
        tmp1[2] = "Outline";
        tmp1[3] = "Both";
        tmp1[4] = "Complex";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Outline", tmp1);
        this.slideSmoothness = new NumberSetting("Smoothness", "The smoothness for the slide while target block is changing.", 1, 0, 20);
        var tmp2 = new String[2];
        tmp2[0] = "Fill";
        tmp2[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultFillColor());
        var tmp3 = new String[2];
        tmp3[0] = "Outline";
        tmp3[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp3), ColorUtils.getDefaultOutlineColor());
        this.prevPosition = null;
        this.renderPosition = null;
        this.animationStart = 0L;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("None")) return;
        Object var3 = mc.hitResult;
        if (!((var3 instanceof BlockHitResult))) return;
        Object var4 = ((BlockHitResult) var3);
        Object var5 = var4.getBlockPos();
        if ((!(this.animationMode.getValue().equals("Slide"))) || (var5 == null)) break label_5;
        if (this.renderPosition == null) {
            this.renderPosition = MathUtils.getVec(var5);
        }
        if (WorldUtils.equals(var5, this.prevPosition)) break label_5;
        this.animationStart = System.currentTimeMillis();
        this.prevPosition = var5;
        label_5:
        Object var6 = MathUtils.getVec(var5);
        if (this.animationMode.getValue().equalsIgnoreCase("Slide") && this.renderPosition != null) {
            float var7 = Easing.ease(Easing.toDelta(this.animationStart, ((int) (Math.pow(this.slideSmoothness.getValue().doubleValue(), 1.4) * 1000.0))), Easing.Method.EASE_OUT_QUART);
            this.renderPosition = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(var5).subtract(this.renderPosition), var7));
            var6 = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(var5).subtract(this.renderPosition), var7));
        }
        var tmp0 = mc.level.getBlockState(var5);
        Object var2 = tmp0;
        if ((tmp0.isAir()) || (!(mc.level.getWorldBorder().isWithinBounds(var5)))) {
            return;
        }
        var7 = var2.getShape(mc.level, var5);
        if (var7.isEmpty()) return;
        if (this.mode.getValue().equalsIgnoreCase("Complex")) {
            Object var8 = var7.toAabbs().iterator();
            while (var8.hasNext()) {
                Object var9 = ((AABB) var8.next());
                Renderer3D.renderBox(arg0.getMatrices(), var9.move(var6), this.fillColor.getColor());
                Renderer3D.renderBoxOutline(arg0.getMatrices(), var9.move(var6), this.outlineColor.getColor());
            }
            break label_16;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBox(arg0.getMatrices(), var7.bounds().move(var6), this.fillColor.getColor());
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
            if (!(this.mode.getValue().equalsIgnoreCase("Both"))) break label_16;
        }
        Renderer3D.renderBoxOutline(arg0.getMatrices(), var7.bounds().move(var6), this.outlineColor.getColor());
        label_16:
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

}
