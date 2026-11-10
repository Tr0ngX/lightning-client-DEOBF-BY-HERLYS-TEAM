package lightning.modules.impl.combat;

import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TippedArrowItem;

@RegisterModule(name = "SelfBow", description = "Automatically shoots arrows at you in order to give yourself potion effects.", category = Module.Category.COMBAT)
public class SelfBowModule extends Module {
    public BooleanSetting manual;
    public ModeSetting autoSwitch;
    public NumberSetting chargeTime;
    public BooleanSetting effectCycle;
    private boolean switched;
    private boolean todo;
    private boolean first;
    private int previousSlot;
    private int chargeTicks;
    private int bestArrow;
    private final ConcurrentLinkedQueue queue;

    public SelfBowModule() {
        this.manual = new BooleanSetting("Manual", "Whether or not to do the self bow manually.", false);
        var tmp0 = new String[2];
        tmp0[0] = "None";
        tmp0[1] = "Normal";
        this.autoSwitch = new ModeSetting("Switch", "The mode that will be used for switching slots.", "Normal", tmp0);
        this.chargeTime = new NumberSetting("ChargeTime", "The amount of ticks that the module will be charging the bow for.", 4, 0, 20);
        this.effectCycle = new BooleanSetting("EffectCycle", "Fires multiple arrows in case of having more than one arrow type.", false);
        this.switched = false;
        this.todo = false;
        this.first = false;
        this.previousSlot = -1;
        this.chargeTicks = 0;
        this.bestArrow = -1;
        this.queue = new ConcurrentLinkedQueue();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        int var3 = InventoryUtils.findHotbar(Items.BOW);
        if (this.manual.getValue()) {
            if (!(mc.player.isUsingItem())) break label_2;
            var tmp0 = (mc.player.getInventory().getSelectedItem().getItem() == Items.BOW ? 1 : 0);
            int var4 = tmp0;
            int var5 = tmp0;
            if (!(!var4) && !this.todo) {
                if (this.effectCycle.getValue()) {
                    findArrows();
                }
                this.todo = true;
                this.first = true;
            }
            if (!(!this.todo)) {
                if (mc.player.getInventory().getSelectedItem().getItem() != Items.BOW) {
                    this.todo = false;
                    mc.options.keyUse.setDown(false);
                    return;
                }
                mc.options.keyUse.setDown(true);
            }
            if (!this.todo) return;
        } else {
            if (this.autoSwitch.getValue().equals("None") && mc.player.getInventory().getSelectedItem().getItem() != Items.BOW) {
                LightningClient.CHAT_MANAGER.tagged("You are currently not holding a bow.", getName());
                setToggled(false);
                return;
            }
            if (!(this.autoSwitch.getValue().equals("None")) && var3 == -1) {
                LightningClient.CHAT_MANAGER.tagged("Could not find a bow in your hotbar.", getName());
                setToggled(false);
                return;
            }
            if (mc.player.getMainHandItem().getItem() != Items.BOW) {
                InventoryUtils.switchSlot(this.autoSwitch.getValue(), var3, this.previousSlot);
                this.switched = true;
            }
            mc.options.keyUse.setDown(true);
        }
        if (this.chargeTicks < (this.chargeTime.getValue().intValue() - (!(!this.first) ? 1 : 0))) {
            this.chargeTicks = (this.chargeTicks + 1);
            return;
        }
        if ((!(this.effectCycle.getValue())) || (this.queue.isEmpty())) break label_17;
        var tmp1 = ((Integer) this.queue.poll()).intValue();
        int var2 = tmp1;
        if (tmp1 == this.bestArrow) break label_17;
        InventoryUtils.swap("Pickup", var2, this.bestArrow);
        label_17:
        if (mc.getConnection() != null) {
            ProxyServerTickListener.allowSend(() -> mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(LightningClient.POSITION_MANAGER.getServerX(), LightningClient.POSITION_MANAGER.getServerY(), LightningClient.POSITION_MANAGER.getServerZ(), mc.player.getYRot(), -90.0f, LightningClient.POSITION_MANAGER.isServerOnGround(), mc.player.horizontalCollision)));
        }
        mc.options.keyUse.setDown(false);
        mc.gameMode.releaseUsingItem(mc.player);
        this.chargeTicks = 0;
        this.first = false;
        if (this.effectCycle.getValue()) {
            if (!(this.queue.isEmpty())) break label_21;
        }
        if (this.manual.getValue()) {
            this.todo = false;
            mc.options.keyUse.setDown(false);
            break label_21;
        }
        setToggled(false);
        label_21:
    }

    public void onEnable() {
        if (!(getNull())) {
            if (this.effectCycle.getValue() && !(this.manual.getValue())) {
                findArrows();
            }
            this.previousSlot = mc.player.getInventory().getSelectedSlot();
        }
    }

    public void onDisable() {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(!this.switched)) {
            InventoryUtils.switchSlot(this.autoSwitch.getValue(), this.previousSlot, this.previousSlot);
        }
        mc.options.keyUse.setDown(false);
    }

    private void findArrows() {
        this.bestArrow = -1;
        int var1 = 9;
        while (var1 < 36) {
            if (mc.player.getInventory().getItem(var1).isEmpty()) break label_3;
            var tmp0 = mc.player.getInventory().getItem(var1).getItem();
            Object var2 = tmp0;
            if (!((tmp0 instanceof TippedArrowItem))) {
                break label_3;
            }
            if (this.bestArrow == -1) {
                this.bestArrow = var1;
            }
            this.queue.add(var1);
            label_3:
            var1++;
        }
    }

    public String getMetaData() {
        if (mc.player.getInventory().getSelectedItem().getItem() == Items.BOW) {
        } else {
        }
        int var1 = 0;
        return String.valueOf(var1);
    }

}
