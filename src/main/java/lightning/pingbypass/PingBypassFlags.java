package lightning.pingbypass;

import lightning.LightningClient;

public class PingBypassFlags {
    public static volatile boolean tolerateRegistryErrors;
    public static volatile boolean suppressEncoderErrors;
    public static volatile boolean suppressAllDisconnects;
    public static volatile boolean proxyForwardingActive;
    public static volatile boolean clientOnGround;
    public static volatile boolean clientHorizontalCollision;

    public static boolean isPingBypassActive() {
        if ((!proxyForwardingActive) || (LightningClient.PINGBYPASS_CONFIG == null)) break label_0;
        return !(LightningClient.PINGBYPASS_CONFIG.isServer());
    }

    static {
        tolerateRegistryErrors = false;
        suppressEncoderErrors = false;
        suppressAllDisconnects = false;
        proxyForwardingActive = false;
        clientOnGround = true;
        clientHorizontalCollision = false;
    }

}
