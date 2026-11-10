package lightning.modules.impl.combat;

import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

@RegisterModule(name = "AutoLog", description = "Logs out automatically to avoid dying.", category = Module.Category.COMBAT)
public class AutoLogModule extends Module {
    public BooleanSetting healthCheck;
    public NumberSetting health;
    public BooleanSetting totemCheck;
    public NumberSetting totemCount;
    public BooleanSetting selfDisable;

    public AutoLogModule() {
        this.healthCheck = new BooleanSetting("HealthCheck", "Checks if you are at a specific health to log out.", false);
        this.health = new NumberSetting("Health", "The health the player must be at to log out.", new BooleanSetting.Visibility(this.healthCheck, true), 10, 0, 20);
        this.totemCheck = new BooleanSetting("TotemCheck", "Checks if you ran out of totems to be able to log out.", true);
        this.totemCount = new NumberSetting("Totems", "The amount of totems to have in your inventory to log out.", new BooleanSetting.Visibility(this.totemCheck, true), 2, 0, 9);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module after logging out.", true);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        int var2 = mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
        if ((!(this.healthCheck.getValue())) || (mc.player.getHealth() > ((float) this.health.getValue().intValue()))) break label_1;
        mc.getConnection().onDisconnect(new DisconnectionDetails(Component.literal(("Health was lower than or equal to " + this.health.getValue().intValue() + "."))));
        if (!(this.selfDisable.getValue())) break label_1;
        setToggled(false);
        label_1:
        if ((!(this.totemCheck.getValue())) || (var2 > this.totemCount.getValue().intValue())) break label_2;
        mc.getConnection().onDisconnect(new DisconnectionDetails(Component.literal("Couldn't find totems in your inventory.")));
        if (!(this.selfDisable.getValue())) break label_2;
        setToggled(false);
        label_2:
    }

}
