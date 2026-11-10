package lightning.utils.system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class FileUtils {
    public static void resetFile(String arg0) throws IOException {
        if (Files.exists(Paths.get(arg0, new String[0]), new LinkOption[0])) {
            new File(arg0).delete();
        }
        Files.createFile(Paths.get(arg0, new String[0]), new FileAttribute[0]);
    }

    public static void createDirectory(String arg0) throws IOException {
        if (!(Files.exists(Paths.get(arg0, new String[0]), new LinkOption[0]))) {
            Files.createDirectories(Paths.get(arg0, new String[0]), new FileAttribute[0]);
        }
    }

    public static boolean fileExists(String arg0) {
        return Files.exists(Paths.get(arg0, new String[0]), new LinkOption[0]);
    }

    public static void playSound(File arg0, float arg1) {
        if (!(arg0.exists())) break label_3;
        try {
            Object var2 = AudioSystem.getClip();
            var2.open(AudioSystem.getAudioInputStream(arg0));
            ((FloatControl) var2.getControl(FloatControl.Type.MASTER_GAIN)).setValue((20.0f * ((float) Math.log10(((double) arg1)))));
            var2.start();
            var2.addLineListener((arg1) -> {
    if (arg1.getType() == LineEvent.Type.STOP) {
        arg0.close();
    }
});
        } catch (Exception ex2) {
            var2 = ex2;
            var2.printStackTrace();
        }
        label_3:
    }

    public static List readLines(File arg0) {
        Object var1 = new ArrayList();
        if (!(arg0.exists())) break label_10;
        try {
            Object var2 = Files.lines(Paths.get(arg0.getPath(), new String[0]));
            try {
                var1 = var2.filter((arg0) -> !(arg0.isEmpty())).toList();
                if (var2 == null) break label_8;
                var2.close();
            } catch (Throwable ex3) {
                Object var3 = ex3;
                if (var2 == null) break label_7;
                try {
                    var2.close();
                } catch (Throwable ex6) {
                    Object var4 = ex6;
                    var3.addSuppressed(var4);
                }
                label_7:
                throw var3;
            }
            label_8:
        } catch (Exception ex9) {
            var2 = ex9;
        }
        label_10:
        return var1;
    }

}
