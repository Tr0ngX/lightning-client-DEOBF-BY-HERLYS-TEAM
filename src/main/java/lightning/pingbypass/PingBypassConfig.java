package lightning.pingbypass;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassConfig {
    private static final Logger LOGGER;
    public static final String DEFAULT_SERVER = "false";
    public static final String DEFAULT_IP = "0.0.0.0";
    public static final String DEFAULT_PORT = "25565";
    public static final String DEFAULT_PASSWORD = "";
    private static final String KEY_SERVER = "pb.server";
    private static final String KEY_IP = "pb.ip";
    private static final String KEY_PORT = "pb.port";
    private static final String KEY_PASSWORD = "pb.password";
    private final Properties properties;
    private final Path configPath;
    private boolean loaded;

    public PingBypassConfig(Path arg0) {
        this.properties = new Properties();
        var tmp0 = new String[1];
        tmp0[0] = "pingbypass.properties";
        this.configPath = arg0.resolve(Paths.get("lightning", tmp0));
    }

    public void load() {
        try {
            Object var1 = this.configPath.getParent();
            if (var1 == null) break label_1;
            Files.createDirectories(var1, new FileAttribute[0]);
            label_1:
            if (Files.exists(this.configPath, new LinkOption[0])) break label_3;
            var tmp0 = new String[1];
            tmp0[0] = "pingbypass.properties";
            Object var2 = this.configPath.getParent().getParent().resolve(Paths.get("euclient", tmp0));
            if (Files.exists(var2, new LinkOption[0])) {
                var tmp1 = new CopyOption[1];
                tmp1[0] = StandardCopyOption.COPY_ATTRIBUTES;
                Files.copy(var2, this.configPath, tmp1);
                break label_3;
            }
            createDefaults();
            label_3:
            var2 = Files.newInputStream(this.configPath, new OpenOption[0]);
            try {
                this.properties.load(var2);
                if (var2 == null) break label_11;
                var2.close();
            } catch (Throwable ex6) {
                Object var3 = ex6;
                if (var2 == null) break label_10;
                try {
                    var2.close();
                } catch (Throwable ex9) {
                    Object var4 = ex9;
                    var3.addSuppressed(var4);
                }
                label_10:
                throw var3;
            }
            label_11:
            this.loaded = true;
        } catch (IOException ex12) {
            var1 = ex12;
            LOGGER.error("Failed to load PingBypass config from {}", this.configPath, var1);
            this.loaded = true;
            break label_16;
            Object var5 = null;
            this.loaded = true;
            throw var5;
        }
        label_16:
    }

    private void createDefaults() throws IOException {
        Object var1 = new Properties();
        var1.setProperty("pb.server", "false");
        var1.setProperty("pb.ip", "0.0.0.0");
        var1.setProperty("pb.port", "25565");
        var1.setProperty("pb.password", "");
        Object var2 = Files.newOutputStream(this.configPath, new OpenOption[0]);
        try {
            var1.store(var2, "PingBypass configuration");
            if (var2 == null) break label_7;
            var2.close();
        } catch (Throwable ex2) {
            Object var3 = ex2;
            if (var2 == null) break label_6;
            try {
                var2.close();
            } catch (Throwable ex5) {
                Object var4 = ex5;
                var3.addSuppressed(var4);
            }
            label_6:
            throw var3;
        }
        label_7:
    }

    public boolean isServer() {
        return Boolean.parseBoolean(getProperty("pb.server", "false"));
    }

    public String getIp() {
        return getProperty("pb.ip", "0.0.0.0");
    }

    public int getPort() {
        Object var1 = getProperty("pb.port", "25565");
        try {
            int var2 = Integer.parseInt(var1);
            if (var2 >= 1) {
                if (var2 > 65535) {
                }
                LOGGER.error("PingBypass port out of range (1-65535): {}, falling back to default 25565", var2);
                return 25565;
                try {
                }
                return var2;
            } catch (NumberFormatException ex5) {
                var2 = ex5;
                LOGGER.error("PingBypass port is not a valid integer: '{}', falling back to default 25565", var1);
                return 25565;
            }
        }
    }

    public String getPassword() {
        return getProperty("pb.password", "");
    }

    public boolean hasPassword() {
        Object var1 = getPassword();
        if (var1 == null) break label_0;
        return !(var1.isEmpty());
    }

    public String getProperty(String arg0, String arg1) {
        Object var3 = System.getProperty(arg0);
        if (var3 != null) return var3;
        return this.properties.getProperty(arg0, arg1);
    }

    public Properties toProperties() {
        Object var1 = new Properties();
        var1.setProperty("pb.server", String.valueOf(isServer()));
        var1.setProperty("pb.ip", getIp());
        var1.setProperty("pb.port", String.valueOf(getPort()));
        var1.setProperty("pb.password", getPassword());
        return var1;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    static {
        LOGGER = LoggerFactory.getLogger(PingBypassConfig.class);
    }

}
