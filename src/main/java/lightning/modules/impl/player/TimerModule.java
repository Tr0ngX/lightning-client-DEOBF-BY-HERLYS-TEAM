package lightning.modules.impl.player;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "Timer", description = "Makes your game run at a faster tick speed.", category = Module.Category.PLAYER)
public class TimerModule extends Module {
    public NumberSetting multiplier;

    public TimerModule() {
        this.multiplier = new NumberSetting("Multiplier", "The multiplier that will be added to the game's speed.", 1.0f, 0.0f, 20.0f);
    }

    @SubscribeEvent(priority = -2147483648)
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        LightningClient.WORLD_MANAGER.setTimerMultiplier(this.multiplier.getValue().floatValue());
    }

    public void onEnable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(this.multiplier.getValue().floatValue());
    }

    public void onDisable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    public String getMetaData() {
        return String.valueOf(this.multiplier.getValue().floatValue());
    }

}
