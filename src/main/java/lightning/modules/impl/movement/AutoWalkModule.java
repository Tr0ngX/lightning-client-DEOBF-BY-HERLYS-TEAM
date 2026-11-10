package lightning.modules.impl.movement;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IAutoWalkModule;
import lightning.settings.impl.BooleanSetting;

@RegisterModule(name = "AutoWalk", description = "Holds forward (or backward) movement every tick. Auto-pauses while Baritone is actively driving movement (e.g. EBounce+'s ObstaclePassing) so the two don't fight over input.", category = Module.Category.MOVEMENT)
public class AutoWalkModule extends Module implements IAutoWalkModule {
    public final BooleanSetting backward;

    public AutoWalkModule() {
        this.backward = new BooleanSetting("Backward", "Walk backward instead of forward.", false);
    }

    public boolean isBackward() {
        return this.backward.getValue();
    }

}
