package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Glint;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.mixins.IChamsCapture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@RegisterModule(name = "Chams", description = "Adds a customizable render on top of the default Minecraft rendering.", category = Module.Category.VISUALS)
public class ChamsModule extends Module {
    public NumberSetting range;
    public CategorySetting entitiesCategory;
    public BooleanSetting players;
    public BooleanSetting hostiles;
    public BooleanSetting passives;
    public BooleanSetting entityPulse;
    public BooleanSetting entityShine;
    public BooleanSetting entityGlint;
    public ColorSetting entityGlintColor;
    public NumberSetting entityGlintSpeedU;
    public NumberSetting entityGlintSpeedV;
    public NumberSetting entityGlintScale;
    public ModeSetting entityGlintDirection;
    public ModeSetting entityMode;
    public ColorSetting entityFillColor;
    public ColorSetting entityOutlineColor;
    public ModeSetting friendMode;
    public ColorSetting friendFillColor;
    public ColorSetting friendOutlineColor;
    public BooleanSetting damageModify;
    public ColorSetting damageColor;
    public NumberSetting entityOpacity;
    public CategorySetting crystalsCategory;
    public BooleanSetting crystals;
    public BooleanSetting crystalPulse;
    public BooleanSetting crystalShine;
    public BooleanSetting crystalGlint;
    public ColorSetting crystalGlintColor;
    public NumberSetting crystalGlintSpeedU;
    public NumberSetting crystalGlintSpeedV;
    public NumberSetting crystalGlintScale;
    public ModeSetting crystalGlintDirection;
    public ModeSetting crystalMode;
    public ColorSetting crystalFillColor;
    public ColorSetting crystalOutlineColor;
    public NumberSetting crystalOpacity;
    public NumberSetting crystalSize;
    public CategorySetting vehiclesCategory;
    public BooleanSetting vehicles;
    public BooleanSetting vehiclePulse;
    public BooleanSetting vehicleShine;
    public ModeSetting vehicleMode;
    public ColorSetting vehicleFillColor;
    public ColorSetting vehicleOutlineColor;
    public NumberSetting vehicleOpacity;

    public ChamsModule() {
        this.range = new NumberSetting("Range", "The maximum range at which chams will be rendered.", 64.0f, 1.0f, 256.0f);
        this.entitiesCategory = new CategorySetting("Entities", "The category for settings related to chams rendered on living entities.");
        this.players = new BooleanSetting("Players", "Renders the chams on player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
        this.hostiles = new BooleanSetting("Hostiles", "Renders the chams on hostile entities.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.passives = new BooleanSetting("Passives", "Renders the chams on passive entities.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.entityPulse = new BooleanSetting("EntityPulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.entityShine = new BooleanSetting("EntityShine", "Shine", "Adds a shine effect to the chams.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.entityGlint = new BooleanSetting("EntityGlint", "Glint", "Adds a scrolling texture overlay to living entities.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.entityGlintColor = new ColorSetting("EntityGlintColor", "GlintColor", "Color tint for the entity glint overlay.", new BooleanSetting.Visibility(this.entityGlint, true), new ColorSetting.Color(new Color(255, 255, 255, 180), false, false));
        this.entityGlintSpeedU = new NumberSetting("EntityGlintSpeedU", "GlintSpeedU", "Horizontal scroll speed of entity glint.", new BooleanSetting.Visibility(this.entityGlint, true), 0.25f, -5.0f, 5.0f);
        this.entityGlintSpeedV = new NumberSetting("EntityGlintSpeedV", "GlintSpeedV", "Vertical scroll speed of entity glint.", new BooleanSetting.Visibility(this.entityGlint, true), 0.1f, -5.0f, 5.0f);
        this.entityGlintScale = new NumberSetting("EntityGlintScale", "GlintScale", "Scale of the glint texture on entity models.", new BooleanSetting.Visibility(this.entityGlint, true), 1.0f, 0.1f, 5.0f);
        var tmp0 = new String[3];
        tmp0[0] = "Diagonal";
        tmp0[1] = "Horizontal";
        tmp0[2] = "Vertical";
        this.entityGlintDirection = new ModeSetting("EntityGlintDirection", "GlintDirection", "Projection mode for entity glint UVs.", new BooleanSetting.Visibility(this.entityGlint, true), "Diagonal", tmp0);
        var tmp1 = new String[3];
        tmp1[0] = "Fill";
        tmp1[1] = "Outline";
        tmp1[2] = "Both";
        this.entityMode = new ModeSetting("EntityMode", "Mode", "The rendering that will be applied to living entities.", new CategorySetting.Visibility(this.entitiesCategory), "Both", tmp1);
        var tmp2 = new String[2];
        tmp2[0] = "Fill";
        tmp2[1] = "Both";
        this.entityFillColor = new ColorSetting("EntityFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.entityMode, tmp2), ColorUtils.getDefaultFillColor());
        var tmp3 = new String[2];
        tmp3[0] = "Outline";
        tmp3[1] = "Both";
        this.entityOutlineColor = new ColorSetting("EntityOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.entityMode, tmp3), ColorUtils.getDefaultOutlineColor());
        var tmp4 = new String[2];
        tmp4[0] = "Fill";
        tmp4[1] = "Both";
        var tmp5 = new String[3];
        tmp5[0] = "Default";
        tmp5[1] = "Custom";
        tmp5[2] = "Sync";
        this.friendMode = new ModeSetting("FriendMode", "The mode for the friend color.", new ModeSetting.Visibility(this.entityMode, tmp4), "Default", tmp5);
        var tmp6 = new String[2];
        tmp6[0] = "Fill";
        tmp6[1] = "Both";
        this.friendFillColor = new ColorSetting("FriendFillColor", "FriendFillColor", "The color that will be used for the fill rendering on friends.", new ModeSetting.Visibility(this.entityMode, tmp6), new ColorSetting.Color(new Color(85, 255, 255, ColorUtils.getDefaultFillColor().getColor().getAlpha()), false, false));
        var tmp7 = new String[2];
        tmp7[0] = "Outline";
        tmp7[1] = "Both";
        this.friendOutlineColor = new ColorSetting("FriendOutlineColor", "FriendOutlineColor", "The color that will be used for the outline rendering on friends.", new ModeSetting.Visibility(this.entityMode, tmp7), new ColorSetting.Color(new Color(85, 255, 255, ColorUtils.getDefaultOutlineColor().getColor().getAlpha()), false, false));
        var tmp8 = new String[2];
        tmp8[0] = "Fill";
        tmp8[1] = "Both";
        this.damageModify = new BooleanSetting("DamageModify", "Changes the color of the chams when a player takes damage.", new ModeSetting.Visibility(this.entityMode, tmp8), false);
        this.damageColor = new ColorSetting("DamageColor", "The color to apply on chams when a player takes damage.", new BooleanSetting.Visibility(this.damageModify, true), new ColorSetting.Color(new Color(255, 0, 0), false, false));
        this.entityOpacity = new NumberSetting("EntityOpacity", "Opacity", "How see-through the target's real skin renders (100 = untouched).", new CategorySetting.Visibility(this.entitiesCategory), 100, 0, 100);
        this.crystalsCategory = new CategorySetting("Crystals", "The category for settings related to crystal chams.");
        this.crystals = new BooleanSetting("Crystals", "Enabled", "Renders the chams on crystal entities.", new CategorySetting.Visibility(this.crystalsCategory), true);
        this.crystalPulse = new BooleanSetting("CrystalPulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.Visibility(this.crystalsCategory), false);
        this.crystalShine = new BooleanSetting("CrystalShine", "Shine", "Adds a shine effect on crystal chams.", new CategorySetting.Visibility(this.crystalsCategory), false);
        this.crystalGlint = new BooleanSetting("CrystalGlint", "Glint", "Adds a scrolling texture overlay to crystals.", new CategorySetting.Visibility(this.crystalsCategory), false);
        this.crystalGlintColor = new ColorSetting("CrystalGlintColor", "GlintColor", "Color tint for the crystal glint overlay.", new BooleanSetting.Visibility(this.crystalGlint, true), new ColorSetting.Color(new Color(255, 255, 255, 180), false, false));
        this.crystalGlintSpeedU = new NumberSetting("CrystalGlintSpeedU", "GlintSpeedU", "Horizontal scroll speed of crystal glint.", new BooleanSetting.Visibility(this.crystalGlint, true), 0.25f, -5.0f, 5.0f);
        this.crystalGlintSpeedV = new NumberSetting("CrystalGlintSpeedV", "GlintSpeedV", "Vertical scroll speed of crystal glint.", new BooleanSetting.Visibility(this.crystalGlint, true), 0.1f, -5.0f, 5.0f);
        this.crystalGlintScale = new NumberSetting("CrystalGlintScale", "GlintScale", "Scale of the glint texture on crystal models.", new BooleanSetting.Visibility(this.crystalGlint, true), 1.0f, 0.1f, 5.0f);
        var tmp9 = new String[3];
        tmp9[0] = "Diagonal";
        tmp9[1] = "Horizontal";
        tmp9[2] = "Vertical";
        this.crystalGlintDirection = new ModeSetting("CrystalGlintDirection", "GlintDirection", "Projection mode for crystal glint UVs.", new BooleanSetting.Visibility(this.crystalGlint, true), "Diagonal", tmp9);
        var tmp10 = new String[3];
        tmp10[0] = "Fill";
        tmp10[1] = "Outline";
        tmp10[2] = "Both";
        this.crystalMode = new ModeSetting("CrystalMode", "Mode", "The rendering that will be applied to crystal entities.", new CategorySetting.Visibility(this.crystalsCategory), "Both", tmp10);
        var tmp11 = new String[2];
        tmp11[0] = "Fill";
        tmp11[1] = "Both";
        this.crystalFillColor = new ColorSetting("CrystalFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.crystalMode, tmp11), ColorUtils.getDefaultFillColor());
        var tmp12 = new String[2];
        tmp12[0] = "Outline";
        tmp12[1] = "Both";
        this.crystalOutlineColor = new ColorSetting("CrystalOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.crystalMode, tmp12), ColorUtils.getDefaultOutlineColor());
        this.crystalOpacity = new NumberSetting("CrystalOpacity", "Opacity", "How see-through the crystal's real texture renders (100 = untouched).", new CategorySetting.Visibility(this.crystalsCategory), 100, 0, 100);
        this.crystalSize = new NumberSetting("CrystalSize", "Size", "Scales the crystal model (1.0 = vanilla size).", new CategorySetting.Visibility(this.crystalsCategory), 1.0f, 0.1f, 5.0f);
        this.vehiclesCategory = new CategorySetting("Vehicles", "The category for settings related to boat/minecart chams.");
        this.vehicles = new BooleanSetting("Vehicles", "Enabled", "Renders the chams on all boat and minecart types.", new CategorySetting.Visibility(this.vehiclesCategory), true);
        this.vehiclePulse = new BooleanSetting("VehiclePulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.Visibility(this.vehiclesCategory), false);
        this.vehicleShine = new BooleanSetting("VehicleShine", "Shine", "Adds a shine effect on vehicle chams.", new CategorySetting.Visibility(this.vehiclesCategory), false);
        var tmp13 = new String[3];
        tmp13[0] = "Fill";
        tmp13[1] = "Outline";
        tmp13[2] = "Both";
        this.vehicleMode = new ModeSetting("VehicleMode", "Mode", "The rendering that will be applied to boat/minecart entities.", new CategorySetting.Visibility(this.vehiclesCategory), "Both", tmp13);
        var tmp14 = new String[2];
        tmp14[0] = "Fill";
        tmp14[1] = "Both";
        this.vehicleFillColor = new ColorSetting("VehicleFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.vehicleMode, tmp14), ColorUtils.getDefaultFillColor());
        var tmp15 = new String[2];
        tmp15[0] = "Outline";
        tmp15[1] = "Both";
        this.vehicleOutlineColor = new ColorSetting("VehicleOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.vehicleMode, tmp15), ColorUtils.getDefaultOutlineColor());
        this.vehicleOpacity = new NumberSetting("VehicleOpacity", "Opacity", "How see-through the vehicle's real texture renders (100 = untouched).", new CategorySetting.Visibility(this.vehiclesCategory), 100, 0, 100);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (this.entityGlint.getValue()) {
            Glint.ENTITY.speedU = this.entityGlintSpeedU.getValue().floatValue();
            Glint.ENTITY.speedV = this.entityGlintSpeedV.getValue().floatValue();
            Glint.ENTITY.scale = this.entityGlintScale.getValue().floatValue();
            Glint.ENTITY.direction = this.entityGlintDirection.getValue();
        }
        if (this.crystalGlint.getValue()) {
            Glint.CRYSTAL.speedU = this.crystalGlintSpeedU.getValue().floatValue();
            Glint.CRYSTAL.speedV = this.crystalGlintSpeedV.getValue().floatValue();
            Glint.CRYSTAL.scale = this.crystalGlintScale.getValue().floatValue();
            Glint.CRYSTAL.direction = this.crystalGlintDirection.getValue();
        }
        if (!(this.entityGlint.getValue())) {
            if (this.crystalGlint.getValue()) {
            }
            Glint.tick();
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
        if (this.hostiles.getValue() && EntityUtils.isHostile(arg0)) return true;
        if ((!(this.passives.getValue())) || (!(EntityUtils.isAnimal(arg0)))) break label_3;
        return !(EntityUtils.isHostile(arg0));
    }

    public void applyEntityChams(LivingEntity arg0, IChamsCapture arg1) {
        if (!(this.entityMode.getValue().equals("Fill"))) {
            if (this.entityMode.getValue().equals("Both")) {
            }
        } else {
        }
        int var3 = 0;
        if (!(this.entityMode.getValue().equals("Outline"))) {
            if (this.entityMode.getValue().equals("Both")) {
            }
        } else {
        }
        int var4 = 0;
        arg1.night$setChams(var3, pickEntityColor(arg0, this.entityFillColor.getColor(), this.friendFillColor.getColor()).getRGB(), var4, pickEntityColor(arg0, this.entityOutlineColor.getColor(), this.friendOutlineColor.getColor()).getRGB(), this.entityShine.getValue());
        arg1.night$setChamsRealAlpha((this.entityOpacity.getValue().floatValue() / 100.0f));
        if (this.entityGlint.getValue()) {
            arg1.night$setChamsGlint(true, this.entityGlintColor.getColor().getRGB(), Glint.ENTITY);
        }
    }

    public void applyCrystalChams(IChamsCapture arg0) {
        if (!(this.crystalMode.getValue().equals("Fill"))) {
            if (!(this.crystalMode.getValue().equals("Both"))) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        int var2 = 0;
        if (!(this.crystalMode.getValue().equals("Outline"))) {
            if (this.crystalMode.getValue().equals("Both")) {
            }
        } else {
        }
        int var3 = 0;
        if (this.crystalPulse.getValue()) {
        } else {
        }
        Object var4 = this.crystalFillColor.getColor();
        if (this.crystalPulse.getValue()) {
        } else {
        }
        Object var5 = this.crystalOutlineColor.getColor();
        arg0.night$setChams(var2, var4.getRGB(), var3, var5.getRGB(), this.crystalShine.getValue());
        arg0.night$setChamsRealAlpha((this.crystalOpacity.getValue().floatValue() / 100.0f));
        if (this.crystalGlint.getValue()) {
            arg0.night$setChamsGlint(true, this.crystalGlintColor.getColor().getRGB(), Glint.CRYSTAL);
        }
    }

    public void applyVehicleChams(IChamsCapture arg0) {
        if (!(this.vehicleMode.getValue().equals("Fill"))) {
            if (this.vehicleMode.getValue().equals("Both")) {
            }
        } else {
        }
        int var2 = 0;
        if (!(this.vehicleMode.getValue().equals("Outline"))) {
            if (this.vehicleMode.getValue().equals("Both")) {
            }
        } else {
        }
        int var3 = 0;
        if (this.vehiclePulse.getValue()) {
        } else {
        }
        Object var4 = this.vehicleFillColor.getColor();
        if (this.vehiclePulse.getValue()) {
        } else {
        }
        Object var5 = this.vehicleOutlineColor.getColor();
        arg0.night$setChams(var2, var4.getRGB(), var3, var5.getRGB(), this.vehicleShine.getValue());
        arg0.night$setChamsRealAlpha((this.vehicleOpacity.getValue().floatValue() / 100.0f));
    }

    private Color pickEntityColor(LivingEntity arg0, Color arg1, Color arg2) {
        if (!(this.damageModify.getValue())) break label_0;
        var tmp0 = (arg0.hurtTime > 0 ? 1 : 0);
        int var5 = tmp0;
        int var6 = tmp0;
        if (!((arg0 instanceof Player))) break label_4;
        var tmp1 = ((Player) arg0);
        Object var4 = tmp1;
        if ((!(LightningClient.FRIEND_MANAGER.contains(tmp1.getName().getString()))) || (this.friendMode.getValue().equals("Sync"))) break label_4;
        if (!(!var5)) {
            break label_6;
        }
        if (this.friendMode.getValue().equals("Default")) {
            break label_6;
        }
        break label_6;
        label_4:
        if (!(!var5)) {
            break label_6;
        }
        label_6:
        Object var7 = arg1;
        if (this.entityPulse.getValue()) {
        } else {
        }
        return var7;
    }

}
