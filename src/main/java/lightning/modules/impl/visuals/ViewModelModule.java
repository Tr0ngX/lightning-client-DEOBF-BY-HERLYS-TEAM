package lightning.modules.impl.visuals;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "ViewModel", description = "Modifies the position, scale and rotation of the player viewmodel.", category = Module.Category.VISUALS)
public class ViewModelModule extends Module {
    public final ModeSetting mode;
    public final BooleanSetting mainhandSwap;
    public final BooleanSetting offhandSwap;
    public final CategorySetting mainHandCategory;
    public final NumberSetting scaleMainX;
    public final NumberSetting scaleMainY;
    public final NumberSetting scaleMainZ;
    public final NumberSetting positionMainX;
    public final NumberSetting positionMainY;
    public final NumberSetting positionMainZ;
    public final NumberSetting rotationMainX;
    public final NumberSetting rotationMainY;
    public final NumberSetting rotationMainZ;
    public final CategorySetting offHandCategory;
    public final NumberSetting scaleOffX;
    public final NumberSetting scaleOffY;
    public final NumberSetting scaleOffZ;
    public final NumberSetting positionOffX;
    public final NumberSetting positionOffY;
    public final NumberSetting positionOffZ;
    public final NumberSetting rotationOffX;
    public final NumberSetting rotationOffY;
    public final NumberSetting rotationOffZ;
    public final CategorySetting eatingCategory;
    public final BooleanSetting eatAnimation;
    public final NumberSetting eatX;
    public final NumberSetting eatY;
    public final NumberSetting eatingMultiplier;

    public ViewModelModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Each";
        tmp0[1] = "Both";
        this.mode = new ModeSetting("Mode", "Hand modification mode.", "Each", tmp0);
        this.mainhandSwap = new BooleanSetting("MainhandSwap", "Disables the drop down animation when switching mainhand items.", true);
        this.offhandSwap = new BooleanSetting("OffhandSwap", "Disables the drop down animation when switching offhand items.", true);
        this.mainHandCategory = new CategorySetting("MainHand", "Settings for main hand item rendering.");
        this.scaleMainX = new NumberSetting("ScaleMainX", "Scale X", "Scale on X axis.", new CategorySetting.Visibility(this.mainHandCategory), 1.0f, 0.1f, 5.0f);
        this.scaleMainY = new NumberSetting("ScaleMainY", "Scale Y", "Scale on Y axis.", new CategorySetting.Visibility(this.mainHandCategory), 1.0f, 0.1f, 5.0f);
        this.scaleMainZ = new NumberSetting("ScaleMainZ", "Scale Z", "Scale on Z axis.", new CategorySetting.Visibility(this.mainHandCategory), 1.0f, 0.1f, 5.0f);
        this.positionMainX = new NumberSetting("PositionMainX", "Pos X", "Translation on X axis.", new CategorySetting.Visibility(this.mainHandCategory), 0.0f, -3.0f, 3.0f);
        this.positionMainY = new NumberSetting("PositionMainY", "Pos Y", "Translation on Y axis.", new CategorySetting.Visibility(this.mainHandCategory), 0.0f, -3.0f, 3.0f);
        this.positionMainZ = new NumberSetting("PositionMainZ", "Pos Z", "Translation on Z axis.", new CategorySetting.Visibility(this.mainHandCategory), 0.0f, -3.0f, 3.0f);
        this.rotationMainX = new NumberSetting("RotationMainX", "Rot X", "Rotation on X axis.", new CategorySetting.Visibility(this.mainHandCategory), 0.0f, -180.0f, 180.0f);
        this.rotationMainY = new NumberSetting("RotationMainY", "Rot Y", "Rotation on Y axis.", new CategorySetting.Visibility(this.mainHandCategory), 0.0f, -180.0f, 180.0f);
        this.rotationMainZ = new NumberSetting("RotationMainZ", "Rot Z", "Rotation on Z axis.", new CategorySetting.Visibility(this.mainHandCategory), 0.0f, -180.0f, 180.0f);
        var tmp1 = new String[1];
        tmp1[0] = "Each";
        this.offHandCategory = new CategorySetting("OffHand", "Settings for off hand item rendering.", new ModeSetting.Visibility(this.mode, tmp1));
        this.scaleOffX = new NumberSetting("ScaleOffX", "Scale X", "Scale on X axis.", new CategorySetting.Visibility(this.offHandCategory), 1.0f, 0.1f, 5.0f);
        this.scaleOffY = new NumberSetting("ScaleOffY", "Scale Y", "Scale on Y axis.", new CategorySetting.Visibility(this.offHandCategory), 1.0f, 0.1f, 5.0f);
        this.scaleOffZ = new NumberSetting("ScaleOffZ", "Scale Z", "Scale on Z axis.", new CategorySetting.Visibility(this.offHandCategory), 1.0f, 0.1f, 5.0f);
        this.positionOffX = new NumberSetting("PositionOffX", "Pos X", "Translation on X axis.", new CategorySetting.Visibility(this.offHandCategory), 0.0f, -3.0f, 3.0f);
        this.positionOffY = new NumberSetting("PositionOffY", "Pos Y", "Translation on Y axis.", new CategorySetting.Visibility(this.offHandCategory), 0.0f, -3.0f, 3.0f);
        this.positionOffZ = new NumberSetting("PositionOffZ", "Pos Z", "Translation on Z axis.", new CategorySetting.Visibility(this.offHandCategory), 0.0f, -3.0f, 3.0f);
        this.rotationOffX = new NumberSetting("RotationOffX", "Rot X", "Rotation on X axis.", new CategorySetting.Visibility(this.offHandCategory), 0.0f, -180.0f, 180.0f);
        this.rotationOffY = new NumberSetting("RotationOffY", "Rot Y", "Rotation on Y axis.", new CategorySetting.Visibility(this.offHandCategory), 0.0f, -180.0f, 180.0f);
        this.rotationOffZ = new NumberSetting("RotationOffZ", "Rot Z", "Rotation on Z axis.", new CategorySetting.Visibility(this.offHandCategory), 0.0f, -180.0f, 180.0f);
        this.eatingCategory = new CategorySetting("Eating", "Settings for eating animation.");
        this.eatAnimation = new BooleanSetting("EatAnimation", "Modify eating animation position.", new CategorySetting.Visibility(this.eatingCategory), false);
        this.eatX = new NumberSetting("EatX", "Eat X", "Eating offset X.", new BooleanSetting.Visibility(this.eatAnimation, true), 1.0f, -1.0f, 2.0f);
        this.eatY = new NumberSetting("EatY", "Eat Y", "Eating offset Y.", new BooleanSetting.Visibility(this.eatAnimation, true), 1.0f, -1.0f, 2.0f);
        this.eatingMultiplier = new NumberSetting("EatingMultiplier", "Eating Multiplier", "Scales the eating bob: 1 = vanilla, 0 = no bobbing (only the bring-to-mouth motion stays).", new CategorySetting.Visibility(this.eatingCategory), 1.0f, 0.0f, 1.0f, 0.1f);
    }

}
