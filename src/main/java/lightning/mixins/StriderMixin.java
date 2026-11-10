package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.movement.EntityControlModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Strider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Strider.class)
public abstract class StriderMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void night$onGetControllingPassenger(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((EntityControlModule) LightningClient.MODULE_MANAGER.getModule(EntityControlModule.class));
        Object var3 = tmp0;
        if (((tmp0 == null) || (!(var3.isToggled()))) || (!(var3.control.getValue()))) break label_0;
        var tmp1 = ((Strider) this).getFirstPassenger();
        Object var2 = tmp1;
        if (!((tmp1 instanceof LivingEntity))) break label_0;
        Object var4 = ((LivingEntity) var2);
        arg0.setReturnValue(var4);
        label_0:
    }

}
