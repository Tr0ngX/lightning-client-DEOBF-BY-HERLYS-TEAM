package lightning.managers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lightning.LightningClient;
import lightning.modules.impl.core.FontModule;
import lightning.modules.impl.core.HUDModule;
import lightning.utils.IMinecraft;
import lightning.utils.color.ColorUtils;
import lightning.utils.font.FontRenderer;
import lightning.utils.graphics.Renderer3D;
import lombok.Generated;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public class FontManager implements IMinecraft {
    private FontRenderer fontRenderer;
    private static final ThreadLocal HUD_RENDERING;
    private static final ThreadLocal CLIENT_TEXT_DEPTH;

    public static void pushClientTextRendering() {
        CLIENT_TEXT_DEPTH.set((((Integer) CLIENT_TEXT_DEPTH.get()).intValue() + 1));
    }

    public static void popClientTextRendering() {
        int var0 = (((Integer) CLIENT_TEXT_DEPTH.get()).intValue() - 1);
        CLIENT_TEXT_DEPTH.set(Math.max(0, var0));
    }

    public static boolean isClientTextRendering() {
        return ((Integer) CLIENT_TEXT_DEPTH.get()).intValue() > 0;
    }

    public static void setHudRendering(boolean arg0) {
        HUD_RENDERING.set(arg0);
    }

    public static boolean isHudRendering() {
        return ((Boolean) HUD_RENDERING.get()).booleanValue();
    }

    public boolean isHudGlowEnabled() {
        if (LightningClient.MODULE_MANAGER != null) {
            break label_1;
        }
        label_1:
        Object var1 = null;
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var2 = null;
        if ((var1 == null) || (!(var1.isToggled()))) break label_4;
        int var3 = (var1.glow.getValue() ? 1 : 0);
        if ((var2 == null) || (!(var2.isToggled()))) break label_6;
        int var4 = (var2.textGlow.getValue() ? 1 : 0);
        if (!var3) {
            if (!(!var4)) {
            }
        } else {
        }
        return false;
    }

    public boolean isCustomFontActive() {
        return useCustomFont(false);
    }

    private boolean useCustomFont() {
        return useCustomFont(false);
    }

    private boolean useCustomFont(boolean arg0) {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var3 = null;
        Object var4 = null;
        if ((var3 == null) || (!(var3.isToggled()))) {
            return false;
        }
        if (!(var3.customFont.getValue()) && !arg0) {
            if (isHudRendering() && isHudGlowEnabled()) {
            }
        } else {
        }
        int var2 = 0;
        int var5 = 0;
        if (this.fontRenderer == null && !(!var2)) {
            var3.updateFontRenderer();
        }
        if (this.fontRenderer == null) break label_8;
        return !(!var2);
    }

    private boolean shadowEnabled() {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var1 = null;
        if (var1 == null) break label_2;
        return !(var1.shadowMode.getValue().equalsIgnoreCase("None"));
    }

    public void drawText(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, Color arg4) {
        drawText(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void drawText(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, Color arg4, boolean arg5) {
        pushClientTextRendering();
        if (!arg5) {
            if (isHudRendering() && isHudGlowEnabled()) {
            }
        } else {
        }
        int var7 = 0;
        int var8 = 0;
        if (useCustomFont(var7)) {
            if (!(!var7)) {
                this.fontRenderer.drawString(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, true, false);
            }
            this.fontRenderer.drawString(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, false, true);
        } else {
            arg0.text(mc.font, arg1, arg2, arg3, arg4.getRGB(), false);
        }
        popClientTextRendering();
        break label_9;
        Object var9 = 1;
        popClientTextRendering();
        throw var9;
        label_9:
    }

    public void drawTextWithShadow(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, Color arg4) {
        drawTextWithShadow(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void drawTextWithShadow(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, Color arg4, boolean arg5) {
        pushClientTextRendering();
        if (arg5) break label_1;
        if (isHudRendering() && isHudGlowEnabled()) {
            label_1:
        } else {
        }
        int var7 = 0;
        int var8 = 0;
        if (useCustomFont(var7)) {
            if (!(!var7)) {
                this.fontRenderer.drawString(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, true, false);
            }
            if (shadowEnabled()) {
                this.fontRenderer.drawString(arg0, arg1, (((float) arg2) + getShadowOffset()), (((float) arg3) + getShadowOffset()), arg4.getRGB(), true, false, true);
            }
            this.fontRenderer.drawString(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, false, true);
        } else {
            arg0.text(mc.font, arg1, arg2, arg3, arg4.getRGB(), shadowEnabled());
        }
        popClientTextRendering();
        break label_10;
        Object var9 = 1;
        popClientTextRendering();
        throw var9;
        label_10:
    }

    public void drawText(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, int arg2, int arg3, Color arg4) {
        drawText(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void drawText(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, int arg2, int arg3, Color arg4, boolean arg5) {
        pushClientTextRendering();
        if (!arg5) {
            if (isHudRendering() && isHudGlowEnabled()) {
            }
        } else {
        }
        int var7 = 0;
        int var8 = 0;
        if (useCustomFont(var7)) {
            if (!(!var7)) {
                this.fontRenderer.drawText(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, true, false);
            }
            this.fontRenderer.drawText(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, false, true);
        } else {
            arg0.text(mc.font, arg1, arg2, arg3, arg4.getRGB(), false);
        }
        popClientTextRendering();
        break label_9;
        Object var9 = 1;
        popClientTextRendering();
        throw var9;
        label_9:
    }

    public void drawTextWithShadow(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, int arg2, int arg3, Color arg4) {
        drawTextWithShadow(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void drawTextWithShadow(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, int arg2, int arg3, Color arg4, boolean arg5) {
        pushClientTextRendering();
        if (arg5) break label_1;
        if (isHudRendering() && isHudGlowEnabled()) {
            label_1:
        } else {
        }
        int var7 = 0;
        int var8 = 0;
        if (useCustomFont(var7)) {
            if (!(!var7)) {
                this.fontRenderer.drawText(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, true, false);
            }
            if (shadowEnabled()) {
                this.fontRenderer.drawText(arg0, arg1, (((float) arg2) + getShadowOffset()), (((float) arg3) + getShadowOffset()), arg4.getRGB(), true, false, true);
            }
            this.fontRenderer.drawText(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false, false, true);
        } else {
            arg0.text(mc.font, arg1, arg2, arg3, arg4.getRGB(), shadowEnabled());
        }
        popClientTextRendering();
        break label_10;
        Object var9 = 1;
        popClientTextRendering();
        throw var9;
        label_10:
    }

    public void drawTextWithShadow(PoseStack arg0, String arg1, int arg2, int arg3, Color arg4) {
        drawTextWithShadow(arg0, arg1, ((float) arg2), ((float) arg3), arg4, false);
    }

    public void drawTextWithShadow(PoseStack arg0, String arg1, int arg2, int arg3, Color arg4, boolean arg5) {
        drawTextWithShadow(arg0, arg1, ((float) arg2), ((float) arg3), arg4, arg5);
    }

    public void drawTextWithShadow(PoseStack arg0, String arg1, float arg2, float arg3, Color arg4) {
        drawTextWithShadow(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void drawTextWithShadow(PoseStack arg0, String arg1, float arg2, float arg3, Color arg4, boolean arg5) {
        pushClientTextRendering();
        if (arg5) break label_1;
        if ((!(isHudRendering())) || (!(isHudGlowEnabled()))) break label_2;
        label_1:
        break label_3;
        label_2:
        label_3:
        int var7 = 0;
        int var8 = 0;
        if (useCustomFont(var7)) {
            if (!(!var7)) {
                this.fontRenderer.drawString(arg0, arg1, arg2, arg3, arg4.getRGB(), false, true, false);
            }
            if (shadowEnabled() && !arg5) {
                this.fontRenderer.drawString(arg0, arg1, (arg2 + getShadowOffset()), (arg3 + getShadowOffset()), arg4.getRGB(), true, false, true);
            }
            this.fontRenderer.drawString(arg0, arg1, arg2, arg3, arg4.getRGB(), false, false, true);
            popClientTextRendering();
            break label_22;
        }
        if (!(shadowEnabled())) break label_8;
        Object var9 = mc.font.prepareText(arg1, arg2, arg3, arg4.getRGB(), !arg5, 0);
        Object var10 = new HashMap();
        Object var11 = new HashMap();
        var9.visit(new FontManager.1(this, var11, var10, arg0));
        Object var12 = var11.entrySet().iterator();
        while (var12.hasNext()) {
            Object var13 = ((Map.Entry) var12.next());
            Object var14 = ((BufferBuilder) var13.getValue()).build();
            Renderer3D.draw(((RenderType) var13.getKey()), var14);
        }
        var12 = var10.values().iterator();
        while (var12.hasNext()) {
            var13 = ((ByteBufferBuilder) var12.next());
            var13.close();
        }
        break label_19;
        Object var15 = 1;
        Object var16 = var10.values().iterator();
        while (var16.hasNext()) {
            Object var17 = ((ByteBufferBuilder) var16.next());
            var17.close();
        }
        throw var15;
        label_19:
        popClientTextRendering();
        break label_22;
        Object var18 = null;
        popClientTextRendering();
        throw var18;
        label_22:
    }

    public void drawText(PoseStack arg0, String arg1, int arg2, int arg3, Color arg4) {
        drawText(arg0, arg1, ((float) arg2), ((float) arg3), arg4, false);
    }

    public void drawText(PoseStack arg0, String arg1, int arg2, int arg3, Color arg4, boolean arg5) {
        drawText(arg0, arg1, ((float) arg2), ((float) arg3), arg4, arg5);
    }

    public void drawText(PoseStack arg0, String arg1, float arg2, float arg3, Color arg4) {
        drawText(arg0, arg1, arg2, arg3, arg4, false);
    }

    public void drawText(PoseStack arg0, String arg1, float arg2, float arg3, Color arg4, boolean arg5) {
        pushClientTextRendering();
        if (arg5) break label_1;
        if (isHudRendering() && isHudGlowEnabled()) {
            label_1:
        } else {
        }
        int var7 = 0;
        int var8 = 0;
        if (useCustomFont(var7)) {
            if (!(!var7)) {
                this.fontRenderer.drawString(arg0, arg1, arg2, arg3, arg4.getRGB(), false, true, false);
            }
            this.fontRenderer.drawString(arg0, arg1, arg2, arg3, arg4.getRGB(), false, false, true);
            popClientTextRendering();
            break label_19;
        }
        Object var9 = mc.font.prepareText(arg1, arg2, arg3, arg4.getRGB(), false, 0);
        Object var10 = new HashMap();
        Object var11 = new HashMap();
        var9.visit(new FontManager.2(this, var11, var10, arg0));
        Object var12 = var11.entrySet().iterator();
        while (var12.hasNext()) {
            Object var13 = ((Map.Entry) var12.next());
            Object var14 = ((BufferBuilder) var13.getValue()).build();
            Renderer3D.draw(((RenderType) var13.getKey()), var14);
        }
        var12 = var10.values().iterator();
        while (var12.hasNext()) {
            var13 = ((ByteBufferBuilder) var12.next());
            var13.close();
        }
        break label_16;
        Object var15 = 1;
        Object var16 = var10.values().iterator();
        while (var16.hasNext()) {
            Object var17 = ((ByteBufferBuilder) var16.next());
            var17.close();
        }
        throw var15;
        label_16:
        popClientTextRendering();
        break label_19;
        Object var18 = null;
        popClientTextRendering();
        throw var18;
        label_19:
    }

    public void drawTextWithOutline(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, Color arg4, Color arg5) {
        if (useCustomFont()) {
            this.fontRenderer.drawString(arg0, FontRenderer.stripControlCodes(arg1), (((float) arg2) + 0.5f), (((float) arg3) - 0.5f), arg5.getRGB(), false);
            this.fontRenderer.drawString(arg0, FontRenderer.stripControlCodes(arg1), (((float) arg2) - 0.5f), (((float) arg3) + 0.5f), arg5.getRGB(), false);
            this.fontRenderer.drawString(arg0, FontRenderer.stripControlCodes(arg1), (((float) arg2) + 0.5f), (((float) arg3) + 0.5f), arg5.getRGB(), false);
            this.fontRenderer.drawString(arg0, FontRenderer.stripControlCodes(arg1), (((float) arg2) - 0.5f), (((float) arg3) - 0.5f), arg5.getRGB(), false);
            this.fontRenderer.drawString(arg0, arg1, ((float) arg2), ((float) arg3), arg4.getRGB(), false);
        } else {
            int var7 = -1;
            while (var7 <= 1) {
                int var8 = -1;
                while (var8 <= 1) {
                    if (!var7 && !var8) {
                    } else {
                        arg0.text(mc.font, arg1, (arg2 + var7), (arg3 + var8), arg5.getRGB(), false);
                    }
                    var8++;
                }
                var7++;
            }
            arg0.text(mc.font, arg1, arg2, arg3, arg4.getRGB(), false);
        }
    }

    public void drawRainbowString(GuiGraphicsExtractor arg0, String arg1, int arg2, int arg3, long arg4) {
        Object var7 = Component.empty();
        var tmp0 = new int[1];
        tmp0[0] = 0;
        Object var8 = tmp0;
        Component.literal(arg1).getVisualOrderText().accept((arg3, arg4, arg5) -> {
    Object var7 = Component.empty();
    if (arg4.getColor() == null) {
        long var8 = (((long) arg0[0]) * arg1);
        Object var10 = ColorUtils.getOffsetRainbow(var8);
        var7.append(Component.literal(String.valueOf(Character.toChars(arg5))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(var10.getRGB()))));
    } else {
        var7.append(Component.literal(String.valueOf(Character.toChars(arg5))).setStyle(arg4));
    }
    arg2.append(var7);
    arg0[0] = (arg0[0] + 1);
    return true;
});
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, var7.getVisualOrderText(), arg2, arg3, Color.WHITE);
    }

    public int getWidth(String arg0) {
        if (useCustomFont()) return ((int) this.fontRenderer.getTextWidth(arg0));
        return mc.font.width(arg0);
    }

    public int getHeight() {
        if (useCustomFont()) {
            if (LightningClient.MODULE_MANAGER != null) {
            } else {
            }
            Object var1 = null;
            if (var1 != null) {
            } else {
            }
            int var2 = 0;
            return (((int) this.fontRenderer.getHeight()) + var2);
        }
        Objects.requireNonNull(mc.font);
        return 9;
    }

    public float getShadowOffset() {
        if (LightningClient.MODULE_MANAGER != null) {
        } else {
        }
        Object var1 = null;
        Object var2 = null;
        if ((var1 == null) || (var1.shadowMode.getValue().equalsIgnoreCase("None"))) {
            return 0.0f;
        }
        if (var1.shadowMode.getValue().equalsIgnoreCase("Custom")) return var1.shadowOffset.getValue().floatValue();
        return 1.0f;
    }

    public boolean hasFont(String arg0) {
        Object var2 = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        int var3 = var2.length;
        int var4 = 0;
        while (var4 < var3) {
            Object var5 = var2[var4];
            if (!(var5.equalsIgnoreCase(arg0))) {
            } else {
                return true;
            }
            var4++;
        }
        return false;
    }

    @Generated
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    @Generated
    public void setFontRenderer(FontRenderer arg0) {
        this.fontRenderer = arg0;
    }

    static {
        HUD_RENDERING = ThreadLocal.withInitial(() -> false);
        CLIENT_TEXT_DEPTH = ThreadLocal.withInitial(() -> 0);
    }


    class 1 implements Font.GlyphVisitor {

        1(FontManager arg0, Map arg1, Map arg2, PoseStack arg3) {
            this.val$buffers = arg1;
            this.val$owners = arg2;
            this.val$matrices = arg3;
            Objects.requireNonNull(arg0);
        }

        public void acceptRenderable(TextRenderable arg0) {
            Object var2 = arg0.renderType(Font.DisplayMode.SEE_THROUGH);
            Object var3 = ((BufferBuilder) this.val$buffers.computeIfAbsent(var2, (arg1) -> {
    Object var2 = new ByteBufferBuilder(256);
    arg0.put(arg1, var2);
    return new BufferBuilder(var2, arg1.primitiveTopology(), arg1.format());
}));
            arg0.render(this.val$matrices.last().pose(), var3, 15728880, false);
        }

    }

    class 2 implements Font.GlyphVisitor {

        2(FontManager arg0, Map arg1, Map arg2, PoseStack arg3) {
            this.val$buffers = arg1;
            this.val$owners = arg2;
            this.val$matrices = arg3;
            Objects.requireNonNull(arg0);
        }

        public void acceptRenderable(TextRenderable arg0) {
            Object var2 = arg0.renderType(Font.DisplayMode.SEE_THROUGH);
            Object var3 = ((BufferBuilder) this.val$buffers.computeIfAbsent(var2, (arg1) -> {
    Object var2 = new ByteBufferBuilder(256);
    arg0.put(arg1, var2);
    return new BufferBuilder(var2, arg1.primitiveTopology(), arg1.format());
}));
            arg0.render(this.val$matrices.last().pose(), var3, 15728880, false);
        }

    }
}
