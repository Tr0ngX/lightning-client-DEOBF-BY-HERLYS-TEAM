package lightning.modules.impl.visuals;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.LimbAnimatorAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name = "PopChams", description = "Renders chams on a frozen snapshot of the entity's pose the moment they pop a totem.", category = Module.Category.VISUALS)
public class PopChamsModule extends Module {
    public NumberSetting duration;
    public NumberSetting yOffset;
    public ModeSetting mode;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private static final EquipmentSlot[] COPIED_SLOTS;
    private final Map ghosts;
    private int nextId;

    public PopChamsModule() {
        this.duration = new NumberSetting("Duration", "The duration for the pop chams fade.", 1500, 0, 5000);
        this.yOffset = new NumberSetting("YOffset", "The vertical float/rise offset for pop chams.", 0, -5, 5);
        var tmp0 = new String[3];
        tmp0[0] = "Fill";
        tmp0[1] = "Outline";
        tmp0[2] = "Both";
        this.mode = new ModeSetting("Mode", "The rendering that will be applied to the pop chams.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.ghosts = new ConcurrentHashMap();
        this.nextId = -90000;
    }

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent arg0) {
        if (((!(isToggled())) || (mc.level == null)) || (arg0.getPlayer() == mc.player)) {
            return;
        }
        Object var2 = arg0.getPlayer();
        mc.execute(() -> {
    if (mc.level == null) return;
    Object var2 = new GameProfile(UUID.randomUUID(), "");
    Object var3 = new RemotePlayer(mc.level, var2);
    this.nextId = (this.nextId - 1);
    var3.setId(this.nextId);
    var3.setPos(arg0.getX(), arg0.getY(), arg0.getZ());
    var3.xo = arg0.getX();
    var3.yo = arg0.getY();
    var3.zo = arg0.getZ();
    var3.xOld = arg0.getX();
    var3.yOld = arg0.getY();
    var3.zOld = arg0.getZ();
    var3.setYRot(arg0.getYRot());
    var3.setXRot(arg0.getXRot());
    var3.yRotO = arg0.getYRot();
    var3.xRotO = arg0.getXRot();
    var3.setYHeadRot(arg0.getYHeadRot());
    var3.yHeadRotO = arg0.getYHeadRot();
    var3.yBodyRot = arg0.yBodyRot;
    var3.yBodyRotO = arg0.yBodyRot;
    var3.setPose(arg0.getPose());
    var3.setShiftKeyDown(arg0.isShiftKeyDown());
    var3.setSwimming(arg0.isSwimming());
    var3.setSprinting(arg0.isSprinting());
    var3.refreshDimensions();
    var3.walkAnimation.setSpeed(arg0.walkAnimation.speed());
    ((LimbAnimatorAccessor) var3.walkAnimation).setPos(arg0.walkAnimation.position());
    var3.swinging = arg0.swinging;
    var3.swingTime = arg0.swingTime;
    var3.swingingArm = arg0.swingingArm;
    var3.attackAnim = arg0.attackAnim;
    var3.oAttackAnim = arg0.oAttackAnim;
    var3.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    var3.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    Object var4 = COPIED_SLOTS;
    int var5 = var4.length;
    int var6 = 0;
    while (var6 < var5) {
        Object var7 = var4[var6];
        var3.setItemSlot(var7, arg0.getItemBySlot(var7).copy());
        var6++;
    }
    mc.level.addEntity(var3);
    this.ghosts.put(var3, System.currentTimeMillis());
});
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        long var2 = System.currentTimeMillis();
        this.ghosts.entrySet().removeIf(() -> {
    if ((arg0 - ((Long) arg1.getValue()).longValue()) <= ((long) this.duration.getValue().intValue())) return false;
    despawn(((RemotePlayer) arg1.getKey()));
    return true;
});
    }

    public void onDisable() {
        this.ghosts.keySet().forEach(this::despawn);
        this.ghosts.clear();
    }

    private void despawn(RemotePlayer arg0) {
        if (mc.level != null) {
            mc.level.removeEntity(arg0.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    public boolean isGhost(Entity arg0) {
        return this.ghosts.containsKey(arg0);
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
        return withFade(this.fillColor.getColor(), arg0);
    }

    public Color getOutlineColor(RemotePlayer arg0) {
        return withFade(this.outlineColor.getColor(), arg0);
    }

    public float getYOffset(RemotePlayer arg0) {
        if (!(this.yOffset.getValue().intValue())) return 0.0f;
        Object var2 = ((Long) this.ghosts.get(arg0));
        if (var2 == null) return 0.0f;
        int var3 = this.duration.getValue().intValue();
        if (var3 <= 0) return 0.0f;
        float var4 = Easing.toDelta(var2.longValue(), var3);
        return (this.yOffset.getValue().floatValue() * var4);
    }

    private Color withFade(Color arg0, RemotePlayer arg1) {
        Object var3 = ((Long) this.ghosts.get(arg1));
        if (var3 == null) return arg0;
        float var4 = (1.0f - Easing.toDelta(var3.longValue(), this.duration.getValue().intValue()));
        return ColorUtils.getColor(arg0, ((int) (((float) arg0.getAlpha()) * var4)));
    }

    static {
        var tmp0 = new EquipmentSlot[4];
        tmp0[0] = EquipmentSlot.HEAD;
        tmp0[1] = EquipmentSlot.CHEST;
        tmp0[2] = EquipmentSlot.LEGS;
        tmp0[3] = EquipmentSlot.FEET;
        COPIED_SLOTS = tmp0;
    }

}
