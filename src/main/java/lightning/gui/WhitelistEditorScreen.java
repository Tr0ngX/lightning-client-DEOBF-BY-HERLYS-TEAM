package lightning.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.minecraft.IdentifierUtils;
import lightning.utils.system.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;

public class WhitelistEditorScreen extends Screen {
    private static final int COL_WIDTH = 150;
    private static final int COL_GAP = 10;
    private static final int HEADER_H = 13;
    private static final int ROW_H = 18;
    private static final int ICON_SIZE = 16;
    private static final int SLIDE_DURATION_MS = 320;
    private static final float SLIDE_DISTANCE = 160.0f;
    private static final int SEARCH_STAGGER_MS = 40;
    private static final int SEARCH_ROW_ANIM_MS = 220;
    private static final float SEARCH_SLIDE_DISTANCE = 40.0f;
    private final WhitelistSetting setting;
    private final Screen parent;
    private final List allEntries;
    private String query;
    private String lastQuery;
    private long queryChangeTime;
    private float scrollAll;
    private float targetScrollAll;
    private float scrollWhite;
    private float targetScrollWhite;
    private final Animation slideAnim;
    private boolean closing;
    private final Timer lineTimer;
    private boolean showLine;
    private int colX1;
    private int colX2;
    private int colY;
    private int colHeight;
    private static final String GROUP_PREFIX = "group:";
    private static final String[] DYE_COLORS;
    private static final int MIN_FAMILY_SIZE = 8;

    public WhitelistEditorScreen(WhitelistSetting arg0, Screen arg1) {
        super(Component.literal("night-whitelist-editor"));
        this.query = "";
        this.lastQuery = "";
        this.queryChangeTime = System.currentTimeMillis();
        this.scrollAll = 0.0f;
        this.targetScrollAll = 0.0f;
        this.scrollWhite = 0.0f;
        this.targetScrollWhite = 0.0f;
        this.slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
        this.closing = false;
        this.lineTimer = new Timer();
        this.showLine = false;
        this.setting = arg0;
        this.parent = arg1;
        this.allEntries = buildEntries(arg0);
    }

    private static String familyOf(String arg0) {
        Object var1 = DYE_COLORS;
        int var2 = var1.length;
        int var3 = 0;
        while (var3 < var2) {
            Object var4 = var1[var3];
            if (arg0.equals(var4)) return "";
            if (!(arg0.startsWith((var4 + "_")))) {
            } else {
                return arg0.substring((var4.length() + 1));
            }
            var3++;
        }
        return null;
    }

    private static List buildEntries(WhitelistSetting arg0) {
        Object var1 = new ArrayList();
        Object var2 = new LinkedHashMap();
        if (arg0.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach((arg2) -> {
    Object var3 = ((Block) arg2.getValue());
    Object var4 = ((ResourceKey) arg2.getKey()).identifier().toString();
    Object var5 = var3.asItem();
    if (var5 == Items.AIR) {
    } else {
    }
    Object var6 = new ItemStack(var5);
    Object var7 = new WhitelistEditorScreen.Entry(var4, var4.replace("minecraft:", ""), var6, var3);
    arg0.add(var7);
    Object var8 = familyOf(var4.substring("minecraft:".length()));
    if (var8 != null && !(var8.isEmpty())) {
        ((List) arg1.computeIfAbsent(var8, (arg0) -> new ArrayList())).add(var7);
    }
});
            break label_2;
        }
        if (arg0.getType() == WhitelistSetting.Type.POTIONS) {
            BuiltInRegistries.POTION.entrySet().forEach((arg1) -> {
    Object var2 = ((Potion) arg1.getValue());
    Object var3 = ((ResourceKey) arg1.getKey()).identifier().toString();
    Object var4 = BuiltInRegistries.POTION.wrapAsHolder(var2);
    Object var5 = new ItemStack(Items.SPLASH_POTION);
    var5.set(DataComponents.POTION_CONTENTS, new PotionContents(var4));
    Object var6 = ("Splash Potion of " + var2.name().replace("_", " "));
    arg0.add(new WhitelistEditorScreen.Entry(var3, var6, var5, var2));
});
            break label_2;
        }
        BuiltInRegistries.ITEM.entrySet().forEach((arg2) -> {
    Object var3 = ((Item) arg2.getValue());
    Object var4 = ((ResourceKey) arg2.getKey()).identifier().toString();
    Object var5 = new WhitelistEditorScreen.Entry(var4, var4.replace("minecraft:", ""), new ItemStack(var3), var3);
    arg0.add(var5);
    Object var6 = familyOf(var4.substring("minecraft:".length()));
    if (var6 != null && !(var6.isEmpty())) {
        ((List) arg1.computeIfAbsent(var6, (arg0) -> new ArrayList())).add(var5);
    }
});
        label_2:
        Object var3 = var2.entrySet().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Map.Entry) var3.next());
            if (((List) var4.getValue()).size() >= 8) {
                Object var5 = ((WhitelistEditorScreen.Entry) ((List) var4.getValue()).get(0));
                Object var6 = ((String) var4.getKey()).replace("_", " ");
                var1.add(new WhitelistEditorScreen.Entry(("group:" + ((String) var4.getKey())), (var6 + " (all colors)"), var5.icon, null));
            }
        }
        var1.sort((arg0, arg1) -> arg0.name.compareToIgnoreCase(arg1.name));
        return var1;
    }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        Renderer2D.renderQuad(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(10, 10, 10, 150));
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        super.extractRenderState(arg0, arg1, arg2, arg3);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float var5 = this.slideAnim.get((!(!this.closing) ? 0.0f : 1.0f));
        if (!(!this.closing) && var5 <= 0.001f) {
            Minecraft.getInstance().gui.setScreen(this.parent);
            return;
        }
        int var6 = 310;
        this.colX1 = ((this.width - var6) / 2);
        this.colX2 = ((this.colX1 + 150) + 10);
        this.colY = 20;
        this.colHeight = (this.height - 40);
        arg0.pose().pushMatrix();
        arg0.pose().translate(0.0f, ((1.0f - var5) * -160.0f));
        Object var7 = this.setting.getWhitelistIds();
        Object var8 = this.allEntries.stream().filter((arg1) -> arg0.contains(arg1.id)).toList();
        Object var9 = this.allEntries.stream().filter((arg1) -> !(arg0.contains(arg1.id))).toList();
        if (this.query.isEmpty()) {
        } else {
        }
        Object var10 = filter(var9, this.query);
        renderSearchColumn(arg0, arg1, arg2, this.colX1, var10, var7);
        renderColumn(arg0, arg1, arg2, this.colX2, ("Whitelist (" + var8.size() + ")"), var8, var7, this.scrollWhite, 2);
        arg0.pose().popMatrix();
    }

    private List filter(List arg0, String arg1) {
        Object var3 = arg1.toLowerCase().split(" ");
        return arg0.stream().filter((arg1) -> {
    Object var2 = arg1.name.toLowerCase();
    Object var3 = arg0;
    int var4 = var3.length;
    int var5 = 0;
    while (var5 < var4) {
        Object var6 = var3[var5];
        if (var6.isEmpty()) break label_2;
        if (var2.contains(var6)) {
            break label_2;
        }
        return false;
        label_2:
        var5++;
    }
    return true;
}).toList();
    }

    private void renderColumn(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, String arg4, List arg5, List arg6, float arg7, int arg8) {
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 150)), ((float) (this.colY + 13)), new Color(21, 21, 21, 200));
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) ((this.colY + 13) - 1)), ((float) (arg3 + 150)), ((float) (this.colY + 13)), ClickGuiScreen.getButtonColor(this.colY, 200));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, arg4, (arg3 + 3), (this.colY + 2), Color.WHITE);
        int var10 = (this.colY + 13);
        int var11 = (this.colHeight - 13);
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) var10), ((float) (arg3 + 150)), ((float) (var10 + var11)), new Color(16, 16, 16, 180));
        Renderer2D.renderOutline(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 150)), ((float) (var10 + var11)), ClickGuiScreen.getButtonColor(this.colY, 120));
        arg0.enableScissor(arg3, var10, (arg3 + 150), (var10 + var11));
        int var12 = ((var11 / 18) + 2);
        int var13 = ((int) arg7);
        int var14 = 0;
        while (var14 < var12) {
            int var15 = (var13 + var14);
            if (var15 < 0) break label_3;
            if (var15 >= arg5.size()) {
                break label_3;
            }
            Object var16 = ((WhitelistEditorScreen.Entry) arg5.get(var15));
            int var17 = (var10 + Math.round((((float) (var14 * 18)) - ((arg7 - ((float) var13)) * 18.0f))));
            if ((var17 + 18) < var10) break label_3;
            if (var17 > (var10 + var11)) {
                break label_3;
            }
            renderRow(arg0, arg1, arg2, arg3, var17, var16, arg6.contains(var16.id), arg8);
            label_3:
            var14++;
        }
        arg0.disableScissor();
    }

    private void renderSearchColumn(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, List arg4, List arg5) {
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 150)), ((float) (this.colY + 13)), new Color(21, 21, 21, 200));
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) ((this.colY + 13) - 1)), ((float) (arg3 + 150)), ((float) (this.colY + 13)), ClickGuiScreen.getButtonColor(this.colY, 200));
        if (this.query.isEmpty()) {
            Object var7 = "Items (search...)";
            int var8 = LightningClient.FONT_MANAGER.getWidth(" ");
            if (!(!this.showLine)) {
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "|", (arg3 + 3), (this.colY + 2), Color.WHITE);
            }
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var7, ((arg3 + 3) + var8), (this.colY + 2), Color.GRAY);
        } else {
            var7 = (this.query + (!(!this.showLine) ? "|" : " "));
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var7, (arg3 + 3), (this.colY + 2), Color.WHITE);
        }
        var7 = (this.colY + 13);
        var8 = (this.colHeight - 13);
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) var7), ((float) (arg3 + 150)), ((float) (var7 + var8)), new Color(16, 16, 16, 180));
        Renderer2D.renderOutline(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 150)), ((float) (var7 + var8)), ClickGuiScreen.getButtonColor(this.colY, 120));
        arg0.enableScissor(arg3, var7, (arg3 + 150), (var7 + var8));
        long var9 = System.currentTimeMillis();
        int var11 = ((var8 / 18) + 2);
        int var12 = ((int) this.scrollAll);
        int var13 = 0;
        while (var13 < var11) {
            int var14 = (var12 + var13);
            if (var14 < 0) break label_8;
            if (var14 >= arg4.size()) {
                break label_8;
            }
            Object var15 = ((WhitelistEditorScreen.Entry) arg4.get(var14));
            int var16 = (var7 + Math.round((((float) (var13 * 18)) - ((this.scrollAll - ((float) var12)) * 18.0f))));
            if ((var16 + 18) < var7) break label_8;
            if (var16 > (var7 + var8)) {
                break label_8;
            }
            float var17 = ((float) ((var9 - this.queryChangeTime) - ((long) (var14 * 40))));
            float var18 = Easing.ease(Mth.clamp((var17 / 220.0f), 0.0f, 1.0f), Easing.Method.EASE_OUT_CUBIC);
            int var19 = Math.round(((1.0f - var18) * 40.0f));
            arg0.pose().pushMatrix();
            arg0.pose().translate(((float) -(var19)), 0.0f);
            renderRow(arg0, arg1, arg2, arg3, var16, var15, arg5.contains(var15.id), 1);
            arg0.pose().popMatrix();
            label_8:
            var13++;
        }
        arg0.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, int arg4, WhitelistEditorScreen.Entry arg5, boolean arg6, int arg7) {
        if (((arg1 < arg3) || (arg1 >= (arg3 + 150))) || (arg2 < arg4)) break label_0;
        var tmp0 = (arg2 < ((arg4 + 18) - 1) ? 1 : 0);
        int var10 = tmp0;
        int var11 = tmp0;
        if (!(!var10)) {
            break label_4;
        }
        if (!(!arg6)) {
            break label_4;
        }
        label_4:
        Object var9 = new Color(0, 0, 0, 0);
        if (var9.getAlpha() > 0) {
            Renderer2D.renderQuad(arg0, ((float) arg3), ((float) arg4), ((float) (arg3 + 150)), ((float) ((arg4 + 18) - 1)), var9);
        }
        if (!(arg5.icon.isEmpty())) {
            arg0.item(arg5.icon, (arg3 + 2), (arg4 + 0));
        }
        Object var12 = (!(!arg6) ? Color.YELLOW : Color.WHITE);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, arg5.name, ((arg3 + 16) + 6), ((arg4 + 8) - (LightningClient.FONT_MANAGER.getHeight() / 2)), var12);
    }

    public boolean mouseClicked(MouseButtonEvent arg0, boolean arg1) {
        double var3 = arg0.x();
        double var5 = arg0.y();
        if (arg0.button()) return super.mouseClicked(arg0, arg1);
        Object var7 = this.setting.getWhitelistIds();
        int var8 = (this.colY + 13);
        int var9 = (this.colHeight - 13);
        if ((var5 < ((double) var8)) || (var5 >= ((double) (var8 + var9)))) {
            return super.mouseClicked(arg0, arg1);
        }
        if (var3 >= ((double) this.colX1) && var3 < ((double) (this.colX1 + 150))) {
            Object var10 = this.allEntries.stream().filter((arg1) -> !(arg0.contains(arg1.id))).toList();
            if (this.query.isEmpty()) {
            } else {
            }
            Object var11 = filter(var10, this.query);
            Object var12 = pickRow(var3, var5, var8, this.scrollAll, var11);
            if (var12 != null) {
                addToWhitelist(var12);
            }
            return true;
        }
        if (var3 >= ((double) this.colX2) && var3 < ((double) (this.colX2 + 150))) {
            var10 = this.allEntries.stream().filter((arg1) -> arg0.contains(arg1.id)).toList();
            var11 = pickRow(var3, var5, var8, this.scrollWhite, var10);
            if (var11 != null) {
                removeFromWhitelist(var11);
            }
            return true;
        }
        return super.mouseClicked(arg0, arg1);
    }

    private WhitelistEditorScreen.Entry pickRow(double arg0, double arg1, int arg2, float arg3, List arg4) {
        int var8 = ((int) (arg1 - ((double) arg2)));
        int var9 = (((int) arg3) + (var8 / 18));
        if ((var9 < 0) || (var9 >= arg4.size())) {
            return null;
        }
        return ((WhitelistEditorScreen.Entry) arg4.get(var9));
    }

    private void addToWhitelist(WhitelistEditorScreen.Entry arg0) {
        if (arg0.id.startsWith("group:")) {
            Object var2 = arg0.id.substring("group:".length());
            if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                BuiltInRegistries.BLOCK.entrySet().forEach(() -> {
    Object var3 = ((ResourceKey) arg1.getKey()).identifier().toString().substring("minecraft:".length());
    if (!(arg0.equals(familyOf(var3)))) {
        if (var3.equals(arg0)) {
        }
        this.setting.add(arg1.getValue());
    }
});
            } else {
                BuiltInRegistries.ITEM.entrySet().forEach(() -> {
    Object var3 = ((ResourceKey) arg1.getKey()).identifier().toString().substring("minecraft:".length());
    if (!(arg0.equals(familyOf(var3)))) {
        if (var3.equals(arg0)) {
        }
        this.setting.add(arg1.getValue());
    }
});
            }
            return;
        }
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.add(IdentifierUtils.getBlock(arg0.id));
            break label_5;
        }
        if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.add(IdentifierUtils.getPotion(arg0.id));
            break label_5;
        }
        this.setting.add(IdentifierUtils.getItem(arg0.id));
        label_5:
    }

    private void removeFromWhitelist(WhitelistEditorScreen.Entry arg0) {
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.remove(IdentifierUtils.getBlock(arg0.id));
            break label_2;
        }
        if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.remove(IdentifierUtils.getPotion(arg0.id));
            break label_2;
        }
        this.setting.remove(IdentifierUtils.getItem(arg0.id));
        label_2:
    }

    public boolean mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        if (arg0 >= ((double) this.colX1) && arg0 < ((double) (this.colX1 + 150))) {
            Object var9 = this.setting.getWhitelistIds();
            Object var10 = this.allEntries.stream().filter((arg1) -> !(arg0.contains(arg1.id))).toList();
            if (this.query.isEmpty()) {
            } else {
            }
            Object var11 = filter(var10, this.query);
            this.targetScrollAll = clampScroll((this.targetScrollAll - ((float) arg3)), var11.size());
            break label_3;
        }
        if ((arg0 < ((double) this.colX2)) || (arg0 >= ((double) (this.colX2 + 150)))) break label_3;
        this.targetScrollWhite = clampScroll((this.targetScrollWhite - ((float) arg3)), this.setting.getWhitelistIds().size());
        label_3:
        this.scrollAll = (this.scrollAll + ((this.targetScrollAll - this.scrollAll) * 0.5f));
        this.scrollWhite = (this.scrollWhite + ((this.targetScrollWhite - this.scrollWhite) * 0.5f));
        return true;
    }

    private float clampScroll(float arg0, int arg1) {
        int var3 = (this.colHeight - 13);
        float var4 = Math.max(0.0f, (((float) arg1) - (((float) var3) / 18.0f)));
        return Math.max(0.0f, Math.min(arg0, var4));
    }

    public boolean keyPressed(KeyEvent arg0) {
        int var2 = arg0.key();
        if (var2 == 256) {
            requestClose();
            return true;
        }
        if (var2 == 259 && !(this.query.isEmpty())) {
            setQuery(this.query.substring(0, (this.query.length() - 1)));
            return true;
        }
        return super.keyPressed(arg0);
    }

    public boolean charTyped(CharacterEvent arg0) {
        int var2 = ((char) arg0.codepoint());
        if (!(Character.isISOControl(var2))) {
            setQuery((this.query + var2));
            return true;
        }
        return super.charTyped(arg0);
    }

    private void setQuery(String arg0) {
        if (!(arg0.equals(this.lastQuery))) {
            this.queryChangeTime = System.currentTimeMillis();
            this.lastQuery = arg0;
            this.scrollAll = 0.0f;
            this.targetScrollAll = 0.0f;
        }
        this.query = arg0;
    }

    public boolean isPauseScreen() {
        return false;
    }

    static {
        var tmp0 = new String[16];
        tmp0[0] = "white";
        tmp0[1] = "orange";
        tmp0[2] = "magenta";
        tmp0[3] = "light_blue";
        tmp0[4] = "yellow";
        tmp0[5] = "lime";
        tmp0[6] = "pink";
        tmp0[7] = "gray";
        tmp0[8] = "light_gray";
        tmp0[9] = "cyan";
        tmp0[10] = "purple";
        tmp0[11] = "blue";
        tmp0[12] = "brown";
        tmp0[13] = "green";
        tmp0[14] = "red";
        tmp0[15] = "black";
        DYE_COLORS = tmp0;
    }


    class Entry {
        final String id;
        final String name;
        final ItemStack icon;

        Entry(String arg0, String arg1, ItemStack arg2, Object arg3) {
            this.id = arg0;
            this.name = arg1;
            this.icon = arg2;
        }

    }
}
