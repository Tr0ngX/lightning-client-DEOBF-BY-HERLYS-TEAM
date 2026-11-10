package lightning.modules.impl.core;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IPatchModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

@RegisterModule(name = "Patch", description = "Server/anticheat-specific hotfixes.", category = Module.Category.CORE, persistent = true, drawn = false)
public class PatchModule extends Module implements IPatchModule {
    public BooleanSetting grimAttackVelocity;
    public BooleanSetting silentSwapFix;

    public PatchModule() {
        this.grimAttackVelocity = new BooleanSetting("GrimAttackVelocity", "Stops your own sprint and movement from being cut after landing extra knockback on a hit.", false);
        this.silentSwapFix = new BooleanSetting("SilentSwapFix", "Stops hotbar slot desync and ghost item duplication during silent switch.", true);
    }

    public boolean isGrimAttackVelocity() {
        return this.grimAttackVelocity.getValue();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (mc.player == null) return;
        if (!(this.silentSwapFix.getValue())) return;
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundContainerSetSlotPacket))) return;
        Object var3 = ((ClientboundContainerSetSlotPacket) var2);
        if (var3.getContainerId()) return;
        int var4 = var3.getSlot();
        if ((var4 < 36) || (var4 > 44)) {
            return;
        }
        int var5 = (var4 - 36);
        Object var6 = var3.getItem();
        Object var7 = mc.player.getInventory().getItem(var5);
        Object var8 = mc.player.getMainHandItem();
        if (!(var6.isEmpty()) && !(var7.isEmpty()) && var6.getItem() == var7.getItem() && var6.getCount() == var7.getCount()) {
            arg0.setCancelled(true);
            return;
        }
        if ((((mc.gui.screen() != null) || (var7.isEmpty())) || (var6.isEmpty())) || (var7.getItem() == var6.getItem())) break label_8;
        int var9 = mc.player.getInventory().getSelectedSlot();
        int var10 = LightningClient.POSITION_MANAGER.getServerSlot();
        if (var5 == var9) break label_8;
        if (var6.getItem() != var8.getItem()) {
            if (((var10 < 0) || (var10 > 8)) || (var6.getItem() != mc.player.getInventory().getItem(var10).getItem())) break label_8;
        }
        arg0.setCancelled(true);
        return;
        label_8:
    }

}
