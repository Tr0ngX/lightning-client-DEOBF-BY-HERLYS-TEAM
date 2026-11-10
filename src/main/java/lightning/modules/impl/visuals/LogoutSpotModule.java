package lightning.modules.impl.visuals;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.EntitySpawnEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerConnectEvent;
import lightning.events.impl.PlayerDeathEvent;
import lightning.events.impl.PlayerDisconnectEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.ServerConnectEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.LimbAnimatorAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.core.IgnoreNakedModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "LogoutSpot", description = "Renders a frozen chams model and nametag where players logged out.", category = Module.Category.VISUALS)
public class LogoutSpotModule extends Module {
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    public BooleanSetting nametag;
    public BooleanSetting health;
    public BooleanSetting totemPops;
    public NumberSetting scale;
    private static final EquipmentSlot[] COPIED_SLOTS;
    private final Map spots;
    private final Map ghosts;
    private final Map trackedPlayers;
    private int nextId;
    private static final long DISCONNECT_DEBOUNCE_MS = 1500L;
    private final Map pendingDisconnects;
    private final Map recentPearlThrowers;
    private final Map activePearls;
    private long lastLocalTeleportTime;
    private Vec3 lastLocalPos;

    public LogoutSpotModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Fill";
        tmp0[1] = "Outline";
        tmp0[2] = "Both";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the logout chams.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.nametag = new BooleanSetting("Nametag", "Renders player info above the logout spot.", true);
        this.health = new BooleanSetting("Health", "Renders the health of the logged out player.", true);
        this.totemPops = new BooleanSetting("TotemPops", "Renders the popped totems count of the logged out player.", true);
        this.scale = new NumberSetting("Scale", "The scaling applied to the nametag rendering.", 30, 10, 100);
        this.spots = new ConcurrentHashMap();
        this.ghosts = new ConcurrentHashMap();
        this.trackedPlayers = new ConcurrentHashMap();
        this.nextId = -80000;
        this.pendingDisconnects = new ConcurrentHashMap();
        this.recentPearlThrowers = new ConcurrentHashMap();
        this.activePearls = new ConcurrentHashMap();
        this.lastLocalTeleportTime = 0L;
        this.lastLocalPos = null;
    }

    private boolean hasRecentlyPearled(UUID arg0) {
        if (arg0 == null) return false;
        Object var2 = ((Long) this.recentPearlThrowers.get(arg0));
        if (var2 != null && (System.currentTimeMillis() - var2.longValue()) < 12000L) return true;
        Object var3 = this.activePearls.values().iterator();
        label_2:
        if (var3.hasNext()) {
            Object var4 = ((UUID) var3.next());
            if (!(arg0.equals(var4))) {
                break label_2;
            }
            return true;
        }
        return false;
    }

    private void onLocalPlayerTeleport() {
        this.lastLocalTeleportTime = System.currentTimeMillis();
        this.pendingDisconnects.clear();
        this.trackedPlayers.clear();
    }

    public boolean isPlayerInServer(UUID arg0, String arg1) {
        if (arg0 == null && arg1 == null) return false;
        if (mc.level == null) break label_4;
        if (arg0 != null && mc.level.getPlayerByUUID(arg0) != null) return true;
        if (arg1 == null) break label_4;
        Object var3 = mc.level.players().iterator();
        label_2:
        if (!(var3.hasNext())) break label_4;
        Object var4 = ((Player) var3.next());
        if (isGhost(var4)) break label_2;
        if (!(arg1.equalsIgnoreCase(var4.getName().getString()))) {
            break label_2;
        }
        return true;
        label_4:
        if (mc.getConnection() == null) break label_11;
        if (arg0 != null && mc.getConnection().getPlayerInfo(arg0) != null) return true;
        if (arg1 != null && mc.getConnection().getPlayerInfo(arg1) != null) return true;
        var3 = mc.getConnection().getOnlinePlayers().iterator();
        label_7:
        if (!(var3.hasNext())) break label_11;
        var4 = ((PlayerInfo) var3.next());
        if (var4 == null) break label_7;
        if (var4.getProfile() == null) {
            break label_7;
        }
        if (arg0 != null && arg0.equals(var4.getProfile().id())) return true;
        if (arg1 == null) break label_7;
        if (!(arg1.equalsIgnoreCase(var4.getProfile().name()))) {
            break label_7;
        }
        return true;
        label_11:
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.level == null) || (mc.player == null)) break label_28;
        if (this.lastLocalPos != null && mc.player.position().distanceToSqr(this.lastLocalPos) > 100.0) {
            onLocalPlayerTeleport();
        }
        this.lastLocalPos = mc.player.position();
        long var2 = System.currentTimeMillis();
        this.recentPearlThrowers.entrySet().removeIf((arg1) -> (arg0 - ((Long) arg1.getValue()).longValue()) > 15000L);
        this.activePearls.entrySet().removeIf((arg0) -> mc.level.getEntity(((Integer) arg0.getKey()).intValue()) == null);
        if (LightningClient.MODULE_MANAGER == null) break label_1;
        break label_2;
        label_1:
        label_2:
        Object var4 = null;
        if (var4 != null && var4.isToggled()) {
        } else {
        }
        Object var5 = null;
        int var6 = ((IgnoreNakedModule) LightningClient.MODULE_MANAGER.getModule(IgnoreNakedModule.class)).isToggled();
        Object var7 = new ArrayList(mc.level.players()).iterator();
        while (var7.hasNext()) {
            Object var8 = ((Player) var7.next());
            if ((((var8 == mc.player) || (var8 == var5)) || (isGhost(var8))) || (!(var8.isAlive()))) break label_15;
            if (!(!var6)) {
                if (EntityUtils.isNaked(var8)) break label_15;
            }
            Object var9 = var8.getUUID();
            this.pendingDisconnects.remove(var9);
            if (this.spots.containsKey(var9)) {
                removeSpot(var9);
            }
            if (var8.getMainHandItem().getItem() != Items.ENDER_PEARL) {
                if (var8.getOffhandItem().getItem() == Items.ENDER_PEARL) {
                }
            } else {
            }
            int var10 = 0;
            if (!var10) break label_12;
            if (!var8.swinging) {
                if (!(var8.isUsingItem())) break label_12;
            }
            this.recentPearlThrowers.put(var9, var2);
            label_12:
            if (LightningClient.WORLD_MANAGER != null) {
            } else {
            }
            int var11 = 0;
            this.trackedPlayers.put(var9, new LogoutSpotModule.TrackedPlayer(var8, var11));
            label_15:
        }
        if (this.pendingDisconnects.isEmpty()) break label_23;
        var7 = this.pendingDisconnects.entrySet().iterator();
        while (var7.hasNext()) {
            var8 = ((Map.Entry) var7.next());
            var9 = ((UUID) var8.getKey());
            var10 = ((LogoutSpotModule.TrackedPlayer) this.trackedPlayers.get(var9));
            var11 = (var10 != null ? var10.name : null);
            if (isPlayerInServer(var9, var11)) {
                this.pendingDisconnects.remove(var9);
                break label_22;
            }
            if (hasRecentlyPearled(var9)) {
                this.pendingDisconnects.remove(var9);
                break label_22;
            }
            if ((var2 - ((Long) var8.getValue()).longValue()) < 1500L) break label_22;
            this.pendingDisconnects.remove(var9);
            spawnGhost(var9);
            label_22:
        }
        label_23:
        if (this.spots.isEmpty()) break label_28;
        var7 = this.spots.entrySet().iterator();
        while (var7.hasNext()) {
            var8 = ((Map.Entry) var7.next());
            var9 = ((UUID) var8.getKey());
            var10 = ((LogoutSpotModule.Spot) var8.getValue());
            if (var10.data != null) {
            } else {
            }
            var11 = null;
            if (isPlayerInServer(var9, var11)) {
                removeSpot(var9);
            }
        }
        label_28:
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerDisconnectEvent arg0) {
        if (((!(isToggled())) || (mc.level == null)) || (mc.player == null)) {
            return;
        }
        Object var3 = arg0.getId();
        if ((var3 == null) || (var3.equals(mc.player.getUUID()))) {
            return;
        }
        if ((System.currentTimeMillis() - this.lastLocalTeleportTime) < 3000L) return;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var4 = null;
        if (var2 != null && var2.isToggled() && var2.getPlayer() != null && var3.equals(var2.getPlayer().getUUID())) return;
        if (!(this.trackedPlayers.containsKey(var3))) return;
        if (hasRecentlyPearled(var3)) {
            this.trackedPlayers.remove(var3);
            return;
        }
        this.pendingDisconnects.put(var3, System.currentTimeMillis());
    }

    private void spawnGhost(UUID arg0) {
        Object var2 = ((LogoutSpotModule.TrackedPlayer) this.trackedPlayers.get(arg0));
        if (var2 == null) return;
        mc.execute(() -> {
    if ((mc.level == null) || (this.spots.containsKey(arg0))) {
        return;
    }
    if (isPlayerInServer(arg0, arg1.name)) return;
    if (hasRecentlyPearled(arg0)) return;
    Object var3 = new GameProfile(UUID.randomUUID(), arg1.name);
    Object var4 = new RemotePlayer(mc.level, var3);
    this.nextId = (this.nextId - 1);
    var4.setId(this.nextId);
    var4.setPos(arg1.x, arg1.y, arg1.z);
    var4.xo = arg1.xo;
    var4.yo = arg1.yo;
    var4.zo = arg1.zo;
    var4.xOld = arg1.x;
    var4.yOld = arg1.y;
    var4.zOld = arg1.z;
    var4.setYRot(arg1.yRot);
    var4.setXRot(arg1.xRot);
    var4.yRotO = arg1.yRot;
    var4.xRotO = arg1.xRot;
    var4.setYHeadRot(arg1.yHeadRot);
    var4.yHeadRotO = arg1.yHeadRot;
    var4.yBodyRot = arg1.yBodyRot;
    var4.yBodyRotO = arg1.yBodyRot;
    var4.setPose(arg1.pose);
    var4.setShiftKeyDown(arg1.shift);
    var4.setSwimming(arg1.swim);
    var4.setSprinting(arg1.sprint);
    var4.refreshDimensions();
    var4.walkAnimation.setSpeed(arg1.walkSpeed);
    ((LimbAnimatorAccessor) var4.walkAnimation).setPos(arg1.walkPos);
    var4.swinging = arg1.swinging;
    var4.swingTime = arg1.swingTime;
    var4.swingingArm = arg1.swingingArm;
    var4.attackAnim = arg1.attackAnim;
    var4.oAttackAnim = arg1.oAttackAnim;
    var4.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    var4.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    Object var5 = COPIED_SLOTS;
    int var6 = var5.length;
    int var7 = 0;
    while (var7 < var6) {
        Object var8 = var5[var7];
        var4.setItemSlot(var8, ((ItemStack) arg1.armor.getOrDefault(var8, ItemStack.EMPTY)).copy());
        var7++;
    }
    mc.level.addEntity(var4);
    var5 = new LogoutSpotModule.Spot(var4, arg1);
    this.spots.put(arg0, var5);
    this.ghosts.put(var4, var5);
});
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent arg0) {
        Object var2 = arg0.getId();
        if (var2 == null) return;
        this.pendingDisconnects.remove(var2);
        if (this.spots.containsKey(var2)) {
            mc.execute(() -> removeSpot(arg0));
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent arg0) {
        if (arg0.getPlayer() != mc.player) return;
        clearAll();
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        clearAll();
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent arg0) {
        clearAll();
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent arg0) {
        clearAll();
    }

    @SubscribeEvent
    public void onEntitySpawn(EntitySpawnEvent arg0) {
        if (mc.level == null) return;
        Object var2 = arg0.getEntity();
        if (!((var2 instanceof ThrownEnderpearl))) break label_6;
        Object var3 = ((ThrownEnderpearl) var2);
        Object var4 = null;
        Object var5 = var3.getOwner();
        if ((var5 instanceof Player)) {
            Object var6 = ((Player) var5);
            if (var6 != mc.player && !(isGhost(var6))) {
                var4 = var6.getUUID();
            }
            break label_5;
        }
        var6 = 36.0;
        Object var8 = mc.level.players().iterator();
        while (var8.hasNext()) {
            Object var9 = ((Player) var8.next());
            if ((var9 == mc.player) || (isGhost(var9))) break label_3;
            double var10 = var9.distanceToSqr(var3.position());
            if (var9.distanceToSqr(var3.position()) >= var6) {
                break label_3;
            }
            var6 = var10;
            var4 = var9.getUUID();
        }
        label_5:
        if (var4 == null) break label_6;
        this.recentPearlThrowers.put(var4, System.currentTimeMillis());
        this.activePearls.put(var3.getId(), var4);
        label_6:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((arg0.getPacket() instanceof ClientboundRespawnPacket)) {
            mc.execute(this::clearAll);
            return;
        }
        Object var2 = arg0.getPacket();
        if (!((var2 instanceof ClientboundPlayerPositionPacket))) break label_1;
        Object var10 = ((ClientboundPlayerPositionPacket) var2);
        if (mc.player == null) break label_1;
        var tmp0 = mc.player.position();
        Object var9 = tmp0;
        double var7 = var10.change().position().x();
        double var5 = var10.change().position().y();
        double var3 = var10.change().position().z();
        if (tmp0.distanceToSqr(var10.change().position().x(), var10.change().position().y(), var10.change().position().z()) <= 100.0) break label_1;
        mc.execute(this::onLocalPlayerTeleport);
        label_1:
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent.Post arg0) {
        if (((!(isToggled())) || (mc.level == null)) || (mc.player == null)) {
            return;
        }
        if ((!(this.nametag.getValue())) || (this.spots.isEmpty())) {
            return;
        }
        Object var2 = arg0.getMatrices();
        Object var3 = this.spots.values().iterator();
        while (var3.hasNext()) {
            Object var4 = ((LogoutSpotModule.Spot) var3.next());
            Object var5 = var4.ghost;
            if (var5 == null) {
                break label_4;
            }
            double var6 = var5.getX();
            double var8 = (var5.getY() + ((double) (var5.isShiftKeyDown() ? 1.9f : 2.1f)));
            double var10 = var5.getZ();
            Object var12 = new Vec3((var6 - mc.gameRenderer.mainCamera().position().x), (var8 - mc.gameRenderer.mainCamera().position().y), (var10 - mc.gameRenderer.mainCamera().position().z));
            float var13 = ((float) Math.sqrt(mc.gameRenderer.mainCamera().position().distanceToSqr(var6, var8, var10)));
            float var14 = (0.0018f + ((((float) this.scale.getValue().intValue()) / 10000.0f) * var13));
            if (var13 <= 8.0f) {
                var14 = 0.0245f;
            }
            var2.pushPose();
            var2.translate(var12.x, var12.y, var12.z);
            var2.mulPose(mc.gameRenderer.mainCamera().rotation());
            var2.scale(var14, -(var14), var14);
            Object var15 = new StringBuilder(var4.data.name);
            if (this.health.getValue()) {
                float var16 = (var4.data.health + var4.data.absorption);
                var15.append(" HP: ").append(new DecimalFormat("0.0").format(((double) var16)));
            }
            if (this.totemPops.getValue() && var4.data.totemPops > 0) {
                var15.append(" -").append(var4.data.totemPops);
            }
            var16 = var15.toString();
            int var17 = LightningClient.FONT_MANAGER.getWidth(var16);
            int var18 = LightningClient.FONT_MANAGER.getHeight();
            Renderer3D.renderQuad(var2, ((((float) -(var17)) / 2.0f) - 2.0f), ((float) (-(var18) - 1)), ((((float) var17) / 2.0f) + 2.0f), 1.0f, new Color(0, 0, 0, 120));
            Renderer3D.renderOutline(var2, ((((float) -(var17)) / 2.0f) - 2.0f), ((float) (-(var18) - 1)), ((((float) var17) / 2.0f) + 2.0f), 1.0f, new Color(0, 0, 0, 160));
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
            LightningClient.FONT_MANAGER.drawText(var2, var16, (-(var17) / 2), -(var18), ColorUtils.getColor(this.fillColor.getColor(), 255));
            var2.popPose();
        }
    }

    public void onDisable() {
        clearAll();
    }

    private void removeSpot(UUID arg0) {
        Object var2 = ((LogoutSpotModule.Spot) this.spots.remove(arg0));
        if (var2 != null) {
            this.ghosts.remove(var2.ghost);
            despawn(var2.ghost);
        }
    }

    private void clearAll() {
        this.spots.values().forEach(() -> despawn(arg0.ghost));
        this.spots.clear();
        this.ghosts.clear();
        this.trackedPlayers.clear();
        this.pendingDisconnects.clear();
        this.recentPearlThrowers.clear();
        this.activePearls.clear();
        this.lastLocalPos = null;
    }

    private void despawn(RemotePlayer arg0) {
        if (arg0 == null) return;
        Object var2 = () -> {
    if (mc.level != null) {
        mc.level.removeEntity(arg0.getId(), Entity.RemovalReason.DISCARDED);
    }
};
        if (mc.isSameThread()) {
            var2.run();
        } else {
            mc.execute(var2);
        }
    }

    public boolean isGhost(Entity arg0) {
        if (!((arg0 instanceof RemotePlayer))) break label_0;
        return this.ghosts.containsKey(arg0);
    }

    public Collection getGhosts() {
        return this.ghosts.keySet();
    }

    public LogoutSpotModule.Spot getSpot(RemotePlayer arg0) {
        return ((LogoutSpotModule.Spot) this.ghosts.get(arg0));
    }

    public boolean shouldFill() {
        if (!(this.mode.getValue().equals("Fill"))) {
            if (this.mode.getValue().equals("Both")) {
            }
        } else {
        }
        return false;
    }

    public boolean shouldOutline() {
        if (!(this.mode.getValue().equals("Outline"))) {
            if (this.mode.getValue().equals("Both")) {
            }
        } else {
        }
        return false;
    }

    public Color getFillColor(RemotePlayer arg0) {
        return this.fillColor.getColor();
    }

    public Color getOutlineColor(RemotePlayer arg0) {
        return this.outlineColor.getColor();
    }

    static {
        var tmp0 = new EquipmentSlot[4];
        tmp0[0] = EquipmentSlot.HEAD;
        tmp0[1] = EquipmentSlot.CHEST;
        tmp0[2] = EquipmentSlot.LEGS;
        tmp0[3] = EquipmentSlot.FEET;
        COPIED_SLOTS = tmp0;
    }


    public class TrackedPlayer {
        public final String name;
        public final double x;
        public final double y;
        public final double z;
        public final double xo;
        public final double yo;
        public final double zo;
        public final float yRot;
        public final float xRot;
        public final float yHeadRot;
        public final float yBodyRot;
        public final Pose pose;
        public final boolean shift;
        public final boolean swim;
        public final boolean sprint;
        public final float walkSpeed;
        public final float walkPos;
        public final boolean swinging;
        public final int swingTime;
        public final InteractionHand swingingArm;
        public final float attackAnim;
        public final float oAttackAnim;
        public final ItemStack mainHand;
        public final ItemStack offHand;
        public final Map armor;
        public final float health;
        public final float absorption;
        public final int totemPops;

        public TrackedPlayer(Player arg0, int arg1) {
            this.armor = new ConcurrentHashMap();
            this.name = arg0.getName().getString();
            this.x = arg0.getX();
            this.y = arg0.getY();
            this.z = arg0.getZ();
            this.xo = arg0.xo;
            this.yo = arg0.yo;
            this.zo = arg0.zo;
            this.yRot = arg0.getYRot();
            this.xRot = arg0.getXRot();
            this.yHeadRot = arg0.getYHeadRot();
            this.yBodyRot = arg0.yBodyRot;
            this.pose = arg0.getPose();
            this.shift = arg0.isShiftKeyDown();
            this.swim = arg0.isSwimming();
            this.sprint = arg0.isSprinting();
            this.walkSpeed = arg0.walkAnimation.speed();
            this.walkPos = arg0.walkAnimation.position();
            this.swinging = arg0.swinging;
            this.swingTime = arg0.swingTime;
            this.swingingArm = arg0.swingingArm;
            this.attackAnim = arg0.attackAnim;
            this.oAttackAnim = arg0.oAttackAnim;
            this.mainHand = arg0.getMainHandItem().copy();
            this.offHand = arg0.getOffhandItem().copy();
            Object var3 = LogoutSpotModule.COPIED_SLOTS;
            int var4 = var3.length;
            int var5 = 0;
            while (var5 < var4) {
                Object var6 = var3[var5];
                this.armor.put(var6, arg0.getItemBySlot(var6).copy());
                var5++;
            }
            this.health = arg0.getHealth();
            this.absorption = arg0.getAbsorptionAmount();
            this.totemPops = arg1;
        }

    }

    public class Spot {
        public final RemotePlayer ghost;
        public final LogoutSpotModule.TrackedPlayer data;
        public final long timestamp;

        public Spot(RemotePlayer arg0, LogoutSpotModule.TrackedPlayer arg1) {
            this.ghost = arg0;
            this.data = arg1;
            this.timestamp = System.currentTimeMillis();
        }

    }
}
