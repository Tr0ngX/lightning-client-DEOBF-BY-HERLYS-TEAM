package lightning.gui.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.HeaderFx;
import lightning.gui.LightningSkin;
import lightning.gui.impl.ModuleButton;
import lightning.gui.impl.WhitelistButton;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.modules.impl.core.PingBypassModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class Frame {
    public static final int HEADER_H = 16;
    public static final int ROW_H = 14;
    public static final int CHILD_H = 13;
    private static final int BODY_PAD = 4;
    private final Module.Category category;
    private final String headerLabel;
    private int x;
    private int y;
    private int width;
    private int height;
    private int totalHeight;
    private int baseContentHeight;
    private int dragX;
    private int dragY;
    private int textPadding;
    private int maxViewHeight;
    public boolean open;
    public boolean dragging;
    private final ArrayList buttons;
    private float scrollOffset;
    private float targetScroll;
    private long lastScrollTime;
    private final Animation openAnim;

    public Frame(Module.Category category, int n, int n2, int n3, int n4) {
        this.dragX = 0;
        this.dragY = 0;
        this.textPadding = 3;
        this.maxViewHeight = 2147483647;
        this.open = true;
        this.dragging = false;
        this.buttons = new ArrayList();
        this.scrollOffset = 0.0f;
        this.targetScroll = 0.0f;
        this.lastScrollTime = System.currentTimeMillis();
        this.openAnim = new Animation(1.0f, 1.0f, 220, Easing.Method.EASE_OUT_CUBIC);
        this.category = category;
        this.headerLabel = null;
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        Object var6 = LightningClient.MODULE_MANAGER.getModules(category).iterator();
        while (var6.hasNext()) {
            Module module = ((Module) var6.next());
            if ((module instanceof PingBypassModule)) {
                break label_19;
            }
            this.buttons.add(new ModuleButton(module, this, 14));
        }
    }

    public Frame(String string, List list, int n, int n2, int n3, int n4) {
        this.dragX = 0;
        this.dragY = 0;
        this.textPadding = 3;
        this.maxViewHeight = 2147483647;
        this.open = true;
        this.dragging = false;
        this.buttons = new ArrayList();
        this.scrollOffset = 0.0f;
        this.targetScroll = 0.0f;
        this.lastScrollTime = System.currentTimeMillis();
        this.openAnim = new Animation(1.0f, 1.0f, 220, Easing.Method.EASE_OUT_CUBIC);
        this.category = null;
        this.headerLabel = string;
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        this.buttons.addAll(list);
    }

    public int getRequiredWidth() {
        int n = 0;
        Object var2 = this.buttons.iterator();
        while (var2.hasNext()) {
            Button button = ((Button) var2.next());
            if (!((button instanceof ModuleButton))) {
                break label_2;
            }
            n = Math.max(n, ((ModuleButton) button).getRequiredWidth());
        }
        return n;
    }

    private int visibleContentHeight() {
        return Math.min(this.baseContentHeight, this.maxViewHeight);
    }

    public int getMaxViewHeight() {
        return this.maxViewHeight;
    }

    public void setMaxViewHeight(int n) {
        this.maxViewHeight = n;
    }

    private String getHeaderText() {
        if (this.category != null) {
        } else {
        }
        return this.headerLabel;
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        render(guiGraphicsExtractor, n, n2, f, "");
    }

    public void render(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f, String string) {
        if (!this.dragging) break label_3;
        setX((n - this.dragX));
        setY((n2 - this.dragY));
        label_3:
        if (string == null) break label_4;
        boolean bl = !(string.isEmpty());
        this.totalHeight = this.height;
        this.baseContentHeight = 0;
        this.totalHeight = (this.totalHeight + 1);
        float f7 = this.buttons.iterator();
        while (f7.hasNext()) {
            Button button = ((Button) f7.next());
            if (!(!bl) && (button instanceof ExpandableRow)) {
                ExpandableRow expandableRow = ((ExpandableRow) button);
                if (!(expandableRow.getRowName().toLowerCase().contains(string.toLowerCase()))) {
                    button.setVisible(false);
                    break label_10;
                }
                button.setVisible(true);
                expandableRow.setSearchQuery(string);
                break label_22;
            }
            if ((bl) || (!((button instanceof ExpandableRow)))) break label_22;
            expandableRow = ((ExpandableRow) button);
            button.setVisible(true);
            expandableRow.setSearchQuery("");
            label_22:
            if (!(button.isVisible())) {
                break label_10;
            }
            this.baseContentHeight = (this.baseContentHeight + button.getHeight());
            button.setX(this.x);
            button.setY((this.y + this.totalHeight));
            this.totalHeight = (this.totalHeight + button.getHeight());
            if (!((button instanceof ExpandableRow))) {
                break label_10;
            }
            expandableRow = ((ExpandableRow) button);
            float f3 = expandableRow.getOpenAmount();
            List list = expandableRow.getButtons();
            float f4 = 0.0f;
            float f2 = 0.0f;
            int n6 = list.iterator();
            while (n6.hasNext()) {
                Button button2 = ((Button) n6.next());
                button2.getSetting().getVisibility().update();
                boolean bl2 = button2.getSetting().getVisibility().isVisible();
                button2.setVisible(bl2);
                if (!bl2) {
                    break label_34;
                }
                float f5 = 1.0f;
                Object object = button2.getSetting().getVisibility();
                if ((object instanceof CategorySetting.Visibility)) {
                    CategorySetting.Visibility visibility = ((CategorySetting.Visibility) object);
                    f5 = visibility.getValue().getOpenAmount();
                    break label_48;
                }
                if (!((object instanceof BooleanSetting.Visibility))) break label_48;
                visibility = ((BooleanSetting.Visibility) object);
                f5 = visibility.getOpenAmount();
                label_48:
                float f6 = (((float) button2.getHeight()) * f5);
                button2.setX(this.x);
                button2.setY(((this.y + this.totalHeight) + Math.round(f4)));
                f4 = (f4 + f6);
                f2 = (f2 + f6);
            }
            n6 = Math.round((f2 * f3));
            expandableRow.setRevealHeight(n6);
            this.totalHeight = (this.totalHeight + n6);
            if (!bl) {
                break label_10;
            }
            this.baseContentHeight = (this.baseContentHeight + n6);
        }
        f7 = ((float) ((this.totalHeight - this.height) - 1));
        float f8 = ((float) visibleContentHeight());
        float f9 = 0.0f;
        f3 = -(Math.max(0.0f, (f7 - f8)));
        long l = System.currentTimeMillis();
        f2 = Math.min((((float) (l - this.lastScrollTime)) / 1000.0f), 0.05f);
        this.lastScrollTime = l;
        if (f2 <= 1.0E-4f) {
            f2 = 0.016f;
        }
        if (this.targetScroll > f9) {
            this.targetScroll = (this.targetScroll + ((f9 - this.targetScroll) * Math.min(1.0f, (8.0f * f2))));
            break label_73;
        }
        if (this.targetScroll >= f3) break label_73;
        this.targetScroll = (this.targetScroll + ((f3 - this.targetScroll) * Math.min(1.0f, (8.0f * f2))));
        label_73:
        float f10 = (1.0f - ((float) Math.exp((-9.0 * ((double) f2)))));
        this.scrollOffset = Mth.lerp(f10, this.scrollOffset, this.targetScroll);
        if (Math.abs((this.scrollOffset - this.targetScroll)) < 0.01f && this.targetScroll >= f3 && this.targetScroll <= f9) {
            this.scrollOffset = this.targetScroll;
        }
        var tmp0 = Math.round(this.scrollOffset);
        int n5 = tmp0;
        if (!tmp0) break label_91;
        button2 = this.buttons.iterator();
        while (button2.hasNext()) {
            button = ((Button) button2.next());
            if (!(button.isVisible())) {
                break label_80;
            }
            button.setY((button.getY() + n5));
            if (!((button instanceof ExpandableRow))) {
                break label_80;
            }
            ExpandableRow scrollRow = ((ExpandableRow) button);
            visibility = scrollRow.getButtons().iterator();
            while (visibility.hasNext()) {
                Button button3 = ((Button) visibility.next());
                if (!(button3.isVisible())) {
                    break label_86;
                }
                button3.setY((button3.getY() + n5));
            }
        }
        label_91:
        ClickGuiModule clickGuiModule = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        boolean bl3 = clickGuiModule.categoryIcons.getValue();
        HeaderFx.headerBg(guiGraphicsExtractor, ((float) this.x), ((float) this.y), ((float) (this.x + this.width)), ((float) (this.y + this.height)), ClickGuiScreen.getButtonColor(this.y, 55));
        if (!(!bl3)) {
        } else {
        }
        object = "";
        HeaderFx.header(LightningClient.FONT_MANAGER, guiGraphicsExtractor, (((String) object) + getHeaderText()), (this.x + this.textPadding), (this.y + 2), Color.WHITE);
        if (clickGuiModule.triangles.getValue()) {
            String string2 = (!(!this.open) ? "\u25b2" : "\u25bc");
            int n7 = (((this.x + this.width) - this.textPadding) - LightningClient.FONT_MANAGER.getWidth(string2));
            int n4 = (this.y + Math.round((((float) (this.height - LightningClient.FONT_MANAGER.getHeight())) / 2.0f)));
            LightningClient.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string2, n7, n4, new Color(255, 255, 255, 180));
        }
        float f11 = this.openAnim.get((!(!this.open) ? 1.0f : 0.0f));
        int n8 = Math.round((((float) visibleContentHeight()) * f11));
        if (n8 > 0) {
            n4 = (this.y + this.height);
            int n3 = ((n4 + n8) + 4);
            guiGraphicsExtractor.enableScissor(this.x, n4, (this.x + this.width), n3);
            HeaderFx.bodyBg(guiGraphicsExtractor, ((float) this.x), ((float) n4), ((float) (this.x + this.width)), ((float) n3), new Color(14, 14, 14, 195));
            float f12 = this.buttons.iterator();
            while (f12.hasNext()) {
                button = ((Button) f12.next());
                if (!(button.isVisible())) {
                    break label_115;
                }
                if ((button.getY() + button.getHeight()) >= n4) {
                    if (button.getY() > n3) {
                    }
                } else {
                }
                int n9 = 0;
                int n10 = 0;
                if (n9 == 0) break label_123;
                if (!((button instanceof ExpandableRow))) break label_115;
                if (((ExpandableRow) button).getOpenAmount() > 0.001f) break label_123;
                break label_115;
                label_123:
                button.render(guiGraphicsExtractor, n, n2, f);
            }
            if (f7 > f8 && f8 > 0.0f && n8 > 10) {
                f12 = (f7 - f8);
                float f13 = Mth.clamp((-(this.scrollOffset) / f12), 0.0f, 1.0f);
                n9 = Math.max(10, Math.round((((float) n8) * (f8 / f7))));
                int n11 = (n4 + Math.round((f13 * ((float) (n8 - n9)))));
                LightningSkin.fillQuad(guiGraphicsExtractor, ((float) ((this.x + this.width) - 3)), ((float) n11), ((float) ((this.x + this.width) - 1)), ((float) (n11 + n9)), ClickGuiScreen.getButtonColor(this.y, 160));
            }
            guiGraphicsExtractor.disableScissor();
        }
        Color color = ClickGuiScreen.getButtonColor(this.y, 230);
        if (!(!this.open)) {
        } else {
        }
        n3 = (this.y + this.height);
        LightningSkin.drawOutline(guiGraphicsExtractor, ((float) this.x), ((float) this.y), ((float) (this.x + this.width)), ((float) n3), color);
    }

    private String getCategoryIcon() {
        if (this.category == null) return "\u2726";
        switch (Frame.1.$SwitchMap$lightning$modules$Module$Category[this.category.ordinal()]) {
            case 1 -> { break label_4; }
            case 2 -> { break label_5; }
            case 3 -> { break label_6; }
            case 4 -> { break label_7; }
            case 5 -> { break label_8; }
            case 6 -> { break label_9; }
            default -> { break label_3; }
        }
        label_3:
        throw new MatchException(null, null);
        label_4:
        break label_10;
        label_5:
        break label_10;
        label_6:
        break label_10;
        label_7:
        break label_10;
        label_8:
        break label_10;
        label_9:
        label_10:
        return "\u25c6";
    }

    public void mouseClicked(double d, double d2, int n) {
        if (!(isHovering(d, d2))) break label_13;
        if (n == 0) {
            this.dragging = true;
            this.dragX = ((int) (d - ((double) getX())));
            this.dragY = ((int) (d2 - ((double) getY())));
            break label_13;
        }
        if (n != 1) break label_13;
        this.open = !this.open;
        var tmp0 = (!(!this.open) ? 1 : 0);
        int n2 = tmp0;
        int n4 = tmp0;
        label_13:
        if (!this.open) break label_24;
        n2 = (this.y + this.height);
        n4 = ((n2 + Math.round((((float) visibleContentHeight()) * this.openAnim.get((!(!this.open) ? 1.0f : 0.0f))))) + 4);
        if ((((d < ((double) this.x)) || (d > ((double) (this.x + this.width)))) || (d2 < ((double) n2))) || (d2 > ((double) n4))) break label_24;
        Object var8 = this.buttons.iterator();
        while (var8.hasNext()) {
            Button button = ((Button) var8.next());
            if (!(button.isVisible())) {
                break label_20;
            }
            button.mouseClicked(d, d2, n);
        }
        label_24:
    }

    public void mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            this.dragging = false;
        }
        Object var6 = this.buttons.iterator();
        while (var6.hasNext()) {
            Button button = ((Button) var6.next());
            if (!(button.isVisible())) {
                break label_3;
            }
            button.mouseReleased(d, d2, n);
        }
    }

    public void mouseScrolled(double d, double d2, double d3, double d4) {
        int n = this.y;
        if (!(!this.open)) {
        } else {
        }
        int n2 = ((Math.round((((float) visibleContentHeight()) * this.openAnim.get((!(!this.open) ? 1.0f : 0.0f)))) + 4) + 0);
        if ((((d < ((double) this.x)) || (d > ((double) (this.x + this.width)))) || (d2 < ((double) n))) || (d2 > ((double) n2))) break label_34;
        boolean bl = false;
        float f = this.buttons.iterator();
        while (f.hasNext()) {
            Button button2 = ((Button) f.next());
            if (!((button2 instanceof ExpandableRow))) break label_9;
            var tmp0 = ((ExpandableRow) button2);
            ExpandableRow expandableRow = tmp0;
            if (!(tmp0.isOpen())) {
                break label_9;
            }
            List list = expandableRow.getButtons().stream().filter((button) -> (button instanceof WhitelistButton)).toList();
            float f5 = list.iterator();
            label_14:
            if (f5.hasNext()) {
                Button button3 = ((Button) f5.next());
                if (!((button3 instanceof WhitelistButton))) break label_14;
                var tmp1 = ((WhitelistButton) button3);
                WhitelistButton whitelistButton = tmp1;
                if (!(tmp1.isHandlingScroll(d, d2))) {
                    break label_14;
                }
                whitelistButton.mouseScrolled(d, d2, d3, d4);
                bl = true;
                break label_9;
            }
        }
        if ((bl) || (d4 == 0.0)) break label_34;
        f = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).scrollSpeed.getValue().floatValue();
        float f2 = ((float) ((this.totalHeight - this.height) - 1));
        float f3 = ((float) visibleContentHeight());
        float f4 = 0.0f;
        f5 = -(Math.max(0.0f, (f2 - f3)));
        float f6 = ((float) ((d4 * ((double) f)) * 12.0));
        float f7 = 1.0f;
        if (this.targetScroll > f4) {
            f7 = (1.0f / (1.0f + ((this.targetScroll - f4) * 0.05f)));
            break label_33;
        }
        if (this.targetScroll >= f5) break label_33;
        f7 = (1.0f / (1.0f + ((f5 - this.targetScroll) * 0.05f)));
        label_33:
        this.targetScroll = (this.targetScroll + (f6 * f7));
        label_34:
    }

    public void mouseDragged(double d, double d2, int n, double d3, double d4) {
        Object var10 = this.buttons.iterator();
        while (var10.hasNext()) {
            Button button = ((Button) var10.next());
            button.mouseDragged(d, d2, n, d3, d4);
        }
    }

    public void keyPressed(int n, int n2, int n3) {
        if (!this.open) break label_6;
        Object var4 = this.buttons.iterator();
        while (var4.hasNext()) {
            Button button = ((Button) var4.next());
            if (!(button.isVisible())) {
                break label_2;
            }
            button.keyPressed(n, n2, n3);
        }
        label_6:
    }

    public void charTyped(char c, int n) {
        if (!this.open) break label_6;
        Object var3 = this.buttons.iterator();
        while (var3.hasNext()) {
            Button button = ((Button) var3.next());
            if (!(button.isVisible())) {
                break label_2;
            }
            button.charTyped(c, n);
        }
        label_6:
    }

    public boolean isHovering(double d, double d2) {
        if (((((double) this.x) > d) || (((double) this.y) > d2)) || (((double) (this.x + this.width)) <= d)) break label_1;
        return ((double) (this.y + this.height)) > d2;
    }

    public Module.Category getCategory() {
        return this.category;
    }

    public String getHeaderLabel() {
        return this.headerLabel;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getTotalHeight() {
        return this.totalHeight;
    }

    public int getBaseContentHeight() {
        return this.baseContentHeight;
    }

    public int getDragX() {
        return this.dragX;
    }

    public int getDragY() {
        return this.dragY;
    }

    public int getTextPadding() {
        return this.textPadding;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public ArrayList getButtons() {
        return this.buttons;
    }

    public float getScrollOffset() {
        return this.scrollOffset;
    }

    public float getTargetScroll() {
        return this.targetScroll;
    }

    public long getLastScrollTime() {
        return this.lastScrollTime;
    }

    public Animation getOpenAnim() {
        return this.openAnim;
    }

    public void setX(int n) {
        this.x = n;
    }

    public void setY(int n) {
        this.y = n;
    }

    public void setWidth(int n) {
        this.width = n;
    }

    public void setHeight(int n) {
        this.height = n;
    }

    public void setTotalHeight(int n) {
        this.totalHeight = n;
    }

    public void setBaseContentHeight(int n) {
        this.baseContentHeight = n;
    }

    public void setDragX(int n) {
        this.dragX = n;
    }

    public void setDragY(int n) {
        this.dragY = n;
    }

    public void setTextPadding(int n) {
        this.textPadding = n;
    }

    public void setOpen(boolean bl) {
        this.open = bl;
    }

    public void setDragging(boolean bl) {
        this.dragging = bl;
    }

    public void setScrollOffset(float f) {
        this.scrollOffset = f;
    }

    public void setTargetScroll(float f) {
        this.targetScroll = f;
    }

    public void setLastScrollTime(long l) {
        this.lastScrollTime = l;
    }


    class 1 {

        static {
            $SwitchMap$lightning$modules$Module$Category = new int[Module.Category.values().length];
            try {
                $SwitchMap$lightning$modules$Module$Category[Module.Category.COMBAT.ordinal()] = 1;
            } catch (NoSuchFieldError ex3) {
                Object var0 = ex3;
                try {
                }
                $SwitchMap$lightning$modules$Module$Category[Module.Category.MOVEMENT.ordinal()] = 2;
            } catch (NoSuchFieldError ex6) {
                var0 = ex6;
                try {
                }
                $SwitchMap$lightning$modules$Module$Category[Module.Category.VISUALS.ordinal()] = 3;
            } catch (NoSuchFieldError ex9) {
                var0 = ex9;
                try {
                }
                $SwitchMap$lightning$modules$Module$Category[Module.Category.PLAYER.ordinal()] = 4;
            } catch (NoSuchFieldError ex12) {
                var0 = ex12;
                try {
                }
                $SwitchMap$lightning$modules$Module$Category[Module.Category.MISCELLANEOUS.ordinal()] = 5;
            } catch (NoSuchFieldError ex15) {
                var0 = ex15;
                try {
                }
                $SwitchMap$lightning$modules$Module$Category[Module.Category.CORE.ordinal()] = 6;
            } catch (NoSuchFieldError ex18) {
                var0 = ex18;
            }
        }

    }
}
