package lightning.modules.impl.visuals.chestscan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ChestScanStore {
    private final Map records;
    private File file;

    public ChestScanStore() {
        this.records = new HashMap();
    }

    public static String currentWorldKey() {
        Object var0 = Minecraft.getInstance();
        if (var0.getCurrentServer() != null) {
            break label_2;
        }
        if (var0.hasSingleplayerServer() && var0.getSingleplayerServer() != null) {
            break label_2;
        }
        label_2:
        Object var1 = "unknown";
        if (var0.level != null) {
        } else {
        }
        Object var2 = "no_dimension";
        return (sanitize(((String) var1)) + "__" + sanitize(var2));
    }

    private static String sanitize(String arg0) {
        return arg0.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    public void loadForWorld() {
        this.records.clear();
        Object var1 = new File(FabricLoader.getInstance().getGameDir().toFile(), "lightning/chestscan");
        if (!(var1.exists())) {
            var1.mkdirs();
        }
        this.file = new File(var1, (currentWorldKey() + ".json"));
        if (!(this.file.exists())) break label_16;
        try {
            Object var2 = new FileReader(this.file);
            try {
                Object var3 = new Gson();
                Object var4 = new ChestScanStore.1(this).getType();
                Object var5 = ((List) var3.fromJson(var2, var4));
                if (var5 == null) break label_8;
                Object var6 = var5.iterator();
                while (var6.hasNext()) {
                    Object var7 = ((ChestScanStore.Entry) var6.next());
                    try {
                        this.records.put(new BlockPos(var7.x, var7.y, var7.z), ChestScanStore.ChestStatus.valueOf(var7.status));
                    } catch (Exception ex6) {
                        Object var8 = ex6;
                    }
                }
                label_8:
                var2.close();
            } catch (Throwable ex9) {
                var3 = ex9;
                try {
                    var2.close();
                } catch (Throwable ex12) {
                    var4 = ex12;
                    var3.addSuppressed(var4);
                }
                throw var3;
            }
        } catch (Exception ex15) {
            var2 = ex15;
        }
        label_16:
    }

    private void save() {
        if (this.file == null) {
            return;
            try {
            }
            Object var1 = new FileWriter(this.file);
            try {
                Object var2 = new ArrayList();
                Object var3 = this.records.entrySet().iterator();
                while (var3.hasNext()) {
                    Object var4 = ((Map.Entry) var3.next());
                    Object var5 = new ChestScanStore.Entry();
                    var5.x = ((BlockPos) var4.getKey()).getX();
                    var5.y = ((BlockPos) var4.getKey()).getY();
                    var5.z = ((BlockPos) var4.getKey()).getZ();
                    var5.status = ((ChestScanStore.ChestStatus) var4.getValue()).name();
                    var2.add(var5);
                }
                new GsonBuilder().setPrettyPrinting().create().toJson(var2, var1);
                var1.close();
            } catch (Throwable ex5) {
                var2 = ex5;
                try {
                    var1.close();
                } catch (Throwable ex8) {
                    var3 = ex8;
                    var2.addSuppressed(var3);
                }
                throw var2;
            }
        } catch (Exception ex11) {
            var1 = ex11;
        }
    }

    public void put(BlockPos arg0, ChestScanStore.ChestStatus arg1) {
        this.records.put(arg0.immutable(), arg1);
        save();
    }

    public ChestScanStore.ChestStatus get(BlockPos arg0) {
        return ((ChestScanStore.ChestStatus) this.records.get(arg0));
    }

    public void remove(BlockPos arg0) {
        if (this.records.remove(arg0) != null) {
            save();
        }
    }

    public Set positions() {
        return this.records.keySet();
    }


    class Entry {
        int x;
        int y;
        int z;
        String status;

        private Entry() {
        }

    }

    public enum ChestStatus {
        EMPTY,
        PARTIAL,
        FULL;


        private ChestStatus() {
        }

    }

    class 1 extends TypeToken {
        1(ChestScanStore arg0) {
            Objects.requireNonNull(arg0);
        }

    }
}
