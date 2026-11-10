package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

@RegisterModule(name = "AutoBowRelease", description = "Automatically releases your bow after a certain amount of time has passed.", category = Module.Category.COMBAT)
public class AutoBowReleaseModule extends Module {
    public NumberSetting ticks;

    public AutoBowReleaseModule() {
        this.ticks = new NumberSetting("Ticks", "The number of ticks that have to be waited for before releasing the bow.", 3, 0, 20);
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (((SelfBowModule) LightningClient.MODULE_MANAGER.getModule(SelfBowModule.class)).isToggled()) return;
        if (mc.player.getOffhandItem().getItem() != Items.BOW) {
            if (mc.player.getMainHandItem().getItem() != Items.BOW) break label_2;
        }
        if ((!(mc.player.isUsingItem())) || (mc.player.getTicksUsingItem() < this.ticks.getValue().intValue())) break label_2;
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, mc.player.getDirection()));
        NetworkUtils.sendSequencedPacket((arg0) -> new ServerboundUseItemPacket((mc.player.getOffhandItem().getItem() == Items.BOW ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND), arg0, mc.player.getYRot(), mc.player.getXRot()));
        mc.player.stopUsingItem();
        label_2:
    }

    public String getMetaData() {
        return String.valueOf(this.ticks.getValue().intValue());
    }

}
