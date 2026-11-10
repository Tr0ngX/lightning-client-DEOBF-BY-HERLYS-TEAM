package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public class BossBarHudMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void render(GuiGraphicsExtractor arg0, CallbackInfo arg1) {
        if (((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).isToggled() && ((NoRenderModule) LightningClient.MODULE_MANAGER.getModule(NoRenderModule.class)).bossBar.getValue()) {
            arg1.cancel();
        }
    }

}
