package lightning.mixins.accessors;

import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextColor.class)
public interface TextColorAccessor {
    @Invoker("<init>")
    static TextColor create(int arg0, String arg1) {
        throw new AssertionError();
    }

}
