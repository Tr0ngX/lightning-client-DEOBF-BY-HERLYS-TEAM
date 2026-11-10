package lightning.modules.impl.miscellaneous;

import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import lightning.LightningClient;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.ImageTransferable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

@RegisterModule(name = "Screenshot", description = "Automatically copies screenshots to clipboard when pressing F2.", category = Module.Category.MISCELLANEOUS, toggled = true)
public class ScreenshotModule extends Module {
    public BooleanSetting clipboard;
    public BooleanSetting copyFile;
    public BooleanSetting notify;
    public BooleanSetting openFolder;

    public ScreenshotModule() {
        this.clipboard = new BooleanSetting("Clipboard", "Copies screenshot image directly to your clipboard.", true);
        this.copyFile = new BooleanSetting("CopyFile", "Also includes file flavor for pasting into file explorers.", true);
        this.notify = new BooleanSetting("Notify", "Sends a confirmation message in chat when copied.", true);
        this.openFolder = new BooleanSetting("OpenFolder", "Automatically opens the screenshots folder.", false);
    }

    public void onScreenshotCaptured(Component arg0) {
        if (!(isToggled())) return;
        Object var2 = extractScreenshotFile(arg0);
        if (var2 != null) {
            if (!(var2.exists())) {
            }
            var2 = findLatestScreenshotFile();
        }
        if ((var2 == null) || (!(var2.exists()))) {
            return;
        }
        Object var3 = var2;
        CompletableFuture.runAsync(() -> {
    try {
        int var3 = 0;
        if (!(this.clipboard.getValue())) break label_1;
        var tmp0 = ImageIO.read(arg0);
        Object var2 = tmp0;
        if (tmp0 == null) break label_1;
        Object var4 = new ImageTransferable(var2, arg0, this.copyFile.getValue());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(var4, null);
        var3 = 1;
        label_1:
        if (!var3 && this.openFolder.getValue()) {
            Util.getPlatform().openFile(arg0.getParentFile());
        }
        if (this.notify.getValue()) {
            LightningClient.CHAT_MANAGER.message(("Screenshot copied to clipboard: " + String.valueOf(ChatUtils.getPrimary()) + arg0.getName()), "screenshot-copied");
        }
    } catch (Throwable ex4) {
        var2 = ex4;
        LightningClient.LOGGER.error("Failed to copy screenshot to clipboard", var2);
    }
});
    }

    private File extractScreenshotFile(Component arg0) {
        if (arg0 == null) return null;
        Object var3 = arg0.getStyle();
        if (var3 == null) break label_1;
        var tmp0 = var3.getClickEvent();
        Object var2 = tmp0;
        if (!((tmp0 instanceof ClickEvent.OpenFile))) break label_1;
        Object var4 = ((ClickEvent.OpenFile) var2);
        return var4.file();
        label_1:
        var4 = arg0.getSiblings().iterator();
        label_2:
        if (var4.hasNext()) {
            Object var5 = ((Component) var4.next());
            Object var6 = extractScreenshotFile(var5);
            if (var6 == null) {
                break label_2;
            }
            return var6;
        }
        return null;
    }

    private File findLatestScreenshotFile() {
        try {
            Object var1 = new File(mc.gameDirectory, "screenshots");
            if (var1.exists()) {
                if (!(var1.isDirectory())) {
                }
                return null;
                try {
                }
                Object var2 = var1.listFiles((arg0, arg1) -> arg1.toLowerCase().endsWith(".png"));
                if (var2 != null) {
                    if (var2.length == 0) {
                    }
                    return null;
                    try {
                    }
                    return ((File) Arrays.stream(var2).max(Comparator.comparingLong(File::lastModified)).orElse(null));
                } catch (Throwable ex8) {
                    var1 = ex8;
                    return null;
                }
            }
        }
    }

}
