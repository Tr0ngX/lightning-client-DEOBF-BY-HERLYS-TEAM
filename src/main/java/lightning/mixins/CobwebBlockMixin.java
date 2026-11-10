package lightning.mixins;

import lightning.LightningClient;
import lightning.modules.impl.movement.FastWebModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public class CobwebBlockMixin implements IMinecraft {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void onEntityCollision(BlockState arg0, Level arg1, BlockPos arg2, Entity arg3, InsideBlockEffectApplier arg4, boolean arg5, CallbackInfo arg6) {
        if (!(((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).isToggled())) break label_2;
        if (((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).sneak.getValue() && !(mc.player.isShiftKeyDown())) return;
        if (((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).mode.getValue().equalsIgnoreCase("Ignore")) {
            arg3.resetFallDistance();
            arg6.cancel();
        }
        if (!(((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).mode.getValue().equalsIgnoreCase("Strong"))) break label_2;
        arg3.makeStuckInBlock(arg0, new Vec3(((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).horizontal.getValue().doubleValue(), ((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).vertical.getValue().doubleValue(), ((FastWebModule) LightningClient.MODULE_MANAGER.getModule(FastWebModule.class)).horizontal.getValue().doubleValue()));
        arg6.cancel();
        label_2:
    }

}
