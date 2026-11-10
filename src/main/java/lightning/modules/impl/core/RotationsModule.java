package lightning.modules.impl.core;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;

@RegisterModule(name = "Rotations", description = "Manages the client's rotation system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class RotationsModule extends Module {
    public BooleanSetting movementFix;
    public BooleanSetting snapBack;
    public ModeSetting jitter;

    public RotationsModule() {
        this.movementFix = new BooleanSetting("MovementFix", "Makes your movement in accordance with your yaw.", false);
        this.snapBack = new BooleanSetting("SnapBack", "Reverts rotations to previous values after rotating.", false);
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Grim";
        tmp0[2] = "Normal";
        this.jitter = new ModeSetting("Jitter", "Adds random noise to sent rotations so they're never bit-for-bit identical, defeating aim-consistency checks like Grim's AimModulo360.", "None", tmp0);
    }

}
