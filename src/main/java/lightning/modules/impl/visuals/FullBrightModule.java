package lightning.modules.impl.visuals;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFullBrightModule;
import lightning.settings.impl.ModeSetting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@RegisterModule(name = "FullBright", description = "Gives you the ability to clearly see even when in the dark.", category = Module.Category.VISUALS)
public class FullBrightModule extends Module implements IFullBrightModule {
    public ModeSetting mode;

    public FullBrightModule() {
        var tmp0 = new String[2];
        tmp0[0] = "Gamma";
        tmp0[1] = "Potion";
        this.mode = new ModeSetting("Mode", "The way that will be used to change the game's brightness.", "Gamma", tmp0);
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent arg0) {
        if (((arg0.getSetting() != this.mode) || (!(isToggled()))) || (mc.player == null)) {
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Potion")) {
            if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) break label_3;
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
            break label_3;
        }
        if (!(mc.player.hasEffect(MobEffects.NIGHT_VISION))) break label_3;
        mc.player.removeEffect(MobEffects.NIGHT_VISION);
        label_3:
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (!(this.mode.getValue().equalsIgnoreCase("Potion"))) return;
        if (!(mc.player.hasEffect(MobEffects.NIGHT_VISION))) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
        }
    }

    public void onEnable() {
        if (mc.player == null) return;
        if (!(this.mode.getValue().equalsIgnoreCase("Potion"))) return;
        if (!(mc.player.hasEffect(MobEffects.NIGHT_VISION))) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1));
        }
    }

    public void onDisable() {
        if (mc.player == null) return;
        if (!(this.mode.getValue().equalsIgnoreCase("Potion"))) return;
        if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

    public String getModeValue() {
        return this.mode.getValue();
    }

}
