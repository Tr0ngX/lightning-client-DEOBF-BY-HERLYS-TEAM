package lightning.utils.minecraft;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityUtils implements IMinecraft {
    public static Map POTION_COLORS;
    private static final Identifier ATTACKING_SPEED_MODIFIER;

    public static boolean isPhased(Entity arg0) {
        if ((arg0 == null) || (mc.level == null)) {
            return false;
        }
        Object var1 = arg0.getBoundingBox();
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
                    if (var12.isEmpty()) break label_6;
                    if (!(var12.bounds().move(var11).intersects(var1))) {
                        break label_6;
                    }
                    return true;
                    label_6:
                    var10++;
                }
                var9++;
            }
            var8++;
        }
        return false;
    }

    public static List getPhasedBlocks(Entity arg0) {
        Object var1 = new ArrayList();
        if ((arg0 == null) || (mc.level == null)) {
            return var1;
        }
        Object var2 = arg0.getBoundingBox();
        int var3 = Mth.floor(var2.minX);
        int var4 = Mth.ceil(var2.maxX);
        int var5 = Mth.floor(var2.minY);
        int var6 = Mth.ceil(var2.maxY);
        int var7 = Mth.floor(var2.minZ);
        int var8 = Mth.ceil(var2.maxZ);
        int var9 = var3;
        while (var9 < var4) {
            int var10 = var5;
            while (var10 < var6) {
                int var11 = var7;
                while (var11 < var8) {
                    Object var12 = new BlockPos(var9, var10, var11);
                    Object var13 = mc.level.getBlockState(var12).getCollisionShape(mc.level, var12);
                    if (var13.isEmpty()) break label_6;
                    if (!(var13.bounds().move(var12).intersects(var2))) {
                        break label_6;
                    }
                    var1.add(var12);
                    label_6:
                    var11++;
                }
                var10++;
            }
            var9++;
        }
        return var1;
    }

    public static boolean isEating() {
        if (mc.player == null) return false;
        if (mc.player.isUsingItem()) return true;
        if (mc.options.keyUse.isDown()) {
            Object var0 = mc.player.getMainHandItem();
            Object var1 = mc.player.getOffhandItem();
            if (!(var0.isEmpty()) && var0.getUseDuration(mc.player) > 0) return true;
            if (!(var1.isEmpty()) && var1.getUseDuration(mc.player) > 0) return true;
        }
        return false;
    }

    public static boolean isBot(Player arg0) {
        if (((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class)).isToggled() && arg0 == ((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class)).getPlayer()) return false;
        Object var1 = mc.getConnection().getPlayerInfo(arg0.getUUID());
        if (var1 != null && var1.getProfile() != null && !(arg0.getUUID().toString().startsWith(arg0.getName().getString()))) {
            if (!(arg0.getGameProfile().name().equals(arg0.getName().getString()))) {
            }
        } else {
        }
        return false;
    }

    public static boolean isNaked(Player arg0) {
        if (arg0 == null) return false;
        var tmp0 = new EquipmentSlot[4];
        tmp0[0] = EquipmentSlot.HEAD;
        tmp0[1] = EquipmentSlot.CHEST;
        tmp0[2] = EquipmentSlot.LEGS;
        tmp0[3] = EquipmentSlot.FEET;
        Object var1 = tmp0;
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Object var4 = var1[var3];
            Object var5 = arg0.getItemBySlot(var4);
            if (var5.isEmpty()) break label_3;
            if (var4 == EquipmentSlot.CHEST && var5.getItem() == Items.ELYTRA) {
                break label_3;
            }
            return false;
            label_3:
            var3++;
        }
        return true;
    }

    public static boolean isGhost(Entity arg0) {
        if ((arg0 == null) || (LightningClient.MODULE_MANAGER == null)) {
            return false;
        }
        Object var1 = ((PopChamsModule) LightningClient.MODULE_MANAGER.getModule(PopChamsModule.class));
        if (var1 != null && var1.isGhost(arg0)) return true;
        Object var2 = ((LogoutSpotModule) LightningClient.MODULE_MANAGER.getModule(LogoutSpotModule.class));
        if (var2 == null) break label_3;
        return var2.isGhost(arg0);
    }

    public static int getLatency(Player arg0) {
        Object var1 = mc.getConnection().getPlayerInfo(arg0.getUUID());
        if (var1 == null) {
        } else {
        }
        return var1.getLatency();
    }

    public static GameType getGameMode(Player arg0) {
        Object var1 = mc.getConnection().getPlayerInfo(arg0.getUUID());
        if (var1 == null) {
        } else {
        }
        return var1.getGameMode();
    }

    public static String getGameModeName(GameType arg0) {
        switch (EntityUtils.1.$SwitchMap$net$minecraft$world$level$GameType[arg0.ordinal()]) {
            case 1 -> { break label_0; }
            case 2 -> { break label_1; }
            case 3 -> { break label_2; }
            default -> { break label_3; }
        }
        label_0:
        break label_4;
        label_1:
        break label_4;
        label_2:
        break label_4;
        label_3:
        label_4:
        return "S";
    }

    public static double getSpeed(Entity arg0, EntityUtils.SpeedUnit arg1) {
        double var2 = Math.sqrt((Mth.square(Math.abs((arg0.getX() - arg0.xo))) + Mth.square(Math.abs((arg0.getZ() - arg0.zo)))));
        if (arg1 == EntityUtils.SpeedUnit.KILOMETERS) return (((var2 * 3.6) * ((double) LightningClient.WORLD_MANAGER.getTimerMultiplier())) * 20.0);
        return ((var2 / 0.05) * ((double) LightningClient.WORLD_MANAGER.getTimerMultiplier()));
    }

    public static boolean hasNegativeEffects(Player arg0) {
        Object var1 = new ArrayList(arg0.getActiveEffects()).iterator();
        while (var1.hasNext()) {
            Object var2 = ((MobEffectInstance) var1.next());
            if (!(((MobEffect) var2.getEffect().value()).isBeneficial())) return true;
        }
        return false;
    }

    public static Vec3 getRenderPos(Entity arg0, float arg1) {
        double var2 = Mth.lerp(((double) arg1), arg0.xo, arg0.getX());
        double var4 = Mth.lerp(((double) arg1), arg0.yo, arg0.getY());
        double var6 = Mth.lerp(((double) arg1), arg0.zo, arg0.getZ());
        return new Vec3(var2, var4, var6);
    }

    public static LivingEntity getClosestEntity(Entity arg0) {
        Object var1 = null;
        Object var2 = mc.level.entitiesForRendering().iterator();
        while (var2.hasNext()) {
            Object var3 = ((Entity) var2.next());
            if (!((var3 instanceof LivingEntity))) break label_0;
            var tmp0 = ((LivingEntity) var3);
            Object var4 = tmp0;
            if (((arg0.distanceTo(tmp0) > 10.0f) || (var4.getHealth() <= 0.0f)) || (!(var4.isAlive()))) break label_0;
            if (arg0 == var4) {
                break label_0;
            }
            if (var1 == null) {
                var1 = var4;
                break label_0;
            }
            if (arg0.distanceTo(var4) >= arg0.distanceTo(var1)) {
                break label_0;
            }
            var1 = var4;
        }
        return var1;
    }

    public static Direction getPearlDirection(ThrownEnderpearl arg0) {
        Object var1 = arg0.getDirection();
        if (var1.equals(Direction.WEST)) return Direction.EAST;
        if (var1.equals(Direction.EAST)) return Direction.WEST;
        return var1;
    }

    public static boolean isThrowable(Item arg0) {
        if (!(arg0 instanceof EnderpearlItem) && !(arg0 instanceof TridentItem) && !(arg0 instanceof ExperienceBottleItem) && !(arg0 instanceof SnowballItem) && !(arg0 instanceof EggItem) && !(arg0 instanceof SplashPotionItem)) {
            if ((arg0 instanceof LingeringPotionItem)) {
            }
        } else {
        }
        return false;
    }

    public static boolean isInWeb(Entity arg0) {
        var tmp0 = new float[3];
        tmp0[0] = 0.0f;
        tmp0[1] = 0.3f;
        tmp0[2] = -0.3f;
        Object var1 = tmp0;
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            float var4 = var1[var3];
            var tmp1 = new float[3];
            tmp1[0] = 0.0f;
            tmp1[1] = 0.3f;
            tmp1[2] = -0.3f;
            Object var5 = tmp1;
            int var6 = var5.length;
            int var7 = 0;
            while (var7 < var6) {
                float var8 = var5[var7];
                var tmp2 = new int[4];
                tmp2[0] = -1;
                tmp2[1] = 0;
                tmp2[2] = 1;
                tmp2[3] = 2;
                Object var9 = tmp2;
                int var10 = var9.length;
                int var11 = 0;
                while (var11 < var10) {
                    int var12 = var9[var11];
                    Object var13 = BlockPos.containing((arg0.getX() + ((double) var4)), arg0.getY(), (arg0.getZ() + ((double) var8))).above(var12);
                    if (!(new AABB(var13).intersects(arg0.getBoundingBox()))) break label_4;
                    if (mc.level.getBlockState(var13).getBlock() != Blocks.COBWEB) {
                        break label_4;
                    }
                    return true;
                    label_4:
                    var11++;
                }
                var7++;
            }
            var3++;
        }
        return false;
    }

    public static boolean isNeutral(Entity arg0) {
        if (arg0 == null) return false;
        if ((arg0.getType() == EntityTypes.PIGLIN) || (arg0.getType() == EntityTypes.PIGLIN_BRUTE)) {
            return false;
        }
        if ((arg0 instanceof NeutralMob)) return true;
        Object var1 = arg0.getType();
        if (var1 != EntityTypes.ZOMBIFIED_PIGLIN && var1 != EntityTypes.ENDERMAN && var1 != EntityTypes.IRON_GOLEM && var1 != EntityTypes.WOLF && var1 != EntityTypes.POLAR_BEAR && var1 != EntityTypes.BEE && var1 != EntityTypes.DOLPHIN && var1 != EntityTypes.GOAT && var1 != EntityTypes.PANDA && var1 != EntityTypes.LLAMA) {
            if (var1 == EntityTypes.TRADER_LLAMA) {
            }
        } else {
        }
        return false;
    }

    public static boolean isAngry(Entity arg0) {
        if (!((arg0 instanceof Mob))) return false;
        Object var9 = ((Mob) arg0);
        if (var9.isAggressive()) return true;
        Object var10 = var9.getAttribute(Attributes.MOVEMENT_SPEED);
        if (var10 != null && var10.hasModifier(ATTACKING_SPEED_MODIFIER)) return true;
        if ((arg0 instanceof NeutralMob)) {
            var tmp0 = ((NeutralMob) arg0);
            Object var8 = tmp0;
            if (tmp0.isAngry()) return true;
        }
        if ((arg0 instanceof EnderMan)) {
            var tmp1 = ((EnderMan) arg0);
            Object var7 = tmp1;
            if ((tmp1.isCreepy()) || (var7.hasBeenStaredAt())) {
                return true;
            }
        }
        if ((arg0 instanceof PolarBear)) {
            var tmp2 = ((PolarBear) arg0);
            Object var6 = tmp2;
            if (tmp2.isStanding()) return true;
        }
        if ((arg0 instanceof IronGolem)) {
            var tmp3 = ((IronGolem) arg0);
            Object var5 = tmp3;
            if (tmp3.getAttackAnimationTick() > 0) return true;
        }
        if ((arg0 instanceof Bee)) {
            var tmp4 = ((Bee) arg0);
            Object var4 = tmp4;
            if ((tmp4.hasStung()) || (var4.isAngry())) {
                return true;
            }
        }
        if ((arg0 instanceof Panda)) {
            var tmp5 = ((Panda) arg0);
            Object var3 = tmp5;
            if (tmp5.isAggressive()) return true;
        }
        if ((arg0 instanceof Piglin)) {
            var tmp6 = ((Piglin) arg0);
            Object var1 = tmp6;
            var tmp7 = tmp6.getArmPose();
            Object var2 = tmp7;
            if (((tmp7 == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) || (var2 == PiglinArmPose.CROSSBOW_HOLD)) || (var2 == PiglinArmPose.CROSSBOW_CHARGE)) {
                return true;
            }
        }
        if (mc.player != null) {
            Object var11 = mc.player.getLastDamageSource();
            if (var11 != null && var11.getEntity() == arg0) return true;
            Object var12 = var9.getLastDamageSource();
            if (var12 != null && var12.getEntity() == mc.player) return true;
        }
        if (var9.getTarget() != null) return true;
        if (!((arg0 instanceof NeutralMob))) break label_16;
        var tmp8 = ((NeutralMob) arg0);
        var8 = tmp8;
        return tmp8.getPersistentAngerTarget() != null;
    }

    public static boolean isHostile(Entity arg0) {
        if (arg0 == null) return false;
        if (isAngry(arg0)) return true;
        if (isNeutral(arg0)) return false;
        if ((((arg0 instanceof Enemy)) || ((arg0 instanceof Monster))) || ((arg0 instanceof Endermite))) {
            return true;
        }
        if ((arg0.getType() == EntityTypes.ENDERMITE) || (arg0.getType() == EntityTypes.SILVERFISH)) {
            return true;
        }
        return arg0.getType().getCategory() == MobCategory.MONSTER;
    }

    public static boolean isAnimal(Entity arg0) {
        if (((arg0 == null) || (isNeutral(arg0))) || (isHostile(arg0))) {
            return false;
        }
        if ((arg0 instanceof Animal)) return true;
        Object var1 = arg0.getType().getCategory();
        if (var1 != MobCategory.CREATURE && var1 != MobCategory.WATER_CREATURE && var1 != MobCategory.WATER_AMBIENT && var1 != MobCategory.UNDERGROUND_WATER_CREATURE && var1 != MobCategory.AXOLOTLS) {
            if (var1 == MobCategory.AMBIENT) {
            }
        } else {
        }
        return false;
    }

    public static boolean isPassive(Entity arg0) {
        return isAnimal(arg0);
    }

    static {
        POTION_COLORS = new HashMap();
        POTION_COLORS.put(((MobEffect) MobEffects.SPEED.value()), new Color(124, 175, 198));
        POTION_COLORS.put(((MobEffect) MobEffects.SLOWNESS.value()), new Color(90, 108, 129));
        POTION_COLORS.put(((MobEffect) MobEffects.HASTE.value()), new Color(217, 192, 67));
        POTION_COLORS.put(((MobEffect) MobEffects.MINING_FATIGUE.value()), new Color(74, 66, 23));
        POTION_COLORS.put(((MobEffect) MobEffects.STRENGTH.value()), new Color(147, 36, 35));
        POTION_COLORS.put(((MobEffect) MobEffects.INSTANT_HEALTH.value()), new Color(67, 10, 9));
        POTION_COLORS.put(((MobEffect) MobEffects.INSTANT_DAMAGE.value()), new Color(67, 10, 9));
        POTION_COLORS.put(((MobEffect) MobEffects.JUMP_BOOST.value()), new Color(34, 255, 76));
        POTION_COLORS.put(((MobEffect) MobEffects.NAUSEA.value()), new Color(85, 29, 74));
        POTION_COLORS.put(((MobEffect) MobEffects.REGENERATION.value()), new Color(205, 92, 171));
        POTION_COLORS.put(((MobEffect) MobEffects.RESISTANCE.value()), new Color(153, 69, 58));
        POTION_COLORS.put(((MobEffect) MobEffects.FIRE_RESISTANCE.value()), new Color(228, 154, 58));
        POTION_COLORS.put(((MobEffect) MobEffects.WATER_BREATHING.value()), new Color(46, 82, 153));
        POTION_COLORS.put(((MobEffect) MobEffects.INVISIBILITY.value()), new Color(127, 131, 146));
        POTION_COLORS.put(((MobEffect) MobEffects.BLINDNESS.value()), new Color(31, 31, 35));
        POTION_COLORS.put(((MobEffect) MobEffects.NIGHT_VISION.value()), new Color(31, 31, 161));
        POTION_COLORS.put(((MobEffect) MobEffects.HUNGER.value()), new Color(88, 118, 83));
        POTION_COLORS.put(((MobEffect) MobEffects.WEAKNESS.value()), new Color(72, 77, 72));
        POTION_COLORS.put(((MobEffect) MobEffects.POISON.value()), new Color(78, 147, 49));
        POTION_COLORS.put(((MobEffect) MobEffects.WITHER.value()), new Color(53, 42, 39));
        POTION_COLORS.put(((MobEffect) MobEffects.HEALTH_BOOST.value()), new Color(248, 125, 35));
        POTION_COLORS.put(((MobEffect) MobEffects.ABSORPTION.value()), new Color(37, 82, 165));
        POTION_COLORS.put(((MobEffect) MobEffects.SATURATION.value()), new Color(248, 36, 35));
        POTION_COLORS.put(((MobEffect) MobEffects.GLOWING.value()), new Color(148, 160, 97));
        POTION_COLORS.put(((MobEffect) MobEffects.LEVITATION.value()), new Color(206, 255, 255));
        POTION_COLORS.put(((MobEffect) MobEffects.LUCK.value()), new Color(51, 153, 0));
        POTION_COLORS.put(((MobEffect) MobEffects.UNLUCK.value()), new Color(192, 164, 77));
        ATTACKING_SPEED_MODIFIER = Identifier.withDefaultNamespace("attacking");
    }


    class 1 {

        static {
            $SwitchMap$net$minecraft$world$level$GameType = new int[GameType.values().length];
            try {
                $SwitchMap$net$minecraft$world$level$GameType[GameType.CREATIVE.ordinal()] = 1;
            } catch (NoSuchFieldError ex2) {
                Object var0 = ex2;
                try {
                }
                $SwitchMap$net$minecraft$world$level$GameType[GameType.ADVENTURE.ordinal()] = 2;
            } catch (NoSuchFieldError ex5) {
                var0 = ex5;
                try {
                }
                $SwitchMap$net$minecraft$world$level$GameType[GameType.SPECTATOR.ordinal()] = 3;
            } catch (NoSuchFieldError ex8) {
                var0 = ex8;
            }
        }

    }

    public enum SpeedUnit {
        METERS,
        KILOMETERS;


        private SpeedUnit() {
        }

    }
}
