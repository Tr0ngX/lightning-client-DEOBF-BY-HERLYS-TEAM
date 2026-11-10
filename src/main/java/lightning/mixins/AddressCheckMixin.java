package lightning.mixins;

import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AddressCheck.class)
public interface AddressCheckMixin {
    @Inject(method = "createFromService", at = @At("HEAD"), cancellable = true)
    private static void onCreateFromService(CallbackInfoReturnable arg0) {
        arg0.setReturnValue(new AddressCheckMixin.1());
    }


    class 1 implements AddressCheck {
        public boolean isAllowed(ResolvedServerAddress arg0) {
            return true;
        }

        public boolean isAllowed(ServerAddress arg0) {
            return true;
        }

    }
}
