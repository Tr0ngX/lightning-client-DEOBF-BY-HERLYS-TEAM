package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@RegisterModule(name = "AntiHunger", description = "Reduces the amount of hunger consumption.", category = Module.Category.PLAYER)
public class AntiHungerModule extends Module {
    public BooleanSetting ground;
    public BooleanSetting sprint;
    private boolean lastOnGround;
    private boolean ignore;

    public AntiHungerModule() {
        this.ground = new BooleanSetting("Ground", "Modifies movement packets to decrease hunger consumption.", true);
        this.sprint = new BooleanSetting("Sprint", "Spoofs sprinting packets to decrease hunger consumption.", true);
        this.lastOnGround = false;
        this.ignore = false;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) break label_2;
        if (!(!this.ignore) && (arg0.getPacket() instanceof ServerboundMovePlayerPacket)) {
            this.ignore = false;
            break label_2;
        }
        if (((mc.player.isPassenger()) || (mc.player.isInWater())) || (mc.player.isUnderWater())) break label_2;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundMovePlayerPacket))) break label_1;
        Object var2 = ((ServerboundMovePlayerPacket) var3);
        if ((((!(this.ground.getValue())) || (!(mc.player.onGround()))) || (mc.player.fallDistance > 0.0)) || (mc.gameMode.isDestroying())) break label_1;
        ((PlayerMoveC2SPacketAccessor) var2).setOnGround(false);
        label_1:
        var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundPlayerCommandPacket))) break label_2;
        var2 = ((ServerboundPlayerCommandPacket) var3);
        if ((!(this.sprint.getValue())) || (var2.getAction() != ServerboundPlayerCommandPacket.Action.START_SPRINTING)) break label_2;
        arg0.setCancelled(true);
        label_2:
    }

    @SubscribeEvent
    public void onUpdateMovement(UpdateMovementEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (mc.player.onGround() && !this.lastOnGround && this.ground.getValue()) {
            this.ignore = true;
        }
        this.lastOnGround = mc.player.onGround();
    }

    public void onEnable() {
        if (mc.player == null) return;
        this.lastOnGround = mc.player.onGround();
    }

}
