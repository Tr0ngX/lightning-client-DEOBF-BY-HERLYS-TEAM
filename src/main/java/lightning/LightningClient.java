package lightning;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lightning.commands.CommandManager;
import lightning.events.EventHandler;
import lightning.gui.ClickGuiScreen;
import lightning.managers.*;
import lightning.modules.ModuleManager;
import lightning.pingbypass.PingBypassConfig;
import lightning.pingbypass.modules.PbModuleManager;
import lightning.pingbypass.modules.submodules.crystal.ServerAutoCrystal;
import lightning.pingbypass.modules.submodules.totem.ServerAutoTotem;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.server.ProxyServer;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.pingbypass.server.TransferRehook;
import lightning.utils.BlockEntityInstanceTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purified Main Entrypoint (renamed from lightning.Night -> lightning.LightningClient).
 * Binary-patched 10-byte NOP sled and dead AuthGate references have been completely eradicated.
 */
public class LightningClient implements ModInitializer {
    public static final String MOD_NAME = "Lightning";
    public static final String MOD_ID = "lightning";
    public static final String MOD_VERSION = "Beta";
    public static final String MINECRAFT_VERSION = "26.2";
    public static final String GIT_HASH = "eeeb439b78";
    public static final String GIT_REVISION = "436";
    public static final String BUILD_DATE = "2026-09-17";

    public static final long UPTIME = System.currentTimeMillis();
    public static final EventHandler EVENT_HANDLER = new EventHandler();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    private static final ExecutorService UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
    public static String UPDATE_STATUS = "none";

    public static ChatManager CHAT_MANAGER;
    public static FontManager FONT_MANAGER;
    public static FriendManager FRIEND_MANAGER;
    public static WorldManager WORLD_MANAGER;
    public static PositionManager POSITION_MANAGER;
    public static RotationManager ROTATION_MANAGER;
    public static ServerManager SERVER_MANAGER;
    public static RenderManager RENDER_MANAGER;
    public static TargetManager TARGET_MANAGER;
    public static MacroManager MACRO_MANAGER;
    public static TaskManager TASK_MANAGER;
    public static WaypointManager WAYPOINT_MANAGER;
    public static BoostManager BOOST_MANAGER;
    public static NightUserManager NIGHT_USER_MANAGER;
    public static ImageManager IMAGE_MANAGER;
    public static ModuleManager MODULE_MANAGER;
    public static CommandManager COMMAND_MANAGER;
    public static ConfigManager CONFIG_MANAGER;
    public static AccountManager ACCOUNT_MANAGER;
    public static ClickGuiScreen CLICK_GUI;
    public static PingBypassConfig PINGBYPASS_CONFIG;
    public static ProxyServer PROXY_SERVER;
    public static final PbModuleManager PB_MODULE_MANAGER = new PbModuleManager();

    public LightningClient() {
        BlockEntityInstanceTracker.initialize();
    }

    @Override
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");

        try {
            PayloadTypeRegistry.serverboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            PayloadTypeRegistry.clientboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(PbCustomPayload.ID, (payload, context) -> {});
        } catch (Throwable t) {
            LOGGER.warn("Fabric Networking API not available or optional: {}", t.getMessage());
        }

        try {
            PINGBYPASS_CONFIG = new PingBypassConfig(FabricLoader.getInstance().getGameDir());
            PINGBYPASS_CONFIG.load();
            LOGGER.info("PingBypass mode: {}", PINGBYPASS_CONFIG.isServer() ? "server" : "client");

            if (PINGBYPASS_CONFIG.isServer()) {
                try {
                    PROXY_SERVER = new ProxyServer(PINGBYPASS_CONFIG);
                    PROXY_SERVER.bind(InetAddress.getByName(PINGBYPASS_CONFIG.getIp()), PINGBYPASS_CONFIG.getPort());
                    EVENT_HANDLER.subscribe(new ProxyServerTickListener(PROXY_SERVER));
                    new TransferRehook();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (PROXY_SERVER != null) {
                            PROXY_SERVER.shutdown();
                        }
                    }, "PingBypass-Shutdown"));
                    LOGGER.info("PingBypass proxy server started on {}:{}", PINGBYPASS_CONFIG.getIp(), PINGBYPASS_CONFIG.getPort());
                } catch (Exception e) {
                    LOGGER.error("Failed to start PingBypass proxy server", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize PingBypass config/server", e);
        }

        initManagers();
    }

    public static synchronized void initManagers() {
        if (CHAT_MANAGER != null) {
            return;
        }
        CHAT_MANAGER = new ChatManager();
        FONT_MANAGER = new FontManager();
        FRIEND_MANAGER = new FriendManager();
        WORLD_MANAGER = new WorldManager();
        POSITION_MANAGER = new PositionManager();
        ROTATION_MANAGER = new RotationManager();
        SERVER_MANAGER = new ServerManager();
        RENDER_MANAGER = new RenderManager();
        TARGET_MANAGER = new TargetManager();
        MACRO_MANAGER = new MacroManager();
        TASK_MANAGER = new TaskManager();
        WAYPOINT_MANAGER = new WaypointManager();
        BOOST_MANAGER = new BoostManager();
        NIGHT_USER_MANAGER = new NightUserManager();
        IMAGE_MANAGER = new ImageManager();
        MODULE_MANAGER = new ModuleManager();
        COMMAND_MANAGER = new CommandManager();
        ACCOUNT_MANAGER = new AccountManager();

        if (PROXY_SERVER != null) {
            PB_MODULE_MANAGER.register(new ServerAutoCrystal());
            PB_MODULE_MANAGER.register(new ServerAutoTotem());
        }
    }

    public static void onPostInitialize() {
        initManagers();
        CONFIG_MANAGER = new ConfigManager();
        CLICK_GUI = new ClickGuiScreen();
        if (IMAGE_MANAGER != null) {
            String activeImg = ImageManager.findSavedActiveImage();
            if (activeImg != null && !activeImg.equalsIgnoreCase("None") && !activeImg.isEmpty()) {
                IMAGE_MANAGER.setCurrentActiveImage(activeImg);
            }
        }
        LOGGER.info("{} {} has been initialized.", MOD_NAME, MOD_VERSION);
    }
}
