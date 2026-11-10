package lightning.mixins;

import java.util.List;
import java.util.Map;
import lightning.utils.graphics.NightPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {
    @Shadow
    @Final
    private Map namespacedManagers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void night$injectClientPack(PackType arg0, List arg1, CallbackInfo arg2) {
        if (arg0 == PackType.CLIENT_RESOURCES) {
            Object var4 = ((FallbackResourceManager) this.namespacedManagers.computeIfAbsent("lightning", (arg1) -> new FallbackResourceManager(arg0, "lightning")));
            var4.push(NightPackResources.INSTANCE);
        }
    }

}
