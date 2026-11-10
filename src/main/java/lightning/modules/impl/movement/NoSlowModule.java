package lightning.modules.impl.movement;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.minecraft.NetworkUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@RegisterModule(name = "NoSlow", description = "Removes the slowness effect that you receive when doing certain actions.", category = Module.Category.MOVEMENT)
public class NoSlowModule extends Module {
    public BooleanSetting items;
    public ModeSetting itemMode;
    public BooleanSetting crawl;
    public BooleanSetting sneak;
    public BooleanSetting soulSand;
    public BooleanSetting slimeBlocks;
    public BooleanSetting honeyBlocks;

    public NoSlowModule() {
        this.items = new BooleanSetting("Items", "Removes the slowness effect from eating or using items.", true);
        var tmp0 = new String[4];
        tmp0[0] = "Normal";
        tmp0[1] = "Strict";
        tmp0[2] = "GrimV2";
        tmp0[3] = "GrimV3";
        this.itemMode = new ModeSetting("Mode", "Item slowdown bypass mode.", "Normal", tmp0);
        this.crawl = new BooleanSetting("Crawl", "Removes the slowness effect from crawling.", false);
        this.sneak = new BooleanSetting("Sneak", "Removes the slowness effect from sneaking.", false);
        this.soulSand = new BooleanSetting("SoulSand", "Removes the slowness effect from walking on soul sand.", false);
        this.slimeBlocks = new BooleanSetting("SlimeBlocks", "Removes the slowness effect from walking on slime blocks.", false);
        this.honeyBlocks = new BooleanSetting("HoneyBlocks", "Removes the slowness effect from walking on honey blocks.", false);
    }

    @SubscribeEvent
    public void onUpdateMovementPost(UpdateMovementEvent.Post arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if ((((!(this.items.getValue())) || (!(mc.player.isUsingItem()))) || (mc.player.isPassenger())) || (mc.player.isFallFlying())) {
            return;
        }
        if (LightningClient.ROTATION_MANAGER.getRotation() != null) {
        } else {
        }
        float var3 = mc.player.getYRot();
        if (LightningClient.ROTATION_MANAGER.getRotation() != null) {
        } else {
        }
        var tmp0 = mc.player.getXRot();
        float var2 = tmp0;
        float var4 = tmp0;
        if (this.itemMode.getValue().equals("GrimV2")) {
            if (mc.player.isShiftKeyDown()) break label_13;
            if (!(mc.player.getUseItem().has(DataComponents.FOOD))) {
                if (!(canUseItem(mc.player.getUseItem()))) break label_13;
            }
            Object var5 = (mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            NetworkUtils.sendSequencedPacket((arg3) -> new ServerboundUseItemPacket(arg0, arg3, arg1, arg2));
            break label_13;
        }
        if ((!(this.itemMode.getValue().equals("Strict"))) || (NetworkUtils.isLegacyProtocol())) break label_13;
        if (mc.player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            var5 = mc.player.getInventory().getSelectedSlot();
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(((var5 + 1) % 9)));
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(var5));
            break label_13;
        }
        NetworkUtils.sendSequencedPacket((arg2) -> new ServerboundUseItemPacket(InteractionHand.OFF_HAND, arg2, arg0, arg1));
        label_13:
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.itemMode.getValue().equals("Strict"))) break label_6;
        if (!(arg0.getPacket() instanceof ServerboundMovePlayerPacket.PosRot) && !(arg0.getPacket() instanceof ServerboundMovePlayerPacket.Pos) && !(arg0.getPacket() instanceof ServerboundMovePlayerPacket.Rot)) {
            if ((arg0.getPacket() instanceof ServerboundMovePlayerPacket.StatusOnly)) {
            }
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
        }
        if (!((arg0.getPacket() instanceof ServerboundContainerClickPacket))) break label_6;
        if (mc.player.isUsingItem()) {
            mc.player.stopUsingItem();
        }
        if (mc.player.isSprinting()) {
            mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        }
        if (!(mc.player.isShiftKeyDown())) break label_6;
        mc.player.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, false, false)));
        label_6:
    }

    public void onDisable() {
        LightningClient.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    public boolean canUseItem(ItemStack arg0) {
        if ((arg0 == null) || (arg0.isEmpty())) {
            return false;
        }
        if (!(arg0.has(DataComponents.CONSUMABLE)) && !(arg0.has(DataComponents.FOOD)) && !(arg0.is(Items.BOW)) && !(arg0.is(Items.CROSSBOW)) && !(arg0.is(Items.SHIELD)) && !(arg0.is(Items.TRIDENT)) && !(arg0.is(Items.SPYGLASS))) {
            if (arg0.is(Items.GOAT_HORN)) {
            }
        } else {
        }
        return false;
    }

    public boolean canBypassGrimUseTime() {
        if (mc.player == null) return false;
        if (mc.player.getTicksUsingItem() <= 1) break label_1;
        return (mc.player.getTicksUsingItem() % 2) != 0;
    }

    public boolean shouldSlow() {
        if ((mc.player == null) || (!(this.items.getValue()))) {
            return true;
        }
        if (this.itemMode.getValue().equals("GrimV3")) return !(canBypassGrimUseTime());
        return false;
    }

}
