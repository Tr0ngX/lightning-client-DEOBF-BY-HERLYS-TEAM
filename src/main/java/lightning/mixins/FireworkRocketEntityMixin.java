package lightning.mixins;

import lightning.LightningClient;
import lightning.events.impl.RemoveFireworkEvent;
import lightning.utils.IMinecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin implements IMinecraft {
    @Shadow
    private int life;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;updateRotation()V", shift = At.Shift.AFTER), cancellable = true)
    private void tick(CallbackInfo arg0) {
        Object var2 = ((FireworkRocketEntity) this);
        Object var3 = new RemoveFireworkEvent(var2);
        LightningClient.EVENT_HANDLER.post(var3);
        if (!(var3.isCancelled())) break label_1;
        arg0.cancel();
        if (!this.life && !(var2.isSilent())) {
            mc.level.playSound(null, var2.getX(), var2.getY(), var2.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
        }
        this.life = (this.life + 1);
        if ((this.life % 2) >= 2) break label_1;
        mc.level.addParticle(ParticleTypes.FIREWORK, var2.getX(), var2.getY(), var2.getZ(), (mc.level.getRandom().nextGaussian() * 0.05), (-(var2.getDeltaMovement().y) * 0.5), (mc.level.getRandom().nextGaussian() * 0.05));
        label_1:
    }

}
