package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.PositionUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

@RegisterModule(name = "SelfFill", description = "Automatically places a block in the spot that you were previously in.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class SelfFillModule extends Module {
    public ModeSetting autoSwitch;
    public ModeSetting jumpMode;
    public ModeSetting burrow;
    public BooleanSetting obsidianOnly;
    public BooleanSetting rotate;
    public BooleanSetting strictDirection;
    public BooleanSetting crystalDestruction;
    public BooleanSetting render;
    private BlockPos lastPosition;
    private boolean jumped;
    private boolean rotatedBypass;
    private int ticks;

    public SelfFillModule() {
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for automatically switching to necessary items.", "Silent", InventoryUtils.SWITCH_MODES);
        var tmp0 = new String[2];
        tmp0[0] = "Normal";
        tmp0[1] = "Packet";
        this.jumpMode = new ModeSetting("JumpMode", "The mode that will be used for jumping.", "Normal", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Packet";
        var tmp2 = new String[2];
        tmp2[0] = "None";
        tmp2[1] = "Bypass";
        this.burrow = new ModeSetting("Burrow", "Teleports you inside of the block that was placed.", new ModeSetting.Visibility(this.jumpMode, tmp1), "None", tmp2);
        this.obsidianOnly = new BooleanSetting("ObsidianOnly", "Only places using obsidian.", false);
        this.rotate = new BooleanSetting("Rotate", "Whether or not to rotate when placing the block.", true);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        this.crystalDestruction = new BooleanSetting("CrystalDestruction", "Whether or not to destroy crystals that obstruct the block's placement.", true);
        this.render = new BooleanSetting("Render", "Whether or not to render the place position.", true);
        this.lastPosition = null;
        this.jumped = false;
        this.rotatedBypass = false;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) return;
        if (this.lastPosition != null) break label_1;
        setToggled(false);
        return;
        label_1:
        if (this.jumpMode.getValue().equalsIgnoreCase("Normal")) {
            if (this.jumped) break label_2;
            mc.player.jumpFromGround();
            this.jumped = true;
            this.ticks = 0;
            return;
            label_2:
            this.ticks = (this.ticks + 1);
            if (this.ticks < 3) return;
        }
        var tmp0 = WorldUtils.getDirection(this.lastPosition, this.strictDirection.getValue());
        Object var2 = tmp0;
        if (tmp0 == null) {
            setToggled(false);
            return;
        }
        if (this.jumpMode.getValue().equalsIgnoreCase("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
            if (!this.rotatedBypass) {
                this.rotatedBypass = true;
                this.ticks = 0;
                return;
            }
            this.ticks = (this.ticks + 1);
            if (this.ticks < 3) return;
        }
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None"))) break label_10;
        if ((mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            if ((!(this.obsidianOnly.getValue())) || (mc.player.getMainHandItem().getItem() == Items.OBSIDIAN)) break label_10;
        }
        LightningClient.CHAT_MANAGER.tagged(("You are currently not holding any " + (this.obsidianOnly.getValue() ? "valid " : "") + "blocks."), getName());
        setToggled(false);
        return;
        label_10:
        if (!(mc.level.getBlockState(this.lastPosition).canBeReplaced())) return;
        IF (mc.level.getEntities(((Entity) null), new AABB(this.lastPosition), (arg0) -> {
    if (((arg0 == mc.player) || ((arg0 instanceof ExperienceOrb))) || ((arg0 instanceof ItemEntity))) break label_0;
    return !((arg0 instanceof EndCrystal));
}).isEmpty()) break label_12;
        return;
        label_12:
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
            if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
            }
        } else {
        }
        int var3 = InventoryUtils.findHardestBlock(35, 8);
        int var4 = mc.player.getInventory().getSelectedSlot();
        if (this.obsidianOnly.getValue()) {
            if (!(this.autoSwitch.getValue().equalsIgnoreCase("AltSwap"))) {
                if (this.autoSwitch.getValue().equalsIgnoreCase("AltPickup")) {
                }
            } else {
            }
            var3 = InventoryUtils.find(0, 35, 8);
        }
        if (!(this.autoSwitch.getValue().equalsIgnoreCase("None")) && var3 == -1) {
            LightningClient.CHAT_MANAGER.tagged(("There are currently no " + (this.obsidianOnly.getValue() ? "valid " : "") + "blocks in your hotbar."), getName());
            setToggled(false);
            return;
        }
        if (this.jumpMode.getValue().equals("Packet")) {
            ProxyServerTickListener.allowSend(() -> {
    mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), (mc.player.getY() + 0.4), mc.player.getZ(), false, mc.player.horizontalCollision));
    mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), (mc.player.getY() + 0.75), mc.player.getZ(), false, mc.player.horizontalCollision));
    mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), (mc.player.getY() + 1.01), mc.player.getZ(), false, mc.player.horizontalCollision));
    if (!(this.jumpMode.getValue().equalsIgnoreCase("Packet"))) break label_1;
    mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), (mc.player.getY() + (this.burrow.getValue().equalsIgnoreCase("Bypass") ? 0.99999992 : 1.15)), mc.player.getZ(), false, mc.player.horizontalCollision));
});
        }
        InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, var4);
        if (this.jumpMode.getValue().equalsIgnoreCase("Packet")) {
            if (this.burrow.getValue().equalsIgnoreCase("Bypass")) break label_25;
        }
        int var5 = WorldUtils.placeBlock(this.lastPosition, var2, InteractionHand.MAIN_HAND, this.rotate.getValue(), this.crystalDestruction.getValue(), this.render.getValue());
        InventoryUtils.switchBack(this.autoSwitch.getValue(), var3, var4);
        if (!var5) return;
        if (this.jumpMode.getValue().equals("Packet") && this.burrow.getValue().equalsIgnoreCase("Bypass")) {
            ProxyServerTickListener.allowSend(() -> {
    mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), (mc.player.getY() + 1.15), mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));
    mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision));
});
            mc.player.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
        setToggled(false);
        this.jumped = false;
        this.rotatedBypass = false;
    }

    @SubscribeEvent(priority = 4)
    public void onClientRotation(ClientRotationEvent arg0) {
        if (((!this.rotatedBypass) || (this.lastPosition == null)) || (arg0.isCancelled())) {
            return;
        }
        Object var2 = WorldUtils.getDirection(this.lastPosition, this.strictDirection.getValue());
        if (var2 == null) return;
        Object var3 = RotationUtils.getRotations(WorldUtils.getHitVector(this.lastPosition, var2));
        arg0.setYaw(var3[0]);
        arg0.setPitch(var3[1]);
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
            return;
        }
        if (!(mc.player.onGround())) {
            LightningClient.CHAT_MANAGER.tagged("You are currently in the air.", getName());
            setToggled(false);
            return;
        }
        this.lastPosition = PositionUtils.getFlooredPosition(mc.player);
    }

}
