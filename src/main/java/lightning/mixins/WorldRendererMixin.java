package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Set;
import lightning.LightningClient;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.modules.impl.visuals.BlockHighlightModule;
import lightning.modules.impl.visuals.ShadersModule;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private RenderTarget entityOutlineTarget;

    @ModifyReturnValue(method = "entityOutlineTarget", at = @At("RETURN"))
    private RenderTarget night$outlineTargetOutsideFrameGraph(RenderTarget arg0) {
        return (arg0 != null ? arg0 : this.entityOutlineTarget);
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean renderOutline(boolean arg0) {
        if (LightningClient.MODULE_MANAGER != null && ((BlockHighlightModule) LightningClient.MODULE_MANAGER.getModule(BlockHighlightModule.class)).isToggled()) return false;
        return arg0;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private boolean night$forceShouldRenderSky(boolean arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if (var2 != null && var2.isToggled() && var2.modifySky.getValue()) return true;
        return arg0;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain night$interceptEntityOutlineChain(ShaderManager arg0, Identifier arg1, Set arg2) {
        if (arg1.equals(Identifier.withDefaultNamespace("entity_outline")) && ShadersModule.pickActiveOutlineChain() != null) return null;
        return arg0.getPostChain(arg1, arg2);
    }

}
