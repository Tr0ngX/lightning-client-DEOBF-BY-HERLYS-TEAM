package lightning.mixins.accessors;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Style.class)
public interface StyleAccessor {
    @Invoker("<init>")
    static Style create(TextColor arg0, Integer arg1, Boolean arg2, Boolean arg3, Boolean arg4, Boolean arg5, Boolean arg6, ClickEvent arg7, HoverEvent arg8, String arg9, FontDescription arg10) {
        return null;
    }

}
