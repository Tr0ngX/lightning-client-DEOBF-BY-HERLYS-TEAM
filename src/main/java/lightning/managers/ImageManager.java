package lightning.managers;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import lightning.LightningClient;
import lightning.utils.graphics.CustomImageTexture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class ImageManager {
    public static final Path IMAGES_DIR;
    public static final Identifier CUSTOM_IMAGE_ID;
    public static final Identifier CUSTOM_IMAGE_ID_LEGACY;
    private final Map cache;
    private String currentImageName;

    public ImageManager() {
        this.cache = new ConcurrentHashMap();
        this.currentImageName = "None";
        ensureDirectoryExists();
        try {
            Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, CustomImageTexture.INSTANCE);
            Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID_LEGACY, CustomImageTexture.INSTANCE);
        } catch (Throwable ex2) {
            Object var1 = ex2;
        }
        var1 = findSavedActiveImage();
        if (var1 != null && !(var1.equalsIgnoreCase("None")) && !(var1.isEmpty())) {
            setCurrentActiveImage(var1);
        } else {
            initDefaultTexture();
        }
    }

    public static String findSavedActiveImage() {
        try {
            Object var1 = "default";
            var tmp0 = new String[1];
            tmp0[0] = "General.json";
            Object var2 = Paths.get("Lightning", tmp0);
            if (!(Files.exists(var2, new LinkOption[0]))) break label_8;
            Object var3 = Files.newInputStream(var2, new OpenOption[0]);
            try {
                Object var4 = JsonParser.parseReader(new InputStreamReader(var3)).getAsJsonObject();
                if (!(var4.has("Config"))) break label_2;
                var1 = var4.get("Config").getAsString();
                label_2:
                if (var3 == null) break label_8;
                var3.close();
            } catch (Throwable ex3) {
                var4 = ex3;
                if (var3 == null) break label_7;
                try {
                    var3.close();
                } catch (Throwable ex6) {
                    Object var5 = ex6;
                    var4.addSuppressed(var5);
                }
                label_7:
                throw var4;
            }
            label_8:
            var tmp1 = new String[2];
            tmp1[0] = "Configs";
            tmp1[1] = (var1 + ".json");
            var tmp2 = Paths.get("Lightning", tmp1);
            Object var0 = tmp2;
            if (!(Files.exists(tmp2, new LinkOption[0]))) {
                var tmp3 = new String[2];
                tmp3[0] = "Configs";
                tmp3[1] = "default.json";
                var0 = Paths.get("Lightning", tmp3);
            }
            if (!(Files.exists(var0, new LinkOption[0]))) {
                return "None";
                try {
                }
                var3 = Files.newInputStream(var0, new OpenOption[0]);
                try {
                    var4 = JsonParser.parseReader(new InputStreamReader(var3)).getAsJsonObject();
                    if (!(var4.has("Shaders"))) {
                        var5 = "None";
                        if (var3 != null) {
                            var3.close();
                        }
                        return var5;
                        try {
                        }
                        var5 = var4.getAsJsonObject("Shaders");
                        if (!(var5.has("Settings"))) {
                            Object var6 = "None";
                            if (var3 != null) {
                                var3.close();
                            }
                            return var6;
                            try {
                            }
                            var6 = var5.getAsJsonObject("Settings");
                            if (!(var6.has("Image"))) {
                                Object var7 = "None";
                                if (var3 != null) {
                                    var3.close();
                                }
                                return var7;
                                try {
                                }
                                var7 = var6.get("Image").getAsString();
                                if (var7 == null) {
                                    Object var8 = "None";
                                    if (var3 != null) {
                                        var3.close();
                                    }
                                    return var8;
                                    try {
                                    }
                                    if (var7.isEmpty()) {
                                        var8 = "None";
                                        if (var3 != null) {
                                            var3.close();
                                        }
                                        return var8;
                                        try {
                                        }
                                        if (var7.equalsIgnoreCase("None")) {
                                            var8 = "None";
                                            if (var3 != null) {
                                                var3.close();
                                            }
                                            return var8;
                                            try {
                                            }
                                            var8 = var7;
                                            Object var9 = var8;
                                            if (var3 != null) {
                                                var3.close();
                                            }
                                            return var9;
                                            try {
                                            } catch (Throwable ex33) {
                                                var4 = ex33;
                                                if (var3 == null) break label_37;
                                                try {
                                                    var3.close();
                                                } catch (Throwable ex36) {
                                                    var5 = ex36;
                                                    var4.addSuppressed(var5);
                                                }
                                                label_37:
                                                throw var4;
                                            }
                                        } catch (Throwable ex38) {
                                            var0 = ex38;
                                            LightningClient.LOGGER.debug("Could not pre-read saved image config: {}", var0.getMessage());
                                            return "None";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void ensureDirectoryExists() {
        try {
            if (!(Files.exists(IMAGES_DIR, new LinkOption[0]))) {
                Files.createDirectories(IMAGES_DIR, new FileAttribute[0]);
            }
        } catch (Exception ex2) {
            Object var1 = ex2;
            LightningClient.LOGGER.error("Failed to create images directory", var1);
        }
    }

    public static NativeImage loadNativeImage(File arg0) throws Exception {
        Object var1 = ImageIO.read(arg0);
        if (var1 == null) throw new Exception(("ImageIO failed to read image file: " + arg0.getName()));
        Object var2 = new NativeImage(NativeImage.Format.RGBA, var1.getWidth(), var1.getHeight(), false);
        int var3 = 0;
        while (var3 < var1.getHeight()) {
            int var4 = 0;
            while (var4 < var1.getWidth()) {
                var2.setPixel(var4, var3, var1.getRGB(var4, var3));
                var4++;
            }
            var3++;
        }
        return var2;
    }

    public static NativeImage loadNativeImage(InputStream arg0) throws Exception {
        Object var1 = ImageIO.read(arg0);
        if (var1 == null) throw new Exception("ImageIO failed to read image stream");
        Object var2 = new NativeImage(NativeImage.Format.RGBA, var1.getWidth(), var1.getHeight(), false);
        int var3 = 0;
        while (var3 < var1.getHeight()) {
            int var4 = 0;
            while (var4 < var1.getWidth()) {
                var2.setPixel(var4, var3, var1.getRGB(var4, var3));
                var4++;
            }
            var3++;
        }
        return var2;
    }

    private void initDefaultTexture() {
        try {
            Object var1 = new NativeImage(1, 1, false);
            var1.setPixel(0, 0, 0);
            CustomImageTexture.INSTANCE.update(var1);
        } catch (Throwable ex2) {
            var1 = ex2;
            LightningClient.LOGGER.warn("Failed to load default custom_image fallback", var1);
        }
    }

    public static File resolveImageFile(String arg0) {
        if (((arg0 == null) || (arg0.isEmpty())) || (arg0.equalsIgnoreCase("None"))) {
            return null;
        }
        Object var1 = new ArrayList();
        var1.add(IMAGES_DIR);
        var tmp0 = new String[1];
        tmp0[0] = "Images";
        var1.add(Paths.get("Lightning", tmp0));
        var tmp1 = new String[1];
        tmp1[0] = "images";
        var1.add(Paths.get("Lightning", tmp1));
        var tmp2 = new String[1];
        tmp2[0] = "Images";
        var1.add(Paths.get("Lightning", tmp2));
        var tmp3 = new String[1];
        tmp3[0] = "images";
        var1.add(Paths.get("lightning", tmp3));
        try {
            Object var2 = FabricLoader.getInstance().getGameDir();
            var1.add(var2.resolve("Lightning").resolve("images"));
            var1.add(var2.resolve("Lightning").resolve("Images"));
            var1.add(var2.resolve("Lightning").resolve("images"));
        } catch (Throwable ex4) {
            var2 = ex4;
        }
        var2 = var1.iterator();
        label_6:
        if (var2.hasNext()) {
            Object var3 = ((Path) var2.next());
            Object var4 = var3.resolve(arg0).toFile();
            if (!(var4.exists())) break label_6;
            if (!(var4.isFile())) {
                break label_6;
            }
            return var4;
        }
        return IMAGES_DIR.resolve(arg0).toFile();
    }

    public List getImageFiles() {
        ensureDirectoryExists();
        Object var1 = new LinkedHashMap();
        Object var2 = new ArrayList();
        var2.add(IMAGES_DIR);
        var tmp0 = new String[1];
        tmp0[0] = "Images";
        var2.add(Paths.get("Lightning", tmp0));
        var tmp1 = new String[1];
        tmp1[0] = "images";
        var2.add(Paths.get("Lightning", tmp1));
        try {
            Object var3 = FabricLoader.getInstance().getGameDir();
            var2.add(var3.resolve("Lightning").resolve("images"));
            var2.add(var3.resolve("Lightning").resolve("Images"));
        } catch (Throwable ex2) {
            var3 = ex2;
        }
        var3 = var2.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Path) var3.next());
            Object var6 = var4.toFile();
            if ((!(var6.exists())) || (!(var6.isDirectory()))) break label_4;
            var tmp2 = var6.listFiles((arg0, arg1) -> {
    Object var2 = arg1.toLowerCase();
    if (!(var2.endsWith(".png")) && !(var2.endsWith(".jpg")) && !(var2.endsWith(".jpeg")) && !(var2.endsWith(".bmp"))) {
        if (var2.endsWith(".webp")) {
        }
    } else {
    }
    return false;
});
            Object var5 = tmp2;
            if (tmp2 == null) {
                break label_4;
            }
            Object var7 = var5;
            int var8 = var7.length;
            int var9 = 0;
            while (var9 < var8) {
                Object var10 = var7[var9];
                var1.putIfAbsent(var10.getName(), var10);
                var9++;
            }
        }
        var3 = new ArrayList(var1.values());
        var3.sort((arg0, arg1) -> arg0.getName().compareToIgnoreCase(arg1.getName()));
        return var3;
    }

    public ImageManager.ImageEntry getImageEntry(String arg0) {
        if (((arg0 == null) || (arg0.isEmpty())) || (arg0.equalsIgnoreCase("None"))) {
            return null;
        }
        if (this.cache.containsKey(arg0)) {
            Object var3 = ((ImageManager.ImageEntry) this.cache.get(arg0));
            if (var3.file.exists()) return var3;
            var3.close();
            this.cache.remove(arg0);
        }
        var tmp0 = resolveImageFile(arg0);
        Object var2 = tmp0;
        if (tmp0 != null && var2.exists()) {
            if (!(var2.isFile())) {
            }
            return null;
            try {
            }
            var3 = loadNativeImage(var2);
            Object var4 = Identifier.fromNamespaceAndPath("lightning", ("preview_" + Math.abs(arg0.hashCode()) + "_" + System.currentTimeMillis()));
            Object var5 = new DynamicTexture(() -> ("night/" + arg0), var3);
            var5.upload();
            Minecraft.getInstance().getTextureManager().register(var4, var5);
            Object var6 = new ImageManager.ImageEntry(arg0, var2, var4, var5, var3.getWidth(), var3.getHeight());
            this.cache.put(arg0, var6);
            return var6;
        } catch (Throwable ex7) {
            var3 = ex7;
            LightningClient.LOGGER.error(("Failed to load image: " + arg0), var3);
            return null;
        }
    }

    public void setCurrentActiveImage(String arg0) {
        this.currentImageName = (arg0 != null ? arg0 : "None");
        Object var2 = (arg0 != null ? arg0 : "None");
        if (!(this.currentImageName.equalsIgnoreCase("None"))) {
            if (this.currentImageName.isEmpty()) {
            }
            initDefaultTexture();
            return;
        }
        Object var3 = resolveImageFile(this.currentImageName);
        if (var3 != null) {
            if (!(var3.exists())) {
            }
            initDefaultTexture();
            return;
            try {
            }
            Object var4 = loadNativeImage(var3);
            CustomImageTexture.INSTANCE.update(var4);
            try {
                Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID, CustomImageTexture.INSTANCE);
                Minecraft.getInstance().getTextureManager().register(CUSTOM_IMAGE_ID_LEGACY, CustomImageTexture.INSTANCE);
            } catch (Throwable ex8) {
                Object var5 = ex8;
            }
        } catch (Throwable ex10) {
            var4 = ex10;
            LightningClient.LOGGER.error(("Failed to set active custom image: " + arg0), var4);
            initDefaultTexture();
        }
    }

    public String getCurrentImageName() {
        return this.currentImageName;
    }

    static {
        var tmp0 = new String[1];
        tmp0[0] = "images";
        IMAGES_DIR = Paths.get("Lightning", tmp0);
        CUSTOM_IMAGE_ID = Identifier.fromNamespaceAndPath("lightning", "textures/effect/custom_image.png");
        CUSTOM_IMAGE_ID_LEGACY = Identifier.fromNamespaceAndPath("lightning", "textures/custom_image.png");
    }


    public class ImageEntry {
        public final String name;
        public final File file;
        public final Identifier identifier;
        public final DynamicTexture texture;
        public final int width;
        public final int height;

        public ImageEntry(String arg0, File arg1, Identifier arg2, DynamicTexture arg3, int arg4, int arg5) {
            this.name = arg0;
            this.file = arg1;
            this.identifier = arg2;
            this.texture = arg3;
            this.width = arg4;
            this.height = arg5;
        }

        public void close() {
            try {
                if (this.texture != null) {
                    this.texture.close();
                }
                if (this.identifier != null) {
                    Minecraft.getInstance().getTextureManager().release(this.identifier);
                }
            } catch (Throwable ex3) {
                Object var1 = ex3;
            }
        }

    }
}
