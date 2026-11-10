package lightning.gui.api;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lightning.LightningClient;

public final class GuiDescriptionTranslations {
    public static String translate(String arg0) {
        return ((String) GuiDescriptionTranslations.Holder.VALUES.getOrDefault(arg0, ""));
    }

    private static Map load() {
        try {
            Object var0 = GuiDescriptionTranslations.class.getResourceAsStream("/assets/lightning/gui/module-descriptions-vi.json");
            try {
                if (var0 == null) throw new IllegalStateException("Missing Vietnamese description resource");
                Object var1 = ((GuiDescriptionTranslations.Entry[]) new Gson().fromJson(new InputStreamReader(var0, StandardCharsets.UTF_8), GuiDescriptionTranslations.Entry[].class));
                if (var1 == null) throw new IllegalStateException("Empty Vietnamese description resource");
                Object var2 = new HashMap();
                Object var3 = var1;
                int var4 = var3.length;
                int var5 = 0;
                while (var5 < var4) {
                    Object var6 = var3[var5];
                    if ((((var6 == null) || (var6.english == null)) || (var6.vi == null)) || (var6.vi.isBlank())) {
                        throw new IllegalStateException("Invalid Vietnamese description entry");
                    }
                    Object var7 = ((String) var2.putIfAbsent(var6.english, var6.vi));
                    if (var7 != null && !(var7.equals(var6.vi))) throw new IllegalStateException("Conflicting Vietnamese descriptions");
                    var5++;
                }
                var3 = Map.copyOf(var2);
                if (var0 != null) {
                    var0.close();
                }
                return var3;
                try {
                } catch (Throwable ex11) {
                    var1 = ex11;
                    if (var0 == null) break label_15;
                    try {
                        var0.close();
                    } catch (Throwable ex14) {
                        var2 = ex14;
                        var1.addSuppressed(var2);
                    }
                    label_15:
                    throw var1;
                }
            } catch (Exception ex16) {
                var0 = ex16;
                LightningClient.LOGGER.warn("Could not load Vietnamese GUI descriptions", var0);
                return Map.of();
            }
        }
    }


    record Entry(String module, String english, String vi) {

        private Entry(String arg0, String arg1, String arg2) {
            this.module = arg0;
            this.english = arg1;
            this.vi = arg2;
        }

        public String module() {
            return this.module;
        }

        public String english() {
            return this.english;
        }

        public String vi() {
            return this.vi;
        }

    }

    final class Holder {
        private static final Map VALUES;

        static {
            VALUES = GuiDescriptionTranslations.load();
        }

    }
}
