package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IReachModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "Reach", description = "Allows you to modify the distance at which you can interact with blocks.", category = Module.Category.PLAYER)
public class ReachModule extends Module implements IReachModule {
    public NumberSetting amount;

    public ReachModule() {
        this.amount = new NumberSetting("Amount", "The maximum distance at which you will be able to interact with blocks.", 6.0, 0.0, 8.0);
    }

    public String getMetaData() {
        return String.valueOf(this.amount.getValue().doubleValue());
    }

    public double getAmountValue() {
        return this.amount.getValue().doubleValue();
    }

}
