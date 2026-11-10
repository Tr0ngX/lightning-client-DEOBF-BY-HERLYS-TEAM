package lightning.modules.impl.miscellaneous;

import com.google.common.collect.Sets;
import java.util.Set;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

@RegisterModule(name = "NoSoundLag", description = "Prevents lagging caused by a large amount of sounds being played.", category = Module.Category.MISCELLANEOUS)
public class NoSoundLagModule extends Module {
    public BooleanSetting armor;
    public BooleanSetting withers;
    public BooleanSetting ghasts;
    public BooleanSetting piston;
    public BooleanSetting dispenser;
    public static final Set ARMOR_SOUNDS;
    public static final Set WITHER_SOUNDS;
    public Set GHAST_SOUNDS;
    public static final Set PISTON_SOUNDS;
    public static final Set DISPENSER_SOUNDS;

    public NoSoundLagModule() {
        this.armor = new BooleanSetting("Armor", "Prevents lagging caused by armor sounds.", true);
        this.withers = new BooleanSetting("Withers", "Prevents lagging caused by wither sounds.", true);
        this.ghasts = new BooleanSetting("Ghasts", "Prevents lagging caused by ghast sounds.", true);
        this.piston = new BooleanSetting("Piston", "Prevents lagging caused by piston sounds.", true);
        this.dispenser = new BooleanSetting("Dispenser", "Prevents lagging caused by dispenser sounds.", true);
        var tmp0 = new SoundEvent[6];
        tmp0[0] = SoundEvents.GHAST_AMBIENT;
        tmp0[1] = SoundEvents.GHAST_DEATH;
        tmp0[2] = SoundEvents.GHAST_HURT;
        tmp0[3] = SoundEvents.GHAST_SCREAM;
        tmp0[4] = SoundEvents.GHAST_SHOOT;
        tmp0[5] = SoundEvents.GHAST_WARN;
        this.GHAST_SOUNDS = Sets.newHashSet(tmp0);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundSoundPacket))) break label_5;
        Object var3 = ((ClientboundSoundPacket) var2);
        if (this.armor.getValue()) {
            if (ARMOR_SOUNDS.contains(var3.getSound())) break label_4;
        }
        if (this.withers.getValue()) {
            if (WITHER_SOUNDS.contains(var3.getSound().value())) break label_4;
        }
        if (this.ghasts.getValue()) {
            if (this.GHAST_SOUNDS.contains(var3.getSound().value())) break label_4;
        }
        if (this.piston.getValue()) {
            if (PISTON_SOUNDS.contains(var3.getSound().value())) break label_4;
        }
        if ((!(this.dispenser.getValue())) || (!(DISPENSER_SOUNDS.contains(var3.getSound().value())))) break label_5;
        label_4:
        arg0.setCancelled(true);
        label_5:
        var tmp0 = arg0.getPacket();
        var2 = tmp0;
        if (!((tmp0 instanceof ClientboundLevelEventPacket))) break label_7;
        var3 = ((ClientboundLevelEventPacket) var2);
        if (!(this.dispenser.getValue())) break label_7;
        if (var3.getType() != 1000 && var3.getType() != 1001) {
            if (var3.getType() != 1002) break label_7;
        }
        arg0.setCancelled(true);
        label_7:
    }

    static {
        var tmp0 = new Holder[7];
        tmp0[0] = SoundEvents.ARMOR_EQUIP_GENERIC;
        tmp0[1] = SoundEvents.ARMOR_EQUIP_ELYTRA;
        tmp0[2] = SoundEvents.ARMOR_EQUIP_DIAMOND;
        tmp0[3] = SoundEvents.ARMOR_EQUIP_IRON;
        tmp0[4] = SoundEvents.ARMOR_EQUIP_GOLD;
        tmp0[5] = SoundEvents.ARMOR_EQUIP_CHAIN;
        tmp0[6] = SoundEvents.ARMOR_EQUIP_LEATHER;
        ARMOR_SOUNDS = Sets.newHashSet(tmp0);
        var tmp1 = new SoundEvent[6];
        tmp1[0] = SoundEvents.WITHER_AMBIENT;
        tmp1[1] = SoundEvents.WITHER_DEATH;
        tmp1[2] = SoundEvents.WITHER_BREAK_BLOCK;
        tmp1[3] = SoundEvents.WITHER_HURT;
        tmp1[4] = SoundEvents.WITHER_SPAWN;
        tmp1[5] = SoundEvents.WITHER_SHOOT;
        WITHER_SOUNDS = Sets.newHashSet(tmp1);
        var tmp2 = new SoundEvent[2];
        tmp2[0] = SoundEvents.PISTON_EXTEND;
        tmp2[1] = SoundEvents.PISTON_CONTRACT;
        PISTON_SOUNDS = Sets.newHashSet(tmp2);
        var tmp3 = new SoundEvent[3];
        tmp3[0] = SoundEvents.DISPENSER_DISPENSE;
        tmp3[1] = SoundEvents.DISPENSER_FAIL;
        tmp3[2] = SoundEvents.DISPENSER_LAUNCH;
        DISPENSER_SOUNDS = Sets.newHashSet(tmp3);
    }

}
