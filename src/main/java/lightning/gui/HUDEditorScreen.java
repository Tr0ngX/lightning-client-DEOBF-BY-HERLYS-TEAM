package lightning.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lightning.LightningClient;
import lightning.gui.api.Button;
import lightning.gui.api.ExpandableRow;
import lightning.gui.api.Frame;
import lightning.gui.impl.BindButton;
import lightning.gui.impl.BooleanButton;
import lightning.gui.impl.CategoryButton;
import lightning.gui.impl.ColorButton;
import lightning.gui.impl.HudElementButton;
import lightning.gui.impl.ModeButton;
import lightning.gui.impl.NumberButton;
import lightning.gui.impl.StringButton;
import lightning.gui.impl.WhitelistButton;
import lightning.managers.FontManager;
import lightning.managers.HudElementRegistry;
import lightning.modules.impl.core.HUDEditorModule;
import lightning.modules.impl.core.HUDModule;
import lightning.settings.Setting;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HUDEditorScreen extends Screen {
    private String draggingElement;
    private float dragOffsetX;
    private float dragOffsetY;
    private final Frame elementsFrame;

    public HUDEditorScreen() {
        super(Component.literal("night-hud-editor"));
        this.draggingElement = null;
        int var1 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int var2 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int var3 = Math.round((((float) var1) * 0.23f));
        int var4 = Math.round((((float) var2) * 0.14f));
        this.elementsFrame = new Frame("HUD", List.of(), var3, var4, 110, 13);
        Object var5 = ((HUDModule) LightningClient.MODULE_MANAGER.getModule(HUDModule.class));
        Object var6 = var5.getSettings();
        Object var7 = new HashSet();
        Object var8 = new HashSet();
        Object var9 = HudElementRegistry.getElements().values().iterator();
        while (var9.hasNext()) {
            Object var10 = ((HudElementRegistry.Element) var9.next());
            if (var10.category() != null) {
                var7.add(var10.category());
            }
            if (var10.enabled() != null) {
                var8.add(var10.enabled());
            }
            Object var11 = new ArrayList();
            if (var10.category() != null) {
                var11.addAll(var6.stream().filter((arg1) -> {
    Object var3 = arg1.getVisibility();
    if (!((var3 instanceof CategorySetting.Visibility))) break label_0;
    var tmp0 = ((CategorySetting.Visibility) var3);
    Object var2 = tmp0;
    return tmp0.getValue() == arg0.category();
}).filter((arg0) -> {
    if ((arg0 instanceof BooleanSetting)) {
        if (!(arg0.getTag().equals("Enabled"))) {
        }
    } else {
    }
    return false;
}).toList());
            }
            if (var10.enabled() != null) {
                var11.addAll(var6.stream().filter((arg1) -> {
    Object var3 = arg1.getVisibility();
    if (!((var3 instanceof BooleanSetting.Visibility))) break label_0;
    var tmp0 = ((BooleanSetting.Visibility) var3);
    Object var2 = tmp0;
    return tmp0.getSetting() == arg0.enabled();
}).toList());
            }
            this.elementsFrame.getButtons().add(new HudElementButton(var10, var11, this.elementsFrame, 13));
        }
        var9 = var6.iterator();
        while (var9.hasNext()) {
            var10 = ((Setting) var9.next());
            if (!((var10 instanceof CategorySetting))) break label_6;
            var tmp0 = ((CategorySetting) var10);
            Object var12 = tmp0;
            if (var7.contains(tmp0)) break label_6;
            var tmp1 = var6.stream().filter((arg1) -> {
    Object var3 = arg1.getVisibility();
    if (!((var3 instanceof CategorySetting.Visibility))) break label_0;
    var tmp0 = ((CategorySetting.Visibility) var3);
    Object var2 = tmp0;
    return tmp0.getValue() == arg0;
}).filter((arg1) -> !(arg0.contains(arg1))).toList();
            var11 = tmp1;
            if (tmp1.isEmpty()) {
                break label_6;
            }
            this.elementsFrame.getButtons().add(new HUDEditorScreen.CustomCategoryButton(var12, var11, this.elementsFrame, 13));
        }
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
    }

    public void extractRenderState(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        FontManager.setHudRendering(true);
        Object var5 = HudElementRegistry.getElements().entrySet().iterator();
        while (var5.hasNext()) {
            Object var6 = ((Map.Entry) var5.next());
            Object var8 = ((String) var6.getKey());
            Object var9 = ((HudElementRegistry.Element) var6.getValue());
            Object var10 = HudElementRegistry.getBounds(var8);
            if (var10 != null) break label_2;
            float var11 = var9.offset().getX();
            float var12 = var9.offset().getY();
            var tmp0 = new float[4];
            tmp0[0] = var11;
            tmp0[1] = var12;
            tmp0[2] = (var11 + 60.0f);
            tmp0[3] = (var12 + 15.0f);
            var10 = tmp0;
            label_2:
            if (((((float) arg1) < var10[0]) || (((float) arg1) > var10[2])) || (((float) arg2) < var10[1])) break label_3;
            var11 = (((float) arg2) <= var10[3] ? 1 : 0);
            var12 = var9.enabled().getValue();
            int var13 = var8.equals(this.draggingElement);
            if (!(!var12)) {
            } else {
            }
            var tmp1 = new Color(150, 150, 150);
            Object var7 = tmp1;
            Object var14 = tmp1;
            if (!(!var13)) {
                break label_9;
            }
            int var15 = (!(!var11) ? 45 : 25);
            Renderer2D.renderQuad(arg0, (var10[0] - 1.0f), (var10[1] - 1.0f), (var10[2] + 1.0f), (var10[3] + 1.0f), new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), var15));
            Renderer2D.renderOutline(arg0, (var10[0] - 1.0f), (var10[1] - 1.0f), (var10[2] + 1.0f), (var10[3] + 1.0f), new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), (!(!var12) ? 220 : 120)));
            if (!var11 && !var13) {
                break label_1;
            }
            Object var16 = (var8 + (!(!var12) ? "" : " (disabled)"));
            float var17 = var10[0];
            float var18 = ((var10[1] - ((float) LightningClient.FONT_MANAGER.getHeight())) - 2.0f);
            Renderer2D.renderQuad(arg0, (var17 - 1.0f), (var18 - 1.0f), ((var17 + ((float) LightningClient.FONT_MANAGER.getWidth(var16))) + 1.0f), (var18 + ((float) LightningClient.FONT_MANAGER.getHeight())), new Color(0, 0, 0, 180));
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var16, ((int) var17), ((int) var18), var7);
        }
        this.elementsFrame.render(arg0, arg1, arg2, arg3);
        FontManager.setHudRendering(false);
        break label_20;
        Object var19 = 70;
        FontManager.setHudRendering(false);
        throw var19;
        label_20:
    }

    private boolean insideElementsFrame(double arg0, double arg1) {
        if (((arg0 < ((double) this.elementsFrame.getX())) || (arg0 > ((double) (this.elementsFrame.getX() + this.elementsFrame.getWidth())))) || (arg1 < ((double) this.elementsFrame.getY()))) break label_0;
        return arg1 <= ((double) ((this.elementsFrame.getY() + this.elementsFrame.getTotalHeight()) + 4));
    }

    public boolean mouseClicked(MouseButtonEvent arg0, boolean arg1) {
        double var5 = arg0.x();
        double var3 = arg0.y();
        if (insideElementsFrame(var5, arg0.y())) {
            this.elementsFrame.mouseClicked(var5, var3, arg0.button());
            return true;
        }
        Object var7 = HudElementRegistry.getElements().entrySet().iterator();
        label_1:
        if (var7.hasNext()) {
            Object var8 = ((Map.Entry) var7.next());
            Object var9 = ((String) var8.getKey());
            Object var10 = ((HudElementRegistry.Element) var8.getValue());
            Object var11 = HudElementRegistry.getBounds(var9);
            if (var11 == null) {
                float var12 = var10.offset().getX();
                float var13 = var10.offset().getY();
                var tmp0 = new float[4];
                tmp0[0] = var12;
                tmp0[1] = var13;
                tmp0[2] = (var12 + 60.0f);
                tmp0[3] = (var13 + 15.0f);
                var11 = tmp0;
            }
            if (((var5 < ((double) var11[0])) || (var5 > ((double) var11[2]))) || (var3 < ((double) var11[1]))) break label_1;
            if (var3 > ((double) var11[3])) {
                break label_1;
            }
            if (arg0.button() == 1) {
                var10.enabled().setValue(!(var10.enabled().getValue()));
                break label_7;
            }
            if (arg0.button()) break label_7;
            this.draggingElement = var9;
            this.dragOffsetX = (((float) var5) - var10.offset().getX());
            this.dragOffsetY = (((float) var3) - var10.offset().getY());
            label_7:
            return true;
        }
        return super.mouseClicked(arg0, arg1);
    }

    public boolean mouseDragged(MouseButtonEvent arg0, double arg1, double arg2) {
        if (this.draggingElement != null) {
            Object var6 = ((HudElementRegistry.Element) HudElementRegistry.getElements().get(this.draggingElement));
            if (var6 != null) {
                Object var7 = var6.offset();
                var7.set((((float) arg0.x()) - this.dragOffsetX), (((float) arg0.y()) - this.dragOffsetY));
                HudElementRegistry.clamp(this.draggingElement, this.width, this.height);
            }
            return true;
        }
        this.elementsFrame.mouseDragged(arg0.x(), arg0.y(), arg0.button(), arg1, arg2);
        return super.mouseDragged(arg0, arg1, arg2);
    }

    public boolean mouseReleased(MouseButtonEvent arg0) {
        this.draggingElement = null;
        this.elementsFrame.mouseReleased(arg0.x(), arg0.y(), arg0.button());
        return super.mouseReleased(arg0);
    }

    public boolean mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        if (insideElementsFrame(arg0, arg1)) {
            this.elementsFrame.mouseScrolled(arg0, arg1, arg2, arg3);
            return true;
        }
        return super.mouseScrolled(arg0, arg1, arg2, arg3);
    }

    public boolean keyPressed(KeyEvent arg0) {
        int var2 = arg0.key();
        int var3 = arg0.scancode();
        int var4 = arg0.modifiers();
        this.elementsFrame.keyPressed(var2, var3, var4);
        if (var2 == 256 && !(BindButton.isAnyListening())) {
            onClose();
            return true;
        }
        if (var2 == 256) return true;
        return super.keyPressed(arg0);
    }

    public boolean charTyped(CharacterEvent arg0) {
        int var2 = ((char) arg0.codepoint());
        int var3 = 0;
        this.elementsFrame.charTyped(var2, var3);
        return super.charTyped(arg0);
    }

    public void onClose() {
        super.onClose();
        ((HUDEditorModule) LightningClient.MODULE_MANAGER.getModule(HUDEditorModule.class)).setToggled(false);
    }

    public boolean isPauseScreen() {
        return false;
    }


    class CustomCategoryButton extends CategoryButton implements ExpandableRow {
        private final List childButtons;
        private final CategorySetting category;
        private int revealHeight;

        public CustomCategoryButton(CategorySetting arg0, List arg1, Frame arg2, int arg3) {
            super(arg0, null, arg2, arg3);
            this.childButtons = new ArrayList();
            this.revealHeight = 0;
            this.category = arg0;
            Object var5 = arg1.iterator();
            while (var5.hasNext()) {
                Object var6 = ((Setting) var5.next());
                if ((var6 instanceof BooleanSetting)) {
                    Object var7 = ((BooleanSetting) var6);
                    this.childButtons.add(new BooleanButton(var7, arg2, arg3));
                    break label_0;
                }
                if ((var6 instanceof NumberSetting)) {
                    var7 = ((NumberSetting) var6);
                    this.childButtons.add(new NumberButton(var7, arg2, arg3));
                    break label_0;
                }
                if ((var6 instanceof CategorySetting)) {
                    var7 = ((CategorySetting) var6);
                    this.childButtons.add(new CategoryButton(var7, null, arg2, arg3));
                    break label_0;
                }
                if ((var6 instanceof BindSetting)) {
                    var7 = ((BindSetting) var6);
                    this.childButtons.add(new BindButton(var7, arg2, arg3));
                    break label_0;
                }
                if ((var6 instanceof ModeSetting)) {
                    var7 = ((ModeSetting) var6);
                    this.childButtons.add(new ModeButton(var7, arg2, arg3));
                    break label_0;
                }
                if ((var6 instanceof WhitelistSetting)) {
                    var7 = ((WhitelistSetting) var6);
                    this.childButtons.add(new WhitelistButton(var7, arg2, arg3));
                    break label_0;
                }
                if ((var6 instanceof StringSetting)) {
                    var7 = ((StringSetting) var6);
                    this.childButtons.add(new StringButton(var7, arg2, arg3));
                    break label_0;
                }
                if (!((var6 instanceof ColorSetting))) {
                    break label_0;
                }
                var7 = ((ColorSetting) var6);
                this.childButtons.add(new ColorButton(var7, arg2, arg3));
            }
        }

        public String getRowName() {
            return this.category.getTag();
        }

        public boolean isOpen() {
            return this.category.isOpen();
        }

        public float getOpenAmount() {
            return this.category.getOpenAmount();
        }

        public List getButtons() {
            return this.childButtons;
        }

        public void setRevealHeight(int arg0) {
            this.revealHeight = arg0;
        }

        public void setSearchQuery(String arg0) {
        }

        public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
            super.render(arg0, arg1, arg2, arg3);
            if (this.revealHeight > 0) {
                int var5 = (getY() + getHeight());
                arg0.enableScissor(getX(), var5, (getX() + getWidth()), (var5 + this.revealHeight));
                Object var6 = this.childButtons.iterator();
                while (var6.hasNext()) {
                    Object var7 = ((Button) var6.next());
                    if (!(var7.isVisible())) {
                        break label_0;
                    }
                    var7.render(arg0, arg1, arg2, arg3);
                }
                arg0.disableScissor();
            }
        }

        public void mouseClicked(double arg0, double arg1, int arg2) {
            super.mouseClicked(arg0, arg1, arg2);
            if (!(this.category.isOpen())) break label_2;
            Object var6 = this.childButtons.iterator();
            while (var6.hasNext()) {
                Object var7 = ((Button) var6.next());
                if (!(var7.isVisible())) {
                    break label_0;
                }
                var7.mouseClicked(arg0, arg1, arg2);
            }
            label_2:
        }

        public void mouseReleased(double arg0, double arg1, int arg2) {
            super.mouseReleased(arg0, arg1, arg2);
            if (!(this.category.isOpen())) break label_2;
            Object var6 = this.childButtons.iterator();
            while (var6.hasNext()) {
                Object var7 = ((Button) var6.next());
                if (!(var7.isVisible())) {
                    break label_0;
                }
                var7.mouseReleased(arg0, arg1, arg2);
            }
            label_2:
        }

        public void mouseDragged(double arg0, double arg1, int arg2, double arg3, double arg4) {
            super.mouseDragged(arg0, arg1, arg2, arg3, arg4);
            if (!(this.category.isOpen())) break label_2;
            Object var10 = this.childButtons.iterator();
            while (var10.hasNext()) {
                Object var11 = ((Button) var10.next());
                if (!(var11.isVisible())) {
                    break label_0;
                }
                var11.mouseDragged(arg0, arg1, arg2, arg3, arg4);
            }
            label_2:
        }

        public void keyPressed(int arg0, int arg1, int arg2) {
            super.keyPressed(arg0, arg1, arg2);
            if (!(this.category.isOpen())) break label_2;
            Object var4 = this.childButtons.iterator();
            while (var4.hasNext()) {
                Object var5 = ((Button) var4.next());
                if (!(var5.isVisible())) {
                    break label_0;
                }
                var5.keyPressed(arg0, arg1, arg2);
            }
            label_2:
        }

        public void charTyped(char arg0, int arg1) {
            super.charTyped(arg0, arg1);
            if (!(this.category.isOpen())) break label_2;
            Object var3 = this.childButtons.iterator();
            while (var3.hasNext()) {
                Object var4 = ((Button) var3.next());
                if (!(var4.isVisible())) {
                    break label_0;
                }
                var4.charTyped(arg0, arg1);
            }
            label_2:
        }

    }
}
