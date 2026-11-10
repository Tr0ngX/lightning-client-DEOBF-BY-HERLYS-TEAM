package lightning.mixins.accessors;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface ClientPlayerEntityAccessor {
    @Invoker("isMoving")
    boolean invokeIsWalking();

    @Invoker("isSprintingPossible")
    boolean invokeCanSprint(boolean arg0);

    @Invoker("sendPosition")
    void invokeSendMovementPackets();

    @Accessor("lastOnGround")
    void setLastOnGround(boolean arg0);

    @Accessor("yRotLast")
    void setLastYaw(float arg0);

    @Accessor("xRotLast")
    void setLastPitch(float arg0);

    @Accessor("lastSentInput")
    void setLastSentInput(Input arg0);

}
