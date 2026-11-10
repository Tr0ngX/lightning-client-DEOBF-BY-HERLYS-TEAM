package lightning.modules.impl.visuals.stashfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class StashFinderStore {
    private final Set evaluated;
    private final List stashes;
    private File file;
    private boolean dirty;
    private long lastSaveMs;
    private static final long SAVE_INTERVAL_MS = 10000L;

    public StashFinderStore() {
        this.evaluated = new HashSet();
        this.stashes = new ArrayList();
        this.dirty = false;
        this.lastSaveMs = 0L;
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

    public static long chunkKey(int arg0, int arg1) {
        return ((((long) arg0) & 4294967295L) | ((((long) arg1) & 4294967295L) << 32));
    }

    public void loadForWorld() {
        flush();
        this.evaluated.clear();
        this.stashes.clear();
        Object var1 = new File(FabricLoader.getInstance().getGameDir().toFile(), "lightning/stashfinder");
        if (!(var1.exists())) {
            var1.mkdirs();
        }
        this.file = new File(var1, (currentWorldKey() + ".json"));
        if (!(this.file.exists())) break label_12;
        try {
            Object var2 = new FileReader(this.file);
            try {
                Object var3 = new Gson();
                Object var4 = new StashFinderStore.1(this).getType();
                Object var5 = ((StashFinderStore.PersistentData) var3.fromJson(var2, var4));
                if (var5 == null) break label_4;
                if (var5.evaluated != null) {
                    this.evaluated.addAll(var5.evaluated);
                }
                if (var5.stashes == null) break label_4;
                this.stashes.addAll(var5.stashes);
                label_4:
                var2.close();
            } catch (Throwable ex5) {
                var3 = ex5;
                try {
                    var2.close();
                } catch (Throwable ex8) {
                    var4 = ex8;
                    var3.addSuppressed(var4);
                }
                throw var3;
            }
        } catch (Exception ex11) {
            var2 = ex11;
        }
        label_12:
    }

    private void save() {
        if (this.file == null) {
            return;
            try {
            }
            Object var1 = new FileWriter(this.file);
            try {
                Object var2 = new StashFinderStore.PersistentData();
                var2.evaluated = this.evaluated;
                var2.stashes = this.stashes;
                new GsonBuilder().create().toJson(var2, var1);
                var1.close();
            } catch (Throwable ex3) {
                var2 = ex3;
                try {
                    var1.close();
                } catch (Throwable ex6) {
                    Object var3 = ex6;
                    var2.addSuppressed(var3);
                }
                throw var2;
            }
        } catch (Exception ex9) {
            var1 = ex9;
        }
    }

    public boolean isEvaluated(long arg0) {
        return this.evaluated.contains(arg0);
    }

    public void markEvaluated(long arg0) {
        if (this.evaluated.add(arg0)) {
            this.dirty = true;
        }
    }

    public void recordStash(int arg0, int arg1, String arg2, Map arg3) {
        this.stashes.add(new StashFinderStore.StashData(arg0, arg1, arg2, arg3, System.currentTimeMillis()));
        this.dirty = true;
    }

    public List getStashes() {
        return Collections.unmodifiableList(this.stashes);
    }

    public void flushIfStale() {
        if (!(!this.dirty) && (System.currentTimeMillis() - this.lastSaveMs) >= 10000L) {
            flush();
        }
    }

    public void flush() {
        if (!this.dirty) return;
        save();
        this.dirty = false;
        this.lastSaveMs = System.currentTimeMillis();
    }

    public void clearAll() {
        this.evaluated.clear();
        this.stashes.clear();
        this.dirty = true;
        flush();
    }


    public record StashData(int x, int z, String dimension, Map counts, long timestamp) {

        public StashData(int arg0, int arg1, String arg2, Map arg3, long arg4) {
            this.x = arg0;
            this.z = arg1;
            this.dimension = arg2;
            this.counts = arg3;
            this.timestamp = arg4;
        }

        public int x() {
            return this.x;
        }

        public int z() {
            return this.z;
        }

        public String dimension() {
            return this.dimension;
        }

        public Map counts() {
            return this.counts;
        }

        public long timestamp() {
            return this.timestamp;
        }

    }

    class 1 extends TypeToken {
        1(StashFinderStore arg0) {
            Objects.requireNonNull(arg0);
        }

    }

    class PersistentData {
        Set evaluated;
        List stashes;

        private PersistentData() {
            this.evaluated = new HashSet();
            this.stashes = new ArrayList();
        }

    }
}
