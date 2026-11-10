package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ChorusControlModule extends Module {
    public BindSetting confirm;
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private ClientboundPlayerPositionPacket packet;
    private boolean cancel;

    public ChorusControlModule() {
        this.confirm = new BindSetting("Confirm", "The key that will be used to confirm the teleportation.", 340).disableHoldModes();
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "Fill";
        tmp0[2] = "Outline";
        tmp0[3] = "Both";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the target block.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.cancel = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if ((this.cancel) || (!(mc.player.isUsingItem()))) break label_2;
        var tmp0 = mc.player.getItemInHand(mc.player.getUsedItemHand());
        Object var2 = tmp0;
        if ((tmp0.getItem() != Items.CHORUS_FRUIT) || ((var2.getUseDuration(mc.player) - mc.player.getTicksUsingItem()) > 1)) break label_2;
        this.cancel = true;
        label_2:
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        if (this.packet == null) return;
        if (arg0.getKey() != this.confirm.getValue()) return;
        this.packet.handle(mc.getConnection());
        this.packet = null;
        this.cancel = false;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!this.cancel) return;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundMovePlayerPacket))) break label_3;
        var tmp0 = ((ServerboundMovePlayerPacket) var3);
        Object var2 = tmp0;
        if (!(tmp0.hasPosition())) break label_3;
        arg0.setCancelled(true);
        label_3:
        if ((arg0.getPacket() instanceof ServerboundAcceptTeleportationPacket)) {
            arg0.setCancelled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundPlayerPositionPacket))) break label_2;
        Object var3 = ((ClientboundPlayerPositionPacket) var2);
        if (!this.cancel) break label_2;
        arg0.setCancelled(true);
        this.packet = var3;
        label_2:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gui != null && mc.gui.screen() != null)) {
            return;
        }
        if (this.packet == null) return;
        Object var2 = new Vec3(this.packet.change().position().x(), this.packet.change().position().y(), this.packet.change().position().z());
        Object var3 = EntityTypes.PLAYER.getDimensions().makeBoundingBox(var2);
        if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBox(arg0.getMatrices(), var3, this.fillColor.getColor());
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var3, this.outlineColor.getColor());
        }
    }

    public void onDisable() {
        if (mc.getConnection() != null && this.packet != null) {
            this.packet.handle(mc.getConnection());
            this.packet = null;
            this.cancel = false;
        }
    }

}
