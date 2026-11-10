package lightning.modules.impl.combat;

import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

@RegisterModule(name = "AutoPot", description = "Throws a splash potion under/above you to keep whitelisted potions' effects topped up.", category = Module.Category.COMBAT)
public class AutoPotModule extends Module {
    public ModeSetting mode;
    public WhitelistSetting whitelist;
    public NumberSetting amplifier;
    public BooleanSetting rotate;
    public ModeSetting throwMode;
    public ModeSetting swapMode;
    public BooleanSetting whenNoTarget;
    public BooleanSetting onlyPhased;
    public BooleanSetting selfToggle;
    public NumberSetting range;
    private final Timer throwTimer;
    private static final long THROW_DELAY_MS = 5000L;

    public AutoPotModule() {
        var tmp0 = new String[2];
        tmp0[0] = "WhiteList";
        tmp0[1] = "BlackList";
        this.mode = new ModeSetting("Mode", "WhiteList = keep these potions' effects topped up. BlackList = keep every potion's effects topped up except these.", "WhiteList", tmp0);
        this.whitelist = new WhitelistSetting("Whitelist", "Potions this mode's WhiteList/BlackList compares against.", WhitelistSetting.Type.POTIONS);
        this.amplifier = new NumberSetting("Amplifier", "Minimum amplifier level required before an effect counts as \"already have it\".", 1, 0, 4);
        this.rotate = new BooleanSetting("Rotate", "Rotates to look straight up/down before throwing (nami: real rotation, waits for it to land before throwing).", false);
        var tmp1 = new String[2];
        tmp1[0] = "Above";
        tmp1[1] = "Under";
        this.throwMode = new ModeSetting("Throw", "Where the potion lands relative to you.", new BooleanSetting.Visibility(this.rotate, true), "Under", tmp1);
        var tmp2 = new String[2];
        tmp2[0] = "Normal";
        tmp2[1] = "Silent";
        this.swapMode = new ModeSetting("Swap", "How the potion slot gets selected before throwing.", "Silent", tmp2);
        this.whenNoTarget = new BooleanSetting("NoTarget", "Only throws while no enemy is nearby.", false);
        this.onlyPhased = new BooleanSetting("OnlyPhased", "Only throws while phased into a block.", false);
        this.selfToggle = new BooleanSetting("SelfToggle", "Turns the module off once every whitelisted effect is already topped up.", true);
        this.range = new NumberSetting("Range", "How close an enemy has to be for NoTarget to see them.", new BooleanSetting.Visibility(this.whenNoTarget, true), 32, 4, 64);
        this.throwTimer = new Timer();
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        Object var3 = findMissingPotion();
        if (var3 == null) {
            if (this.selfToggle.getValue()) {
                setToggled(false);
            }
            return;
        }
        if (this.whenNoTarget.getValue() && hasNearbyEnemy()) {
            if (this.selfToggle.getValue()) {
                setToggled(false);
            }
            return;
        }
        if (this.onlyPhased.getValue() && !(isPhased())) {
            if (this.selfToggle.getValue()) {
                setToggled(false);
            }
            return;
        }
        int var4 = findPot(var3);
        if (var4 == -1) {
            if (this.selfToggle.getValue()) {
                setToggled(false);
            }
            return;
        }
        if (!(this.throwTimer.hasTimeElapsed(5000L))) return;
        int var5 = (var4 < 9 ? var4 : -1);
        int var6 = mc.player.getInventory().getSelectedSlot();
        if (var5 == -1) {
            InventoryUtils.swap("Pickup", var4, InventoryUtils.HOTBAR_START);
            var5 = InventoryUtils.HOTBAR_START;
        }
        var tmp0 = (this.throwMode.getValue().equalsIgnoreCase("Above") ? -90.0f : 90.0f);
        float var2 = tmp0;
        float var7 = tmp0;
        if (this.rotate.getValue()) {
            var tmp1 = new float[2];
            tmp1[0] = mc.player.getYRot();
            tmp1[1] = var2;
            Object var8 = tmp1;
            LightningClient.ROTATION_MANAGER.legacyRotate(var8, this, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
        }
        this.throwTimer.reset();
        InventoryUtils.switchSlot(this.swapMode.getValue(), var5, var6);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        InventoryUtils.switchBack(this.swapMode.getValue(), var5, var6);
    }

    private Potion findMissingPotion() {
        int var1 = this.amplifier.getValue().intValue();
        Object var2 = this.whitelist.getWhitelistedPotions().iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Potion) var2.next());
            int var4 = 0;
            Object var5 = var3.getEffects().iterator();
            label_1:
            if (!(var5.hasNext())) break label_6;
            Object var6 = ((MobEffectInstance) var5.next());
            int var7 = 0;
            Object var8 = mc.player.getActiveEffects().iterator();
            label_2:
            if (!(var8.hasNext())) break label_4;
            Object var9 = ((MobEffectInstance) var8.next());
            if (var9.getEffect() != var6.getEffect()) break label_2;
            if (var9.getAmplifier() < var1) {
                break label_2;
            }
            var7 = 1;
            break label_4;
            label_4:
            if (!(!var7)) {
                break label_1;
            }
            var4 = 1;
            break label_6;
            label_6:
            if (!var4) {
                break label_0;
            }
            return var3;
        }
        return null;
    }

    private int findPot(Potion arg0) {
        int var2 = 0;
        while (var2 < 36) {
            Object var4 = mc.player.getInventory().getItem(var2);
            if ((var4.isEmpty()) || (var4.getItem() != Items.SPLASH_POTION)) break label_2;
            var tmp0 = ((PotionContents) var4.get(DataComponents.POTION_CONTENTS));
            Object var3 = tmp0;
            if ((tmp0 == null) || (var3.potion().isEmpty())) break label_2;
            if (((Holder) var3.potion().get()).value() != arg0) {
                break label_2;
            }
            return var2;
            label_2:
            var2++;
        }
        return -1;
    }

    private boolean hasNearbyEnemy() {
        double var1 = Mth.square(this.range.getValue().doubleValue());
        Object var3 = mc.level.players().iterator();
        label_0:
        if (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            if ((var4 == mc.player) || (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString()))) break label_0;
            if (mc.player.distanceToSqr(var4) > var1) {
                break label_0;
            }
            return true;
        }
        return false;
    }

    private boolean isPhased() {
        Object var1 = mc.player.getBoundingBox();
        int var2 = Mth.floor(var1.minX);
        int var3 = Mth.ceil(var1.maxX);
        int var4 = Mth.floor(var1.minY);
        int var5 = Mth.ceil(var1.maxY);
        int var6 = Mth.floor(var1.minZ);
        int var7 = Mth.ceil(var1.maxZ);
        int var8 = var2;
        while (var8 < var3) {
            int var9 = var4;
            while (var9 < var5) {
                int var10 = var6;
                while (var10 < var7) {
                    Object var11 = new BlockPos(var8, var9, var10);
                    Object var12 = mc.level.getBlockState(var11).getCollisionShape(mc.level, var11);
                    if (var12.isEmpty()) break label_4;
                    if (!(var12.bounds().move(var11).intersects(var1))) {
                        break label_4;
                    }
                    return true;
                    label_4:
                    var10++;
                }
                var9++;
            }
            var8++;
        }
        return false;
    }

}
