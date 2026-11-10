package lightning.modules.impl.core;

import lightning.modules.Module;
import lightning.modules.RegisterModule;

@RegisterModule(name = "IgnoreNaked", description = "Ignore Naked, even that player wears elytra only", category = Module.Category.CORE)
public class IgnoreNakedModule extends Module {
    public IgnoreNakedModule() {
    }

}
