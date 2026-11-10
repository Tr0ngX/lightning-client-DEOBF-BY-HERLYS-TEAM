package lightning.modules.impl.core;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Font;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.font.FontRenderer;

@RegisterModule(name = "Font", description = "Manages the client and the game's font rendering.", category = Module.Category.CORE, toggled = true, drawn = false)
public class FontModule extends Module {
    public CategorySetting customFontCategory;
    public BooleanSetting customFont;
    public StringSetting name;
    public NumberSetting size;
    public ModeSetting style;
    public BooleanSetting global;
    public CategorySetting offsetsCategory;
    public NumberSetting xOffset;
    public NumberSetting yOffset;
    public NumberSetting widthOffset;
    public NumberSetting heightOffset;
    public CategorySetting shadowsCategory;
    public ModeSetting shadowMode;
    public NumberSetting shadowOffset;
    public BooleanSetting glow;

    public FontModule() {
        this.customFontCategory = new CategorySetting("CustomFont", "The category for settings relating to custom fonts.");
        this.customFont = new BooleanSetting("CustomFont", "Enabled", "Enables custom font rendering.", new CategorySetting.Visibility(this.customFontCategory), true);
        this.name = new StringSetting("Name", "The name of the font that will be rendered.", new CategorySetting.Visibility(this.customFontCategory), "Verdana");
        this.size = new NumberSetting("Size", "The size of the custom font that will be rendered.", new CategorySetting.Visibility(this.customFontCategory), 18, 8, 48);
        var tmp0 = new String[4];
        tmp0[0] = "Plain";
        tmp0[1] = "Bold";
        tmp0[2] = "Italic";
        tmp0[3] = "BoldItalic";
        this.style = new ModeSetting("Style", "The style that will be used in the font's rendering.", new CategorySetting.Visibility(this.customFontCategory), "Italic", tmp0);
        this.global = new BooleanSetting("Global", "Applies the custom font rendering on every part of the game.", new CategorySetting.Visibility(this.customFontCategory), false);
        this.offsetsCategory = new CategorySetting("Offsets", "Allows you to offset the custom font rendering to make it render perfectly.");
        this.xOffset = new NumberSetting("XOffset", "The offset that will be applied to the font on the X axis.", new CategorySetting.Visibility(this.offsetsCategory), 0, -10, 10);
        this.yOffset = new NumberSetting("YOffset", "The offset that will be applied to the font on the Y axis.", new CategorySetting.Visibility(this.offsetsCategory), 0, -10, 10);
        this.widthOffset = new NumberSetting("WidthOffset", "The offset that will be applied to the font on the X axis.", new CategorySetting.Visibility(this.offsetsCategory), 0, -10, 10);
        this.heightOffset = new NumberSetting("HeightOffset", "The font's offset on the Y axis.", new CategorySetting.Visibility(this.offsetsCategory), 0, -10, 10);
        this.shadowsCategory = new CategorySetting("Shadows", "The category for settings related to font shadows.");
        var tmp1 = new String[3];
        tmp1[0] = "None";
        tmp1[1] = "Default";
        tmp1[2] = "Custom";
        this.shadowMode = new ModeSetting("ShadowMode", "Mode", "The way that the shadow will be rendered.", new CategorySetting.Visibility(this.shadowsCategory), "Default", tmp1);
        var tmp2 = new String[1];
        tmp2[0] = "Custom";
        this.shadowOffset = new NumberSetting("ShadowOffset", "Offset", "The distance of the shadow from the text being rendered.", new ModeSetting.Visibility(this.shadowMode, tmp2), 0.5f, -2.0f, 2.0f);
        this.glow = new BooleanSetting("Glow", "Renders a glow shader effect for HUD text only.", false);
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent arg0) {
        if (arg0.getSetting() != this.glow) break label_0;
        Object var2 = HUDModule.INSTANCE;
        if ((HUDModule.INSTANCE == null) || (var2.textGlow.getValue() == this.glow.getValue())) break label_0;
        var2.textGlow.setValue(this.glow.getValue());
        label_0:
        if (arg0.getSetting() != this.name && arg0.getSetting() != this.size && arg0.getSetting() != this.style && arg0.getSetting() != this.customFont) {
            if (arg0.getSetting() == this.glow) {
            }
            updateFontRenderer();
        }
    }

    public void onEnable() {
        updateFontRenderer();
    }

    public void updateFontRenderer() {
        try {
            if (RenderSystem.getDevice() == null) return;
        } catch (Throwable ex3) {
            Object var1 = ex3;
            return;
        }
        if (this.style.getValue().equalsIgnoreCase("BoldItalic")) {
            break label_8;
        }
        if (this.style.getValue().equalsIgnoreCase("Bold")) {
            break label_8;
        }
        var1 = (this.style.getValue().equalsIgnoreCase("Italic") ? 2 : 0);
        int var2 = this.size.getValue().intValue();
        Object var3 = new Font(this.name.getValue(), var1, var2);
        Object var4 = new Font("Segoe UI Symbol", var1, var2);
        Object var5 = new Font("Segoe UI Emoji", 0, var2);
        Object var6 = new Font("Arial Unicode MS", var1, var2);
        Object var7 = new Font("SansSerif", var1, var2);
        Object var8 = new Font("Dialog", var1, var2);
        var tmp0 = new Font[6];
        tmp0[0] = var3;
        tmp0[1] = var4;
        tmp0[2] = var5;
        tmp0[3] = var6;
        tmp0[4] = var7;
        tmp0[5] = var8;
        LightningClient.FONT_MANAGER.setFontRenderer(new FontRenderer(tmp0, (this.size.getValue().floatValue() / 2.0f)));
    }

}
