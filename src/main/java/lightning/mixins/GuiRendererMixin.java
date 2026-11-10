package lightning.mixins;

import lightning.managers.RenderManager;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @Inject(method = "draw", at = @At("RETURN"))
    private void night$postGuiRender(CallbackInfo arg0) {
        Object var2 = RenderManager.POST_GUI_RENDERERS.iterator();
        while (var2.hasNext()) {
            Object var3 = ((Runnable) var2.next());
            var3.run();
        }
    }

}
