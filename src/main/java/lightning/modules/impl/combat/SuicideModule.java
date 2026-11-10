package lightning.modules.impl.combat;

import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;

@RegisterModule(name = "Suicide", description = "Makes all of the combat modules target you so that you die.", category = Module.Category.COMBAT)
public class SuicideModule extends Module {
    public BooleanSetting offhandOverride;
    public BooleanSetting deathDisable;
    public BooleanSetting loginDisable;

    public SuicideModule() {
        this.offhandOverride = new BooleanSetting("OffhandOverride", "Changes the target offhand item in order to make it easier to die.", true);
        this.deathDisable = new BooleanSetting("DeathDisable", "Disables the module once you have died.", true);
        this.loginDisable = new BooleanSetting("LoginDisable", "Disables the module when you logged in.", true);
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if (!(this.deathDisable.getValue())) return;
        if (arg0.getPlayer() != mc.player) return;
        if (arg0.getPlayer().getId() != mc.player.getId()) return;
        setToggled(false);
    }

    @SubscribeEvent
    public void onPlayerLogin(ClientConnectEvent arg0) {
        if (this.loginDisable.getValue()) {
            setToggled(false);
        }
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
        }
    }

}
