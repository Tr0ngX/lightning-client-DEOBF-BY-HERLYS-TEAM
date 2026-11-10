package lightning.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lightning.LightningClient;
import lightning.managers.ImageManager;
import lightning.settings.impl.ImageSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.system.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class ImageSelectScreen extends Screen {
    private static final int COL_WIDTH = 175;
    private static final int COL_GAP = 14;
    private static final int HEADER_H = 15;
    private static final int ROW_H = 18;
    private static final int SLIDE_DURATION_MS = 320;
    private static final float SLIDE_DISTANCE = 160.0f;
    private final ImageSetting setting;
    private final Screen parent;
    private List allFiles;
    private String query;
    private String lastQuery;
    private long queryChangeTime;
    private float scrollList;
    private float targetScrollList;
    private String selectedPreviewName;
    private ImageManager.ImageEntry currentPreviewEntry;
    private final Animation slideAnim;
    private boolean closing;
    private final Timer lineTimer;
    private boolean showLine;
    private int colX1;
    private int colX2;
    private int colY;
    private int colHeight;
    private long lastClickTime;
    private String lastClickedItem;

    public ImageSelectScreen(ImageSetting arg0, Screen arg1) {
        super(Component.literal("night-image-select"));
        this.allFiles = new ArrayList();
        this.query = "";
        this.lastQuery = "";
        this.queryChangeTime = System.currentTimeMillis();
        this.scrollList = 0.0f;
        this.targetScrollList = 0.0f;
        this.selectedPreviewName = null;
        this.currentPreviewEntry = null;
        this.slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
        this.closing = false;
        this.lineTimer = new Timer();
        this.showLine = false;
        this.lastClickTime = 0L;
        this.lastClickedItem = null;
        this.setting = arg0;
        this.parent = arg1;
        this.selectedPreviewName = arg0.getValue();
        refreshFiles();
    }

    public void refreshFiles() {
        if (LightningClient.IMAGE_MANAGER != null) {
            this.allFiles = LightningClient.IMAGE_MANAGER.getImageFiles();
            if (this.selectedPreviewName != null) {
                if (!(this.selectedPreviewName.equals("None"))) break label_1;
            }
            if (this.allFiles.isEmpty()) break label_1;
            this.selectedPreviewName = ((File) this.allFiles.get(0)).getName();
            label_1:
            updatePreview();
        }
    }

    private void updatePreview() {
        if (this.selectedPreviewName != null && !(this.selectedPreviewName.equalsIgnoreCase("None")) && LightningClient.IMAGE_MANAGER != null) {
        } else {
        }
        LightningClient.IMAGE_MANAGER.getImageEntry(this.selectedPreviewName).currentPreviewEntry = null;
    }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        Renderer2D.renderQuad(arg0, 0.0f, 0.0f, ((float) this.width), ((float) this.height), new Color(10, 10, 10, 160));
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
        int var6 = 364;
        this.colX1 = ((this.width - var6) / 2);
        this.colX2 = ((this.colX1 + 175) + 14);
        this.colY = Math.max(15, ((this.height - 240) / 2));
        this.colHeight = Math.min(260, (this.height - 30));
        arg0.pose().pushMatrix();
        arg0.pose().translate(0.0f, ((1.0f - var5) * -160.0f));
        if (this.query.isEmpty()) {
        } else {
        }
        Object var7 = filterFiles(this.allFiles, this.query);
        renderListColumn(arg0, arg1, arg2, this.colX1, var7);
        renderPreviewColumn(arg0, arg1, arg2, this.colX2);
        arg0.pose().popMatrix();
    }

    private List filterFiles(List arg0, String arg1) {
        Object var3 = arg1.toLowerCase().trim();
        return arg0.stream().filter((arg1) -> arg1.getName().toLowerCase().contains(arg0)).toList();
    }

    private void renderListColumn(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, List arg4) {
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 175)), ((float) (this.colY + 15)), new Color(21, 21, 21, 220));
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) ((this.colY + 15) - 1)), ((float) (arg3 + 175)), ((float) (this.colY + 15)), ClickGuiScreen.getButtonColor(this.colY, 200));
        if (!(this.query.isEmpty())) break label_1;
        Object var6 = "Images (search...)";
        int var7 = LightningClient.FONT_MANAGER.getWidth(" ");
        if (!(!this.showLine)) {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "|", (arg3 + 3), (this.colY + 3), Color.WHITE);
        }
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var6, ((arg3 + 3) + var7), (this.colY + 3), Color.GRAY);
        break label_4;
        label_1:
        var6 = (this.query + (!(!this.showLine) ? "|" : " "));
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var6, (arg3 + 3), (this.colY + 3), Color.WHITE);
        label_4:
        var6 = (this.colY + 15);
        var7 = (this.colHeight - 15);
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) var6), ((float) (arg3 + 175)), ((float) (var6 + var7)), new Color(16, 16, 16, 190));
        Renderer2D.renderOutline(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 175)), ((float) (var6 + var7)), ClickGuiScreen.getButtonColor(this.colY, 120));
        if (arg4.isEmpty()) {
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "No images found in:", (arg3 + 10), (var6 + 20), Color.GRAY);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Lightning/images", (arg3 + 10), (var6 + 32), ClickGuiScreen.getButtonColor(var6, 220));
            int var8 = (var6 + 55);
            int var9 = 16;
            if (((arg1 < (arg3 + 10)) || (arg1 > ((arg3 + 175) - 10))) || (arg2 < var8)) break label_5;
            int var10 = (arg2 <= (var8 + var9) ? 1 : 0);
            if (!(!var10)) {
            } else {
            }
            Object var11 = new Color(31, 31, 31, 200);
            Renderer2D.renderQuad(arg0, ((float) (arg3 + 10)), ((float) var8), ((float) ((arg3 + 175) - 10)), ((float) (var8 + var9)), var11);
            Renderer2D.renderOutline(arg0, ((float) (arg3 + 10)), ((float) var8), ((float) ((arg3 + 175) - 10)), ((float) (var8 + var9)), ClickGuiScreen.getButtonColor(var8, 150));
            int var12 = LightningClient.FONT_MANAGER.getWidth("Open Folder");
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "Open Folder", (arg3 + ((175 - var12) / 2)), (var8 + 4), Color.WHITE);
            return;
        }
        arg0.enableScissor(arg3, var6, (arg3 + 175), (var6 + var7));
        var8 = ((var7 / 18) + 2);
        var9 = ((int) this.scrollList);
        var10 = 0;
        while (var10 < var8) {
            var11 = (var9 + var10);
            if (var11 < 0) break label_13;
            if (var11 >= arg4.size()) {
                break label_13;
            }
            var12 = ((File) arg4.get(var11));
            int var13 = (var6 + Math.round((((float) (var10 * 18)) - ((this.scrollList - ((float) var9)) * 18.0f))));
            if ((var13 + 18) < var6) break label_13;
            if (var13 > (var6 + var7)) {
                break label_13;
            }
            renderRow(arg0, arg1, arg2, arg3, var13, var12);
            label_13:
            var10++;
        }
        arg0.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3, int arg4, File arg5) {
        Object var9 = arg5.getName();
        int var10 = var9.equals(this.selectedPreviewName);
        int var11 = var9.equals(this.setting.getValue());
        if (((arg1 < arg3) || (arg1 >= (arg3 + 175))) || (arg2 < arg4)) break label_0;
        var tmp0 = (arg2 < ((arg4 + 18) - 1) ? 1 : 0);
        int var8 = tmp0;
        int var12 = tmp0;
        if (!(!var10)) {
            break label_4;
        }
        if (!(!var8)) {
            break label_4;
        }
        label_4:
        Object var7 = new Color(0, 0, 0, 0);
        if (var7.getAlpha() > 0) {
            Renderer2D.renderQuad(arg0, ((float) arg3), ((float) arg4), ((float) (arg3 + 175)), ((float) ((arg4 + 18) - 1)), var7);
        }
        if (!(!var11)) {
            Renderer2D.renderQuad(arg0, ((float) arg3), ((float) arg4), ((float) (arg3 + 3)), ((float) ((arg4 + 18) - 1)), ClickGuiScreen.getButtonColor(arg4, 255));
        }
        if (!(!var11)) {
            break label_9;
        }
        if (!(!var10)) {
            break label_9;
        }
        label_9:
        Object var13 = Color.WHITE;
        Object var14 = var9;
        if (LightningClient.FONT_MANAGER.getWidth(((String) var14)) > 155) {
            while (LightningClient.FONT_MANAGER.getWidth((((String) var14) + "...")) > 155 && ((String) var14).length() > 3) {
                var14 = ((String) var14).substring(0, (((String) var14).length() - 1));
            }
            var14 = (((String) var14) + "...");
        }
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, ((String) var14), (arg3 + 8), (arg4 + ((18 - LightningClient.FONT_MANAGER.getHeight()) / 2)), var13);
    }

    private void renderPreviewColumn(GuiGraphicsExtractor arg0, int arg1, int arg2, int arg3) {
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 175)), ((float) (this.colY + 15)), new Color(21, 21, 21, 220));
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) ((this.colY + 15) - 1)), ((float) (arg3 + 175)), ((float) (this.colY + 15)), ClickGuiScreen.getButtonColor(this.colY, 200));
        if (this.selectedPreviewName != null) {
        } else {
        }
        Object var5 = ("Preview" + "");
        if (LightningClient.FONT_MANAGER.getWidth(((String) var5)) > 165) {
            var5 = "Preview";
        }
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, ((String) var5), (arg3 + 5), (this.colY + 3), Color.WHITE);
        int var6 = (this.colY + 15);
        int var7 = (this.colHeight - 15);
        Renderer2D.renderQuad(arg0, ((float) arg3), ((float) var6), ((float) (arg3 + 175)), ((float) (var6 + var7)), new Color(16, 16, 16, 190));
        Renderer2D.renderOutline(arg0, ((float) arg3), ((float) this.colY), ((float) (arg3 + 175)), ((float) (var6 + var7)), ClickGuiScreen.getButtonColor(this.colY, 120));
        if (this.currentPreviewEntry != null && this.currentPreviewEntry.texture != null && this.currentPreviewEntry.identifier != null) {
            int var10 = 12;
            int var11 = (arg3 + var10);
            int var12 = (var6 + var10);
            int var13 = (175 - (var10 * 2));
            int var14 = ((var7 - (var10 * 2)) - 28);
            Renderer2D.renderQuad(arg0, ((float) (var11 - 1)), ((float) (var12 - 1)), ((float) ((var11 + var13) + 1)), ((float) ((var12 + var14) + 1)), new Color(5, 5, 5, 220));
            Renderer2D.renderOutline(arg0, ((float) (var11 - 1)), ((float) (var12 - 1)), ((float) ((var11 + var13) + 1)), ((float) ((var12 + var14) + 1)), new Color(41, 41, 41, 150));
            float var15 = (((float) this.currentPreviewEntry.width) / ((float) Math.max(1, this.currentPreviewEntry.height)));
            float var16 = (((float) var13) / ((float) var14));
            if (var15 > var16) {
                int var9 = var13;
                int var8 = Math.max(1, Math.round((((float) var13) / var15)));
            } else {
                var8 = var14;
                var9 = Math.max(1, Math.round((((float) var14) * var15)));
            }
            int var17 = (var11 + ((var13 - var9) / 2));
            int var18 = (var12 + ((var14 - var8) / 2));
            arg0.blit(RenderPipelines.GUI_TEXTURED, this.currentPreviewEntry.identifier, var17, var18, 0.0f, 0.0f, var9, var8, var9, var8, var9, var8, -1);
            Object var19 = (this.currentPreviewEntry.width + " x " + this.currentPreviewEntry.height + " px");
            int var20 = LightningClient.FONT_MANAGER.getWidth(var19);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var19, (arg3 + ((175 - var20) / 2)), ((var12 + var14) + 4), Color.LIGHT_GRAY);
            Object var21 = "Double-click item to select";
            int var22 = LightningClient.FONT_MANAGER.getWidth(var21);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var21, (arg3 + ((175 - var22) / 2)), ((var12 + var14) + 15), Color.GRAY);
        } else {
            var8 = "Select an image on the left";
            var9 = LightningClient.FONT_MANAGER.getWidth(var8);
            LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var8, (arg3 + ((175 - var9) / 2)), ((var6 + (var7 / 2)) - 6), Color.GRAY);
        }
    }

    public boolean mouseClicked(MouseButtonEvent arg0, boolean arg1) {
        double var3 = arg0.x();
        double var5 = arg0.y();
        if (arg0.button()) return super.mouseClicked(arg0, arg1);
        int var7 = (this.colY + 15);
        int var8 = (this.colHeight - 15);
        if (((((!(this.allFiles.isEmpty())) || (var3 < ((double) (this.colX1 + 10)))) || (var3 > ((double) ((this.colX1 + 175) - 10)))) || (var5 < ((double) (var7 + 55)))) || (var5 > ((double) (var7 + 71)))) break label_5;
        try {
            Desktop.getDesktop().open(ImageManager.IMAGES_DIR.toFile());
        } catch (Exception ex3) {
            Object var9 = ex3;
        }
        return true;
        label_5:
        if ((((var3 < ((double) this.colX1)) || (var3 >= ((double) (this.colX1 + 175)))) || (var5 < ((double) var7))) || (var5 >= ((double) (var7 + var8)))) break label_12;
        if (this.query.isEmpty()) {
        } else {
        }
        var9 = filterFiles(this.allFiles, this.query);
        int var10 = ((int) (var5 - ((double) var7)));
        int var11 = (((int) this.scrollList) + (var10 / 18));
        if ((var11 < 0) || (var11 >= var9.size())) break label_12;
        Object var12 = ((File) var9.get(var11));
        Object var13 = var12.getName();
        long var14 = System.currentTimeMillis();
        if (!arg1) {
            if (var13.equals(this.lastClickedItem) && (var14 - this.lastClickTime) < 350L) {
            }
            this.setting.setValue(var13);
            if (LightningClient.IMAGE_MANAGER != null) {
                LightningClient.IMAGE_MANAGER.setCurrentActiveImage(var13);
            }
            requestClose();
            return true;
        }
        this.selectedPreviewName = var13;
        this.lastClickedItem = var13;
        this.lastClickTime = var14;
        this.setting.setValue(var13);
        if (LightningClient.IMAGE_MANAGER != null) {
            LightningClient.IMAGE_MANAGER.setCurrentActiveImage(var13);
        }
        updatePreview();
        return true;
        label_12:
        return super.mouseClicked(arg0, arg1);
    }

    public boolean mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        if (arg0 >= ((double) this.colX1) && arg0 < ((double) (this.colX1 + 175))) {
            if (this.query.isEmpty()) {
            } else {
            }
            Object var9 = filterFiles(this.allFiles, this.query);
            int var10 = (this.colHeight - 15);
            float var11 = Math.max(0.0f, (((float) var9.size()) - (((float) var10) / 18.0f)));
            this.targetScrollList = Math.max(0.0f, Math.min((this.targetScrollList - ((float) arg3)), var11));
            this.scrollList = (this.scrollList + ((this.targetScrollList - this.scrollList) * 0.5f));
            return true;
        }
        return super.mouseScrolled(arg0, arg1, arg2, arg3);
    }

    public boolean keyPressed(KeyEvent arg0) {
        int var2 = arg0.key();
        if (var2 == 256) {
            requestClose();
            return true;
        }
        if (var2 == 259 && !(this.query.isEmpty())) {
            this.query = this.query.substring(0, (this.query.length() - 1));
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.keyPressed(arg0);
    }

    public boolean charTyped(CharacterEvent arg0) {
        int var2 = ((char) arg0.codepoint());
        if (!(Character.isISOControl(var2))) {
            this.query = (this.query + var2);
            this.scrollList = 0.0f;
            this.targetScrollList = 0.0f;
            return true;
        }
        return super.charTyped(arg0);
    }

    public boolean isPauseScreen() {
        return false;
    }

}
