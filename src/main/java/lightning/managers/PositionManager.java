package lightning.managers;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.utils.IMinecraft;
import lombok.Generated;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public class PositionManager implements IMinecraft {
    private double serverX;
    private double serverY;
    private double serverZ;
    private boolean serverOnGround;
    private boolean serverSprinting;
    private boolean serverSneaking;
    private int serverSlot;

    public PositionManager() {
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if (mc.player == null) break label_6;
        Object var3 = arg0.getPacket();
        if ((var3 instanceof ServerboundMovePlayerPacket)) {
            Object var2 = ((ServerboundMovePlayerPacket) var3);
            if (var2.hasPosition()) {
                this.serverX = var2.getX(mc.player.getX());
                this.serverY = var2.getY(mc.player.getY());
                this.serverZ = var2.getZ(mc.player.getZ());
            }
            this.serverOnGround = var2.isOnGround();
        }
        var3 = arg0.getPacket();
        if ((var3 instanceof ServerboundSetCarriedItemPacket)) {
            var2 = ((ServerboundSetCarriedItemPacket) var3);
            this.serverSlot = var2.getSlot();
        }
        var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundPlayerCommandPacket))) break label_5;
        var2 = ((ServerboundPlayerCommandPacket) var3);
        switch (PositionManager.1.$SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action[var2.getAction().ordinal()]) {
            case 1 -> { break label_3; }
            case 2 -> { break label_4; }
            default -> { break label_5; }
        }
        label_3:
        this.serverSprinting = true;
        break label_5;
        label_4:
        this.serverSprinting = false;
        label_5:
        var3 = arg0.getPacket();
        if (!((var3 instanceof ServerboundPlayerInputPacket))) break label_6;
        var2 = ((ServerboundPlayerInputPacket) var3);
        this.serverSneaking = var2.input().shift();
        label_6:
    }

    public double getServerX() {
        if (mc.player != null) {
        } else {
        }
        return this.serverX;
    }

    public double getServerY() {
        if (mc.player != null) {
        } else {
        }
        return this.serverY;
    }

    public double getServerZ() {
        if (mc.player != null) {
        } else {
        }
        return this.serverZ;
    }

    public boolean isServerOnGround() {
        if (mc.player != null) {
        } else {
        }
        return this.serverOnGround;
    }

    @Generated
    public boolean isServerSprinting() {
        return this.serverSprinting;
    }

    @Generated
    public boolean isServerSneaking() {
        return this.serverSneaking;
    }

    @Generated
    public int getServerSlot() {
        return this.serverSlot;
    }


    class 1 {

        static {
            $SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action = new int[ServerboundPlayerCommandPacket.Action.values().length];
            try {
                $SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action[ServerboundPlayerCommandPacket.Action.START_SPRINTING.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$network$protocol$game$ServerboundPlayerCommandPacket$Action[ServerboundPlayerCommandPacket.Action.STOP_SPRINTING.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
            }
        }

    }
}
