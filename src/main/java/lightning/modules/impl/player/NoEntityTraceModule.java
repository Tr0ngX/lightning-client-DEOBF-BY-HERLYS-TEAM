package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.WhitelistSetting;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.VehicleEntity;

@RegisterModule(name = "NoEntityTrace", description = "Allows you to interact with blocks, bypassing the entities between you and the block.", category = Module.Category.PLAYER)
public class NoEntityTraceModule extends Module {
    public BooleanSetting pickaxeOnly;
    public BooleanSetting angryMobs;
    public WhitelistSetting ignoredItem;

    public NoEntityTraceModule() {
        this.pickaxeOnly = new BooleanSetting("PickaxeOnly", "Only bypasses entities if you're holding a pickaxe.", false);
        this.angryMobs = new BooleanSetting("AngryMobs", "Only bypasses non-living entities (Crystals, Boats, Minecarts) and neutral/angerable mobs (Endermen, Piglins, Golems, etc.).", false);
        this.ignoredItem = new WhitelistSetting("IgnoredItem", "Do not bypasses entities if you're holding these items.", WhitelistSetting.Type.ITEMS);
    }

    public boolean shouldIgnore() {
        return shouldIgnore(null);
    }

    public boolean shouldIgnore(Entity arg0) {
        if (mc.player == null) return false;
        if (this.pickaxeOnly.getValue() && !(mc.player.getMainHandItem().is(ItemTags.PICKAXES))) return false;
        if (this.ignoredItem.isWhitelistContains(mc.player.getMainHandItem().getItem())) return false;
        if (this.angryMobs.getValue() && arg0 != null) {
            if (!(arg0 instanceof EndCrystal) && !(arg0 instanceof VehicleEntity) && !(arg0 instanceof ItemFrame) && !(arg0 instanceof ArmorStand)) {
                if (!((arg0 instanceof LivingEntity))) {
                }
            } else {
            }
            int var2 = 0;
            int var3 = (arg0 instanceof NeutralMob);
            if (!var2) {
                if (!(!var3)) {
                }
            } else {
            }
            return false;
        }
        return true;
    }

}
