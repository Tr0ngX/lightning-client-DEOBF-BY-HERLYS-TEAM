package lightning.mixins;

import java.awt.Color;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import lightning.LightningClient;
import lightning.managers.ChatManager;
import lightning.modules.impl.core.CommandsModule;
import lightning.modules.impl.miscellaneous.BetterChatModule;
import lightning.utils.text.FormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Shadow
    @Final
    private List trimmedMessages;

    @Inject(method = "addMessageToDisplayQueue", at = @At("TAIL"))
    private void night$trackAnimation(GuiMessage arg0, CallbackInfo arg1) {
        Object var3 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (var3.isToggled() && var3.animation.getValue() && !(this.trimmedMessages.isEmpty())) {
            var3.getAnimationMap().put(((GuiMessage.Line) this.trimmedMessages.getFirst()), System.currentTimeMillis());
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At("HEAD"), cancellable = true)
    private void night$preventChatOverGui(GuiGraphicsExtractor arg0, Font arg1, int arg2, int arg3, int arg4, ChatComponent.DisplayMode arg5, boolean arg6, CallbackInfo arg7) {
        Object var9 = Minecraft.getInstance();
        if (var9.gui != null && var9.gui.screen() != null && !(var9.gui.screen() instanceof ChatScreen) && !ChatManager.isInsideHudExtractChat) {
            arg7.cancel();
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At("TAIL"))
    private void night$cleanupAnimations(CallbackInfo arg0) {
        Object var2 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (var2.isToggled() && var2.animation.getValue()) {
            var2.getAnimationMap().entrySet().removeIf((arg1) -> (System.currentTimeMillis() - ((Long) arg1.getValue()).longValue()) > ((long) arg0.delay.getValue().intValue()));
        }
    }

    @Redirect(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;updatePose(Ljava/util/function/Consumer;)V"))
    private void night$applyOffset(ChatComponent.ChatGraphicsAccess arg0, Consumer arg1) {
        Object var3 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (var3.isToggled()) {
        } else {
        }
        int var4 = 0;
        arg0.updatePose((arg2) -> {
    arg0.accept(arg2);
    if (arg1 != 0) {
        arg2.translate(((float) arg1), 0.0f);
    }
});
    }

    @Redirect(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object night$textOpacity(OptionInstance arg0) {
        Object var2 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (var2.isToggled()) return (var2.textAlpha.getValue().doubleValue() / 255.0);
        return arg0.get();
    }

    @Redirect(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private Object night$backgroundOpacity(OptionInstance arg0) {
        Object var2 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (var2.isToggled()) {
            Object var3 = var2.background.getValue();
            int var4 = -1;
            switch (var3.hashCode()) {
                case 65193517 -> { break label_0; }
                case 2029746065 -> { break label_1; }
                default -> { break label_2; }
            }
            label_0:
            if (!(var3.equals("Clear"))) break label_2;
            var4 = 0;
            break label_2;
            label_1:
            if (!(var3.equals("Custom"))) break label_2;
            var4 = 1;
            label_2:
            switch (var4) {
                case 0 -> { break label_3; }
                case 1 -> { break label_4; }
                default -> { break label_5; }
            }
            label_3:
            break label_6;
            label_4:
            break label_6;
            label_5:
            label_6:
            return arg0.get();
        }
        return arg0.get();
    }

    @Redirect(method = "lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;black(F)I"))
    private static int night$backgroundColor(float arg0) {
        Object var1 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (var1.isToggled() && var1.background.getValue().equalsIgnoreCase("Custom")) return ARGB.color(Math.round((arg0 * 255.0f)), (var1.color.getColor().getRGB() & 16777215));
        return ARGB.black(arg0);
    }

    @ModifyArgs(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V"))
    private void addMessage(Args arg0, Component arg1, MessageSignature arg2, GuiMessageSource arg3, GuiMessageTag arg4) {
        Object var9 = ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class));
        if (!(var9.isToggled())) return;
        Object var10 = arg1;
        Object var11 = Minecraft.getInstance();
        if ((!(var9.highlight.getValue())) || (var11.player == null)) break label_2;
        var tmp0 = var11.player.getGameProfile().name();
        Object var8 = tmp0;
        if ((tmp0 == null) || (var8.isEmpty())) break label_2;
        var tmp1 = arg1.getString();
        Object var6 = tmp1;
        var tmp2 = night$isSender(tmp1, var8);
        int var7 = tmp2;
        if ((tmp2) || (!(night$containsWord(var6.toLowerCase(), var8.toLowerCase())))) break label_2;
        if (var9.highlightSound.getValue()) {
            var11.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
        var10 = night$highlightMessage(var10, var8, var9.highlightColor.getColor());
        label_2:
        if (var9.timestamps.getValue()) {
            var10 = Component.literal("").append(Component.literal((String.valueOf(FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + var9.opening.getValue() + String.valueOf(FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).primaryWatermarkColor.getValue())) + new SimpleDateFormat("HH:mm").format(new Date()) + String.valueOf(FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + var9.closing.getValue() + String.valueOf(ChatFormatting.RESET) + " "))).append(var10);
        }
        arg0.set(1, var10);
    }

    private static boolean night$isSender(String arg0, String arg1) {
        if (((arg0 == null) || (arg1 == null)) || (arg1.isEmpty())) {
            return false;
        }
        Object var4 = arg0.toLowerCase();
        Object var5 = arg1.toLowerCase();
        if ((var4.startsWith("you whisper to")) || (var4.startsWith("to "))) {
            return true;
        }
        int var6 = -1;
        if (!(arg0.startsWith("<"))) break label_4;
        var tmp0 = arg0.indexOf(62);
        int var3 = tmp0;
        if (tmp0 == -1) break label_4;
        var6 = var3;
        label_4:
        if (var6 != -1) break label_8;
        var tmp1 = new String[5];
        tmp1[0] = ": ";
        tmp1[1] = " \u00bb ";
        tmp1[2] = " > ";
        tmp1[3] = " -> ";
        tmp1[4] = ":";
        Object var7 = tmp1;
        Object var8 = tmp1;
        int var9 = var8.length;
        int var10 = 0;
        while (var10 < var9) {
            Object var11 = var8[var10];
            int var12 = arg0.indexOf(var11);
            if (var12 == -1) break label_7;
            if (var6 != -1 && var12 >= var6) {
                break label_7;
            }
            var6 = var12;
            label_7:
            var10++;
        }
        label_8:
        if (var6 == -1) break label_9;
        var tmp2 = arg0.substring(0, var6);
        Object var2 = tmp2;
        return night$containsWord(tmp2.toLowerCase(), var5);
    }

    private static boolean night$containsWord(String arg0, String arg1) {
        if ((arg0 == null) || (arg1 == null)) {
            return false;
        }
        int var2 = 0;
        label_2:
        var tmp0 = arg0.indexOf(arg1, var2);
        var2 = tmp0;
        if (tmp0 != -1) {
            if (!(!var2)) {
                if (!(night$isWordChar(arg0.charAt((var2 - 1))))) {
                }
            } else {
            }
            int var4 = 0;
            if ((var2 + arg1.length()) != arg0.length()) {
                if (!(night$isWordChar(arg0.charAt((var2 + arg1.length()))))) {
                }
            } else {
            }
            int var3 = 0;
            int var5 = 0;
            if (!(!var4) && !(!var3)) return true;
            var2 = (var2 + arg1.length());
            break label_2;
        }
        return false;
    }

    private static boolean night$isWordChar(char arg0) {
        if (!(Character.isLetterOrDigit(arg0))) {
            if (arg0 == 95) {
            }
        } else {
        }
        return false;
    }

    private static Component night$highlightMessage(Component arg0, String arg1, Color arg2) {
        Object var3 = TextColor.fromRgb((arg2.getRGB() & 16777215));
        Object var4 = Style.EMPTY.withColor(var3);
        Object var5 = Component.empty();
        arg0.visit((arg3, arg4) -> {
    if ((arg4 == null) || (arg4.isEmpty())) {
        return Optional.empty();
    }
    Object var5 = arg4.toLowerCase();
    Object var6 = arg0.toLowerCase();
    int var7 = 0;
    while (var7 < arg4.length()) {
        int var9 = var5.indexOf(var6, var7);
        if (var9 == -1) {
            arg1.append(Component.literal(arg4.substring(var7)).withStyle(arg3));
            break label_12;
        }
        if (!(!var9)) {
            if (!(night$isWordChar(arg4.charAt((var9 - 1))))) {
            }
        } else {
        }
        int var10 = 0;
        if ((var9 + var6.length()) != arg4.length()) {
            if (!(night$isWordChar(arg4.charAt((var9 + var6.length()))))) {
            }
        } else {
        }
        int var8 = 0;
        int var11 = 0;
        if (!(!var10) && !(!var8)) {
            if (var9 > var7) {
                arg1.append(Component.literal(arg4.substring(var7, var9)).withStyle(arg3));
            }
            arg1.append(Component.literal(arg4.substring(var9, (var9 + var6.length()))).withStyle(arg2));
            var7 = (var9 + var6.length());
            break label_2;
        }
        arg1.append(Component.literal(arg4.substring(var7, (var9 + 1))).withStyle(arg3));
        var7 = (var9 + 1);
    }
    label_12:
    return Optional.empty();
}, Style.EMPTY);
        return var5;
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void night$hideInventoryFull(Component arg0, MessageSignature arg1, GuiMessageSource arg2, GuiMessageTag arg3, CallbackInfo arg4) {
        if (arg0 != null && Normalizer.normalize(arg0.getString(), Normalizer.Form.NFC).toLowerCase(Locale.ROOT).contains("t\u00fai \u0111\u1ed3 c\u1ee7a b\u1ea1n \u0111\u00e3 \u0111\u1ea7y")) {
            arg4.cancel();
        }
    }

}
