package lightning.mixins.accessors;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatHudAccessor {
    @Accessor("allMessages")
    List getMessages();

    @Accessor("trimmedMessages")
    List getVisibleMessages();

    @Accessor("chatScrollbarPos")
    int getScrolledLines();

    @Accessor("newMessageSinceScroll")
    void setHasUnreadNewMessages(boolean arg0);

    @Invoker("logChatMessage")
    void invokeLogChatMessage(GuiMessage arg0);

    @Invoker("addMessageToDisplayQueue")
    void invokeAddVisibleMessage(GuiMessage arg0);

    @Invoker("addMessageToQueue")
    void invokeAddMessage(GuiMessage arg0);

    @Invoker("getWidth")
    int invokeGetWidth();

    @Invoker("getScale")
    double invokeGetScale();

}
