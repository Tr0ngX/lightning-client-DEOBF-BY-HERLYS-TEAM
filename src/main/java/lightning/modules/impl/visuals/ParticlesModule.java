package lightning.modules.impl.visuals;

import java.awt.Color;
import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.ParticleManager;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Particles", description = "Custom particle effects for totem pops and entity hits.", category = Module.Category.VISUALS)
public class ParticlesModule extends Module {
    private final ParticleManager particleManager;
    public CategorySetting totemCategory;
    public BooleanSetting totem;
    public ModeSetting totemMode;
    public NumberSetting totemNumber;
    public ColorSetting vanillaColorA;
    public ColorSetting vanillaColorB;
    public ColorSetting customColorA;
    public ColorSetting customColorB;
    public BooleanSetting totemGlow;
    public BooleanSetting totemPhysics;
    public NumberSetting totemVelocityX;
    public NumberSetting totemVelocityY;
    public NumberSetting totemVelocityZ;
    public CategorySetting hitCategory;
    public BooleanSetting hit;
    public NumberSetting hitNumber;
    public ColorSetting hitColorA;
    public ColorSetting hitColorB;
    public BooleanSetting hitGlow;
    public BooleanSetting hitPhysics;

    public ParticlesModule() {
        this.particleManager = new ParticleManager();
        this.totemCategory = new CategorySetting("Totem", "Settings for totem pop particle effects.");
        this.totem = new BooleanSetting("Totem", "Enabled", "Enables totem particle customization.", new CategorySetting.Visibility(this.totemCategory), true);
        var tmp0 = new String[2];
        tmp0[0] = "Vanilla";
        tmp0[1] = "Custom";
        this.totemMode = new ModeSetting("TotemMode", "Mode", "Particle mode for totem pops.", new CategorySetting.Visibility(this.totemCategory), "Vanilla", tmp0);
        this.totemNumber = new NumberSetting("TotemNumber", "Number", "Amount of particles per totem pop.", new CategorySetting.Visibility(this.totemCategory), 15, 1, 100);
        var tmp1 = new String[1];
        tmp1[0] = "Vanilla";
        this.vanillaColorA = new ColorSetting("VanillaColorA", "Color A", "Primary particle color for vanilla totem pop.", new ModeSetting.Visibility(this.totemMode, tmp1), new ColorSetting.Color(new Color(255, 255, 255), false, false));
        var tmp2 = new String[1];
        tmp2[0] = "Vanilla";
        this.vanillaColorB = new ColorSetting("VanillaColorB", "Color B", "Fade particle color for vanilla totem pop.", new ModeSetting.Visibility(this.totemMode, tmp2), new ColorSetting.Color(new Color(255, 255, 255), false, false));
        var tmp3 = new String[1];
        tmp3[0] = "Custom";
        this.customColorA = new ColorSetting("CustomColorA", "Color A", "Primary particle color.", new ModeSetting.Visibility(this.totemMode, tmp3), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
        var tmp4 = new String[1];
        tmp4[0] = "Custom";
        this.customColorB = new ColorSetting("CustomColorB", "Color B", "Secondary particle color.", new ModeSetting.Visibility(this.totemMode, tmp4), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
        var tmp5 = new String[1];
        tmp5[0] = "Custom";
        this.totemGlow = new BooleanSetting("TotemGlow", "Glow", "Adds a glow effect to totem particles.", new ModeSetting.Visibility(this.totemMode, tmp5), true);
        var tmp6 = new String[1];
        tmp6[0] = "Custom";
        this.totemPhysics = new BooleanSetting("TotemPhysics", "Physics", "Enables gravity and bouncing for totem particles.", new ModeSetting.Visibility(this.totemMode, tmp6), true);
        var tmp7 = new String[1];
        tmp7[0] = "Custom";
        this.totemVelocityX = new NumberSetting("TotemVelocityX", "X", "X velocity multiplier.", new ModeSetting.Visibility(this.totemMode, tmp7), 1.0, -5.0, 5.0);
        var tmp8 = new String[1];
        tmp8[0] = "Custom";
        this.totemVelocityY = new NumberSetting("TotemVelocityY", "Y", "Y velocity multiplier.", new ModeSetting.Visibility(this.totemMode, tmp8), 1.0, -5.0, 5.0);
        var tmp9 = new String[1];
        tmp9[0] = "Custom";
        this.totemVelocityZ = new NumberSetting("TotemVelocityZ", "Z", "Z velocity multiplier.", new ModeSetting.Visibility(this.totemMode, tmp9), 1.0, -5.0, 5.0);
        this.hitCategory = new CategorySetting("Hit", "Settings for hit particle effects.");
        this.hit = new BooleanSetting("Hit", "Enabled", "Enables hit particles.", new CategorySetting.Visibility(this.hitCategory), true);
        this.hitNumber = new NumberSetting("HitNumber", "Number", "Amount of particles per hit.", new CategorySetting.Visibility(this.hitCategory), 15, 1, 100);
        this.hitColorA = new ColorSetting("HitColorA", "Color A", "Primary particle color.", new CategorySetting.Visibility(this.hitCategory), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
        this.hitColorB = new ColorSetting("HitColorB", "Color B", "Secondary particle color.", new CategorySetting.Visibility(this.hitCategory), new ColorSetting.Color(new Color(255, 255, 255, 255), false, false));
        this.hitGlow = new BooleanSetting("HitGlow", "Glow", "Adds a glow effect to hit particles.", new CategorySetting.Visibility(this.hitCategory), true);
        this.hitPhysics = new BooleanSetting("HitPhysics", "Physics", "Enables gravity and bouncing for hit particles.", new CategorySetting.Visibility(this.hitCategory), false);
    }

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent arg0) {
        if ((arg0.getPlayer() == null) || (!(this.totem.getValue()))) {
            return;
        }
        if (!(this.totemMode.getValue().equalsIgnoreCase("Custom"))) return;
        Object var2 = arg0.getPlayer().getBoundingBox().getCenter();
        Object var3 = new Vec3(this.totemVelocityX.getValue().doubleValue(), this.totemVelocityY.getValue().doubleValue(), this.totemVelocityZ.getValue().doubleValue());
        spawnTotemParticles(var2, var3);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent arg0) {
        if ((arg0.getTarget() == null) || (!(this.hit.getValue()))) {
            return;
        }
        Object var2 = arg0.getTarget().getBoundingBox().getCenter();
        spawnHitParticles(var2);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.level != null) {
            if (mc.player == null) {
            }
            this.particleManager.clear();
            return;
        }
        this.particleManager.tick();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (this.particleManager.isEmpty()) return;
        this.particleManager.render(arg0.getMatrices(), arg0.getTickDelta());
    }

    private void spawnTotemParticles(Vec3 arg0, Vec3 arg1) {
        int var3 = this.customColorA.getColor().getRGB();
        int var4 = this.customColorB.getColor().getRGB();
        int var5 = this.totemGlow.getValue();
        int var6 = this.totemPhysics.getValue();
        int var7 = this.totemNumber.getValue().intValue();
        int var8 = 0;
        while (var8 < var7) {
            Object var9 = ParticleManager.randomMotion((0.5 * (0.75 + (Math.random() * 0.25))));
            var9 = new Vec3((var9.x * arg1.x), (var9.y * arg1.y), (var9.z * arg1.z));
            if (!(!var6)) {
                this.particleManager.addBouncy(arg0, var9, 1.5, var3, var4, var5);
            } else {
                this.particleManager.addFriction(arg0, var9, 0.9, 1.5, var3, var4, var5);
            }
            var8++;
        }
    }

    private void spawnHitParticles(Vec3 arg0) {
        int var2 = this.hitColorA.getColor().getRGB();
        int var3 = this.hitColorB.getColor().getRGB();
        int var4 = this.hitGlow.getValue();
        int var5 = this.hitPhysics.getValue();
        int var6 = this.hitNumber.getValue().intValue();
        int var7 = 0;
        while (var7 < var6) {
            Object var8 = ParticleManager.randomMotion((0.5 * (0.75 + (Math.random() * 0.25))));
            if (!(!var5)) {
                this.particleManager.addBouncy(arg0, var8, 1.0, var2, var3, var4);
            } else {
                this.particleManager.addFriction(arg0, var8, 0.9, 1.0, var2, var3, var4);
            }
            var7++;
        }
    }

    @Generated
    public ParticleManager getParticleManager() {
        return this.particleManager;
    }

}
