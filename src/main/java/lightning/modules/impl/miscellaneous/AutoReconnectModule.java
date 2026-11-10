package lightning.modules.impl.miscellaneous;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "AutoReconnect", description = "Automatically reconnects you to a server after a specified time period.", category = Module.Category.MISCELLANEOUS)
public class AutoReconnectModule extends Module {
    public NumberSetting delay;

    public AutoReconnectModule() {
        this.delay = new NumberSetting("Delay", "The amount of seconds that have to pass before reconnecting.", 5, 0, 20);
    }

    public String getMetaData() {
        return String.valueOf(this.delay.getValue().intValue());
    }

}
