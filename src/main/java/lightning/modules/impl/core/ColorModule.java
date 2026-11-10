package lightning.modules.impl.core;

import java.awt.Color;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "Color", description = "Manages the client's global color system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class ColorModule extends Module {
    public ColorSetting color;
    public CategorySetting rainbowCategory;
    public NumberSetting rainbowSpeed;
    public NumberSetting rainbowSaturation;
    public NumberSetting rainbowBrightness;
    public NumberSetting rainbowScale;

    public ColorModule() {
        this.color = new ColorSetting("Color", "The global color that is used in sync and in most of the elements.", new ColorSetting.Color(new Color(160, 120, 255), false, false));
        this.rainbowCategory = new CategorySetting("Rainbow", "The category containing all settings related to rainbow coloring.");
        this.rainbowSpeed = new NumberSetting("RainbowSpeed", "Speed", "The speed that rainbow colors will be cycling at.", new CategorySetting.Visibility(this.rainbowCategory), 6L, 1L, 20L);
        this.rainbowSaturation = new NumberSetting("RainbowSaturation", "Saturation", "The saturation value of the rainbow color.", new CategorySetting.Visibility(this.rainbowCategory), 100.0f, 0.0f, 100.0f);
        this.rainbowBrightness = new NumberSetting("RainbowBrightness", "Brightness", "The brightness value of the rainbow color.", new CategorySetting.Visibility(this.rainbowCategory), 100.0f, 0.0f, 100.0f);
        this.rainbowScale = new NumberSetting("RainbowScale", "Scale", "The scale / density of the rainbow bands.", new CategorySetting.Visibility(this.rainbowCategory), 100.0f, 10.0f, 500.0f);
    }

}
