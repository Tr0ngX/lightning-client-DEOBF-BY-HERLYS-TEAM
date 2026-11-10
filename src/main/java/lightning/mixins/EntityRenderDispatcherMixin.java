package lightning.mixins;

import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow
    private Map renderers;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void night$safeShouldRender(Entity arg0, Frustum arg1, double arg2, double arg3, double arg4, CallbackInfoReturnable arg5) {
        if (arg0 != null) {
            if (arg0.getType() == null) {
            }
            arg5.setReturnValue(false);
            return;
        }
        if ((arg0 instanceof AbstractClientPlayer)) break label_3;
        if (this.renderers != null) {
            if (this.renderers.containsKey(arg0.getType())) break label_3;
        }
        arg5.setReturnValue(false);
        label_3:
    }

}
