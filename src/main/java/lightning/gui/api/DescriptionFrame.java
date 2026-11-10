package lightning.gui.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.LightningSkin;
import lightning.managers.FontManager;
import lightning.modules.impl.core.ClickGuiModule;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DescriptionFrame {
    private String description;
    private int x;
    private int y;
    private int width;
    private int preferredWidth;
    private int preferredX;
    private int preferredY;
    private int originalHeaderHeight;
    private boolean rendered;
    private int height;
    private int dragX;
    private int dragY;
    private int textPadding;
    private boolean dragging;

    public DescriptionFrame(int arg0, int arg1, int arg2, int arg3) {
        this.description = "";
        this.dragX = 0;
        this.dragY = 0;
        this.textPadding = 3;
        this.dragging = false;
        this.x = arg0;
        this.preferredX = arg0;
        this.y = arg1;
        this.preferredY = arg1;
        this.width = arg2;
        this.preferredWidth = arg2;
        this.height = arg3;
        this.originalHeaderHeight = arg3;
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        if (!(!this.dragging)) {
            setX((arg1 - this.dragX));
            setY((arg2 - this.dragY));
        }
        this.rendered = false;
        Object var5 = LightningClient.CLICK_GUI;
        if (var5.isClosing()) break label_1;
        if ((System.currentTimeMillis() - var5.getSlideAnim().getStartTime()) >= (320L + (((long) Math.max(0, (var5.getFrames().size() - 1))) * 35L))) break label_2;
        label_1:
        return;
        label_2:
        float var6 = var5.getAppliedScale();
        if ((var6 <= 0.0f) || (!(Float.isFinite(var6)))) {
            return;
        }
        int var7 = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int var8 = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int var9 = ((int) Math.floor(((double) (((float) var7) / var6))));
        int var10 = ((int) Math.floor(((double) (((float) var8) / var6))));
        Object var11 = new ArrayList();
        Object var12 = var5.getFrames().iterator();
        while (var12.hasNext()) {
            Object var13 = ((Frame) var12.next());
            int var14 = Math.round((((float) var13.getBaseContentHeight()) * var13.getOpenAnim().get((var13.isOpen() ? 1.0f : 0.0f))));
            if (var14 > 0) {
            } else {
            }
            new DescriptionLayout.Rect.add(new DescriptionLayout.Rect);
        }
        var12 = var5.getSearchFrame();
        if (var12.isVisible()) {
            var11.add(new DescriptionLayout.Rect(((var7 - var12.getWidth()) / 2), ((var8 - var12.getHeight()) - 8), var12.getWidth(), var12.getHeight()));
        }
        var13 = (this.description.isEmpty() ? "Hover over a module or setting to view details.\nCtrl+F to search" : this.description);
        var14 = Math.max(1, LightningClient.FONT_MANAGER.getHeight());
        if (this.description.isEmpty()) {
        } else {
        }
        Objects.requireNonNull(LightningClient.FONT_MANAGER);
        Object var15 = DescriptionLayout.create("", GuiDescriptionTranslations.translate(this.description), "\u2139 Description", "[Ctrl+F]", this.textPadding, var14, this.originalHeaderHeight, this.preferredWidth, 2147483647, 2147483647, (var9 - 8), (var10 - 8), List.of(), LightningClient.FONT_MANAGER::getWidth);
        if (var15 == null) return;
        this.x = var15.bounds().x();
        this.y = var15.bounds().y();
        this.width = var15.bounds().width();
        this.height = var15.headerHeight();
        if (!(!this.dragging)) {
            this.dragX = (arg1 - this.x);
            this.dragY = (arg2 - this.y);
        }
        this.rendered = true;
        Object var16 = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        int var17 = var16.glowAccent.getValue();
        LightningSkin.drawFrameTop(arg0, ((float) this.x), ((float) this.y), ((float) (this.x + this.width)), ((float) (this.y + this.height)), ClickGuiScreen.getButtonColor(this.y, 55));
        if (!(!var17)) {
            LightningSkin.drawNoOp(arg0, ((float) this.x), ((float) this.y), ((float) (this.x + this.width)), ((float) (this.y + 1)), ClickGuiScreen.getButtonColor(this.y, 220));
        }
        Object var18 = ClickGuiScreen.getButtonColor(this.y, 220);
        LightningSkin.drawNoOp(arg0, ((float) this.x), ((float) ((this.y + this.height) - 1)), ((float) (this.x + this.width)), ((float) (this.y + this.height)), var18);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "\u2139 Description", (this.x + this.textPadding), (this.y + 2), Color.WHITE);
        Object var19 = "[Ctrl+F]";
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var19, (((this.x + this.width) - this.textPadding) - LightningClient.FONT_MANAGER.getWidth(var19)), (this.y + 2), new Color(130, 130, 140));
        int var20 = (var15.totalHeight() - this.height);
        LightningSkin.drawFrameBottom(arg0, ((float) this.x), ((float) (this.y + this.height)), ((float) (this.x + this.width)), ((float) (this.y + var15.totalHeight())), new Color(14, 14, 14, 195));
        int var21 = 0;
        Object var22 = var15.english().iterator();
        while (var22.hasNext()) {
            Object var23 = ((String) var22.next());
            if (this.description.isEmpty()) {
                if (var23.equals("Ctrl+F to search")) {
                    break label_23;
                }
                break label_23;
            }
            label_23:
            Object var24 = Color.WHITE;
            var21++;
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var23, (this.x + this.textPadding), (((this.y + this.height) + this.textPadding) + (var14 * var21)), var24);
        }
        var22 = var15.vietnamese().iterator();
        while (var22.hasNext()) {
            var23 = ((String) var22.next());
            var21++;
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var23, (this.x + this.textPadding), (((this.y + this.height) + this.textPadding) + (var14 * var21)), new Color(190, 190, 200));
        }
        var22 = ClickGuiScreen.getButtonColor(this.y, 130);
        LightningSkin.drawOutline(arg0, ((float) this.x), ((float) this.y), ((float) (this.x + this.width)), ((float) ((this.y + this.height) + var20)), var22);
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (isHovering(arg0, arg1) && arg2 == 0) {
            this.dragging = true;
            this.dragX = ((int) (arg0 - ((double) getX())));
            this.dragY = ((int) (arg1 - ((double) getY())));
        }
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        if (arg2 == 0) {
            this.dragging = false;
        }
    }

    public boolean isHovering(double arg0, double arg1) {
        if ((((!this.rendered) || (((double) this.x) > arg0)) || (((double) this.y) > arg1)) || (((double) (this.x + this.width)) <= arg0)) break label_0;
        return ((double) (this.y + this.height)) > arg1;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public int getX() {
        return this.x;
    }

    @Generated
    public int getY() {
        return this.y;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public int getDragX() {
        return this.dragX;
    }

    @Generated
    public int getDragY() {
        return this.dragY;
    }

    @Generated
    public int getTextPadding() {
        return this.textPadding;
    }

    @Generated
    public boolean isDragging() {
        return this.dragging;
    }

    @Generated
    public void setDescription(String arg0) {
        this.description = arg0;
    }

    @Generated
    public void setX(int arg0) {
        this.x = arg0;
        this.preferredX = arg0;
    }

    @Generated
    public void setY(int arg0) {
        this.y = arg0;
        this.preferredY = arg0;
    }

    @Generated
    public void setWidth(int arg0) {
        this.width = arg0;
        this.preferredWidth = arg0;
    }

    @Generated
    public void setHeight(int arg0) {
        this.height = arg0;
        this.originalHeaderHeight = arg0;
    }

    @Generated
    public void setDragX(int arg0) {
        this.dragX = arg0;
    }

    @Generated
    public void setDragY(int arg0) {
        this.dragY = arg0;
    }

    @Generated
    public void setTextPadding(int arg0) {
        this.textPadding = arg0;
    }

    @Generated
    public void setDragging(boolean arg0) {
        this.dragging = arg0;
    }

}
