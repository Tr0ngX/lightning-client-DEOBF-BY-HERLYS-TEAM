package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void night$hideFromRaytrace(CallbackInfoReturnable arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.armorStand.getValue()) {
            arg0.setReturnValue(false);
        }
    }

}
