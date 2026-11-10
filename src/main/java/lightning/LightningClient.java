package lightning;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lightning.commands.CommandManager;
import lightning.events.EventHandler;
import lightning.gui.ClickGuiScreen;
import lightning.managers.AccountManager;
import lightning.managers.BoostManager;
import lightning.managers.ChatManager;
import lightning.managers.ConfigManager;
import lightning.managers.FontManager;
import lightning.managers.FriendManager;
import lightning.managers.ImageManager;
import lightning.managers.MacroManager;
import lightning.managers.NightUserManager;
import lightning.managers.PositionManager;
import lightning.managers.RenderManager;
import lightning.managers.RotationManager;
import lightning.managers.ServerManager;
import lightning.managers.TargetManager;
import lightning.managers.TaskManager;
import lightning.managers.WaypointManager;
import lightning.managers.WorldManager;
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

public class LightningClient implements ModInitializer {
    public static final String MOD_NAME = "Lightning";
    public static final String MOD_ID = "lightning";
    public static final String MOD_VERSION = "Beta";
    public static final String MINECRAFT_VERSION = "26.2";
    public static final String GIT_HASH = "eeeb439b78";
    public static final String GIT_REVISION = "436";
    public static final String BUILD_DATE = "2026-09-17";
    public static final long UPTIME;
    public static final EventHandler EVENT_HANDLER;
    public static final Logger LOGGER;
    private static final ExecutorService UPDATE_EXECUTOR;
    public static String UPDATE_STATUS;
    private static final String VERSION_URL = "";
    private static final String SECRET_KEY = "";
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
    public static final PbModuleManager PB_MODULE_MANAGER;

    public LightningClient() {
        BlockEntityInstanceTracker.initialize();
    }

    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");
        try {
            PayloadTypeRegistry.serverboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            PayloadTypeRegistry.clientboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(PbCustomPayload.ID, (arg0, arg1) -> {
});
        } catch (Throwable ex2) {
            Object var1 = ex2;
            LOGGER.warn("Fabric Networking API not available or optional: {}", var1.getMessage());
            try {
            }
            PINGBYPASS_CONFIG = new PingBypassConfig(FabricLoader.getInstance().getGameDir());
            PINGBYPASS_CONFIG.load();
            LOGGER.info("PingBypass mode: {}", (PINGBYPASS_CONFIG.isServer() ? "server" : "client"));
            if (!(PINGBYPASS_CONFIG.isServer())) break label_9;
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
            } catch (IOException ex8) {
                var1 = ex8;
                LOGGER.error("Failed to start PingBypass proxy server", var1);
            }
            label_9:
        } catch (Throwable ex10) {
            var1 = ex10;
            LOGGER.error("Failed to initialize PingBypass config/server", var1);
        }
        initManagers();
    }

    public static synchronized void initManagers() {
        if (CHAT_MANAGER != null) return;
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
        if (IMAGE_MANAGER == null) break label_0;
        var tmp0 = ImageManager.findSavedActiveImage();
        Object var0 = tmp0;
        if (((tmp0 == null) || (var0.equalsIgnoreCase("None"))) || (var0.isEmpty())) break label_0;
        IMAGE_MANAGER.setCurrentActiveImage(var0);
        label_0:
        LOGGER.info("{} {} has been initialized.", "Lightning", "Beta");
    }

    public static void checkForUpdates() {
        UPDATE_EXECUTOR.submit(() -> {
    if (FabricLoader.getInstance().isDevelopmentEnvironment()) return;
    if (!(FabricLoader.getInstance().isModLoaded("night-updater"))) {
        return;
        try {
        }
        Object var0 = ((HttpURLConnection) new URL("").openConnection());
        var0.setRequestMethod("GET");
        var0.connect();
        if (var0.getResponseCode() == 200) {
            Object var1 = new InputStreamReader(var0.getInputStream());
            Object var2 = JsonParser.parseReader(var1).getAsJsonObject();
            if (!(var2.has("version"))) {
                return;
                try {
                }
                if (!"Beta".equalsIgnoreCase(var2.get("version").getAsString())) {
                    UPDATE_STATUS = "update-available";
                }
            } else {
                UPDATE_STATUS = "failed-connection";
            }
        } catch (IOException ex7) {
            var0 = ex7;
            UPDATE_STATUS = "failed";
        }
        if (UPDATE_STATUS.equalsIgnoreCase("none")) {
            UPDATE_STATUS = "up-to-date";
        }
    }
});
    }

    static {
        UPTIME = System.currentTimeMillis();
        EVENT_HANDLER = new EventHandler();
        LOGGER = LoggerFactory.getLogger("Lightning");
        UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
        UPDATE_STATUS = "none";
        PB_MODULE_MANAGER = new PbModuleManager();
    }

}
