package lightning.modules.impl.core;

import java.awt.Color;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;

@RegisterModule(name = "Renders", description = "Manages the client world renders.", category = Module.Category.CORE, persistent = true, drawn = false)
public class RendersModule extends Module {
    public ModeSetting mode;
    public NumberSetting duration;
    public ModeSetting renderMode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;

    public RendersModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Fade";
        tmp0[1] = "Shrink";
        this.mode = new ModeSetting("Mode", "The mode for the place render.", "Fade", tmp0);
        this.duration = new NumberSetting("Duration", "The duration for the place render.", 300, 0, 1000);
        var tmp1 = new String[3];
        tmp1[0] = "Fill";
        tmp1[1] = "Outline";
        tmp1[2] = "Both";
        this.renderMode = new ModeSetting("RenderMode", "The rendering that will be applied to the blocks highlighted.", "Both", tmp1);
        var tmp2 = new String[2];
        tmp2[0] = "Fill";
        tmp2[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.renderMode, tmp2), ColorUtils.getDefaultFillColor());
        var tmp3 = new String[2];
        tmp3[0] = "Outline";
        tmp3[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.renderMode, tmp3), ColorUtils.getDefaultOutlineColor());
    }

    public Color getColor(String arg0, Color arg1, float arg2) {
        if (arg0.equalsIgnoreCase("Fade")) return ColorUtils.getColor(arg1, ((int) (((float) arg1.getAlpha()) * arg2)));
        return arg1;
    }

}
