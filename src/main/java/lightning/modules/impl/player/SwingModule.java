package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "Swing", description = "Allows you to modify the swing animation and the hand that will be used for swinging.", category = Module.Category.PLAYER)
public class SwingModule extends Module {
    public ModeSetting hand;
    public BooleanSetting noPacket;
    public CategorySetting speedCategory;
    public BooleanSetting modifySpeed;
    public NumberSetting speed;
    public CategorySetting translationCategory;
    public BooleanSetting translateX;
    public BooleanSetting translateY;
    public BooleanSetting translateZ;
    public CategorySetting rotationCategory;
    public BooleanSetting rotationX;
    public BooleanSetting rotationY;
    public BooleanSetting rotationZ;

    public SwingModule() {
        var tmp0 = new String[6];
        tmp0[0] = "Default";
        tmp0[1] = "None";
        tmp0[2] = "Packet";
        tmp0[3] = "Mainhand";
        tmp0[4] = "Offhand";
        tmp0[5] = "Both";
        this.hand = new ModeSetting("Hand", "The hand that will be used for swinging.", "Default", tmp0);
        var tmp1 = new String[4];
        tmp1[0] = "Default";
        tmp1[1] = "Mainhand";
        tmp1[2] = "Offhand";
        tmp1[3] = "Both";
        this.noPacket = new BooleanSetting("NoPacket", "Only swings clientside, meaning nobody will be able to tell if you're swinging or not.", new ModeSetting.Visibility(this.hand, tmp1), false);
        this.speedCategory = new CategorySetting("Speed", "The category containing all settings related to swing speed.");
        this.modifySpeed = new BooleanSetting("ModifySpeed", "Enabled", "Modifies the speed of your swing animation.", new CategorySetting.Visibility(this.speedCategory), false);
        this.speed = new NumberSetting("Speed", "Amount", "The speed at which your hand will be swinging.", new BooleanSetting.Visibility(this.modifySpeed, true), 15, 1, 20);
        this.translationCategory = new CategorySetting("Translation", "The category related to the swing translations.");
        this.translateX = new BooleanSetting("TranslateX", "X", "Enables the swing translation on the X axis.", new CategorySetting.Visibility(this.translationCategory), true);
        this.translateY = new BooleanSetting("TranslateY", "Y", "Enables the swing translation on the Y axis.", new CategorySetting.Visibility(this.translationCategory), true);
        this.translateZ = new BooleanSetting("TranslateZ", "Z", "Enables the swing translation on the Z axis.", new CategorySetting.Visibility(this.translationCategory), true);
        this.rotationCategory = new CategorySetting("Rotation", "The category related to the swing rotations.");
        this.rotationX = new BooleanSetting("RotationX", "X", "Enables the swing rotation on the X axis.", new CategorySetting.Visibility(this.rotationCategory), true);
        this.rotationY = new BooleanSetting("RotationY", "Y", "Enables the swing rotation on the Y axis.", new CategorySetting.Visibility(this.rotationCategory), true);
        this.rotationZ = new BooleanSetting("RotationZ", "Z", "Enables the swing rotation on the Z axis.", new CategorySetting.Visibility(this.rotationCategory), true);
    }

}
