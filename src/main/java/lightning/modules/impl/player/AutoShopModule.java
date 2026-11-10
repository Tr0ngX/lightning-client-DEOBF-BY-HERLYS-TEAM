package lightning.modules.impl.player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.NumberSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@RegisterModule(name = "AutoShop", description = "Automates the server /shop GUI to buy totems, end crystals, or exp bottles.", category = Module.Category.PLAYER, persistent = true)
public class AutoShopModule extends Module {
    public CategorySetting totemCategory;
    public BooleanSetting totemBelowThreshold;
    public BindSetting totem;
    public BindSetting totemToggle;
    public NumberSetting totemAmount;
    public NumberSetting totemLostCount;
    public CategorySetting crystalCategory;
    public BooleanSetting crystalBelowThreshold;
    public BindSetting crystal;
    public NumberSetting crystalStacks;
    public NumberSetting crystalThreshold;
    public CategorySetting expCategory;
    public BooleanSetting expBelowThreshold;
    public BindSetting exp;
    public NumberSetting expStacks;
    public NumberSetting expThreshold;
    public CategorySetting obsidianCategory;
    public BooleanSetting obsidianBelowThreshold;
    public BindSetting obsidian;
    public NumberSetting obsidianStacks;
    public NumberSetting obsidianThreshold;
    public CategorySetting enderPearlCategory;
    public BooleanSetting enderPearlBelowThreshold;
    public BindSetting enderPearl;
    public NumberSetting enderPearlStacks;
    public NumberSetting enderPearlThreshold;
    public BooleanSetting silent;
    public NumberSetting actionDelay;
    private static final int SLOT_ITEM_TOTEM = 13;
    private static final int SLOT_ITEM_CRYSTAL = 10;
    private static final int SLOT_ITEM_EXP = 16;
    private static final int SLOT_ITEM_OBSIDIAN = 9;
    private static final int SLOT_ITEM_ENDERPEARL = 14;
    private static final int SLOT_SET_1 = 15;
    private static final int SLOT_SET_11 = 16;
    private static final int SLOT_CONFIRM = 23;
    private static final int STEP_TIMEOUT_TICKS = 100;
    private AutoShopModule.State state;
    private final Queue orderQueue;
    private AutoShopModule.PurchaseOrder currentOrder;
    private int ticks;
    private int timeoutTicks;
    private boolean quantitySet;
    private final AutoShopModule.AutoControl[] autoControls;
    private boolean totemTrackingInitialized;
    private boolean totemContainerResyncPending;
    private boolean totemZeroFailsafePending;
    private boolean totemZeroFailsafeUsed;
    private boolean totemOrderRecovered;
    private String totemEpisodeTriggerReason;
    private int baselineTotems;
    private int previousObservedTotems;
    private Object lastWorld;
    private Object lastPlayer;
    private long generation;
    private long expectedGeneration;
    private AutoShopModule.ResponseReason expectedReason;
    private long expectationExpiresAt;
    private boolean realShopScreenIntercepted;
    private boolean silentSession;
    public BooleanSetting antiGhostTotem;
    private String lastBuyMsg;

    public AutoShopModule() {
        this.totemCategory = new CategorySetting("Totem", "Automatic Totem restock and manual purchase settings.");
        this.totemBelowThreshold = new BooleanSetting("TotemBelowThreshold", "Enabled", "Automatically restock when Totem stock is at or below the threshold.", new CategorySetting.Visibility(this.totemCategory), false);
        this.totem = new BindSetting("Totem", "Bind", "Keybind to auto buy totems.", new CategorySetting.Visibility(this.totemCategory), 0).disableHoldModes();
        this.totemToggle = new BindSetting("TotemToggle", "Toggle", "Keybind to turn auto buy totems on/off.", new CategorySetting.Visibility(this.totemCategory), 0).disableHoldModes();
        this.totemAmount = new NumberSetting("TotemAmount", "Amount", "How many totems to buy.", new CategorySetting.Visibility(this.totemCategory), 1, 1, 10);
        this.totemLostCount = new NumberSetting("TotemLostCount", "Stock Threshold", "Automatically buy when total Totem stock is at or below this threshold.", new CategorySetting.Visibility(this.totemCategory), 7, 1, 64);
        this.crystalCategory = new CategorySetting("Crystal", "Automatic Crystal restock and manual purchase settings.");
        this.crystalBelowThreshold = new BooleanSetting("CrystalBelowThreshold", "Enabled", "Buy one Crystal order when stock reaches the threshold.", new CategorySetting.Visibility(this.crystalCategory), false);
        this.crystal = new BindSetting("Crystal", "Bind", "Keybind to auto buy end crystals.", new CategorySetting.Visibility(this.crystalCategory), 0).disableHoldModes();
        this.crystalStacks = new NumberSetting("CrystalStacks", "Stacks", "How many stacks (64) of end crystals to buy.", new CategorySetting.Visibility(this.crystalCategory), 1, 1, 10);
        this.crystalThreshold = new NumberSetting("CrystalThreshold", "Threshold", "Crystal stack threshold.", new CategorySetting.Visibility(this.crystalCategory), 1, 0, 20);
        this.expCategory = new CategorySetting("Exp", "Automatic Exp restock and manual purchase settings.");
        this.expBelowThreshold = new BooleanSetting("ExpBelowThreshold", "Enabled", "Buy one Exp order when stock reaches the threshold.", new CategorySetting.Visibility(this.expCategory), false);
        this.exp = new BindSetting("Exp", "Bind", "Keybind to auto buy exp bottles.", new CategorySetting.Visibility(this.expCategory), 0).disableHoldModes();
        this.expStacks = new NumberSetting("ExpStacks", "Stacks", "How many stacks (64) of exp bottles to buy.", new CategorySetting.Visibility(this.expCategory), 1, 1, 10);
        this.expThreshold = new NumberSetting("ExpThreshold", "Threshold", "Exp stack threshold.", new CategorySetting.Visibility(this.expCategory), 1, 0, 20);
        this.obsidianCategory = new CategorySetting("Obsidian", "Automatic Obsidian restock and manual purchase settings.");
        this.obsidianBelowThreshold = new BooleanSetting("ObsidianBelowThreshold", "Enabled", "Buy one Obsidian order when stock reaches the threshold.", new CategorySetting.Visibility(this.obsidianCategory), false);
        this.obsidian = new BindSetting("Obsidian", "Bind", "Keybind to auto buy obsidian.", new CategorySetting.Visibility(this.obsidianCategory), 0).disableHoldModes();
        this.obsidianStacks = new NumberSetting("ObsidianStacks", "Stacks", "How many stacks (64) of obsidian to buy.", new CategorySetting.Visibility(this.obsidianCategory), 1, 1, 10);
        this.obsidianThreshold = new NumberSetting("ObsidianThreshold", "Threshold", "Obsidian stack threshold.", new CategorySetting.Visibility(this.obsidianCategory), 1, 0, 20);
        this.enderPearlCategory = new CategorySetting("EnderPearl", "Automatic EnderPearl restock and manual purchase settings.");
        this.enderPearlBelowThreshold = new BooleanSetting("EnderPearlBelowThreshold", "Enabled", "Buy one EnderPearl order when stock reaches the threshold.", new CategorySetting.Visibility(this.enderPearlCategory), false);
        this.enderPearl = new BindSetting("EnderPearl", "Bind", "Keybind to auto buy ender pearls.", new CategorySetting.Visibility(this.enderPearlCategory), 0).disableHoldModes();
        this.enderPearlStacks = new NumberSetting("EnderPearlStacks", "Stacks", "How many stacks (64) of ender pearls to buy.", new CategorySetting.Visibility(this.enderPearlCategory), 1, 1, 10);
        this.enderPearlThreshold = new NumberSetting("EnderPearlThreshold", "Threshold", "EnderPearl stack threshold.", new CategorySetting.Visibility(this.enderPearlCategory), 1, 0, 20);
        this.silent = new BooleanSetting("Silent", "Suppress expected real shop screens before presentation.", false);
        this.antiGhostTotem = new BooleanSetting("AntiGhostTotem", "Stops AutoTotem from swapping while AutoShop is buying (keybind or auto), preventing ghost totems.", true);
        this.actionDelay = new NumberSetting("ActionDelay", "Ticks between each shop GUI click.", 1, 0, 20);
        this.state = AutoShopModule.State.IDLE;
        this.orderQueue = new ConcurrentLinkedQueue();
        this.currentOrder = null;
        this.ticks = 0;
        this.timeoutTicks = 0;
        this.quantitySet = false;
        var tmp0 = new AutoShopModule.AutoControl[5];
        tmp0[0] = new AutoShopModule.AutoControl();
        tmp0[1] = new AutoShopModule.AutoControl();
        tmp0[2] = new AutoShopModule.AutoControl();
        tmp0[3] = new AutoShopModule.AutoControl();
        tmp0[4] = new AutoShopModule.AutoControl();
        this.autoControls = tmp0;
        this.totemEpisodeTriggerReason = "LOSS";
        this.expectedReason = AutoShopModule.ResponseReason.NONE;
        this.bind.disableHoldModes();
    }

    public void handleKey(int arg0) {
        totemToggleKey(arg0);
        if (arg0 == 0) return;
        if (this.totem.getValue() && arg0 == this.totem.getValue()) {
            enqueueOrder(AutoShopModule.ShopMode.Totem, this.totemAmount.getValue().intValue());
            break label_5;
        }
        if (this.crystal.getValue() && arg0 == this.crystal.getValue()) {
            enqueueOrder(AutoShopModule.ShopMode.Crystal, this.crystalStacks.getValue().intValue());
            break label_5;
        }
        if (this.exp.getValue() && arg0 == this.exp.getValue()) {
            enqueueOrder(AutoShopModule.ShopMode.Exp, this.expStacks.getValue().intValue());
            break label_5;
        }
        if (this.obsidian.getValue() && arg0 == this.obsidian.getValue()) {
            enqueueOrder(AutoShopModule.ShopMode.Obsidian, this.obsidianStacks.getValue().intValue());
            break label_5;
        }
        if ((!(this.enderPearl.getValue())) || (arg0 != this.enderPearl.getValue())) break label_5;
        enqueueOrder(AutoShopModule.ShopMode.EnderPearl, this.enderPearlStacks.getValue().intValue());
        label_5:
    }

    public synchronized void enqueueOrder(AutoShopModule.ShopMode arg0, int arg1) {
        if (!(canAccept(itemFor(arg0)))) return;
        LightningClient.LOGGER.info("[AutoShop] MANUAL_REQUEST mode={} amount={}", arg0, arg1);
        enqueuePurchaseOrder(new AutoShopModule.PurchaseOrder(arg0, arg1));
    }

    private synchronized void enqueuePurchaseOrder(AutoShopModule.PurchaseOrder arg0) {
        if ((mc.player == null) || (mc.getConnection() == null)) {
            return;
        }
        if (arg0.remaining <= 0) return;
        this.orderQueue.add(arg0);
        if (this.state == AutoShopModule.State.IDLE) {
            this.currentOrder = ((AutoShopModule.PurchaseOrder) this.orderQueue.poll());
            beginOrder();
        }
    }

    private void beginOrder() {
        this.generation = (this.generation + 1L);
        clearSilentState();
        this.lastWorld = mc.level;
        this.lastPlayer = mc.player;
        this.ticks = 0;
        this.timeoutTicks = 0;
        this.quantitySet = false;
        sendShopCommand();
        this.state = AutoShopModule.State.WAIT_SHOP;
    }

    private void sendShopCommand() {
        if (mc.getConnection() != null) {
            armExpectedResponse(AutoShopModule.ResponseReason.SHOP_COMMAND);
            if (this.currentOrder == null) {
            } else {
            }
            if (this.currentOrder == null) {
            } else {
            }
            "unknown".info(this.currentOrder.origin, "unknown", this.currentOrder.mode);
            mc.getConnection().sendCommand("shop open gear");
        }
    }

    public synchronized AutoShopModule.ScreenDecision classifyScreen(Screen arg0) {
        if (this.state != AutoShopModule.State.IDLE && this.silent.getValue() && this.currentOrder != null && (arg0 instanceof AbstractContainerScreen)) {
            Object var2 = ((AbstractContainerScreen) arg0);
            if ((arg0 instanceof InventoryScreen)) {
            }
            return AutoShopModule.ScreenDecision.PASS;
        }
        if (mc.player != null && mc.player.containerMenu != null && mc.player.containerMenu != mc.player.inventoryMenu && var2.getMenu() == mc.player.containerMenu) {
            if (mc.gui.screen() != null) {
            }
            resetTransactionWithoutClosingContainer("Unexpected container structure");
            return AutoShopModule.ScreenDecision.FAIL_OPEN_UNEXPECTED;
        }
        expireExpectedResponse();
        if (this.expectedReason != AutoShopModule.ResponseReason.NONE && this.expectedGeneration != this.generation) {
            resetTransactionWithoutClosingContainer("No valid expected shop response");
            return AutoShopModule.ScreenDecision.FAIL_OPEN_UNEXPECTED;
        }
        Object var3 = this.expectedReason;
        var tmp0 = new Object[5];
        tmp0[0] = this.state;
        tmp0[1] = var3;
        tmp0[2] = this.generation;
        tmp0[3] = mc.player.containerMenu.containerId;
        tmp0[4] = arg0.getClass().getSimpleName();
        LightningClient.LOGGER.info("[AutoShop] SHOP_SCREEN_INTERCEPT state={} reason={} generation={} containerId={} screenClass={}", tmp0);
        if (var3 == AutoShopModule.ResponseReason.SHOP_COMMAND) {
            this.realShopScreenIntercepted = true;
        }
        this.silentSession = true;
        this.expectedReason = AutoShopModule.ResponseReason.NONE;
        LightningClient.LOGGER.info("[AutoShop] SILENT_SUPPRESS generation={} containerId={}", this.generation, mc.player.containerMenu.containerId);
        return AutoShopModule.ScreenDecision.SUPPRESS_EXPECTED;
    }

    private void armExpectedResponse(AutoShopModule.ResponseReason arg0) {
        if ((!(this.silent.getValue())) || (this.currentOrder == null)) {
            return;
        }
        this.expectedReason = arg0;
        this.expectedGeneration = this.generation;
        this.expectationExpiresAt = (System.nanoTime() + 2000000000L);
        var tmp0 = new Object[3];
        tmp0[0] = arg0;
        tmp0[1] = this.generation;
        tmp0[2] = this.currentOrder.mode;
        LightningClient.LOGGER.info("[AutoShop] SILENT_EXPECT reason={} generation={} mode={}", tmp0);
    }

    private void expireExpectedResponse() {
        if (this.expectedReason != AutoShopModule.ResponseReason.NONE && (System.nanoTime() - this.expectationExpiresAt) >= 0L) {
            LightningClient.LOGGER.info("[AutoShop] SILENT_EXPECT_EXPIRE reason={} generation={}", this.expectedReason, this.expectedGeneration);
            this.expectedReason = AutoShopModule.ResponseReason.NONE;
        }
    }

    private void clearSilentState() {
        this.expectedReason = AutoShopModule.ResponseReason.NONE;
        this.expectedGeneration = 0L;
        this.realShopScreenIntercepted = false;
        this.silentSession = false;
    }

    private void resetTransactionWithoutClosingContainer(String arg0) {
        var tmp0 = new Object[3];
        tmp0[0] = arg0;
        tmp0[1] = this.state;
        tmp0[2] = this.generation;
        LightningClient.LOGGER.warn("[AutoShop] UNEXPECTED_SCREEN FAIL_OPEN_RESET reason={} state={} generation={}", tmp0);
        markAutoFailure();
        this.totemOrderRecovered = false;
        this.state = AutoShopModule.State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.quantitySet = false;
        this.ticks = 0;
        this.timeoutTicks = 0;
        this.generation = (this.generation + 1L);
        clearSilentState();
    }

    public boolean isRunning() {
        return this.state != AutoShopModule.State.IDLE;
    }

    private void resetAutoControls() {
        Object var1 = this.autoControls;
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Object var4 = var1[var3];
            var4.reset();
            var3++;
        }
    }

    private AutoShopModule.AutoControl totemAutoControl() {
        return this.autoControls[AutoShopModule.ShopMode.Totem.ordinal()];
    }

    private void clearTotemTracking() {
        this.totemTrackingInitialized = false;
        this.totemContainerResyncPending = false;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = false;
        this.totemOrderRecovered = false;
        this.totemEpisodeTriggerReason = "LOSS";
        this.baselineTotems = 0;
        this.previousObservedTotems = 0;
        totemAutoControl().reset();
    }

    private void resetTotemBaseline(int arg0, String arg1) {
        int var3 = this.baselineTotems;
        this.baselineTotems = arg0;
        this.previousObservedTotems = arg0;
        this.totemTrackingInitialized = true;
        this.totemContainerResyncPending = false;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = false;
        this.totemEpisodeTriggerReason = "LOSS";
        Object var4 = totemAutoControl();
        var4.reset();
        var4.wasEnabled = true;
        var tmp0 = new Object[3];
        tmp0[0] = arg1;
        tmp0[1] = var3;
        tmp0[2] = arg0;
        LightningClient.LOGGER.info("[AutoShop] TOTEM_BASELINE_RESET reason={} old={} new={}", tmp0);
    }

    private boolean isTotemAutoOrder() {
        if ((this.currentOrder == null) || (this.currentOrder.mode != AutoShopModule.ShopMode.Totem)) break label_0;
        return this.currentOrder.origin != AutoShopModule.OrderOrigin.MANUAL;
    }

    private boolean isUnrelatedInventorySession() {
        Object var1 = mc.gui.screen();
        if (this.state == AutoShopModule.State.IDLE) break label_0;
        int var2 = (this.currentOrder != null ? 1 : 0);
        if ((var1 instanceof InventoryScreen)) return true;
        if ((var1 instanceof AbstractContainerScreen)) return !var2;
        if (mc.player.hasContainerOpen()) {
            if (!(!var2) && this.silent.getValue()) {
                if (!this.silentSession && this.expectedReason == AutoShopModule.ResponseReason.NONE) {
                }
            } else {
            }
            return false;
        }
        return false;
    }

    private void observeTotemStock() {
        Object var1 = totemAutoControl();
        if (!(this.totemBelowThreshold.getValue())) {
            if (!this.totemTrackingInitialized) {
                if (!var1.wasEnabled) break label_1;
            }
            LightningClient.LOGGER.info("[AutoShop] TOTEM_AUTO_DISABLE");
            clearTotemTracking();
            label_1:
            return;
        }
        if (mc.player != null && mc.level != null && mc.getConnection() != null && mc.player.isAlive() && !(mc.player.isDeadOrDying())) {
            if (mc.player.getHealth() <= 0.0f) {
            }
            if (!(!this.totemTrackingInitialized)) {
                clearTotemTracking();
            }
            return;
        }
        int var2 = stockCount(Items.TOTEM_OF_UNDYING);
        if (isUnrelatedInventorySession()) {
            this.totemContainerResyncPending = true;
            return;
        }
        if (!(!this.totemContainerResyncPending)) {
            resetTotemBaseline(var2, "CONTAINER_RESYNC");
            return;
        }
        if (!this.totemTrackingInitialized) {
            resetTotemBaseline(var2, "ENABLE/SESSION");
            LightningClient.LOGGER.info("[AutoShop] TOTEM_AUTO_ENABLE baseline={}", var2);
            return;
        }
        if (var2 > this.previousObservedTotems) {
            if (isTotemAutoOrder()) {
                this.totemOrderRecovered = true;
            }
            resetTotemBaseline(var2, "STOCK_INCREASE");
            LightningClient.LOGGER.info("[AutoShop] TOTEM_AUTO_RECOVER stock={}", var2);
            return;
        }
        if (var2 < this.previousObservedTotems) {
            if (this.previousObservedTotems > 0 && !var2 && this.baselineTotems > 0 && !this.totemZeroFailsafeUsed) {
                this.totemZeroFailsafePending = true;
                LightningClient.LOGGER.info("[AutoShop] TOTEM_ZERO_FAILSAFE baseline={} current=0 configured={}", this.baselineTotems, this.totemLostCount.getValue().intValue());
            }
            int var3 = Math.max(0, (this.baselineTotems - var2));
            var tmp0 = new Object[4];
            tmp0[0] = this.baselineTotems;
            tmp0[1] = var2;
            tmp0[2] = var3;
            tmp0[3] = this.totemLostCount.getValue().intValue();
            LightningClient.LOGGER.info("[AutoShop] TOTEM_LOSS baseline={} current={} lost={} configured={}", tmp0);
        }
        this.previousObservedTotems = var2;
    }

    public void onEnable() {
        resetAutoControls();
        clearTotemTracking();
    }

    public void onDisable() {
        resetAutoControls();
        clearTotemTracking();
    }

    private void markAutoFailure() {
        if ((this.currentOrder == null) || (this.currentOrder.origin == AutoShopModule.OrderOrigin.MANUAL)) {
            return;
        }
        if (!(isTotemAutoOrder())) break label_5;
        if ((!(this.totemBelowThreshold.getValue())) || (!(!this.totemOrderRecovered))) {
            return;
        }
        if (this.currentOrder.origin == AutoShopModule.OrderOrigin.AUTO_PRIMARY) {
            LightningClient.LOGGER.warn("[AutoShop] TOTEM_AUTO_RETRY cooldownSeconds=5");
            break label_5;
        }
        LightningClient.LOGGER.warn("[AutoShop] TOTEM_AUTO_RETRY_EXHAUSTED");
        label_5:
        Object var1 = this.autoControls[this.currentOrder.mode.ordinal()];
        if (this.currentOrder.origin == AutoShopModule.OrderOrigin.AUTO_PRIMARY) {
            var1.phase = AutoShopModule.AutoPhase.RETRY_COOLDOWN;
            var1.retryAtNanos = (System.nanoTime() + 5000000000L);
            LightningClient.LOGGER.warn("[AutoShop] AUTO_RETRY_SCHEDULED mode={} cooldownSeconds=5", this.currentOrder.mode);
        } else {
            var1.phase = AutoShopModule.AutoPhase.EXHAUSTED;
            LightningClient.LOGGER.warn("[AutoShop] AUTO_RETRY_EXHAUSTED mode={}", this.currentOrder.mode);
        }
    }

    private void markAutoServiced() {
        if ((this.currentOrder == null) || (this.currentOrder.origin == AutoShopModule.OrderOrigin.MANUAL)) {
            return;
        }
        if (isTotemAutoOrder()) {
            if ((!(this.totemBelowThreshold.getValue())) || (!(!this.totemOrderRecovered))) {
                return;
            }
        }
        Object var1 = this.autoControls[this.currentOrder.mode.ordinal()];
        var1.phase = AutoShopModule.AutoPhase.SERVICED;
        int var2 = stockCount(itemFor(this.currentOrder.mode));
        var1.peakStock = Math.max(var1.startStock, var2);
        var tmp0 = new Object[3];
        tmp0[0] = this.currentOrder.mode;
        tmp0[1] = var2;
        tmp0[2] = var1.startStock;
        LightningClient.LOGGER.info("[AutoShop] AUTO_SERVICED mode={} observedStock={} startStock={}", tmp0);
    }

    private static Item itemFor(AutoShopModule.ShopMode arg0) {
        switch (arg0.ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_0; }
        }
        label_0:
        throw new MatchException(null, null);
        label_1:
        break label_6;
        label_2:
        break label_6;
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        return Items.ENDER_PEARL;
    }

    private int stockCount(Item arg0) {
        if (mc.player == null) return 0;
        int var2 = 0;
        int var3 = 0;
        while (var3 < 36) {
            Object var4 = mc.player.getInventory().getItem(var3);
            if (var4.getItem() == arg0) {
                var2 = (var2 + var4.getCount());
            }
            var3++;
        }
        return var2;
    }

    private boolean tryStartAutomaticTotemOrder() {
        if (((((((((!(this.totemBelowThreshold.getValue())) || (!this.totemTrackingInitialized)) || (this.totemContainerResyncPending)) || (mc.player == null)) || (mc.level == null)) || (mc.getConnection() == null)) || (!(mc.player.isAlive()))) || (mc.player.isDeadOrDying())) || (mc.player.getHealth() <= 0.0f)) {
            return false;
        }
        Object var1 = totemAutoControl();
        if (this.state != AutoShopModule.State.IDLE) break label_2;
        if (!(this.orderQueue.isEmpty())) {
            label_2:
            return false;
        }
        if (var1.phase != AutoShopModule.AutoPhase.READY && var1.phase != AutoShopModule.AutoPhase.RETRY_COOLDOWN) return false;
        int var2 = this.previousObservedTotems;
        int var3 = var2;
        int var4 = this.totemLostCount.getValue().intValue();
        int var5 = (var3 <= var4 ? 1 : 0);
        if (((this.baselineTotems <= 0) || (var2)) || (!this.totemZeroFailsafePending)) break label_7;
        int var6 = (!this.totemZeroFailsafeUsed ? 1 : 0);
        if (var1.phase != AutoShopModule.AutoPhase.RETRY_COOLDOWN && !var5) {
            if (!(!var6)) {
            }
            if (!(autoStartSafe(Items.TOTEM_OF_UNDYING))) {
            }
            return false;
        }
        if (var1.phase == AutoShopModule.AutoPhase.RETRY_COOLDOWN && (System.nanoTime() - var1.retryAtNanos) < 0L) return false;
        Object var7 = (var1.phase == AutoShopModule.AutoPhase.READY ? AutoShopModule.OrderOrigin.AUTO_PRIMARY : AutoShopModule.OrderOrigin.AUTO_RETRY);
        if (var7 == AutoShopModule.OrderOrigin.AUTO_PRIMARY) {
            var1.startStock = var2;
        }
        var1.phase = (var7 == AutoShopModule.OrderOrigin.AUTO_PRIMARY ? AutoShopModule.AutoPhase.RUNNING_PRIMARY : AutoShopModule.AutoPhase.RUNNING_RETRY);
        var1.peakStock = Math.max(var1.peakStock, var2);
        this.totemOrderRecovered = false;
        if (var7 != AutoShopModule.OrderOrigin.AUTO_PRIMARY) break label_20;
        this.totemEpisodeTriggerReason = (!(!var5) ? "LOSS" : "ZERO_FAILSAFE");
        if ((!var6) || (var5)) break label_20;
        this.totemZeroFailsafePending = false;
        this.totemZeroFailsafeUsed = true;
        label_20:
        int var8 = this.totemAmount.getValue().intValue();
        var tmp0 = new Object[5];
        tmp0[0] = this.totemEpisodeTriggerReason;
        tmp0[1] = var3;
        tmp0[2] = var4;
        tmp0[3] = var8;
        tmp0[4] = var7;
        LightningClient.LOGGER.info("[AutoShop] TOTEM_AUTO_TRIGGER reason={} lost={} configured={} amount={} attempt={}", tmp0);
        enqueuePurchaseOrder(new AutoShopModule.PurchaseOrder(AutoShopModule.ShopMode.Totem, var8, var7));
        return true;
    }

    private void tryStartAutomaticOrder() {
        if (((mc.player == null) || (mc.level == null)) || (mc.getConnection() == null)) {
            return;
        }
        Object var1 = AutoShopModule.ShopMode.values();
        Object var2 = var1;
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (var5 == AutoShopModule.ShopMode.Totem) {
                if (!(tryStartAutomaticTotemOrder())) break label_26;
                return;
            }
            int var6 = var5.ordinal();
            Object var7 = this.autoControls[var6];
            int var8 = autoEnabled(var5);
            if (!var8) {
                var7.wasEnabled = false;
                break label_26;
            }
            if (!var7.wasEnabled) {
                var7.reset();
                var7.wasEnabled = true;
                LightningClient.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason=TOGGLE_ON", var5);
            }
            int var9 = stockCount(itemFor(var5));
            int var10 = thresholdCount(var5, itemFor(var5));
            if (var7.phase != AutoShopModule.AutoPhase.SERVICED) {
                if (var7.phase == AutoShopModule.AutoPhase.EXHAUSTED) {
                }
                var7.peakStock = Math.max(var7.peakStock, var9);
                if (var9 <= var10) {
                    if (((var7.peakStock <= var7.startStock) || (var9 >= var7.peakStock)) || (var9 > var10)) break label_12;
                }
                var7.phase = AutoShopModule.AutoPhase.READY;
                LightningClient.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason={}", var5, (var9 > var10 ? "ABOVE_THRESHOLD" : "RECOVERY_THEN_CONSUMPTION"));
                break label_12;
            }
            if (var7.phase != AutoShopModule.AutoPhase.RETRY_COOLDOWN) break label_12;
            if (var9 > var7.startStock) {
                var7.phase = AutoShopModule.AutoPhase.SERVICED;
                var7.peakStock = var9;
                LightningClient.LOGGER.info("[AutoShop] AUTO_SERVICED mode={} reason=STOCK_INCREASE_DURING_COOLDOWN", var5);
                break label_12;
            }
            if (var9 <= var10) break label_12;
            var7.phase = AutoShopModule.AutoPhase.READY;
            LightningClient.LOGGER.info("[AutoShop] AUTO_REARM mode={} reason=ABOVE_THRESHOLD", var5);
            label_12:
            if (this.state != AutoShopModule.State.IDLE) break label_26;
            if (!(this.orderQueue.isEmpty())) {
                break label_26;
            }
            if (var7.phase != AutoShopModule.AutoPhase.READY && var7.phase != AutoShopModule.AutoPhase.RETRY_COOLDOWN) {
                break label_26;
            }
            if (var9 > var10) break label_26;
            if (!(autoStartSafe(itemFor(var5)))) {
                break label_26;
            }
            if (var7.phase == AutoShopModule.AutoPhase.RETRY_COOLDOWN && (System.nanoTime() - var7.retryAtNanos) < 0L) {
                break label_26;
            }
            Object var11 = (var7.phase == AutoShopModule.AutoPhase.READY ? AutoShopModule.OrderOrigin.AUTO_PRIMARY : AutoShopModule.OrderOrigin.AUTO_RETRY);
            if (var11 == AutoShopModule.OrderOrigin.AUTO_PRIMARY) {
                var7.startStock = var9;
            }
            var7.phase = (var11 == AutoShopModule.OrderOrigin.AUTO_PRIMARY ? AutoShopModule.AutoPhase.RUNNING_PRIMARY : AutoShopModule.AutoPhase.RUNNING_RETRY);
            var7.peakStock = Math.max(var7.peakStock, var9);
            int var12 = manualAmount(var5);
            var tmp0 = new Object[6];
            tmp0[0] = (var11 == AutoShopModule.OrderOrigin.AUTO_PRIMARY ? "AUTO_TRIGGER" : "AUTO_RETRY");
            tmp0[1] = var5;
            tmp0[2] = (var11 == AutoShopModule.OrderOrigin.AUTO_PRIMARY ? "PRIMARY" : "RETRY");
            tmp0[3] = var9;
            tmp0[4] = var10;
            tmp0[5] = var12;
            LightningClient.LOGGER.info("[AutoShop] {} mode={} attempt={} stock={} threshold={} amount={}", tmp0);
            enqueuePurchaseOrder(new AutoShopModule.PurchaseOrder(var5, var12, var11));
            return;
            label_26:
            var4++;
        }
    }

    private boolean autoStartSafe(Item arg0) {
        if ((mc.gui.screen() != null) || (mc.player.hasContainerOpen())) {
            return false;
        }
        int var2 = 0;
        while (var2 < 36) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if ((var3.isEmpty()) || (var3.getItem() == arg0 && var3.getCount() < var3.getMaxStackSize())) {
                return true;
            }
            var2++;
        }
        return false;
    }

    private boolean autoEnabled(AutoShopModule.ShopMode arg0) {
        switch (arg0.ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_0; }
        }
        label_0:
        throw new MatchException(null, null);
        label_1:
        break label_6;
        label_2:
        break label_6;
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        return this.enderPearlBelowThreshold.getValue();
    }

    private int thresholdCount(AutoShopModule.ShopMode arg0, Item arg1) {
        switch (arg0.ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_0; }
        }
        label_0:
        throw new MatchException(null, null);
        label_1:
        throw new IllegalArgumentException("Totem automatic buying uses loss tracking");
        label_2:
        break label_6;
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        int var3 = this.enderPearlThreshold.getValue().intValue();
        if (arg0 == AutoShopModule.ShopMode.Totem) {
        } else {
        }
        return (var3 * arg1.getDefaultInstance().getMaxStackSize());
    }

    private int manualAmount(AutoShopModule.ShopMode arg0) {
        switch (arg0.ordinal()) {
            case 0 -> { break label_1; }
            case 1 -> { break label_2; }
            case 2 -> { break label_3; }
            case 3 -> { break label_4; }
            case 4 -> { break label_5; }
            default -> { break label_0; }
        }
        label_0:
        throw new MatchException(null, null);
        label_1:
        break label_6;
        label_2:
        break label_6;
        label_3:
        break label_6;
        label_4:
        break label_6;
        label_5:
        label_6:
        return this.enderPearlStacks.getValue().intValue();
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        this.state = AutoShopModule.State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.lastWorld = null;
        this.lastPlayer = null;
        this.timeoutTicks = 0;
        this.ticks = 0;
        this.quantitySet = false;
        this.generation = (this.generation + 1L);
        clearSilentState();
        resetAutoControls();
        clearTotemTracking();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (this.lastWorld == mc.level) {
            if (this.lastPlayer == mc.player) break label_2;
        }
        if ((this.lastWorld == null) || (this.state == AutoShopModule.State.IDLE)) break label_1;
        abort("Player changed world/session");
        label_1:
        this.lastWorld = mc.level;
        this.lastPlayer = mc.player;
        this.generation = (this.generation + 1L);
        clearSilentState();
        resetAutoControls();
        clearTotemTracking();
        label_2:
        if (mc.player == null) break label_4;
        if ((mc.player.isDeadOrDying()) || (!(mc.player.isAlive()))) break label_3;
        if (mc.player.getHealth() > 0.0f) break label_4;
        label_3:
        if (!(isTotemAutoOrder())) break label_4;
        abort("Player dead during automatic Totem order");
        clearTotemTracking();
        label_4:
        observeTotemStock();
        tryStartAutomaticOrder();
        if (this.state == AutoShopModule.State.IDLE) return;
        if (mc.player != null) {
            if (mc.level == null) {
            }
            abort("Player left world");
            return;
        }
        expireExpectedResponse();
        if (this.ticks < AutoShopSync.delay(this.actionDelay.getValue())) {
            this.ticks = (this.ticks + 1);
            return;
        }
        this.ticks = 0;
        Object var3 = mc.gui.screen();
        if (!((var3 instanceof AbstractContainerScreen))) break label_9;
        var tmp0 = (!((var3 instanceof InventoryScreen)) ? 1 : 0);
        int var2 = tmp0;
        int var4 = tmp0;
        if (this.silent.getValue() && !(!this.silentSession)) {
            if (!var2) {
                if (mc.player.hasContainerOpen()) {
                }
            } else {
            }
            var2 = 0;
        }
        if ((this.state == AutoShopModule.State.WAIT_SHOP) || (var2)) break label_15;
        abort("Shop GUI closed unexpectedly.");
        return;
        label_15:
        switch (this.state.ordinal()) {
            case 1 -> { break label_16; }
            case 2 -> { break label_23; }
            case 3 -> { break label_33; }
            case 4 -> { break label_41; }
            case 5 -> { break label_47; }
            default -> { break label_53; }
        }
        label_16:
        if (!var2) {
            if (this.silent.getValue() && !(!this.realShopScreenIntercepted)) {
            }
            this.realShopScreenIntercepted = false;
            if (this.currentOrder == null) {
            } else {
            }
            "[AutoShop] SHOP_ACCEPTED mode={} generation={}".info("unknown", this.currentOrder.mode, this.generation);
            this.state = AutoShopModule.State.CLICK_ITEM;
            this.timeoutTicks = 0;
            break label_53;
        }
        this.timeoutTicks = (this.timeoutTicks + 1);
        if ((this.timeoutTicks % 20) == 0) {
            sendShopCommand();
        }
        if (this.timeoutTicks <= 100) {
            break label_53;
        }
        abort("Shop GUI never opened.");
        break label_53;
        label_23:
        if (this.currentOrder == null) {
            this.state = AutoShopModule.State.DECIDE;
            return;
        }
        switch (this.currentOrder.mode.ordinal()) {
            case 0 -> { break label_26; }
            case 1 -> { break label_27; }
            case 2 -> { break label_28; }
            case 3 -> { break label_29; }
            case 4 -> { break label_30; }
            default -> { break label_25; }
        }
        label_25:
        throw new MatchException(null, null);
        label_26:
        break label_31;
        label_27:
        break label_31;
        label_28:
        break label_31;
        label_29:
        break label_31;
        label_30:
        label_31:
        int var5 = 14;
        if (this.silent.getValue() && !(!this.silentSession)) {
            armExpectedResponse(AutoShopModule.ResponseReason.ITEM_SELECTION);
        }
        LightningClient.LOGGER.info("[AutoShop] ITEM_CLICK mode={} slot={}", this.currentOrder.mode, var5);
        click(var5);
        this.state = AutoShopModule.State.DECIDE;
        break label_53;
        label_33:
        if (fullGate()) return;
        if (this.currentOrder != null) {
            if (this.currentOrder.remaining <= 0) {
            }
            if (!(this.orderQueue.isEmpty())) {
                if (isTotemAutoOrder()) {
                    markAutoServiced();
                    this.totemOrderRecovered = false;
                }
                this.currentOrder = ((AutoShopModule.PurchaseOrder) this.orderQueue.poll());
                beginOrder();
                return;
            }
            finish();
            return;
        }
        if (this.currentOrder.mode == AutoShopModule.ShopMode.Totem) {
            this.state = AutoShopModule.State.CONFIRM;
            break label_53;
        }
        if (!this.quantitySet) {
            this.state = AutoShopModule.State.SET_11;
            break label_53;
        }
        this.state = AutoShopModule.State.CONFIRM;
        break label_53;
        label_41:
        if (this.currentOrder == null) break label_42;
        var5 = (this.currentOrder.mode == AutoShopModule.ShopMode.EnderPearl ? 15 : 16);
        if (this.silent.getValue() && !(!this.silentSession)) {
            armExpectedResponse(AutoShopModule.ResponseReason.QUANTITY_SELECTION);
        }
        if (this.currentOrder == null) {
        } else {
        }
        "[AutoShop] QUANTITY_CLICK mode={} slot={}".info("unknown", this.currentOrder.mode, var5);
        click(var5);
        this.quantitySet = true;
        this.state = AutoShopModule.State.CONFIRM;
        break label_53;
        label_47:
        if (this.currentOrder == null) {
        } else {
        }
        if (this.currentOrder == null) {
        } else {
        }
        "unknown".info(this.currentOrder.mode, 0, this.currentOrder.remaining);
        click(23);
        if (this.currentOrder != null) {
            this.currentOrder.remaining = (this.currentOrder.remaining - 1);
        }
        this.state = AutoShopModule.State.DECIDE;
        label_53:
        if (this.state == AutoShopModule.State.DECIDE) {
            break label_15;
        }
    }

    private void finish() {
        this.lastBuyMsg = buildBuyMessage(this);
        if (this.currentOrder == null) {
        } else {
        }
        "[AutoShop] FINISH mode={}".info("unknown", this.currentOrder.mode);
        markAutoServiced();
        this.totemOrderRecovered = false;
        this.state = AutoShopModule.State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.generation = (this.generation + 1L);
        clearSilentState();
        if (mc.player != null) {
            mc.player.closeContainer();
        }
        LightningClient.CHAT_MANAGER.tagged(this.lastBuyMsg, getName());
    }

    public void abort(String arg0) {
        LightningClient.LOGGER.warn("[AutoShop] ABORT reason={} state={}", arg0, this.state);
        markAutoFailure();
        this.totemOrderRecovered = false;
        this.state = AutoShopModule.State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.generation = (this.generation + 1L);
        clearSilentState();
        if (mc.player != null) {
            mc.player.closeContainer();
        }
        LightningClient.CHAT_MANAGER.tagged(("Aborted: " + arg0), getName());
    }

    private void click(int arg0) {
        AutoShopSync.click(arg0, 0, ContainerInput.PICKUP);
    }

    public String getMetaData() {
        if (this.currentOrder != null) {
            if (this.orderQueue.isEmpty()) {
            } else {
            }
            return (this.currentOrder.remaining + " " + "" + (" (+" + this.orderQueue.size() + ")"));
        }
        return "";
    }

    private void totemToggleKey(int arg0) {
        if ((!(this.totemToggle.getValue())) || (arg0 != this.totemToggle.getValue())) break label_1;
        this.totemBelowThreshold.setValue((this.totemBelowThreshold.getValue() ^ 1));
        if (!(this.chatNotify.getValue())) break label_1;
        if (this.totemBelowThreshold.getValue()) {
            LightningClient.CHAT_MANAGER.message("\u00a7bAutoShop \u00a77Auto buy totem: \u00a7aON", "autoshop_totem_toggle");
            break label_1;
        }
        LightningClient.CHAT_MANAGER.message("\u00a7bAutoShop \u00a77Auto buy totem: \u00a7cOFF", "autoshop_totem_toggle");
        label_1:
    }

    private boolean canAccept(Item arg0) {
        int var2 = 0;
        while (var2 < 36) {
            Object var3 = mc.player.getInventory().getItem(var2);
            if ((var3.isEmpty()) || (var3.getItem() == arg0 && var3.getCount() < var3.getMaxStackSize())) {
                return true;
            }
            var2++;
        }
        return false;
    }

    private boolean fullGate() {
        Object var1 = this.currentOrder;
        if (((var1 == null) || (var1.remaining <= 0)) || (canAccept(itemFor(var1.mode)))) {
            return false;
        }
        markAutoServiced();
        this.state = AutoShopModule.State.IDLE;
        this.orderQueue.clear();
        this.currentOrder = null;
        this.generation = (this.generation + 1L);
        this.totemOrderRecovered = false;
        clearSilentState();
        if (mc.player != null) {
            mc.player.closeContainer();
        }
        return true;
    }


    enum OrderOrigin {
        MANUAL,
        AUTO_PRIMARY,
        AUTO_RETRY;


        private OrderOrigin() {
        }

    }

    public class PurchaseOrder {
        public final AutoShopModule.ShopMode mode;
        public int remaining;
        private final AutoShopModule.OrderOrigin origin;

        public PurchaseOrder(AutoShopModule.ShopMode arg0, int arg1) {
            this(arg0, arg1, AutoShopModule.OrderOrigin.MANUAL);
        }

        private PurchaseOrder(AutoShopModule.ShopMode arg0, int arg1, AutoShopModule.OrderOrigin arg2) {
            this.mode = arg0;
            this.remaining = arg1;
            this.origin = arg2;
        }

    }

    enum ResponseReason {
        NONE,
        SHOP_COMMAND,
        ITEM_SELECTION,
        QUANTITY_SELECTION;


        private ResponseReason() {
        }

    }

    enum State {
        IDLE,
        WAIT_SHOP,
        CLICK_ITEM,
        DECIDE,
        SET_11,
        CONFIRM;


        private State() {
        }

    }

    enum AutoPhase {
        READY,
        RUNNING_PRIMARY,
        RETRY_COOLDOWN,
        RUNNING_RETRY,
        SERVICED,
        EXHAUSTED;


        private AutoPhase() {
        }

    }

    final class AutoControl {
        private AutoShopModule.AutoPhase phase;
        private boolean wasEnabled;
        private int startStock;
        private int peakStock;
        private long retryAtNanos;

        private AutoControl() {
            this.phase = AutoShopModule.AutoPhase.READY;
        }

        private void reset() {
            this.phase = AutoShopModule.AutoPhase.READY;
            this.wasEnabled = false;
            this.startStock = 0;
            this.peakStock = 0;
            this.retryAtNanos = 0L;
        }

    }

    public enum ScreenDecision {
        PASS,
        SUPPRESS_EXPECTED,
        FAIL_OPEN_UNEXPECTED;


        private ScreenDecision() {
        }

    }

    public enum ShopMode {
        Totem,
        Crystal,
        Exp,
        Obsidian,
        EnderPearl;


        private ShopMode() {
        }

    }
}
