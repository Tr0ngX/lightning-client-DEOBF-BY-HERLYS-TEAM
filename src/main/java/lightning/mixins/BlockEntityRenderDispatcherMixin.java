package lightning.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin implements IMinecraft {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void night$submit(BlockEntityRenderState arg0, PoseStack arg1, SubmitNodeCollector arg2, CameraRenderState arg3, CallbackInfo arg4) {
        Object var6 = ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class));
        if ((!(var6.isToggled())) || (var6.tileEntities.getValue().equals("Never"))) break label_1;
        if (!(var6.tileEntities.getValue().equals("Always"))) {
            if ((!(var6.tileEntities.getValue().equals("Distance"))) || (Math.sqrt(mc.player.distanceToSqr(((double) arg0.blockPos.getX()), ((double) arg0.blockPos.getY()), ((double) arg0.blockPos.getZ()))) <= ((double) var6.tileDistance.getValue().floatValue()))) break label_1;
        }
        arg4.cancel();
        label_1:
    }

}
