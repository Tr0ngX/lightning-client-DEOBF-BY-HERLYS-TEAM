package lightning.mixins;

import lightning.utils.mixins.IChatHudLineVisible;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public class ChatHudLineVisibleMixin implements IChatHudLineVisible {
    @Unique
    private boolean clientMessage;
    @Unique
    private String clientIdentifier;

    public ChatHudLineVisibleMixin() {
        this.clientMessage = false;
        this.clientIdentifier = "";
    }

    public boolean night$isClientMessage() {
        return this.clientMessage;
    }

    public void night$setClientMessage(boolean arg0) {
        this.clientMessage = arg0;
    }

    public String night$getClientIdentifier() {
        return this.clientIdentifier;
    }

    public void night$setClientIdentifier(String arg0) {
        this.clientIdentifier = arg0;
    }

}
