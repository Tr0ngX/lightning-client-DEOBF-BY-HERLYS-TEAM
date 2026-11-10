package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.system.Timer;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "HitboxDesync", description = "Precisely offsets your position to glitch out Minecraft hitbox calculations.", category = Module.Category.MOVEMENT)
public class HitboxDesyncModule extends Module {
    public BooleanSetting alternating;
    public BooleanSetting minimal;
    public BooleanSetting specific;
    public BooleanSetting close;
    public BooleanSetting selfDisable;
    public BooleanSetting jumpDisable;
    private final Timer timer;
    private double prevY;

    public HitboxDesyncModule() {
        this.alternating = new BooleanSetting("Alternating", "Modifies your position in such a way that it messes with other clients.", false);
        this.minimal = new BooleanSetting("Minimal", "Makes alternating minimal, only required on certain servers.", new BooleanSetting.Visibility(this.alternating, true), false);
        this.specific = new BooleanSetting("Specific", "Specific alternating mode, only required against certain clients.", new BooleanSetting.Visibility(this.alternating, true), false);
        this.close = new BooleanSetting("Close", "Whether or not to have the surround place blocks on desynced positions.", false);
        this.selfDisable = new BooleanSetting("SelfDisable", "Toggles off the module after having desynced your hitbox.", new BooleanSetting.Visibility(this.alternating, false), true);
        this.jumpDisable = new BooleanSetting("JumpDisable", "Toggles off the module whenever your Y level changes.", new BooleanSetting.Visibility(this.alternating, true), true);
        this.timer = new Timer();
    }

    @SubscribeEvent(priority = 2147483647)
    public void onTick(TickEvent arg0) {
        if (!(getNull())) {
            if (this.jumpDisable.getValue() && mc.player.getY() != this.prevY) {
            }
            setToggled(false);
            return;
        }
        Object var2 = Vec3.atCenterOf(mc.player.blockPosition());
        double var3 = (this.minimal.getValue() ? 0.001 : 0.002);
        double var5 = (this.specific.getValue() ? 500.0 : 1500.0);
        if ((!(this.timer.hasTimeElapsed(var5))) || (!(this.alternating.getValue()))) break label_6;
        int var7 = (!(mc.player.isShiftKeyDown()) ? 1 : 0);
        int var8 = ((var2.x - mc.player.getX()) > 0.0 ? 1 : 0);
        int var9 = ((var2.z - mc.player.getZ()) > 0.0 ? 1 : 0);
        double var10 = ((var2.x + ((!(!var7) ? var3 : 0.0) * ((double) (!(!var8) ? 1 : -1)))) + (0.20000000009497754 * ((double) (!(!var8) ? -1 : 1))));
        double var12 = ((var2.z + ((!(!var7) ? var3 : 0.0) * ((double) (!(!var9) ? 1 : -1)))) + (0.2000000000949811 * ((double) (!(!var9) ? -1 : 1))));
        mc.player.setPos(var10, mc.player.getY(), var12);
        if (this.timer.hasTimeElapsed(var5)) {
            this.timer.reset();
        }
        if (this.selfDisable.getValue() && !(this.alternating.getValue())) {
            setToggled(false);
        }
    }

    public void onEnable() {
        if (mc.player != null) {
            if (mc.level == null) {
            }
            setToggled(false);
            return;
        }
        this.prevY = mc.player.getY();
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

}
