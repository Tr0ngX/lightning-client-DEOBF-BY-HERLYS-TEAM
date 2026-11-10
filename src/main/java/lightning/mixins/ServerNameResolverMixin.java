package lightning.mixins;

import java.net.InetSocketAddress;
import java.util.Optional;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerNameResolver.class)
public class ServerNameResolverMixin {
    private static final Logger LOGGER;

    @Inject(method = "resolveAddress", at = @At("RETURN"), cancellable = true)
    private void resolveAddress(ServerAddress arg0, CallbackInfoReturnable arg1) {
        if (((Optional) arg1.getReturnValue()).isPresent()) {
            return;
            try {
            }
            Object var3 = new InetSocketAddress(arg0.getHost(), arg0.getPort());
            if (var3.isUnresolved()) {
                LOGGER.warn("[PB] java.net fallback resolve of {} also unresolved", arg0.getHost());
                return;
                try {
                }
                LOGGER.info("[PB] Resolved {} via java.net fallback: {}", arg0.getHost(), var3);
                arg1.setReturnValue(Optional.of(ResolvedServerAddress.from(var3)));
            } catch (Exception ex4) {
                var3 = ex4;
                LOGGER.warn("[PB] java.net fallback resolve of {} failed", arg0.getHost(), var3);
            }
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger("Lightning/DnsFix");
    }

}
