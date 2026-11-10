package lightning.mixins;

import lightning.utils.mixins.IChatHudLine;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public class ChatHudLineMixin implements IChatHudLine {
    @Unique
    private boolean clientMessage;
    @Unique
    private String clientIdentifier;

    public ChatHudLineMixin() {
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
