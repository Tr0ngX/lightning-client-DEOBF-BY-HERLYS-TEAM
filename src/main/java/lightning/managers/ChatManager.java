package lightning.managers;

import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.ChatHudAccessor;
import lightning.modules.impl.core.CommandsModule;
import lightning.modules.impl.miscellaneous.BetterChatModule;
import lightning.utils.IMinecraft;
import lightning.utils.chat.ChatUtils;
import lightning.utils.mixins.IChatHudLine;
import lightning.utils.mixins.IChatHudLineVisible;
import lightning.utils.text.FormattingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class ChatManager implements IMinecraft {
    public static boolean isInsideHudExtractChat;
    private final List awaitMessages;

    public ChatManager() {
        this.awaitMessages = new ArrayList();
        LightningClient.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        if (this.awaitMessages.isEmpty()) return;
        Object var2 = new ArrayList(this.awaitMessages).iterator();
        while (var2.hasNext()) {
            Object var3 = ((String) var2.next());
            addMessage(var3);
            this.awaitMessages.remove(var3);
        }
    }

    public void message(String arg0) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        addMessage((getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void message(String arg0, String arg1) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        deleteMessage(arg1);
        addMessage((getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + arg0), arg1);
    }

    public void tagged(String arg0, String arg1) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        addMessage((getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + arg1 + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void tagged(String arg0, String arg1, String arg2) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        deleteMessage(arg2);
        addMessage((getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + arg1 + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + arg0), arg2);
    }

    public void info$await(String arg0) {
        this.awaitMessages.add((getWatermark() + String.valueOf(ChatFormatting.DARK_BLUE) + " [" + String.valueOf(ChatFormatting.BLUE) + "?" + String.valueOf(ChatFormatting.DARK_BLUE) + "] " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void info(String arg0) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        addMessage((getWatermark() + String.valueOf(ChatFormatting.DARK_BLUE) + " [" + String.valueOf(ChatFormatting.BLUE) + "?" + String.valueOf(ChatFormatting.DARK_BLUE) + "] " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void warn$await(String arg0) {
        this.awaitMessages.add((getWatermark() + String.valueOf(ChatFormatting.GOLD) + " [" + String.valueOf(ChatFormatting.YELLOW) + "!" + String.valueOf(ChatFormatting.GOLD) + "] " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void warn(String arg0) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        addMessage((getWatermark() + String.valueOf(ChatFormatting.GOLD) + " [" + String.valueOf(ChatFormatting.YELLOW) + "!" + String.valueOf(ChatFormatting.GOLD) + "] " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void error$await(String arg0) {
        this.awaitMessages.add((getWatermark() + String.valueOf(ChatFormatting.DARK_RED) + " [" + String.valueOf(ChatFormatting.RED) + "!!" + String.valueOf(ChatFormatting.DARK_RED) + "] " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void error(String arg0) {
        if ((mc.player == null) || (mc.gui == null)) {
            return;
        }
        addMessage((getWatermark() + String.valueOf(ChatFormatting.DARK_RED) + " [" + String.valueOf(ChatFormatting.RED) + "!!" + String.valueOf(ChatFormatting.DARK_RED) + "] " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void await(String arg0) {
        this.awaitMessages.add((getWatermark() + " " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void await(String arg0, String arg1) {
        this.awaitMessages.add((getWatermark() + String.valueOf(ChatFormatting.DARK_AQUA) + " [" + String.valueOf(ChatFormatting.AQUA) + arg1 + String.valueOf(ChatFormatting.DARK_AQUA) + "]: " + String.valueOf(ChatUtils.getSecondary()) + arg0));
    }

    public void addMessage(String arg0) {
        addTextMessage(Component.literal(arg0), "");
    }

    public void addMessage(String arg0, String arg1) {
        addTextMessage(Component.literal(arg0), arg1);
    }

    public void addMessage(Component arg0) {
        addTextMessage(arg0, "");
    }

    public void addMessage(Component arg0, String arg1) {
        addTextMessage(arg0, arg1);
    }

    private void addTextMessage(Component arg0, String arg1) {
        if (!(mc.isSameThread())) {
            mc.execute(() -> addTextMessage(arg0, arg1));
            return;
        }
        Object var3 = new GuiMessage(mc.gui.hud.getGuiTicks(), arg0, null, GuiMessageSource.SYSTEM_CLIENT, GuiMessageTag.system());
        ((IChatHudLine) var3).night$setClientMessage(true);
        ((IChatHudLine) var3).night$setClientIdentifier(arg1);
        ((ChatHudAccessor) mc.gui.hud.getChat()).invokeLogChatMessage(var3);
        ((ChatHudAccessor) mc.gui.hud.getChat()).invokeAddMessage(var3);
        Object var4 = ComponentRenderUtils.wrapComponents(var3.content(), Mth.floor((((double) ((ChatHudAccessor) mc.gui.hud.getChat()).invokeGetWidth()) / ((ChatHudAccessor) mc.gui.hud.getChat()).invokeGetScale())), mc.font);
        int var5 = 0;
        while (var5 < var4.size()) {
            Object var6 = ((FormattedCharSequence) var4.get(var5));
            if (mc.gui.hud.getChat().isChatFocused() && ((ChatHudAccessor) mc.gui.hud.getChat()).getScrolledLines() > 0) {
                ((ChatHudAccessor) mc.gui.hud.getChat()).setHasUnreadNewMessages(true);
                mc.gui.hud.getChat().scrollChat(1);
            }
            int var7 = (var5 == (var4.size() - 1) ? 1 : 0);
            Object var8 = new GuiMessage.Line(var3, var6, var7);
            ((IChatHudLineVisible) var8).night$setClientMessage(true);
            ((IChatHudLineVisible) var8).night$setClientIdentifier(arg1);
            ((ChatHudAccessor) mc.gui.hud.getChat()).getVisibleMessages().addFirst(var8);
            if (!(((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class)).isToggled())) break label_6;
            if (!(((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class)).animation.getValue())) {
                break label_6;
            }
            ((BetterChatModule) LightningClient.MODULE_MANAGER.getModule(BetterChatModule.class)).getAnimationMap().put(var8, System.currentTimeMillis());
            label_6:
            var5++;
            while (((ChatHudAccessor) mc.gui.hud.getChat()).getVisibleMessages().size() > 100) {
                ((ChatHudAccessor) mc.gui.hud.getChat()).getVisibleMessages().removeLast();
            }
        }
    }

    public static void deleteMessage(String arg0) {
        try {
            Object var1 = new ArrayList();
            Object var2 = ((ChatHudAccessor) mc.gui.hud.getChat()).getMessages().iterator();
            while (var2.hasNext()) {
                Object var3 = ((GuiMessage) var2.next());
                if ((!(((IChatHudLine) var3).night$isClientMessage())) || (((IChatHudLine) var3).night$getClientIdentifier().isEmpty())) break label_1;
                if (!(((IChatHudLine) var3).night$getClientIdentifier().equals(arg0))) {
                    break label_1;
                }
                var1.add(var3);
            }
            var2 = new ArrayList();
            var3 = ((ChatHudAccessor) mc.gui.hud.getChat()).getVisibleMessages().iterator();
            while (var3.hasNext()) {
                Object var4 = ((GuiMessage.Line) var3.next());
                if ((!(((IChatHudLineVisible) var4).night$isClientMessage())) || (((IChatHudLineVisible) var4).night$getClientIdentifier().isEmpty())) break label_4;
                if (!(((IChatHudLineVisible) var4).night$getClientIdentifier().equals(arg0))) {
                    break label_4;
                }
                var2.add(var4);
            }
            ((ChatHudAccessor) mc.gui.hud.getChat()).getMessages().removeAll(var1);
            ((ChatHudAccessor) mc.gui.hud.getChat()).getVisibleMessages().removeAll(var2);
        } catch (Exception ex8) {
            var1 = ex8;
            var1.printStackTrace();
        }
    }

    private String getWatermark() {
        return getWatermark(null);
    }

    private String getWatermark(String arg0) {
        if (!(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).watermark.getValue())) return "";
        if (arg0 == null) {
        } else {
        }
        return (((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).opening.getValue() + String.valueOf(FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).primaryWatermarkColor.getValue())) + ((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).watermarkText.getValue() + arg0 + String.valueOf(FormattingUtils.getFormatting(((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).secondaryWatermarkColor.getValue())) + ((CommandsModule) LightningClient.MODULE_MANAGER.getModule(CommandsModule.class)).closing.getValue());
    }

    static {
        isInsideHudExtractChat = false;
    }

}
