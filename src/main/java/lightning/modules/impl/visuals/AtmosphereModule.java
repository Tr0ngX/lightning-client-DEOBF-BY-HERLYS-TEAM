package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import net.minecraft.core.particles.ParticleTypes;

@RegisterModule(name = "Atmosphere", description = "Modifies the world's atmosphere, such as time and color.", category = Module.Category.VISUALS)
public class AtmosphereModule extends Module {
    public BooleanSetting modifyTime;
    public NumberSetting time;
    public BooleanSetting modifyFog;
    public NumberSetting fogStart;
    public NumberSetting fogEnd;
    public ColorSetting fogColor;
    public BooleanSetting modifySky;
    public ModeSetting skyMode;
    public ColorSetting skyColor;
    public ModeSetting customSkyMode;
    public NumberSetting intensity;
    public NumberSetting speed;
    public ColorSetting skyTint;
    public ModeSetting weather;
    public CategorySetting starGlowCategory;
    public BooleanSetting starGlow;
    public NumberSetting starGlowIntensity;

    public AtmosphereModule() {
        this.modifyTime = new BooleanSetting("ModifyTime", "Modifies the world's time.", true);
        this.time = new NumberSetting("Time", "The time that the world will be set to.", new BooleanSetting.Visibility(this.modifyTime, true), 200, -200, 200);
        this.modifyFog = new BooleanSetting("ModifyFog", "Modifies certain things about the world's fog.", false);
        this.fogStart = new NumberSetting("FogStart", "The start value of the world's fog.", new BooleanSetting.Visibility(this.modifyFog, true), 50, 0, 300);
        this.fogEnd = new NumberSetting("FogEnd", "The end value of the world's fog.", new BooleanSetting.Visibility(this.modifyFog, true), 150, 0, 300);
        this.fogColor = new ColorSetting("FogColor", "Modifies the color of the world's fog.", new BooleanSetting.Visibility(this.modifyFog, true), ColorUtils.getDefaultOutlineColor());
        this.modifySky = new BooleanSetting("ModifySky", "Modifies the sky rendering.", false);
        var tmp0 = new String[2];
        tmp0[0] = "Vanilla";
        tmp0[1] = "Custom";
        this.skyMode = new ModeSetting("SkyMode", "The sky rendering mode.", new BooleanSetting.Visibility(this.modifySky, true), "Vanilla", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Vanilla";
        this.skyColor = new ColorSetting("SkyColor", "The color of the vanilla sky.", new ModeSetting.Visibility(this.skyMode, tmp1), new ColorSetting.Color(new Color(0, 0, 0, 255), true, false));
        var tmp2 = new String[1];
        tmp2[0] = "Custom";
        var tmp3 = new String[2];
        tmp3[0] = "Thunder";
        tmp3[1] = "Storm";
        this.customSkyMode = new ModeSetting("CustomSky", "The custom sky shader to use.", new ModeSetting.Visibility(this.skyMode, tmp2), "Thunder", tmp3);
        var tmp4 = new String[1];
        tmp4[0] = "Custom";
        this.intensity = new NumberSetting("Intensity", "The intensity of the sky shader.", new ModeSetting.Visibility(this.skyMode, tmp4), 1.0f, 0.0f, 10.0f);
        var tmp5 = new String[1];
        tmp5[0] = "Custom";
        this.speed = new NumberSetting("Speed", "The speed of the sky shader animation.", new ModeSetting.Visibility(this.skyMode, tmp5), 1.0f, 0.0f, 5.0f);
        var tmp6 = new String[1];
        tmp6[0] = "Custom";
        this.skyTint = new ColorSetting("SkyTint", "The tint of the sky shader.", new ModeSetting.Visibility(this.skyMode, tmp6), new ColorSetting.Color(new Color(255, 255, 255), false, false));
        var tmp7 = new String[6];
        tmp7[0] = "Unchanged";
        tmp7[1] = "Clear";
        tmp7[2] = "Rain";
        tmp7[3] = "Thunder";
        tmp7[4] = "Snow";
        tmp7[5] = "Dust";
        this.weather = new ModeSetting("Weather", "Overrides the world's weather (visual only, not sent to the server).", "Unchanged", tmp7);
        this.starGlowCategory = new CategorySetting("StarGlow", "The category for the star glow effect.");
        this.starGlow = new BooleanSetting("StarGlow", "Enabled", "Adds a soft glow to vanilla sky stars.", new CategorySetting.Visibility(this.starGlowCategory), false);
        this.starGlowIntensity = new NumberSetting("StarGlowIntensity", "Intensity", "How strong the glow is.", new CategorySetting.Visibility(this.starGlowCategory), 3.0f, 0.0f, 1000.0f);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.weather.getValue().equalsIgnoreCase("Snow")) && !(this.weather.getValue().equalsIgnoreCase("Dust"))) return;
        int var2 = this.weather.getValue().equalsIgnoreCase("Snow");
        Object var3 = (!(!var2) ? ParticleTypes.SNOWFLAKE : ParticleTypes.MYCELIUM);
        int var4 = 0;
        while (var4 < 6) {
            double var5 = (mc.player.getX() + ((mc.player.getRandom().nextDouble() - 0.5) * 20.0));
            if (!(!var2)) {
            } else {
            }
            double var7 = ((8.0 + (mc.player.getRandom().nextDouble() * 4.0)) + ((mc.player.getRandom().nextDouble() * 6.0) - 1.0));
            double var9 = (mc.player.getZ() + ((mc.player.getRandom().nextDouble() - 0.5) * 20.0));
            mc.level.addParticle(var3, var5, var7, var9, 0.0, (!(!var2) ? -0.15 : 0.01), 0.0);
            var4++;
        }
    }

}
