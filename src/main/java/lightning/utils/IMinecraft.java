package lightning.utils;

import net.minecraft.client.Minecraft;

public interface IMinecraft {
    public static final Minecraft mc;

    static {
        mc = Minecraft.getInstance();
    }

}
