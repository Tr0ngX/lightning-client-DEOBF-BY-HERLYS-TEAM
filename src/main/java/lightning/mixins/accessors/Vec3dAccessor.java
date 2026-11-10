package lightning.mixins.accessors;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3.class)
public interface Vec3dAccessor {
    @Accessor("x")
    @Mutable
    void setX(double arg0);

    @Accessor("y")
    @Mutable
    void setY(double arg0);

    @Accessor("z")
    @Mutable
    void setZ(double arg0);

}
