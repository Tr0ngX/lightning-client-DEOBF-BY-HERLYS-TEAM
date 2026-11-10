package lightning.modules.impl.player;

import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name = "ChestStealer", description = "Automatically steals, dumps, or drops items to and from an open container.", category = Module.Category.PLAYER)
public class ChestStealerModule extends Module {
    public BooleanSetting steal;
    public ModeSetting stealMode;
    public WhitelistSetting stealWhitelist;
    public BooleanSetting dump;
    public ModeSetting dumpMode;
    public WhitelistSetting dumpWhitelist;
    public BooleanSetting drop;
    public ModeSetting dropMode;
    public WhitelistSetting dropWhitelist;
    public ModeSetting interact;
    public NumberSetting delay;
    public NumberSetting actionsPerTick;
    public BooleanSetting buttonMode;
    public BooleanSetting ignoreCustomName;
    private int ticks;
    public static volatile long lastContainerActionMs;
    private ChestStealerModule.Task activeTask;
    private int taskStep;
    private int lastContainerId;
    private int autoStealStep;
    private int autoDumpStep;
    private int autoDropStep;

    public ChestStealerModule() {
        this.steal = new BooleanSetting("Steal", "Move matching items FROM the open container INTO your inventory.", true);
        var tmp0 = new String[3];
        tmp0[0] = "WhiteList";
        tmp0[1] = "BlackList";
        tmp0[2] = "All";
        this.stealMode = new ModeSetting("Mode", "WhiteList = steal items IN the list. BlackList = steal items NOT in the list. All = steal everything.", new BooleanSetting.Visibility(this.steal, true), "All", tmp0);
        this.stealWhitelist = new WhitelistSetting("List", "Items to compare against.", new BooleanSetting.Visibility(this.steal, true), WhitelistSetting.Type.ITEMS);
        this.dump = new BooleanSetting("Dump", "Move matching items FROM your inventory INTO the open container.", false);
        var tmp1 = new String[3];
        tmp1[0] = "WhiteList";
        tmp1[1] = "BlackList";
        tmp1[2] = "All";
        this.dumpMode = new ModeSetting("Mode", "WhiteList = dump items IN the list. BlackList = dump items NOT in the list. All = dump everything.", new BooleanSetting.Visibility(this.dump, true), "WhiteList", tmp1);
        this.dumpWhitelist = new WhitelistSetting("List", "Items to compare against.", new BooleanSetting.Visibility(this.dump, true), WhitelistSetting.Type.ITEMS);
        this.drop = new BooleanSetting("Drop", "Throw matching items OUT (Q / THROW).", false);
        var tmp2 = new String[3];
        tmp2[0] = "WhiteList";
        tmp2[1] = "BlackList";
        tmp2[2] = "All";
        this.dropMode = new ModeSetting("DropMode", "Mode", "WhiteList = drop items IN the list. BlackList = drop items NOT in the list. All = drop everything.", new BooleanSetting.Visibility(this.drop, true), "All", tmp2);
        this.dropWhitelist = new WhitelistSetting("DropList", "List", "Items to compare against for drop.", new BooleanSetting.Visibility(this.drop, true), WhitelistSetting.Type.ITEMS);
        var tmp3 = new String[3];
        tmp3[0] = "Hotbar";
        tmp3[1] = "Inventory";
        tmp3[2] = "Both";
        this.interact = new ModeSetting("Interact", "Which of YOUR slots Dump/Drop act on: Hotbar (0-8), Inventory (9-35), or Both.", "Both", tmp3);
        this.delay = new NumberSetting("Delay", "Tick delay between action passes.", 1, 0, 20);
        this.actionsPerTick = new NumberSetting("ActionsPerTick", "Max slot actions per pass.", 5, 1, 36);
        this.buttonMode = new BooleanSetting("ButtonMode", "Adds vanilla Steal/Dump/Drop buttons at the top-right of the container/inventory GUI.", false);
        this.ignoreCustomName = new BooleanSetting("IgnoreCustomName", "Skip any container with a custom (non-vanilla) title such as a shop GUI, except shulker boxes.", false);
        this.ticks = 0;
        this.activeTask = ChestStealerModule.Task.NONE;
        this.taskStep = 0;
        this.lastContainerId = -1;
        this.autoStealStep = 0;
        this.autoDumpStep = 0;
        this.autoDropStep = 0;
    }

    public void onDisable() {
        this.activeTask = ChestStealerModule.Task.NONE;
        this.taskStep = 0;
        this.autoStealStep = 0;
        this.autoDumpStep = 0;
        this.autoDropStep = 0;
        this.lastContainerId = -1;
        this.ticks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gameMode == null)) {
            return;
        }
        if (mc.player.isCreative()) return;
        if (mc.player.containerMenu == null) return;
        if (!(mc.player.containerMenu.getCarried().isEmpty())) return;
        int var6 = mc.player.containerMenu.containerId;
        if (var6 != this.lastContainerId) {
            this.lastContainerId = var6;
            this.taskStep = 0;
            this.autoStealStep = 0;
            this.autoDumpStep = 0;
            this.autoDropStep = 0;
            this.activeTask = ChestStealerModule.Task.NONE;
        }
        long var4 = System.currentTimeMillis();
        if (((System.currentTimeMillis() - RekitModule.lastContainerActionMs) < 200L) || ((var4 - InventorySorterModule.lastContainerActionMs) < 200L)) {
            return;
        }
        if (this.ticks < this.delay.getValue().intValue()) {
            this.ticks = (this.ticks + 1);
            return;
        }
        this.ticks = 0;
        int var7 = this.actionsPerTick.getValue().intValue();
        if (this.activeTask != ChestStealerModule.Task.NONE) {
            if (!((mc.gui.screen() instanceof AbstractContainerScreen))) break label_9;
            int var8 = (!((mc.gui.screen() instanceof InventoryScreen)) ? 1 : 0);
            int var9 = (mc.gui.screen() instanceof InventoryScreen);
            if (!var8 && !var9) {
                this.activeTask = ChestStealerModule.Task.NONE;
                this.taskStep = 0;
                return;
            }
            switch (this.activeTask.ordinal()) {
                case 1 -> { break label_12; }
                case 2 -> { break label_15; }
                case 3 -> { break label_18; }
                default -> { break label_22; }
            }
            label_12:
            if (!(!var8)) {
                int var10 = (mc.player.containerMenu.slots.size() - 36);
                this.taskStep = (this.taskStep + processStealStep(this.stealMode.getValue(), this.stealWhitelist, var7, this.taskStep));
                if (this.taskStep < var10) {
                    break label_22;
                }
                this.activeTask = ChestStealerModule.Task.NONE;
                this.taskStep = 0;
                break label_22;
            }
            this.activeTask = ChestStealerModule.Task.NONE;
            this.taskStep = 0;
            break label_22;
            label_15:
            if (!(!var8)) {
                this.taskStep = (this.taskStep + processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, var7, this.taskStep));
                if (this.taskStep < 36) {
                    break label_22;
                }
                this.activeTask = ChestStealerModule.Task.NONE;
                this.taskStep = 0;
                break label_22;
            }
            this.activeTask = ChestStealerModule.Task.NONE;
            this.taskStep = 0;
            break label_22;
            label_18:
            var10 = var8;
            if (!(!var10)) {
            } else {
            }
            int var11 = 0;
            int var12 = (var11 + 36);
            this.taskStep = (this.taskStep + processDropStep(this.dropMode.getValue(), this.dropWhitelist, var7, this.taskStep));
            if (this.taskStep < var12) {
                break label_22;
            }
            this.activeTask = ChestStealerModule.Task.NONE;
            this.taskStep = 0;
            label_22:
            return;
        }
        if (this.buttonMode.getValue()) return;
        if (!((mc.gui.screen() instanceof AbstractContainerScreen))) break label_25;
        var tmp0 = (!((mc.gui.screen() instanceof InventoryScreen)) ? 1 : 0);
        int var3 = tmp0;
        var8 = tmp0;
        if (!(!var3) && this.ignoreCustomName.getValue() && hasCustomContainerTitle() && !(mc.player.containerMenu instanceof ShulkerBoxMenu)) return;
        if (((!(this.steal.getValue())) || (!var3)) || (var7 <= 0)) break label_28;
        int var2 = (mc.player.containerMenu.slots.size() - 36);
        var9 = processStealStep(this.stealMode.getValue(), this.stealWhitelist, var7, this.autoStealStep);
        this.autoStealStep = (this.autoStealStep + var9);
        if (this.autoStealStep < var2) break label_28;
        this.autoStealStep = 0;
        label_28:
        if (((!(this.dump.getValue())) || (!var3)) || (var7 <= 0)) break label_29;
        var9 = processDumpStep(this.dumpMode.getValue(), this.dumpWhitelist, var7, this.autoDumpStep);
        this.autoDumpStep = (this.autoDumpStep + var9);
        if (this.autoDumpStep < 36) break label_29;
        this.autoDumpStep = 0;
        label_29:
        if ((!(this.drop.getValue())) || (var7 <= 0)) break label_32;
        if (!(!var3)) {
        } else {
        }
        var2 = 0;
        var9 = (var2 + 36);
        var10 = processDropStep(this.dropMode.getValue(), this.dropWhitelist, var7, this.autoDropStep);
        this.autoDropStep = (this.autoDropStep + var10);
        if (this.autoDropStep < var9) break label_32;
        this.autoDropStep = 0;
        label_32:
    }

    public void triggerSteal() {
        if (canAct(true)) {
            this.activeTask = ChestStealerModule.Task.STEAL;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    public void triggerDump() {
        if (canAct(true)) {
            this.activeTask = ChestStealerModule.Task.DUMP;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    public void triggerDrop() {
        if (canAct(false)) {
            this.activeTask = ChestStealerModule.Task.DROP;
            this.taskStep = 0;
            this.ticks = this.delay.getValue().intValue();
        }
    }

    private boolean hasCustomContainerTitle() {
        Object var1 = mc.gui.screen();
        if (!((var1 instanceof AbstractContainerScreen))) return false;
        Object var2 = ((AbstractContainerScreen) var1);
        Object var3 = var2.getTitle();
        if (var3 == null) return false;
        return !((var3.getContents() instanceof TranslatableContents));
    }

    private boolean canAct(boolean arg0) {
        if (((mc.player == null) || (mc.level == null)) || (mc.gameMode == null)) {
            return false;
        }
        if (mc.player.isCreative()) return false;
        if (mc.player.containerMenu == null) return false;
        if (!(mc.player.containerMenu.getCarried().isEmpty())) return false;
        if (!(!arg0)) {
            if (!((mc.gui.screen() instanceof AbstractContainerScreen))) break label_5;
            int var2 = (!((mc.gui.screen() instanceof InventoryScreen)) ? 1 : 0);
            int var3 = (mc.gui.screen() instanceof InventoryScreen);
            if (!var2 && !var3) return false;
        }
        return true;
    }

    private int processStealStep(String arg0, WhitelistSetting arg1, int arg2, int arg3) {
        if ((mc.gui.screen() instanceof InventoryScreen)) return 0;
        int var6 = mc.player.containerMenu.containerId;
        int var7 = (mc.player.containerMenu.slots.size() - 36);
        int var8 = 0;
        int var5 = arg3;
        while (var5 < var7 && var8 < arg2) {
            Object var9 = ((Slot) mc.player.containerMenu.slots.get(var5)).getItem();
            if (var9.isEmpty()) break label_3;
            if (!(matches(var9, arg0, arg1))) {
                break label_3;
            }
            quickMove(var6, var5);
            var8++;
            label_3:
            var5++;
        }
        return Math.max(1, (var5 - arg3));
    }

    private int processDumpStep(String arg0, WhitelistSetting arg1, int arg2, int arg3) {
        if ((mc.gui.screen() instanceof InventoryScreen)) return 0;
        int var6 = mc.player.containerMenu.containerId;
        int var7 = 0;
        int var5 = arg3;
        while (var5 < 36 && var7 < arg2) {
            if (!(inInteractRange(var5))) break label_3;
            var tmp0 = mc.player.getInventory().getItem(var5);
            Object var9 = tmp0;
            if ((tmp0.isEmpty()) || (!(matches(var9, arg0, arg1)))) break label_3;
            var tmp1 = invToHandlerSlot(var5);
            int var8 = tmp1;
            if (tmp1 < 0) {
                break label_3;
            }
            quickMove(var6, var8);
            var7++;
            label_3:
            var5++;
        }
        return Math.max(1, (var5 - arg3));
    }

    private int processDropStep(String arg0, WhitelistSetting arg1, int arg2, int arg3) {
        int var6 = mc.player.containerMenu.containerId;
        if (!((mc.gui.screen() instanceof AbstractContainerScreen))) break label_0;
        int var7 = (!((mc.gui.screen() instanceof InventoryScreen)) ? 1 : 0);
        if (!(!var7)) {
        } else {
        }
        int var8 = 0;
        int var9 = (var8 + 36);
        int var10 = 0;
        int var5 = arg3;
        while (var5 < var9 && var10 < arg2) {
            if (var5 < var8) {
                Object var13 = ((Slot) mc.player.containerMenu.slots.get(var5)).getItem();
                if (var13.isEmpty()) break label_8;
                if (!(matches(var13, arg0, arg1))) {
                    break label_8;
                }
                throwSlot(var6, var5);
                var10++;
                break label_8;
            }
            var13 = (var5 - var8);
            if (!(inInteractRange(var13))) break label_8;
            var tmp0 = mc.player.getInventory().getItem(var13);
            Object var12 = tmp0;
            if ((tmp0.isEmpty()) || (!(matches(var12, arg0, arg1)))) break label_8;
            var tmp1 = invToHandlerSlot(var13);
            int var11 = tmp1;
            if (tmp1 < 0) {
                break label_8;
            }
            throwSlot(var6, var11);
            var10++;
            label_8:
            var5++;
        }
        return Math.max(1, (var5 - arg3));
    }

    private void quickMove(int arg0, int arg1) {
        mc.gameMode.handleContainerInput(arg0, arg1, 0, ContainerInput.QUICK_MOVE, mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private void throwSlot(int arg0, int arg1) {
        mc.gameMode.handleContainerInput(arg0, arg1, 1, ContainerInput.THROW, mc.player);
        lastContainerActionMs = System.currentTimeMillis();
    }

    private boolean matches(ItemStack arg0, String arg1, WhitelistSetting arg2) {
        if ((arg0 == null) || (arg0.isEmpty())) {
            return false;
        }
        if (arg2 == null) break label_2;
        int var4 = (arg2.isWhitelistContains(arg0.getItem()) ? 1 : 0);
        Object var5 = arg1;
        int var6 = -1;
        switch (var5.hashCode()) {
            case -958939875 -> { break label_5; }
            case 65921 -> { break label_4; }
            default -> { break label_6; }
        }
        label_4:
        if (!(var5.equals("All"))) break label_6;
        var6 = 0;
        break label_6;
        label_5:
        if (!(var5.equals("BlackList"))) break label_6;
        var6 = 1;
        label_6:
        switch (var6) {
            case 0 -> { break label_7; }
            case 1 -> { break label_8; }
            default -> { break label_10; }
        }
        label_7:
        break label_11;
        label_8:
        if (!var4) {
            break label_11;
        }
        break label_11;
        label_10:
        label_11:
        return var4;
    }

    private boolean inInteractRange(int arg0) {
        Object var2 = this.interact.getValue();
        int var3 = -1;
        switch (var2.hashCode()) {
            case -2127604538 -> { break label_0; }
            case -16631492 -> { break label_1; }
            default -> { break label_2; }
        }
        label_0:
        if (!(var2.equals("Hotbar"))) break label_2;
        var3 = 0;
        break label_2;
        label_1:
        if (!(var2.equals("Inventory"))) break label_2;
        var3 = 1;
        label_2:
        switch (var3) {
            case 0 -> { break label_3; }
            case 1 -> { break label_5; }
            default -> { break label_7; }
        }
        label_3:
        if (arg0 <= 8) {
            break label_8;
        }
        break label_8;
        label_5:
        if (arg0 >= 9) {
            break label_8;
        }
        break label_8;
        label_7:
        label_8:
        return true;
    }

    private int invToHandlerSlot(int arg0) {
        Object var2 = mc.player.getInventory();
        Object var3 = mc.player.containerMenu.slots.iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Slot) var3.next());
            if (var4.container != var2) break label_0;
            if (var4.getContainerSlot() != arg0) {
                break label_0;
            }
            return var4.index;
        }
        return -1;
    }

    public String getMetaData() {
        Object var1 = new StringBuilder();
        if (this.steal.getValue()) {
            var1.append("Steal");
        }
        if (this.dump.getValue()) {
            var1.append((var1.length() > 0 ? "/Dump" : "Dump"));
        }
        if (this.drop.getValue()) {
            var1.append((var1.length() > 0 ? "/Drop" : "Drop"));
        }
        return var1.toString();
    }

    static {
        lastContainerActionMs = 0L;
    }


    enum Task {
        NONE,
        STEAL,
        DUMP,
        DROP;


        private Task() {
        }

    }
}
