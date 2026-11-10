package lightning.mixins.accessors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("getInputVector")
    static Vec3 invokeMovementInputToVelocity(Vec3 arg0, float arg1, float arg2) {
        throw new AssertionError();
    }

    @Invoker("setSharedFlag")
    void invokeSetSharedFlag(int arg0, boolean arg1);

    @Accessor("yRot")
    float getRawYRot();

    @Accessor("xRot")
    float getRawXRot();

    @Accessor("yRot")
    void setRawYRot(float arg0);

    @Accessor("xRot")
    void setRawXRot(float arg0);

}
