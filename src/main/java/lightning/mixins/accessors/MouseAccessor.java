package lightning.mixins.accessors;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseAccessor {
    @Invoker("onButton")
    void invokeOnButton(long arg0, MouseButtonInfo arg1, int arg2);

}
