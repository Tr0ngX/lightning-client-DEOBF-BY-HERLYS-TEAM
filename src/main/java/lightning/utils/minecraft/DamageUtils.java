package lightning.utils.minecraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lightning.utils.IMinecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DamageUtils implements IMinecraft {
    private static final Map PROTECTION_MAP;

    public static float getCrystalDamage(Entity arg0, AABB arg1, EndCrystal arg2, boolean arg3) {
        if (arg2 == null) return 0.0f;
        return getDamage(arg0, arg1, arg2.position(), 6.0f, null, arg3);
    }

    public static float getCrystalDamage(Entity arg0, AABB arg1, BlockPos arg2, BlockPos arg3, boolean arg4) {
        if (arg2 == null) return 0.0f;
        return getDamage(arg0, arg1, new Vec3((((double) arg2.getX()) + 0.5), ((double) (arg2.getY() + 1)), (((double) arg2.getZ()) + 0.5)), 6.0f, arg3, arg4);
    }

    public static float getDamage(Entity arg0, AABB arg1, Vec3 arg2, float arg3, BlockPos arg4, boolean arg5) {
        if (mc.level.getDifficulty() == Difficulty.PEACEFUL) return 0.0f;
        if (((arg0 instanceof RemotePlayer)) || (!((arg0 instanceof Player)))) break label_1;
        var tmp0 = ((Player) arg0);
        Object var6 = tmp0;
        if (EntityUtils.getGameMode(tmp0) == GameType.CREATIVE) return 0.0f;
        label_1:
        float var7 = (arg3 * 2.0f);
        if (arg1 != null) {
        } else {
        }
        Object var8 = arg0.getBoundingBox();
        Object var9 = new Vec3(var8.getCenter().x, var8.minY, var8.getCenter().z);
        double var10 = (Math.sqrt(var9.distanceToSqr(arg2)) / ((double) var7));
        if (var10 > 1.0) return 0.0f;
        int var12 = (arg0 == mc.player ? 0 : arg5);
        double var13 = ((1.0 - var10) * ((double) getExposure(arg2, var8, arg4, var12)));
        float var15 = ((float) ((int) ((((((var13 * var13) + var13) / 2.0) * 7.0) * ((double) var7)) + 1.0)));
        if (var15 <= 0.0f) return 0.0f;
        if ((arg0 instanceof LivingEntity)) {
            Object var16 = ((LivingEntity) arg0);
            if (mc.level.getDifficulty() == Difficulty.EASY) {
                break label_10;
            }
            if (mc.level.getDifficulty() == Difficulty.HARD) {
                break label_10;
            }
            label_10:
            var15 = var15;
            var15 = CombatRules.getDamageAfterAbsorb(var16, var15, mc.level.damageSources().explosion(null), ((float) var16.getArmorValue()), ((float) var16.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()));
            if (var16.hasEffect(MobEffects.RESISTANCE)) {
            } else {
            }
            var15 = ((float) ((1.0 - (((double) (var16.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1)) * 0.2)) * 1.0));
            var15 = CombatRules.getDamageAfterMagicAbsorb(var15, ((float) getProtectionAmount(List.of(var16.getItemBySlot(EquipmentSlot.FEET), var16.getItemBySlot(EquipmentSlot.LEGS), var16.getItemBySlot(EquipmentSlot.CHEST), var16.getItemBySlot(EquipmentSlot.HEAD)))));
        }
        return Math.max(var15, 0.0f);
    }

    public static int getProtectionAmount(Iterable arg0) {
        int var1 = 0;
        Object var2 = arg0.iterator();
        while (var2.hasNext()) {
            Object var3 = ((ItemStack) var2.next());
            var1 = (var1 + getProtectionAmount(var3));
        }
        return var1;
    }

    public static int getProtectionAmount(ItemStack arg0) {
        int var1 = 0;
        Object var2 = arg0.getEnchantments();
        Object var3 = var2.keySet().iterator();
        label_0:
        if (!(var3.hasNext())) break label_2;
        Object var4 = ((Holder) var3.next());
        Object var5 = var4.getRegisteredName().replace("minecraft:", "");
        if (!(PROTECTION_MAP.containsKey(var5))) {
            break label_0;
        }
        var1 = (var1 + (var2.getLevel(var4) * ((Integer) PROTECTION_MAP.get(var5)).intValue()));
        break label_2;
        label_2:
        return var1;
    }

    private static float getExposure(Vec3 arg0, AABB arg1, BlockPos arg2, boolean arg3) {
        int var4 = 0;
        int var5 = 0;
        double var6 = 0.0;
        while (var6 <= 1.0) {
            double var8 = 0.0;
            while (var8 <= 1.0) {
                double var10 = 0.0;
                while (var10 <= 1.0) {
                    Object var12 = new Vec3((Mth.lerp(var6, arg1.minX, arg1.maxX) + 0.045454555306552624), Mth.lerp(var8, arg1.minY, arg1.maxY), (Mth.lerp(var10, arg1.minZ, arg1.maxZ) + 0.045454555306552624));
                    if (raycast(var12, arg0, arg2, arg3) == HitResult.Type.MISS) {
                        var4++;
                    }
                    var5++;
                    var10 = (var10 + 0.4545454446934474);
                }
                var8 = (var8 + 0.21739130885479366);
            }
            var6 = (var6 + 0.4545454446934474);
        }
        return (((float) var4) / ((float) var5));
    }

    private static HitResult.Type raycast(Vec3 arg0, Vec3 arg1, BlockPos arg2, boolean arg3) {
        if (mc.level == null) return HitResult.Type.MISS;
        return ((HitResult.Type) BlockGetter.traverseBlocks(arg0, arg1, null, (arg4, arg5) -> {
    if (arg5.equals(arg0)) {
        Object var6 = Blocks.AIR.defaultBlockState();
        break label_1;
    }
    var6 = mc.level.getBlockState(arg5);
    if ((var6.getBlock().getExplosionResistance() >= 600.0f) || (!arg1)) break label_1;
    var6 = Blocks.AIR.defaultBlockState();
    label_1:
    Object var7 = var6.getCollisionShape(mc.level, arg5).clip(arg2, arg3, arg5);
    if (var7 == null) {
    } else {
    }
    return var7.getType();
}, (arg0) -> HitResult.Type.MISS));
    }

    static {
        PROTECTION_MAP = new DamageUtils.1();
    }


    class 1 extends HashMap {
        1() {
            put("protection", 1);
            put("blast_protection", 2);
            put("projectile_protection", 1);
            put("feather_falling", 1);
            put("fire_protection", 1);
        }

    }
}
