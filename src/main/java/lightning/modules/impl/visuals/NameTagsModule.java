package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lightning.LightningClient;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "NameTags", description = "Replaces the default Minecraft NameTag with a more visible and customizable one.", category = Module.Category.VISUALS)
public class NameTagsModule extends Module {
    private static final Identifier LOGO_ID;
    public BooleanSetting gameMode;
    public BooleanSetting ping;
    public BooleanSetting entityId;
    public BooleanSetting health;
    public BooleanSetting totemPops;
    public BooleanSetting antiBot;
    public BooleanSetting nightCheck;
    public BooleanSetting self;
    public BooleanSetting items;
    public BooleanSetting enchantments;
    public BooleanSetting durability;
    public BooleanSetting itemName;
    public BooleanSetting glow;
    public NumberSetting scale;
    public ModeSetting border;
    public ColorSetting fillColor;
    public ColorSetting outlineColor;
    private static final Map ENCHANT_CACHE;
    private static final Map NAME_CACHE;

    public NameTagsModule() {
        this.gameMode = new BooleanSetting("GameMode", "Renders the player's gamemode.", false);
        this.ping = new BooleanSetting("Ping", "Renders the player's latency to the server.", true);
        this.entityId = new BooleanSetting("EntityID", "Renders the player's entity ID.", false);
        this.health = new BooleanSetting("Health", "Renders the player's health and absorption.", true);
        this.totemPops = new BooleanSetting("TotemPops", "Renders the amount of totems that the player has popped.", true);
        this.antiBot = new BooleanSetting("AntiBot", "Prevents bots from having nametags rendered for them.", false);
        this.nightCheck = new BooleanSetting("LightningCheck", "Adds an indicator next to the name of other Lightning users.", true);
        this.self = new BooleanSetting("Self", "Also renders a nametag above your own player.", false);
        this.items = new BooleanSetting("Items", "Renders the items that the player is wearing or holding.", true);
        this.enchantments = new BooleanSetting("Enchantments", "Renders the enchantments of the player's items.", false);
        this.durability = new BooleanSetting("Durability", "Renders the durability of the player's items.", true);
        this.itemName = new BooleanSetting("ItemName", "Renders the name of the item that the player is currently holding.", true);
        this.glow = new BooleanSetting("Glow", "Renders a glow shader effect around nametag text.", false);
        this.scale = new NumberSetting("Scale", "The scaling that will be applied to the nametag rendering.", 30, 10, 100);
        var tmp0 = new String[4];
        tmp0[0] = "None";
        tmp0[1] = "Fill";
        tmp0[2] = "Outline";
        tmp0[3] = "Both";
        this.border = new ModeSetting("Border", "The border that will surround the text.", "Both", tmp0);
        var tmp1 = new String[2];
        tmp1[0] = "Fill";
        tmp1[1] = "Both";
        this.fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.border, tmp1), new ColorSetting.Color(new Color(0, 0, 0, 100), false, false));
        var tmp2 = new String[2];
        tmp2[0] = "Outline";
        tmp2[1] = "Both";
        this.outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.border, tmp2), new ColorSetting.Color(new Color(0, 0, 0, 100), false, false));
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent.Post arg0) {
        if ((mc.level == null) || (mc.player == null)) {
            return;
        }
        Object var2 = arg0.getMatrices();
        Object var3 = mc.level.players().stream().sorted(Comparator.comparing((arg0) -> -(mc.player.distanceTo(arg0)))).toList().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Player) var3.next());
            int var5 = ((IFreecamModule) LightningClient.MODULE_MANAGER.getModule("Freecam")).isToggled();
            if (var4 != mc.player) break label_3;
            if (!(this.self.getValue())) break label_2;
            if (!(mc.options.getCameraType().isFirstPerson())) break label_3;
            if (!var5) break label_2;
            label_3:
            if ((!(this.antiBot.getValue())) || (!(EntityUtils.isBot(var4)))) break label_4;
            break label_2;
            label_4:
            if (!((var4 instanceof RemotePlayer))) break label_6;
            Object var6 = ((RemotePlayer) var4);
            if (LightningClient.MODULE_MANAGER.getModule(PopChamsModule.class) == null) break label_5;
            if (((PopChamsModule) LightningClient.MODULE_MANAGER.getModule(PopChamsModule.class)).isGhost(var6)) break label_2;
            label_5:
            if ((LightningClient.MODULE_MANAGER.getModule(LogoutSpotModule.class) == null) || (!(((LogoutSpotModule) LightningClient.MODULE_MANAGER.getModule(LogoutSpotModule.class)).isGhost(var6)))) break label_6;
            break label_2;
            label_6:
            if (!(Renderer3D.isFrustumVisible(var4.getBoundingBox()))) {
                break label_2;
            }
            var6 = Mth.lerp(((double) arg0.getTickDelta()), var4.xo, var4.getX());
            double var8 = (Mth.lerp(((double) arg0.getTickDelta()), var4.yo, var4.getY()) + ((double) (var4.isShiftKeyDown() ? 1.9f : 2.1f)));
            double var10 = Mth.lerp(((double) arg0.getTickDelta()), var4.zo, var4.getZ());
            Object var12 = new Vec3((var6 - mc.gameRenderer.mainCamera().position().x), (var8 - mc.gameRenderer.mainCamera().position().y), (var10 - mc.gameRenderer.mainCamera().position().z));
            float var13 = ((float) Math.sqrt(mc.gameRenderer.mainCamera().position().distanceToSqr(var6, var8, var10)));
            float var14 = (0.0018f + ((((float) this.scale.getValue().intValue()) / 10000.0f) * var13));
            if (((double) var13) <= 8.0) {
                var14 = 0.0245f;
            }
            var2.pushPose();
            var2.translate(var12.x, var12.y, var12.z);
            var2.mulPose(mc.gameRenderer.mainCamera().rotation());
            var2.scale(var14, -(var14), var14);
            if ((!(this.nightCheck.getValue())) || (LightningClient.NIGHT_USER_MANAGER == null)) break label_11;
            int var15 = (LightningClient.NIGHT_USER_MANAGER.isNightUser(var4) ? 1 : 0);
            Object var16 = var4.getName().getString();
            if (this.gameMode.getValue()) {
                var16 = (((String) var16) + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(var4)) + "]");
            }
            if (this.ping.getValue()) {
                var16 = (((String) var16) + " " + EntityUtils.getLatency(var4) + "ms");
            }
            if (this.entityId.getValue()) {
                var16 = (((String) var16) + " " + var4.getId());
            }
            if (this.health.getValue()) {
                int var17 = Math.round((var4.getHealth() + var4.getAbsorptionAmount()));
                var16 = (((String) var16) + " " + String.valueOf(ColorUtils.getHealthColor(((double) (var4.getHealth() + var4.getAbsorptionAmount())))) + var17 + String.valueOf(ChatFormatting.RESET));
            }
            var17 = ((Integer) LightningClient.WORLD_MANAGER.getPoppedTotems().getOrDefault(var4.getUUID(), 0)).intValue();
            if (this.totemPops.getValue() && var17 > 0) {
                var16 = (((String) var16) + " " + String.valueOf(ColorUtils.getTotemColor(var17)) + "-" + var17);
            }
            int var18 = LightningClient.FONT_MANAGER.getWidth(((String) var16));
            int var19 = LightningClient.FONT_MANAGER.getHeight();
            if (!(!var15)) {
            } else {
            }
            int var20 = 0;
            int var21 = (!(!var15) ? 3 : 0);
            int var22 = ((var18 + var20) + var21);
            if (!(this.border.getValue().equalsIgnoreCase("Fill"))) {
                if (this.border.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderQuad(var2, ((((float) -(var22)) / 2.0f) - 2.0f), ((float) (-(var19) - 1)), ((((float) var22) / 2.0f) + 2.0f), 1.0f, this.fillColor.getColor());
            }
            if (!(this.border.getValue().equalsIgnoreCase("Outline"))) {
                if (this.border.getValue().equalsIgnoreCase("Both")) {
                }
                Renderer3D.renderOutline(var2, ((((float) -(var22)) / 2.0f) - 2.0f), ((float) (-(var19) - 1)), ((((float) var22) / 2.0f) + 2.0f), 1.0f, this.outlineColor.getColor());
            }
            float var23 = (((float) -(var22)) / 2.0f);
            if (!(!var15)) {
                Renderer3D.renderTexture(var2, LOGO_ID, var23, (((float) -(var19)) + 0.5f), (var23 + ((float) var20)), 0.5f, -1);
                var23 = (var23 + ((float) (var20 + var21)));
            }
            if (((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class)).isToggled() && ((FakePlayerModule) LightningClient.MODULE_MANAGER.getModule(FakePlayerModule.class)).getPlayer() == var4) {
                break label_30;
            }
            if (var4.isShiftKeyDown()) {
                break label_30;
            }
            if (LightningClient.FRIEND_MANAGER.contains(var4.getName().getString())) {
                break label_30;
            }
            label_30:
            Object var24 = Color.WHITE;
            drawText(var2, ((String) var16), ((float) ((int) var23)), ((float) -(var19)), var24);
            float var25 = ((float) (-(var19) - 2));
            if (!(this.items.getValue())) break label_50;
            Object var26 = new ItemStack[6];
            Object var27 = new List[6];
            int var28 = 0;
            int var29 = 0;
            int var30 = 0;
            while (var30 < 6) {
                var tmp0 = getItem(var4, var30);
                Object var32 = tmp0;
                var26[var30] = tmp0;
                if (var32.isEmpty()) {
                    break label_35;
                }
                var29 = 1;
                if (!(this.enchantments.getValue())) {
                    break label_35;
                }
                var tmp1 = getCachedEnchantments(var32);
                Object var31 = tmp1;
                var27[var30] = tmp1;
                if (var31.size() <= var28) {
                    break label_35;
                }
                var28 = var31.size();
                label_35:
                var30++;
            }
            if (!var29) break label_50;
            float var34 = (((float) var28) * 4.2f);
            float var35 = Math.max(16.0f, var34);
            var tmp2 = ((((float) -(var19)) - 4.0f) - var35);
            float var33 = tmp2;
            var25 = tmp2;
            var32 = 0;
            while (var32 < 6) {
                var31 = var26[var32];
                if (var31 == null) break label_39;
                if (var31.isEmpty()) {
                    break label_39;
                }
                var30 = ((-54 + (var32 * 18)) + 1);
                var2.pushPose();
                var2.translate(((float) (var30 + 8)), (var33 + 8.0f), -0.5f);
                var2.scale(16.0f, -16.0f, 1.0E-4f);
                Renderer3D.renderItem(var2, var31, var4);
                var2.popPose();
                label_39:
                var32++;
            }
            var32 = 0;
            while (var32 < 6) {
                var31 = var26[var32];
                if (var31 == null) break label_48;
                if (var31.isEmpty()) {
                    break label_48;
                }
                var30 = ((-54 + (var32 * 18)) + 1);
                if (this.durability.getValue() && var31.isDamageableItem()) {
                    float var36 = (((float) (var31.getMaxDamage() - var31.getDamageValue())) / ((float) var31.getMaxDamage()));
                    float var37 = (1.0f - var36);
                    Object var38 = (Math.round(((((float) (var31.getMaxDamage() - var31.getDamageValue())) * 100.0f) / ((float) var31.getMaxDamage()))) + "%");
                    float var39 = (((float) LightningClient.FONT_MANAGER.getWidth(var38)) * 0.5f);
                    var2.pushPose();
                    var2.translate((((float) (var30 + 8)) - (var39 / 2.0f)), (var33 - 5.5f), 1.0f);
                    var2.scale(0.5f, 0.5f, 1.0f);
                    drawText(var2, var38, 0.0f, 0.0f, new Color(var37, var36, 0.0f));
                    var2.popPose();
                }
                if (var31.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                    var2.pushPose();
                    var2.translate(((float) (var30 + 1)), (var33 + 1.0f), 1.0f);
                    var2.scale(0.5f, 0.5f, 1.0f);
                    drawText(var2, "God", 0.0f, 0.0f, new Color(255, 125, 255));
                    var2.popPose();
                }
                if (var31.getCount() != 1) {
                    var36 = String.valueOf(var31.getCount());
                    var37 = ((float) LightningClient.FONT_MANAGER.getWidth(var36));
                    var2.pushPose();
                    var2.translate((((float) (var30 + 16)) - var37), (var33 + 7.0f), 1.0f);
                    var2.scale(1.0f, 1.0f, 1.0f);
                    drawText(var2, var36, 0.0f, 0.0f, Color.WHITE);
                    var2.popPose();
                }
                if ((!(this.enchantments.getValue())) || (var27[var32] == null)) break label_48;
                if (var27[var32].isEmpty()) {
                    break label_48;
                }
                var36 = var27[var32];
                var37 = 4.2f;
                var38 = 0;
                while (var38 < var36.size()) {
                    var39 = ((String) var36.get(var38));
                    float var40 = ((var33 + 8.0f) + (((float) var38) * var37));
                    var2.pushPose();
                    var2.translate(((float) (var30 + 1)), var40, 1.0f);
                    var2.scale(0.5f, 0.5f, 1.0f);
                    drawText(var2, var39, 0.0f, 0.0f, Color.WHITE);
                    var2.popPose();
                    var38++;
                }
                label_48:
                var32++;
            }
            if (!(this.durability.getValue())) break label_50;
            var25 = (var33 - 6.0f);
            label_50:
            if (this.itemName.getValue() && !(var4.getMainHandItem().isEmpty())) {
                var26 = var4.getMainHandItem().getHoverName().getString();
                var27 = (((float) LightningClient.FONT_MANAGER.getWidth(var26)) * 0.5f);
                var28 = ((var25 - (((float) var19) * 0.5f)) - 2.0f);
                var2.pushPose();
                var2.translate((-(var27) / 2.0f), var28, 1.0f);
                var2.scale(0.5f, 0.5f, 1.0f);
                drawText(var2, var26, 0.0f, 0.0f, Color.WHITE);
                var2.popPose();
            }
            var2.popPose();
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
        }
    }

    private void drawText(PoseStack arg0, String arg1, float arg2, float arg3, Color arg4) {
        if (this.glow.getValue()) {
            LightningClient.FONT_MANAGER.drawText(arg0, arg1, arg2, arg3, arg4, true);
        } else {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, arg1, arg2, arg3, arg4, false);
        }
    }

    private List getCachedEnchantments(ItemStack arg0) {
        if (arg0 != null && !(arg0.isEmpty())) {
            Object var2 = ((ItemEnchantments) arg0.get(DataComponents.ENCHANTMENTS));
            if (var2 != null && !(var2.isEmpty())) {
            } else {
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private static String parseShortName(Holder arg0) {
        Object var2 = arg0.getRegisteredName();
        int var3 = var2.indexOf(58);
        if (var3 != -1) {
            break label_1;
        }
        label_1:
        Object var1 = var2;
        Object var4 = var2;
        if (!(var1.startsWith("protection"))) break label_2;
        break label_42;
        label_2:
        if (!(var1.startsWith("blast_protection"))) break label_3;
        break label_42;
        label_3:
        if (!(var1.startsWith("fire_protection"))) break label_4;
        break label_42;
        label_4:
        if (var1.startsWith("projectile_protection")) {
            break label_42;
        }
        if (var1.startsWith("unbreaking")) {
            break label_42;
        }
        if (var1.startsWith("mending")) {
            break label_42;
        }
        if (var1.startsWith("thorns")) {
            break label_42;
        }
        if (var1.startsWith("respiration")) {
            break label_42;
        }
        if (var1.startsWith("aqua_affinity")) {
            break label_42;
        }
        if (var1.startsWith("depth_strider")) {
            break label_42;
        }
        if (var1.startsWith("feather_falling")) {
            break label_42;
        }
        if (var1.startsWith("soul_speed")) {
            break label_42;
        }
        if (var1.startsWith("swift_sneak")) {
            break label_42;
        }
        if (var1.startsWith("sharpness")) {
            break label_42;
        }
        if (var1.startsWith("smite")) {
            break label_42;
        }
        if (var1.startsWith("bane_of_arthropods")) {
            break label_42;
        }
        if (var1.startsWith("knockback")) {
            break label_42;
        }
        if (var1.startsWith("fire_aspect")) {
            break label_42;
        }
        if (var1.startsWith("looting")) {
            break label_42;
        }
        if (var1.startsWith("sweeping_edge")) {
            break label_42;
        }
        if (var1.startsWith("efficiency")) {
            break label_42;
        }
        if (var1.startsWith("silk_touch")) {
            break label_42;
        }
        if (var1.startsWith("fortune")) {
            break label_42;
        }
        if (var1.startsWith("power")) {
            break label_42;
        }
        if (var1.startsWith("punch")) {
            break label_42;
        }
        if (var1.startsWith("flame")) {
            break label_42;
        }
        if (var1.startsWith("infinity")) {
            break label_42;
        }
        if (var1.startsWith("loyalty")) {
            break label_42;
        }
        if (var1.startsWith("impaling")) {
            break label_42;
        }
        if (var1.startsWith("riptide")) {
            break label_42;
        }
        if (var1.startsWith("channeling")) {
            break label_42;
        }
        if (var1.startsWith("multishot")) {
            break label_42;
        }
        if (var1.startsWith("quick_charge")) {
            break label_42;
        }
        if (var1.startsWith("piercing")) {
            break label_42;
        }
        if (var1.startsWith("density")) {
            break label_42;
        }
        if (var1.startsWith("breach")) {
            break label_42;
        }
        if (var1.startsWith("wind_burst")) {
            break label_42;
        }
        if (var1.startsWith("vanishing_curse")) {
            break label_42;
        }
        if (var1.startsWith("binding_curse")) {
            break label_42;
        }
        if (var1.length() > 3) {
            break label_42;
        }
        label_42:
        Object var5 = var1;
        return (var5.substring(0, 1).toUpperCase() + var5.substring(1));
    }

    private ItemStack getItem(Player arg0, int arg1) {
        switch (arg1) {
            case 0 -> { break label_0; }
            case 1 -> { break label_1; }
            case 2 -> { break label_2; }
            case 3 -> { break label_3; }
            case 4 -> { break label_4; }
            case 5 -> { break label_5; }
            default -> { break label_6; }
        }
        label_0:
        break label_7;
        label_1:
        break label_7;
        label_2:
        break label_7;
        label_3:
        break label_7;
        label_4:
        break label_7;
        label_5:
        break label_7;
        label_6:
        label_7:
        return ItemStack.EMPTY;
    }

    static {
        LOGO_ID = Identifier.fromNamespaceAndPath("lightning", "textures/gui/nametag_bolt.png");
        ENCHANT_CACHE = new ConcurrentHashMap();
        NAME_CACHE = new ConcurrentHashMap();
    }

}
