package lightning.modules.impl.player;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@RegisterModule(name = "FastPlace", description = "Allows you to customize the tick delay between using items.", category = Module.Category.PLAYER)
public class FastPlaceModule extends Module {
    public NumberSetting ticks;
    public BooleanSetting ignoreBlocks;
    public BooleanSetting ignoreFireworks;
    public BooleanSetting ignorePearls;
    public BooleanSetting ignoreEquipment;

    public FastPlaceModule() {
        this.ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before using items again.", 1, 0, 20);
        this.ignoreBlocks = new BooleanSetting("IgnoreBlocks", "Uses the default Minecraft delay when holding a block item.", true);
        this.ignoreFireworks = new BooleanSetting("IgnoreFireworks", "Uses the default Minecraft delay when holding fireworks.", true);
        this.ignorePearls = new BooleanSetting("IgnorePearls", "Uses the default Minecraft delay when holding pearls.", true);
        this.ignoreEquipment = new BooleanSetting("IgnoreEquipment", "Uses the default Minecraft delay when holding an equipment item.", true);
    }

    public String getMetaData() {
        return String.valueOf(this.ticks.getValue().intValue());
    }

    public boolean isValidItem(Item arg0) {
        if (this.ignoreBlocks.getValue() && (arg0 instanceof BlockItem)) return false;
        if (this.ignoreFireworks.getValue() && arg0 == Items.FIREWORK_ROCKET) return false;
        if (this.ignorePearls.getValue() && arg0 == Items.ENDER_PEARL) return false;
        if (!(arg0.builtInRegistryHolder().is(ItemTags.FOOT_ARMOR)) && !(arg0.builtInRegistryHolder().is(ItemTags.LEG_ARMOR)) && !(arg0.builtInRegistryHolder().is(ItemTags.CHEST_ARMOR))) {
            if (arg0.builtInRegistryHolder().is(ItemTags.HEAD_ARMOR)) {
            }
        } else {
        }
        int var2 = 0;
        if (this.ignoreEquipment.getValue()) {
            if (!var2 && arg0 != Items.ELYTRA) {
            }
        } else {
        }
        return false;
    }

}
