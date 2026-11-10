package lightning.shaded0.discordipc;

public enum Opcode {
    Handshake,
    Frame,
    Close,
    Ping,
    Pong;

    private static final Opcode[] VALUES;

    private Opcode() {
    }

    static {
        VALUES = values();
    }

}
