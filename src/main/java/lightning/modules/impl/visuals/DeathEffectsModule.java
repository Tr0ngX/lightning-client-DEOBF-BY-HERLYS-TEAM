package lightning.modules.impl.visuals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.KillEffectMemeRenderer;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

@RegisterModule(name = "DeathEffects", description = "Renders certain effects on players when they die or disconnect.", category = Module.Category.VISUALS)
public class DeathEffectsModule extends Module {
    public ModeSetting mode;
    public BooleanSetting lightningOnKill;
    public BooleanSetting lightningOnDisconnect;
    public NumberSetting lightningAmount;
    public NumberSetting disconnectRange;
    public BooleanSetting overlayOnKill;
    public BooleanSetting overlayOnDisconnect;
    public NumberSetting overlayDuration;
    public NumberSetting overlayFadeDuration;
    public NumberSetting overlayAlpha;
    public BooleanSetting memeOnKill;
    public BooleanSetting memeOnDisconnect;
    public NumberSetting memeSize;
    private static final Identifier VINE_SOUND_ID;
    private static final SoundEvent VINE_SOUND;
    private final Map lastPlayerPositions;
    private final Set trackedPlayers;
    private long freezeEndTime;
    private boolean frostActive;
    private int nextLightningId;

    public DeathEffectsModule() {
        var tmp0 = new String[4];
        tmp0[0] = "Lightning";
        tmp0[1] = "Overlay";
        tmp0[2] = "Meme";
        tmp0[3] = "Both";
        this.mode = new ModeSetting("Mode", "The effect that will be rendered.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Lightning";
        tmp1[1] = "Both";
        this.lightningOnKill = new BooleanSetting("LightningOnKill", "Spawn lightning bolt when a player dies near you.", new ModeSetting.Visibility(this.mode, tmp1), true);
        var tmp2 = new String[2];
        tmp2[0] = "Lightning";
        tmp2[1] = "Both";
        this.lightningOnDisconnect = new BooleanSetting("LightningOnDisconnect", "Spawn lightning bolt when a player disconnects near you.", new ModeSetting.Visibility(this.mode, tmp2), true);
        var tmp3 = new String[2];
        tmp3[0] = "Lightning";
        tmp3[1] = "Both";
        this.lightningAmount = new NumberSetting("LightningAmount", "The amount of lightning bolts to spawn.", new ModeSetting.Visibility(this.mode, tmp3), 1, 1, 10);
        this.disconnectRange = new NumberSetting("DisconnectRange", "Range to detect player disconnects.", 64.0, 1.0, 256.0);
        var tmp4 = new String[1];
        tmp4[0] = "Overlay";
        this.overlayOnKill = new BooleanSetting("OverlayOnKill", "Show freeze overlay when a player dies near you.", new ModeSetting.Visibility(this.mode, tmp4), true);
        var tmp5 = new String[1];
        tmp5[0] = "Overlay";
        this.overlayOnDisconnect = new BooleanSetting("OverlayOnDisconnect", "Show freeze overlay when a player disconnects near you.", new ModeSetting.Visibility(this.mode, tmp5), true);
        var tmp6 = new String[1];
        tmp6[0] = "Overlay";
        this.overlayDuration = new NumberSetting("OverlayDuration", "Duration of the freeze overlay in milliseconds.", new ModeSetting.Visibility(this.mode, tmp6), 3000, 100, 10000);
        var tmp7 = new String[1];
        tmp7[0] = "Overlay";
        this.overlayFadeDuration = new NumberSetting("OverlayFadeDuration", "Fade duration of the freeze overlay in milliseconds.", new ModeSetting.Visibility(this.mode, tmp7), 2000, 100, 5000);
        var tmp8 = new String[1];
        tmp8[0] = "Overlay";
        this.overlayAlpha = new NumberSetting("OverlayAlpha", "Transparency of the freeze overlay (0-255, higher = more transparent).", new ModeSetting.Visibility(this.mode, tmp8), 200, 0, 255);
        var tmp9 = new String[2];
        tmp9[0] = "Meme";
        tmp9[1] = "Both";
        this.memeOnKill = new BooleanSetting("MemeOnKill", "Show the meme effect when a player dies near you.", new ModeSetting.Visibility(this.mode, tmp9), true);
        var tmp10 = new String[2];
        tmp10[0] = "Meme";
        tmp10[1] = "Both";
        this.memeOnDisconnect = new BooleanSetting("MemeOnDisconnect", "Show the meme effect when a player disconnects near you.", new ModeSetting.Visibility(this.mode, tmp10), true);
        var tmp11 = new String[2];
        tmp11[0] = "Meme";
        tmp11[1] = "Both";
        this.memeSize = new NumberSetting("MemeSize", "Half-size of the circle/arrow, in blocks.", new ModeSetting.Visibility(this.mode, tmp11), 1.0, 0.3, 3.0);
        this.lastPlayerPositions = new HashMap();
        this.trackedPlayers = new HashSet();
        this.freezeEndTime = 0L;
        this.frostActive = false;
        this.nextLightningId = -90000;
    }

    public void onEnable() {
        this.lastPlayerPositions.clear();
        this.trackedPlayers.clear();
        this.freezeEndTime = 0L;
        this.frostActive = false;
    }

    public void onDisable() {
        if (mc.player != null) {
            mc.player.setTicksFrozen(0);
        }
        this.frostActive = false;
        this.freezeEndTime = 0L;
        this.lastPlayerPositions.clear();
        this.trackedPlayers.clear();
    }

    private boolean isInvalidTarget(Player arg0) {
        if ((arg0 == null) || (arg0 == mc.player)) {
            return true;
        }
        if (arg0.getId() < 0) return true;
        if (EntityUtils.isGhost(arg0)) return true;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if ((var2 == null) || (!(var2.isToggled()))) break label_6;
        return arg0 == var2.getPlayer();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        Object var3 = arg0.getPacket();
        if (!((var3 instanceof ClientboundEntityEventPacket))) break label_2;
        var tmp0 = ((ClientboundEntityEventPacket) var3);
        Object var2 = tmp0;
        if (tmp0.getEventId() != 3) break label_2;
        mc.execute(() -> {
    if (mc.level != null) {
        if (mc.player == null) {
        }
        return;
        try {
        }
        Object var5 = arg0.getEntity(mc.level);
        if (!((var5 instanceof Player))) break label_6;
        var tmp0 = ((Player) var5);
        Object var4 = tmp0;
        if (isInvalidTarget(tmp0)) break label_6;
        double var2 = mc.player.distanceToSqr(var4);
        if (mc.player.distanceToSqr(var4) > 100.0) break label_6;
        if (!(this.mode.getValue().equals("Lightning"))) {
            if (!(this.mode.getValue().equals("Both"))) break label_3;
        }
        if (!(this.lightningOnKill.getValue())) break label_3;
        spawnLightning(new Vec3(var4.getX(), var4.getY(), var4.getZ()));
        label_3:
        if (this.mode.getValue().equals("Overlay") && this.overlayOnKill.getValue()) {
            startOverlay();
        }
        if (!(this.mode.getValue().equals("Meme"))) {
            if (!(this.mode.getValue().equals("Both"))) break label_6;
        }
        if (!(this.memeOnKill.getValue())) break label_6;
        spawnMeme(new Vec3(var4.getX(), (var4.getY() + (((double) var4.getBbHeight()) * 0.5)), var4.getZ()));
        label_6:
    } catch (Exception ex7) {
        var2 = ex7;
    }
});
        label_2:
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        updateFrostEffect();
        Object var2 = new HashSet();
        Object var3 = mc.level.players().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if (isInvalidTarget(var4)) {
                break label_2;
            }
            var2.add(var4.getUUID());
            this.lastPlayerPositions.put(var4.getUUID(), new Vec3(var4.getX(), var4.getY(), var4.getZ()));
        }
        var3 = new HashSet(this.trackedPlayers).iterator();
        while (var3.hasNext()) {
            var4 = ((UUID) var3.next());
            if (var2.contains(var4)) break label_5;
            if (!(this.lastPlayerPositions.containsKey(var4))) {
                break label_5;
            }
            Object var5 = ((Vec3) this.lastPlayerPositions.get(var4));
            double var6 = Math.sqrt(((Math.pow((mc.player.getX() - var5.x), 2.0) + Math.pow((mc.player.getY() - var5.y), 2.0)) + Math.pow((mc.player.getZ() - var5.z), 2.0)));
            if (var6 > this.disconnectRange.getValue().doubleValue()) break label_11;
            if (!(this.mode.getValue().equals("Meme"))) {
                if (!(this.mode.getValue().equals("Both"))) break label_8;
            }
            if (!(this.memeOnDisconnect.getValue())) break label_8;
            spawnMeme(var5.add(0.0, 0.9, 0.0));
            label_8:
            if (!(this.mode.getValue().equals("Lightning"))) {
                if (!(this.mode.getValue().equals("Both"))) break label_10;
            }
            if (!(this.lightningOnDisconnect.getValue())) break label_10;
            spawnLightning(var5);
            label_10:
            if ((!(this.mode.getValue().equals("Overlay"))) || (!(this.overlayOnDisconnect.getValue()))) break label_11;
            startOverlay();
            label_11:
            this.lastPlayerPositions.remove(var4);
        }
        this.trackedPlayers.clear();
        this.trackedPlayers.addAll(var2);
    }

    private void updateFrostEffect() {
        if (!(!this.frostActive) && mc.player != null) {
            long var1 = System.currentTimeMillis();
            long var3 = (var1 - ((this.freezeEndTime - this.overlayDuration.getValue().longValue()) - this.overlayFadeDuration.getValue().longValue()));
            long var5 = (this.overlayDuration.getValue().longValue() + this.overlayFadeDuration.getValue().longValue());
            if (var3 < this.overlayDuration.getValue().longValue()) {
                int var7 = ((int) (140.0 * (this.overlayAlpha.getValue().doubleValue() / 255.0)));
                mc.player.setTicksFrozen(var7);
                break label_2;
            }
            if (var3 < var5) {
                var7 = (var3 - this.overlayDuration.getValue().longValue());
                double var9 = (1.0 - (((double) var7) / ((double) this.overlayFadeDuration.getValue().longValue())));
                int var11 = ((int) ((140.0 * (this.overlayAlpha.getValue().doubleValue() / 255.0)) * var9));
                mc.player.setTicksFrozen(var11);
                break label_2;
            }
            this.frostActive = false;
            this.freezeEndTime = 0L;
            mc.player.setTicksFrozen(0);
            label_2:
            break label_4;
        }
        if ((((mc.player == null) || (mc.player.getTicksFrozen() <= 0)) || (mc.level == null)) || (mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.POWDER_SNOW))) break label_4;
        mc.player.setTicksFrozen(0);
        label_4:
    }

    private void startOverlay() {
        this.frostActive = true;
        this.freezeEndTime = System.currentTimeMillis();
    }

    private void spawnLightning(Vec3 arg0) {
        if (mc.level == null) return;
        if (!(mc.isSameThread())) {
            mc.execute(() -> spawnLightning(arg0));
            return;
        }
        int var2 = this.lightningAmount.getValue().intValue();
        int var3 = 0;
        while (var3 < var2) {
            Object var4 = new LightningBolt(EntityTypes.LIGHTNING_BOLT, mc.level);
            var4.setPos(arg0.x, arg0.y, arg0.z);
            this.nextLightningId = (this.nextLightningId - 1);
            var4.setId(this.nextLightningId);
            mc.level.addEntity(var4);
            var3++;
        }
    }

    private void spawnMeme(Vec3 arg0) {
        KillEffectMemeRenderer.INSTANCE.spawn(new Vector3f(((float) arg0.x), ((float) arg0.y), ((float) arg0.z)), this.memeSize.getValue().floatValue());
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(VINE_SOUND, 1.0f, 1.0f));
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (!(this.mode.getValue().equals("Meme")) && !(this.mode.getValue().equals("Both"))) return;
        KillEffectMemeRenderer.INSTANCE.render(arg0.getMatrices());
    }

    static {
        VINE_SOUND_ID = Identifier.fromNamespaceAndPath("lightning", "vine");
        VINE_SOUND = SoundEvent.createVariableRangeEvent(VINE_SOUND_ID);
    }

}
