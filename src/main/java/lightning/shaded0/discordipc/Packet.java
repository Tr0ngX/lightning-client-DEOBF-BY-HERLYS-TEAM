package lightning.shaded0.discordipc;

import com.google.gson.JsonObject;

public record Packet(Opcode opcode, JsonObject data) {

    public Packet(Opcode arg0, JsonObject arg1) {
        this.opcode = arg0;
        this.data = arg1;
    }

    public Opcode opcode() {
        return this.opcode;
    }

    public JsonObject data() {
        return this.data;
    }

}
