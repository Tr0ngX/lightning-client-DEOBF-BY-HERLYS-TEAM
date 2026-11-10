package lightning.mixins.accessors;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
    @Invoker("startUseItem")
    void invokeStartUseItem();

    @Accessor("user")
    User getUser();

    @Mutable
    @Accessor("user")
    void setUser(User arg0);

    @Accessor("userApiService")
    UserApiService getUserApiService();

    @Mutable
    @Accessor("userApiService")
    void setUserApiService(UserApiService arg0);

    @Accessor("profileKeyPairManager")
    ProfileKeyPairManager getProfileKeyPairManager();

    @Mutable
    @Accessor("profileKeyPairManager")
    void setProfileKeyPairManager(ProfileKeyPairManager arg0);

}
