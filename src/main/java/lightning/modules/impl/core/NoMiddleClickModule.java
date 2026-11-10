package lightning.modules.impl.core;

import lightning.modules.Module;
import lightning.modules.RegisterModule;

@RegisterModule(name = "NoMiddleClick", description = "Prevents middle clicking from picking blocks into your hand.", category = Module.Category.CORE)
public class NoMiddleClickModule extends Module {
    public NoMiddleClickModule() {
    }

}
