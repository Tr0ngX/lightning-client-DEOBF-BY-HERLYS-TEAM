package lightning.modules.impl.core;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.events.impl.SettingChangeEvent;
import lightning.events.impl.TickEvent;
import lightning.gui.HUDEditorScreen;
import lightning.managers.FontManager;
import lightning.managers.HudElementRegistry;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.PositionSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.text.FormattingUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

@RegisterModule(name = "HUD", description = "Renders information about the game and the client on the screen.", category = Module.Category.CORE, toggled = true, drawn = false)
public class HUDModule extends Module {
    private static final Identifier LOGO_ID;
    private static final int LOGO_SIZE = 64;
    public CategorySetting watermarkCategory;
    public BooleanSetting watermark;
    public StringSetting watermarkText;
    public BooleanSetting watermarkLogo;
    public BooleanSetting watermarkVersion;
    public BooleanSetting watermarkMinecraftVersion;
    public ModeSetting watermarkVersionMode;
    public BooleanSetting watermarkSync;
    public CategorySetting welcomerCategory;
    public BooleanSetting welcomer;
    public StringSetting welcomerText;
    public BooleanSetting welcomerSync;
    public CategorySetting moduleListCategory;
    public BooleanSetting moduleList;
    public ModeSetting moduleListMode;
    public BooleanSetting metaData;
    public BooleanSetting bindOnly;
    public ModeSetting moduleColorMode;
    public ModeSetting moduleListSorting;
    public CategorySetting playerRadarCategory;
    public BooleanSetting playerRadar;
    public NumberSetting playerRadarLimit;
    public ModeSetting playerRadarSorting;
    public BooleanSetting playerRadarAntiBot;
    public BooleanSetting playerIcons;
    public BooleanSetting playerRadarDistance;
    public BooleanSetting playerRadarEntityID;
    public BooleanSetting playerRadarGameMode;
    public BooleanSetting playerRadarPing;
    public BooleanSetting playerRadarHealth;
    public BooleanSetting playerRadarTotems;
    public BooleanSetting playerRadarTurtlePot;
    public CategorySetting itemsCategory;
    public BooleanSetting armor;
    public ModeSetting armorDurability;
    public BooleanSetting totemCounter;
    public BooleanSetting crystalCounter;
    public BooleanSetting xpCounter;
    public BooleanSetting counterChatOffset;
    public BooleanSetting itemsSmooth;
    public CategorySetting inventoryCategory;
    public BooleanSetting inventory;
    public BooleanSetting inventoryBackground;
    public BooleanSetting inventorySlotBackground;
    public NumberSetting inventoryAlpha;
    public PositionSetting inventoryPosition;
    public CategorySetting informationCategory;
    public BooleanSetting health;
    public BooleanSetting ping;
    public BooleanSetting tps;
    public BooleanSetting fps;
    public BooleanSetting durability;
    public ModeSetting speed;
    public BooleanSetting uptime;
    public BooleanSetting serverBrand;
    public BooleanSetting informationSync;
    public BooleanSetting informationChatOffset;
    public CategorySetting potionsCategory;
    public BooleanSetting potions;
    public BooleanSetting potionIcons;
    public ModeSetting potionColor;
    public ModeSetting potionSorting;
    public ModeSetting vanillaPotions;
    public CategorySetting positionCategory;
    public BooleanSetting coordinates;
    public BooleanSetting netherCoordinates;
    public BooleanSetting fakeCoords;
    public NumberSetting fakeThreshold;
    public BooleanSetting direction;
    public BooleanSetting positionSync;
    public BooleanSetting positionChatOffset;
    public CategorySetting colorCategory;
    public ModeSetting colorMode;
    public ColorSetting customColor;
    public ModeSetting rainbowMode;
    public NumberSetting rainbowOffset;
    public BooleanSetting inversion;
    public BooleanSetting textGlow;
    public BooleanSetting informationElement;
    public PositionSetting watermarkPosition;
    public PositionSetting welcomerPosition;
    public PositionSetting moduleListPosition;
    public PositionSetting playerRadarPosition;
    public PositionSetting armorPosition;
    public PositionSetting totemCounterPosition;
    public PositionSetting crystalCounterPosition;
    public PositionSetting xpCounterPosition;
    public PositionSetting informationPosition;
    public PositionSetting coordinatesPosition;
    public PositionSetting potionsPosition;
    public PositionSetting musicHudPosition;
    public CategorySetting musicHudCategory;
    public BooleanSetting musicHud;
    public NumberSetting musicHudBarAlpha;
    public BooleanSetting musicHudGradientBars;
    public ModeSetting musicHudBgMode;
    public NumberSetting musicHudBlurIntensity;
    public BooleanSetting musicHudCompactMode;
    public BooleanSetting musicHudDisk;
    public BooleanSetting musicHudUltraDisk;
    public NumberSetting musicHudDiskSize;
    public BooleanSetting musicHudTextBloom;
    public BooleanSetting musicHudTextBloomPlus;
    public ColorSetting musicHudColor;
    public ModeSetting musicHudButtonEffect;
    public static HUDModule INSTANCE;
    private final HUDModule.SmoothCounter totemSmoothCounter;
    private final HUDModule.SmoothCounter crystalSmoothCounter;
    private final HUDModule.SmoothCounter xpSmoothCounter;
    private final Animation potionsAnimation;
    private final Animation chatAnimation;
    private float chatOffset;
    private List moduleEntries;
    private List playerEntries;
    private List potionEntries;
    private final int fakeOffsetX;
    private final int fakeOffsetZ;
    private static final EquipmentSlot[] ARMOR_SLOTS;
    private static ItemStack[] dummyArmor;
    private final Map moduleColorAnimations;

    public static boolean isMusicAddonLoaded() {
        if (!(FabricLoader.getInstance().isModLoaded("night-music"))) {
            if (LightningClient.MODULE_MANAGER != null && LightningClient.MODULE_MANAGER.getModule("PlayMusic") != null) {
            }
        } else {
        }
        return false;
    }

    public HUDModule() {
        this.watermarkCategory = new CategorySetting("Watermark", "The settings for the client's watermark.");
        this.watermark = new BooleanSetting("Watermark", "Enabled", "Renders the client's name and version at the top left.", new CategorySetting.Visibility(this.watermarkCategory), true);
        this.watermarkText = new StringSetting("WatermarkText", "Component", "The client name that will be rendered.", new CategorySetting.Visibility(this.watermarkCategory), "LightningClient");
        this.watermarkLogo = new BooleanSetting("WatermarkLogo", "Logo", "Renders the client's logo before the watermark text, scaled to the text's own line height.", new CategorySetting.Visibility(this.watermarkCategory), true);
        this.watermarkVersion = new BooleanSetting("WatermarkVersion", "Version", "Renders the client's version after the name.", new CategorySetting.Visibility(this.watermarkCategory), true);
        this.watermarkMinecraftVersion = new BooleanSetting("WatermarkMinecraftVersion", "MinecraftVersion", "Renders the client's minecraft version after the version.", new CategorySetting.Visibility(this.watermarkCategory), false);
        var tmp0 = new String[2];
        tmp0[0] = "GitHash";
        tmp0[1] = "Nightly";
        this.watermarkVersionMode = new ModeSetting("WatermarkVersionMode", "Revision", "What to render next to the version: the git revision/hash, or a nightly-build date stamp.", new BooleanSetting.Visibility(this.watermarkVersion, true), "GitHash", tmp0);
        this.watermarkSync = new BooleanSetting("WatermarkSync", "ColorSync", "Uses the client's color for the version.", new CategorySetting.Visibility(this.watermarkCategory), false);
        this.welcomerCategory = new CategorySetting("Welcomer", "The settings for the client's welcomer.");
        this.welcomer = new BooleanSetting("Welcomer", "Enabled", "Renders a nice welcome message directed to you.", new CategorySetting.Visibility(this.welcomerCategory), true);
        this.welcomerText = new StringSetting("WelcomerText", "Component", "The message that will be rendered.", new CategorySetting.Visibility(this.welcomerCategory), "Welcome back, [username]");
        this.welcomerSync = new BooleanSetting("WelcomerSync", "ColorSync", "Uses the client's color for the username.", new CategorySetting.Visibility(this.welcomerCategory), false);
        this.moduleListCategory = new CategorySetting("ModuleList", "The settings for the client's list of enabled modules.");
        this.moduleList = new BooleanSetting("ModuleList", "Enabled", "Renders every enabled module in an organized list.", new CategorySetting.Visibility(this.moduleListCategory), true);
        var tmp1 = new String[2];
        tmp1[0] = "Pro";
        tmp1[1] = "Newbie";
        this.moduleListMode = new ModeSetting("ModuleListMode", "Mode", "The display mode for the module list.", new CategorySetting.Visibility(this.moduleListCategory), "Pro", tmp1);
        this.metaData = new BooleanSetting("MetaData", "Whether or not to show module metadata in the module list.", new CategorySetting.Visibility(this.moduleListCategory), true);
        this.bindOnly = new BooleanSetting("BindOnly", "Only lists modules that have a keybind set.", new CategorySetting.Visibility(this.moduleListCategory), false);
        var tmp2 = new String[3];
        tmp2[0] = "Default";
        tmp2[1] = "Rainbow";
        tmp2[2] = "Random";
        this.moduleColorMode = new ModeSetting("ModuleColor", "Color", "The color mode for the modules on the list.", new CategorySetting.Visibility(this.moduleListCategory), "Default", tmp2);
        var tmp3 = new String[2];
        tmp3[0] = "Width";
        tmp3[1] = "Alphabetical";
        this.moduleListSorting = new ModeSetting("ModuleListSorting", "Sorting", "The sorting for the modules on the list.", new CategorySetting.Visibility(this.moduleListCategory), "Width", tmp3);
        this.playerRadarCategory = new CategorySetting("Player Radar", "The settings for the client's list of players in render distance.");
        this.playerRadar = new BooleanSetting("PlayerRadar", "Enabled", "Renders the name of every player in render distance.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarLimit = new NumberSetting("PlayerRadarLimit", "Limit", "The maximum amount of players that will be listed. Setting it to 0 means removing the limiter.", new CategorySetting.Visibility(this.playerRadarCategory), 8, 0, 100);
        var tmp4 = new String[3];
        tmp4[0] = "None";
        tmp4[1] = "Distance";
        tmp4[2] = "Alphabetical";
        this.playerRadarSorting = new ModeSetting("PlayerRadarSorting", "Sorting", "The sorting for the players on the radar.", new CategorySetting.Visibility(this.playerRadarCategory), "Distance", tmp4);
        this.playerRadarAntiBot = new BooleanSetting("PlayerRadarAntiBot", "AntiBot", "Prevents bots from being listed on the radar.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerIcons = new BooleanSetting("PlayerIcons", "Icons", "Renders the player's head icon next to their name.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarDistance = new BooleanSetting("PlayerRadarDistance", "Distance", "Renders the distance between you and the player.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarEntityID = new BooleanSetting("PlayerRadarEntityID", "EntityID", "Renders the player's entity ID.", new CategorySetting.Visibility(this.playerRadarCategory), false);
        this.playerRadarGameMode = new BooleanSetting("PlayerRadarGameMode", "GameType", "Renders the player's current gamemode.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarPing = new BooleanSetting("PlayerRadarPing", "Ping", "Renders the player's current latency.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarHealth = new BooleanSetting("PlayerRadarHealth", "Health", "Renders the player's current health.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarTotems = new BooleanSetting("PlayerRadarTotems", "Totems", "Renders the amount of totems that the player has popped.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.playerRadarTurtlePot = new BooleanSetting("PlayerRadarTurtlePot", "TurtlePotIndicator", "Indicates if the player has Resistance 3 or higher.", new CategorySetting.Visibility(this.playerRadarCategory), true);
        this.itemsCategory = new CategorySetting("Items", "The settings for information about items in your inventory and specific item counters.");
        this.armor = new BooleanSetting("Armor", "Renders the armor you're currently wearing and its status.", new CategorySetting.Visibility(this.itemsCategory), true);
        var tmp5 = new String[4];
        tmp5[0] = "None";
        tmp5[1] = "Bar";
        tmp5[2] = "Percentage";
        tmp5[3] = "Both";
        this.armorDurability = new ModeSetting("ArmorDurability", "Durability", "The way that the durability will be rendered in.", new BooleanSetting.Visibility(this.armor, true), "Both", tmp5);
        this.totemCounter = new BooleanSetting("TotemCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
        this.crystalCounter = new BooleanSetting("CrystalCounter", "Renders the amount of crystals that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
        this.xpCounter = new BooleanSetting("XPCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.Visibility(this.itemsCategory), true);
        this.counterChatOffset = new BooleanSetting("CounterChatOffset", "ChatOffset", "Offsets the crystal and XP counter's positions whenever the chat is open.", new CategorySetting.Visibility(this.itemsCategory), false);
        this.itemsSmooth = new BooleanSetting("ItemsSmooth", "Smooth", "Smoothly animates item counts when they change (sliding/lerping).", new CategorySetting.Visibility(this.itemsCategory), true);
        this.inventoryCategory = new CategorySetting("Inventory", "The settings for displaying the inventory on the HUD.");
        this.inventory = new BooleanSetting("Inventory", "Enabled", "Renders your inventory (excluding hotbar) on the screen even when closed.", new CategorySetting.Visibility(this.inventoryCategory), false);
        this.inventoryBackground = new BooleanSetting("InventoryBackground", "Background", "Renders a dark background behind the inventory grid.", new CategorySetting.Visibility(this.inventoryCategory), true);
        this.inventorySlotBackground = new BooleanSetting("InventorySlotBackground", "SlotBoxes", "Renders subtle slot boxes for each inventory slot.", new CategorySetting.Visibility(this.inventoryCategory), true);
        this.inventoryAlpha = new NumberSetting("InventoryAlpha", "Alpha", "Background opacity (0-255).", new CategorySetting.Visibility(this.inventoryCategory), 140.0, 0.0, 255.0);
        this.inventoryPosition = new PositionSetting("InventoryPosition", "Drag offset for the inventory HUD element.");
        this.informationCategory = new CategorySetting("Information", "The settings for information about the game and the client.");
        this.health = new BooleanSetting("Health", "Renders your current health in the middle of the screen.", new CategorySetting.Visibility(this.informationCategory), false);
        this.ping = new BooleanSetting("Ping", "Renders your current latency to the server in milliseconds.", new CategorySetting.Visibility(this.informationCategory), true);
        this.tps = new BooleanSetting("TPS", "Renders the server's current tick-rate.", new CategorySetting.Visibility(this.informationCategory), true);
        this.fps = new BooleanSetting("FPS", "Renders the game's frames per second counter.", new CategorySetting.Visibility(this.informationCategory), true);
        this.durability = new BooleanSetting("Durability", "Renders your held item durability.", new CategorySetting.Visibility(this.informationCategory), true);
        var tmp6 = new String[3];
        tmp6[0] = "None";
        tmp6[1] = "Meters";
        tmp6[2] = "Kilometers";
        this.speed = new ModeSetting("Speed", "Renders the speed that you are currently moving at.", new CategorySetting.Visibility(this.informationCategory), "Kilometers", tmp6);
        this.uptime = new BooleanSetting("Uptime", "Renders the uptime of the client.", new CategorySetting.Visibility(this.informationCategory), false);
        this.serverBrand = new BooleanSetting("ServerBrand", "Renders the brand of the server that you are currently on.", new CategorySetting.Visibility(this.informationCategory), false);
        this.informationSync = new BooleanSetting("InformationSync", "ColorSync", "Uses the client's color for the information elements.", new CategorySetting.Visibility(this.informationCategory), true);
        this.informationChatOffset = new BooleanSetting("InformationChatOffset", "ChatOffset", "Offsets the rendering when the chat is open.", new CategorySetting.Visibility(this.informationCategory), true);
        this.potionsCategory = new CategorySetting("Potions", "The settings for information about potion effects and their status.");
        this.potions = new BooleanSetting("Potions", "Enabled", "Renders the name and status of every potion effect you have.", new CategorySetting.Visibility(this.potionsCategory), true);
        this.potionIcons = new BooleanSetting("PotionIcons", "Icons", "Whether or not to render the icons next to the potion's name.", new CategorySetting.Visibility(this.potionsCategory), true);
        var tmp7 = new String[3];
        tmp7[0] = "Vanilla";
        tmp7[1] = "Enhanced";
        tmp7[2] = "Client";
        this.potionColor = new ModeSetting("PotionColor", "Color", "The color that will be used in rendering the potion's text.", new CategorySetting.Visibility(this.potionsCategory), "Enhanced", tmp7);
        var tmp8 = new String[3];
        tmp8[0] = "None";
        tmp8[1] = "Width";
        tmp8[2] = "Alphabetical";
        this.potionSorting = new ModeSetting("PotionSorting", "Sorting", "The sorting for the potion effects rendered.", new CategorySetting.Visibility(this.potionsCategory), "Alphabetical", tmp8);
        var tmp9 = new String[3];
        tmp9[0] = "Keep";
        tmp9[1] = "Move";
        tmp9[2] = "Hide";
        this.vanillaPotions = new ModeSetting("VanillaPotions", "The way that the vanilla potion icons will be handled.", new CategorySetting.Visibility(this.potionsCategory), "Hide", tmp9);
        this.positionCategory = new CategorySetting("Position", "The settings for information about your current position and velocity.");
        this.coordinates = new BooleanSetting("Coordinates", "Renders your current coordinates.", new CategorySetting.Visibility(this.positionCategory), true);
        this.netherCoordinates = new BooleanSetting("NetherCoordinates", "Renders your current coordinates in the alternate dimension.", new BooleanSetting.Visibility(this.coordinates, true), true);
        this.fakeCoords = new BooleanSetting("FakeCoords", "Fakes your coordinates to prevent leaking.", new CategorySetting.Visibility(this.positionCategory), false);
        this.fakeThreshold = new NumberSetting("FakeThreshold", "Threshold", "Minimum coordinate value required to start faking.", new BooleanSetting.Visibility(this.fakeCoords, true), 1000, 0, 100000);
        this.direction = new BooleanSetting("Direction", "Renders the current direction that you are facing.", new CategorySetting.Visibility(this.positionCategory), true);
        this.positionSync = new BooleanSetting("PositionSync", "ColorSync", "Uses the client's color for the position elements.", new CategorySetting.Visibility(this.positionCategory), true);
        this.positionChatOffset = new BooleanSetting("PositionChatOffset", "ChatOffset", "Offsets the text when the chat is open.", new CategorySetting.Visibility(this.positionCategory), true);
        this.colorCategory = new CategorySetting("Color", "The settings for the coloring of the text.");
        var tmp10 = new String[4];
        tmp10[0] = "Default";
        tmp10[1] = "Rainbow";
        tmp10[2] = "Wave";
        tmp10[3] = "Custom";
        this.colorMode = new ModeSetting("Color", "The color that will be applied to the text.", new CategorySetting.Visibility(this.colorCategory), "Default", tmp10);
        var tmp11 = new String[1];
        tmp11[0] = "Custom";
        this.customColor = new ColorSetting("CustomColor", "The color that will be used for the Custom mode.", new ModeSetting.Visibility(this.colorMode, tmp11), ColorUtils.getDefaultColor());
        var tmp12 = new String[1];
        tmp12[0] = "Rainbow";
        var tmp13 = new String[2];
        tmp13[0] = "Vertical";
        tmp13[1] = "Horizontal";
        this.rainbowMode = new ModeSetting("Rainbow", "The mode for the HUD Rainbow.", new ModeSetting.Visibility(this.colorMode, tmp12), "Vertical", tmp13);
        var tmp14 = new String[2];
        tmp14[0] = "Rainbow";
        tmp14[1] = "Wave";
        this.rainbowOffset = new NumberSetting("RainbowOffset", "Offset", "The offset that will be applied to the rainbow.", new ModeSetting.Visibility(this.colorMode, tmp14), 10L, 1L, 50L);
        this.inversion = new BooleanSetting("Inversion", "Inverts primary and secondary colors.", new CategorySetting.Visibility(this.colorCategory), false);
        this.textGlow = new BooleanSetting("TextGlow", "Glow", "Renders a bloom glow effect for HUD text.", new CategorySetting.Visibility(this.colorCategory), false);
        this.informationElement = new BooleanSetting("InformationElement", "Whether the ping/fps/tps/etc information block is shown at all. See HUDEditor.", true);
        this.watermarkPosition = new PositionSetting("WatermarkPosition", "Drag offset for the watermark HUD element.");
        this.welcomerPosition = new PositionSetting("WelcomerPosition", "Drag offset for the welcomer HUD element.");
        this.moduleListPosition = new PositionSetting("ModuleListPosition", "Drag offset for the module list HUD element.");
        this.playerRadarPosition = new PositionSetting("PlayerRadarPosition", "Drag offset for the player radar HUD element.");
        this.armorPosition = new PositionSetting("ArmorPosition", "Drag offset for the armor HUD element.");
        this.totemCounterPosition = new PositionSetting("TotemCounterPosition", "Drag offset for the totem counter HUD element.");
        this.crystalCounterPosition = new PositionSetting("CrystalCounterPosition", "Drag offset for the crystal counter HUD element.");
        this.xpCounterPosition = new PositionSetting("XPCounterPosition", "Drag offset for the XP counter HUD element.");
        this.informationPosition = new PositionSetting("InformationPosition", "Drag offset for the information HUD element.");
        this.coordinatesPosition = new PositionSetting("CoordinatesPosition", "Drag offset for the coordinates HUD element.");
        this.potionsPosition = new PositionSetting("PotionsPosition", "Drag offset for the potions HUD element.");
        this.musicHudPosition = new PositionSetting("MusicHUDPosition", "Drag offset for the Music HUD element.");
        this.musicHudCategory = new CategorySetting("MusicHUD", "Settings for the Music HUD.");
        this.musicHud = new BooleanSetting("MusicHUD", "Enabled", "Toggle Music HUD display.", new CategorySetting.Visibility(this.musicHudCategory), true);
        this.musicHudBarAlpha = new NumberSetting("MusicHudBarAlpha", "BarAlpha", "Transparency of the visualizer bars (0-255).", new CategorySetting.Visibility(this.musicHudCategory), 160.0, 0.0, 255.0);
        this.musicHudGradientBars = new BooleanSetting("MusicHudGradientBars", "GradientBars", "Gradient colored visualizer bars.", new CategorySetting.Visibility(this.musicHudCategory), true);
        var tmp15 = new String[3];
        tmp15[0] = "LiquidGlass";
        tmp15[1] = "Blur";
        tmp15[2] = "Default";
        this.musicHudBgMode = new ModeSetting("MusicHudBackground", "Background", "Background style of the HUD.", new CategorySetting.Visibility(this.musicHudCategory), "LiquidGlass", tmp15);
        this.musicHudBlurIntensity = new NumberSetting("MusicHudBlurIntensity", "BlurIntensity", "Intensity of the blur effect.", new CategorySetting.Visibility(this.musicHudCategory), 3.0, 0.0, 50.0);
        this.musicHudCompactMode = new BooleanSetting("MusicHudCompactMode", "CompactMode", "Collapse the HUD, limiting its width.", new CategorySetting.Visibility(this.musicHudCategory), false);
        this.musicHudDisk = new BooleanSetting("MusicHudDisk", "Disk", "Enable to show the spinning record.", new CategorySetting.Visibility(this.musicHudCategory), true);
        this.musicHudUltraDisk = new BooleanSetting("MusicHudUltraDisk", "UltraDisk", "Only display the record, hide everything else.", new CategorySetting.Visibility(this.musicHudCategory), false);
        this.musicHudDiskSize = new NumberSetting("MusicHudDiskSize", "DiskSize", "Size of the music disk (Ultra Disk).", new CategorySetting.Visibility(this.musicHudCategory), 100.0, 30.0, 300.0);
        this.musicHudTextBloom = new BooleanSetting("MusicHudTextBloom", "TextBloom", "Text bloom glow effect.", new CategorySetting.Visibility(this.musicHudCategory), true);
        this.musicHudTextBloomPlus = new BooleanSetting("MusicHudTextBloomPlus", "TextBloomPlus", "Enhanced Skia text bloom glow.", new CategorySetting.Visibility(this.musicHudCategory), true);
        this.musicHudColor = new ColorSetting("MusicHudColor", "Color", "Accent color used for the visualizer.", new CategorySetting.Visibility(this.musicHudCategory), ColorUtils.getDefaultColor());
        var tmp16 = new String[3];
        tmp16[0] = "Splash";
        tmp16[1] = "Topo";
        tmp16[2] = "Gradient";
        this.musicHudButtonEffect = new ModeSetting("MusicHudButtonEffect", "ButtonEffect", "Visual effect when clicking buttons.", new CategorySetting.Visibility(this.musicHudCategory), "Gradient", tmp16);
        this.totemSmoothCounter = new HUDModule.SmoothCounter();
        this.crystalSmoothCounter = new HUDModule.SmoothCounter();
        this.xpSmoothCounter = new HUDModule.SmoothCounter();
        this.potionsAnimation = new Animation(300, Easing.Method.EASE_OUT_CUBIC);
        this.chatAnimation = new Animation(300, Easing.Method.EASE_OUT_CUBIC);
        this.moduleEntries = new ArrayList();
        this.playerEntries = new ArrayList();
        this.potionEntries = new ArrayList();
        this.fakeOffsetX = (2000000 + new Random().nextInt(8000000));
        this.fakeOffsetZ = (2000000 + new Random().nextInt(8000000));
        this.moduleColorAnimations = new HashMap();
        INSTANCE = this;
        HudElementRegistry.register("Watermark", this.watermark, this.watermarkPosition, this.watermarkCategory);
        HudElementRegistry.register("Welcomer", this.welcomer, this.welcomerPosition, this.welcomerCategory);
        HudElementRegistry.register("ModuleList", this.moduleList, this.moduleListPosition, this.moduleListCategory);
        HudElementRegistry.register("PlayerRadar", this.playerRadar, this.playerRadarPosition, this.playerRadarCategory);
        HudElementRegistry.register("Armor", this.armor, this.armorPosition);
        HudElementRegistry.register("TotemCounter", this.totemCounter, this.totemCounterPosition);
        HudElementRegistry.register("CrystalCounter", this.crystalCounter, this.crystalCounterPosition);
        HudElementRegistry.register("XPCounter", this.xpCounter, this.xpCounterPosition);
        HudElementRegistry.register("Inventory", this.inventory, this.inventoryPosition, this.inventoryCategory);
        HudElementRegistry.register("Information", this.informationElement, this.informationPosition, this.informationCategory);
        HudElementRegistry.register("Coordinates", this.coordinates, this.coordinatesPosition, this.positionCategory);
        HudElementRegistry.register("Potions", this.potions, this.potionsPosition, this.potionsCategory);
        this.musicHudPosition.set(20.0f, 60.0f);
        this.inventoryPosition.set(20.0f, 120.0f);
    }

    private int getFakedCoord(int arg0, int arg1) {
        if (!(this.fakeCoords.getValue())) return arg0;
        if (Math.abs(arg0) < this.fakeThreshold.getValue().intValue()) return arg0;
        long var5 = (((long) arg1) + (((long) Math.abs(arg0)) / 3L));
        int var7 = ((((Math.abs(arg0) * 31) + arg1) % 2) == 0 ? 1 : 0);
        if (!(!var7)) {
        } else {
        }
        long var3 = var5;
        long var8 = var5;
        if (mc.player != null && mc.player.level() != null && mc.player.level().dimension() == Level.NETHER) {
            var3 = (var3 / 8L);
        }
        return ((int) var3);
    }

    @SubscribeEvent
    public void onSettingChange(SettingChangeEvent arg0) {
        if (arg0.getSetting() != this.textGlow) break label_2;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        Object var3 = null;
        if ((var2 == null) || (var2.glow.getValue() == this.textGlow.getValue())) break label_2;
        var2.glow.setValue(this.textGlow.getValue());
        label_2:
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) break label_28;
        if (this.moduleList.getValue()) {
            Object var2 = Comparator.comparingInt(() -> -(LightningClient.FONT_MANAGER.getWidth(HudListStyle.moduleText(this, arg0))));
            Object var3 = Comparator.comparing(Module::getName);
            int var4 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
            Object var5 = new ArrayList();
            Object var6 = LightningClient.MODULE_MANAGER.getModules().stream().filter((arg0) -> arg0.drawn.getValue()).filter(() -> {
    if (!(!arg0)) {
        if (arg1.getBind()) {
            break label_3;
        }
        if (this.bindOnly.getValue()) break label_2;
        if (!(arg1.isToggled())) {
            if (arg1.getAnimationOffset().value() <= 0.01f) break label_2;
        }
        break label_3;
        label_2:
        label_3:
        return false;
    }
    if (this.bindOnly.getValue() && !(arg1.getBind())) {
        break label_8;
    }
    if (!(arg1.isToggled())) {
        if (arg1.getAnimationOffset().value() > 0.01f) {
        }
        break label_8;
    }
    label_8:
    return false;
}).sorted((this.moduleListSorting.getValue().equalsIgnoreCase("Width") ? var2 : var3)).toList().iterator();
            while (var6.hasNext()) {
                Object var7 = ((Module) var6.next());
                Object var8 = HudListStyle.moduleText(this, var7);
                var5.add(new HUDModule.ModuleEntry(var7, var8));
            }
            this.moduleEntries = var5;
        }
        if (this.playerRadar.getValue()) {
            var2 = Comparator.comparingDouble((arg0) -> ((double) mc.player.distanceTo(arg0)));
            var3 = Comparator.comparing((arg0) -> arg0.getName().getString());
            var4 = new ArrayList();
            var5 = mc.level.players().stream().filter((arg0) -> arg0 != mc.player).filter((arg0) -> !(EntityUtils.isGhost(arg0))).filter(() -> {
    if (this.playerRadarAntiBot.getValue()) {
        if (!(EntityUtils.isBot(arg0))) {
        }
    } else {
    }
    return false;
}).filter((arg0) -> {
    if (((IgnoreNakedModule) LightningClient.MODULE_MANAGER.getModule(IgnoreNakedModule.class)).isToggled()) {
        if (!(EntityUtils.isNaked(arg0))) {
        }
    } else {
    }
    return false;
}).sorted((this.playerRadarSorting.getValue().equalsIgnoreCase("Distance") ? var2 : var3)).limit(this.playerRadarLimit.getValue().longValue()).toList().iterator();
            while (var5.hasNext()) {
                var6 = ((Player) var5.next());
                var7 = null;
                if ((!(this.playerIcons.getValue())) || (mc.getConnection() == null)) break label_8;
                var8 = mc.getConnection().getPlayerInfo(var6.getName().getString());
                if (var8 == null) break label_8;
                var7 = var8.getSkin().body().texturePath();
                label_8:
                var8 = var6.getName().getString();
                if (this.playerRadarDistance.getValue()) {
                    var8 = (var8 + String.valueOf(ChatFormatting.WHITE) + " " + new DecimalFormat("0.0").format(((double) mc.player.distanceTo(var6))));
                }
                if (this.playerRadarEntityID.getValue()) {
                    var8 = (var8 + String.valueOf(ChatFormatting.WHITE) + " " + var6.getId());
                }
                if (this.playerRadarGameMode.getValue()) {
                    var8 = (var8 + String.valueOf(ChatFormatting.WHITE) + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(var6)) + "]");
                }
                if (this.playerRadarPing.getValue()) {
                    var8 = (var8 + String.valueOf(ChatFormatting.WHITE) + " " + EntityUtils.getLatency(var6) + "ms");
                }
                if (this.playerRadarHealth.getValue()) {
                    var8 = (var8 + String.valueOf(ColorUtils.getHealthColor(((double) (var6.getHealth() + var6.getAbsorptionAmount())))) + " " + new DecimalFormat("0.0").format(((double) (var6.getHealth() + var6.getAbsorptionAmount()))) + String.valueOf(ChatFormatting.RESET));
                }
                int var9 = ((Integer) LightningClient.WORLD_MANAGER.getPoppedTotems().getOrDefault(var6.getUUID(), 0)).intValue();
                if (this.playerRadarTotems.getValue() && var9 > 0) {
                    var8 = (var8 + String.valueOf(ColorUtils.getTotemColor(var9)) + " -" + var9 + String.valueOf(ChatFormatting.RESET));
                }
                if (!(this.playerRadarTurtlePot.getValue())) break label_15;
                Object var10 = var6.getEffect(MobEffects.RESISTANCE);
                if ((var10 == null) || (var10.getAmplifier() < 2)) break label_15;
                var8 = (var8 + String.valueOf(ChatFormatting.RED) + " [TM]" + String.valueOf(ChatFormatting.RESET));
                label_15:
                var4.add(new HUDModule.PlayerEntry(var6, var8, var7));
            }
            this.playerEntries = var4;
        }
        if (!(this.potions.getValue())) break label_28;
        var2 = Comparator.comparingInt((arg0) -> -(LightningClient.FONT_MANAGER.getWidth((((MobEffect) arg0.getEffect().value()).getDisplayName().getString() + " " + (arg0.getAmplifier() + 1)))));
        var3 = Comparator.comparing((arg0) -> ((MobEffect) arg0.getEffect().value()).getDisplayName().getString());
        var4 = new ArrayList();
        var5 = mc.player.getActiveEffects().stream().sorted((this.potionSorting.getValue().equalsIgnoreCase("Width") ? var2 : var3)).toList().iterator();
        while (var5.hasNext()) {
            var6 = ((MobEffectInstance) var5.next());
            var7 = getPotionText(var6);
            var8 = null;
            if (this.potionIcons.getValue()) {
                var8 = Hud.getMobEffectSprite(var6.getEffect());
            }
            if (this.potionColor.getValue().equalsIgnoreCase("Vanilla")) {
                break label_26;
            }
            if (this.potionColor.getValue().equalsIgnoreCase("Enhanced")) {
                if (EntityUtils.POTION_COLORS.containsKey(var6.getEffect().value())) {
                    break label_26;
                }
                break label_26;
            }
            label_26:
            new HUDModule.PotionEntry(((Color) EntityUtils.POTION_COLORS.get(var6.getEffect().value())), new Color(((MobEffect) var6.getEffect().value()).getColor()), null);
            var7.add(var8);
        }
        this.potionEntries = var4;
        label_28:
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        FontManager.setHudRendering(true);
        this.chatOffset = this.chatAnimation.get(((mc.gui.screen() instanceof ChatScreen) ? 14.0f : 0.0f));
        Renderer2D.renderQuad(arg0.getContext(), 2.0f, (((float) mc.getWindow().getGuiScaledHeight()) - this.chatOffset), ((float) (mc.getWindow().getGuiScaledWidth() - 2)), (((float) (mc.getWindow().getGuiScaledHeight() + 12)) - this.chatOffset), new Color(0, 0, 0, ((int) (((Double) mc.options.textBackgroundOpacity().get()).doubleValue() * 255.0))));
        renderWatermark(arg0);
        renderModuleList(arg0);
        renderPlayerRadar(arg0);
        renderItems(arg0);
        renderInventory(arg0);
        renderPotions(arg0);
        renderInformation(arg0);
        renderCoordinates(arg0);
        FontManager.setHudRendering(false);
        break label_6;
        Object var2 = null;
        FontManager.setHudRendering(false);
        throw var2;
        label_6:
    }

    private void renderWatermark(RenderOverlayEvent arg0) {
        Object var2 = arg0.getMatrices();
        if (!(this.watermark.getValue())) {
            if (this.uptime.getValue()) {
            }
            var2.pushMatrix();
            var2.translate(this.watermarkPosition.getX(), this.watermarkPosition.getY());
            int var3 = 0;
            int var4 = 0;
            if (this.watermark.getValue()) {
                int var6 = 1;
                var tmp0 = (!(!var6) ? "" : "26");
                Object var5 = tmp0;
                Object var7 = tmp0;
                if (!(!var6)) {
                    if (!(this.watermarkVersion.getValue())) {
                    }
                    break label_6;
                }
                if (!(this.watermarkVersionMode.getValue().equals("GitHash"))) {
                    break label_6;
                }
                label_6:
                Object var8 = "+436.eeeb439b78";
                if (this.watermarkVersion.getValue()) {
                    if (this.watermarkSync.getValue()) {
                        break label_9;
                    }
                } else {
                }
                Object var9 = ((String.valueOf((this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + " " + var5 + (this.watermarkMinecraftVersion.getValue() ? "-mc26.2" : "") + var8) + "");
                float var10 = 2.0f;
                if (this.watermarkLogo.getValue()) {
                    int var11 = LightningClient.FONT_MANAGER.getHeight();
                    int var12 = getHudColor(2.0f).getRGB();
                    arg0.getContext().blit(RenderPipelines.GUI_TEXTURED, LOGO_ID, 2, 2, 0.0f, 0.0f, var11, var11, 64, 64, 64, 64, var12);
                    var10 = ((float) ((2 + var11) + 3));
                }
                drawText(arg0.getContext(), var9, var10, 2.0f);
                var3 = Math.max(var3, (((int) (var10 - 2.0f)) + LightningClient.FONT_MANAGER.getWidth(var9)));
                var4++;
            }
            if (this.uptime.getValue()) {
                var5 = FormattingUtils.formatSeconds(((System.currentTimeMillis() - LightningClient.UPTIME) / 1000L));
                var6 = ("Uptime " + String.valueOf(ChatFormatting.WHITE) + var5[0] + ":" + var5[1] + ":" + var5[2]);
                if (this.watermark.getValue()) {
                } else {
                }
                if (this.informationSync.getValue()) {
                } else {
                }
                var6.drawText(2.0f, 2, ((float) (LightningClient.FONT_MANAGER.getHeight() + 0)), null, new Color(170, 170, 170));
                var3 = Math.max(var3, LightningClient.FONT_MANAGER.getWidth(var6));
                var4++;
            }
            HudElementRegistry.reportBounds("Watermark", 2.0f, 2.0f, ((float) (2 + var3)), ((float) (2 + (var4 * LightningClient.FONT_MANAGER.getHeight()))));
            var2.popMatrix();
        }
        if (this.welcomer.getValue()) {
            var2.pushMatrix();
            var2.translate(this.welcomerPosition.getX(), this.welcomerPosition.getY());
            if (this.welcomerSync.getValue()) {
                break label_24;
            }
            var3 = "[username]".replace("", (String.valueOf((this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE)) + mc.player.getName().getString() + String.valueOf(ChatFormatting.RESET)));
            var4 = ((((float) mc.getWindow().getGuiScaledWidth()) / 2.0f) - (((float) LightningClient.FONT_MANAGER.getWidth(var3)) / 2.0f));
            drawText(arg0.getContext(), var3, var4, 6.0f);
            HudElementRegistry.reportBounds("Welcomer", var4, 6.0f, (var4 + ((float) LightningClient.FONT_MANAGER.getWidth(var3))), ((float) (6 + LightningClient.FONT_MANAGER.getHeight())));
            var2.popMatrix();
        }
    }

    private void renderModuleList(RenderOverlayEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.moduleList.getValue())) return;
        if (this.vanillaPotions.getValue().equalsIgnoreCase("Move")) {
            if (mc.player.getActiveEffects().isEmpty()) {
            }
        } else {
        }
        float var2 = 0.0f.get(((float) (EntityUtils.hasNegativeEffects(mc.player) ? 51 : 25)));
        Object var3 = arg0.getMatrices();
        var3.pushMatrix();
        var3.translate(this.moduleListPosition.getX(), this.moduleListPosition.getY());
        int var4 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        int var5 = 0;
        int var6 = 0;
        Object var7 = this.moduleEntries.iterator();
        while (var7.hasNext()) {
            Object var8 = ((HUDModule.ModuleEntry) var7.next());
            if (!(var8.module().isToggled())) {
                if (!(!var4) && var8.module().getBind()) {
                }
            } else {
            }
            int var9 = 0;
            if (!(!var9)) {
            } else {
            }
            float var10 = 0.0f;
            float var11 = (((float) mc.getWindow().getGuiScaledWidth()) - var8.module().getAnimationOffset().get(var10));
            float var12 = ((2.0f + var2) + ((float) (var6 * LightningClient.FONT_MANAGER.getHeight())));
            drawModuleText(var8.module(), arg0.getContext(), var8.text(), var11, var12);
            var5 = Math.max(var5, (LightningClient.FONT_MANAGER.getWidth(var8.text()) + 2));
            var6++;
        }
        if (!(this.moduleEntries.isEmpty())) {
            var7 = ((float) mc.getWindow().getGuiScaledWidth());
            HudElementRegistry.reportBounds("ModuleList", (var7 - ((float) var5)), (2.0f + var2), var7, ((2.0f + var2) + ((float) (this.moduleEntries.size() * LightningClient.FONT_MANAGER.getHeight()))));
        }
        var3.popMatrix();
    }

    private void renderPlayerRadar(RenderOverlayEvent arg0) {
        if (!(this.playerRadar.getValue())) return;
        Object var2 = arg0.getMatrices();
        var2.pushMatrix();
        var2.translate(this.playerRadarPosition.getX(), this.playerRadarPosition.getY());
        int var3 = 0;
        int var4 = 0;
        Object var5 = this.playerEntries.iterator();
        while (var5.hasNext()) {
            Object var6 = ((HUDModule.PlayerEntry) var5.next());
            if (var6.headTexture() != null) {
                PlayerFaceExtractor.extractRenderState(arg0.getContext(), var6.headTexture(), 2, ((1 + (LightningClient.FONT_MANAGER.getHeight() * 2)) + ((LightningClient.FONT_MANAGER.getHeight() + 1) * var4)), LightningClient.FONT_MANAGER.getHeight(), true, false, Color.WHITE.getRGB());
            }
            if (var6.headTexture() != null) {
            } else {
            }
            int var7 = ((LightningClient.FONT_MANAGER.getHeight() + 2) + 0);
            if (LightningClient.FRIEND_MANAGER.contains(var6.player().getName().getString())) {
            } else {
            }
            arg0.getContext().drawText(var6.text(), ((float) var7), ((float) ((2 + (LightningClient.FONT_MANAGER.getHeight() * 2)) + ((LightningClient.FONT_MANAGER.getHeight() + 1) * var4))), LightningClient.FRIEND_MANAGER.getDefaultFriendColor(), null);
            var3 = Math.max(var3, (var7 + LightningClient.FONT_MANAGER.getWidth(var6.text())));
            var4++;
        }
        if (!(this.playerEntries.isEmpty())) {
            HudElementRegistry.reportBounds("PlayerRadar", 2.0f, ((float) (1 + (LightningClient.FONT_MANAGER.getHeight() * 2))), ((float) var3), ((float) ((1 + (LightningClient.FONT_MANAGER.getHeight() * 2)) + ((LightningClient.FONT_MANAGER.getHeight() + 1) * this.playerEntries.size()))));
        }
        var2.popMatrix();
    }

    private static int countItem(Player arg0, Item arg1) {
        if (arg0 == null) return 0;
        int var2 = 0;
        Object var3 = arg0.getInventory().getNonEquipmentItems().iterator();
        while (var3.hasNext()) {
            Object var4 = ((ItemStack) var3.next());
            if (!(var4.is(arg1))) {
                break label_1;
            }
            var2 = (var2 + var4.getCount());
        }
        if (arg0.getOffhandItem().is(arg1)) {
            var2 = (var2 + arg0.getOffhandItem().getCount());
        }
        return var2;
    }

    private static ItemStack[] dummyArmor() {
        if (dummyArmor == null) {
            var tmp0 = new ItemStack[4];
            tmp0[0] = new ItemStack(Items.NETHERITE_BOOTS);
            tmp0[1] = new ItemStack(Items.NETHERITE_LEGGINGS);
            tmp0[2] = new ItemStack(Items.NETHERITE_CHESTPLATE);
            tmp0[3] = new ItemStack(Items.NETHERITE_HELMET);
            dummyArmor = tmp0;
        }
        return dummyArmor;
    }

    private void renderItems(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        renderArmor(arg0);
        renderTotemCounter(arg0);
        renderCrystalCounter(arg0);
        renderXPCounter(arg0);
    }

    private void renderArmor(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        int var2 = (mc.gui.screen() instanceof HUDEditorScreen);
        if (mc.player.isEyeInFluid(FluidTags.WATER)) break label_1;
        if (mc.player.getAirSupply() < mc.player.getMaxAirSupply()) {
            label_1:
        } else {
        }
        int var3 = 0;
        int var4 = ((mc.getWindow().getGuiScaledWidth() / 2) + 15);
        int var5 = ((mc.getWindow().getGuiScaledHeight() - 55) - var3);
        HudElementRegistry.reportBounds("Armor", ((float) (var4 - 1)), ((float) (var5 - 3)), ((float) (var4 + 71)), ((float) (var5 + 17)));
        if (!(this.armor.getValue()) && !var2) return;
        Object var6 = arg0.getMatrices();
        var6.pushMatrix();
        var6.translate(this.armorPosition.getX(), this.armorPosition.getY());
        int var7 = 0;
        Object var8 = ARMOR_SLOTS;
        int var9 = var8.length;
        int var10 = 0;
        while (var10 < var9) {
            Object var11 = var8[var10];
            if (mc.player.getItemBySlot(var11).isEmpty()) {
            } else {
                var7 = 1;
                break label_8;
            }
            var10++;
        }
        label_8:
        if (!(!var7) && this.armor.getValue()) {
            var8 = 0;
            var9 = ARMOR_SLOTS;
            var10 = var9.length;
            var11 = 0;
            while (var11 < var10) {
                Object var12 = var9[var11];
                Object var13 = mc.player.getItemBySlot(var12);
                if (var13.isEmpty()) {
                } else {
                    int var14 = (((mc.getWindow().getGuiScaledWidth() / 2) + 69) - (18 * var8));
                    int var15 = var5;
                    arg0.getContext().item(var13, var14, var15);
                    if (!(this.armorDurability.getValue().equalsIgnoreCase("Bar"))) {
                        if (this.armorDurability.getValue().equalsIgnoreCase("Both")) {
                        }
                        arg0.getContext().itemDecorations(mc.font, var13, var14, var15);
                    }
                    int var16 = var13.getDamageValue();
                    int var17 = var13.getMaxDamage();
                    if (!(this.armorDurability.getValue().equalsIgnoreCase("Percentage"))) {
                        if (!(this.armorDurability.getValue().equalsIgnoreCase("Both"))) break label_14;
                    }
                    if (var17 <= 0) break label_14;
                    var6.pushMatrix();
                    var6.scale(0.625f, 0.625f);
                    drawText(arg0.getContext(), ((((var17 - var16) * 100) / var17) + "%"), ((float) ((int) (((float) (((mc.getWindow().getGuiScaledWidth() >> 1) + 70) - (18 * var8))) * 1.6f))), ((float) ((int) ((((float) (var5 - 3)) * 1.6f) - 5.0f))), false, new Color((1.0f - (((float) (var17 - var16)) / ((float) var17))), (((float) (var17 - var16)) / ((float) var17)), 0.0f));
                    var6.popMatrix();
                    label_14:
                    var8++;
                }
                var11++;
            }
            break label_19;
        }
        if (!var2) break label_19;
        var8 = dummyArmor();
        var9 = 0;
        while (var9 < var8.length) {
            var10 = (((mc.getWindow().getGuiScaledWidth() / 2) + 69) - (18 * var9));
            var11 = var5;
            arg0.getContext().item(var8[var9], var10, var11);
            var9++;
        }
        label_19:
        var6.popMatrix();
    }

    private void renderTotemCounter(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        int var2 = (mc.gui.screen() instanceof HUDEditorScreen);
        float var3 = ((((float) mc.getWindow().getGuiScaledWidth()) / 2.0f) - 9.0f);
        if ((!(mc.player.isEyeInFluid(FluidTags.WATER))) || (mc.gameMode == null)) break label_1;
        float var4 = ((float) ((mc.getWindow().getGuiScaledHeight() - 55) - (mc.gameMode.getPlayerMode() != GameType.CREATIVE ? 10 : 0)));
        HudElementRegistry.reportBounds("TotemCounter", (var3 - 1.0f), (var4 - 1.0f), (var3 + 19.0f), (var4 + 19.0f));
        if (!(this.totemCounter.getValue()) && !var2) return;
        int var5 = countItem(mc.player, Items.TOTEM_OF_UNDYING);
        this.totemSmoothCounter.update(var5);
        Object var6 = arg0.getMatrices();
        var6.pushMatrix();
        var6.translate(this.totemCounterPosition.getX(), this.totemCounterPosition.getY());
        if (!(this.totemCounter.getValue())) break label_5;
        if (var5 <= 0) {
            if (this.totemSmoothCounter.lastCount <= 0) break label_5;
        }
        Object var7 = new ItemStack(Items.TOTEM_OF_UNDYING);
        var6.pushMatrix();
        var6.translate(var3, var4);
        arg0.getContext().item(var7, 0, 0);
        this.totemSmoothCounter.render(arg0.getContext(), this.itemsSmooth.getValue());
        var6.popMatrix();
        break label_8;
        label_5:
        if (!var2) break label_8;
        var7 = new ItemStack(Items.TOTEM_OF_UNDYING);
        var6.pushMatrix();
        var6.translate(var3, var4);
        arg0.getContext().item(var7, 0, 0);
        if (LightningClient.FONT_MANAGER != null) {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0.getContext(), "1", (17 - LightningClient.FONT_MANAGER.getWidth("1")), 9, Color.WHITE);
        } else {
            arg0.getContext().text(mc.font, "1", (17 - mc.font.width("1")), 9, -1, true);
        }
        var6.popMatrix();
        label_8:
        var6.popMatrix();
    }

    private void renderCrystalCounter(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        int var2 = (mc.gui.screen() instanceof HUDEditorScreen);
        float var3 = ((((float) mc.getWindow().getGuiScaledWidth()) / 2.0f) + 106.0f);
        float var4 = (((float) (mc.getWindow().getGuiScaledHeight() - 40)) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f));
        HudElementRegistry.reportBounds("CrystalCounter", (var3 - 1.0f), (var4 - 1.0f), (var3 + 19.0f), (var4 + 19.0f));
        if (!(this.crystalCounter.getValue()) && !var2) return;
        int var5 = countItem(mc.player, Items.END_CRYSTAL);
        this.crystalSmoothCounter.update(var5);
        Object var6 = arg0.getMatrices();
        var6.pushMatrix();
        var6.translate(this.crystalCounterPosition.getX(), this.crystalCounterPosition.getY());
        if (!(this.crystalCounter.getValue())) break label_5;
        if (var5 <= 0) {
            if (this.crystalSmoothCounter.lastCount <= 0) break label_5;
        }
        Object var7 = new ItemStack(Items.END_CRYSTAL);
        var6.pushMatrix();
        var6.translate(var3, var4);
        arg0.getContext().item(var7, 0, 0);
        this.crystalSmoothCounter.render(arg0.getContext(), this.itemsSmooth.getValue());
        var6.popMatrix();
        break label_8;
        label_5:
        if (!var2) break label_8;
        var7 = new ItemStack(Items.END_CRYSTAL);
        var6.pushMatrix();
        var6.translate(var3, var4);
        arg0.getContext().item(var7, 0, 0);
        if (LightningClient.FONT_MANAGER != null) {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0.getContext(), "64", (17 - LightningClient.FONT_MANAGER.getWidth("64")), 9, Color.WHITE);
        } else {
            arg0.getContext().text(mc.font, "64", (17 - mc.font.width("64")), 9, -1, true);
        }
        var6.popMatrix();
        label_8:
        var6.popMatrix();
    }

    private void renderXPCounter(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        int var2 = (mc.gui.screen() instanceof HUDEditorScreen);
        float var3 = ((((float) mc.getWindow().getGuiScaledWidth()) / 2.0f) + 106.0f);
        float var4 = (((float) (mc.getWindow().getGuiScaledHeight() - 20)) - (this.counterChatOffset.getValue() ? this.chatOffset : 0.0f));
        HudElementRegistry.reportBounds("XPCounter", (var3 - 1.0f), (var4 - 1.0f), (var3 + 19.0f), (var4 + 19.0f));
        if (!(this.xpCounter.getValue()) && !var2) return;
        int var5 = countItem(mc.player, Items.EXPERIENCE_BOTTLE);
        this.xpSmoothCounter.update(var5);
        Object var6 = arg0.getMatrices();
        var6.pushMatrix();
        var6.translate(this.xpCounterPosition.getX(), this.xpCounterPosition.getY());
        if (!(this.xpCounter.getValue())) break label_5;
        if (var5 <= 0) {
            if (this.xpSmoothCounter.lastCount <= 0) break label_5;
        }
        Object var7 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        var6.pushMatrix();
        var6.translate(var3, var4);
        arg0.getContext().item(var7, 0, 0);
        this.xpSmoothCounter.render(arg0.getContext(), this.itemsSmooth.getValue());
        var6.popMatrix();
        break label_8;
        label_5:
        if (!var2) break label_8;
        var7 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        var6.pushMatrix();
        var6.translate(var3, var4);
        arg0.getContext().item(var7, 0, 0);
        if (LightningClient.FONT_MANAGER != null) {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0.getContext(), "64", (17 - LightningClient.FONT_MANAGER.getWidth("64")), 9, Color.WHITE);
        } else {
            arg0.getContext().text(mc.font, "64", (17 - mc.font.width("64")), 9, -1, true);
        }
        var6.popMatrix();
        label_8:
        var6.popMatrix();
    }

    private void renderInventory(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        if (!(this.inventory.getValue())) return;
        Object var2 = arg0.getMatrices();
        var2.pushMatrix();
        var2.translate(this.inventoryPosition.getX(), this.inventoryPosition.getY());
        int var3 = 3;
        int var4 = 18;
        int var5 = ((9 * var4) + (var3 * 2));
        int var6 = ((3 * var4) + (var3 * 2));
        HudElementRegistry.reportBounds("Inventory", 0.0f, 0.0f, ((float) var5), ((float) var6));
        if (this.inventoryBackground.getValue()) {
            int var7 = ((int) Math.clamp(this.inventoryAlpha.getValue().floatValue(), 0.0f, 255.0f));
            Object var8 = new Color(15, 15, 18, var7);
            Object var9 = new Color(255, 255, 255, Math.min(var7, 30));
            Renderer2D.renderQuad(arg0.getContext(), 0.0f, 0.0f, ((float) var5), ((float) var6), var8);
            Renderer2D.renderOutline(arg0.getContext(), 0.0f, 0.0f, ((float) var5), ((float) var6), var9);
        }
        var7 = 9;
        while (var7 <= 35) {
            var9 = ((var7 - 9) % 9);
            int var10 = ((var7 - 9) / 9);
            int var11 = (var3 + (var9 * var4));
            int var12 = (var3 + (var10 * var4));
            if (this.inventorySlotBackground.getValue()) {
                Object var13 = new Color(255, 255, 255, 10);
                Renderer2D.renderQuad(arg0.getContext(), ((float) var11), ((float) var12), ((float) (var11 + 18)), ((float) (var12 + 18)), var13);
            }
            var tmp0 = mc.player.getInventory().getItem(var7);
            var8 = tmp0;
            if (tmp0.isEmpty()) {
            } else {
                arg0.getContext().item(var8, (var11 + 1), (var12 + 1));
                arg0.getContext().itemDecorations(mc.font, var8, (var11 + 1), (var12 + 1));
            }
            var7++;
        }
        var2.popMatrix();
    }

    private void renderPotions(RenderOverlayEvent arg0) {
        if (mc.player == null) return;
        if (!(this.potions.getValue())) return;
        Object var2 = arg0.getMatrices();
        var2.pushMatrix();
        var2.translate(this.potionsPosition.getX(), this.potionsPosition.getY());
        float var3 = (this.informationChatOffset.getValue() ? this.chatOffset : 0.0f);
        int var4 = 0;
        int var5 = 0;
        Object var6 = this.potionEntries.iterator();
        while (var6.hasNext()) {
            Object var7 = ((HUDModule.PotionEntry) var6.next());
            int var8 = LightningClient.FONT_MANAGER.getWidth(var7.text());
            if (var7.sprite() != null) {
            } else {
            }
            int var9 = ((LightningClient.FONT_MANAGER.getHeight() + 2) + 0);
            var5 = Math.max(var5, var9);
            int var10 = ((mc.getWindow().getGuiScaledWidth() - 2) - var8);
            int var11 = ((((mc.getWindow().getGuiScaledHeight() - ((int) var3)) - 2) - LightningClient.FONT_MANAGER.getHeight()) - (LightningClient.FONT_MANAGER.getHeight() * var4));
            if (var7.sprite() != null) {
                var2.pushMatrix();
                var2.translate(((float) ((var10 - LightningClient.FONT_MANAGER.getHeight()) - 2)), ((float) (var11 - 1)));
                arg0.getContext().blitSprite(RenderPipelines.GUI_TEXTURED, var7.sprite(), 0, 0, LightningClient.FONT_MANAGER.getHeight(), LightningClient.FONT_MANAGER.getHeight());
                var2.popMatrix();
            }
            if ((!(this.potionColor.getValue().equals("Client"))) || (!(this.colorMode.getValue().equals("Rainbow")))) break label_8;
            drawText(arg0.getContext(), var7.text(), ((float) var10), ((float) var11), this.rainbowMode.getValue().equals("Horizontal"), var7.color());
            var4++;
        }
        if (var4 > 0) {
            var6 = ((float) mc.getWindow().getGuiScaledWidth());
            var7 = (((float) mc.getWindow().getGuiScaledHeight()) - var3);
            HudElementRegistry.reportBounds("Potions", (((var6 - 2.0f) - ((float) var5)) - 4.0f), ((var7 - 2.0f) - ((float) (var4 * LightningClient.FONT_MANAGER.getHeight()))), var6, var7);
        }
        var2.popMatrix();
    }

    private void renderInformation(RenderOverlayEvent arg0) {
        if (mc.player != null && this.informationElement.getValue()) {
            Object var2 = arg0.getMatrices();
            var2.pushMatrix();
            var2.translate(this.informationPosition.getX(), this.informationPosition.getY());
            int var3 = 0;
            float var4 = (this.informationChatOffset.getValue() ? this.chatOffset : 0.0f);
            if (!(this.health.getValue())) break label_2;
            Object var5 = new DecimalFormat("0").format(((double) (mc.player.getHealth() + mc.player.getAbsorptionAmount())));
            Object var6 = new Color(TextColor.fromLegacyFormat(ColorUtils.getHealthColor(((double) (mc.player.getHealth() + mc.player.getAbsorptionAmount())))).getValue());
            LightningClient.FONT_MANAGER.drawTextWithOutline(arg0.getContext(), var5, ((mc.getWindow().getGuiScaledWidth() / 2) - (LightningClient.FONT_MANAGER.getWidth(var5) / 2)), ((mc.getWindow().getGuiScaledHeight() / 2) + 16), var6, Color.BLACK);
            label_2:
            var5 = new ArrayList();
            if (this.ping.getValue()) {
                var5.add((String.valueOf(getPrimary()) + "Ping " + String.valueOf(getSecondary()) + LightningClient.SERVER_MANAGER.getPing() + "ms"));
            }
            if (this.fps.getValue()) {
                var5.add((String.valueOf(getPrimary()) + "FPS " + String.valueOf(getSecondary()) + LightningClient.RENDER_MANAGER.getFps()));
            }
            if (this.durability.getValue()) {
                var5.add(("Durability " + (mc.player.getMainHandItem().getMaxDamage() - mc.player.getMainHandItem().getDamageValue())));
            }
            if (!(this.speed.getValue().equalsIgnoreCase("None"))) {
                var5.add((String.valueOf(getPrimary()) + "Speed " + String.valueOf(getSecondary()) + new DecimalFormat("0.00").format(EntityUtils.getSpeed(mc.player, (this.speed.getValue().equalsIgnoreCase("Meters") ? EntityUtils.SpeedUnit.METERS : EntityUtils.SpeedUnit.KILOMETERS))) + (this.speed.getValue().equalsIgnoreCase("Meters") ? "m/s" : "km/h")));
            }
            if (this.serverBrand.getValue()) {
                var5.add((String.valueOf(getPrimary()) + "Brand " + String.valueOf(getSecondary()) + LightningClient.SERVER_MANAGER.getServerBrand()));
            }
            var6 = LightningClient.SERVER_MANAGER.getTickRate();
            if (this.tps.getValue()) {
                if (((double) var6) > 19.79) {
                } else {
                }
                String.valueOf(getPrimary()).add((String.valueOf(getSecondary()) + "TPS " + "20.00" + new DecimalFormat("00.00").format(((double) var6))));
            }
            if (!(var5.isEmpty())) {
                Objects.requireNonNull(LightningClient.FONT_MANAGER);
                var5.sort(Comparator.comparingInt(LightningClient.FONT_MANAGER::getWidth).reversed());
                Object var7 = var5.iterator();
                while (var7.hasNext()) {
                    Object var8 = ((String) var7.next());
                    if (var8.startsWith("Durability")) {
                        if (!(mc.player.getMainHandItem().isDamageableItem())) break label_21;
                        int var9 = mc.player.getMainHandItem().getMaxDamage();
                        int var10 = mc.player.getMainHandItem().getDamageValue();
                        Object var11 = String.valueOf((var9 - var10));
                        if (this.informationSync.getValue()) {
                        } else {
                        }
                        arg0.getContext().drawText((String.valueOf(getPrimary()) + "Durability "), ((float) (((mc.getWindow().getGuiScaledWidth() - 2) - LightningClient.FONT_MANAGER.getWidth("Durability ")) - LightningClient.FONT_MANAGER.getWidth(var11))), ((((((float) mc.getWindow().getGuiScaledHeight()) - var4) - 2.0f) - ((float) LightningClient.FONT_MANAGER.getHeight())) + ((float) (var3 * -(LightningClient.FONT_MANAGER.getHeight())))), null, new Color(170, 170, 170));
                        drawText(arg0.getContext(), var11, ((float) ((mc.getWindow().getGuiScaledWidth() - 2) - LightningClient.FONT_MANAGER.getWidth(var11))), ((((((float) mc.getWindow().getGuiScaledHeight()) - var4) - 2.0f) - ((float) LightningClient.FONT_MANAGER.getHeight())) + ((float) (var3 * -(LightningClient.FONT_MANAGER.getHeight())))), false, new Color((1.0f - (((float) (var9 - var10)) / ((float) var9))), (((float) (var9 - var10)) / ((float) var9)), 0.0f));
                        var3++;
                        break label_21;
                    }
                    if (this.informationSync.getValue()) {
                    } else {
                    }
                    arg0.getContext().drawText(var8, ((float) ((mc.getWindow().getGuiScaledWidth() - 2) - LightningClient.FONT_MANAGER.getWidth(var8))), ((((((float) mc.getWindow().getGuiScaledHeight()) - var4) - 2.0f) - ((float) LightningClient.FONT_MANAGER.getHeight())) + ((float) (var3 * -(LightningClient.FONT_MANAGER.getHeight())))), null, new Color(170, 170, 170));
                    var3++;
                    label_21:
                }
                var7 = 0;
                var8 = var5.iterator();
                while (var8.hasNext()) {
                    var9 = ((String) var8.next());
                    var7 = Math.max(var7, LightningClient.FONT_MANAGER.getWidth(var9));
                }
                var8 = ((float) mc.getWindow().getGuiScaledWidth());
                var9 = (((float) mc.getWindow().getGuiScaledHeight()) - var4);
                HudElementRegistry.reportBounds("Information", ((var8 - 2.0f) - ((float) var7)), ((var9 - 2.0f) - ((float) (var3 * LightningClient.FONT_MANAGER.getHeight()))), var8, var9);
            }
            var2.popMatrix();
        }
    }

    private void renderCoordinates(RenderOverlayEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.coordinates.getValue()) && !(this.direction.getValue())) return;
        Object var2 = arg0.getMatrices();
        var2.pushMatrix();
        var2.translate(this.coordinatesPosition.getX(), this.coordinatesPosition.getY());
        float var3 = (this.positionChatOffset.getValue() ? this.chatOffset : 0.0f);
        int var4 = 0;
        int var5 = 0;
        int var6 = 0;
        if (this.coordinates.getValue()) {
            int var7 = getFakedCoord(mc.player.getBlockX(), this.fakeOffsetX);
            int var8 = getFakedCoord(mc.player.getBlockZ(), this.fakeOffsetZ);
            if (this.netherCoordinates.getValue()) {
            } else {
            }
            if (!(this.inversion.getValue())) {
                if (this.positionSync.getValue()) {
                }
            } else {
            }
            if (!(this.inversion.getValue())) {
                if (this.positionSync.getValue()) {
                }
            } else {
            }
            if (this.netherCoordinates.getValue()) {
            } else {
            }
            Object var9 = (ChatFormatting.RESET + String.valueOf(ChatFormatting.GRAY) + String.valueOf(getSecondary()) + mc.player.getBlockY() + ", " + ChatFormatting.RESET + String.valueOf(ChatFormatting.GRAY) + String.valueOf(getSecondary()) + ", " + String.valueOf(var8) + (String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(getSecondary()) + WorldUtils.getNetherPosition(var8) + String.valueOf(ChatFormatting.GRAY) + "]") + "");
            drawText(arg0.getContext(), var9, 2.0f, ((((((float) mc.getWindow().getGuiScaledHeight()) - var3) - ((float) var4)) - ((float) LightningClient.FONT_MANAGER.getHeight())) - 2.0f));
            var5 = Math.max(var5, LightningClient.FONT_MANAGER.getWidth(var9));
            var4 = (var4 + LightningClient.FONT_MANAGER.getHeight());
            var6++;
        }
        if (this.direction.getValue()) {
            if (this.inversion.getValue()) {
            } else {
            }
            if (this.inversion.getValue()) {
            } else {
            }
            if (this.inversion.getValue()) {
            } else {
            }
            var7 = (String.valueOf(ChatFormatting.GRAY) + getSecondary() + String.valueOf(ChatFormatting.WHITE) + " [" + WorldUtils.getFacingAxes(mc.player.getYRot()) + getSecondary() + String.valueOf(ChatFormatting.GRAY) + "]");
            drawText(arg0.getContext(), var7, 2.0f, ((((((float) mc.getWindow().getGuiScaledHeight()) - var3) - ((float) var4)) - ((float) LightningClient.FONT_MANAGER.getHeight())) - 2.0f), (this.positionSync.getValue() ? null : Color.WHITE));
            var5 = Math.max(var5, LightningClient.FONT_MANAGER.getWidth(var7));
            var6++;
        }
        var7 = (((float) mc.getWindow().getGuiScaledHeight()) - var3);
        HudElementRegistry.reportBounds("Coordinates", 2.0f, ((var7 - 2.0f) - ((float) (var6 * LightningClient.FONT_MANAGER.getHeight()))), ((float) (2 + var5)), (var7 - 2.0f));
        var2.popMatrix();
    }

    private static Color lerpColor(Color arg0, Color arg1, float arg2) {
        float var3 = Math.max(0.0f, Math.min(1.0f, arg2));
        int var4 = Math.round((((float) arg0.getRed()) + (var3 * ((float) (arg1.getRed() - arg0.getRed())))));
        int var5 = Math.round((((float) arg0.getGreen()) + (var3 * ((float) (arg1.getGreen() - arg0.getGreen())))));
        int var6 = Math.round((((float) arg0.getBlue()) + (var3 * ((float) (arg1.getBlue() - arg0.getBlue())))));
        int var7 = Math.round((((float) arg0.getAlpha()) + (var3 * ((float) (arg1.getAlpha() - arg0.getAlpha())))));
        return new Color(var4, var5, var6, var7);
    }

    private void drawModuleText(Module arg0, GuiGraphicsExtractor arg1, String arg2, float arg3, float arg4) {
        int var6 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        if (!var6) {
            if (!(arg0.isToggled())) {
                drawText(arg1, arg2, arg3, arg4, false, new Color(170, 170, 170));
                return;
            }
            Object var7 = getHudColor(arg4);
            if (!(this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow"))) break label_1;
            long var8 = ((((long) arg4) / ((long) LightningClient.FONT_MANAGER.getHeight())) * (this.rainbowOffset.getValue().longValue() * 10L));
            var7 = ColorUtils.getOffsetRainbow(var8);
            break label_2;
            label_1:
            if (!(this.moduleColorMode.getValue().equals("Random"))) break label_2;
            var7 = ColorUtils.getHashColor(arg0.getName());
            label_2:
            if (!(this.moduleColorMode.getValue().equals("Rainbow"))) {
                if ((!(this.moduleColorMode.getValue().equals("Default"))) || (!(this.colorMode.getValue().equals("Rainbow")))) break label_4;
            }
            drawText(arg1, arg2, arg3, arg4, this.rainbowMode.getValue().equals("Horizontal"), var7);
            return;
        }
        var7 = ((Animation) this.moduleColorAnimations.computeIfAbsent(arg0, (arg0) -> new Animation((arg0.isToggled() ? 1.0f : 0.0f), (arg0.isToggled() ? 1.0f : 0.0f), 350, Easing.Method.EASE_IN_OUT_CUBIC)));
        var8 = var7.get((arg0.isToggled() ? 1.0f : 0.0f));
        Object var9 = getHudColor(arg4);
        if (this.moduleColorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long var10 = ((((long) arg4) / ((long) LightningClient.FONT_MANAGER.getHeight())) * (this.rainbowOffset.getValue().longValue() * 10L));
            var9 = ColorUtils.getOffsetRainbow(var10);
            break label_10;
        }
        if (!(this.moduleColorMode.getValue().equals("Random"))) break label_10;
        var9 = ColorUtils.getHashColor(arg0.getName());
        label_10:
        if (!(this.moduleColorMode.getValue().equals("Rainbow"))) {
            if ((!(this.moduleColorMode.getValue().equals("Default"))) || (!(this.colorMode.getValue().equals("Rainbow")))) break label_12;
        }
        var10 = (this.rainbowMode.getValue().equals("Horizontal") ? 1 : 0);
        HudListStyle.drawNewbie(this, arg0, arg1, arg3, arg4, var8, var9, var10);
    }

    private void drawNewbieModuleText(Module arg0, GuiGraphicsExtractor arg1, float arg2, float arg3, float arg4, Color arg5, boolean arg6) {
        if (arg0.getBind()) {
            break label_1;
        }
        label_1:
        Object var10 = "";
        var tmp0 = arg0.getName();
        Object var9 = tmp0;
        if (!(arg0.getMetaData().isEmpty())) {
            if (!(this.metaData.getValue())) {
            }
        } else {
        }
        var tmp1 = (" [" + arg0.getMetaData() + "]");
        Object var8 = tmp1;
        Object var11 = (tmp0 + "" + tmp1);
        if (var11.isEmpty()) return;
        Object var12 = new Color(170, 170, 170);
        Object var13 = Color.WHITE;
        float var14 = ((float) LightningClient.FONT_MANAGER.getWidth(var11));
        float var15 = 0.0f;
        float var16 = 0.35f;
        Object var17 = Component.empty();
        int var18 = ((String) var10).length();
        int var19 = var9.length();
        int var20 = 0;
        while (var20 < var11.length()) {
            int var21 = var11.charAt(var20);
            Object var22 = String.valueOf(var21);
            float var23 = ((float) LightningClient.FONT_MANAGER.getWidth(var22));
            if (var14 > 0.0f) {
            } else {
            }
            float var24 = 0.0f;
            var15 = (var15 + var23);
            Object var25 = var12;
            if (var20 < var18) {
                if (var21 != 91 && var21 != 93) {
                    if (var21 == 32) {
                    }
                    break label_16;
                }
                break label_16;
            }
            if (var20 < (var18 + var19)) {
                if (!(!arg6)) {
                    break label_16;
                }
                break label_16;
            }
            if (var21 != 91 && var21 != 93) {
                if (var21 == 32) {
                }
                break label_16;
            }
            label_16:
            Object var26 = var13;
            float var27 = Math.max(0.0f, Math.min(1.0f, (((arg4 * (1.0f + var16)) - var24) / var16)));
            float var28 = ((var27 * var27) * (3.0f - (2.0f * var27)));
            Object var29 = lerpColor(var25, var26, var28);
            var17.append(Component.literal(var22).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(var29.getRGB()))));
            var20++;
        }
        var20 = arg1.pose();
        var20.pushMatrix();
        var20.translate(arg2, arg3);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg1, var17.getVisualOrderText(), 0, 0, Color.WHITE);
        var20.popMatrix();
    }

    private void drawText(GuiGraphicsExtractor arg0, String arg1, float arg2, float arg3) {
        drawText(arg0, arg1, arg2, arg3, null);
    }

    public void drawText(GuiGraphicsExtractor arg0, String arg1, float arg2, float arg3, Color arg4) {
        if (!(this.colorMode.getValue().equals("Rainbow"))) break label_0;
        drawText(arg0, arg1, arg2, arg3, this.rainbowMode.getValue().equals("Horizontal"), arg4);
    }

    public void drawText(GuiGraphicsExtractor arg0, String arg1, float arg2, float arg3, boolean arg4, Color arg5) {
        if (arg5 == null) {
            arg5 = getHudColor(arg3);
        }
        Object var7 = arg0.pose();
        var7.pushMatrix();
        var7.translate(arg2, arg3);
        if (!(!arg4)) {
            LightningClient.FONT_MANAGER.drawRainbowString(arg0, arg1, 0, 0, (this.rainbowOffset.getValue().longValue() * 5L));
        } else {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, arg1, 0, 0, arg5);
        }
        var7.popMatrix();
    }

    private Color getHudColor(float arg0) {
        if (this.colorMode.getValue().equalsIgnoreCase("Rainbow")) {
            long var2 = ((((long) arg0) / ((long) LightningClient.FONT_MANAGER.getHeight())) * (this.rainbowOffset.getValue().longValue() * 10L));
            return ColorUtils.getOffsetRainbow(var2);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Wave")) {
            var2 = ((((long) arg0) / ((long) LightningClient.FONT_MANAGER.getHeight())) * (this.rainbowOffset.getValue().longValue() * 20L));
            return ColorUtils.getOffsetWave(ColorUtils.getGlobalColor(), var2);
        }
        if (this.colorMode.getValue().equalsIgnoreCase("Custom")) return ColorUtils.getColor(this.customColor.getColor(), 255);
        return ColorUtils.getGlobalColor();
    }

    public Setting getSetting(String arg0) {
        if ((arg0.equalsIgnoreCase("ModuleListMode")) || (arg0.equalsIgnoreCase("Mode"))) {
            return this.moduleListMode;
        }
        return super.getSetting(arg0);
    }

    private String getModuleText(Module arg0) {
        int var2 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
        Object var3 = "";
        if (!(!var2) && arg0.getBind()) {
            var3 = (String.valueOf(ChatFormatting.GRAY) + "[" + String.valueOf(ChatFormatting.WHITE) + KeyboardUtils.getKeyName(arg0.getBind()) + String.valueOf(ChatFormatting.GRAY) + "] ");
        }
        if (!var2 && !(arg0.isToggled())) {
            if (!(arg0.getMetaData().isEmpty())) {
                if (!(this.metaData.getValue())) {
                }
            } else {
            }
            return (String.valueOf(ChatFormatting.GRAY) + arg0.getName() + "" + (String.valueOf(ChatFormatting.DARK_GRAY) + " [" + String.valueOf(ChatFormatting.GRAY) + arg0.getMetaData() + String.valueOf(ChatFormatting.DARK_GRAY) + "]"));
        }
        if (!(arg0.getMetaData().isEmpty())) {
            if (!(this.metaData.getValue())) {
            }
        } else {
        }
        return (arg0.getName() + "" + (String.valueOf(ChatFormatting.GRAY) + " [" + String.valueOf(ChatFormatting.WHITE) + arg0.getMetaData() + String.valueOf(ChatFormatting.GRAY) + "]"));
    }

    private String getPotionText(MobEffectInstance arg0) {
        if (arg0.isInfiniteDuration()) {
            Object var2 = "**:**";
        } else {
            int var3 = Math.round((((float) arg0.getDuration()) / mc.level.tickRateManager().tickrate()));
            var tmp0 = new Object[2];
            tmp0[0] = (var3 / 60);
            tmp0[1] = (var3 % 60);
            var2 = String.format("%02d:%02d", tmp0);
        }
        return (((MobEffect) arg0.getEffect().value()).getDisplayName().getString() + " " + (arg0.getAmplifier() + 1) + " " + String.valueOf(ChatFormatting.WHITE) + var2);
    }

    private ChatFormatting getPrimary() {
        return (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.RESET);
    }

    private ChatFormatting getSecondary() {
        return (this.inversion.getValue() ? ChatFormatting.RESET : ChatFormatting.WHITE);
    }

    static {
        LOGO_ID = Identifier.fromNamespaceAndPath("lightning", "textures/gui/nametag_bolt.png");
        var tmp0 = new EquipmentSlot[4];
        tmp0[0] = EquipmentSlot.FEET;
        tmp0[1] = EquipmentSlot.LEGS;
        tmp0[2] = EquipmentSlot.CHEST;
        tmp0[3] = EquipmentSlot.HEAD;
        ARMOR_SLOTS = tmp0;
    }


    public record ModuleEntry(Module module, String text) {

        public ModuleEntry(Module arg0, String arg1) {
            this.module = arg0;
            this.text = arg1;
        }

        public Module module() {
            return this.module;
        }

        public String text() {
            return this.text;
        }

    }

    public record PotionEntry(String text, Identifier sprite, Color color) {

        public PotionEntry(String arg0, Identifier arg1, Color arg2) {
            this.text = arg0;
            this.sprite = arg1;
            this.color = arg2;
        }

        public String text() {
            return this.text;
        }

        public Identifier sprite() {
            return this.sprite;
        }

        public Color color() {
            return this.color;
        }

    }

    class SmoothCounter {
        private int lastCount;
        private int prevCount;
        private long changeTime;
        private int direction;

        private SmoothCounter() {
            this.lastCount = -1;
            this.prevCount = -1;
            this.changeTime = 0L;
            this.direction = 0;
        }

        public void update(int arg0) {
            if (this.lastCount == -1) {
                this.lastCount = arg0;
                this.prevCount = arg0;
                return;
            }
            if (arg0 != this.lastCount) {
                this.prevCount = this.lastCount;
                this.lastCount = arg0;
                this.direction = (arg0 < this.prevCount ? -1 : 1);
                this.changeTime = System.currentTimeMillis();
            }
        }

        public void render(GuiGraphicsExtractor arg0, boolean arg1) {
            if (this.lastCount <= 0 && this.prevCount <= 0) return;
            Object var6 = String.valueOf(Math.max(0, this.lastCount));
            if (LightningClient.FONT_MANAGER == null) break label_1;
            break label_2;
            label_1:
            label_2:
            int var7 = 9;
            long var8 = (System.currentTimeMillis() - this.changeTime);
            float var10 = 320.0f;
            if (this.changeTime == 0L) {
            } else {
            }
            var tmp0 = Math.min(1.0f, (((float) var8) / var10));
            float var5 = tmp0;
            float var11 = tmp0;
            if (!(!arg1) && var5 < 1.0f && this.prevCount != -1) {
                if (this.prevCount == this.lastCount) {
                }
                if (LightningClient.FONT_MANAGER != null) {
                } else {
                }
                int var12 = Minecraft.getInstance().font.width(var6);
                float var13 = ((float) (17 - var12));
                float var14 = 9.0f;
                if (LightningClient.FONT_MANAGER != null) {
                    LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var6, ((int) var13), ((int) var14), Color.WHITE);
                } else {
                    arg0.text(Minecraft.getInstance().font, var6, ((int) var13), ((int) var14), -1, true);
                }
                return;
            }
            var12 = Easing.ease(var5, Easing.Method.EASE_OUT_CUBIC);
            var13 = String.valueOf(Math.max(0, this.prevCount));
            if (LightningClient.FONT_MANAGER != null) {
            } else {
            }
            var14 = Minecraft.getInstance().font.width(var6);
            if (LightningClient.FONT_MANAGER != null) {
            } else {
            }
            int var15 = Minecraft.getInstance().font.width(var13);
            float var16 = ((float) (17 - var14));
            float var17 = ((float) (17 - var15));
            float var18 = 9.0f;
            float var19 = ((float) (var7 + 3));
            if (this.direction < 0) {
                float var4 = (var12 * var19);
                float var3 = (-((1.0f - var12)) * var19);
            } else {
                var4 = (-(var12) * var19);
                var3 = ((1.0f - var12) * var19);
            }
            int var20 = Math.max(0, Math.min(255, Math.round(((1.0f - var12) * 255.0f))));
            int var21 = Math.max(0, Math.min(255, Math.round((var12 * 255.0f))));
            if (var20 <= 5) break label_18;
            Object var22 = new Color(255, 255, 255, var20);
            if (LightningClient.FONT_MANAGER != null) {
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var13, ((int) var17), ((int) (var18 + var4)), var22);
                break label_18;
            }
            arg0.text(Minecraft.getInstance().font, var13, ((int) var17), ((int) (var18 + var4)), ((var20 << 24) | 16777215), true);
            label_18:
            if (var21 <= 5) break label_20;
            var22 = new Color(255, 255, 255, var21);
            if (LightningClient.FONT_MANAGER != null) {
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var6, ((int) var16), ((int) (var18 + var3)), var22);
                break label_20;
            }
            arg0.text(Minecraft.getInstance().font, var6, ((int) var16), ((int) (var18 + var3)), ((var21 << 24) | 16777215), true);
            label_20:
        }

    }

    public record PlayerEntry(Player player, String text, Identifier headTexture) {

        public PlayerEntry(Player arg0, String arg1, Identifier arg2) {
            this.player = arg0;
            this.text = arg1;
            this.headTexture = arg2;
        }

        public Player player() {
            return this.player;
        }

        public String text() {
            return this.text;
        }

        public Identifier headTexture() {
            return this.headTexture;
        }

    }
}
