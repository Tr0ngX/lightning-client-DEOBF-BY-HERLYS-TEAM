package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IAmbienceModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "Ambience", description = "Modifies the world ambient lighting and atmosphere color.", category = Module.Category.VISUALS)
public class AmbienceModule extends Module implements IAmbienceModule {
    public ColorSetting color;
    public BooleanSetting useSaturation;
    public NumberSetting saturation;
    public BooleanSetting lightmap;
    public BooleanSetting sky;
    public BooleanSetting blocks;

    public AmbienceModule() {
        this.color = new ColorSetting("Color", "The ambience color applied to the world.", new ColorSetting.Color(new Color(255, 128, 128, 255), true, false));
        this.useSaturation = new BooleanSetting("UseSaturation", "Overrides the saturation of the ambience color.", false);
        this.saturation = new NumberSetting("Saturation", "The custom saturation level.", new BooleanSetting.Visibility(this.useSaturation, true), 0.5f, 0.0f, 1.0f);
        this.lightmap = new BooleanSetting("Lightmap", "Modifies the world lightmap lighting.", true);
        this.sky = new BooleanSetting("Sky", "Tints sky lighting with ambience.", true);
        this.blocks = new BooleanSetting("Blocks", "Tints block lighting with ambience.", true);
    }

    public Color getColor() {
        Object var1 = this.color.getColor();
        if (this.useSaturation.getValue()) {
            Object var2 = Color.RGBtoHSB(var1.getRed(), var1.getGreen(), var1.getBlue(), null);
            Object var3 = Color.getHSBColor(var2[0], this.saturation.getValue().floatValue(), var2[2]);
            return new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), var1.getAlpha());
        }
        return var1;
    }

    public boolean useSaturation() {
        return this.useSaturation.getValue();
    }

    public float getSaturation() {
        return this.saturation.getValue().floatValue();
    }

    public boolean isLightmapEnabled() {
        return this.lightmap.getValue();
    }

    public boolean isSkyEnabled() {
        return this.sky.getValue();
    }

    public boolean isBlocksEnabled() {
        return this.blocks.getValue();
    }

}
