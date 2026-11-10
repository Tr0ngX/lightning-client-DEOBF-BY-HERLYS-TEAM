package lightning.mixins.accessors;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityFlagAccessor {
    @Accessor("FLAG_FALL_FLYING")
    static int getFlagFallFlying() {
        throw new AssertionError();
    }

    @Invoker("getSharedFlag")
    boolean invokeGetSharedFlag(int arg0);

    @Invoker("setSharedFlag")
    void invokeSetSharedFlag(int arg0, boolean arg1);

}
