package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.movement.EntityControlModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void night$onGetControllingPassenger(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((EntityControlModule) LightningClient.MODULE_MANAGER.getModule(EntityControlModule.class));
        Object var3 = tmp0;
        if (((tmp0 == null) || (!(var3.isToggled()))) || (!(var3.control.getValue()))) break label_0;
        var tmp1 = ((AbstractHorse) this).getFirstPassenger();
        Object var2 = tmp1;
        if (!((tmp1 instanceof LivingEntity))) break label_0;
        Object var4 = ((LivingEntity) var2);
        arg0.setReturnValue(var4);
        label_0:
    }

    @Inject(method = "isTamed", at = @At("HEAD"), cancellable = true)
    private void night$onIsTamed(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((EntityControlModule) LightningClient.MODULE_MANAGER.getModule(EntityControlModule.class));
        Object var2 = tmp0;
        if (((tmp0 == null) || (!(var2.isToggled()))) || (!(var2.control.getValue()))) break label_0;
        arg0.setReturnValue(true);
        label_0:
    }

}
