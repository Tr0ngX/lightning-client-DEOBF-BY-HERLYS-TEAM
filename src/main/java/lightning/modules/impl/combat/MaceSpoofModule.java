package lightning.modules.impl.combat;

import lightning.events.SubscribeEvent;
import lightning.events.impl.AttackEntityEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.mixins.accessors.PlayerMoveC2SPacketAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;

@RegisterModule(name = "MaceSpoof", description = "Spoof fall height for mace damage.", category = Module.Category.COMBAT)
public class MaceSpoofModule extends Module {
    public ModeSetting mode;
    public BooleanSetting noCrystal;
    public BooleanSetting inventorySwap;
    public BooleanSetting onlyGround;
    public NumberSetting height;
    private boolean attacking;

    public MaceSpoofModule() {
        var tmp0 = new String[3];
        tmp0[0] = "Vanilla";
        tmp0[1] = "NCP";
        tmp0[2] = "Swap";
        this.mode = new ModeSetting("Mode", "Mode of spoofing.", "Vanilla", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Swap";
        this.noCrystal = new BooleanSetting("NoCrystal", "Don't apply on crystal attack.", new ModeSetting.Visibility(this.mode, tmp1), true);
        var tmp2 = new String[1];
        tmp2[0] = "Swap";
        this.inventorySwap = new BooleanSetting("InventorySwap", "Use inventory swap (vs hotbar swap).", new ModeSetting.Visibility(this.mode, tmp2), false);
        var tmp3 = new String[1];
        tmp3[0] = "Vanilla";
        this.onlyGround = new BooleanSetting("OnlyGround", "Only spoof when on ground or flying.", new ModeSetting.Visibility(this.mode, tmp3), true);
        var tmp4 = new String[1];
        tmp4[0] = "Vanilla";
        this.height = new NumberSetting("Height", "Fake fall height.", new ModeSetting.Visibility(this.mode, tmp4), 25.0, 1.0, 2000.0);
        this.attacking = false;
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent arg0) {
        if (!(!this.attacking)) return;
        if ((mc.player == null) || (mc.level == null)) break label_1;
        if (mc.getConnection() == null) {
            label_1:
            return;
        }
        if (this.mode.getValue().equalsIgnoreCase("Vanilla")) {
            if (!(mc.player.getMainHandItem().is(Items.MACE))) return;
            Object var2 = arg0.getTarget();
            if ((var2 == null) || ((var2 instanceof EndCrystal))) {
                return;
            }
            if (this.onlyGround.getValue() && !(mc.player.onGround()) && !(mc.player.getAbilities().flying)) return;
            if (((mc.player.isInLava()) || (mc.player.isUnderWater())) || (mc.player.isInWater())) {
                return;
            }
            int var3 = 0;
            while (var3 < 4) {
                sendFakeY(0.0);
                var3++;
            }
            sendFakeY(this.height.getValue().doubleValue());
            sendFakeY(0.0);
            break label_17;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("Swap"))) break label_17;
        var2 = arg0.getTarget();
        if (this.noCrystal.getValue() && (var2 instanceof EndCrystal)) return;
        var3 = getMaceSlot();
        if (var3 == -1) return;
        this.attacking = true;
        doSpoof(var2, var3);
        this.attacking = false;
        break label_17;
        Object var4 = null;
        this.attacking = false;
        throw var4;
        label_17:
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (!(this.mode.getValue().equalsIgnoreCase("NCP"))) break label_2;
        var tmp0 = arg0.getPacket();
        Object var2 = tmp0;
        if (!((tmp0 instanceof ServerboundMovePlayerPacket))) break label_2;
        Object var3 = ((ServerboundMovePlayerPacket) var2);
        ((PlayerMoveC2SPacketAccessor) var3).setOnGround(false);
        label_2:
    }

    private void doSpoof(Entity arg0, int arg1) {
        int var3 = mc.player.getInventory().getSelectedSlot();
        if (this.inventorySwap.getValue()) {
            InventoryUtils.click(arg1, var3, ContainerInput.SWAP);
            mc.gameMode.attack(mc.player, arg0);
            mc.player.resetAttackStrengthTicker();
            InventoryUtils.click(arg1, var3, ContainerInput.SWAP);
        } else {
            InventoryUtils.switchSlot("Silent", arg1, var3);
            mc.gameMode.attack(mc.player, arg0);
            mc.player.resetAttackStrengthTicker();
            InventoryUtils.switchBack("Silent", arg1, var3);
        }
    }

    private int getMaceSlot() {
        if (mc.player == null) return -1;
        if (this.inventorySwap.getValue()) {
            int var1 = 9;
            while (var1 <= 35) {
                if (!(mc.player.getInventory().getItem(var1).is(Items.MACE))) {
                } else {
                    return var1;
                }
                var1++;
            }
            break label_9;
        }
        var1 = 0;
        while (var1 < 9) {
            if (!(mc.player.getInventory().getItem(var1).is(Items.MACE))) {
            } else {
                return var1;
            }
            var1++;
        }
        label_9:
        return -1;
    }

    private void sendFakeY(double arg0) {
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), (mc.player.getY() + arg0), mc.player.getZ(), false, mc.player.horizontalCollision));
    }

    public String getMetaData() {
        return this.mode.getValue();
    }

}
