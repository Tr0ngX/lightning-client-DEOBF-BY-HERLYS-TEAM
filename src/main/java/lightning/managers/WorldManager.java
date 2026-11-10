package lightning.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TickEvent;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class WorldManager implements IMinecraft {
    private static final Vec3i[] SPHERE;
    private static final int[] INDICES;
    private final Map poppedTotems;
    private final Set deadPlayers;
    private float timerMultiplier;
    private final Timer placeTimer;
    private final Set reservedPlacements;
    private final Map pendingCrystalRetries;

    public void reservePlacement(BlockPos arg0) {
        this.reservedPlacements.add(arg0);
    }

    public boolean isReserved(BlockPos arg0) {
        return this.reservedPlacements.contains(arg0);
    }

    public Set getReservedPlacements() {
        return this.reservedPlacements;
    }

    public void onCrystalAttacked(int arg0, Runnable arg1) {
        this.pendingCrystalRetries.put(arg0, arg1);
    }

    public WorldManager() {
        this.poppedTotems = new ConcurrentHashMap();
        this.deadPlayers = ConcurrentHashMap.newKeySet();
        this.timerMultiplier = 1.0f;
        this.placeTimer = new Timer();
        this.reservedPlacements = ConcurrentHashMap.newKeySet();
        this.pendingCrystalRetries = new ConcurrentHashMap();
        LightningClient.EVENT_HANDLER.subscribe(this);
        Object var1 = BlockPos.ZERO;
        Object var2 = new TreeSet(new WorldManager.1(this, var1));
        int var3 = (var1.getX() - 100);
        while (var3 <= (var1.getX() + 100)) {
            int var4 = (var1.getZ() - 100);
            while (var4 <= (var1.getZ() + 100)) {
                int var5 = (var1.getY() - 100);
                while (var5 < (var1.getY() + 100)) {
                    double var6 = ((double) ((((var1.getX() - var3) * (var1.getX() - var3)) + ((var1.getZ() - var4) * (var1.getZ() - var4))) + ((var1.getY() - var5) * (var1.getY() - var5))));
                    if (var6 < ((double) Mth.square(100))) {
                        var2.add(new BlockPos(var3, var5, var4));
                    }
                    var5++;
                }
                var4++;
            }
            var3++;
        }
        var3 = 0;
        var4 = 0;
        var5 = var2.iterator();
        while (var5.hasNext()) {
            var6 = ((BlockPos) var5.next());
            if (Math.sqrt(var1.distSqr(var6)) > ((double) var4)) {
                var4++;
                INDICES[var4] = var3;
            }
            var3++;
            SPHERE[var3] = var6;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        this.reservedPlacements.clear();
        InventoryUtils.tickPendingRestore();
        if (mc.level == null) break label_6;
        Object var2 = new ArrayList(mc.level.players());
        Object var3 = new ArrayList(this.poppedTotems.keySet()).iterator();
        while (var3.hasNext()) {
            Object var4 = ((UUID) var3.next());
            if (var2.stream().noneMatch((arg1) -> arg1.getUUID().equals(arg0))) {
                this.poppedTotems.remove(var4);
            }
        }
        var3 = var2.iterator();
        while (var3.hasNext()) {
            var4 = ((Player) var3.next());
            if (var4.deathTime <= 0 && var4.getHealth() > 0.0f) {
                this.deadPlayers.remove(var4.getUUID());
                break label_5;
            }
            if (this.deadPlayers.contains(var4.getUUID())) break label_5;
            LightningClient.EVENT_HANDLER.post(new PlayerDeathEvent(var4));
            this.deadPlayers.add(var4.getUUID());
            this.poppedTotems.remove(var4.getUUID());
            label_5:
        }
        label_6:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (mc.level == null) break label_6;
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundSetHeldSlotPacket))) break label_0;
        Object var2 = ((ClientboundSetHeldSlotPacket) var3);
        if ((mc.player == null) || (var2.slot() == mc.player.getInventory().getSelectedSlot())) break label_0;
        arg0.setCancelled(true);
        break label_6;
        label_0:
        Object var4 = arg0.getPacket();
        if (!((var4 instanceof ClientboundEntityEventPacket))) break label_3;
        var3 = ((ClientboundEntityEventPacket) var4);
        if (var3.getEventId() != 35) break label_3;
        Object var5 = var3.getEntity(mc.level);
        if ((var5 instanceof Player)) {
            var4 = ((Player) var5);
        } else {
            return;
        }
        var5 = ((Integer) this.poppedTotems.getOrDefault(var4.getUUID(), 0)).intValue();
        var5++;
        this.poppedTotems.put(var4.getUUID(), var5);
        LightningClient.EVENT_HANDLER.post(new PlayerPopEvent(var4, var5));
        label_3:
        if (this.pendingCrystalRetries.isEmpty()) break label_6;
        var4 = arg0.getPacket();
        if (!((var4 instanceof ClientboundRemoveEntitiesPacket))) break label_6;
        var3 = ((ClientboundRemoveEntitiesPacket) var4);
        var4 = var3.getEntityIds().iterator();
        while (var4.hasNext()) {
            var5 = ((Integer) var4.next()).intValue();
            Object var6 = ((Runnable) this.pendingCrystalRetries.remove(var5));
            if (var6 != null) {
                var6.run();
            }
        }
        label_6:
    }

    public int getRadius(double arg0) {
        return INDICES[Mth.clamp(((int) Math.ceil(arg0)), 0, INDICES.length)];
    }

    public Vec3i getOffset(int arg0) {
        return SPHERE[arg0];
    }

    @Generated
    public Map getPoppedTotems() {
        return this.poppedTotems;
    }

    @Generated
    public float getTimerMultiplier() {
        return this.timerMultiplier;
    }

    @Generated
    public void setTimerMultiplier(float arg0) {
        this.timerMultiplier = arg0;
    }

    @Generated
    public Timer getPlaceTimer() {
        return this.placeTimer;
    }

    static {
        SPHERE = new Vec3i[4187707];
        INDICES = new int[101];
    }


    class 1 implements Comparator {

        1(WorldManager arg0, BlockPos arg1) {
            this.val$origin = arg1;
            Objects.requireNonNull(arg0);
        }

        public int compare(BlockPos arg0, BlockPos arg1) {
            if (arg0.equals(arg1)) return 0;
            int var3 = Double.compare(this.val$origin.distSqr(arg0), this.val$origin.distSqr(arg1));
            if (!var3) {
                var3 = Integer.compare(((Math.abs(arg0.getX()) + Math.abs(arg0.getY())) + Math.abs(arg0.getZ())), ((Math.abs(arg1.getX()) + Math.abs(arg1.getY())) + Math.abs(arg1.getZ())));
            }
            return (!var3 ? 1 : var3);
        }

    }
}
