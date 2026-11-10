package lightning.utils.font;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.awt.Font;
import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lightning.LightningClient;
import lightning.mixins.accessors.GuiGraphicsExtractorAccessor;
import lightning.modules.impl.core.FontModule;
import lightning.utils.IMinecraft;
import lightning.utils.graphics.GuiQuadRenderState;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.graphics.TextGlowShader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public class FontRenderer implements Closeable, IMinecraft {
    private final ObjectList maps;
    private static final Map WORLD_TYPES;
    private final Font[] originalFonts;
    private Font[] fonts;
    private final float size;
    private final int charsPerPage;
    private final int padding;
    private int multiplier;
    private int previousGameScale;
    private boolean initialized;
    private final ReentrantReadWriteLock LOCK;

    public FontRenderer(Font[] arg0, float arg1) {
        this(arg0, arg1, 256, 48);
    }

    public FontRenderer(Font[] arg0, float arg1, int arg2, int arg3) {
        this.maps = new ObjectArrayList();
        this.multiplier = 0;
        this.previousGameScale = -1;
        this.LOCK = new ReentrantReadWriteLock();
        Preconditions.checkArgument(arg1 > 0.0f, "size <= 0");
        Preconditions.checkArgument(arg0.length > 0, "fonts.length <= 0");
        Preconditions.checkArgument(arg2 > 4, "Unreasonable charactersPerPage count");
        Preconditions.checkArgument(arg3 > 0, "paddingBetweenCharacters <= 0");
        this.originalFonts = ((Font[]) arg0.clone());
        this.size = arg1;
        this.charsPerPage = arg2;
        this.padding = arg3;
        init(this.originalFonts, arg1);
    }

    public void drawString(GuiGraphicsExtractor arg0, String arg1, float arg2, float arg3, int arg4, boolean arg5) {
        drawString(arg0, arg1, arg2, arg3, arg4, arg5, false, true);
    }

    public void drawString(GuiGraphicsExtractor arg0, String arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6) {
        drawString(arg0, arg1, arg2, arg3, arg4, arg5, arg6, true);
    }

    public void drawString(GuiGraphicsExtractor arg0, String arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6, boolean arg7) {
        drawText(arg0, Component.literal(arg1).getVisualOrderText(), arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public void drawText(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, float arg2, float arg3, int arg4, boolean arg5) {
        drawText(arg0, arg1, arg2, arg3, arg4, arg5, false, true);
    }

    public void drawText(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6) {
        drawText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, true);
    }

    public void drawText(GuiGraphicsExtractor arg0, FormattedCharSequence arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6, boolean arg7) {
        Object var9 = ((GuiGraphicsExtractorAccessor) arg0).night$getGuiRenderState();
        submitGlyphs(var9, new Matrix3x2f(arg0.pose()), Renderer2D.peekScissor(arg0), arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public void submitGlyphs(GuiRenderState arg0, Matrix3x2fc arg1, ScreenRectangle arg2, FormattedCharSequence arg3, float arg4, float arg5, int arg6, boolean arg7) {
        submitGlyphs(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, false, true);
    }

    public void submitGlyphs(GuiRenderState arg0, Matrix3x2fc arg1, ScreenRectangle arg2, FormattedCharSequence arg3, float arg4, float arg5, int arg6, boolean arg7, boolean arg8, boolean arg9) {
        ensureScale();
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        Object var11 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        if ((var11 == null) || (!(var11.isToggled()))) break label_0;
        arg4 = (arg4 + var11.xOffset.getValue().floatValue());
        arg5 = (arg5 + var11.yOffset.getValue().floatValue());
        label_0:
        var11 = layoutGlyphs(arg3, arg6, arg7, new float[1]);
        if (var11.isEmpty()) break label_8;
        float var12 = (1.0f / ((float) this.multiplier));
        Object var13 = var11.entrySet().iterator();
        while (var13.hasNext()) {
            Object var14 = ((Map.Entry) var13.next());
            Object var15 = ((GlyphMap) var14.getKey());
            Object var16 = ((List) var14.getValue());
            int var17 = var16.size();
            Object var18 = TextureSetup.singleTexture(var15.getTexture().getTextureView(), var15.getTexture().getSampler());
            if (!(!arg8)) {
                float var19 = 17.0f;
                float var20 = (var19 / ((float) var15.getWidth()));
                float var21 = (var19 / ((float) var15.getHeight()));
                float var22 = (var19 * var12);
                Object var23 = new float[(var17 * 4)];
                Object var24 = new float[(var17 * 4)];
                Object var25 = new float[(var17 * 4)];
                Object var26 = new float[(var17 * 4)];
                Object var27 = new int[(var17 * 4)];
                int var28 = 0;
                Object var29 = var16.iterator();
                while (var29.hasNext()) {
                    Object var30 = ((FontRenderer.DrawEntry) var29.next());
                    Object var31 = var30.toDraw();
                    float var32 = (((float) var31.u()) / ((float) var15.getWidth()));
                    float var33 = (((float) var31.v()) / ((float) var15.getHeight()));
                    float var34 = (((float) (var31.u() + var31.width())) / ((float) var15.getWidth()));
                    float var35 = (((float) (var31.v() + var31.height())) / ((float) var15.getHeight()));
                    float var36 = ((arg4 + (var30.atX() * var12)) - var22);
                    float var37 = ((arg5 + (var30.atY() * var12)) - var22);
                    float var38 = ((arg4 + ((var30.atX() + ((float) var31.width())) * var12)) + var22);
                    float var39 = ((arg5 + ((var30.atY() + ((float) var31.height())) * var12)) + var22);
                    int var40 = (var28 * 4);
                    var23[var40] = var36;
                    var24[var40] = var37;
                    var25[var40] = (var32 - var20);
                    var26[var40] = (var33 - var21);
                    var23[(var40 + 1)] = var36;
                    var24[(var40 + 1)] = var39;
                    var25[(var40 + 1)] = (var32 - var20);
                    var26[(var40 + 1)] = (var35 + var21);
                    var23[(var40 + 2)] = var38;
                    var24[(var40 + 2)] = var39;
                    var25[(var40 + 2)] = (var34 + var20);
                    var26[(var40 + 2)] = (var35 + var21);
                    var23[(var40 + 3)] = var38;
                    var24[(var40 + 3)] = var37;
                    var25[(var40 + 3)] = (var34 + var20);
                    var26[(var40 + 3)] = (var33 - var21);
                    var30.argb()[(var40 + 3)] = var30.argb();
                    var27[(var40 + 2)] = var27;
                    var27[(var40 + 1)] = var27;
                    var27[var40] = var27;
                    var28++;
                }
                arg0.addGuiElement(new GuiQuadRenderState(TextGlowShader.TEXT_GLOW_PIPELINE, var18, new Matrix3x2f(arg1), var23, var24, var27, var25, var26, arg2));
            }
            if (!(!arg9)) {
                var19 = new float[(var17 * 4)];
                var20 = new float[(var17 * 4)];
                var21 = new float[(var17 * 4)];
                var22 = new float[(var17 * 4)];
                var23 = new int[(var17 * 4)];
                var24 = 0;
                var25 = var16.iterator();
                while (var25.hasNext()) {
                    var26 = ((FontRenderer.DrawEntry) var25.next());
                    var27 = var26.toDraw();
                    var28 = (((float) var27.u()) / ((float) var15.getWidth()));
                    var29 = (((float) var27.v()) / ((float) var15.getHeight()));
                    var30 = (((float) (var27.u() + var27.width())) / ((float) var15.getWidth()));
                    var31 = (((float) (var27.v() + var27.height())) / ((float) var15.getHeight()));
                    var32 = (arg4 + (var26.atX() * var12));
                    var33 = (arg5 + (var26.atY() * var12));
                    var34 = (arg4 + ((var26.atX() + ((float) var27.width())) * var12));
                    var35 = (arg5 + ((var26.atY() + ((float) var27.height())) * var12));
                    var36 = (var24 * 4);
                    var19[var36] = var32;
                    var20[var36] = var33;
                    var21[var36] = var28;
                    var22[var36] = var29;
                    var19[(var36 + 1)] = var32;
                    var20[(var36 + 1)] = var35;
                    var21[(var36 + 1)] = var28;
                    var22[(var36 + 1)] = var31;
                    var19[(var36 + 2)] = var34;
                    var20[(var36 + 2)] = var35;
                    var21[(var36 + 2)] = var30;
                    var22[(var36 + 2)] = var31;
                    var19[(var36 + 3)] = var34;
                    var20[(var36 + 3)] = var33;
                    var21[(var36 + 3)] = var30;
                    var22[(var36 + 3)] = var29;
                    var26.argb()[(var36 + 3)] = var26.argb();
                    var23[(var36 + 2)] = var23;
                    var23[(var36 + 1)] = var23;
                    var23[var36] = var23;
                    var24++;
                }
                arg0.addGuiElement(new GuiQuadRenderState(var18, new Matrix3x2f(arg1), var19, var20, var23, var21, var22, arg2));
            }
        }
        label_8:
    }

    public void drawString(PoseStack arg0, String arg1, float arg2, float arg3, int arg4, boolean arg5) {
        drawString(arg0, arg1, arg2, arg3, arg4, arg5, false, true);
    }

    public void drawString(PoseStack arg0, String arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6) {
        drawString(arg0, arg1, arg2, arg3, arg4, arg5, arg6, true);
    }

    public void drawString(PoseStack arg0, String arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6, boolean arg7) {
        drawText(arg0, Component.literal(arg1).getVisualOrderText(), arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public void drawText(PoseStack arg0, FormattedCharSequence arg1, float arg2, float arg3, int arg4, boolean arg5) {
        drawText(arg0, arg1, arg2, arg3, arg4, arg5, false, true);
    }

    public void drawText(PoseStack arg0, FormattedCharSequence arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6) {
        drawText(arg0, arg1, arg2, arg3, arg4, arg5, arg6, true);
    }

    public void drawText(PoseStack arg0, FormattedCharSequence arg1, float arg2, float arg3, int arg4, boolean arg5, boolean arg6, boolean arg7) {
        ensureScale();
        if (LightningClient.MODULE_MANAGER == null) break label_0;
        var tmp0 = ((FontModule) LightningClient.MODULE_MANAGER.getModule(FontModule.class));
        Object var10 = tmp0;
        if ((tmp0 == null) || (!(var10.isToggled()))) break label_0;
        arg2 = (arg2 + var10.xOffset.getValue().floatValue());
        arg3 = (arg3 + var10.yOffset.getValue().floatValue());
        label_0:
        var tmp1 = layoutGlyphs(arg1, arg4, arg5, new float[1]);
        Object var9 = tmp1;
        if (tmp1.isEmpty()) return;
        arg0.pushPose();
        arg0.translate(arg2, arg3, 0.0f);
        arg0.scale((1.0f / ((float) this.multiplier)), (1.0f / ((float) this.multiplier)), 1.0f);
        Object var11 = arg0.last().pose();
        Object var12 = var9.entrySet().iterator();
        while (var12.hasNext()) {
            Object var13 = ((Map.Entry) var12.next());
            Object var14 = ((GlyphMap) var13.getKey());
            if (!arg6) break label_15;
            try {
                float var15 = 17.0f;
                float var16 = (var15 / ((float) var14.getWidth()));
                float var17 = (var15 / ((float) var14.getHeight()));
                Object var18 = ByteBufferBuilder.exactlySized(((((ObjectList) var13.getValue()).size() * 4) * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));
                try {
                    Object var19 = new BufferBuilder(var18, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                    Object var20 = ((ObjectList) var13.getValue()).iterator();
                    while (var20.hasNext()) {
                        Object var21 = ((FontRenderer.DrawEntry) var20.next());
                        Object var22 = var21.toDraw();
                        float var23 = (((float) var22.u()) / ((float) var14.getWidth()));
                        float var24 = (((float) var22.v()) / ((float) var14.getHeight()));
                        float var25 = (((float) (var22.u() + var22.width())) / ((float) var14.getWidth()));
                        float var26 = (((float) (var22.v() + var22.height())) / ((float) var14.getHeight()));
                        var19.addVertex(var11, (var21.atX() - var15), ((var21.atY() + ((float) var22.height())) + var15), 0.0f).setUv((var23 - var16), (var26 + var17)).setColor(var21.argb());
                        var19.addVertex(var11, ((var21.atX() + ((float) var22.width())) + var15), ((var21.atY() + ((float) var22.height())) + var15), 0.0f).setUv((var25 + var16), (var26 + var17)).setColor(var21.argb());
                        var19.addVertex(var11, ((var21.atX() + ((float) var22.width())) + var15), (var21.atY() - var15), 0.0f).setUv((var25 + var16), (var24 - var17)).setColor(var21.argb());
                        var19.addVertex(var11, (var21.atX() - var15), (var21.atY() - var15), 0.0f).setUv((var23 - var16), (var24 - var17)).setColor(var21.argb());
                    }
                    var20 = var19.build();
                    if (var20 != null) {
                        Renderer3D.draw(TextGlowShader.getWorldGlowType(var14.getTextureId()), var20);
                    }
                    if (var18 == null) break label_13;
                    var18.close();
                } catch (Throwable ex8) {
                    var19 = ex8;
                    if (var18 == null) break label_12;
                    try {
                        var18.close();
                    } catch (Throwable ex11) {
                        var20 = ex11;
                        var19.addSuppressed(var20);
                    }
                    label_12:
                    throw var19;
                }
                label_13:
            } catch (Throwable ex14) {
                var15 = ex14;
            }
            label_15:
            if (!arg7) {
                break label_2;
            }
            var15 = ByteBufferBuilder.exactlySized(((((ObjectList) var13.getValue()).size() * 4) * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));
            var16 = new BufferBuilder(var15, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            var17 = ((ObjectList) var13.getValue()).iterator();
            while (var17.hasNext()) {
                var18 = ((FontRenderer.DrawEntry) var17.next());
                var19 = var18.toDraw();
                var20 = (((float) var19.u()) / ((float) var14.getWidth()));
                var21 = (((float) var19.v()) / ((float) var14.getHeight()));
                var22 = (((float) (var19.u() + var19.width())) / ((float) var14.getWidth()));
                var23 = (((float) (var19.v() + var19.height())) / ((float) var14.getHeight()));
                var16.addVertex(var11, var18.atX(), (var18.atY() + ((float) var19.height())), 0.0f).setUv(var20, var23).setColor(var18.argb());
                var16.addVertex(var11, (var18.atX() + ((float) var19.width())), (var18.atY() + ((float) var19.height())), 0.0f).setUv(var22, var23).setColor(var18.argb());
                var16.addVertex(var11, (var18.atX() + ((float) var19.width())), var18.atY(), 0.0f).setUv(var22, var21).setColor(var18.argb());
                var16.addVertex(var11, var18.atX(), var18.atY(), 0.0f).setUv(var20, var21).setColor(var18.argb());
            }
            var17 = var16.build();
            Renderer3D.draw(worldTexturedType(var14.getTextureId()), var17);
            if (var15 == null) {
                break label_2;
            }
            var15.close();
            break label_25;
            Object var27 = null;
            if (var15 == null) {
                break label_2;
            }
            var15.close();
            throw var27;
            label_25:
        }
        arg0.popPose();
    }

    private Map layoutGlyphs(FormattedCharSequence arg0, int arg1, boolean arg2, float[] arg3) {
        if ((arg1 & -67108864) == 0) {
            arg1 = (arg1 | -16777216);
        }
        if (!(!arg2)) {
            arg1 = (((arg1 & 16579836) >> 2) | (arg1 & -16777216));
        }
        int var5 = ((arg1 >> 24) & 255);
        int var6 = ((arg1 >> 16) & 255);
        int var7 = ((arg1 >> 8) & 255);
        int var8 = (arg1 & 255);
        Object var9 = new LinkedHashMap();
        var tmp0 = new float[1];
        tmp0[0] = 0.0f;
        Object var10 = tmp0;
        arg0.accept((arg8, arg9) -> {
    Object var11 = Character.toChars(arg9);
    int var12 = arg0;
    int var13 = arg1;
    int var14 = arg2;
    if (arg8.getColor() != null) {
        int var15 = arg8.getColor().getValue();
        if ((var15 & -67108864) == 0) {
            var15 = (var15 | -16777216);
        }
        if (!(!arg3)) {
            var15 = (((var15 & 16579836) >> 2) | (var15 & -16777216));
        }
        var12 = ((var15 >> 16) & 255);
        var13 = ((var15 >> 8) & 255);
        var14 = (var15 & 255);
    }
    var15 = ((((arg4 << 24) | (var12 << 16)) | (var13 << 8)) | var14);
    Object var16 = var11;
    int var17 = var16.length;
    int var18 = 0;
    while (var18 < var17) {
        int var19 = var16[var18];
        Object var20 = locateGlyph(var19);
        if (var20 == null) {
        } else {
            if (var20.value() != 32) {
                ((ObjectList) arg5.computeIfAbsent(var20.parent(), (arg0) -> new ObjectArrayList())).add(new FontRenderer.DrawEntry(arg6[0], 0.0f, var15, var20));
            }
            arg6[0] = (arg6[0] + ((float) var20.width()));
        }
        var18++;
    }
    return true;
});
        arg3[0] = var10[0];
        return var9;
    }

    private static RenderType worldTexturedType(Identifier arg0) {
        return ((RenderType) WORLD_TYPES.computeIfAbsent(arg0, (arg0) -> RenderType.create(("night_font_" + String.valueOf(arg0)), RenderSetup.builder(RenderPipelines.GUI_TEXTURED).withTexture("Sampler0", arg0).createRenderSetup())));
    }

    private void ensureScale() {
        if (mc.getWindow().getGuiScale() != this.previousGameScale) {
            close();
            init(this.originalFonts, this.size);
        }
    }

    public float getTextWidth(String arg0) {
        Object var2 = stripControlCodes(arg0).toCharArray();
        float var3 = 0.0f;
        Object var4 = var2;
        int var5 = var4.length;
        int var6 = 0;
        while (var6 < var5) {
            int var7 = var4[var6];
            Object var8 = locateGlyph(var7);
            if (var8 == null) {
            } else {
                var3 = (var3 + (((float) var8.width()) / ((float) this.multiplier)));
            }
            var6++;
        }
        return Math.max(var3, 0.0f);
    }

    public float getTextWidth(FormattedCharSequence arg0) {
        Object var2 = new float[2];
        arg0.accept((arg2, arg3) -> {
    int var5 = ((char) arg3);
    Object var6 = locateGlyph(var5);
    if (var6 != null) {
        arg0[0] = (arg0[0] + (((float) var6.width()) / ((float) this.multiplier)));
    }
    return true;
});
        return Math.max(var2[0], var2[1]);
    }

    public float getHeight() {
        Object var1 = locateGlyph(65);
        if (var1 != null) return (((float) var1.height()) / ((float) this.multiplier));
        return 0.0f;
    }

    public static String stripControlCodes(String arg0) {
        Object var1 = arg0.toCharArray();
        Object var2 = new StringBuilder();
        int var3 = 0;
        while (var3 < var1.length) {
            int var4 = var1[var3];
            if (var4 == 167) {
                var3++;
            } else {
                var2.append(var4);
            }
            var3++;
        }
        return var2.toString();
    }

    private void init(Font[] arg0, float arg1) {
        if (!(!this.initialized)) throw new IllegalStateException("Double call to init()");
        this.LOCK.writeLock().lock();
        if (mc != null && mc.getWindow() != null) {
        } else {
        }
        int var3 = 2;
        this.previousGameScale = (var3 <= 0 ? 2 : var3);
        this.multiplier = (this.previousGameScale * 2);
        this.fonts = new Font[arg0.length];
        int var4 = 0;
        while (var4 < arg0.length) {
            this.fonts[var4] = arg0[var4].deriveFont((arg1 * ((float) this.multiplier)));
            var4++;
        }
        this.initialized = true;
        this.LOCK.writeLock().unlock();
        break label_11;
        Object var5 = mc.getWindow().getGuiScale();
        this.LOCK.writeLock().unlock();
        throw var5;
        label_11:
        locateGlyph(65);
    }

    private Glyph locateGlyph(char arg0) {
        this.LOCK.readLock().lock();
        Object var2 = this.maps.iterator();
        while (var2.hasNext()) {
            Object var3 = ((GlyphMap) var2.next());
            if (var3.contains(arg0)) {
                Object var4 = var3.getGlyph(arg0);
                this.LOCK.readLock().unlock();
                return var4;
            }
        }
        this.LOCK.readLock().unlock();
        break label_7;
        Object var5 = null;
        this.LOCK.readLock().unlock();
        throw var5;
        label_7:
        var2 = (this.charsPerPage * ((int) Math.floor((((double) arg0) / ((double) this.charsPerPage)))));
        var3 = new GlyphMap(this.fonts, ((char) var2), ((char) (var2 + this.charsPerPage)), this.padding);
        this.LOCK.writeLock().lock();
        var3.generate();
        this.maps.add(var3);
        this.LOCK.writeLock().unlock();
        break label_12;
        Object var6 = null;
        this.LOCK.writeLock().unlock();
        throw var6;
        label_12:
        return var3.getGlyph(arg0);
    }

    public void close() {
        this.LOCK.writeLock().lock();
        Object var1 = this.maps.iterator();
        while (var1.hasNext()) {
            Object var2 = ((GlyphMap) var1.next());
            var2.destroy();
        }
        this.maps.clear();
        this.initialized = false;
        this.LOCK.writeLock().unlock();
        break label_5;
        Object var3 = null;
        this.LOCK.writeLock().unlock();
        throw var3;
        label_5:
    }

    static {
        WORLD_TYPES = new HashMap();
    }


    public record DrawEntry(float atX, float atY, int argb, Glyph toDraw) {

        public DrawEntry(float arg0, float arg1, int arg2, Glyph arg3) {
            this.atX = arg0;
            this.atY = arg1;
            this.argb = arg2;
            this.toDraw = arg3;
        }

        public float atX() {
            return this.atX;
        }

        public float atY() {
            return this.atY;
        }

        public int argb() {
            return this.argb;
        }

        public Glyph toDraw() {
            return this.toDraw;
        }

    }
}
