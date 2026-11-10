package lightning.managers;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.TickEvent;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.MovementUtils;
import lightning.utils.system.Timer;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;

public class BoostManager implements IMinecraft {
    public static BoostManager INSTANCE;
    private final Timer explosionTimer;
    private final Timer longjumpTimer;
    double boostExplosionSpeed;
    boolean canLongjump;

    public BoostManager() {
        this.explosionTimer = new Timer();
        this.longjumpTimer = new Timer();
        this.canLongjump = false;
        INSTANCE = this;
        LightningClient.EVENT_HANDLER.subscribe(this);
        this.boostExplosionSpeed = 0.0;
    }

    @SubscribeEvent(priority = 100)
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundExplodePacket))) break label_2;
        Object var3 = ((ClientboundExplodePacket) var2);
        if (mc.player.position().distanceTo(var3.center()) > 6.0) break label_2;
        var3.playerKnockback().ifPresent(() -> {
    if (arg0.x == 0.0) {
        if (arg0.z != 0.0) {
        }
        this.explosionTimer.reset();
        this.boostExplosionSpeed = Math.hypot(arg0.x, arg0.z);
        this.canLongjump = true;
        this.longjumpTimer.reset();
    }
});
        label_2:
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(MovementUtils.isMoving())) {
            this.boostExplosionSpeed = 0.0;
            this.canLongjump = false;
        }
        if (this.explosionTimer.timeElapsed() >= 400L) {
            this.boostExplosionSpeed = 0.0;
        }
        if (this.longjumpTimer.timeElapsed() >= 500L) {
            this.canLongjump = false;
        }
    }

    public double getBoostSpeed(boolean arg0) {
        return this.boostExplosionSpeed;
    }

    public boolean canDoLongjump() {
        return this.canLongjump;
    }

}
