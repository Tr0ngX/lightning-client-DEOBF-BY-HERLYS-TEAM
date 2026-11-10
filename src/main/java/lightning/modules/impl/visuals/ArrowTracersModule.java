package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@RegisterModule(name = "ArrowTracers", description = "Renders arrows towards players who are off screen.", category = Module.Category.VISUALS)
public class ArrowTracersModule extends Module {
    public NumberSetting width;
    public NumberSetting height;
    public NumberSetting distance;
    public BooleanSetting antiBot;
    public BooleanSetting onScreen;
    public ModeSetting alpha;
    public NumberSetting fadeDistance;
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;

    public ArrowTracersModule() {
        this.width = new NumberSetting("Width", "The width of the arrows being rendered.", 4.0f, 0.5f, 15.0f);
        this.height = new NumberSetting("Height", "The height of the arrows being rendered.", 8.0f, 0.5f, 15.0f);
        this.distance = new NumberSetting("Distance", "The distance that will be between the crosshair and the arrows.", 45, 5, 200);
        this.antiBot = new BooleanSetting("AntiBot", "Prevents bots from having arrow tracers rendered for them.", false);
        this.onScreen = new BooleanSetting("OnScreen", "Renders the arrow tracers even for players who are on screen.", false);
        var tmp0 = new String[2];
        tmp0[0] = "Default";
        tmp0[1] = "Fade";
        this.alpha = new ModeSetting("Alpha", "The alpha that will be used in the arrow rendering.", "Fade", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Fade";
        this.fadeDistance = new NumberSetting("FadeDistance", "The distance at which the arrows will start fading.", new ModeSetting.Visibility(this.alpha, tmp1), 100.0f, 10.0f, 200.0f);
        var tmp2 = new String[3];
        tmp2[0] = "Fill";
        tmp2[1] = "Outline";
        tmp2[2] = "Both";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the arrows", "Both", tmp2);
        var tmp3 = new String[2];
        tmp3[0] = "Fill";
        tmp3[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp3), ColorUtils.getDefaultOutlineColor());
        var tmp4 = new String[2];
        tmp4[0] = "Outline";
        tmp4[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp4), new ColorSetting.Color(new Color(0, 0, 0), true, false));
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getContext();
        float var3 = (((float) mc.getWindow().getGuiScaledWidth()) / 2.0f);
        float var4 = (((float) mc.getWindow().getGuiScaledHeight()) / 2.0f);
        float var5 = this.distance.getValue().floatValue();
        float var6 = this.width.getValue().floatValue();
        float var7 = this.height.getValue().floatValue();
        Object var8 = EntityUtils.getRenderPos(mc.player, arg0.getTickDelta());
        Object var9 = mc.level.players().iterator();
        while (var9.hasNext()) {
            Object var10 = ((Player) var9.next());
            if ((var10 == mc.player) || (EntityUtils.isGhost(var10))) break label_2;
            if (EntityUtils.isBot(var10)) {
                if (this.antiBot.getValue()) break label_2;
            }
            if (!(this.onScreen.getValue()) && Renderer3D.isFrustumVisible(var10.getBoundingBox())) {
                break label_2;
            }
            Object var11 = EntityUtils.getRenderPos(var10, arg0.getTickDelta());
            double var12 = (var11.x - var8.x);
            double var14 = (var11.z - var8.z);
            double var16 = (Math.toDegrees(Math.atan2(var14, var12)) - 90.0);
            double var18 = Mth.wrapDegrees((var16 - ((double) mc.player.getYRot())));
            int var20 = ((int) Mth.clamp((255.0f - ((255.0f / this.fadeDistance.getValue().floatValue()) * mc.player.distanceTo(var10))), 80.0f, 255.0f));
            if (this.alpha.getValue().equalsIgnoreCase("Fade")) {
            } else {
            }
            Object var21 = this.fillColor.getColor();
            if (this.alpha.getValue().equalsIgnoreCase("Fade")) {
            } else {
            }
            Object var22 = this.outlineColor.getColor();
            var2.pose().pushMatrix();
            var2.pose().translate(var3, var4);
            var2.pose().rotate(((float) Math.toRadians(var18)));
            var2.pose().translate(-(var3), -(var4));
            if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
                if (this.mode.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer2D.renderArrow(var2, var3, (var4 - var5), var6, var7, var21);
            }
            if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
                if (this.mode.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer2D.renderArrowOutline(var2, var3, (var4 - var5), var6, var7, var22);
            }
            var2.pose().popMatrix();
        }
    }

}
