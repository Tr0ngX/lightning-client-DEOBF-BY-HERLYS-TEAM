package lightning.modules.impl.combat;

import java.awt.Color;
import java.util.Comparator;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "SpearKill", description = "Lunges at players and attacks with a spear/trident.", category = Module.Category.COMBAT)
public class SpearKillModule extends Module {
    public NumberSetting range;
    public NumberSetting wallRange;
    public NumberSetting hitRange;
    public NumberSetting delay;
    public BooleanSetting attackDelay;
    public NumberSetting attackSpeed;
    public ModeSetting autoSwap;
    public BooleanSetting rotate;
    public BooleanSetting lunge;
    public NumberSetting lungeSpeed;
    public NumberSetting lungeY;
    public NumberSetting stopDistance;
    public NumberSetting lungeDelay;
    public BooleanSetting swing;
    public BooleanSetting render;
    public BooleanSetting renderLine;
    public ColorSetting fillColor;
    public ColorSetting lineColor;
    private long lastAttackMs;
    private long lastLungeMs;
    private Player target;

    public SpearKillModule() {
        this.range = new NumberSetting("Range", "Range to search for players.", 12.0f, 1.0f, 100.0f);
        this.wallRange = new NumberSetting("WallRange", "Range to search through walls.", 4.0f, 0.0f, 100.0f);
        this.hitRange = new NumberSetting("HitRange", "Range to send spear attacks.", 4.2f, 1.0f, 100.0f);
        this.delay = new NumberSetting("Delay", "Minimum delay in milliseconds between attacks.", 100, 0, 1000);
        this.attackDelay = new BooleanSetting("AttackDelay", "Uses vanilla attack cooldown before attacking.", true);
        this.attackSpeed = new NumberSetting("AttackSpeed", "Attacks per second when AttackDelay is off.", new BooleanSetting.Visibility(this.attackDelay, false), 8.0f, 1.0f, 20.0f);
        var tmp0 = new String[3];
        tmp0[0] = "Off";
        tmp0[1] = "Normal";
        tmp0[2] = "Silent";
        this.autoSwap = new ModeSetting("AutoSwap", "Swaps to a spear before attacking.", "Normal", tmp0);
        this.rotate = new BooleanSetting("Rotate", "Rotates to the target before lunging and attacking.", true);
        this.lunge = new BooleanSetting("Lunge", "Pushes you toward the target.", true);
        this.lungeSpeed = new NumberSetting("LungeSpeed", "Horizontal lunge speed.", new BooleanSetting.Visibility(this.lunge, true), 1.25f, 0.1f, 5.0f);
        this.lungeY = new NumberSetting("LungeY", "Upward velocity added while lunging.", new BooleanSetting.Visibility(this.lunge, true), 0.08f, 0.0f, 1.0f);
        this.stopDistance = new NumberSetting("StopDistance", "Stops lunging inside this distance.", new BooleanSetting.Visibility(this.lunge, true), 2.4f, 0.0f, 6.0f);
        this.lungeDelay = new NumberSetting("LungeDelay", "Delay in milliseconds between lunges.", new BooleanSetting.Visibility(this.lunge, true), 150, 0, 1000);
        this.swing = new BooleanSetting("Swing", "Swings the hand after attacking.", true);
        this.render = new BooleanSetting("Render", "Renders the current SpearKill target.", true);
        this.renderLine = new BooleanSetting("RenderLine", "Renders a line to the current target.", new BooleanSetting.Visibility(this.render, true), true);
        this.fillColor = new ColorSetting("FillColor", "Fill color for the target render.", new BooleanSetting.Visibility(this.render, true), new ColorSetting.Color(new Color(255, 210, 90, 45), false, false));
        this.lineColor = new ColorSetting("LineColor", "Outline and line color for the target render.", new BooleanSetting.Visibility(this.render, true), new ColorSetting.Color(new Color(255, 210, 90, 180), false, false));
        this.lastAttackMs = 0L;
        this.lastLungeMs = 0L;
    }

    public String getMetaData() {
        if (this.target != null) {
        } else {
        }
        return "Spear";
    }

    public void onDisable() {
        this.target = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.player.isSpectator())) {
            return;
        }
        this.target = findTarget();
        if (this.target == null) return;
        int var2 = getSpearSlot();
        if (var2 == -1 && !(isHoldingSpear())) return;
        int var3 = 0;
        int var4 = mc.player.getInventory().getSelectedSlot();
        if (isHoldingSpear()) break label_6;
        if (this.autoSwap.getValue().equalsIgnoreCase("Off")) return;
        if (this.autoSwap.getValue().equalsIgnoreCase("Normal")) {
            InventoryUtils.switchSlot("Normal", var2, var4);
            break label_6;
        }
        if (!(this.autoSwap.getValue().equalsIgnoreCase("Silent"))) break label_6;
        InventoryUtils.switchSlot("Silent", var2, var4);
        var3 = 1;
        label_6:
        if (this.rotate.getValue()) {
            Object var5 = RotationUtils.getRotations(this.target.getX(), (this.target.getY() + (((double) this.target.getEyeHeight()) / 2.0)), this.target.getZ());
            LightningClient.ROTATION_MANAGER.legacyRotate(var5[0], var5[1], this, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        var5 = ((double) mc.player.distanceTo(this.target));
        if (this.lunge.getValue() && var5 > this.stopDistance.getValue().doubleValue() && (System.currentTimeMillis() - this.lastLungeMs) >= this.lungeDelay.getValue().longValue()) {
            doLunge(this.target);
            this.lastLungeMs = System.currentTimeMillis();
        }
        if (var5 <= this.hitRange.getValue().doubleValue() && canAttack()) {
            doAttack(this.target);
            this.lastAttackMs = System.currentTimeMillis();
        }
        if (!(!var3)) {
            InventoryUtils.switchBack("Silent", var2, var4);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((((!(this.render.getValue())) || (this.target == null)) || (!(this.target.isAlive()))) || (this.target.isRemoved())) {
            return;
        }
        Object var2 = arg0.getMatrices();
        Object var3 = EntityUtils.getRenderPos(this.target, arg0.getTickDelta());
        Object var4 = this.target.getBoundingBox().move((var3.x - this.target.getX()), (var3.y - this.target.getY()), (var3.z - this.target.getZ()));
        Renderer3D.renderBox(var2, var4, this.fillColor.getColor());
        Renderer3D.renderBoxOutline(var2, var4, this.lineColor.getColor());
        if (this.renderLine.getValue()) {
            Object var5 = EntityUtils.getRenderPos(mc.player, arg0.getTickDelta()).add(0.0, ((double) mc.player.getEyeHeight(mc.player.getPose())), 0.0);
            Renderer3D.renderLine(var2, var5, var4.getCenter(), this.lineColor.getColor());
        }
    }

    private Player findTarget() {
        return ((Player) mc.level.players().stream().filter(this::isValidTarget).min(Comparator.comparingDouble((arg0) -> mc.player.distanceToSqr(arg0))).orElse(null));
    }

    private boolean isValidTarget(Player arg0) {
        if ((((arg0 == mc.player) || (!(arg0.isAlive()))) || (arg0.isRemoved())) || (arg0.isSpectator())) {
            return false;
        }
        if (EntityUtils.isGhost(arg0)) return false;
        if (LightningClient.FRIEND_MANAGER.contains(arg0.getName().getString())) return false;
        double var2 = ((double) mc.player.distanceTo(arg0));
        if (var2 > this.range.getValue().doubleValue()) return false;
        if (!(mc.player.hasLineOfSight(arg0))) {
            if (var2 <= this.wallRange.getValue().doubleValue()) {
            }
        } else {
        }
        return false;
    }

    private boolean canAttack() {
        if ((System.currentTimeMillis() - this.lastAttackMs) < this.delay.getValue().longValue()) return false;
        if (this.attackDelay.getValue()) return mc.player.getAttackStrengthScale(0.5f) >= 1.0f;
        return (System.currentTimeMillis() - this.lastAttackMs) >= ((long) (1000.0f / Math.max(this.attackSpeed.getValue().floatValue(), 0.1f)));
    }

    private void doLunge(Player arg0) {
        Object var2 = arg0.position().subtract(mc.player.position());
        Object var3 = new Vec3(var2.x, 0.0, var2.z);
        if (var3.length() < 1.0E-4) return;
        Object var4 = var3.normalize();
        double var5 = Math.max(mc.player.getDeltaMovement().y, this.lungeY.getValue().doubleValue());
        mc.player.setDeltaMovement((var4.x * this.lungeSpeed.getValue().doubleValue()), var5, (var4.z * this.lungeSpeed.getValue().doubleValue()));
    }

    private void doAttack(Player arg0) {
        mc.gameMode.attack(mc.player, arg0);
        mc.player.resetAttackStrengthTicker();
        if (this.swing.getValue()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        } else {
            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
    }

    private boolean isHoldingSpear() {
        if (mc.player == null) return false;
        Object var1 = mc.player.getMainHandItem();
        if (!(var1.is(Items.TRIDENT))) {
            if ((var1.getItem() instanceof TridentItem)) {
            }
        } else {
        }
        return false;
    }

    private int getSpearSlot() {
        if (mc.player == null) return -1;
        int var1 = 0;
        while (var1 < 9) {
            Object var2 = mc.player.getInventory().getItem(var1);
            if (!(var2.is(Items.TRIDENT)) && !(var2.getItem() instanceof TridentItem)) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

}
