package lightning.modules.impl.movement;

import lightning.modules.Module;
import lightning.modules.RegisterModule;

@RegisterModule(name = "BounceDebug", description = "Prints ElytraFly Bounce speed/recast diagnostics to chat -- toggle independently of ElytraFly's own settings.", category = Module.Category.MOVEMENT)
public class ElytraBounceDebugModule extends Module {
    public ElytraBounceDebugModule() {
    }

}
