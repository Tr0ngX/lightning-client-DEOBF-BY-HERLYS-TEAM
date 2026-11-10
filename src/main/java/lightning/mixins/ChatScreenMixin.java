package lightning.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.awt.Color;
import lightning.LightningClient;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.CommandInputEvent;
import lightning.modules.impl.core.HUDModule;
import lightning.modules.impl.core.IRCModule;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
    @Shadow
    protected EditBox input;

    protected ChatScreenMixin(Component arg0) {
        super(arg0);
    }

    @Inject(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"), cancellable = true)
    private void sendMessage(String arg0, boolean arg1, CallbackInfo arg2) {
        Object var4 = new ChatInputEvent(arg0);
        LightningClient.EVENT_HANDLER.post(var4);
        if (var4.isCancelled()) {
            arg2.cancel();
        }
    }

    @Inject(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V"), cancellable = true)
    private void sendCommand(String arg0, boolean arg1, CallbackInfo arg2) {
        Object var4 = new CommandInputEvent(arg0);
        LightningClient.EVENT_HANDLER.post(var4);
        if (var4.isCancelled()) {
            arg2.cancel();
        }
    }

    @WrapWithCondition(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private boolean render(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
        return !(((HUDModule) LightningClient.MODULE_MANAGER.getModule(HUDModule.class)).isToggled());
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void night$renderIrcOutline(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, CallbackInfo arg4) {
        if (this.input == null) return;
        Object var6 = this.input.getValue();
        Object var7 = ((IRCModule) LightningClient.MODULE_MANAGER.getModule(IRCModule.class));
        if ((var7 == null) || (!(var7.isToggled()))) {
            return;
        }
        Object var8 = var7.prefix.getValue();
        if (var8 != null) {
            if (var8.isEmpty()) {
            }
            var8 = "$";
        }
        if (var6 != null && var6.startsWith(var8)) {
            int var9 = (this.input.getX() - 2);
            int var10 = (this.input.getY() - 2);
            int var11 = ((this.input.getX() + this.input.getWidth()) + 2);
            int var12 = ((this.input.getY() + this.input.getHeight()) + 2);
            Object var13 = ColorUtils.getGlobalColor();
            Renderer2D.renderOutline(arg0, (((float) var9) - 1.0f), (((float) var10) - 1.0f), (((float) var11) + 1.0f), (((float) var12) + 1.0f), new Color(var13.getRed(), var13.getGreen(), var13.getBlue(), 90));
            Renderer2D.renderOutline(arg0, ((float) var9), ((float) var10), ((float) var11), ((float) var12), var13);
            Renderer2D.renderQuad(arg0, ((float) var9), ((float) var10), ((float) var11), ((float) var12), new Color(var13.getRed(), var13.getGreen(), var13.getBlue(), 35));
        }
    }

}
