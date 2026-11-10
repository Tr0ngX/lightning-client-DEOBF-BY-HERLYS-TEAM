package lightning.modules.impl.visuals;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;

@RegisterModule(name = "NoRender", description = "Disables the rendering of certain things.", category = Module.Category.VISUALS)
public class NoRenderModule extends Module {
    public BooleanSetting hurtCamera;
    public BooleanSetting explosions;
    public BooleanSetting fireOverlay;
    public BooleanSetting blockOverlay;
    public BooleanSetting liquidOverlay;
    public BooleanSetting snowOverlay;
    public BooleanSetting pumpkinOverlay;
    public BooleanSetting portalOverlay;
    public BooleanSetting totemAnimation;
    public BooleanSetting totemPop;
    public BooleanSetting bossBar;
    public BooleanSetting scoreboard;
    public BooleanSetting vignette;
    public BooleanSetting blindness;
    public BooleanSetting fog;
    public BooleanSetting signText;
    public BooleanSetting armor;
    public BooleanSetting limbSwing;
    public BooleanSetting corpses;
    public BooleanSetting player;
    public BooleanSetting background;
    public ModeSetting tileEntities;
    public NumberSetting tileDistance;
    public BooleanSetting items;
    public NumberSetting limit;
    public BooleanSetting displays;
    public BooleanSetting armorStand;
    public BooleanSetting minecart;
    public BooleanSetting advancements;
    public BooleanSetting itemName;
    public BooleanSetting wither;
    private int renderedItems;

    public NoRenderModule() {
        this.hurtCamera = new BooleanSetting("HurtCamera", "Disables the rendering of the hurt camera.", true);
        this.explosions = new BooleanSetting("Explosions", "Disables the rendering of explosion particles.", true);
        this.fireOverlay = new BooleanSetting("FireOverlay", "Disables the rendering of the fire overlay.", true);
        this.blockOverlay = new BooleanSetting("BlockOverlay", "Disables the rendering of the block suffocation overlay.", false);
        this.liquidOverlay = new BooleanSetting("LiquidOverlay", "Disables the rendering of the liquid overlay.", false);
        this.snowOverlay = new BooleanSetting("SnowOverlay", "Disables the rendering of the snow overlay.", false);
        this.pumpkinOverlay = new BooleanSetting("PumpkinOverlay", "Disables the rendering of the pumpkin overlay.", true);
        this.portalOverlay = new BooleanSetting("PortalOverlay", "Disables the rendering of the portal overlay.", false);
        this.totemAnimation = new BooleanSetting("TotemAnimation", "Disables the rendering of the totem pop animation.", false);
        this.totemPop = new BooleanSetting("TotemPop", "Disables the rendering of totem pop particles.", false);
        this.bossBar = new BooleanSetting("BossBar", "Disables the rendering of the boss bar.", false);
        this.scoreboard = new BooleanSetting("Scoreboard", "Disables the rendering of the scoreboard.", false);
        this.vignette = new BooleanSetting("Vignette", "Disables the rendering of the vignette.", true);
        this.blindness = new BooleanSetting("Blindness", "Disables the rendering of the blindness and darkness potion effects.", true);
        this.fog = new BooleanSetting("Fog", "Disables the rendering of the fog.", false);
        this.signText = new BooleanSetting("SignText", "Disables the rendering of sign text.", false);
        this.armor = new BooleanSetting("Armor", "Disables the rendering of armor.", false);
        this.limbSwing = new BooleanSetting("LimbSwing", "Disables the rendering of limb swing animations.", false);
        this.corpses = new BooleanSetting("Corpses", "Disables the rendering of corpses.", false);
        this.player = new BooleanSetting("Player", "Disables the rendering of other players standing in the same spot as you.", false);
        this.background = new BooleanSetting("Background", "Disables the dark background dimming behind inventory/other screens.", false);
        var tmp0 = new String[3];
        tmp0[0] = "Never";
        tmp0[1] = "Distance";
        tmp0[2] = "Always";
        this.tileEntities = new ModeSetting("TileEntities", "Disables the rendering of tile entities, such as chests, when meeting requirements.", "Never", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Distance";
        this.tileDistance = new NumberSetting("TileDistance", "The distance at which tile entities will stop rendering.", new ModeSetting.Visibility(this.tileEntities, tmp1), 10.0f, 0.0f, 100.0f);
        this.items = new BooleanSetting("Items", "Limits how many dropped items are rendered in view.", false);
        this.limit = new NumberSetting("Limit", "Max number of items to render per frame.", new BooleanSetting.Visibility(this.items, true), 100, 0, 100);
        this.displays = new BooleanSetting("Displays", "Disables the rendering of Display entities (BlockDisplay, ItemDisplay, TextDisplay).", false);
        this.armorStand = new BooleanSetting("ArmorStand", "Disables the rendering of armor stands.", false);
        this.minecart = new BooleanSetting("Minecart", "Disables the rendering of minecarts.", false);
        this.advancements = new BooleanSetting("Advancements", "Disables the rendering of advancement/recipe toast popups.", false);
        this.itemName = new BooleanSetting("ItemName", "Hides the item name popup that shows above the hotbar when switching items.", false);
        this.wither = new BooleanSetting("Wither", "Stops hearts turning black/withered when the player has Wither effect.", false);
        this.renderedItems = 0;
    }

    public void resetItemCounter() {
        this.renderedItems = 0;
    }

    public boolean shouldRenderItem() {
        if (!(this.items.getValue())) return true;
        if (this.renderedItems >= this.limit.getValue().intValue()) return false;
        this.renderedItems = (this.renderedItems + 1);
        return true;
    }

}
