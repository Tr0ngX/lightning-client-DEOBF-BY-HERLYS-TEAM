package lightning.modules.impl.movement;

import java.util.ArrayList;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.system.Timer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "FakeLag", description = "Chokes sent packets to look like you are lagging.", category = Module.Category.MOVEMENT)
public class FakeLagModule extends Module {
    public NumberSetting choke;
    public BooleanSetting onlyOnHoleLeave;
    public NumberSetting autoDisable;
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private final ArrayList packets;
    private final Timer timer;
    private final Timer safety;
    private final Timer chokeTimer;
    private final Timer autoDisableTimer;
    private boolean pendingAutoDisable;
    private boolean sending;
    private Vec3 serverPosition;
    private boolean isChoking;
    private BlockPos currentPos;

    public FakeLagModule() {
        this.choke = new NumberSetting("Choke", "The delay to choke packets for.", 2, 1, 5);
        this.onlyOnHoleLeave = new BooleanSetting("OnlyOnHoleLeave", "Only choke packets when leaving a hole.", false);
        this.autoDisable = new NumberSetting("AutoDisable", "AutoDisable", "Delay (ms) after FakeLag is enabled before it disables itself. 0 = off.", new Setting.Visibility(), 0, 0, 1000, 100);
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "Fill";
        tmp0[2] = "Outline";
        tmp0[3] = "Both";
        this.mode = new ModeSetting("RenderMode", "The rendering that will be applied to the target entity.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.packets = new ArrayList();
        this.timer = new Timer();
        this.safety = new Timer();
        this.chokeTimer = new Timer();
        this.autoDisableTimer = new Timer();
        this.pendingAutoDisable = false;
        this.sending = false;
        this.serverPosition = null;
        this.isChoking = false;
        this.currentPos = null;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (getNull()) return;
        Object var3 = arg0.getPacket();
        if ((var3 instanceof ClientboundPlayerPositionPacket)) {
            Object var4 = ((ClientboundPlayerPositionPacket) var3);
            this.serverPosition = new Vec3(var4.change().position().x(), var4.change().position().y(), var4.change().position().z());
            sendPackets();
            this.safety.reset();
            break label_3;
        }
        if (!((arg0.getPacket() instanceof ClientboundDisconnectPacket))) {
            var tmp0 = arg0.getPacket();
            var3 = tmp0;
            if (!((tmp0 instanceof ClientboundSetHealthPacket))) break label_3;
            var tmp1 = ((ClientboundSetHealthPacket) var3);
            Object var2 = tmp1;
            if (tmp1.getHealth() > 0.0f) break label_3;
        }
        sendPackets();
        this.safety.reset();
        label_3:
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((getNull()) || (this.sending)) break label_0;
        var tmp0 = arg0.getPacket();
        Object var3 = tmp0;
        if (!((tmp0 instanceof ServerboundMovePlayerPacket))) break label_0;
        var tmp1 = ((ServerboundMovePlayerPacket) var3);
        Object var2 = tmp1;
        if (!(hasPositionChange(tmp1))) {
            label_0:
            return;
        }
        if (!(shouldChoke())) {
            this.serverPosition = mc.player.position();
            return;
        }
        var3 = this.packets;
        Object var4 = var3;
        // synchronized (var3)
        arg0.setCancelled(true);
        this.packets.add(var2);
        break label_7;
        Object var5 = null;
        throw var5;
        label_7:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((getNull()) || (this.serverPosition == null)) || (this.mode.getValue().equalsIgnoreCase("None"))) {
            return;
        }
        Object var2 = new AABB((this.serverPosition.x - (mc.player.getBoundingBox().getXsize() / 2.0)), this.serverPosition.y, (this.serverPosition.z - (mc.player.getBoundingBox().getZsize() / 2.0)), (this.serverPosition.x + (mc.player.getBoundingBox().getXsize() / 2.0)), (this.serverPosition.y + mc.player.getBoundingBox().getYsize()), (this.serverPosition.z + (mc.player.getBoundingBox().getZsize() / 2.0)));
        if (!(this.mode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBox(arg0.getMatrices(), var2, this.fillColor.getColor());
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Outline"))) {
            if (this.mode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var2, this.outlineColor.getColor());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (getNull()) return;
        if (this.onlyOnHoleLeave.getValue()) {
            Object var3 = this.currentPos;
            this.currentPos = BlockPos.containing(mc.player.position());
            var tmp0 = (var3 == null ? this.currentPos : var3);
            Object var2 = tmp0;
            Object var4 = tmp0;
            if (this.currentPos.equals(var2)) break label_3;
            int var5 = HoleSnapModule.isInHole(this.currentPos);
            int var6 = HoleSnapModule.isInHole(var2);
            if ((var5) || (!var6)) break label_3;
            this.isChoking = true;
            this.chokeTimer.reset();
            label_3:
            if (!(!this.isChoking) && this.chokeTimer.hasTimeElapsed(((int) (this.choke.getValue().floatValue() * 100.0f)))) {
                sendPackets();
                this.isChoking = false;
            }
            break label_6;
        }
        if ((this.packets.isEmpty()) || (!(this.timer.hasTimeElapsed(((int) (this.choke.getValue().floatValue() * 100.0f)))))) break label_6;
        sendPackets();
        this.timer.reset();
        label_6:
        if (!(!this.pendingAutoDisable) && this.autoDisableTimer.hasTimeElapsed(this.autoDisable.getValue().intValue())) {
            this.pendingAutoDisable = false;
            setToggled(false);
        }
    }

    private void startAutoDisable() {
        this.pendingAutoDisable = true;
        this.autoDisableTimer.reset();
    }

    public void onEnable() {
        this.timer.reset();
        this.isChoking = false;
        if (this.autoDisable.getValue().intValue() > 0) {
            startAutoDisable();
        } else {
            this.pendingAutoDisable = false;
        }
        this.currentPos = null;
        if (mc.player != null) {
        } else {
        }
        mc.player.position().serverPosition = null;
    }

    public void onDisable() {
        if (getNull()) return;
        sendPackets();
        this.isChoking = false;
        this.currentPos = null;
    }

    private void sendPackets() {
        Object var1 = this.packets;
        Object var2 = var1;
        // synchronized (var1)
        this.sending = true;
        Object var3 = this.packets.iterator();
        while (var3.hasNext()) {
            Object var4 = ((ServerboundMovePlayerPacket) var3.next());
            mc.player.connection.send(var4);
        }
        if (!(this.packets.isEmpty())) {
            this.serverPosition = mc.player.position();
        }
        this.packets.clear();
        this.sending = false;
        break label_7;
        Object var5 = null;
        throw var5;
        label_7:
    }

    private boolean shouldChoke() {
        if (this.onlyOnHoleLeave.getValue()) {
            break label_3;
        }
        if (EntityUtils.getSpeed(mc.player, EntityUtils.SpeedUnit.KILOMETERS) < 5.0) {
            if (mc.player.fallDistance <= 0.0) break label_2;
        }
        return this.safety.hasTimeElapsed(1000);
    }

    private boolean hasPositionChange(ServerboundMovePlayerPacket arg0) {
        if (!((arg0 instanceof ServerboundMovePlayerPacket.Pos))) {
            if ((arg0 instanceof ServerboundMovePlayerPacket.PosRot)) {
            }
        } else {
        }
        return false;
    }

    public String getMetaData() {
        return (String.valueOf((shouldChoke() ? ChatFormatting.GREEN : ChatFormatting.RED)) + "Choke");
    }

}
