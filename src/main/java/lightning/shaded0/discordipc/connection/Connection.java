package lightning.shaded0.discordipc.connection;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Consumer;
import lightning.shaded0.discordipc.Opcode;

public abstract class Connection {
    private static final String[] UNIX_TEMP_PATHS;

    public static Connection open(Consumer arg0) {
        Object var1 = System.getProperty("os.name").toLowerCase();
        if (!(var1.contains("win"))) break label_6;
        int var2 = 0;
        while (var2 < 10) {
            try {
                return new WinConnection(("\\\\?\\pipe\\discord-ipc-" + var2), arg0);
            } catch (IOException ex3) {
                Object var3 = ex3;
                break label_4;
                label_4:
                var2++;
            }
            break label_15;
            label_6:
            var3 = null;
            Object var4 = UNIX_TEMP_PATHS;
            int var5 = var4.length;
            int var6 = 0;
            while (var6 < var5) {
                var tmp0 = var4[var6];
                var2 = tmp0;
                var tmp1 = System.getenv(tmp0);
                var3 = tmp1;
                if (tmp1 != null) break label_8;
                var6++;
            }
            label_8:
            if (var3 == null) {
                var3 = "/tmp";
            }
            var3 = (((String) var3) + "/discord-ipc-");
            var6 = 0;
            while (var6 < 10) {
                try {
                    return new UnixConnection((((String) var3) + var6), arg0);
                }
            } catch (IOException ex13) {
                Object var7 = ex13;
                break label_14;
                label_14:
                var6++;
            }
            label_15:
            return null;
        }
    }

    public void write(Opcode arg0, JsonObject arg1) {
        arg1.addProperty("nonce", UUID.randomUUID().toString());
        Object var3 = arg1.toString().getBytes();
        Object var4 = ByteBuffer.allocate((var3.length + 8));
        var4.putInt(Integer.reverseBytes(arg0.ordinal()));
        var4.putInt(Integer.reverseBytes(var3.length));
        var4.put(var3);
        var4.rewind();
        write(var4);
    }

    protected abstract void write(ByteBuffer arg0);

    public abstract void close();

    static {
        var tmp0 = new String[4];
        tmp0[0] = "XDG_RUNTIME_DIR";
        tmp0[1] = "TMPDIR";
        tmp0[2] = "TMP";
        tmp0[3] = "TEMP";
        UNIX_TEMP_PATHS = tmp0;
    }

}
