package lightning.modules.impl.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.BaseStream;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.ClientPlayerInteractionManagerAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.input.KeyboardUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.rotations.RotationUtils;
import lightning.utils.system.FileUtils;
import lombok.Generated;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Rekit", description = "Automatically regears from an ender chest shulker using a saved kit.", category = Module.Category.PLAYER)
public class RekitModule extends Module {
    public NumberSetting delay;
    public NumberSetting actionsPerTick;
    public BooleanSetting auto;
    public BooleanSetting silentContainer;
    public ModeSetting swapMode;
    public BooleanSetting considerShulkerName;
    public BooleanSetting ignoreCustomName;
    public BindSetting autoPlaceBind;
    public ModeSetting rotate;
    public BooleanSetting strictDirection;
    public ModeSetting pickaxePref;
    public ModeSetting helmetPref;
    public ModeSetting chestplatePref;
    public ModeSetting leggingsPref;
    public ModeSetting bootsPref;
    public Map activeKit;
    public String activeKitName;
    private int ticks;
    private static final String FOLDER = "Lightning/Kits/";
    private static final String LAST_KIT_FILE = "Lightning/last_kit_save.txt";
    public static volatile long lastContainerActionMs;
    private RekitModule.AutoState autoState;
    private int autoTicks;
    private int emptyTicks;
    private int shulkerEnderSlot;
    private BlockPos placedShulkerPos;
    private long placedShulkerRenderMs;
    private BlockPos enderChestPos;
    private boolean hotbarSwapSettled;
    private int hotbarSwapAttempts;
    private static final int MAX_HOTBAR_SWAP_ATTEMPTS = 3;
    private int placeFailStreak;
    private static final int MAX_PLACE_FAIL_STREAK = 3;
    private int cursorWaitTicks;
    private static final int CURSOR_DUMP_AFTER_TICKS = 20;
    private int displacedHotbarSlot;
    private int displacedEnderSlot;
    private boolean autoPlaceActive;
    private boolean autoPlaceBindWasDown;
    private int savedSlot;
    private static final long PLACE_ANIM_MS = 400L;
    private static final long RECENT_COMBAT_TICKS = 100L;
    private static final int FAR_REACH_DECOY_Y_OFFSET = 2000;

    public boolean isAutoActive() {
        return this.autoState != RekitModule.AutoState.IDLE;
    }

    public RekitModule() {
        this.delay = new NumberSetting("Delay", "Tick delay between pull actions.", 1, 0, 10);
        this.actionsPerTick = new NumberSetting("Frequency", "Max pull actions per tick.", 1, 1, 5);
        this.auto = new BooleanSetting("Auto", "Auto pull shulkers from the ender chest.", false);
        this.silentContainer = new BooleanSetting("SilentContainer", "Pull items without the container GUI showing on screen.", false);
        this.swapMode = new ModeSetting("SwapMode", "Swap type used when placing shulkers and swapping to a breaking tool in Auto mode.", "AltSwap", InventoryUtils.SWITCH_MODES);
        this.considerShulkerName = new BooleanSetting("ConsiderShulkerName", "Prefer the shulker whose custom name matches the active kit's name, overriding content-based scoring.", false);
        this.ignoreCustomName = new BooleanSetting("IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false);
        this.autoPlaceBind = new BindSetting("AutoPlace", "Keybind: places the shulker in your inventory, opens it, pulls kit items, closes, then breaks it. No ender chest -- one-shot version of Auto.", 0).disableHoldModes();
        var tmp0 = new String[3];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        tmp0[2] = "Grim";
        this.rotate = new ModeSetting("Rotate", "The rotation mode when placing the shulker.", "Grim", tmp0);
        this.strictDirection = new BooleanSetting("StrictDirection", "Only places using directions that face you.", false);
        var tmp1 = new String[2];
        tmp1[0] = "Efficiency";
        tmp1[1] = "SilkTouch";
        this.pickaxePref = new ModeSetting("Pickaxe", "Preferred pickaxe enchant when choosing between candidates.", "Efficiency", tmp1);
        var tmp2 = new String[2];
        tmp2[0] = "Blast";
        tmp2[1] = "Prot";
        this.helmetPref = new ModeSetting("Helmet", "Preferred helmet enchant when choosing between candidates.", "Prot", tmp2);
        var tmp3 = new String[2];
        tmp3[0] = "Blast";
        tmp3[1] = "Prot";
        this.chestplatePref = new ModeSetting("Chestplate", "Preferred chestplate enchant when choosing between candidates.", "Prot", tmp3);
        var tmp4 = new String[2];
        tmp4[0] = "Blast";
        tmp4[1] = "Prot";
        this.leggingsPref = new ModeSetting("Leggings", "Preferred leggings enchant when choosing between candidates.", "Prot", tmp4);
        var tmp5 = new String[2];
        tmp5[0] = "Blast";
        tmp5[1] = "Prot";
        this.bootsPref = new ModeSetting("Boots", "Preferred boots enchant when choosing between candidates.", "Prot", tmp5);
        this.activeKit = new HashMap();
        this.activeKitName = "";
        this.ticks = 0;
        this.autoState = RekitModule.AutoState.IDLE;
        this.autoTicks = 0;
        this.emptyTicks = 0;
        this.shulkerEnderSlot = -1;
        this.placedShulkerPos = null;
        this.placedShulkerRenderMs = 0L;
        this.enderChestPos = null;
        this.hotbarSwapSettled = false;
        this.hotbarSwapAttempts = 0;
        this.placeFailStreak = 0;
        this.cursorWaitTicks = 0;
        this.displacedHotbarSlot = -1;
        this.displacedEnderSlot = -1;
        this.autoPlaceActive = false;
        this.autoPlaceBindWasDown = false;
        this.savedSlot = -1;
        restoreLastKit();
    }

    private void restoreLastKit() {
        try {
            if (!(FileUtils.fileExists("Lightning/last_kit_save.txt"))) {
                return;
                try {
                }
                Object var1 = Files.readString(Paths.get("Lightning/last_kit_save.txt", new String[0])).trim();
                if (var1.isEmpty()) {
                    return;
                    try {
                    }
                    Object var2 = new File("Lightning/Kits/", (var1 + ".json"));
                    if (!(var2.exists())) {
                        return;
                        try {
                        }
                        Object var3 = new Gson();
                        Object var4 = new FileReader(var2);
                        try {
                            Object var5 = new RekitModule.1(this).getType();
                            this.activeKit = ((Map) var3.fromJson(var4, var5));
                            this.activeKitName = var1;
                            var4.close();
                        } catch (Throwable ex9) {
                            var5 = ex9;
                            try {
                                var4.close();
                            } catch (Throwable ex12) {
                                Object var6 = ex12;
                                var5.addSuppressed(var6);
                            }
                            throw var5;
                        }
                    } catch (Exception ex15) {
                        var1 = ex15;
                    }
                }
            }
        }
    }

    private void info(String arg0) {
        LightningClient.CHAT_MANAGER.tagged(arg0, getName());
    }

    private void error(String arg0) {
        LightningClient.CHAT_MANAGER.tagged(("\u00a7c" + arg0), getName());
    }

    public List getKitNames() {
        Object var1 = new ArrayList();
        Object var2 = new File("Lightning/Kits/").listFiles();
        if (var2 == null) break label_3;
        Object var3 = var2;
        int var4 = var3.length;
        int var5 = 0;
        while (var5 < var4) {
            Object var6 = var3[var5];
            if (!(var6.getName().endsWith(".json"))) {
            } else {
                var1.add(var6.getName().replace(".json", ""));
            }
            var5++;
        }
        label_3:
        return var1;
    }

    public void saveKit(String arg0) {
        if (mc.player == null) return;
        if (mc.player.isCreative()) break label_1;
        if (mc.player.isSpectator()) {
            label_1:
            return;
        }
        Object var2 = new HashMap();
        int var3 = 0;
        label_3:
        if (var3 < 36) {
            int var4 = getHandlerSlotPlayerOnly(var3);
            Object var5 = mc.player.inventoryMenu.getSlot(var4).getItem();
            if (var5.isEmpty()) {
            } else {
                Object var6 = new RekitModule.KitItem();
                var6.id = BuiltInRegistries.ITEM.getKey(var5.getItem()).toString();
                var6.maxCount = var5.getMaxStackSize();
                if (var5.has(DataComponents.CUSTOM_NAME)) {
                    var6.name = var5.getHoverName().getString();
                }
                var2.put(var3, var6);
            }
            var3++;
            break label_3;
            try {
            }
            FileUtils.createDirectory("Lightning/Kits/");
            var3 = new GsonBuilder().setPrettyPrinting().create();
            var4 = new FileWriter(new File("Lightning/Kits/", (arg0 + ".json")));
            try {
                var3.toJson(var2, var4);
                var4.close();
            } catch (Throwable ex10) {
                var5 = ex10;
                try {
                    var4.close();
                } catch (Throwable ex13) {
                    var6 = ex13;
                    var5.addSuppressed(var6);
                }
                throw var5;
            }
            this.activeKit = var2;
            this.activeKitName = arg0;
            Files.writeString(Paths.get("Lightning/last_kit_save.txt", new String[0]), arg0, new OpenOption[0]);
            info(("Kit saved and activated: " + arg0));
        } catch (Exception ex17) {
            var3 = ex17;
            error("Error saving kit!");
        }
    }

    public void loadKit(String arg0) {
        try {
            Object var2 = new File("Lightning/Kits/", (arg0 + ".json"));
            if (!(var2.exists())) {
                error(("Kit not found: " + arg0));
                return;
                try {
                }
                Object var3 = new Gson();
                Object var4 = new FileReader(var2);
                try {
                    Object var5 = new RekitModule.2(this).getType();
                    this.activeKit = ((Map) var3.fromJson(var4, var5));
                    this.activeKitName = arg0;
                    var4.close();
                } catch (Throwable ex5) {
                    var5 = ex5;
                    try {
                        var4.close();
                    } catch (Throwable ex8) {
                        Object var6 = ex8;
                        var5.addSuppressed(var6);
                    }
                    throw var5;
                }
                Files.writeString(Paths.get("Lightning/last_kit_save.txt", new String[0]), arg0, new OpenOption[0]);
                info(("Kit loaded: " + arg0));
            } catch (Exception ex12) {
                var2 = ex12;
                error("Error occurred while reading kit!");
            }
        }
    }

    public void listKits() {
        Object var1 = new File("Lightning/Kits/").listFiles();
        if (var1 != null) {
            if (var1.length == 0) {
            }
            info("You don't have any kits.");
            return;
        }
        info("Available kits:");
        Object var2 = var1;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (!(var5.getName().endsWith(".json"))) {
                break label_5;
            }
            Object var6 = var5.getName().replace(".json", "");
            if (var6.equals(this.activeKitName)) {
                LightningClient.CHAT_MANAGER.tagged(("\u00a79- " + var6 + " [active]"), getName());
                break label_5;
            }
            LightningClient.CHAT_MANAGER.tagged(("\u00a77- " + var6), getName());
            label_5:
            var4++;
        }
    }

    public void showActiveKit() {
        if (this.activeKitName != null) {
            if (this.activeKitName.isEmpty()) {
            }
            error("No kit is currently active.");
        } else {
            LightningClient.CHAT_MANAGER.tagged(("\u00a7aActive kit: \u00a79" + this.activeKitName), getName());
        }
    }

    public void deleteKit(String arg0) {
        Object var2 = new File("Lightning/Kits/", (arg0 + ".json"));
        if (var2.exists() && var2.delete()) {
            if (arg0.equals(this.activeKitName)) {
                this.activeKit.clear();
                this.activeKitName = "";
            }
            info(("Kit deleted: " + arg0));
        } else {
            error("Failed to delete kit.");
        }
    }

    public void onEnable() {
        this.autoState = RekitModule.AutoState.IDLE;
        this.autoTicks = 0;
        this.emptyTicks = 0;
        this.shulkerEnderSlot = -1;
        this.placedShulkerPos = null;
        this.enderChestPos = null;
        this.hotbarSwapSettled = false;
        this.cursorWaitTicks = 0;
        this.displacedHotbarSlot = -1;
        this.displacedEnderSlot = -1;
        this.autoPlaceActive = false;
        this.autoPlaceBindWasDown = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (mc.player == null) return;
        if (mc.gui.screen() == null) {
            int var2 = KeyboardUtils.isBindDown(this.autoPlaceBind.getValue());
            if (!(!var2) && !this.autoPlaceBindWasDown && this.autoState == RekitModule.AutoState.IDLE && !(mc.player.isCreative()) && !(mc.player.isSpectator()) && findShulkerInInventory() != -1) {
                this.autoPlaceActive = true;
                this.autoState = RekitModule.AutoState.PLACE_SHULKER;
                this.autoTicks = 0;
                this.hotbarSwapSettled = false;
                this.hotbarSwapAttempts = 0;
            }
            this.autoPlaceBindWasDown = var2;
        } else {
            this.autoPlaceBindWasDown = false;
        }
        if ((mc.player.isCreative()) || (mc.player.isSpectator())) {
            return;
        }
        if (!(this.auto.getValue())) {
            if (!(!this.autoPlaceActive)) {
            }
            handleAutoTick();
            return;
        }
        hideContainerScreenIfSilent();
        if (!(mc.player.hasContainerOpen())) return;
        manualPullTick();
    }

    private void hideContainerScreenIfSilent() {
        if (this.silentContainer.getValue() && (mc.gui.screen() instanceof AbstractContainerScreen) && !(mc.gui.screen() instanceof InventoryScreen)) {
            mc.gui.setScreen(null);
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldEvent arg0) {
        if ((!(this.auto.getValue())) || (this.placedShulkerPos == null)) {
            return;
        }
        long var2 = (System.currentTimeMillis() - this.placedShulkerRenderMs);
        float var4 = Math.min(1.0f, (((float) var2) / 400.0f));
        float var5 = (0.15f + (0.85f * var4));
        double var6 = (((double) this.placedShulkerPos.getX()) + 0.5);
        double var8 = (((double) this.placedShulkerPos.getY()) + 0.5);
        double var10 = (((double) this.placedShulkerPos.getZ()) + 0.5);
        double var12 = (0.5 * ((double) var5));
        Object var14 = new AABB((var6 - var12), (var8 - var12), (var10 - var12), (var6 + var12), (var8 + var12), (var10 + var12));
        Renderer3D.renderBoxOutline(arg0.getMatrices(), var14, new Color(255, 220, 0, 230));
    }

    private boolean hasCustomContainerTitle() {
        Object var1 = mc.gui.screen();
        if (!((var1 instanceof AbstractContainerScreen))) return false;
        Object var2 = ((AbstractContainerScreen) var1);
        Object var3 = var2.getTitle();
        if (var3 == null) return false;
        return !((var3.getContents() instanceof TranslatableContents));
    }

    private void manualPullTick() {
        if (this.activeKit.isEmpty()) return;
        if (this.ignoreCustomName.getValue() && hasCustomContainerTitle() && !(mc.player.containerMenu instanceof ShulkerBoxMenu)) return;
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        this.ticks = 0;
        int var1 = 0;
        while (var1 < this.actionsPerTick.getValue().intValue() && pullFromContainerTick()) {
            var1++;
        }
    }

    private void handleAutoTick() {
        hideContainerScreenIfSilent();
        int var1 = mc.player.hasContainerOpen();
        if (this.autoState != RekitModule.AutoState.IDLE) break label_4;
        this.autoPlaceActive = false;
        if (!var1) break label_3;
        if ((mc.player.containerMenu instanceof ShulkerBoxMenu)) {
            manualPullTick();
            return;
        }
        detectEnderChestPos();
        if (this.enderChestPos != null) break label_1;
        manualPullTick();
        return;
        label_1:
        if (!(this.activeKit.isEmpty())) break label_2;
        setToggled(false);
        return;
        label_2:
        this.autoState = RekitModule.AutoState.FIND_SHULKER;
        this.autoTicks = 0;
        this.emptyTicks = 0;
        label_3:
        return;
        label_4:
        if (this.autoState == RekitModule.AutoState.FIND_SHULKER) {
            if (!var1) return;
            if (!(isKitComplete())) break label_6;
            mc.player.closeContainer();
            this.autoState = RekitModule.AutoState.IDLE;
            return;
            label_6:
            int var2 = (mc.player.containerMenu.slots.size() - 36);
            if (var2 <= 0) return;
            this.shulkerEnderSlot = findShulkerInContainer(var2);
            if (this.shulkerEnderSlot == -1) {
                error("Auto stopped: no shulker has the items this kit still needs.");
                mc.player.closeContainer();
                this.autoState = RekitModule.AutoState.IDLE;
                setToggled(false);
                return;
            }
            this.autoState = RekitModule.AutoState.GRAB_SHULKER;
            this.autoTicks = 0;
            return;
        }
        if (this.autoState == RekitModule.AutoState.GRAB_SHULKER) {
            if (!var1) return;
            var2 = (mc.player.containerMenu.slots.size() - 36);
            if (var2 <= 0) return;
            Object var3 = mc.player.containerMenu;
            Object var4 = var3.getSlot(this.shulkerEnderSlot).getItem();
            if (!(isShulkerBox(var4))) {
                this.autoState = RekitModule.AutoState.FIND_SHULKER;
                return;
            }
            int var5 = firstEmptyHotbarSlot();
            if (var5 != -1) {
                mc.gameMode.handleContainerInput(var3.containerId, this.shulkerEnderSlot, var5, ContainerInput.SWAP, mc.player);
            } else {
                int var6 = (!(mc.player.getInventory().getSelectedSlot()) ? 1 : 0);
                mc.gameMode.handleContainerInput(var3.containerId, this.shulkerEnderSlot, var6, ContainerInput.SWAP, mc.player);
                this.displacedHotbarSlot = var6;
                this.displacedEnderSlot = this.shulkerEnderSlot;
            }
            this.autoState = RekitModule.AutoState.PLACE_SHULKER;
            this.autoTicks = 0;
            this.hotbarSwapSettled = false;
            this.hotbarSwapAttempts = 0;
            return;
        }
        if (this.autoState == RekitModule.AutoState.PLACE_SHULKER) {
            if (!(!var1)) {
                mc.player.closeContainer();
                return;
            }
            if (this.autoTicks < 1) {
                this.autoTicks = (this.autoTicks + 1);
                return;
            }
            var3 = findShulkerInInventory();
            if (var3 == -1) {
                this.autoState = RekitModule.AutoState.IDLE;
                return;
            }
            if (var3 > 8 && !this.hotbarSwapSettled) {
                ensureHotbar(var3);
                this.hotbarSwapSettled = true;
                return;
            }
            if (var3 > 8) {
                this.hotbarSwapAttempts = (this.hotbarSwapAttempts + 1);
                if (this.hotbarSwapAttempts > 3) {
                    this.placeFailStreak = (this.placeFailStreak + 1);
                    this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                    return;
                }
                this.hotbarSwapSettled = false;
                return;
            }
            var4 = findPlaceableSpot();
            if (var4 == null) {
                this.placeFailStreak = (this.placeFailStreak + 1);
                this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                return;
            }
            var5 = getHitResultForPlace(var4);
            if (var5 == null) {
                this.placeFailStreak = (this.placeFailStreak + 1);
                this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                return;
            }
            var3 = findShulkerInInventory();
            if (var3 >= 0) {
                if (var3 > 8) {
                }
                this.placeFailStreak = (this.placeFailStreak + 1);
                this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                return;
            }
            LightningClient.ROTATION_MANAGER.beginBatchRotation();
            if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
                var6 = RotationUtils.getRotations(var5.getLocation());
                LightningClient.ROTATION_MANAGER.batchRotate(var6[0], var6[1]);
            }
            swapToSlotNormal(var3);
            if ((!(isPositionClear(var4))) || (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, var5) == InteractionResult.FAIL)) break label_30;
            var2 = (!(mc.level.getBlockState(var4).canBeReplaced()) ? 1 : 0);
            LightningClient.ROTATION_MANAGER.endBatchRotation();
            break label_35;
            Object var7 = null;
            LightningClient.ROTATION_MANAGER.endBatchRotation();
            throw var7;
            label_35:
            if (!var2) {
                swapBackNormal();
                this.placeFailStreak = (this.placeFailStreak + 1);
                this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                return;
            }
            this.placedShulkerPos = var4;
            this.placedShulkerRenderMs = System.currentTimeMillis();
            this.autoState = RekitModule.AutoState.OPEN_SHULKER;
            this.autoTicks = 0;
            return;
        }
        if (this.autoState == RekitModule.AutoState.OPEN_SHULKER) {
            if (!(!var1)) {
                swapBackNormal();
                this.autoState = RekitModule.AutoState.PULL_ITEMS;
                this.autoTicks = 0;
                this.emptyTicks = 0;
                this.placeFailStreak = 0;
                return;
            }
            if (!(!this.autoTicks)) {
                if ((this.autoTicks % 4) == 0) {
                }
                var2 = new BlockHitResult(Vec3.atCenterOf(this.placedShulkerPos), Direction.UP, this.placedShulkerPos, false);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, var2);
            }
            this.autoTicks = (this.autoTicks + 1);
            if (this.autoTicks > 60) {
                swapBackNormal();
                this.autoState = RekitModule.AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
            }
            return;
        }
        if (this.autoState == RekitModule.AutoState.PULL_ITEMS) {
            if (!var1) {
                this.autoState = RekitModule.AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.activeKit.isEmpty()) {
                this.autoState = RekitModule.AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.ticks < this.delay.getValue().intValue()) {
                this.ticks = (this.ticks + 1);
                return;
            }
            this.ticks = 0;
            var2 = 0;
            var3 = (mc.player.containerMenu.slots.size() - 36);
            if (var3 <= 0) break label_47;
            var4 = 0;
            while (var4 < this.actionsPerTick.getValue().intValue() && pullFromContainerTick()) {
                var2 = 1;
                var4++;
            }
            label_47:
            if (!(!var2)) {
            } else {
                this.emptyTicks = (this.emptyTicks + 1);
            }
            0.emptyTicks = (this.emptyTicks + 1);
            if (this.emptyTicks >= 5) {
                this.autoState = RekitModule.AutoState.BREAK_SHULKER;
                this.autoTicks = 0;
            }
            return;
        }
        if (this.autoState == RekitModule.AutoState.BREAK_SHULKER) {
            if (!(!var1)) {
                mc.player.closeContainer();
                return;
            }
            if (this.autoTicks < 2) {
                this.autoTicks = (this.autoTicks + 1);
                return;
            }
            if (this.placedShulkerPos != null) {
                if (mc.level == null) {
                }
                this.autoState = RekitModule.AutoState.IDLE;
                return;
            }
            if (mc.level.getBlockState(this.placedShulkerPos).canBeReplaced()) {
                if (findShulkerInInventory() == -1 && this.autoTicks < 100) {
                    this.autoTicks = (this.autoTicks + 1);
                    return;
                }
                this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                this.autoTicks = 0;
                return;
            }
            if (this.autoTicks > 200) {
                this.autoState = RekitModule.AutoState.RETURN_SHULKER;
                this.autoTicks = 0;
                return;
            }
            instantBreak(this.placedShulkerPos);
            this.autoTicks = (this.autoTicks + 1);
            return;
        }
        if (this.autoState == RekitModule.AutoState.RETURN_SHULKER) {
            if (this.enderChestPos == null) {
                this.autoState = RekitModule.AutoState.IDLE;
                return;
            }
            if (!var1) {
                if (this.autoTicks < 2) {
                    this.autoTicks = (this.autoTicks + 1);
                    return;
                }
                if (this.autoTicks == 2) {
                    var2 = new BlockHitResult(Vec3.atCenterOf(this.enderChestPos), Direction.UP, this.enderChestPos, false);
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, var2);
                }
                this.autoTicks = (this.autoTicks + 1);
                if (this.autoTicks > 40) {
                    this.autoState = RekitModule.AutoState.IDLE;
                }
                return;
            }
            this.autoTicks = 0;
            var2 = findShulkerInInventory();
            if (var2 == -1) {
                if (this.placedShulkerPos != null && mc.level != null && !(mc.level.getBlockState(this.placedShulkerPos).canBeReplaced())) {
                    this.autoState = RekitModule.AutoState.BREAK_SHULKER;
                    this.autoTicks = 0;
                    return;
                }
                this.autoState = RekitModule.AutoState.FIND_SHULKER;
                this.autoTicks = 0;
                return;
            }
            var3 = (mc.player.containerMenu.slots.size() - 36);
            if (var3 <= 0) return;
            var4 = mc.player.containerMenu;
            if (this.displacedHotbarSlot != -1) {
                var5 = getPlayerHandlerSlot(var3, var2);
                click(var4.containerId, var5, 0, ContainerInput.PICKUP);
                click(var4.containerId, this.displacedEnderSlot, 0, ContainerInput.PICKUP);
                var6 = getPlayerHandlerSlot(var3, this.displacedHotbarSlot);
                click(var4.containerId, var6, 0, ContainerInput.PICKUP);
                this.displacedHotbarSlot = -1;
                this.displacedEnderSlot = -1;
            } else {
                var5 = -1;
                var6 = 0;
                while (var6 < var3) {
                    if (!(var4.getSlot(var6).getItem().isEmpty())) {
                    } else {
                        var5 = var6;
                        break label_72;
                    }
                    var6++;
                }
                label_72:
                if (var5 == -1) {
                    this.autoState = RekitModule.AutoState.IDLE;
                    return;
                }
                var6 = getPlayerHandlerSlot(var3, var2);
                atomicSwap(var4.containerId, var6, var5);
            }
            this.placedShulkerPos = null;
            this.shulkerEnderSlot = -1;
            if (this.placeFailStreak >= 3) {
                error(("Auto stopped: could not place/open a shulker after " + this.placeFailStreak + " attempts."));
                mc.player.closeContainer();
                this.autoState = RekitModule.AutoState.IDLE;
                setToggled(false);
                this.placeFailStreak = 0;
                break label_77;
            }
            if (isKitComplete()) {
                mc.player.closeContainer();
                this.autoState = RekitModule.AutoState.IDLE;
                break label_77;
            }
            this.autoState = RekitModule.AutoState.FIND_SHULKER;
            label_77:
            this.autoTicks = 0;
        }
    }

    private void detectEnderChestPos() {
        Object var2 = mc.hitResult;
        if (!((var2 instanceof BlockHitResult))) break label_0;
        var tmp0 = ((BlockHitResult) var2);
        Object var1 = tmp0;
        if (!(mc.level.getBlockState(tmp0.getBlockPos()).is(Blocks.ENDER_CHEST))) break label_0;
        this.enderChestPos = var1.getBlockPos();
        return;
        label_0:
        Object var3 = mc.player.blockPosition();
        int var4 = -3;
        while (var4 <= 3) {
            int var5 = -1;
            while (var5 <= 3) {
                int var6 = -3;
                while (var6 <= 3) {
                    Object var7 = var3.offset(var4, var5, var6);
                    if (!(mc.level.getBlockState(var7).is(Blocks.ENDER_CHEST))) {
                    } else {
                        this.enderChestPos = var7;
                        return;
                    }
                    var6++;
                }
                var5++;
            }
            var4++;
        }
    }

    private int findShulkerInContainer(int arg0) {
        Object var2 = mc.player.containerMenu;
        Object var3 = new HashSet();
        Object var4 = this.activeKit.entrySet().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Map.Entry) var4.next());
            Object var6 = mc.player.getInventory().getItem(((Integer) var5.getKey()).intValue());
            if ((((var5.getValue() == null) || (((RekitModule.KitItem) var5.getValue()).id == null)) || (((RekitModule.KitItem) var5.getValue()).id.isEmpty())) || (isCorrectItem(var6, ((RekitModule.KitItem) var5.getValue())))) break label_1;
            Object var7 = Identifier.tryParse(((RekitModule.KitItem) var5.getValue()).id);
            if (var7 == null) break label_1;
            Object var8 = ((Item) BuiltInRegistries.ITEM.getValue(var7));
            if (var8 == null) break label_1;
            var3.add(var8);
            label_1:
        }
        var4 = -1;
        var5 = -1;
        var6 = 0;
        while (var6 < arg0) {
            var7 = var2.getSlot(var6).getItem();
            if (!(isShulkerBox(var7))) break label_10;
            var8 = 0;
            Object var9 = ((ItemContainerContents) var7.get(DataComponents.CONTAINER));
            if (var9 == null) break label_8;
            var tmp0 = var9.nonEmptyItemCopyStream();
            Objects.requireNonNull(tmp0);
            Object var10 = tmp0::iterator.iterator();
            while (var10.hasNext()) {
                Object var11 = ((ItemStack) var10.next());
                if (var3.contains(var11.getItem())) {
                    var8 = (var8 + (var11.getMaxStackSize() == 1 ? 100 : 1));
                }
            }
            label_8:
            if (this.considerShulkerName.getValue() && !(this.activeKitName.isEmpty()) && var7.has(DataComponents.CUSTOM_NAME) && normalizeName(var7.getHoverName().getString()).equalsIgnoreCase(normalizeName(this.activeKitName))) {
                var8 = (var8 + 1000000);
            }
            if (var8 <= var5) break label_10;
            var5 = var8;
            var4 = var6;
            label_10:
            var6++;
        }
        return (var5 <= 0 ? -1 : var4);
    }

    private int ensureHotbar(int arg0) {
        if (arg0 >= 0 && arg0 <= 8) return arg0;
        int var2 = mc.player.getInventory().getSelectedSlot();
        mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, getHandlerSlotPlayerOnly(arg0), var2, ContainerInput.SWAP, mc.player);
        return var2;
    }

    private int findShulkerInInventory() {
        int var1 = 0;
        while (var1 < 36) {
            if (!(isShulkerBox(mc.player.getInventory().getItem(var1)))) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

    private int firstEmptyHotbarSlot() {
        int var1 = 0;
        while (var1 <= 8) {
            if (!(mc.player.getInventory().getItem(var1).isEmpty())) {
            } else {
                return var1;
            }
            var1++;
        }
        return -1;
    }

    private Player getActiveCombatOpponent() {
        Object var1 = mc.player.getLastHurtByMob();
        if (!((var1 instanceof Player))) return null;
        Object var2 = ((Player) var1);
        long var3 = (mc.level.getGameTime() - ((long) mc.player.getLastHurtByMobTimestamp()));
        return (var3 <= 100L ? var2 : null);
    }

    private boolean isPositionClear(BlockPos arg0) {
        return mc.level.getEntities(((Entity) null), new AABB(arg0), Entity::isAlive).isEmpty();
    }

    private BlockPos findPlaceableSpot() {
        Object var1 = mc.player.blockPosition();
        Object var2 = getActiveCombatOpponent();
        int var3 = ((int) Math.ceil(mc.player.blockInteractionRange()));
        Object var4 = null;
        double var5 = 1.7976931348623157E308;
        int var7 = -(var3);
        while (var7 <= var3) {
            int var8 = -(var3);
            while (var8 <= var3) {
                Object var11 = var1.offset(var7, 0, var8);
                if ((((((var11.distSqr(var1) > (((double) var3) * ((double) var3))) || (var11.below().equals(this.enderChestPos))) || (!(isPositionClear(var11)))) || (!(mc.level.getBlockState(var11).canBeReplaced()))) || (mc.level.getBlockState(var11.below()).canBeReplaced())) || (mc.level.getBlockState(var11.below()).is(Blocks.AIR))) break label_6;
                if (!(mc.level.getBlockState(var11.above()).canBeReplaced())) {
                    break label_6;
                }
                if (var2 != null) {
                } else {
                }
                double var9 = ((double) ((var7 * var7) + (var8 * var8)));
                double var12 = ((double) ((var7 * var7) + (var8 * var8)));
                if (var9 >= var5) {
                    break label_6;
                }
                var5 = var9;
                var4 = var11;
                label_6:
                var8++;
            }
            var7++;
        }
        return var4;
    }

    private BlockHitResult getHitResultForPlace(BlockPos arg0) {
        Object var2 = WorldUtils.getDirection(arg0, null, this.strictDirection.getValue());
        if (var2 == null) return null;
        Object var3 = arg0.relative(var2);
        Object var4 = Vec3.atCenterOf(var3).add((((double) -(var2.getStepX())) * 0.48), (((double) -(var2.getStepY())) * 0.48), (((double) -(var2.getStepZ())) * 0.48));
        return new BlockHitResult(var4, var2.getOpposite(), var3, false);
    }

    private boolean isKitComplete() {
        if (this.activeKit.isEmpty()) return true;
        Object var1 = this.activeKit.entrySet().iterator();
        label_1:
        if (var1.hasNext()) {
            Object var2 = ((Map.Entry) var1.next());
            if (isCorrectItem(mc.player.getInventory().getItem(((Integer) var2.getKey()).intValue()), ((RekitModule.KitItem) var2.getValue()))) {
                break label_1;
            }
            return false;
        }
        return true;
    }

    private boolean pullFromContainerTick() {
        Object var5 = mc.player.containerMenu;
        int var6 = (var5.slots.size() - 36);
        if (var6 <= 0) return false;
        if (!(var5.getCarried().isEmpty())) {
            this.cursorWaitTicks = (this.cursorWaitTicks + 1);
            if (this.cursorWaitTicks < 20) return false;
            this.cursorWaitTicks = 0;
            int var8 = -1;
            int var7 = 0;
            while (var7 < var6) {
                if (!(var5.getSlot(var7).getItem().isEmpty())) {
                } else {
                    var8 = var7;
                    break label_5;
                }
                var7++;
            }
            label_5:
            if (var8 != -1) break label_9;
            var7 = 0;
            while (var7 < 36) {
                int var9 = getPlayerHandlerSlot(var6, var7);
                if (!(var5.getSlot(var9).getItem().isEmpty())) {
                } else {
                    var8 = var9;
                    break label_9;
                }
                var7++;
            }
            label_9:
            if (var8 != -1) {
                click(var5.containerId, var8, 0, ContainerInput.PICKUP);
                return true;
            }
            return false;
        }
        this.cursorWaitTicks = 0;
        int var4 = 0;
        while (var4 < 36) {
            Object var3 = ((RekitModule.KitItem) this.activeKit.get(var4));
            if (var3 == null) {
                break label_19;
            }
            int var2 = getPlayerHandlerSlot(var6, var4);
            Object var1 = var5.getSlot(var2).getItem();
            var8 = isCorrectItem(var1, var3);
            if (!var8) {
                if (isShulkerBox(var1)) break label_19;
                var tmp0 = findBestItemInContainer(var5, var6, var3);
                var9 = tmp0;
                if (tmp0 == -1) {
                    break label_19;
                }
                atomicSwap(var5.containerId, var9, var2);
                return true;
            }
            if (var1.getCount() >= var1.getMaxStackSize()) {
                break label_19;
            }
            var9 = findExactItemInContainer(var5, var6, var1);
            if (var9 != -1) {
                atomicSwap(var5.containerId, var9, var2);
                return true;
            }
            int var10 = findBestItemInContainer(var5, var6, var3);
            if (var10 == -1) break label_19;
            var tmp1 = var5.getSlot(var10).getItem();
            var7 = tmp1;
            if (tmp1.getCount() <= var1.getCount()) {
                break label_19;
            }
            atomicSwap(var5.containerId, var10, var2);
            return true;
            label_19:
            var4++;
        }
        var4 = 0;
        while (var4 < 36) {
            var3 = ((RekitModule.KitItem) this.activeKit.get(var4));
            if (var3 == null) break label_24;
            var tmp2 = getPlayerHandlerSlot(var6, var4);
            var2 = tmp2;
            var tmp3 = var5.getSlot(tmp2).getItem();
            var1 = tmp3;
            if (!(isShulkerBox(tmp3))) break label_24;
            if (isItemCompensated(var5, var6, var3)) {
                break label_24;
            }
            var7 = findBestItemInContainer(var5, var6, var3);
            var8 = findEmptyUnassignedSlot(var5, var6);
            if (var7 == -1) break label_24;
            if (var8 == -1) {
                break label_24;
            }
            atomicSwap(var5.containerId, var7, var8);
            return true;
            label_24:
            var4++;
        }
        return false;
    }

    private void atomicSwap(int arg0, int arg1, int arg2) {
        click(arg0, arg1, 0, ContainerInput.PICKUP);
        click(arg0, arg2, 0, ContainerInput.PICKUP);
        click(arg0, arg1, 0, ContainerInput.PICKUP);
    }

    private void click(int arg0, int arg1, int arg2, ContainerInput arg3) {
        lastContainerActionMs = System.currentTimeMillis();
        mc.gameMode.handleContainerInput(arg0, arg1, arg2, arg3, mc.player);
    }

    private void swapToSlotNormal(int arg0) {
        this.savedSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(arg0);
        ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
    }

    private void swapBackNormal() {
        if (this.savedSlot == -1) return;
        mc.player.getInventory().setSelectedSlot(this.savedSlot);
        ((ClientPlayerInteractionManagerAccessor) mc.gameMode).invokeSyncSelectedSlot();
        this.savedSlot = -1;
    }

    private void instantBreak(BlockPos arg0) {
        Object var2 = Direction.UP;
        Object var3 = ((SpeedMineModule) LightningClient.MODULE_MANAGER.getModule(SpeedMineModule.class));
        if (var3 == null) break label_0;
        int var4 = (var3.farReach.getValue() ? 1 : 0);
        LightningClient.ROTATION_MANAGER.beginBatchRotation();
        if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
            Object var5 = RotationUtils.getRotations(WorldUtils.getHitVector(arg0, var2));
            LightningClient.ROTATION_MANAGER.batchRotate(var5[0], var5[1]);
        }
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, arg0, var2));
        if (!(!var4)) {
            var5 = arg0.below(2000);
            mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, var5, WorldUtils.getClosestDirection(var5, true)));
        }
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, arg0, var2));
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        break label_8;
        Object var6 = null;
        LightningClient.ROTATION_MANAGER.endBatchRotation();
        throw var6;
        label_8:
    }

    private boolean isCorrectItem(ItemStack arg0, RekitModule.KitItem arg1) {
        if (arg1 == null) return true;
        if (arg0.isEmpty()) return false;
        if ((arg1.id == null) || (arg1.id.isEmpty())) {
            return false;
        }
        Object var3 = Identifier.tryParse(arg1.id);
        if (var3 == null) return false;
        Object var4 = ((Item) BuiltInRegistries.ITEM.getValue(var3));
        if (var4 == null) break label_5;
        return arg0.getItem() == var4;
    }

    private int findBestItemInContainer(AbstractContainerMenu arg0, int arg1, RekitModule.KitItem arg2) {
        if (((arg2 == null) || (arg2.id == null)) || (arg2.id.isEmpty())) {
            return -1;
        }
        Object var4 = Identifier.tryParse(arg2.id);
        if (var4 == null) return -1;
        Object var5 = ((Item) BuiltInRegistries.ITEM.getValue(var4));
        if (var5 == null) return -1;
        int var6 = -1;
        long var7 = -1L;
        int var9 = 0;
        while (var9 < arg1) {
            Object var12 = arg0.getSlot(var9).getItem();
            if ((var12.isEmpty()) || (var12.getItem() != var5)) break label_6;
            long var10 = scoreCandidate(var12);
            if (scoreCandidate(var12) <= var7) {
                break label_6;
            }
            var7 = var10;
            var6 = var9;
            label_6:
            var9++;
        }
        return var6;
    }

    private long scoreCandidate(ItemStack arg0) {
        if (arg0.getMaxStackSize() > 1) return ((long) arg0.getCount());
        long var2 = 0L;
        Object var4 = preferredEnchantId(arg0);
        if (var4 != null && hasEnchant(arg0, var4)) {
            var2 = (var2 + 1000000L);
        }
        if (arg0.isDamageableItem()) {
            var2 = (var2 + ((long) (arg0.getMaxDamage() - arg0.getDamageValue())));
        }
        return var2;
    }

    private String preferredEnchantId(ItemStack arg0) {
        Object var3 = BuiltInRegistries.ITEM.getKey(arg0.getItem()).getPath();
        if (var3.endsWith("_pickaxe")) return (this.pickaxePref.getValue().equalsIgnoreCase("SilkTouch") ? "silk_touch" : "efficiency");
        if (!(var3.endsWith("_helmet"))) {
            if (var3.equals("turtle_helmet")) {
            }
            Object var2 = this.helmetPref.getValue();
            break label_8;
        }
        if (var3.endsWith("_chestplate")) {
            var2 = this.chestplatePref.getValue();
            break label_8;
        }
        if (var3.endsWith("_leggings")) {
            var2 = this.leggingsPref.getValue();
            break label_8;
        }
        if (var3.endsWith("_boots")) {
            var2 = this.bootsPref.getValue();
            break label_8;
        }
        return null;
        label_8:
        return (var2.equalsIgnoreCase("Blast") ? "blast_protection" : "protection");
    }

    private boolean hasEnchant(ItemStack arg0, String arg1) {
        Object var3 = ((ItemEnchantments) arg0.get(DataComponents.ENCHANTMENTS));
        if ((var3 == null) || (var3.isEmpty())) {
            return false;
        }
        Object var4 = Identifier.tryParse(("minecraft:" + arg1));
        if (var4 == null) return false;
        Object var5 = var3.keySet().iterator();
        label_3:
        if (var5.hasNext()) {
            Object var6 = ((Holder) var5.next());
            if (!(var6.is(var4))) {
                break label_3;
            }
            return true;
        }
        return false;
    }

    private int findExactItemInContainer(AbstractContainerMenu arg0, int arg1, ItemStack arg2) {
        int var4 = 0;
        while (var4 < arg1) {
            Object var5 = arg0.getSlot(var4).getItem();
            if (var5.isEmpty()) break label_2;
            if (!(ItemStack.isSameItemSameComponents(var5, arg2))) {
                break label_2;
            }
            return var4;
            label_2:
            var4++;
        }
        return -1;
    }

    private int getPlayerHandlerSlot(int arg0, int arg1) {
        if (arg1 >= 0 && arg1 <= 8) return ((arg0 + 27) + arg1);
        if (arg1 >= 9 && arg1 <= 35) return (arg0 + (arg1 - 9));
        return -1;
    }

    private int getHandlerSlotPlayerOnly(int arg0) {
        if (arg0 >= 0 && arg0 <= 8) return (36 + arg0);
        if (arg0 >= 9 && arg0 <= 35) return arg0;
        return -1;
    }

    private static String normalizeName(String arg0) {
        return Normalizer.normalize(arg0, Normalizer.Form.NFKC).trim();
    }

    private boolean isShulkerBox(ItemStack arg0) {
        if (arg0.isEmpty()) break label_0;
        var tmp0 = arg0.getItem();
        Object var3 = tmp0;
        if (!((tmp0 instanceof BlockItem))) break label_0;
        var tmp1 = ((BlockItem) var3);
        Object var2 = tmp1;
        return (tmp1.getBlock() instanceof ShulkerBoxBlock);
    }

    private boolean isItemCompensated(AbstractContainerMenu arg0, int arg1, RekitModule.KitItem arg2) {
        if (((arg2 == null) || (arg2.id == null)) || (arg2.id.isEmpty())) {
            return false;
        }
        Object var4 = Identifier.tryParse(arg2.id);
        if (var4 == null) return false;
        Object var5 = ((Item) BuiltInRegistries.ITEM.getValue(var4));
        if (var5 == null) return false;
        int var6 = 0;
        while (var6 < 36) {
            if (this.activeKit.get(var6) != null) break label_6;
            var tmp0 = getPlayerHandlerSlot(arg1, var6);
            int var7 = tmp0;
            if (arg0.getSlot(tmp0).getItem().getItem() != var5) {
                break label_6;
            }
            return true;
            label_6:
            var6++;
        }
        return false;
    }

    private int findEmptyUnassignedSlot(AbstractContainerMenu arg0, int arg1) {
        int var3 = 0;
        while (var3 < 36) {
            if (this.activeKit.get(var3) != null) break label_2;
            var tmp0 = getPlayerHandlerSlot(arg1, var3);
            int var4 = tmp0;
            if (!(arg0.getSlot(tmp0).getItem().isEmpty())) {
                break label_2;
            }
            return var4;
            label_2:
            var3++;
        }
        return -1;
    }

    @Generated
    public Map getActiveKit() {
        return this.activeKit;
    }

    @Generated
    public String getActiveKitName() {
        return this.activeKitName;
    }

    static {
        lastContainerActionMs = 0L;
    }


    public class KitItem {
        public String id;
        public String name;
        public int maxCount;

        public KitItem() {
        }

    }

    public enum PickaxePref {
        Efficiency,
        SilkTouch;


        private PickaxePref() {
        }

    }

    enum AutoState {
        IDLE,
        FIND_SHULKER,
        GRAB_SHULKER,
        PLACE_SHULKER,
        OPEN_SHULKER,
        PULL_ITEMS,
        BREAK_SHULKER,
        RETURN_SHULKER;


        private AutoState() {
        }

    }

    class 2 extends TypeToken {
        2(RekitModule arg0) {
            Objects.requireNonNull(arg0);
        }

    }

    class 1 extends TypeToken {
        1(RekitModule arg0) {
            Objects.requireNonNull(arg0);
        }

    }

    public enum ArmorPref {
        Blast,
        Prot;


        private ArmorPref() {
        }

    }
}
