package lightning.shaded0.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import lightning.shaded0.discordipc.Opcode;
import lightning.shaded0.discordipc.Packet;

public class WinConnection extends Connection {
    private final RandomAccessFile raf;
    private final Consumer callback;

    WinConnection(String arg0, Consumer arg1) throws IOException {
        this.raf = new RandomAccessFile(arg0, "rw");
        this.callback = arg1;
        Object var3 = new Thread(this::run);
        var3.setName("Discord IPC - Read thread");
        var3.start();
    }

    protected void write(ByteBuffer arg0) {
        try {
            this.raf.write(arg0.array());
        } catch (IOException ex2) {
            Object var2 = ex2;
            var2.printStackTrace();
        }
    }

    private void run() {
        Object var1 = ByteBuffer.allocate(4);
        try {
            label_0:
            readFully(var1);
            Object var2 = Opcode.valueOf(Integer.reverseBytes(var1.getInt(0)));
            readFully(var1);
            int var3 = Integer.reverseBytes(var1.getInt(0));
            Object var4 = ByteBuffer.allocate(var3);
            readFully(var4);
            Object var5 = Charset.defaultCharset().decode(var4.rewind()).toString();
            this.callback.accept(new Packet(var2, JsonParser.parseString(var5).getAsJsonObject()));
        } catch (Exception ex1) {
            var2 = ex1;
        }
    }

    private void readFully(ByteBuffer arg0) throws IOException {
        arg0.rewind();
        while (this.raf.length() < ((long) arg0.remaining())) {
            Thread.onSpinWait();
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex3) {
                Object var2 = ex3;
                var2.printStackTrace();
                while (arg0.hasRemaining()) {
                    this.raf.getChannel().read(arg0);
                }
            }
        }
    }

    public void close() {
        try {
            this.raf.close();
        } catch (IOException ex2) {
            Object var1 = ex2;
            var1.printStackTrace();
        }
    }

}
