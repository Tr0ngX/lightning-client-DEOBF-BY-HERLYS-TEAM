package lightning.mixins;

import java.awt.Color;
import lightning.LightningClient;
import lightning.modules.impl.core.MenuModule;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class DownloadingTerrainScreenMixin extends Screen {
    @Shadow
    @Final
    private LevelLoadingScreen.Reason reason;

    protected DownloadingTerrainScreenMixin(Component arg0) {
        super(arg0);
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void renderBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        if (this.reason.equals(LevelLoadingScreen.Reason.OTHER) && ((MenuModule) LightningClient.MODULE_MANAGER.getModule(MenuModule.class)).isToggled() && ((MenuModule) LightningClient.MODULE_MANAGER.getModule(MenuModule.class)).mainMenu.getValue()) {
            Renderer2D.renderQuad(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(25, 25, 25, 255));
            int var6 = 0;
            while (var6 < this.width) {
                Object var7 = ColorUtils.getRainbow(2L, 0.7f, 1.0f, 255, (((long) var6) * 5L));
                Renderer2D.renderQuad(arg0, ((float) var6), 0.0f, ((float) (var6 + 1)), 1.0f, var7);
                var6++;
            }
            arg4.cancel();
        }
    }

}
