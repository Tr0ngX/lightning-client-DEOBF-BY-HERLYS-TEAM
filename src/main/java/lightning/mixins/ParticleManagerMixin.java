package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.modules.impl.visuals.ParticlesModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleManagerMixin implements IMinecraft {
    @Unique
    private static int night$totemCounter;
    @Unique
    private static long night$lastTotemTime;

    @Inject(method = "createTrackingEmitter(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/particles/ParticleOptions;I)V", at = @At("HEAD"), cancellable = true)
    private void night$onCreateTrackingEmitter(Entity arg0, ParticleOptions arg1, int arg2, CallbackInfo arg3) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var5 = null;
        Object var6 = null;
        if (var5 != null && var5.isToggled() && var5.totemPop.getValue() && arg1.getType() == ParticleTypes.TOTEM_OF_UNDYING && arg0 == mc.player) {
            arg3.cancel();
        }
    }

    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void addParticle(ParticleOptions arg0, double arg1, double arg2, double arg3, double arg4, double arg5, double arg6, CallbackInfoReturnable arg7) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var16 = null;
        Object var17 = null;
        if (var16 != null && var16.isToggled() && var16.explosions.getValue() && arg0.getType() == ParticleTypes.EXPLOSION) {
            arg7.cancel();
            return;
        }
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var15 = null;
        Object var18 = null;
        if ((((var15 == null) || (!(var15.isToggled()))) || (!(var15.totem.getValue()))) || (arg0.getType() != ParticleTypes.TOTEM_OF_UNDYING)) break label_8;
        if (var15.totemMode.getValue().equalsIgnoreCase("Custom")) {
            arg7.cancel();
            break label_8;
        }
        if (!(var15.totemMode.getValue().equalsIgnoreCase("Vanilla"))) break label_8;
        long var19 = System.currentTimeMillis();
        if ((var19 - night$lastTotemTime) > 400L) {
            night$totemCounter = 0;
            night$lastTotemTime = var19;
        }
        if (night$totemCounter >= var15.totemNumber.getValue().intValue()) {
            arg7.cancel();
            return;
        }
        night$totemCounter = (night$totemCounter + 1);
        label_8:
    }

    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("RETURN"))
    private void onCreatedParticle(ParticleOptions arg0, double arg1, double arg2, double arg3, double arg4, double arg5, double arg6, CallbackInfoReturnable arg7) {
        if (arg0.getType() != ParticleTypes.TOTEM_OF_UNDYING) break label_2;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var16 = null;
        Object var17 = null;
        if ((((var16 == null) || (!(var16.isToggled()))) || (!(var16.totem.getValue()))) || (!(var16.totemMode.getValue().equalsIgnoreCase("Vanilla")))) break label_2;
        var tmp0 = ((Particle) arg7.getReturnValue());
        Object var15 = tmp0;
        if (!((tmp0 instanceof TotemParticle))) break label_2;
        Object var18 = ((TotemParticle) var15);
        Object var19 = var16.vanillaColorA.getColor();
        Object var20 = var16.vanillaColorB.getColor();
        var18.setColor(var19.getRGB());
        var18.setFadeColor(var20.getRGB());
        label_2:
    }

    static {
        night$totemCounter = 0;
        night$lastTotemTime = 0L;
    }

}
