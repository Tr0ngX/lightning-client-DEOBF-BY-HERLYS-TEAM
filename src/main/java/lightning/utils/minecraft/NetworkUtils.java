package lightning.utils.minecraft;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import lightning.mixins.accessors.ClientWorldAccessor;
import lightning.utils.IMinecraft;
import net.minecraft.network.protocol.Packet;

public class NetworkUtils implements IMinecraft {
    private static Object vfpImpl;
    private static Method vfpTargetVersion;
    private static Method protocolVersionGetVersion;

    public static void sendIgnoredPacket(Packet arg0) {
        mc.getConnection().getConnection().send(arg0, null, true);
    }

    public static void sendSequencedPacket(NetworkUtils.SequencedPacketCreator arg0) {
        sendSequencedPacket(arg0, (arg0) -> mc.getConnection().send(arg0));
    }

    public static void sendSequencedPacket(NetworkUtils.SequencedPacketCreator arg0, Consumer arg1) {
        Object var2 = ((ClientWorldAccessor) mc.level).invokeGetPendingUpdateManager().startPredicting();
        try {
            Object var3 = arg0.predict(var2.currentSequence());
            arg1.accept(var3);
            if (var2 == null) break label_7;
            var2.close();
        } catch (Throwable ex2) {
            var3 = ex2;
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

    public static boolean isLegacyProtocol() {
        try {
            if (vfpImpl == null) {
                vfpImpl = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus").getMethod("getImpl", new Class[0]).invoke(null, new Object[0]);
                vfpTargetVersion = Class.forName("com.viaversion.viafabricplus.api.ViaFabricPlusBase").getMethod("getTargetVersion", new Class[0]);
                protocolVersionGetVersion = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion").getMethod("getVersion", new Class[0]);
            }
            var tmp0 = vfpTargetVersion.invoke(vfpImpl, new Object[0]);
            Object var0 = tmp0;
            if (tmp0 == null) break label_2;
            return ((Integer) protocolVersionGetVersion.invoke(var0, new Object[0])).intValue() < 768;
        } catch (Throwable ex4) {
            var0 = ex4;
            return false;
        }
    }


    public interface SequencedPacketCreator {
        Packet predict(int arg0);

    }
}
