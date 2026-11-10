package lightning.modules.impl.miscellaneous;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.PlayerListS2CPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

@RegisterModule(name = "FakePlayer", description = "Spawns in a fake player entity that you can use to test modules on.", category = Module.Category.MISCELLANEOUS, proxyEnhanced = true)
public class FakePlayerModule extends Module {
    public StringSetting name;
    public NumberSetting health;
    public NumberSetting absorption;
    public CategorySetting movementCategory;
    public ModeSetting movementMode;
    public NumberSetting velocity;
    public BooleanSetting changeDirection;
    public NumberSetting timeout;
    private RemotePlayer player;
    private double[] direction;
    private final Timer timer;
    private boolean stepping;
    private final Timer stepTimer;
    private FakePlayerModule.RecordState recordState;
    private final List frames;
    private int playIndex;

    public FakePlayerModule() {
        this.name = new StringSetting("Name", "The name that will be assigned to the fake player.", "Dummy");
        this.health = new NumberSetting("Health", "The amount of health that will be assigned to the fake player.", 20.0f, 1.0f, 20.0f);
        this.absorption = new NumberSetting("Absorption", "The amount of absorption that will be assigned to the fake player.", 16, 0, 16);
        this.movementCategory = new CategorySetting("Movement", "The category that contains settings related to movement.");
        var tmp0 = new String[2];
        tmp0[0] = "None";
        tmp0[1] = "Random";
        this.movementMode = new ModeSetting("Movement", "Mode", "The mode that will be used for the fake player's movement.", new CategorySetting.Visibility(this.movementCategory), "None", tmp0);
        this.velocity = new NumberSetting("Velocity", "Velocity", "The velocity to apply on the fake player.", new CategorySetting.Visibility(this.movementCategory), 0.3f, 0.1f, 0.4f);
        this.changeDirection = new BooleanSetting("ChangeDirection", "Changes the player's direction when a specified amount of time has passed.", new CategorySetting.Visibility(this.movementCategory), false);
        this.timeout = new NumberSetting("Timeout", "The amount of time that it takes for the player to change direction.", new BooleanSetting.Visibility(this.changeDirection, true), 5, 1, 15);
        this.player = null;
        this.direction = generateDirection();
        this.timer = new Timer();
        this.stepping = false;
        this.stepTimer = new Timer();
        this.recordState = FakePlayerModule.RecordState.NONE;
        this.frames = new ArrayList();
        this.playIndex = 0;
    }

    public void startRecording() {
        if (this.player == null) {
            LightningClient.CHAT_MANAGER.warn("Spawn a fake player first before recording!");
            return;
        }
        this.frames.clear();
        this.recordState = FakePlayerModule.RecordState.RECORDING;
        LightningClient.CHAT_MANAGER.tagged("Started recording movements...", "FakePlayer");
    }

    public void stopRecording() {
        this.recordState = FakePlayerModule.RecordState.NONE;
        LightningClient.CHAT_MANAGER.tagged("Record!", "FakePlayer");
    }

    public void startPlaying() {
        if (this.frames.isEmpty()) {
            LightningClient.CHAT_MANAGER.warn("No recorded frames to play! Use '.fakeplayer record' first.");
            return;
        }
        if (this.player == null) {
            setToggled(true);
        }
        this.recordState = FakePlayerModule.RecordState.PLAYING;
        this.playIndex = 0;
        LightningClient.CHAT_MANAGER.tagged("Playing recorded movements in a loop...", "FakePlayer");
    }

    @SubscribeEvent
    public void onDisconnect(ClientConnectEvent arg0) {
        if (isToggled()) {
            setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if (arg0.getPlayer() != mc.player) return;
        if (isToggled()) {
            setToggled(false);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        Object var4 = arg0.getPacket();
        if (!((var4 instanceof ClientboundEntityEventPacket))) break label_2;
        var tmp0 = ((ClientboundEntityEventPacket) var4);
        Object var3 = tmp0;
        if (tmp0.getEventId() != 3) break label_2;
        var tmp1 = var3.getEntity(mc.level);
        Object var2 = tmp1;
        if ((tmp1 != mc.player) || (!(isToggled()))) break label_2;
        setToggled(false);
        label_2:
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) break label_1;
        if (!(mc.player.isDeadOrDying()) && mc.player.getHealth() > 0.0f && mc.player.isAlive()) {
            if ((mc.gui == null) || (!((mc.gui.screen() instanceof DeathScreen)))) break label_1;
        }
        if (!(isToggled())) break label_1;
        setToggled(false);
        label_1:
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (this.recordState == FakePlayerModule.RecordState.RECORDING) {
            this.frames.add(new FakePlayerModule.RecordFrame(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(), mc.player.getYHeadRot(), mc.player.getPose()));
            return;
        }
        if (this.recordState == FakePlayerModule.RecordState.PLAYING) {
            if ((this.player == null) || (this.frames.isEmpty())) {
                return;
            }
            this.playIndex = (this.playIndex + 1);
            if (this.playIndex >= this.frames.size()) {
                this.playIndex = 0;
            }
            broadcastPosition();
            return;
        }
        if (this.player == null) return;
        if (!(this.movementMode.getValue().equalsIgnoreCase("Random"))) return;
        Object var2 = this.player.blockPosition();
        int var3 = 0;
        if (this.changeDirection.getValue() && this.timer.hasTimeElapsed((this.timeout.getValue().longValue() * 1000L))) {
            var3 = 1;
            this.timer.reset();
        }
        if (!(hasObstruction(var2))) break label_14;
        Object var4 = Direction.values();
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            Object var7 = var4[var6];
            Object var8 = var2.relative(var7);
            if (!(WorldUtils.blocksMovement(mc.level.getBlockState(var8)))) {
                if (!(WorldUtils.blocksMovement(mc.level.getBlockState(var8.above())))) break label_13;
            }
            if (!(WorldUtils.blocksMovement(mc.level.getBlockState(var8.above().above())))) break label_13;
            if (!(this.player.getDirection().equals(var7))) {
                break label_13;
            }
            var3 = 1;
            this.timer.reset();
            label_13:
            var6++;
        }
        label_14:
        if (!(!var3)) {
            var4 = generateDirection();
            if (this.direction == var4) {
                var4 = generateDirection();
            }
            this.direction = var4;
        }
        if (!(!this.stepping) && this.stepTimer.hasTimeElapsed(500L)) {
            this.stepping = false;
            this.stepTimer.reset();
        }
        this.player.jumpFromGround();
        var4 = RotationUtils.getRotations(this.player, (this.player.getX() + this.direction[0]), (this.player.getY() + ((double) fixAxisY(this.player))), (this.player.getZ() + this.direction[1]));
        this.player.setYRot(var4[0]);
        this.player.yHeadRot = var4[0];
        this.player.setXRot(0.0f);
        this.player.setPos((this.player.getX() + this.direction[0]), (this.player.getY() + ((double) fixAxisY(this.player))), (this.player.getZ() + this.direction[1]));
        broadcastPosition();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((this.recordState != FakePlayerModule.RecordState.PLAYING) || (this.player == null)) || (this.frames.isEmpty())) {
            return;
        }
        int var2 = this.playIndex;
        int var3 = ((var2 + 1) % this.frames.size());
        Object var4 = ((FakePlayerModule.RecordFrame) this.frames.get(var2));
        Object var5 = ((FakePlayerModule.RecordFrame) this.frames.get(var3));
        float var6 = arg0.getTickDelta();
        if (!var3) {
            var6 = 0.0f;
        }
        double var7 = Mth.lerp(((double) var6), var4.x(), var5.x());
        double var9 = Mth.lerp(((double) var6), var4.y(), var5.y());
        double var11 = Mth.lerp(((double) var6), var4.z(), var5.z());
        float var13 = Mth.rotLerp(var6, var4.yRot(), var5.yRot());
        float var14 = Mth.lerp(var6, var4.xRot(), var5.xRot());
        float var15 = Mth.rotLerp(var6, var4.yHeadRot(), var5.yHeadRot());
        this.player.setPos(var7, var9, var11);
        this.player.setYRot(var13);
        this.player.setXRot(var14);
        this.player.yHeadRot = var15;
        this.player.yBodyRot = var13;
        this.player.setOldPosAndRot();
        this.player.yHeadRotO = var15;
        this.player.yBodyRotO = var13;
        this.player.setPose(var4.pose());
        this.player.refreshDimensions();
    }

    private void broadcastPosition() {
        if ((!(isRunningOnProxy())) || (LightningClient.PROXY_SERVER == null)) {
            return;
        }
        if (this.player == null) return;
        Object var1 = ClientboundTeleportEntityPacket.teleport(this.player.getId(), PositionMoveRotation.of(this.player), Set.of(), this.player.onGround());
        Object var2 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Connection) var2.next());
            if (!(var3.isConnected())) {
                break label_3;
            }
            var3.send(var1);
        }
    }

    public void onEnable() {
        if (shouldRunOnProxy()) return;
        if (mc.level != null) {
            if (mc.player == null) {
            }
            setToggled(false);
            return;
        }
        this.player = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), this.name.getValue()));
        this.player.copyPosition(mc.player);
        this.player.setId(-673);
        this.player.restoreFrom(mc.player);
        this.player.setHealth(this.health.getValue().floatValue());
        this.player.setAbsorptionAmount(this.absorption.getValue().floatValue());
        Object var1 = EquipmentSlot.values();
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Object var4 = var1[var3];
            this.player.setItemSlot(var4, mc.player.getItemBySlot(var4).copy());
            var3++;
        }
        mc.level.addEntity(this.player);
        this.player.tick();
        this.timer.reset();
        broadcastSpawn();
        LightningClient.CHAT_MANAGER.tagged(("Spawned fake player " + this.name.getValue()), "FakePlayer");
    }

    public void onDisable() {
        this.recordState = FakePlayerModule.RecordState.NONE;
        if (shouldRunOnProxy()) return;
        if (this.player != null) {
            broadcastDespawn();
            if (mc.level != null) {
                mc.level.removeEntity(this.player.getId(), Entity.RemovalReason.DISCARDED);
            }
            this.player = null;
            LightningClient.CHAT_MANAGER.tagged("Removed fake player.", "FakePlayer");
        }
    }

    private void broadcastSpawn() {
        if ((!(isRunningOnProxy())) || (LightningClient.PROXY_SERVER == null)) {
            return;
        }
        if (this.player == null) return;
        Object var1 = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(this.player.getUUID(), this.player.getGameProfile(), false, 0, GameType.SURVIVAL, null, false, 0, null));
        Object var2 = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), Collections.emptyList());
        ((PlayerListS2CPacketAccessor) var2).setActions(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER));
        ((PlayerListS2CPacketAccessor) var2).setEntries(var1);
        Object var3 = new ClientboundAddEntityPacket(this.player.getId(), this.player.getUUID(), this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getXRot(), this.player.getYRot(), this.player.getType(), 0, this.player.getDeltaMovement(), ((double) this.player.getYHeadRot()));
        Object var4 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Connection) var4.next());
            if (!(var5.isConnected())) {
                break label_3;
            }
            var5.send(var2);
            var5.send(var3);
        }
    }

    private void broadcastDespawn() {
        if ((!(isRunningOnProxy())) || (LightningClient.PROXY_SERVER == null)) {
            return;
        }
        if (this.player == null) return;
        var tmp0 = new int[1];
        tmp0[0] = this.player.getId();
        Object var1 = new ClientboundRemoveEntitiesPacket(tmp0);
        Object var2 = new ClientboundPlayerInfoRemovePacket(List.of(this.player.getUUID()));
        Object var3 = LightningClient.PROXY_SERVER.getConnections().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Connection) var3.next());
            if (!(var4.isConnected())) {
                break label_3;
            }
            var4.send(var1);
            var4.send(var2);
        }
    }

    public boolean hasObstruction(BlockPos arg0) {
        Object var2 = Direction.values();
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            Object var6 = arg0.relative(var5);
            if (WorldUtils.blocksMovement(mc.level.getBlockState(var6))) return true;
            if (!(WorldUtils.blocksMovement(mc.level.getBlockState(var6.above())))) {
            } else {
                return true;
            }
            var4++;
        }
        return false;
    }

    public float fixAxisY(Player arg0) {
        if (mc.level.getBlockState(arg0.blockPosition().below()).getBlock() == Blocks.AIR && !this.stepping) return -1.0f;
        if (hasObstruction(arg0.blockPosition())) {
            Object var2 = Direction.values();
            int var3 = var2.length;
            int var4 = 0;
            while (var4 < var3) {
                Object var5 = var2[var4];
                Object var6 = arg0.blockPosition().relative(var5);
                if (WorldUtils.blocksMovement(mc.level.getBlockState(var6)) && !(WorldUtils.blocksMovement(mc.level.getBlockState(var6.above()))) && arg0.getDirection().equals(var5) && !(WorldUtils.blocksMovement(mc.level.getBlockState(var6.above().above())))) {
                    this.stepping = true;
                    this.stepTimer.reset();
                    return 1.0f;
                }
                if ((!(WorldUtils.blocksMovement(mc.level.getBlockState(var6.above())))) || (!(arg0.getDirection().equals(var5)))) break label_4;
                if (WorldUtils.blocksMovement(mc.level.getBlockState(var6.above().above()))) {
                    break label_4;
                }
                this.stepping = true;
                this.stepTimer.reset();
                return 2.0f;
                label_4:
                var4++;
            }
            return 0.0f;
        }
        return 0.0f;
    }

    public double[] generateDirection() {
        double var1 = MathUtils.random(6.283185307179586, 0.0);
        var tmp0 = new double[2];
        tmp0[0] = -(Math.sin(var1));
        tmp0[1] = Math.cos(var1);
        Object var3 = tmp0;
        var tmp1 = new double[2];
        tmp1[0] = (var3[0] * ((double) this.velocity.getValue().floatValue()));
        tmp1[1] = (var3[1] * ((double) this.velocity.getValue().floatValue()));
        return tmp1;
    }

    @Generated
    public RemotePlayer getPlayer() {
        return this.player;
    }

    @Generated
    public FakePlayerModule.RecordState getRecordState() {
        return this.recordState;
    }


    enum RecordState {
        NONE,
        RECORDING,
        PLAYING;


        private RecordState() {
        }

    }

    public record RecordFrame(double x, double y, double z, float yRot, float xRot, float yHeadRot, Pose pose) {

        public RecordFrame(double arg0, double arg1, double arg2, float arg3, float arg4, float arg5, Pose arg6) {
            this.x = arg0;
            this.y = arg1;
            this.z = arg2;
            this.yRot = arg3;
            this.xRot = arg4;
            this.yHeadRot = arg5;
            this.pose = arg6;
        }

        public double x() {
            return this.x;
        }

        public double y() {
            return this.y;
        }

        public double z() {
            return this.z;
        }

        public float yRot() {
            return this.yRot;
        }

        public float xRot() {
            return this.xRot;
        }

        public float yHeadRot() {
            return this.yHeadRot;
        }

        public Pose pose() {
            return this.pose;
        }

    }
}
