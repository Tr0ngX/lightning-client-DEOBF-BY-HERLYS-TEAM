package lightning.modules.impl.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientConnectEvent;
import lightning.events.impl.ClientDisconnectEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.ServerConnectEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "Trails", description = "Renders smooth trails behind projectiles and throwables.", category = Module.Category.VISUALS)
public class TrailsModule extends Module {
    public CategorySetting targets;
    public BooleanSetting pearls;
    public BooleanSetting arrows;
    public BooleanSetting witherSkull;
    public BooleanSetting egg;
    public BooleanSetting snowball;
    public BooleanSetting fireCharge;
    public NumberSetting fade;
    public ColorSetting color;
    private final Map trails;

    public TrailsModule() {
        this.targets = new CategorySetting("Targets", "The projectiles to render trails for.");
        this.pearls = new BooleanSetting("Pearls", "Renders trails behind ender pearls.", new CategorySetting.Visibility(this.targets), true);
        this.arrows = new BooleanSetting("Arrows", "Renders trails behind arrows.", new CategorySetting.Visibility(this.targets), true);
        this.witherSkull = new BooleanSetting("WitherSkull", "Renders trails behind wither skulls.", new CategorySetting.Visibility(this.targets), true);
        this.egg = new BooleanSetting("Egg", "Renders trails behind eggs.", new CategorySetting.Visibility(this.targets), true);
        this.snowball = new BooleanSetting("Snowball", "Renders trails behind snowballs.", new CategorySetting.Visibility(this.targets), true);
        this.fireCharge = new BooleanSetting("FireCharge", "Renders trails behind fire charges.", new CategorySetting.Visibility(this.targets), true);
        this.fade = new NumberSetting("Fade", "The duration in milliseconds before a trail segment fades away.", 1000, 100, 5000);
        this.color = new ColorSetting("Color", "The color used for the trails.", ColorUtils.getDefaultOutlineColor());
        this.trails = new ConcurrentHashMap();
    }

    @SubscribeEvent
    public void onTick(TickEvent arg0) {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        long var2 = System.currentTimeMillis();
        Object var4 = mc.level.entitiesForRendering().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Entity) var4.next());
            if (!(isValidTarget(var5))) break label_2;
            if (!(var5.isAlive())) {
                break label_2;
            }
            Object var7 = var5.getUUID();
            Object var8 = var5.position().add(0.0, (((double) var5.getBbHeight()) * 0.5), 0.0);
            var tmp0 = ((List) this.trails.computeIfAbsent(var7, (arg0) -> new ArrayList()));
            Object var6 = tmp0;
            Object var9 = tmp0;
            Object var10 = var9;
            // synchronized (var9)
            if (!(var6.isEmpty())) break label_6;
            if (!((var5 instanceof Projectile))) break label_5;
            var tmp1 = ((Projectile) var5);
            Object var11 = tmp1;
            var tmp2 = tmp1.getOwner();
            Object var12 = tmp2;
            if (!((tmp2 instanceof LivingEntity))) break label_5;
            Object var13 = ((LivingEntity) var12);
            var6.add(new TrailsModule.TrailPoint(var13.getEyePosition().subtract(0.0, 0.1, 0.0), var2));
            break label_6;
            label_5:
            if ((((double) var5.distanceTo(mc.player)) > 3.5) || (!(mc.player.isAlive()))) break label_6;
            var6.add(new TrailsModule.TrailPoint(mc.player.getEyePosition().subtract(0.0, 0.1, 0.0), var2));
            label_6:
            if (!(var6.isEmpty())) {
                if (((TrailsModule.TrailPoint) var6.get((var6.size() - 1))).pos().distanceToSqr(var8) > 1.0E-4) {
                }
                var6.add(new TrailsModule.TrailPoint(var8, var2));
            }
            break label_12;
            Object var14 = null;
            throw var14;
            label_12:
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent arg0) {
        if (((mc.level == null) || (mc.player == null)) || (this.trails.isEmpty())) {
            return;
        }
        long var2 = System.currentTimeMillis();
        long var4 = this.fade.getValue().longValue();
        Object var6 = arg0.getMatrices();
        Object var7 = this.color.getColor();
        Object var8 = new HashMap();
        Object var9 = mc.level.entitiesForRendering().iterator();
        while (var9.hasNext()) {
            Object var10 = ((Entity) var9.next());
            if (!(var10.isAlive())) {
                break label_2;
            }
            var8.put(var10.getUUID(), var10);
        }
        var9 = this.trails.entrySet().iterator();
        while (var9.hasNext()) {
            var10 = ((Map.Entry) var9.next());
            Object var11 = ((UUID) var10.getKey());
            Object var12 = ((List) var10.getValue());
            Object var13 = ((Entity) var8.get(var11));
            Object var14 = var12;
            Object var15 = var14;
            // synchronized (var14)
            var12.removeIf((arg2) -> (arg0 - arg2.time()) > arg1);
            if (var12.isEmpty() && var13 == null) {
                var9.remove();
                break label_5;
            }
            if (var12.size() < 1 && var13 == null) {
                break label_5;
            }
            int var16 = 0;
            while (var16 < (var12.size() - 1)) {
                Object var17 = ((TrailsModule.TrailPoint) var12.get(var16));
                Object var18 = ((TrailsModule.TrailPoint) var12.get((var16 + 1)));
                if (var4 > 0L) {
                } else {
                }
                float var19 = 1.0f;
                if (var4 > 0L) {
                } else {
                }
                float var20 = 1.0f;
                Object var21 = ColorUtils.getColor(var7, Math.round((((float) var7.getAlpha()) * var19)));
                Object var22 = ColorUtils.getColor(var7, Math.round((((float) var7.getAlpha()) * var20)));
                Renderer3D.renderLine(var6, var17.pos(), var18.pos(), var21, var22);
                var16++;
            }
            if (var13 != null && !(var12.isEmpty())) {
                var16 = ((TrailsModule.TrailPoint) var12.get((var12.size() - 1)));
                var17 = EntityUtils.getRenderPos(var13, arg0.getTickDelta()).add(0.0, (((double) var13.getBbHeight()) * 0.5), 0.0);
                if (var4 > 0L) {
                } else {
                }
                var18 = 1.0f;
                var19 = ColorUtils.getColor(var7, Math.round((((float) var7.getAlpha()) * var18)));
                var20 = ColorUtils.getColor(var7, var7.getAlpha());
                Renderer3D.renderLine(var6, var16.pos(), var17, var19, var20);
            }
            break label_23;
            Object var23 = Mth.clamp((1.0f - (((float) (var2 - var16.time())) / ((float) var4))), 0.0f, 1.0f);
            throw var23;
            label_23:
        }
    }

    private boolean isValidTarget(Entity arg0) {
        if (!(this.pearls.getValue())) break label_1;
        if (((arg0 instanceof ThrownEnderpearl)) || (arg0.getType() == EntityTypes.ENDER_PEARL)) {
            return true;
        }
        label_1:
        if (this.arrows.getValue()) {
            if ((((arg0 instanceof AbstractArrow)) || (arg0.getType() == EntityTypes.ARROW)) || (arg0.getType() == EntityTypes.SPECTRAL_ARROW)) {
                return true;
            }
        }
        if (this.witherSkull.getValue()) {
            if (((arg0 instanceof WitherSkull)) || (arg0.getType() == EntityTypes.WITHER_SKULL)) {
                return true;
            }
        }
        if (this.egg.getValue()) {
            if (((arg0 instanceof ThrownEgg)) || (arg0.getType() == EntityTypes.EGG)) {
                return true;
            }
        }
        if (this.snowball.getValue()) {
            if (((arg0 instanceof Snowball)) || (arg0.getType() == EntityTypes.SNOWBALL)) {
                return true;
            }
        }
        if (!(this.fireCharge.getValue())) break label_11;
        if (!(arg0 instanceof SmallFireball) && !(arg0 instanceof LargeFireball) && !(arg0 instanceof Fireball) && !(arg0 instanceof DragonFireball) && arg0.getType() != EntityTypes.SMALL_FIREBALL && arg0.getType() != EntityTypes.FIREBALL) {
            if (arg0.getType() != EntityTypes.DRAGON_FIREBALL) break label_11;
        }
        break label_12;
        label_11:
        label_12:
        return false;
    }

    public void onDisable() {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectEvent arg0) {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onConnect(ClientConnectEvent arg0) {
        this.trails.clear();
    }

    @SubscribeEvent
    public void onServerConnect(ServerConnectEvent arg0) {
        this.trails.clear();
    }


    public record TrailPoint(Vec3 pos, long time) {

        public TrailPoint(Vec3 arg0, long arg1) {
            this.pos = arg0;
            this.time = arg1;
        }

        public Vec3 pos() {
            return this.pos;
        }

        public long time() {
            return this.time;
        }

    }
}
