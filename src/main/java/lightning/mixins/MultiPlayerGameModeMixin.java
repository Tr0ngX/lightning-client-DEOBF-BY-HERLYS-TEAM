package lightning.mixins;

import lightning.gui.PeekScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "handleContainerInput", at = @At("HEAD"), cancellable = true)
    private void night$blockPeekClicks(int arg0, int arg1, int arg2, ContainerInput arg3, Player arg4, CallbackInfo arg5) {
        if ((Minecraft.getInstance().gui.screen() instanceof PeekScreen)) {
            arg5.cancel();
        }
    }

}
