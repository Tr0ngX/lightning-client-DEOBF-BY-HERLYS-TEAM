package lightning.mixins;

import java.awt.Color;
import lightning.LightningClient;
import lightning.gui.components.AccountButton;
import lightning.utils.IMinecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen implements IMinecraft {
    @Shadow
    private boolean fading;
    @Shadow
    private long fadeInStart;

    protected TitleScreenMixin(Component arg0) {
        super(arg0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo arg0) {
        addRenderableWidget(AccountButton.create(8, 8, 75, 20, this));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void render$drawTextWithShadow(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        if (LightningClient.UPDATE_STATUS.equalsIgnoreCase("none")) return;
        Object var8 = "";
        Object var9 = "";
        Object var10 = Color.WHITE;
        if (LightningClient.UPDATE_STATUS.equalsIgnoreCase("update-available")) {
            var9 = "An update is available for Lightning.";
            var8 = "Please restart the game to apply changes.";
            var10 = Color.ORANGE;
        }
        if (LightningClient.UPDATE_STATUS.equalsIgnoreCase("failed-connection")) {
            var9 = "Failed to connect to Lightning's servers.";
            var8 = "Please make sure you have a working internet connection.";
            var10 = Color.RED;
        }
        if (LightningClient.UPDATE_STATUS.equalsIgnoreCase("failed")) {
            var9 = "Failed to update Lightning.";
            var8 = "Please make sure the auto-updater is working properly.";
            var10 = Color.RED;
        }
        if (LightningClient.UPDATE_STATUS.equalsIgnoreCase("up-to-date")) {
            var8 = "Lightning is on the latest version.";
        }
        if (var8.isEmpty()) return;
        float var11 = 1.0f;
        if (!this.fading) break label_6;
        var tmp0 = (((float) (Util.getMillis() - this.fadeInStart)) / 2000.0f);
        float var7 = tmp0;
        if (tmp0 > 1.0f) break label_6;
        var7 = Mth.clamp(var7, 0.0f, 1.0f);
        var11 = Mth.clampedMap(var7, 0.5f, 1.0f, 0.0f, 1.0f);
        label_6:
        var tmp1 = (Mth.ceil((var11 * 255.0f)) << 24);
        int var6 = tmp1;
        if ((tmp1 & -67108864) != 0) {
            if (!(var9.isEmpty())) {
                arg0.text(mc.font, var9, ((this.width / 2) - (mc.font.width(var9) / 2)), (this.height - 30), (var10.getRGB() | var6), true);
            }
            arg0.text(mc.font, var8, ((this.width / 2) - (mc.font.width(var8) / 2)), (this.height - 20), (var10.getRGB() | var6), true);
        }
    }

}
