package lightning.modules.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.PacketReceiveEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EnchantmentUtils;
import lightning.utils.minecraft.EntityUtils;
import lightning.utils.minecraft.GrimUtils;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.rotations.RotationUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "KillAura", description = "Automatically attacks optimal targets using Nami's combat engine.", category = Module.Category.COMBAT, proxyEnhanced = true)
public class KillAuraModule extends Module {
    public NumberSetting range;
    public BooleanSetting stanceAbuse;
    public NumberSetting delay;
    public ModeSetting swap;
    public BooleanSetting onlyGapHold;
    public BooleanSetting onlyTotemHold;
    public BooleanSetting pauseOnEat;
    public ModeSetting tpsMode;
    public BooleanSetting multiTask;
    public ModeSetting stopSprinting;
    public ModeSetting rotate;
    public BooleanSetting swing;
    public BooleanSetting render;
    public ModeSetting renderMode;
    public CategorySetting entitiesCategory;
    public BooleanSetting players;
    public BooleanSetting friends;
    public BooleanSetting ignoreNaked;
    public BooleanSetting hostiles;
    public BooleanSetting neutrals;
    public BooleanSetting animals;
    public BooleanSetting projectiles;
    public ColorSetting boxColor;
    public Entity target;
    private Entity lastTarget;
    private long lastTargetTime;
    private static final long FADE_TIME_MS = 300L;
    private long lastAttackTime;
    private int originalSlot;
    private int silentHeldWeaponSlot;
    private int silentHeldReturnSlot;

    public KillAuraModule() {
        this.range = new NumberSetting("Range", "The reach distance to entities.", 3.0, 1.0, 6.0);
        this.stanceAbuse = new BooleanSetting("StanceAbuse", "Abuses Grim stance eye positions to optimize reach.", false);
        this.delay = new NumberSetting("Delay", "Attack cooldown delay multiplier.", 0.92, 0.0, 1.0);
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "Require";
        tmp0[2] = "Normal";
        tmp0[3] = "Silent";
        this.swap = new ModeSetting("Swap", "Weapon switch mode.", "Require", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Silent";
        this.onlyGapHold = new BooleanSetting("OnlyGapHold", "Only attacks using silent swap when holding a golden apple in main hand.", new ModeSetting.Visibility(this.swap, tmp1), false);
        var tmp2 = new String[1];
        tmp2[0] = "Silent";
        this.onlyTotemHold = new BooleanSetting("OnlyTotemHold", "Only attacks using silent swap when holding a totem in main hand.", new ModeSetting.Visibility(this.swap, tmp2), false);
        this.pauseOnEat = new BooleanSetting("PauseOnEat", "Pauses attacking while eating or using items.", false);
        var tmp3 = new String[2];
        tmp3[0] = "None";
        tmp3[1] = "Average";
        this.tpsMode = new ModeSetting("TPS", "TPS synchronization mode for attack cooldown.", "Average", tmp3);
        this.multiTask = new BooleanSetting("Multitask", "Allows attacking while eating or using items.", true);
        var tmp4 = new String[2];
        tmp4[0] = "None";
        tmp4[1] = "Packet";
        this.stopSprinting = new ModeSetting("Sprinting", "Sprint management during attacks.", "None", tmp4);
        var tmp5 = new String[4];
        tmp5[0] = "None";
        tmp5[1] = "Normal";
        tmp5[2] = "Hold";
        tmp5[3] = "Silent";
        this.rotate = new ModeSetting("Rotate", "Rotation mode.", "Normal", tmp5);
        this.swing = new BooleanSetting("Swing", "Whether to swing your hand when attacking.", true);
        this.render = new BooleanSetting("Render", "Renders an indicator around the current target.", true);
        var tmp6 = new String[3];
        tmp6[0] = "Box";
        tmp6[1] = "Circle";
        tmp6[2] = "Both";
        this.renderMode = new ModeSetting("RenderMode", "Target render mode.", new BooleanSetting.Visibility(this.render, true), "Circle", tmp6);
        this.entitiesCategory = new CategorySetting("Entities", "Target entity filter settings.");
        this.players = new BooleanSetting("Players", "Target player entities.", new CategorySetting.Visibility(this.entitiesCategory), true);
        this.friends = new BooleanSetting("Friends", "Target friends.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.ignoreNaked = new BooleanSetting("IgnoreNaked", "Ignore Naked, even that player wears elytra only", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.hostiles = new BooleanSetting("Hostiles", "Target hostile mobs (Zombies, Skeletons, Spiders, Piglins, etc.).", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.neutrals = new BooleanSetting("Neutrals", "Target neutral mobs (Endermen, Zombie Piglins, etc.).", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.animals = new BooleanSetting("Animals", "Target passive animal mobs.", new CategorySetting.Visibility(this.entitiesCategory), false);
        this.projectiles = new BooleanSetting("Projectiles", "Target shulker bullets & fireballs.", new CategorySetting.Visibility(this.entitiesCategory), true);
        this.boxColor = new ColorSetting("TargetColor", "Color of the target render box.", new BooleanSetting.Visibility(this.render, true), ColorUtils.getDefaultFillColor());
        this.target = null;
        this.lastTarget = null;
        this.lastTargetTime = 0L;
        this.lastAttackTime = 0L;
        this.originalSlot = -1;
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    public Entity getTarget() {
        return this.target;
    }

    private void releaseSilentHold() {
        if (this.silentHeldWeaponSlot == -1) return;
        InventoryUtils.clearLongHold(this.silentHeldWeaponSlot);
        if (mc.player != null) {
            InventoryUtils.switchBack("Silent", this.silentHeldWeaponSlot, this.silentHeldReturnSlot);
        }
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientDisconnectEvent arg0) {
        if (this.silentHeldWeaponSlot != -1) {
            InventoryUtils.clearLongHold(this.silentHeldWeaponSlot);
        }
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
        this.originalSlot = -1;
        this.target = null;
        this.lastTarget = null;
    }

    public void onEnable() {
        this.target = null;
        this.lastTarget = null;
        this.originalSlot = -1;
        this.lastAttackTime = 0L;
        this.silentHeldWeaponSlot = -1;
        this.silentHeldReturnSlot = -1;
    }

    public void onDisable() {
        if (this.originalSlot != -1 && mc.player != null) {
            InventoryUtils.switchSlot("Normal", this.originalSlot, mc.player.getInventory().getSelectedSlot());
            this.originalSlot = -1;
        }
        releaseSilentHold();
        this.target = null;
        this.lastTarget = null;
    }

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent arg0) {
        if (shouldRunOnProxy()) {
            releaseSilentHold();
            break label_55;
        }
        if (mc.player != null && mc.level != null && !(mc.player.isDeadOrDying())) {
            Object var2 = ((AutoCrystalModule) LightningClient.MODULE_MANAGER.getModule(AutoCrystalModule.class));
            if (((var2 == null) || (!(var2.isToggled()))) || (var2.getTarget() == null)) break label_1;
            releaseSilentHold();
            break label_53;
            label_1:
            if ((mc.options.keyUse.isDown()) || (mc.player.isUsingItem())) break label_2;
            if (mc.player.getUseItemRemainingTicks() <= 0) break label_3;
            label_2:
            break label_4;
            label_3:
            label_4:
            int var3 = 0;
            if ((!(this.pauseOnEat.getValue())) || (!var3)) break label_5;
            this.target = null;
            releaseSilentHold();
            break label_53;
            label_5:
            if (!(this.multiTask.getValue()) && !(!var3)) {
                this.target = null;
                releaseSilentHold();
                break label_53;
            }
            Object var4 = mc.player.getMainHandItem();
            if (!(this.swap.getValue().equalsIgnoreCase("Silent"))) break label_15;
            int var5 = this.onlyGapHold.getValue();
            int var6 = this.onlyTotemHold.getValue();
            if (!var5) {
                if (!var6) break label_15;
            }
            if (!(var4.is(Items.GOLDEN_APPLE))) {
                if (var4.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                }
            } else {
            }
            int var7 = 0;
            int var8 = var4.is(Items.TOTEM_OF_UNDYING);
            if (!(!var5)) {
                if (!var7) {
                }
                if (!(!var6) && !(!var8)) {
                }
            } else {
            }
            int var9 = 0;
            if (var9) break label_15;
            this.target = null;
            releaseSilentHold();
            return;
            label_15:
            var5 = findOptimalTarget();
            if (var5 == null) break label_52;
            if (this.swap.getValue().equalsIgnoreCase("Require")) {
                if (!(isItemAWeapon(var4))) break label_52;
            }
            this.target = var5;
            var6 = 0;
            if (!(this.target instanceof ShulkerBullet) && !(this.target instanceof LargeFireball)) {
                var7 = calculatePlayerAttackDamage();
                var9 = this.target;
                if (!((var9 instanceof LivingEntity))) break label_17;
                var8 = ((LivingEntity) var9);
                if (var8.getMaxHealth() > var7) break label_17;
                var6 = 1;
                label_17:
            } else {
                var6 = 1;
            }
            var7 = getBestWeaponSlot();
            var8 = getCooldownMs(var7);
            long var10 = System.currentTimeMillis();
            if (!var6) {
                if (((double) (var10 - this.lastAttackTime)) >= var8) {
                }
            } else {
            }
            int var12 = 0;
            Object var15 = this.rotate.getValue();
            int var16 = -1;
            switch (var15.hashCode()) {
                case -1955878649 -> { break label_24; }
                case 2255071 -> { break label_23; }
                default -> { break label_25; }
            }
            label_23:
            if (!(var15.equals("Hold"))) break label_25;
            var16 = 0;
            break label_25;
            label_24:
            if (!(var15.equals("Normal"))) break label_25;
            var16 = 1;
            label_25:
            switch (var16) {
                case 0 -> { break label_26; }
                case 1 -> { break label_27; }
                default -> { break label_28; }
            }
            label_26:
            break label_29;
            label_27:
            break label_29;
            label_28:
            label_29:
            double var13 = 0.0;
            if (!var6 && !var12) {
                if ((var8 - ((double) (var10 - this.lastAttackTime))) <= var13) {
                }
                var15 = mc.player.getEyePosition(1.0f);
                if (!(this.stanceAbuse.getValue())) break label_33;
                var16 = 1.7976931348623157E308;
                Object var18 = GrimUtils.getPossibleEyePositions(mc.player);
                int var19 = var18.length;
                int var20 = 0;
                while (var20 < var19) {
                    Object var21 = var18[var20];
                    Object var22 = RotationUtils.getClampClosestPoint(var21, this.target.getBoundingBox());
                    double var23 = var21.distanceTo(var22);
                    if (var23 < var16) {
                        var16 = var23;
                        var15 = var21;
                    }
                    var20++;
                }
                label_33:
                var16 = this.target.getBoundingBox().getCenter();
                var18 = this.target;
                if (!((var18 instanceof LivingEntity))) break label_34;
                Object var17 = ((LivingEntity) var18);
                if (this.target.getBoundingBox().getYsize() < 0.8) break label_34;
                var16 = new Vec3(this.target.getX(), (this.target.getY() + (((double) var17.getEyeHeight()) * 0.75)), this.target.getZ());
                label_34:
                var17 = RotationUtils.getYRotToVec(var15, var16);
                var18 = RotationUtils.getXRotToVec(var15, var16);
                var19 = this.target.getBoundingBox().contains(var15);
                if (getTargetDistance(var15, this.target) > this.range.getValue().doubleValue()) {
                    this.target = null;
                    releaseSilentHold();
                    return;
                }
                if (!(this.rotate.getValue().equalsIgnoreCase("None"))) {
                    if (!(!var19)) {
                    }
                } else {
                }
                var20 = 0;
                if (this.rotate.getValue().equalsIgnoreCase("None")) break label_43;
                if (this.rotate.getValue().equalsIgnoreCase("Silent")) {
                    LightningClient.ROTATION_MANAGER.silentRotate(var17, var18);
                    var20 = 1;
                    break label_43;
                }
                LightningClient.ROTATION_MANAGER.legacyRotate(var17, var18, this, LightningClient.ROTATION_MANAGER.getLegacyModulePriority(this));
                var21 = RotationUtils.raycastTarget(var15, this.target, this.range.getValue().doubleValue(), LightningClient.ROTATION_MANAGER.getServerYaw(), LightningClient.ROTATION_MANAGER.getServerPitch());
                if (var21 == null) {
                    if (!(!var19)) {
                    }
                } else {
                }
                var20 = 0;
                label_43:
                if (!var20) return;
            }
            if (!(!var12)) {
                var15 = 0;
                if (this.stopSprinting.getValue().equalsIgnoreCase("Packet") && mc.player.isSprinting() && !(mc.player.isShiftKeyDown())) {
                    mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                    var15 = 1;
                }
                var16 = mc.player.getInventory().getSelectedSlot();
                if (this.swap.getValue().equalsIgnoreCase("Normal") && var7 != -1) {
                    if (this.originalSlot == -1 && var16 != var7) {
                        this.originalSlot = var16;
                    }
                    InventoryUtils.switchSlot("Normal", var7, var16);
                    break label_48;
                }
                if (((!(this.swap.getValue().equalsIgnoreCase("Silent"))) || (var7 == -1)) || (this.silentHeldWeaponSlot == var7)) break label_48;
                releaseSilentHold();
                InventoryUtils.switchSlot("Silent", var7, var16);
                InventoryUtils.markLongHold(var7);
                this.silentHeldWeaponSlot = var7;
                this.silentHeldReturnSlot = var16;
                label_48:
                mc.getConnection().send(new ServerboundAttackPacket(this.target.getId()));
                mc.player.resetAttackStrengthTicker();
                if (this.swing.getValue()) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
                if (!(!var15)) {
                    mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                }
                this.lastAttackTime = System.currentTimeMillis();
            }
            break label_53;
            label_52:
            this.target = null;
            releaseSilentHold();
            label_53:
            break label_55;
        }
        releaseSilentHold();
        label_55:
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent arg0) {
        if (!((arg0.getPacket() instanceof ServerboundSetCarriedItemPacket))) return;
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        mc.execute(() -> this.lastAttackTime = System.currentTimeMillis());
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((!(this.render.getValue())) || (mc.player == null)) || (mc.player.isDeadOrDying())) {
            return;
        }
        Object var2 = this.target;
        float var3 = 1.0f;
        if (var2 != null) {
            this.lastTarget = var2;
            this.lastTargetTime = System.currentTimeMillis();
            break label_4;
        }
        if (this.lastTarget == null) break label_4;
        long var4 = (System.currentTimeMillis() - this.lastTargetTime);
        if (var4 < 300L) {
            var2 = this.lastTarget;
            var3 = (1.0f - (((float) var4) / 300.0f));
            break label_4;
        }
        this.lastTarget = null;
        label_4:
        if (var2 == null) return;
        var4 = ColorUtils.getColor(this.boxColor.getColor(), ((int) (((float) this.boxColor.getColor().getAlpha()) * var3)));
        if (!(this.renderMode.getValue().equalsIgnoreCase("Box"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
            Object var5 = EntityUtils.getRenderPos(var2, arg0.getTickDelta());
            Object var6 = var2.getBoundingBox().move((var5.x - var2.getX()), (var5.y - var2.getY()), (var5.z - var2.getZ()));
            Renderer3D.renderBox(arg0.getMatrices(), var6, var4);
            Renderer3D.renderBoxOutline(arg0.getMatrices(), var6, var4);
        }
        if (!(this.renderMode.getValue().equalsIgnoreCase("Circle"))) {
            if (this.renderMode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderTargetCircle(arg0.getMatrices(), var2, arg0.getTickDelta(), var4);
        }
    }

    public double getTargetDistance(Vec3 arg0, Entity arg1) {
        if ((mc.player == null) || (arg1 == null)) {
            return 1.7976931348623157E308;
        }
        if (this.stanceAbuse.getValue()) {
            double var3 = 1.7976931348623157E308;
            Object var5 = GrimUtils.getPossibleEyePositions(mc.player);
            int var6 = var5.length;
            int var7 = 0;
            while (var7 < var6) {
                Object var8 = var5[var7];
                double var9 = var8.distanceTo(RotationUtils.getClampClosestPoint(var8, arg1.getBoundingBox()));
                if (var9 >= var3) {
                } else {
                    var3 = var9;
                }
                var7++;
            }
            return var3;
        }
        return arg0.distanceTo(RotationUtils.getClampClosestPoint(arg0, arg1.getBoundingBox()));
    }

    private Entity findOptimalTarget() {
        if ((mc.player == null) || (mc.level == null)) {
            return null;
        }
        Object var1 = new ArrayList();
        Object var2 = mc.player.getEyePosition(1.0f);
        double var3 = this.range.getValue().doubleValue();
        Object var5 = mc.level.entitiesForRendering().iterator();
        while (var5.hasNext()) {
            Object var6 = ((Entity) var5.next());
            if (((var6 == mc.player) || (!(var6.isAlive()))) || (EntityUtils.isGhost(var6))) break label_2;
            double var7 = getTargetDistance(var2, var6);
            if (getTargetDistance(var2, var6) > var3) {
                break label_2;
            }
            if ((var6 instanceof Player)) {
                Object var9 = ((Player) var6);
                if (!(this.players.getValue())) break label_2;
                if (!(this.friends.getValue())) {
                    if (LightningClient.FRIEND_MANAGER.contains(var9.getName().getString())) break label_2;
                }
                if (this.ignoreNaked.getValue() && EntityUtils.isNaked(var9)) {
                    break label_2;
                }
                var1.add(var6);
                break label_2;
            }
            if (!(this.projectiles.getValue())) break label_8;
            if (!((var6 instanceof ShulkerBullet))) {
                if (!((var6 instanceof LargeFireball))) break label_8;
            }
            var1.add(var6);
            break label_2;
            label_8:
            if (EntityUtils.isNeutral(var6)) {
                if (!(this.neutrals.getValue()) && !(EntityUtils.isAngry(var6))) {
                    break label_2;
                }
                var1.add(var6);
                break label_2;
            }
            if (this.hostiles.getValue() && EntityUtils.isHostile(var6)) {
                var1.add(var6);
                break label_2;
            }
            if (!(this.animals.getValue())) break label_2;
            if (!(EntityUtils.isAnimal(var6))) {
                break label_2;
            }
            var1.add(var6);
        }
        var5 = var1.stream().filter((arg0) -> (arg0 instanceof Player)).sorted(Comparator.comparingDouble(() -> getTargetDistance(arg0, arg1))).toList();
        if (!(var5.isEmpty())) return ((Entity) var5.get(0));
        var6 = var1.stream().filter(() -> {
    if (!((arg1 instanceof Creeper))) break label_0;
    return getTargetDistance(arg0, arg1) <= 3.0;
}).sorted(Comparator.comparingDouble(() -> getTargetDistance(arg0, arg1))).toList();
        if (!(var6.isEmpty())) return ((Entity) var6.get(0));
        var7 = var1.stream().filter((arg0) -> {
    if (!((arg0 instanceof ShulkerBullet))) {
        if ((arg0 instanceof LargeFireball)) {
        }
    } else {
    }
    return false;
}).sorted(Comparator.comparingDouble(() -> getTargetDistance(arg0, arg1))).toList();
        if (!(var7.isEmpty())) return ((Entity) var7.get(0));
        return ((Entity) var1.stream().min(Comparator.comparingDouble(() -> getTargetDistance(arg0, arg1))).orElse(null));
    }

    private float getTps() {
        if (this.tpsMode.getValue().equalsIgnoreCase("None")) return 20.0f;
        return LightningClient.SERVER_MANAGER.getTickRate();
    }

    private double getCooldownMs(int arg0) {
        if (mc.player == null) return 500.0;
        if (arg0 == -1) {
        } else {
        }
        int var5 = arg0;
        Object var6 = mc.player.getInventory().getItem(var5);
        double var3 = mc.player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        double var7 = mc.player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
        if (!(var6.has(DataComponents.ATTRIBUTE_MODIFIERS))) break label_5;
        var tmp0 = ((ItemAttributeModifiers) var6.get(DataComponents.ATTRIBUTE_MODIFIERS));
        Object var2 = tmp0;
        if (tmp0 == null) break label_5;
        Object var9 = var2.modifiers().iterator();
        while (var9.hasNext()) {
            Object var10 = ((ItemAttributeModifiers.Entry) var9.next());
            if (!(var10.attribute().is(Attributes.ATTACK_SPEED))) {
                break label_3;
            }
            var7 = (var7 + var10.modifier().amount());
        }
        label_5:
        var9 = ((1.0 / Math.max(0.1, var7)) * 20.0);
        double var11 = ((var9 * 50.0) * this.delay.getValue().doubleValue());
        if (this.tpsMode.getValue().equalsIgnoreCase("Average")) {
            float var13 = Math.max(1.0f, LightningClient.SERVER_MANAGER.getTickRate());
            var11 = (var11 * ((double) (20.0f / var13)));
        }
        return var11;
    }

    public int getBestWeaponSlot() {
        if (mc.player == null) return -1;
        int var1 = -1;
        float var2 = -1.0f;
        if (mc.player.getAbilities().flying) break label_1;
        int var3 = (!(mc.player.onGround()) ? 1 : 0);
        int var4 = 0;
        while (var4 < 9) {
            Object var9 = mc.player.getInventory().getItem(var4);
            if (var9.isEmpty()) {
                break label_12;
            }
            int var10 = var9.is(ItemTags.SWORDS);
            int var11 = var9.is(ItemTags.AXES);
            int var12 = (var9.getItem() instanceof TridentItem);
            int var13 = (var9.getItem() instanceof MaceItem);
            if (!var10 && !var11 && !var12 && !var13) {
                break label_12;
            }
            if (!(!var13) && !(!var3)) return var4;
            float var14 = 0.0f;
            if (!(var9.has(DataComponents.ATTRIBUTE_MODIFIERS))) break label_9;
            var tmp0 = ((ItemAttributeModifiers) var9.get(DataComponents.ATTRIBUTE_MODIFIERS));
            Object var8 = tmp0;
            if (tmp0 == null) break label_9;
            Object var15 = var8.modifiers().iterator();
            while (var15.hasNext()) {
                Object var16 = ((ItemAttributeModifiers.Entry) var15.next());
                if (!(var16.attribute().is(Attributes.ATTACK_DAMAGE))) {
                    break label_7;
                }
                var14 = (var14 + ((float) var16.modifier().amount()));
            }
            label_9:
            if (!(!var10)) {
                var14 = (var14 + 5.0f);
            }
            var tmp1 = EnchantmentUtils.getEnchantmentLevel(var9, Enchantments.SHARPNESS);
            int var7 = tmp1;
            var tmp2 = EnchantmentUtils.getEnchantmentLevel(var9, Enchantments.SMITE);
            int var6 = tmp2;
            var tmp3 = EnchantmentUtils.getEnchantmentLevel(var9, Enchantments.BANE_OF_ARTHROPODS);
            int var5 = tmp3;
            var tmp4 = (var14 + (((((float) tmp1) * 1.25f) + (((float) tmp2) * 2.5f)) + (((float) tmp3) * 2.5f)));
            var14 = tmp4;
            if (tmp4 <= var2) {
                break label_12;
            }
            var2 = var14;
            var1 = var4;
            label_12:
            var4++;
        }
        return var1;
    }

    private float calculatePlayerAttackDamage() {
        if (mc.player == null) return 1.0f;
        float var3 = 1.0f;
        if (!(mc.player.hasEffect(MobEffects.STRENGTH))) break label_1;
        var tmp0 = mc.player.getEffect(MobEffects.STRENGTH);
        Object var2 = tmp0;
        if (tmp0 == null) break label_1;
        var3 = (var3 + (3.0f * ((float) (var2.getAmplifier() + 1))));
        label_1:
        if (!(mc.player.hasEffect(MobEffects.WEAKNESS))) break label_2;
        var tmp1 = mc.player.getEffect(MobEffects.WEAKNESS);
        Object var1 = tmp1;
        if (tmp1 == null) break label_2;
        var3 = (var3 - (4.0f * ((float) (var1.getAmplifier() + 1))));
        label_2:
        return Math.max(var3, 0.0f);
    }

    public static boolean isItemAWeapon(ItemStack arg0) {
        if ((arg0 == null) || (arg0.isEmpty())) {
            return false;
        }
        if (!(arg0.is(ItemTags.SWORDS)) && !(arg0.is(ItemTags.AXES)) && !(arg0.getItem() instanceof TridentItem)) {
            if ((arg0.getItem() instanceof MaceItem)) {
            }
        } else {
        }
        return false;
    }

    public String getMetaData() {
        if (this.target == null) {
        } else {
        }
        return this.target.getName().getString();
    }

}
