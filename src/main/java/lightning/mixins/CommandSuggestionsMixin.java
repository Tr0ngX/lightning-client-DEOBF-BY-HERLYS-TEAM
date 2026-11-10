package lightning.mixins;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import lightning.LightningClient;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Nullable
    private ParseResults currentParse;
    @Shadow
    @Nullable
    private CompletableFuture pendingSuggestions;
    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;
    @Shadow
    private boolean keepSuggestions;
    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private void updateUsageInfo(ParseResults arg0, Suggestions arg1) {
    }

    @Inject(method = "updateCommandInfo", at = @At("RETURN"))
    private void night$updateCommandInfo(CallbackInfo arg0) {
        Object var4 = this.input.getValue();
        var tmp0 = LightningClient.COMMAND_MANAGER.getPrefix();
        Object var3 = tmp0;
        if (!(var4.startsWith(tmp0))) return;
        Object var5 = new StringReader(var4);
        var5.setCursor(var3.length());
        Object var6 = LightningClient.COMMAND_MANAGER.getDispatcher();
        var tmp1 = var6.parse(var5, LightningClient.COMMAND_MANAGER);
        Object var2 = tmp1;
        this.currentParse = tmp1;
        if (this.suggestions != null) {
            if (!this.keepSuggestions) {
            }
            var tmp2 = var6.getCompletionSuggestions(var2, this.input.getCursorPosition());
            Object var7 = tmp2;
            this.pendingSuggestions = tmp2;
            var7.thenRun(() -> {
    if (arg0.isDone()) {
        updateUsageInfo(this.currentParse, ((Suggestions) arg0.join()));
    }
});
        }
    }

}
