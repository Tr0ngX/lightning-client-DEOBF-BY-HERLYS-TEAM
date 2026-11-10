package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.Arrays;
import lightning.LightningClient;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.core.ColorModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ImageSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.EspShader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

@RegisterModule(name = "Shaders", description = "Overlays specified entities with a customizable shader.", category = Module.Category.VISUALS)
public class ShadersModule extends Module {
    public NumberSetting range;
    public CategorySetting targets;
    public BooleanSetting players;
    public BooleanSetting hands;
    public BooleanSetting hostiles;
    public BooleanSetting animals;
    public BooleanSetting ambient;
    public BooleanSetting invisibles;
    public BooleanSetting items;
    public BooleanSetting crystals;
    public BooleanSetting pearls;
    public BooleanSetting boats;
    public BooleanSetting minecarts;
    public BooleanSetting others;
    public ModeSetting mode;
    public NumberSetting glowIntensity;
    public ColorSetting glowColor;
    public NumberSetting bloomWidth;
    public BooleanSetting bloomGlowInside;
    public NumberSetting bloomGlowQuality;
    public NumberSetting bloomGlowMultiplier;
    public NumberSetting bloomFillAlpha;
    public NumberSetting bloomOutlineAlpha;
    public ModeSetting bloomFillMode;
    public ColorSetting bloomGradientColor;
    public NumberSetting bloomGradientFactor;
    public ColorSetting color;
    public ModeSetting friends;
    public ColorSetting friendColor;
    private static final String[] SHADER_MODES;
    private static final String[] SHADER_ACTIVE_MODES;
    public ModeSetting shader;
    public NumberSetting shaderOpacity;
    public NumberSetting shaderSpeed;
    public NumberSetting shaderStep;
    public ColorSetting shaderColor1;
    public ColorSetting shaderColor2;
    public ColorSetting shaderColor3;
    public ColorSetting shaderColor4;
    public ColorSetting shaderGlowColor;
    public ImageSetting image;

    public ShadersModule() {
        this.range = new NumberSetting("Range", "The maximum range at which shaders will be rendered.", 64.0f, 1.0f, 256.0f);
        this.targets = new CategorySetting("Targets", "The things that the shader rendering will be applied onto.");
        this.players = new BooleanSetting("Players", "Renders the shader effect on player entities.", new CategorySetting.Visibility(this.targets), true);
        this.hands = new BooleanSetting("Hands", "Renders the shader effect on first-person hands.", new CategorySetting.Visibility(this.targets), true);
        this.hostiles = new BooleanSetting("Hostiles", "Renders the shader effect on hostile entities.", new CategorySetting.Visibility(this.targets), true);
        this.animals = new BooleanSetting("Animals", "Renders the shader effect on animal entities.", new CategorySetting.Visibility(this.targets), true);
        this.ambient = new BooleanSetting("Ambient", "Renders the shader effect on ambient entities.", new CategorySetting.Visibility(this.targets), false);
        this.invisibles = new BooleanSetting("Invisibles", "Renders the shader effect on invisible entities.", new CategorySetting.Visibility(this.targets), true);
        this.items = new BooleanSetting("Items", "Renders the shader effect on item entities.", new CategorySetting.Visibility(this.targets), true);
        this.crystals = new BooleanSetting("Crystals", "Renders the shader effect on crystal entities.", new CategorySetting.Visibility(this.targets), true);
        this.pearls = new BooleanSetting("Pearl", "Renders the shader effect on thrown ender pearls.", new CategorySetting.Visibility(this.targets), true);
        this.boats = new BooleanSetting("Boat", "Renders the shader effect on all boat types.", new CategorySetting.Visibility(this.targets), true);
        this.minecarts = new BooleanSetting("Minecart", "Renders the shader effect on all minecart types.", new CategorySetting.Visibility(this.targets), true);
        this.others = new BooleanSetting("Others", "Renders the shader effect on miscellaneous entities.", new CategorySetting.Visibility(this.targets), false);
        var tmp0 = new String[5];
        tmp0[0] = "Fill";
        tmp0[1] = "Outline";
        tmp0[2] = "Both";
        tmp0[3] = "Glow";
        tmp0[4] = "Bloom";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the target entities.", "Both", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Glow";
        this.glowIntensity = new NumberSetting("GlowIntensity", "Glow intensity", "How wide and how bright the glow around and over the target entities is.", new ModeSetting.Visibility(this.mode, tmp1), 50, 0, 100);
        var tmp2 = new String[1];
        tmp2[0] = "Glow";
        this.glowColor = new ColorSetting("GlowColor", "The color that will be used for the outer glow effect.", new ModeSetting.Visibility(this.mode, tmp2), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
        var tmp3 = new String[1];
        tmp3[0] = "Bloom";
        this.bloomWidth = new NumberSetting("BloomWidth", "Width", "Bloom outline width/radius.", new ModeSetting.Visibility(this.mode, tmp3), 3.0, 1.0, 10.0);
        var tmp4 = new String[1];
        tmp4[0] = "Bloom";
        this.bloomGlowInside = new BooleanSetting("BloomGlowInside", "GlowInside", "Enables bloom/glow on inside of silhouette.", new ModeSetting.Visibility(this.mode, tmp4), true);
        var tmp5 = new String[1];
        tmp5[0] = "Bloom";
        this.bloomGlowQuality = new NumberSetting("BloomQuality", "Quality", "Glow sample step quality.", new ModeSetting.Visibility(this.mode, tmp5), 1, 1, 4);
        var tmp6 = new String[1];
        tmp6[0] = "Bloom";
        this.bloomGlowMultiplier = new NumberSetting("BloomMultiplier", "Multiplier", "Glow brightness multiplier.", new ModeSetting.Visibility(this.mode, tmp6), 1.0, 0.1, 5.0);
        var tmp7 = new String[1];
        tmp7[0] = "Bloom";
        this.bloomFillAlpha = new NumberSetting("BloomFillAlpha", "FillAlpha", "Alpha opacity of fill.", new ModeSetting.Visibility(this.mode, tmp7), 100, 0, 100);
        var tmp8 = new String[1];
        tmp8[0] = "Bloom";
        this.bloomOutlineAlpha = new NumberSetting("BloomOutlineAlpha", "OutlineAlpha", "Alpha opacity of outline/glow.", new ModeSetting.Visibility(this.mode, tmp8), 100, 0, 100);
        var tmp9 = new String[1];
        tmp9[0] = "Bloom";
        var tmp10 = new String[3];
        tmp10[0] = "Color";
        tmp10[1] = "Gradient";
        tmp10[2] = "Rainbow";
        this.bloomFillMode = new ModeSetting("BloomFillMode", "FillMode", "Fill pattern mode.", new ModeSetting.Visibility(this.mode, tmp9), "Color", tmp10);
        var tmp11 = new String[1];
        tmp11[0] = "Bloom";
        this.bloomGradientColor = new ColorSetting("BloomGradientColor", "GradientColor", "Gradient target color.", new ModeSetting.Visibility(this.mode, tmp11), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        var tmp12 = new String[1];
        tmp12[0] = "Bloom";
        this.bloomGradientFactor = new NumberSetting("BloomGradientFactor", "GradientFactor", "Scale factor for gradient fill.", new ModeSetting.Visibility(this.mode, tmp12), 200.0, 10.0, 500.0);
        this.color = new ColorSetting("Color", "The color that will be used for the fill rendering.", ColorUtils.getDefaultColor());
        var tmp13 = new String[3];
        tmp13[0] = "Default";
        tmp13[1] = "Custom";
        tmp13[2] = "Sync";
        this.friends = new ModeSetting("Friends", "The color that will be applied to friended entities.", "Default", tmp13);
        var tmp14 = new String[1];
        tmp14[0] = "Custom";
        this.friendColor = new ColorSetting("FriendColor", "The color that will be used for the shader effect on friends.", new ModeSetting.Visibility(this.friends, tmp14), new ColorSetting.Color(new Color(85, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        this.shader = new ModeSetting("Shader", "The animated shader that will be drawn on the target entities instead of a flat color.", "None", SHADER_MODES);
        this.shaderOpacity = new NumberSetting("ShaderOpacity", "Opacity", "The opacity of the animated shader pattern itself, independent of Color's own alpha.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 100, 0, 100);
        this.shaderSpeed = new NumberSetting("ShaderSpeed", "Speed", "The speed at which the shader animates.", new ModeSetting.Visibility(this.shader, SHADER_ACTIVE_MODES), 1.0f, 0.1f, 10.0f);
        var tmp15 = new String[1];
        tmp15[0] = "Gradient";
        this.shaderStep = new NumberSetting("ShaderStep", "Step", "The size of the gradient bands.", new ModeSetting.Visibility(this.shader, tmp15), 50.0f, 0.1f, 200.0f);
        var tmp16 = new String[1];
        tmp16[0] = "Gradient";
        this.shaderColor1 = new ColorSetting("ShaderColor1", "The first gradient color.", new ModeSetting.Visibility(this.shader, tmp16), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
        var tmp17 = new String[1];
        tmp17[0] = "Gradient";
        this.shaderColor2 = new ColorSetting("ShaderColor2", "The second gradient color.", new ModeSetting.Visibility(this.shader, tmp17), new ColorSetting.Color(new Color(255, 0, 0, 255), false, false));
        var tmp18 = new String[1];
        tmp18[0] = "Gradient";
        this.shaderColor3 = new ColorSetting("ShaderColor3", "The third gradient color.", new ModeSetting.Visibility(this.shader, tmp18), new ColorSetting.Color(new Color(0, 255, 0, 255), false, false));
        var tmp19 = new String[1];
        tmp19[0] = "Gradient";
        this.shaderColor4 = new ColorSetting("ShaderColor4", "The fourth gradient color.", new ModeSetting.Visibility(this.shader, tmp19), new ColorSetting.Color(new Color(0, 0, 255, 255), false, false));
        var tmp20 = new String[1];
        tmp20[0] = "Glowing";
        this.shaderGlowColor = new ColorSetting("ShaderGlowColor", "GlowColor", "The color that the glow shader will be tinted with.", new ModeSetting.Visibility(this.shader, tmp20), new ColorSetting.Color(new Color(255, 0, 255, 255), false, false));
        var tmp21 = new String[1];
        tmp21[0] = "Image";
        this.image = new ImageSetting("Image", "Select", "Select an image from the images directory to project onto targets.", new ModeSetting.Visibility(this.shader, tmp21), "None");
    }

    public void onEnable() {
        if (LightningClient.IMAGE_MANAGER != null && this.image != null) {
            LightningClient.IMAGE_MANAGER.setCurrentActiveImage(this.image.getValue());
        }
    }

    public boolean inRange(Entity arg0) {
        if ((arg0 == null) || (mc.player == null)) {
            return true;
        }
        if (arg0 == mc.player) return true;
        return mc.player.distanceToSqr(arg0) <= ((double) (this.range.getValue().floatValue() * this.range.getValue().floatValue()));
    }

    public boolean isValidEntity(Entity arg0) {
        if (!(inRange(arg0))) return false;
        if (this.players.getValue() && arg0.getType() == EntityTypes.PLAYER) return true;
        if (this.hostiles.getValue() && arg0.getType().getCategory() == MobCategory.MONSTER) return true;
        if (this.animals.getValue()) {
            if (((((arg0.getType().getCategory() == MobCategory.CREATURE) || (arg0.getType().getCategory() == MobCategory.WATER_CREATURE)) || (arg0.getType().getCategory() == MobCategory.WATER_AMBIENT)) || (arg0.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE)) || (arg0.getType().getCategory() == MobCategory.AXOLOTLS)) {
                return true;
            }
        }
        if (this.ambient.getValue() && arg0.getType().getCategory() == MobCategory.AMBIENT) return true;
        if (this.invisibles.getValue() && arg0.isInvisible()) return true;
        if (this.items.getValue()) {
            if ((arg0.getType() == EntityTypes.ITEM) || (arg0.getType() == EntityTypes.EXPERIENCE_BOTTLE)) {
                return true;
            }
        }
        if (this.crystals.getValue() && arg0.getType() == EntityTypes.END_CRYSTAL) return true;
        if (this.pearls.getValue()) {
            if (((arg0 instanceof ThrownEnderpearl)) || (arg0.getType() == EntityTypes.ENDER_PEARL)) {
                return true;
            }
        }
        if (this.boats.getValue() && (arg0 instanceof AbstractBoat)) return true;
        if (this.minecarts.getValue() && (arg0 instanceof AbstractMinecart)) return true;
        return this.others.getValue();
    }

    public boolean shouldOutline() {
        if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        return false;
    }

    public boolean shouldFill() {
        if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
        } else {
        }
        return false;
    }

    public int getFillColor(Entity arg0) {
        return (getColor(arg0).getRGB() | -16777216);
    }

    public Color getColor(Entity arg0) {
        if (!((arg0 instanceof Player))) break label_2;
        var tmp0 = ((Player) arg0);
        Object var2 = tmp0;
        if ((!(LightningClient.FRIEND_MANAGER.contains(tmp0.getName().getString()))) || (this.friends.getValue().equals("Sync"))) break label_2;
        if (this.friends.getValue().equals("Default")) {
        } else {
        }
        return this.friendColor.getColor();
        label_2:
        return this.color.getColor();
    }

    public static int getBloomFillModeIndex(String arg0) {
        Object var1 = arg0;
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1656737386 -> { break label_1; }
            case 154295120 -> { break label_0; }
            default -> { break label_2; }
        }
        label_0:
        if (!(var1.equals("Gradient"))) break label_2;
        var2 = 0;
        break label_2;
        label_1:
        if (!(var1.equals("Rainbow"))) break label_2;
        var2 = 1;
        label_2:
        switch (var2) {
            case 0 -> { break label_3; }
            case 1 -> { break label_4; }
            default -> { break label_5; }
        }
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        return 0;
    }

    public static Identifier pickActiveOutlineChain() {
        Object var0 = ((ShadersModule) LightningClient.MODULE_MANAGER.getModule(ShadersModule.class));
        if (var0.isToggled()) {
            if (var0.mode.getValue().equalsIgnoreCase("Glow")) return Identifier.fromNamespaceAndPath("lightning", "outline_glow");
            if (var0.mode.getValue().equalsIgnoreCase("Bloom")) return Identifier.fromNamespaceAndPath("lightning", "outline_bloom");
            return pickVariant(var0.shouldFill(), var0.shouldOutline(), var0.color.getColor().getAlpha());
        }
        Object var1 = ((PopChamsModule) LightningClient.MODULE_MANAGER.getModule(PopChamsModule.class));
        if (var1.isToggled()) {
            if (!(var1.mode.getValue().equalsIgnoreCase("Fill"))) {
                if (var1.mode.getValue().equalsIgnoreCase("Both")) {
                }
            } else {
            }
            int var2 = 0;
            if (!(var1.mode.getValue().equalsIgnoreCase("Outline"))) {
                if (var1.mode.getValue().equalsIgnoreCase("Both")) {
                }
            } else {
            }
            int var3 = 0;
            return pickVariant(var2, var3, var1.fillColor.getColor().getAlpha());
        }
        return null;
    }

    public static EspShader.Settings shaderSettings() {
        Object var0 = ((ShadersModule) LightningClient.MODULE_MANAGER.getModule(ShadersModule.class));
        if ((var0 == null) || (!(var0.isToggled()))) {
            return new EspShader.Settings(0, 0.0f, 1.0f, 1.0f, 1.0f, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
        }
        int var1 = EspShader.modeIndex(var0.shader.getValue());
        float var2 = (((float) var0.color.getColor().getAlpha()) / 255.0f);
        float var3 = (var0.shaderOpacity.getValue().floatValue() / 100.0f);
        if (LightningClient.MODULE_MANAGER == null) break label_2;
        break label_3;
        label_2:
        label_3:
        Object var4 = null;
        if (var4 != null) {
        } else {
        }
        float var5 = 6.0f;
        if (var4 != null) {
        } else {
        }
        float var6 = 1.0f;
        if (var4 != null && var4.rainbowScale != null) {
        } else {
        }
        float var7 = 1.0f;
        if (var1 == 1) {
        } else {
        }
        float var8 = var7;
        float var9 = ((float) (1000.0 / Math.max(200.0, (10500.0 - (500.0 * ((double) var5))))));
        if (!(!var1) && var1 != 2 && var1 != 10) {
        } else {
            if (var1 == 2) {
            } else {
            }
        }
        float var10 = (var0.shaderSpeed.getValue().floatValue() * 1.0f);
        float var11 = ((((float) (((double) (System.currentTimeMillis() % 20000000L)) / 1000.0)) * var10) * (var1 == 1 ? var8 : 1.0f));
        if (!(!var1)) {
            if (var1 == 7) {
            } else {
            }
            return new EspShader.Settings;
        }
        return new EspShader.Settings(0, var11, var8, var2, var6, var0.color.getColor(), Color.WHITE, Color.WHITE, Color.WHITE);
    }

    public static float glowIntensity() {
        Object var0 = ((ShadersModule) LightningClient.MODULE_MANAGER.getModule(ShadersModule.class));
        if ((!(var0.isToggled())) || (!(var0.mode.getValue().equalsIgnoreCase("Glow")))) {
            return 0.0f;
        }
        return (var0.glowIntensity.getValue().floatValue() / 100.0f);
    }

    public static Identifier pickVariant(boolean arg0, boolean arg1, int arg2) {
        if (!(!arg1) && !arg0) return Identifier.fromNamespaceAndPath("lightning", "outline_outline");
        int var3 = Math.max(20, Math.min(100, (((int) Math.round(((((double) arg2) / 255.0) * 5.0))) * 20)));
        if (!arg0) break label_1;
        Object var4 = (!(!arg1) ? "outline_both_" : "outline_fill_");
        return Identifier.fromNamespaceAndPath("lightning", (var4 + var3));
    }

    static {
        SHADER_MODES = EspShader.MODES;
        SHADER_ACTIVE_MODES = ((String[]) Arrays.copyOfRange(SHADER_MODES, 1, SHADER_MODES.length));
    }

}
