package lightning.shaded0.discordipc.connection;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import lightning.shaded0.discordipc.Opcode;
import lightning.shaded0.discordipc.Packet;

public class UnixConnection extends Connection {
    private final Selector s;
    private final SocketChannel sc;
    private final Consumer callback;

    public UnixConnection(String arg0, Consumer arg1) throws IOException {
        this.s = Selector.open();
        this.sc = SocketChannel.open(UnixDomainSocketAddress.of(arg0));
        this.callback = arg1;
        this.sc.configureBlocking(false);
        this.sc.register(this.s, 1);
        Object var3 = new Thread(this::run);
        var3.setName("Discord IPC - Read thread");
        var3.start();
    }

    private void run() {
        Object var1 = UnixConnection.State.Opcode;
        Object var2 = ByteBuffer.allocate(4);
        Object var3 = null;
        Object var4 = null;
        try {
            label_0:
            this.s.select();
            switch (var1.ordinal()) {
                case 0 -> { break label_1; }
                case 1 -> { break label_3; }
                case 2 -> { break label_5; }
                default -> { break label_7; }
            }
            label_1:
            this.sc.read(var2);
            if (var2.hasRemaining()) {
                break label_0;
            }
            var4 = Opcode.valueOf(Integer.reverseBytes(var2.getInt(0)));
            var1 = UnixConnection.State.Length;
            var2.rewind();
            break label_7;
            label_3:
            this.sc.read(var2);
            if (var2.hasRemaining()) {
                break label_0;
            }
            var3 = ByteBuffer.allocate(Integer.reverseBytes(var2.getInt(0)));
            var1 = UnixConnection.State.Data;
            var2.rewind();
            break label_7;
            label_5:
            this.sc.read(((ByteBuffer) var3));
            if (var3.hasRemaining()) {
                break label_0;
            }
            Object var5 = Charset.defaultCharset().decode(((ByteBuffer) var3).rewind()).toString();
            this.callback.accept(new Packet(var4, JsonParser.parseString(var5).getAsJsonObject()));
            var3 = null;
            var1 = UnixConnection.State.Opcode;
            label_7:
        } catch (Exception ex8) {
            var5 = ex8;
        }
    }

    protected void write(ByteBuffer arg0) {
        try {
            this.sc.write(arg0);
        } catch (IOException ex2) {
            Object var2 = ex2;
            var2.printStackTrace();
        }
    }

    public void close() {
        try {
            this.s.close();
            this.sc.close();
        } catch (IOException ex2) {
            Object var1 = ex2;
            var1.printStackTrace();
        }
    }


    enum State {
        Opcode,
        Length,
        Data;


        private State() {
        }

    }
}
