package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Trajectories", description = "Draws a predicted trajectory of where throwables will end up when you throw them.", category = Module.Category.VISUALS)
public class TrajectoriesModule extends Module {
    public ColorSetting lineColor;
    public CategorySetting entitiesCategory;
    public ModeSetting entitiesMode;
    public ColorSetting entitiesFillColor;
    public ColorSetting entitiesOutlineColor;
    public CategorySetting blocksCategory;
    public ModeSetting blocksMode;
    public ColorSetting blocksFillColor;
    public ColorSetting blocksOutlineColor;
    private List hitEntities;

    public TrajectoriesModule() {
        this.lineColor = new ColorSetting("LineColor", "The color of the trajectory line.", ColorUtils.getDefaultOutlineColor());
        this.entitiesCategory = new CategorySetting("Entities", "The rendering that will be applied to entities hit by the trajectory.");
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "Fill";
        tmp0[2] = "Outline";
        tmp0[3] = "Both";
        this.entitiesMode = new ModeSetting("EntitiesMode", "Mode", "The rendering that will be applied to the target entity.", new CategorySetting.Visibility(this.entitiesCategory), "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.entitiesFillColor = new ColorSetting("EntitiesFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.entitiesMode, tmp1), ColorUtils.getDefaultFillColor());
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.entitiesOutlineColor = new ColorSetting("EntitiesOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.entitiesMode, tmp2), ColorUtils.getDefaultOutlineColor());
        this.blocksCategory = new CategorySetting("Blocks", "The rendering that will be applied to blocks hit by the trajectory.");
        var tmp3 = new String[4];
        tmp3[0] = "None";
        tmp3[1] = "Fill";
        tmp3[2] = "Outline";
        tmp3[3] = "Both";
        this.blocksMode = new ModeSetting("BlocksMode", "Mode", "The rendering that will be applied to the target block.", new CategorySetting.Visibility(this.blocksCategory), "Both", tmp3);
        var tmp4 = new String[2];
        tmp4[0] = "Fill";
        tmp4[1] = "Both";
        this.blocksFillColor = new ColorSetting("BlocksFillColor", "FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.blocksMode, tmp4), ColorUtils.getDefaultFillColor());
        var tmp5 = new String[2];
        tmp5[0] = "Outline";
        tmp5[1] = "Both";
        this.blocksOutlineColor = new ColorSetting("BlocksOutlineColor", "OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.blocksMode, tmp5), ColorUtils.getDefaultOutlineColor());
        this.hitEntities = new ArrayList();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (mc.gui.hud.isHidden()) break label_2;
        if (!(mc.options.getCameraType().isFirstPerson())) {
            label_2:
            return;
        }
        if (!(mc.player.getMainHandItem().getItem() instanceof BowItem) && !(mc.player.getMainHandItem().getItem() instanceof CrossbowItem)) {
            if (EntityUtils.isThrowable(mc.player.getMainHandItem().getItem())) {
            }
            Object var2 = InteractionHand.MAIN_HAND;
            break label_8;
        }
        if (!(mc.player.getOffhandItem().getItem() instanceof BowItem) && !(mc.player.getOffhandItem().getItem() instanceof CrossbowItem)) {
            if (EntityUtils.isThrowable(mc.player.getOffhandItem().getItem())) {
            }
            var2 = InteractionHand.OFF_HAND;
            break label_8;
        }
        return;
        label_8:
        this.hitEntities.clear();
        float var3 = Mth.lerp(arg0.getTickDelta(), mc.player.yRotO, mc.player.getYRot());
        if ((mc.player.getOffhandItem().getItem() instanceof CrossbowItem)) {
            if (!(EnchantmentHelper.getItemEnchantmentLevel(mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), mc.player.getOffhandItem()))) {
            }
            if ((mc.player.getMainHandItem().getItem() instanceof CrossbowItem) && EnchantmentHelper.getItemEnchantmentLevel(mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT), mc.player.getMainHandItem())) {
            }
            if (var2 == InteractionHand.OFF_HAND) {
            } else {
            }
            arg0.getMatrices().project(mc.player.getOffhandItem().getItem(), mc.player.getMainHandItem().getItem(), (var3 - 10.0f), arg0.getTickDelta());
            if (var2 == InteractionHand.OFF_HAND) {
            } else {
            }
            arg0.getMatrices().project(mc.player.getOffhandItem().getItem(), mc.player.getMainHandItem().getItem(), var3, arg0.getTickDelta());
            if (var2 == InteractionHand.OFF_HAND) {
            } else {
            }
            arg0.getMatrices().project(mc.player.getOffhandItem().getItem(), mc.player.getMainHandItem().getItem(), (var3 + 10.0f), arg0.getTickDelta());
        } else {
            if (var2 == InteractionHand.OFF_HAND) {
            } else {
            }
            arg0.getMatrices().project(mc.player.getOffhandItem().getItem(), mc.player.getMainHandItem().getItem(), var3, arg0.getTickDelta());
        }
    }

    private void project(PoseStack arg0, Item arg1, float arg2, float arg3) {
        double var5 = Mth.lerp(((double) arg3), mc.player.xo, mc.player.getX());
        double var7 = Mth.lerp(((double) arg3), mc.player.yo, mc.player.getY());
        double var9 = Mth.lerp(((double) arg3), mc.player.zo, mc.player.getZ());
        var7 = ((var7 + ((double) mc.player.getEyeHeight(mc.player.getPose()))) - 0.1000000014901161);
        float var11 = ((arg1 instanceof BowItem) ? 1.0f : 0.4f);
        float var12 = Mth.lerp(arg3, mc.player.xRotO, mc.player.getXRot());
        double var13 = ((double) ((-(Mth.sin(((double) ((arg2 / 180.0f) * 3.1415927f)))) * Mth.cos(((double) ((var12 / 180.0f) * 3.1415927f)))) * var11));
        double var15 = ((double) (-(Mth.sin(((double) (((var12 - ((float) getThrowPitch(arg1))) / 180.0f) * 3.141593f)))) * var11));
        double var17 = ((double) ((Mth.cos(((double) ((arg2 / 180.0f) * 3.1415927f))) * Mth.cos(((double) ((var12 / 180.0f) * 3.1415927f)))) * var11));
        float var19 = (((float) mc.player.getTicksUsingItem()) / 20.0f);
        var tmp0 = (((var19 * var19) + (var19 * 2.0f)) / 3.0f);
        var19 = tmp0;
        if (tmp0 > 1.0f) break label_2;
        if (var19 == 0.0f) {
            label_2:
            var19 = 1.0f;
        }
        float var20 = Mth.sqrt(((float) (((var13 * var13) + (var15 * var15)) + (var17 * var17))));
        var13 = (var13 / ((double) var20));
        var15 = (var15 / ((double) var20));
        var17 = (var17 / ((double) var20));
        if ((arg1 instanceof BowItem)) {
            break label_6;
        }
        float var21 = (((arg1 instanceof CrossbowItem) ? 2.2f : 1.0f) * getThrowVelocity(arg1));
        var13 = (var13 * ((double) var21));
        var15 = (var15 * ((double) var21));
        var17 = (var17 * ((double) var21));
        if (!(mc.player.onGround())) {
            var15 = (var15 + mc.player.getDeltaMovement().y);
        }
        Object var22 = arg0.last().pose();
        int var23 = 0;
        Object var24 = null;
        Object var25 = null;
        while (!var23 && var7 > -65.0) {
            Object var26 = new Vec3(var5, var7, var9);
            var5 = (var5 + var13);
            var7 = (var7 + var15);
            var9 = (var9 + var17);
            if (mc.level.getBlockState(new BlockPos(((int) (var5 + var13)), ((int) (var7 + var15)), ((int) (var9 + var17)))).getBlock() == Blocks.WATER) {
                var13 = (var13 * 0.8);
                var15 = (var15 * 0.8);
                var17 = (var17 * 0.8);
            } else {
                var13 = (var13 * 0.99);
                var15 = (var15 * 0.99);
                var17 = (var17 * 0.99);
            }
            if ((arg1 instanceof BowItem)) {
                var15 = (var15 - 0.05000000074505806);
                break label_13;
            }
            if ((mc.player.getMainHandItem().getItem() instanceof CrossbowItem)) {
                var15 = (var15 - 0.05000000074505806);
                break label_13;
            }
            var15 = (var15 - 0.029999999329447746);
            label_13:
            var15 = (var15 - 0.029999999329447746);
            Object var27 = new Vec3(var5, var7, var9);
            Object var28 = getHitEntity(new AABB((var5 - 1.0), (var7 - 1.0), (var9 - 1.0), (var5 + 1.0), (var7 + 1.0), (var9 + 1.0)));
            Object var29 = mc.level.clip(new ClipContext(var26, var27, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
            if (var28 != null) {
                var25 = var28;
                var23 = 1;
                break label_15;
            }
            if ((var29 == null) || (var29.getType() == HitResult.Type.MISS)) break label_15;
            var24 = var29;
            var23 = 1;
            label_15:
            var tmp1 = new Renderer3D.Vertex[2];
            tmp1[0] = new Renderer3D.Vertex(var22, ((float) (var26.x - mc.gameRenderer.mainCamera().position().x)), ((float) (var26.y - mc.gameRenderer.mainCamera().position().y)), ((float) (var26.z - mc.gameRenderer.mainCamera().position().z)), this.lineColor.getColor().getRGB());
            tmp1[1] = new Renderer3D.Vertex(var22, ((float) (var27.x - mc.gameRenderer.mainCamera().position().x)), ((float) (var27.y - mc.gameRenderer.mainCamera().position().y)), ((float) (var27.z - mc.gameRenderer.mainCamera().position().z)), this.lineColor.getColor().getRGB());
            Renderer3D.DEBUG_LINES.add(new Renderer3D.VertexCollection(tmp1));
        }
        if ((var24 == null) || (var24.getType() != HitResult.Type.BLOCK)) break label_20;
        var26 = new AABB((var24.getLocation().x - 0.15), (var24.getLocation().y - 0.15), (var24.getLocation().z - 0.15), (var24.getLocation().x + 0.15), (var24.getLocation().y + 0.15), (var24.getLocation().z + 0.15));
        if (!(this.blocksMode.getValue().equalsIgnoreCase("Fill"))) {
            if (this.blocksMode.getValue().equalsIgnoreCase("Both")) {
            }
            Renderer3D.renderBox(arg0, var26, this.blocksFillColor.getColor());
        }
        if (!(this.blocksMode.getValue().equalsIgnoreCase("Outline"))) {
            if (!(this.blocksMode.getValue().equalsIgnoreCase("Both"))) break label_20;
        }
        Renderer3D.renderBoxOutline(arg0, var26, this.blocksOutlineColor.getColor());
        label_20:
        if (var25 != null && !(this.hitEntities.contains(var25))) {
            if (!(this.entitiesMode.getValue().equalsIgnoreCase("Fill"))) {
                if (this.entitiesMode.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBox(arg0, var25.getBoundingBox(), this.entitiesFillColor.getColor());
            }
            if (!(this.entitiesMode.getValue().equalsIgnoreCase("Outline"))) {
                if (this.entitiesMode.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderBoxOutline(arg0, var25.getBoundingBox(), this.entitiesOutlineColor.getColor());
            }
            this.hitEntities.add(var25);
        }
    }

    private Entity getHitEntity(AABB arg0) {
        Object var2 = mc.level.entitiesForRendering().iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Entity) var2.next());
            if ((var3 == mc.player) || ((var3 instanceof Arrow))) break label_0;
            if (!(var3.getBoundingBox().intersects(arg0))) {
                break label_0;
            }
            return var3;
        }
        return null;
    }

    private float getThrowVelocity(Item arg0) {
        if (((arg0 instanceof SplashPotionItem)) || ((arg0 instanceof LingeringPotionItem))) {
            return 0.5f;
        }
        if ((arg0 instanceof ExperienceBottleItem)) return 0.59f;
        if ((arg0 instanceof TridentItem)) return 2.0f;
        return 1.5f;
    }

    private int getThrowPitch(Item arg0) {
        if ((((arg0 instanceof SplashPotionItem)) || ((arg0 instanceof LingeringPotionItem))) || ((arg0 instanceof ExperienceBottleItem))) {
            return 20;
        }
        return 0;
    }

}
