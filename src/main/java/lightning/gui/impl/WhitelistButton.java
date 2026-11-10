package lightning.gui.impl;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.WhitelistEditorScreen;
import lightning.gui.api.Button;
import lightning.gui.api.Frame;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.minecraft.IdentifierUtils;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

public class WhitelistButton extends Button {
    private final WhitelistSetting setting;
    private boolean open;
    private String searchQuery;
    private boolean searching;
    private boolean selecting;
    private int cursorPos;
    private String oldSearchQuery;
    private boolean draggingScrollbar;
    private int dragStartY;
    private float dragStartScroll;
    private final List allElements;
    private List visibleElements;
    private final int maxDisplayed = 6;
    private float scrollOffset;
    private float targetScrollOffset;
    private final int searchBarHeight = 12;
    private final int elementHeight = 12;
    private final int offsetX = 2;
    private final int scrollbarWidth = 5;
    private final int symbolWidth = 15;

    public WhitelistButton(WhitelistSetting arg0, Frame arg1, int arg2) {
        super(arg0, arg1, arg2, arg0.getDescription());
        this.open = false;
        this.searchQuery = "";
        this.searching = false;
        this.selecting = false;
        this.cursorPos = 0;
        this.oldSearchQuery = "";
        this.draggingScrollbar = false;
        this.dragStartY = 0;
        this.dragStartScroll = 0.0f;
        this.visibleElements = new ArrayList();
        this.maxDisplayed = 6;
        this.scrollOffset = 0.0f;
        this.targetScrollOffset = 0.0f;
        this.searchBarHeight = 12;
        this.elementHeight = 12;
        this.offsetX = 2;
        this.scrollbarWidth = 5;
        this.symbolWidth = 15;
        this.setting = arg0;
        this.allElements = getAllElements();
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        int var5 = (!(this.searchQuery.equals(this.oldSearchQuery)) ? 1 : 0);
        this.oldSearchQuery = this.searchQuery;
        float var6 = 0.0f;
        if (!var5) break label_2;
        var6 = getScrollRatio();
        label_2:
        updateVisibleElements();
        clampScrollValues();
        if (!var5) break label_3;
        applyScrollRatio(var6);
        clampScrollValues();
        label_3:
        this.scrollOffset = (this.scrollOffset + ((this.targetScrollOffset - this.scrollOffset) * 0.5f));
        Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) ((getY() + 13) - 1)), ClickGuiScreen.getButtonColor(getY(), 100));
        Renderer2D.renderQuad(arg0, ((float) ((getX() + getPadding()) + 1)), ((float) getY()), ((float) ((getX() + getPadding()) + 2)), ((float) ((getY() + getHeight()) - 1)), ClickGuiScreen.getButtonColor(getY(), 30));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, this.setting.getTag(), ((getX() + getTextPadding()) + 1), (getY() + 2), Color.WHITE);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, (String.valueOf(ChatFormatting.GRAY) + this.setting.getWhitelist().size()), ((((getX() + getWidth()) - getTextPadding()) - 1) - LightningClient.FONT_MANAGER.getWidth((this.setting.getWhitelist().size()))), (getY() + 2), Color.WHITE);
        if (!this.open) break label_47;
        int var8 = (getY() + getParent().getHeight());
        Renderer2D.renderQuad(arg0, ((float) (((getX() + getPadding()) + 1) + 2)), ((float) var8), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) (var8 + 12)), ClickGuiScreen.getButtonColor(getY(), 30));
        Object var9 = this.searchQuery;
        if (!(!this.searching)) {
            var tmp0 = (LightningClient.CLICK_GUI.isShowLine() ? "|" : " ");
            Object var7 = tmp0;
            Object var10 = tmp0;
            if (this.cursorPos <= ((String) var9).length()) {
                var9 = (((String) var9).substring(0, this.cursorPos) + var7 + ((String) var9).substring(this.cursorPos));
            }
        } else {
            var9 = "Search...";
        }
        if (!(!this.searching) && this.searchQuery.isEmpty()) {
            var7 = (LightningClient.CLICK_GUI.isShowLine() ? "|" : " ");
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var7, (((getX() + getTextPadding()) + 2) + 2), ((var8 + 6) - (LightningClient.FONT_MANAGER.getHeight() / 2)), Color.WHITE);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Search...", ((((getX() + getTextPadding()) + 2) + 2) + LightningClient.FONT_MANAGER.getWidth(" ")), ((var8 + 6) - (LightningClient.FONT_MANAGER.getHeight() / 2)), Color.GRAY);
        } else {
            if (!(!this.searching)) {
                if (!(!this.selecting)) {
                    break label_14;
                }
                break label_14;
            }
            label_14:
            ((String) var9).drawTextWithShadow((((getX() + getTextPadding()) + 2) + 2), ((var8 + 6) - (LightningClient.FONT_MANAGER.getHeight() / 2)), ClickGuiScreen.getButtonColor(getY(), 255), Color.WHITE, Color.GRAY);
        }
        var10 = (var8 + 12);
        int var11 = Math.min(this.visibleElements.size(), 6);
        int var12 = ((((getX() + getWidth()) - getPadding()) - 5) - 1);
        int var13 = 72;
        Object var14 = this.setting.getWhitelistIds();
        if (this.searchQuery.isEmpty()) {
        } else {
        }
        Object var15 = this.searchQuery.toLowerCase().split(" ");
        int var16 = 0;
        while (var16 < var11) {
            int var18 = ((int) (((float) var16) + this.scrollOffset));
            if (var18 < 0) break label_42;
            if (var18 >= this.visibleElements.size()) {
                break label_42;
            }
            Object var19 = ((String) this.visibleElements.get(var18));
            int var20 = var14.contains(var19);
            Object var21 = var19.replace("minecraft:", "");
            Object var22 = (!(!var20) ? Color.WHITE : Color.GRAY);
            Object var23 = (!(!var20) ? Color.YELLOW : Color.LIGHT_GRAY);
            int var24 = (var10 + (var16 * 12));
            Renderer2D.renderQuad(arg0, ((float) (((getX() + getPadding()) + 1) + 2)), ((float) var24), ((float) (((getX() + getWidth()) - getPadding()) - 1)), ((float) (var24 + 12)), ClickGuiScreen.getButtonColor(getY(), 30));
            int var25 = (((getX() + getTextPadding()) + 2) + 2);
            int var26 = ((var12 - 15) + 5);
            if (((arg1 < var25) || (arg1 > var26)) || (arg2 < var24)) break label_24;
            int var27 = (arg2 < (var24 + 12) ? 1 : 0);
            Object var28 = highlightMatches(var21, var15, var22, var23);
            int var29 = 0;
            Object var30 = var28.iterator();
            while (var30.hasNext()) {
                Object var31 = ((WhitelistButton.ColoredSegment) var30.next());
                var29 = (var29 + LightningClient.FONT_MANAGER.getWidth(var31.text));
            }
            var30 = (var26 - var25);
            if (!var27) {
                if (var29 <= var30) {
                }
            } else {
            }
            var31 = truncateSegments(var28, var30);
            int var32 = var25;
            Object var33 = var31.iterator();
            while (var33.hasNext()) {
                Object var34 = ((WhitelistButton.ColoredSegment) var33.next());
                LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var34.text, var32, (var24 + 2), var34.color);
                var32 = (var32 + LightningClient.FONT_MANAGER.getWidth(var34.text));
            }
            if (!(!this.searching)) {
                if (!(!var20)) {
                }
            } else {
            }
            var33 = "+";
            var34 = ((var12 - 15) + 5);
            if (((arg1 < var34) || (arg1 > (var34 + 15))) || (arg2 < var24)) break label_36;
            var tmp1 = (arg2 < (var24 + 12) ? 1 : 0);
            int var17 = tmp1;
            int var35 = tmp1;
            if (var33.equals("+")) {
                if (!(!var17)) {
                    break label_41;
                }
                break label_41;
            }
            if (!(!var17)) {
                break label_41;
            }
            label_41:
            Object var36 = Color.WHITE;
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var33, var34, (var24 + 2), var36);
            label_42:
            var16++;
        }
        if (this.visibleElements.size() <= 6) break label_47;
        var16 = ((float) Math.max(0, (this.visibleElements.size() - 6)));
        if (var16 == 0.0f) {
        } else {
        }
        var17 = (this.scrollOffset / var16);
        var18 = ((int) (((float) var13) * (6.0f / ((float) this.visibleElements.size()))));
        if (var18 < 20) {
            var18 = 20;
        }
        var19 = ((int) (((float) (var13 - var18)) * var17));
        Renderer2D.renderQuad(arg0, ((float) var12), ((float) var10), ((float) (var12 + 5)), ((float) (var10 + var13)), ClickGuiScreen.getButtonColor(getY(), 40));
        Renderer2D.renderQuad(arg0, ((float) var12), ((float) (var10 + var19)), ((float) (var12 + 5)), ((float) ((var10 + var19) + var18)), ClickGuiScreen.getButtonColor(getY(), 200));
        label_47:
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (isHovering(arg0, arg1) && arg2 == 1) {
            this.open = !this.open;
            int var6 = (!this.open ? 1 : 0);
        }
        if ((!(isHovering(arg0, arg1))) || (arg2 != 0)) break label_4;
        if (!(!this.open)) {
            if (arg1 >= ((double) (getY() + getParent().getHeight()))) break label_4;
        }
        mc.gui.setScreen(new WhitelistEditorScreen(this.setting, mc.gui.screen()));
        return;
        label_4:
        if (!this.open) return;
        if (arg2 != 0) break label_16;
        int var8 = (getY() + getParent().getHeight());
        if (arg0 >= ((double) (((getX() + getPadding()) + 1) + 2)) && arg0 <= ((double) (((getX() + getWidth()) - getPadding()) - 1)) && arg1 >= ((double) var8) && arg1 <= ((double) (var8 + 12))) {
            this.searching = true;
            this.selecting = false;
            this.cursorPos = this.searchQuery.length();
            return;
        }
        if (!(isWithinOpenArea(arg0, arg1))) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
        }
        if (this.visibleElements.size() > 6 && isHoveringScrollbar(arg0, arg1)) {
            this.draggingScrollbar = true;
            this.dragStartY = ((int) arg1);
            this.dragStartScroll = this.scrollOffset;
            return;
        }
        if (!(isWithinOpenArea(arg0, arg1))) return;
        int var9 = (((getX() + getPadding()) + 1) + 2);
        int var10 = (((getX() + getWidth()) - getPadding()) - 1);
        if ((arg0 < ((double) var9)) || (arg0 > ((double) var10))) {
            return;
        }
        int var11 = (getY() + getParent().getHeight());
        int var12 = (var11 + 12);
        if ((arg1 < ((double) var12)) || (arg1 >= ((double) (var12 + 72)))) break label_16;
        var tmp0 = ((int) (arg1 - ((double) var12)));
        var6 = tmp0;
        var tmp1 = (((int) this.scrollOffset) + (tmp0 / 12));
        int var7 = tmp1;
        if ((tmp1 < 0) || (var7 >= this.visibleElements.size())) break label_16;
        Object var13 = ((String) this.visibleElements.get(var7));
        int var14 = ((((getX() + getWidth()) - getPadding()) - 5) - 1);
        int var15 = ((var14 - 15) + 5);
        int var16 = (var12 + ((var6 / 12) * 12));
        if ((((arg0 < ((double) var15)) || (arg0 > ((double) (var15 + 15)))) || (arg1 < ((double) var16))) || (arg1 >= ((double) (var16 + 12)))) break label_16;
        int var17 = this.setting.getWhitelistIds().contains(var13);
        if (!(!var17)) {
            if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                this.setting.remove(IdentifierUtils.getBlock(var13));
            } else {
                this.setting.remove(IdentifierUtils.getItem(var13));
            }
            this.setting.remove(var13);
            break label_16;
        }
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.add(IdentifierUtils.getBlock(var13));
            break label_16;
        }
        this.setting.add(IdentifierUtils.getItem(var13));
        label_16:
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        if (arg2 == 0 && !(!this.draggingScrollbar)) {
            this.draggingScrollbar = false;
        }
    }

    public void mouseDragged(double arg0, double arg1, int arg2, double arg3, double arg4) {
        if (!(!this.draggingScrollbar) && this.visibleElements.size() > 6) {
            int var10 = 72;
            float var11 = ((float) Math.max(0, (this.visibleElements.size() - 6)));
            float var12 = ((float) (arg1 - ((double) this.dragStartY)));
            float var13 = (var12 / ((float) var10));
            this.targetScrollOffset = (this.dragStartScroll + (var11 * var13));
            clampScrollValues();
        }
    }

    public void mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        if (!(!this.open) && isWithinOpenArea(arg0, arg1)) {
            this.targetScrollOffset = (this.targetScrollOffset - ((float) arg3));
            clampScrollValues();
        }
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!this.searching) return;
        Object var4 = mc.getWindow();
        int var5 = InputConstants.isKeyDown(var4, (Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (arg0 == 256) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
            break label_18;
        }
        if (arg0 == 257) {
            this.searching = false;
            this.selecting = false;
            this.searchQuery = "";
            this.cursorPos = 0;
            break label_18;
        }
        if (arg0 == 259) {
            if (!(!this.selecting)) {
                this.searchQuery = "";
                this.cursorPos = 0;
                this.selecting = false;
                break label_18;
            }
            if (this.cursorPos <= 0) break label_18;
            this.searchQuery = (this.searchQuery.substring(0, (this.cursorPos - 1)) + this.searchQuery.substring(this.cursorPos));
            this.cursorPos = (this.cursorPos - 1);
            break label_18;
        }
        if (arg0 == 261) {
            if (this.cursorPos >= this.searchQuery.length()) break label_18;
            this.searchQuery = (this.searchQuery.substring(0, this.cursorPos) + this.searchQuery.substring((this.cursorPos + 1)));
            break label_18;
        }
        if (arg0 == 263) {
            if (this.cursorPos <= 0) break label_18;
            this.cursorPos = (this.cursorPos - 1);
            break label_18;
        }
        if (arg0 == 262) {
            if (this.cursorPos >= this.searchQuery.length()) break label_18;
            this.cursorPos = (this.cursorPos + 1);
            break label_18;
        }
        if (!var5) break label_18;
        if (arg0 != 86) break label_13;
        try {
            Object var6 = mc.keyboardHandler.getClipboard();
            if (var6 != null) {
                this.searchQuery = (this.searchQuery.substring(0, this.cursorPos) + var6 + this.searchQuery.substring(this.cursorPos));
                this.cursorPos = (this.cursorPos + var6.length());
            }
        } catch (Exception ex12) {
            var6 = ex12;
            LightningClient.LOGGER.error("{}: Failed to process clipboard paste", var6.getClass().getName(), var6);
            break label_18;
            label_13:
            if ((arg0 != 67) || (!this.selecting)) break label_17;
            try {
                mc.keyboardHandler.setClipboard(this.searchQuery);
            } catch (Exception ex16) {
                var6 = ex16;
                LightningClient.LOGGER.error("{}: Failed to process clipboard change", var6.getClass().getName(), var6);
                break label_18;
                label_17:
                if (arg0 != 65) break label_18;
                this.selecting = true;
                this.cursorPos = this.searchQuery.length();
            }
        }
        label_18:
    }

    public void charTyped(char arg0, int arg1) {
        if ((!this.searching) || (Character.isISOControl(arg0))) break label_1;
        if (!(!this.selecting)) {
            this.searchQuery = String.valueOf(arg0);
            this.selecting = false;
            this.cursorPos = 1;
            break label_1;
        }
        this.searchQuery = (this.searchQuery.substring(0, this.cursorPos) + arg0 + this.searchQuery.substring(this.cursorPos));
        this.cursorPos = (this.cursorPos + 1);
        label_1:
    }

    public int getHeight() {
        int var1 = getParent().getHeight();
        if (!this.open) return var1;
        return ((var1 + 12) + 72);
    }

    public boolean isHovering(double arg0, double arg1) {
        if (((((double) (getX() + getPadding())) > arg0) || (((double) getY()) > arg1)) || (((double) ((getX() + getWidth()) - getPadding())) <= arg0)) break label_0;
        return ((double) (getY() + getParent().getHeight())) > arg1;
    }

    public boolean isHandlingScroll(double arg0, double arg1) {
        if (!this.open) break label_1;
        if (!(isWithinOpenArea(arg0, arg1))) {
            if (!this.draggingScrollbar) break label_1;
        }
        break label_2;
        label_1:
        label_2:
        return false;
    }

    private boolean isWithinOpenArea(double arg0, double arg1) {
        int var5 = (getY() + getParent().getHeight());
        int var6 = 84;
        if (((arg0 < ((double) (((getX() + getPadding()) + 1) + 2))) || (arg0 > ((double) (((getX() + getWidth()) - getPadding()) - 1)))) || (arg1 < ((double) var5))) break label_0;
        return arg1 <= ((double) (var5 + var6));
    }

    private boolean isHoveringScrollbar(double arg0, double arg1) {
        if ((!this.open) || (this.visibleElements.size() <= 6)) {
            return false;
        }
        int var5 = (getY() + getParent().getHeight());
        int var6 = (var5 + 12);
        int var7 = ((((getX() + getWidth()) - getPadding()) - 5) - 1);
        int var8 = 72;
        if (((arg0 < ((double) var7)) || (arg0 > ((double) (var7 + 5)))) || (arg1 < ((double) var6))) break label_2;
        return arg1 <= ((double) (var6 + var8));
    }

    private void updateVisibleElements() {
        if (this.searchQuery.isEmpty()) {
            if (!(!this.searching)) {
                Object var1 = new ArrayList(this.allElements);
            } else {
                Object var2 = this.setting.getWhitelistIds();
                Objects.requireNonNull(var2);
                var1 = ((List) this.allElements.stream().filter(var2::contains).collect(Collectors.toList()));
            }
            var1.sort((arg0, arg1) -> {
    int var2 = arg0.compareToIgnoreCase(arg1);
    if (!var2) return Integer.compare(arg0.length(), arg1.length());
    return var2;
});
        } else {
            var2 = this.searchQuery.toLowerCase().split(" ");
            var1 = ((List) this.allElements.stream().filter((arg1) -> {
    Object var2 = arg1.toLowerCase();
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
}).collect(Collectors.toList()));
            var1.sort((arg2) -> {
    int var4 = compareBySearchPriority(arg1, arg2, arg0);
    if (!(!var4)) return var4;
    int var5 = arg1.compareToIgnoreCase(arg2);
    if (!var5) return Integer.compare(arg1.length(), arg2.length());
    return var5;
});
        }
        this.visibleElements = var1;
    }

    private int compareBySearchPriority(String arg0, String arg1, String[] arg2) {
        int var4 = countStarts(arg0, arg2);
        int var5 = countStarts(arg1, arg2);
        return Integer.compare(var4, var5);
    }

    private int countStarts(String arg0, String[] arg1) {
        int var3 = arg0.length();
        Object var4 = arg0.toLowerCase();
        Object var5 = arg1;
        int var6 = var5.length;
        int var7 = 0;
        while (var7 < var6) {
            Object var8 = var5[var7];
            if ((var8.isEmpty()) || (!(var4.contains(var8.toLowerCase())))) break label_2;
            var tmp0 = var4.split(var8.toLowerCase())[0].length();
            int var9 = tmp0;
            if (tmp0 >= var3) {
                break label_2;
            }
            var3 = var9;
            label_2:
            var7++;
        }
        return var3;
    }

    private float getScrollRatio() {
        int var1 = this.visibleElements.size();
        int var2 = Math.max(0, (var1 - 6));
        if (!var2) return 0.0f;
        return (this.scrollOffset / ((float) var2));
    }

    private void applyScrollRatio(float arg0) {
        int var3 = this.visibleElements.size();
        int var4 = Math.max(0, (var3 - 6));
        var tmp0 = (arg0 * ((float) var4));
        float var2 = tmp0;
        this.scrollOffset = tmp0;
        this.targetScrollOffset = var2;
    }

    private void clampScrollValues() {
        int var1 = this.visibleElements.size();
        int var2 = Math.max(0, (var1 - 6));
        if (this.targetScrollOffset < 0.0f) {
            this.targetScrollOffset = 0.0f;
        }
        if (this.targetScrollOffset > ((float) var2)) {
            this.targetScrollOffset = ((float) var2);
        }
        if (this.scrollOffset < 0.0f) {
            this.scrollOffset = 0.0f;
        }
        if (this.scrollOffset > ((float) var2)) {
            this.scrollOffset = ((float) var2);
        }
    }

    private List getAllElements() {
        Object var1 = new ArrayList();
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach((arg1) -> arg0.add(((ResourceKey) arg1.getKey()).identifier().toString()));
        } else {
            BuiltInRegistries.ITEM.entrySet().forEach((arg1) -> arg0.add(((ResourceKey) arg1.getKey()).identifier().toString()));
        }
        var1.sort((arg0, arg1) -> {
    int var2 = arg0.compareToIgnoreCase(arg1);
    if (!var2) return Integer.compare(arg0.length(), arg1.length());
    return var2;
});
        return var1;
    }

    private List truncateSegments(List arg0, int arg1) {
        Object var3 = new ArrayList();
        int var4 = 0;
        Object var5 = arg0.iterator();
        label_0:
        if (!(var5.hasNext())) break label_5;
        Object var6 = ((WhitelistButton.ColoredSegment) var5.next());
        int var7 = LightningClient.FONT_MANAGER.getWidth(var6.text);
        if ((var4 + var7) <= arg1) {
            var3.add(var6);
            var4 = (var4 + var7);
            break label_0;
        }
        int var8 = (arg1 - var4);
        if (var8 > 0) {
            Object var11 = new StringBuilder();
            int var12 = 0;
            while (var12 < var6.text.length()) {
                var tmp0 = var6.text.charAt(var12);
                int var9 = tmp0;
                var tmp1 = LightningClient.FONT_MANAGER.getWidth(String.valueOf(tmp0));
                int var10 = tmp1;
                if (tmp1 > (var8 - LightningClient.FONT_MANAGER.getWidth("\u2026"))) break label_3;
                var11.append(var9);
                var8 = (var8 - var10);
                var12++;
            }
            label_3:
            var11.append("\u2026");
            var3.add(new WhitelistButton.ColoredSegment(var11.toString(), var6.color));
            break label_5;
        }
        var3.add(new WhitelistButton.ColoredSegment("\u2026", Color.GRAY));
        break label_5;
        label_5:
        return var3;
    }

    private List highlightMatches(String arg0, String[] arg1, Color arg2, Color arg3) {
        if (arg1.length == 0) {
            Object var5 = new ArrayList();
            var5.add(new WhitelistButton.ColoredSegment(arg0, arg2));
            return var5;
        }
        var5 = arg0.toLowerCase();
        Object var6 = new ArrayList();
        Object var7 = arg1;
        int var8 = var7.length;
        int var9 = 0;
        while (var9 < var8) {
            Object var10 = var7[var9];
            if (var10.isEmpty()) {
                break label_4;
            }
            int var12 = 0;
            label_3:
            var tmp0 = var5.indexOf(var10, var12);
            int var11 = tmp0;
            if (tmp0 == -1) break label_4;
            var6.add(new WhitelistButton.Range(var11, (var11 + var10.length())));
            var12 = (var11 + var10.length());
            break label_3;
            label_4:
            var9++;
        }
        var6 = mergeRanges(var6);
        var7 = new ArrayList();
        var8 = 0;
        var9 = var6.iterator();
        while (var9.hasNext()) {
            var10 = ((WhitelistButton.Range) var9.next());
            if (var10.start > var8) {
                var7.add(new WhitelistButton.ColoredSegment(arg0.substring(var8, var10.start), arg2));
            }
            var7.add(new WhitelistButton.ColoredSegment(arg0.substring(var10.start, var10.end), arg3));
            var8 = var10.end;
        }
        if (var8 < arg0.length()) {
            var7.add(new WhitelistButton.ColoredSegment(arg0.substring(var8), arg2));
        }
        return var7;
    }

    private List mergeRanges(List arg0) {
        if (arg0.isEmpty()) return arg0;
        arg0.sort(Comparator.comparingInt((arg0) -> arg0.start));
        Object var2 = new ArrayList();
        Object var3 = ((WhitelistButton.Range) arg0.getFirst());
        int var4 = 1;
        while (var4 < arg0.size()) {
            Object var5 = ((WhitelistButton.Range) arg0.get(var4));
            if (var5.start <= var3.end) {
                var3.end = Math.max(var3.end, var5.end);
            } else {
                var2.add(var3);
                var3 = var5;
            }
            var4++;
        }
        var2.add(var3);
        return var2;
    }


    class ColoredSegment {
        String text;
        Color color;

        @Generated
        public ColoredSegment(String arg0, Color arg1) {
            this.text = arg0;
            this.color = arg1;
        }

    }

    class Range {
        int start;
        int end;

        @Generated
        public Range(int arg0, int arg1) {
            this.start = arg0;
            this.end = arg1;
        }

    }
}
