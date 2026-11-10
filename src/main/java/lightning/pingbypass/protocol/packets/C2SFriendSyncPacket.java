package lightning.pingbypass.protocol.packets;

import java.util.ArrayList;
import java.util.List;
import lightning.pingbypass.protocol.PbPacket;
import net.minecraft.network.FriendlyByteBuf;

public class C2SFriendSyncPacket extends PbPacket {
    public static final int ID = 14;
    private final List friends;

    public C2SFriendSyncPacket(List arg0) {
        this.friends = arg0;
    }

    public C2SFriendSyncPacket(FriendlyByteBuf arg0) {
        int var2 = arg0.readVarInt();
        this.friends = new ArrayList(var2);
        int var3 = 0;
        while (var3 < var2) {
            this.friends.add(arg0.readUtf());
            var3++;
        }
    }

    public int getPacketId() {
        return 14;
    }

    public void write(FriendlyByteBuf arg0) {
        arg0.writeVarInt(this.friends.size());
        Object var2 = this.friends.iterator();
        while (var2.hasNext()) {
            Object var3 = ((String) var2.next());
            arg0.writeUtf(var3);
        }
    }

    public List getFriends() {
        return this.friends;
    }

}
