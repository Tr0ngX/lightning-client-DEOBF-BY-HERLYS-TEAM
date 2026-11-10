package lightning.modules.impl.miscellaneous;

import java.io.File;
import java.io.IOException;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.TargetDeathEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.FileUtils;

@RegisterModule(name = "Sounds", description = "Plays custom sounds when something happens. Put .wav files in .minecraft/Lightning/Client/", category = Module.Category.MISCELLANEOUS)
public class SoundsModule extends Module {
    private static final String SOUND_DIR = "Lightning/Client/";
    CategorySetting killsCategory;
    BooleanSetting killSound;
    NumberSetting killVolume;
    StringSetting killName;
    CategorySetting hitsCategory;
    BooleanSetting hitSound;
    NumberSetting hitVolume;
    StringSetting hitName;

    public SoundsModule() {
        this.killsCategory = new CategorySetting("Kills", "The kills category for the sounds.");
        this.killSound = new BooleanSetting("KillSound", "Play sounds when you kill a player.", new CategorySetting.Visibility(this.killsCategory), true);
        this.killVolume = new NumberSetting("KillVolume", "Volume", "The volume for the kill sounds.", new CategorySetting.Visibility(this.killsCategory), 1.0f, 0.0f, 1.0f);
        this.killName = new StringSetting("KillName", "Name", "The name of the kill sound file. Put it in .minecraft/Lightning/Client/", new CategorySetting.Visibility(this.killsCategory), "killsound.wav");
        this.hitsCategory = new CategorySetting("Hits", "The hits category for the sounds.");
        this.hitSound = new BooleanSetting("HitSound", "Play sounds when you hit an entity.", new CategorySetting.Visibility(this.hitsCategory), true);
        this.hitVolume = new NumberSetting("HitVolume", "Volume", "The volume for the hit sounds.", new CategorySetting.Visibility(this.hitsCategory), 1.0f, 0.0f, 1.0f);
        this.hitName = new StringSetting("HitName", "Name", "The name of the hit sound file. Put it in .minecraft/Lightning/Client/", new CategorySetting.Visibility(this.hitsCategory), "hitsound.wav");
    }

    public void onEnable() {
        try {
            FileUtils.createDirectory("Lightning/Client/");
        } catch (IOException ex2) {
            Object var1 = ex2;
        }
    }

    @SubscribeEvent
    public void onTargetDeath(TargetDeathEvent arg0) {
        if (getNull()) return;
        if (this.killSound.getValue()) {
            FileUtils.playSound(new File(("Lightning/Client/" + this.killName.getValue())), this.killVolume.getValue().floatValue());
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent arg0) {
        if (arg0.getPlayer() != mc.player) return;
        if (this.hitSound.getValue()) {
            FileUtils.playSound(new File(("Lightning/Client/" + this.hitName.getValue())), this.hitVolume.getValue().floatValue());
        }
    }

}
