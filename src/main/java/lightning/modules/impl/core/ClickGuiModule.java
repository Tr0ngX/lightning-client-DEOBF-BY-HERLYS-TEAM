package lightning.modules.impl.core;

import java.awt.Color;
import java.util.Arrays;
import lightning.LightningClient;
import lightning.gui.HeaderFx;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.EspShader;

@RegisterModule(name = "ClickGui", description = "Allows you to change and interact with the client's modules and settings through a GUI.", category = Module.Category.CORE, drawn = false, bind = 344)
public class ClickGuiModule extends Module {
    public BooleanSetting sounds;
    public BooleanSetting blur;
    public NumberSetting scrollSpeed;
    public NumberSetting scale;
    public ColorSetting color;
    private static final String[] FILL_MODES;
    public ModeSetting fillMode;
    private static final String[] SHADER_FILL_MODES;
    public NumberSetting neekeriSpeed;
    public NumberSetting neekeriOpacity;
    public static final String[] BG_MODES;
    public ModeSetting background;
    private static final String[] ACTIVE_BG_MODES;
    public NumberSetting bgSpeed;
    public NumberSetting bgOpacity;
    public BooleanSetting glowAccent;
    public BooleanSetting categoryIcons;
    public BooleanSetting triangles;
    public BooleanSetting switches;

    public ClickGuiModule() {
        this.sounds = new BooleanSetting("Sounds", "Plays Minecraft UI sounds when interacting with the client's GUI.", true);
        this.blur = new BooleanSetting("Blur", "Whether or not to blur the background behind the GUI.", true);
        this.scrollSpeed = new NumberSetting("ScrollSpeed", "The speed at which the scrolling of the frames will be at.", 1, 1, 10);
        this.scale = new NumberSetting("Scale", "Scale", "The overall size of the ClickGUI. Applies next time you open the GUI.", new Setting.Visibility(), 1.0f, 0.5f, 2.0f, 0.1f);
        this.color = new ColorSetting("Color", "The color that will be used in the GUI.", new ColorSetting.Color(new Color(160, 120, 255), false, false));
        this.fillMode = new ModeSetting("FillMode", "How a toggled module's row background fills.", "Default", FILL_MODES);
        this.neekeriSpeed = new NumberSetting("NeekeriSpeed", "Speed", "The speed at which the fill animates.", new ModeSetting.Visibility(this.fillMode, SHADER_FILL_MODES), 1.0f, 0.1f, 10.0f);
        this.neekeriOpacity = new NumberSetting("NeekeriOpacity", "Opacity", "The opacity of the fill.", new ModeSetting.Visibility(this.fillMode, SHADER_FILL_MODES), 90, 0, 100);
        this.background = new ModeSetting("Background", "Shader effect rendered behind the GUI.", "Aurora", BG_MODES);
        this.bgSpeed = new NumberSetting("BgSpeed", "Speed", "The speed at which the background shader animates.", new ModeSetting.Visibility(this.background, ACTIVE_BG_MODES), 1.0f, 0.1f, 5.0f);
        this.bgOpacity = new NumberSetting("BgOpacity", "Opacity", "The opacity of the background shader.", new ModeSetting.Visibility(this.background, ACTIVE_BG_MODES), 65, 0, 100);
        this.glowAccent = new BooleanSetting("GlowAccent", "Renders glowing accent lines and header highlights.", true);
        this.categoryIcons = new BooleanSetting("CategoryIcons", "Displays modern icons in frame headers.", true);
        this.triangles = new BooleanSetting("Triangles", "Displays triangle collapse/expand indicators for modules and categories.", false);
        this.switches = new BooleanSetting("Switches", "Displays toggle switch buttons for modules and settings.", true);
    }

    public boolean isRainbow() {
        return this.color.isRainbow();
    }

    private static String[] buildFillModes() {
        Object var0 = EspShader.MODES;
        Object var1 = new String[var0.length];
        var1[0] = "Default";
        System.arraycopy(var0, 1, var1, 1, (var0.length - 1));
        return var1;
    }

    public void onEnable() {
        if (mc.player == null) {
            setToggled(false);
            return;
        }
        HeaderFx.applyScale(LightningClient.CLICK_GUI, this.scale.getValue().floatValue());
        LightningClient.CLICK_GUI.cancelClose();
        mc.gui.setScreen(LightningClient.CLICK_GUI);
    }

    public void onDisable() {
        if (mc.gui.screen() == LightningClient.CLICK_GUI) {
            LightningClient.CLICK_GUI.requestClose();
        } else {
            mc.gui.setScreen(null);
        }
    }

    static {
        FILL_MODES = buildFillModes();
        SHADER_FILL_MODES = ((String[]) Arrays.copyOfRange(FILL_MODES, 1, FILL_MODES.length));
        var tmp0 = new String[6];
        tmp0[0] = "Aurora";
        tmp0[1] = "CyberGrid";
        tmp0[2] = "Constellations";
        tmp0[3] = "MeshGlow";
        tmp0[4] = "DarkFrost";
        tmp0[5] = "None";
        BG_MODES = tmp0;
        var tmp1 = new String[5];
        tmp1[0] = "Aurora";
        tmp1[1] = "CyberGrid";
        tmp1[2] = "Constellations";
        tmp1[3] = "MeshGlow";
        tmp1[4] = "DarkFrost";
        ACTIVE_BG_MODES = tmp1;
    }

}
