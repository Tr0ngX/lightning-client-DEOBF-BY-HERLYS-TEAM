package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.ISprintModule;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.ChatFormatting;

@RegisterModule(name = "TickShift", description = "Manipulates minecraft timer to give you a small speed boost.", category = Module.Category.MOVEMENT)
public class TickShiftModule extends Module {
    public NumberSetting maxTicks;
    public NumberSetting delay;
    public NumberSetting speed;
    int ticks;
    int wait;

    public TickShiftModule() {
        this.maxTicks = new NumberSetting("MaxTicks", "The maximum amount of ticks that the boost will be charging for.", 1, 1, 40);
        this.delay = new NumberSetting("Delay", "The delay between each tick.", 4, 1, 10);
        this.speed = new NumberSetting("Speed", "The speed of charging each tick.", 2.0f, 1.0f, 10.0f);
        this.ticks = 0;
        this.wait = 0;
    }

    public void onEnable() {
        if (getNull()) return;
        reset();
    }

    public void onDisable() {
        if (getNull()) return;
        reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        if (mc.player.fallDistance >= 5.0) return;
        Object var3 = ((ElytraFlyModule) LightningClient.MODULE_MANAGER.getModule(ElytraFlyModule.class));
        if (var3.isToggled() && var3.mode.getValue().equalsIgnoreCase("Control") && mc.player.isFallFlying()) return;
        int var4 = ((ISprintModule) LightningClient.MODULE_MANAGER.getModule("Sprint")).isInstantMode();
        if ((mc.player.xxa != 0.0f) || (mc.player.zza != 0.0f)) break label_3;
        var tmp0 = (mc.player.fallDistance == 0.0 ? 1 : 0);
        int var2 = tmp0;
        int var5 = tmp0;
        if (!var4) {
            var2 = (var2 | (EntityUtils.getSpeed(mc.player, EntityUtils.SpeedUnit.KILOMETERS) <= 5.0 ? 1 : 0));
        }
        if (!(!var2)) {
            LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
            if (this.wait >= this.delay.getValue().intValue()) {
                if (this.ticks < this.maxTicks.getValue().intValue()) {
                    this.ticks = (this.ticks + 1);
                }
                this.wait = 0;
            }
            this.wait = (this.wait + 1);
            break label_13;
        }
        if (this.ticks > 0) {
            if (!(((SpeedModule) LightningClient.MODULE_MANAGER.getModule(SpeedModule.class)).isDrivingTimer()) && !(mc.options.keyJump.isDown())) {
                LightningClient.WORLD_MANAGER.setTimerMultiplier(this.speed.getValue().floatValue());
            }
            this.ticks = (this.ticks - 1);
            break label_13;
        }
        reset();
        label_13:
    }

    public int getTicks() {
        return this.ticks;
    }

    public void setTicks(int arg0) {
        this.ticks = arg0;
    }

    public boolean isShifting() {
        if ((!(isToggled())) || (this.ticks <= 0)) break label_0;
        return LightningClient.WORLD_MANAGER.getTimerMultiplier() > 1.0f;
    }

    public void reset() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
        this.ticks = 0;
        this.wait = 0;
    }

    public String getMetaData() {
        if (this.ticks >= this.maxTicks.getValue().intValue()) {
        } else {
        }
        return ("" + this.ticks + String.valueOf(ChatFormatting.RESET));
    }

}
