package lightning.mixins.accessors;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public interface PlayerListS2CPacketAccessor {
    @Accessor("actions")
    @Mutable
    void setActions(EnumSet arg0);

    @Accessor("entries")
    @Mutable
    void setEntries(List arg0);

}
