package lightning.utils.graphics;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarInputStream;
import lightning.LightningClient;
import lightning.managers.ImageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;

public class NightPackResources extends AbstractPackResources {
    public static final NightPackResources INSTANCE;
    private static final String NAMESPACE = "lightning";
    private static final Set NAMESPACES;
    private static final List FALLBACK_ASSETS;

    public NightPackResources() {
        super(new PackLocationInfo("lightning", Component.literal("Lightning Resources"), PackSource.BUILT_IN, Optional.empty()));
    }

    public IoSupplier getRootResource(String... arg0) {
        Object var2 = String.join("/", arg0);
        if (NightPackResources.class.getClassLoader().getResource(var2) != null) return () -> NightPackResources.class.getClassLoader().getResourceAsStream(arg0);
        return null;
    }

    public IoSupplier getResource(PackType arg0, Identifier arg1) {
        if ((arg0 != PackType.CLIENT_RESOURCES) || (!"lightning".equals(arg1.getNamespace()))) {
            return null;
        }
        if (!(arg1.getPath().equals("textures/effect/custom_image.png"))) {
            if (!(arg1.getPath().equals("textures/custom_image.png"))) break label_3;
        }
        if (LightningClient.IMAGE_MANAGER == null) break label_3;
        var tmp0 = LightningClient.IMAGE_MANAGER.getCurrentImageName();
        Object var4 = tmp0;
        if (((tmp0 == null) || (var4.isEmpty())) || (var4.equalsIgnoreCase("None"))) break label_3;
        var tmp1 = ImageManager.resolveImageFile(var4);
        Object var3 = tmp1;
        if (tmp1 != null && var3.exists() && var3.isFile()) return () -> new FileInputStream(arg0);
        label_3:
        Object var5 = ("assets/lightning/" + arg1.getPath());
        if (NightPackResources.class.getClassLoader().getResource(var5) != null) return () -> NightPackResources.class.getClassLoader().getResourceAsStream(arg0);
        return null;
    }

    public void listResources(PackType arg0, String arg1, String arg2, PackResources.ResourceOutput arg3) {
        if ((arg0 != PackType.CLIENT_RESOURCES) || (!"lightning".equals(arg1))) {
            return;
        }
        Object var6 = new HashSet();
        if (!(arg2.isEmpty())) break label_2;
        break label_4;
        label_2:
        if (arg2.endsWith("/")) {
            break label_4;
        }
        label_4:
        Object var5 = (arg2 + "/");
        try {
            Object var7 = NightPackResources.class.getProtectionDomain().getCodeSource().getLocation();
            if (var7 == null) {
                break label_25;
                try {
                }
                Object var8 = ("assets/lightning/" + var5);
                Object var9 = var7.openStream();
                try {
                    Object var10 = new JarInputStream(var9);
                    try {
                        label_9:
                        var tmp0 = var10.getNextJarEntry();
                        Object var11 = tmp0;
                        if (tmp0 != null) {
                            Object var14 = var11.getName();
                            if ((var11.isDirectory()) || (!(var14.startsWith(var8)))) break label_9;
                            var tmp1 = var14.substring("assets/lightning/".length());
                            Object var13 = tmp1;
                            if (!(var6.add(tmp1))) break label_9;
                            var tmp2 = Identifier.tryBuild("lightning", var13);
                            Object var12 = tmp2;
                            if (tmp2 == null) {
                                break label_9;
                            }
                            arg3.accept(var12, () -> NightPackResources.class.getClassLoader().getResourceAsStream(arg0));
                            break label_9;
                        }
                        var10.close();
                    } catch (Throwable ex12) {
                        var11 = ex12;
                        try {
                            var10.close();
                        } catch (Throwable ex15) {
                            var12 = ex15;
                            var11.addSuppressed(var12);
                        }
                        throw var11;
                    }
                    if (var9 == null) break label_23;
                    var9.close();
                } catch (Throwable ex18) {
                    var10 = ex18;
                    if (var9 == null) break label_22;
                    try {
                        var9.close();
                    } catch (Throwable ex21) {
                        var11 = ex21;
                        var10.addSuppressed(var11);
                    }
                    label_22:
                    throw var10;
                }
                label_23:
            } catch (Throwable ex24) {
                var7 = ex24;
            }
            label_25:
            var7 = FALLBACK_ASSETS.iterator();
            while (var7.hasNext()) {
                var8 = ((String) var7.next());
                if (!(var8.startsWith(var5))) break label_26;
                if (!(var6.add(var8))) {
                    break label_26;
                }
                var10 = ("assets/lightning/" + var8);
                if (NightPackResources.class.getClassLoader().getResource(var10) == null) break label_26;
                var tmp3 = Identifier.tryBuild("lightning", var8);
                var9 = tmp3;
                if (tmp3 == null) {
                    break label_26;
                }
                arg3.accept(var9, () -> NightPackResources.class.getClassLoader().getResourceAsStream(arg0));
            }
        }
    }

    public Set getNamespaces(PackType arg0) {
        if (arg0 == PackType.CLIENT_RESOURCES) {
        } else {
        }
        return Set.of();
    }

    public void close() {
    }

    static {
        INSTANCE = new NightPackResources();
        NAMESPACES = Set.of("lightning");
        var tmp0 = new String[57];
        tmp0[0] = "icon.png";
        tmp0[1] = "post_effect/clickgui_bg.json";
        tmp0[2] = "post_effect/glow_blur.json";
        tmp0[3] = "post_effect/menu_bg.json";
        tmp0[4] = "post_effect/neekeri_ui.json";
        tmp0[5] = "post_effect/outline_bloom.json";
        tmp0[6] = "post_effect/outline_both_100.json";
        tmp0[7] = "post_effect/outline_both_20.json";
        tmp0[8] = "post_effect/outline_both_40.json";
        tmp0[9] = "post_effect/outline_both_60.json";
        tmp0[10] = "post_effect/outline_both_80.json";
        tmp0[11] = "post_effect/outline_fill_100.json";
        tmp0[12] = "post_effect/outline_fill_20.json";
        tmp0[13] = "post_effect/outline_fill_40.json";
        tmp0[14] = "post_effect/outline_fill_60.json";
        tmp0[15] = "post_effect/outline_fill_80.json";
        tmp0[16] = "post_effect/outline_glow.json";
        tmp0[17] = "post_effect/outline_outline.json";
        tmp0[18] = "post_effect/star_glow.json";
        tmp0[19] = "shaders/core/esp.fsh";
        tmp0[20] = "shaders/core/killeffect_meme.fsh";
        tmp0[21] = "shaders/core/menu_shader.fsh";
        tmp0[22] = "shaders/core/sky.fsh";
        tmp0[23] = "shaders/core/text_glow.fsh";
        tmp0[24] = "shaders/program/clickgui_bg.fsh";
        tmp0[25] = "shaders/program/glow_blur.fsh";
        tmp0[26] = "shaders/program/menu_bg.fsh";
        tmp0[27] = "shaders/program/neekeri_ui.fsh";
        tmp0[28] = "shaders/program/outline.fsh";
        tmp0[29] = "shaders/program/outline_bloom.fsh";
        tmp0[30] = "shaders/program/outline_glow.fsh";
        tmp0[31] = "shaders/program/outline_glow_blur.fsh";
        tmp0[32] = "shaders/program/sky/screen_blit.vsh";
        tmp0[33] = "shaders/program/sky/sky_shader.fsh";
        tmp0[34] = "shaders/program/sky/sky_shader.json";
        tmp0[35] = "shaders/program/sky/sky_shader.vsh";
        tmp0[36] = "shaders/program/smoke/smoke.fsh";
        tmp0[37] = "shaders/program/smoke/smoke.json";
        tmp0[38] = "shaders/program/smoke/smoke.vsh";
        tmp0[39] = "shaders/program/star_glow.fsh";
        tmp0[40] = "shaders/program/star_glow_blur.fsh";
        tmp0[41] = "sounds.json";
        tmp0[42] = "sounds/vine.ogg";
        tmp0[43] = "splash.txt";
        tmp0[44] = "textures/cape.png";
        tmp0[45] = "textures/chorus.png";
        tmp0[46] = "textures/crystal.png";
        tmp0[47] = "textures/custom_image.png";
        tmp0[48] = "textures/effect/custom_image.png";
        tmp0[49] = "textures/effect/meme_arrow.png";
        tmp0[50] = "textures/effect/meme_circle.png";
        tmp0[51] = "textures/gear.png";
        tmp0[52] = "textures/glint.png";
        tmp0[53] = "textures/gui/logo.png";
        tmp0[54] = "textures/gui/logo_mask.png";
        tmp0[55] = "textures/gui/moonlight_bg.png";
        tmp0[56] = "textures/pearl.png";
        FALLBACK_ASSETS = List.of(tmp0);
    }

}
