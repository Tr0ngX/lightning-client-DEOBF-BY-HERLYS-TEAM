package lightning.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lightning.LightningClient;
import lightning.gui.api.DescriptionFrame;
import lightning.gui.api.Frame;
import lightning.gui.api.PingBypassFrame;
import lightning.gui.api.SearchFrame;
import lightning.gui.impl.BindButton;
import lightning.modules.Module;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.modules.impl.miscellaneous.FightLogModule;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.ClickGuiBackground;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.system.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class ClickGuiScreen extends Screen {
    private final ArrayList frames;
    private final ArrayList buttons;
    private final DescriptionFrame descriptionFrame;
    private final SearchFrame searchFrame;
    private final PingBypassFrame pingBypassFrame;
    private final Timer lineTimer;
    private boolean showLine;
    private Color colorClipboard;
    private float appliedScale;
    private static final int SLIDE_DURATION_MS = 320;
    private final Animation slideAnim;
    private boolean closing;
    private static final int CATEGORY_STAGGER_MS = 35;
    private boolean readyToRemove;
    private static final int PANEL_WIDTH = 100;
    private static final int PANEL_GAP = 10;
    private static final int RESERVE_TOP = 22;
    private static final int RESERVE_BOTTOM = 46;
    private static final int HEADER_H = 16;
    private static final int SCREEN_PADDING = 8;
    private float ovX1;
    private float ovX2;
    private float ovY2;
    private boolean ovShown;
    private static boolean headLogged;

    public boolean isReadyToRemove() {
        return this.readyToRemove;
    }

    public void requestClose() {
        this.closing = true;
        this.readyToRemove = false;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void cancelClose() {
        this.closing = false;
        this.readyToRemove = false;
        this.slideAnim.setEasing(Easing.Method.EASE_OUT_CUBIC);
    }

    public ClickGuiScreen() {
        super(Component.literal("night-click-gui"));
        this.frames = new ArrayList();
        this.buttons = new ArrayList();
        this.lineTimer = new Timer();
        this.showLine = false;
        this.colorClipboard = null;
        this.appliedScale = 1.0f;
        this.slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
        this.closing = false;
        this.readyToRemove = false;
        this.ovX1 = 0.0f;
        this.ovX2 = 0.0f;
        this.ovY2 = 0.0f;
        this.ovShown = false;
        Window window = Minecraft.getInstance().getWindow();
        if (window != null) {
        } else {
        }
        int n = 480;
        if (window != null) {
        } else {
        }
        int n2 = 270;
        Module.Category[] categoryArray = Module.Category.values();
        int n3 = ((n2 - 22) - 46);
        int n4 = Math.max(40, ((n3 - 16) - 4));
        int n5 = categoryArray;
        int n6 = n5.length;
        Frame frame = 0;
        while (frame < n6) {
            Module.Category category = n5[frame];
            Frame object = new Frame(category, 0, 22, 100, 16);
            object.setMaxViewHeight(n4);
            this.frames.add(object);
            frame++;
        }
        HeaderFx.fitWidths(this.frames, ((float) n), 10);
        n5 = ((this.frames.size() - 1) * 10);
        n6 = this.frames.iterator();
        while (n6.hasNext()) {
            frame = ((Frame) n6.next());
            n5 = (n5 + frame.getWidth());
        }
        n6 = ((n - n5) / 2);
        int n7 = 0;
        category = this.frames.iterator();
        while (category.hasNext()) {
            frame = ((Frame) category.next());
            int n8 = Math.min((frame.getButtons().size() * 14), n4);
            n7 = Math.max(n7, ((16 + n8) + 4));
        }
        int n9 = (22 + Math.max(0, ((n3 - n7) / 2)));
        object = this.frames.iterator();
        while (object.hasNext()) {
            frame = ((Frame) object.next());
            frame.setX(n6);
            frame.setY(n9);
            n6 = (n6 + (frame.getWidth() + 10));
        }
        this.pingBypassFrame = new PingBypassFrame(n6, n9, 100, 13);
        this.descriptionFrame = new DescriptionFrame(((n - 200) / 2), (n2 - 34), 200, 13);
        this.searchFrame = new SearchFrame();
    }

    private float getSlideDistance() {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null) return (((float) window.getGuiScaledWidth()) + 30.0f);
        return 500.0f;
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float f2 = this.slideAnim.get((!(!this.closing) ? 0.0f : 1.0f));
        if (this.frames.isEmpty()) {
        } else {
        }
        var tmp0 = categoryFrameProgress((this.frames.size() - 1));
        float f3 = tmp0;
        float f4 = tmp0;
        if (!(!this.closing) && f2 <= 0.005f && f3 <= 0.005f) {
            this.readyToRemove = true;
            Minecraft.getInstance().gui.setScreen(null);
            return;
        }
        this.descriptionFrame.setDescription("");
        String string = this.searchFrame.getQuery();
        float f5 = getSlideDistance();
        int n3 = Math.round((((float) n) / this.appliedScale));
        int n4 = Math.round((((float) n2) / this.appliedScale));
        HeaderFx.poseHook(guiGraphicsExtractor).pushMatrix();
        guiGraphicsExtractor.pose().scale(this.appliedScale, this.appliedScale);
        int i = 0;
        while (i < this.frames.size()) {
            Frame frame = ((Frame) this.frames.get(i));
            float f6 = categoryFrameProgress(i);
            float f7 = Mth.lerp(f6, -(f5), 0.0f);
            guiGraphicsExtractor.pose().pushMatrix();
            guiGraphicsExtractor.pose().translate(f7, 0.0f);
            frame.render(guiGraphicsExtractor, n3, n4, f, string);
            guiGraphicsExtractor.pose().popMatrix();
            i++;
        }
        float f8 = Mth.lerp(f2, -(f5), 0.0f);
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(f8, 0.0f);
        HeaderFx.descRender(HeaderFx.desc(this), guiGraphicsExtractor, n3, n4, f);
        this.searchFrame.render(guiGraphicsExtractor, n3, n4, f);
        renderOverview(guiGraphicsExtractor);
        renderPlayerCard(guiGraphicsExtractor);
        renderInfoCard(guiGraphicsExtractor);
        renderLogo(guiGraphicsExtractor);
        guiGraphicsExtractor.pose().popMatrix();
        guiGraphicsExtractor.pose().popMatrix();
    }

    private void renderLogo(GuiGraphicsExtractor guiGraphicsExtractor) {
    }

    private float categoryFrameProgress(int n) {
        long l = ((System.currentTimeMillis() - this.slideAnim.getStartTime()) - (((long) n) * 35L));
        float f = Mth.clamp((((float) l) / 320.0f), 0.0f, 1.0f);
        float f2 = Easing.ease(f, (!(!this.closing) ? Easing.Method.EASE_IN_CUBIC : Easing.Method.EASE_OUT_CUBIC));
        if (!(!this.closing)) {
        } else {
        }
        return f2;
    }

    private static int overviewRgb(String string) {
        Object var1 = string;
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1932423455 -> { break label_2; }
            case 2074527 -> { break label_7; }
            case 2366700 -> { break label_6; }
            case 426458062 -> { break label_5; }
            case 678949039 -> { break label_4; }
            case 1185082643 -> { break label_3; }
            case 1993470708 -> { break label_1; }
            default -> { break label_8; }
        }
        label_1:
        if (!(var1.equals("COMBAT"))) break label_8;
        var2 = 0;
        break label_8;
        label_2:
        if (!(var1.equals("PLAYER"))) break label_8;
        var2 = 1;
        break label_8;
        label_3:
        if (!(var1.equals("VISUALS"))) break label_8;
        var2 = 2;
        break label_8;
        label_4:
        if (!(var1.equals("MOVEMENT"))) break label_8;
        var2 = 3;
        break label_8;
        label_5:
        if (!(var1.equals("MISCELLANEOUS"))) break label_8;
        var2 = 4;
        break label_8;
        label_6:
        if (!(var1.equals("MISC"))) break label_8;
        var2 = 5;
        break label_8;
        label_7:
        if (!(var1.equals("CORE"))) break label_8;
        var2 = 6;
        label_8:
        switch (var2) {
            case 0 -> { break label_9; }
            case 1 -> { break label_10; }
            case 2 -> { break label_11; }
            case 3 -> { break label_12; }
            case 4 -> { break label_13; }
            case 5 -> { break label_13; }
            case 6 -> { break label_14; }
            default -> { break label_15; }
        }
        label_9:
        return 16736126;
        label_10:
        return 4906624;
        label_11:
        return 10980346;
        label_12:
        return 3718648;
        label_13:
        return 16498468;
        label_14:
        return 3003583;
        label_15:
        return 12566463;
    }

    private static String overviewLabel(String string) {
        Object var1 = string;
        int var2 = -1;
        switch (var1.hashCode()) {
            case -1932423455 -> { break label_2; }
            case 2074527 -> { break label_7; }
            case 2366700 -> { break label_6; }
            case 426458062 -> { break label_5; }
            case 678949039 -> { break label_4; }
            case 1185082643 -> { break label_3; }
            case 1993470708 -> { break label_1; }
            default -> { break label_8; }
        }
        label_1:
        if (!(var1.equals("COMBAT"))) break label_8;
        var2 = 0;
        break label_8;
        label_2:
        if (!(var1.equals("PLAYER"))) break label_8;
        var2 = 1;
        break label_8;
        label_3:
        if (!(var1.equals("VISUALS"))) break label_8;
        var2 = 2;
        break label_8;
        label_4:
        if (!(var1.equals("MOVEMENT"))) break label_8;
        var2 = 3;
        break label_8;
        label_5:
        if (!(var1.equals("MISCELLANEOUS"))) break label_8;
        var2 = 4;
        break label_8;
        label_6:
        if (!(var1.equals("MISC"))) break label_8;
        var2 = 5;
        break label_8;
        label_7:
        if (!(var1.equals("CORE"))) break label_8;
        var2 = 6;
        label_8:
        switch (var2) {
            case 0 -> { break label_9; }
            case 1 -> { break label_10; }
            case 2 -> { break label_11; }
            case 3 -> { break label_12; }
            case 4 -> { break label_13; }
            case 5 -> { break label_13; }
            case 6 -> { break label_14; }
            default -> { break label_15; }
        }
        label_9:
        return "Combat";
        label_10:
        return "Player";
        label_11:
        return "Visuals";
        label_12:
        return "Movement";
        label_13:
        return "Misc";
        label_14:
        return "Core";
        label_15:
        if (string.isEmpty()) return string;
        return (string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
    }

    private static void overviewDonut(GuiGraphicsExtractor g, float cx, float cy, float ro, float ri, int[] on, int sumTot, int[] rgb) {
        int nc = on.length;
        float[] start = new float[nc];
        float[] end = new float[nc];
        float acc = 0.0f;
        int i = 0;
        while (i < nc) {
            start[i] = acc;
            var tmp0 = (acc + (((float) on[i]) / ((float) Math.max(1, sumTot))));
            acc = tmp0;
            end[i] = tmp0;
            i++;
        }
        Color[] cols = new Color[(nc + 1)];
        i = 0;
        while (i < nc) {
            cols[i] = new Color(((rgb[i] >> 16) & 255), ((rgb[i] >> 8) & 255), (rgb[i] & 255), 255);
            i++;
        }
        cols[nc] = new Color(40, 36, 68, 255);
        float gap = 0.006f;
        float step = 0.4f;
        float y = (cy - ro);
        while (y < (cy + ro)) {
            float dy = ((y + (step / 2.0f)) - cy);
            int runId = -1;
            float runStart = 0.0f;
            float x = (cx - ro);
            while (x <= ((cx + ro) + 0.001f)) {
                float dx = ((x + (step / 2.0f)) - cx);
                float r2 = ((dx * dx) + (dy * dy));
                int id = -1;
                if ((r2 > (ro * ro)) || (r2 < (ri * ri))) break label_40;
                float frac = ((float) ((Math.atan2(((double) dy), ((double) dx)) / 6.283185307179586) + 0.25));
                if (frac < 0.0f) {
                    frac = (frac + 1.0f);
                }
                if (frac >= 1.0f) {
                    frac = (frac - 1.0f);
                }
                id = nc;
                i = 0;
                while (i < nc) {
                    if (((end[i] - start[i]) <= 1.0E-4f) || (frac < (start[i] + gap))) break label_39;
                    if (frac > (end[i] - gap)) {
                        break label_39;
                    }
                    id = i;
                    break label_40;
                    label_39:
                    i++;
                }
                label_40:
                if (id == runId) {
                } else {
                    if (runId >= 0) {
                        Renderer2D.renderQuad(g, runStart, y, x, (y + step), cols[runId]);
                    }
                    runId = id;
                    runStart = x;
                }
                x = (x + step);
            }
            y = (y + step);
        }
    }

    private static void drawSparkle(GuiGraphicsExtractor g, float cx, float cy, float r, Color c) {
        int d = -2;
        while (d <= 2) {
            float len = (r * (1.0f - (((float) Math.abs(d)) / 3.0f)));
            Renderer2D.renderQuad(g, (cx - len), ((cy + ((float) d)) - 0.5f), (cx + len), ((cy + ((float) d)) + 0.5f), c);
            Renderer2D.renderQuad(g, ((cx + ((float) d)) - 0.5f), (cy - len), ((cx + ((float) d)) + 0.5f), (cy + len), c);
            d++;
        }
    }

    private void renderOverview(GuiGraphicsExtractor g) {
        this.ovShown = false;
        try {
            if (!(this.frames.isEmpty())) break label_3;
            return;
            try {
                label_3:
                int minX = 2147483647;
                int minY = 2147483647;
                float avail = this.frames.iterator();
                while (avail.hasNext()) {
                    Frame fr = ((Frame) avail.next());
                    minX = Math.min(minX, fr.getX());
                    minY = Math.min(minY, fr.getY());
                }
                label_10:
                avail = ((((float) minX) - 10.0f) - 6.0f);
                float w = Math.min(150.0f, avail);
                if (w < 104.0f) {
                    return;
                    try {
                    }
                    float x1 = ((((float) minX) - 10.0f) - w);
                    float x2 = (x1 + w);
                    float y1 = ((float) minY);
                    Module.Category[] cats = Module.Category.values();
                    int nc = cats.length;
                    int[] on = new int[nc];
                    int[] tot = new int[nc];
                    int[] rgb = new int[nc];
                    String[] labels = new String[nc];
                    int sumOn = 0;
                    int sumTot = 0;
                    int i = 0;
                    while (i < nc) {
                        float pad = LightningClient.MODULE_MANAGER.getModules(cats[i]).iterator();
                        while (pad.hasNext()) {
                            Module m = ((Module) pad.next());
                            int n = i;
                            tot[n] = (tot[n] + 1);
                            if (!(m.isToggled())) {
                                break label_28;
                            }
                            int n2 = i;
                            on[n2] = (on[n2] + 1);
                        }
                        rgb[i] = overviewRgb(cats[i].name());
                        labels[i] = overviewLabel(cats[i].name());
                        sumOn = (sumOn + on[i]);
                        sumTot = (sumTot + tot[i]);
                        i++;
                    }
                    int fh = LightningClient.FONT_MANAGER.getHeight();
                    pad = 9.0f;
                    float mw = 0.0f;
                    i = 0;
                    while (i < nc) {
                        mw = Math.max(mw, (((float) LightningClient.FONT_MANAGER.getWidth(labels[i])) + 10.0f));
                        i++;
                    }
                    float inner = (w - (pad * 2.0f));
                    int cols = ((mw * 3.0f) <= inner ? 3 : 2);
                    int rows = (((nc + cols) - 1) / cols);
                    float colW = (inner / ((float) cols));
                    float rowH = (((float) fh) + 5.0f);
                    float ro = 27.0f;
                    float ri = 20.5f;
                    float cx = (x1 + (w / 2.0f));
                    float cy = ((y1 + 20.0f) + ro);
                    float ly = ((cy + ro) + 10.0f);
                    float y2 = ((ly + (rowH * ((float) rows))) + 4.0f);
                    this.ovX1 = x1;
                    this.ovX2 = x2;
                    this.ovY2 = y2;
                    this.ovShown = true;
                    LightningSkin.drawRoundedRect(g, x1, y1, x2, y2, 6.0f, true, true, true, true, new Color(16, 15, 34, 240), new Color(12, 11, 28, 240));
                    LightningSkin.glow(g, x1, y1, x2, y2, 6.0f, new Color(160, 130, 255, 255));
                    NeonFx.ring(g, x1, y1, x2, y2, 6.0f, 1.0f, new Color(160, 130, 255, 255));
                    float sp = ((float) ((Math.sin((((double) System.currentTimeMillis()) / 500.0)) * 0.5) + 0.5));
                    float scx = (x2 - 4.0f);
                    float scy = (y1 + 1.0f);
                    LightningSkin.drawRoundedRectAll(g, (scx - 7.0f), (scy - 7.0f), (scx + 7.0f), (scy + 7.0f), 7.0f, new Color(160, 130, 255, (25 + Math.round((30.0f * sp)))));
                    drawSparkle(g, scx, scy, (6.0f + (2.0f * sp)), new Color(255, 255, 255, (200 + Math.round((55.0f * sp)))));
                    LightningClient.FONT_MANAGER.drawTextWithShadow(g, "T\u1ed4NG QUAN", Math.round((x1 + pad)), Math.round((y1 + 7.0f)), Color.WHITE);
                    overviewDonut(g, cx, cy, ro, ri, on, sumTot, rgb);
                    String big = String.valueOf(sumOn);
                    float sc = 1.5f;
                    int bw = LightningClient.FONT_MANAGER.getWidth(big);
                    String sub = ("/ " + sumTot);
                    int sw = LightningClient.FONT_MANAGER.getWidth(sub);
                    float blockH = (((((float) fh) * sc) + 3.0f) + (((float) fh) * 0.85f));
                    float top = (cy - (blockH / 2.0f));
                    g.pose().pushMatrix();
                    g.pose().translate((cx - ((((float) bw) * sc) / 2.0f)), top);
                    g.pose().scale(sc, sc);
                    LightningClient.FONT_MANAGER.drawText(g, big, 0, 0, Color.WHITE);
                    g.pose().popMatrix();
                    float ssc = 0.85f;
                    g.pose().pushMatrix();
                    g.pose().translate((cx - ((((float) sw) * ssc) / 2.0f)), ((top + (((float) fh) * sc)) + 3.0f));
                    g.pose().scale(ssc, ssc);
                    LightningClient.FONT_MANAGER.drawText(g, sub, 0, 0, new Color(150, 145, 190));
                    g.pose().popMatrix();
                    i = 0;
                    while (i < nc) {
                        int col = (i % cols);
                        int row = (i / cols);
                        float lx = ((x1 + pad) + (colW * ((float) col)));
                        float ry = (ly + (rowH * ((float) row)));
                        Color cc = new Color(((rgb[i] >> 16) & 255), ((rgb[i] >> 8) & 255), (rgb[i] & 255), 255);
                        float dy = ((ry + ((((float) fh) - 5.0f) / 2.0f)) + 0.5f);
                        LightningSkin.drawRoundedRectAll(g, lx, dy, (lx + 5.0f), (dy + 5.0f), 2.5f, cc);
                        LightningClient.FONT_MANAGER.drawText(g, labels[i], Math.round((lx + 9.0f)), Math.round(ry), new Color(190, 188, 215));
                        i++;
                    }
                } catch (Throwable ex104) {
                    minX = ex104;
                }
            }
        }
    }

    private static Object rcall(Object o, String m) {
        try {
            if (o == null) {
                return null;
                try {
                }
                return o.getClass().getMethod(m, new Class[0]).invoke(o, new Object[0]);
            } catch (Throwable ex5) {
                Throwable t = ex5;
                return null;
            }
        }
    }

    private static Object selfPlayer() {
        try {
            Minecraft m = Minecraft.getInstance();
            return m.getClass().getField("player").get(m);
        } catch (Throwable ex3) {
            Throwable t = ex3;
            return null;
        }
    }

    private static String rname(Object o) {
        Object c = rcall(o, "getName");
        Object str = rcall(c, "getString");
        if (str == null) {
        } else {
        }
        return String.valueOf(str);
    }

    private static Object rcallOn(String cls, Object o, String m) throws Exception {
        Method mm = Class.forName(cls).getMethod(m, new Class[0]);
        mm.setAccessible(true);
        return mm.invoke(o, new Object[0]);
    }

    private static boolean drawFace(GuiGraphicsExtractor g, Object tex, float x, float y, float size) throws Exception {
        Class pf = Class.forName("net.minecraft.client.gui.components.PlayerFaceExtractor");
        Exception last = null;
        Object var7 = pf.getMethods();
        int var8 = var7.length;
        int var9 = 0;
        while (var9 < var8) {
            Method fm = var7[var9];
            if (!(fm.getName().equals("extractRenderState"))) break label_13;
            if (fm.getParameterCount() != 8) {
                break label_13;
            }
            Class[] pt = fm.getParameterTypes();
            if (!(pt[1].isInstance(tex))) {
                break label_13;
                try {
                }
                fm.setAccessible(true);
                var tmp0 = new Object[8];
                tmp0[0] = g;
                tmp0[1] = tex;
                tmp0[2] = Math.round(x);
                tmp0[3] = Math.round(y);
                tmp0[4] = Math.round(size);
                tmp0[5] = true;
                tmp0[6] = false;
                tmp0[7] = -1;
                fm.invoke(null, tmp0);
                return true;
            } catch (Exception ex11) {
                Exception e = ex11;
                last = e;
                label_13:
                var9++;
            }
            if (last != null) throw last;
            return false;
        }
    }

    private void drawHead(GuiGraphicsExtractor g, String name, float x, float y, float size) {
        boolean ok = false;
        try {
            Object skin = null;
            Object me = selfPlayer();
            if (me == null) break label_10;
            try {
                Method gs = me.getClass().getMethod("getSkin", new Class[0]);
                gs.setAccessible(true);
                skin = gs.invoke(me, new Object[0]);
            } catch (Throwable ex8) {
                Throwable t = ex8;
                skin = null;
            }
            label_10:
            if (skin != null) break label_16;
            Object conn = Minecraft.getInstance().getConnection();
            if ((conn == null) || (name == null)) break label_16;
            var tmp0 = new Class[1];
            tmp0[0] = String.class;
            var tmp1 = new Object[1];
            tmp1[0] = name;
            Object info = conn.getClass().getMethod("getPlayerInfo", tmp0).invoke(conn, tmp1);
            if (info == null) break label_16;
            skin = rcallOn("net.minecraft.client.multiplayer.PlayerInfo", info, "getSkin");
            label_16:
            if (skin != null) {
                Object body = rcallOn("net.minecraft.world.entity.player.PlayerSkin", skin, "body");
                Object tex = rcallOn("net.minecraft.core.ClientAsset$Texture", body, "texturePath");
                ok = drawFace(g, tex, x, y, size);
            }
            if (!ok && !headLogged) {
                headLogged = true;
                System.err.println(("[LightningGUI] drawHead: khong lay duoc skin (skin=" + String.valueOf(skin) + ")"));
            }
        } catch (Throwable ex24) {
            t = ex24;
            if (headLogged) break label_32;
            headLogged = true;
            if (t.getCause() != null) {
            } else {
            }
            Throwable c = t;
            System.err.println(("[LightningGUI] drawHead loi: " + String.valueOf(c)));
            c.printStackTrace();
        }
        label_32:
        if (!ok) {
            LightningSkin.drawRoundedRectAll(g, x, y, (x + size), (y + size), 4.0f, new Color(60, 56, 96, 255));
            if (name != null) {
                if (name.isEmpty()) {
                }
            } else {
            }
            String ch = name.substring(0, 1).toUpperCase();
            int cw = LightningClient.FONT_MANAGER.getWidth(ch);
            int fh = LightningClient.FONT_MANAGER.getHeight();
            LightningClient.FONT_MANAGER.drawText(g, ch, Math.round((x + ((size - ((float) cw)) / 2.0f))), Math.round((y + ((size - ((float) fh)) / 2.0f))), Color.WHITE);
        }
    }

    private void drawScaledText(GuiGraphicsExtractor g, String text, float x, float y, float scale, Color color) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        LightningClient.FONT_MANAGER.drawText(g, text, 0, 0, color);
        g.pose().popMatrix();
    }

    private void renderPlayerCard(GuiGraphicsExtractor g) {
        try {
            if (!this.ovShown) {
                return;
                try {
                }
                Minecraft mc = Minecraft.getInstance();
                int fh = LightningClient.FONT_MANAGER.getHeight();
                float x1 = this.ovX1;
                float x2 = this.ovX2;
                float w = (x2 - x1);
                float y1 = (this.ovY2 + 8.0f);
                float pad = 8.0f;
                float inner = (w - (pad * 2.0f));
                Object me = selfPlayer();
                String myName = rname(me);
                if (myName != null) {
                    if (myName.isEmpty()) {
                    }
                    myName = "Player";
                }
                var tmp0 = rcall(me, "isAlive");
                Object al = tmp0;
                if (tmp0 != null) {
                    if (Boolean.TRUE.equals(al)) {
                    }
                } else {
                }
                boolean alive = false;
                List hist = FightLogModule.getHistory();
                int kills = FightLogModule.getKills();
                int lostTotems = FightLogModule.getTotemsLost();
                int survived = FightLogModule.getSurvived();
                float headSize = 22.0f;
                float numScale = 1.3f;
                float lblScale = 0.8f;
                float statH = ((((4.0f + (((float) fh) * numScale)) + 1.0f) + (((float) fh) * lblScale)) + 4.0f);
                float footH = (((float) fh) + 6.0f);
                float rowH = (((float) fh) + 4.0f);
                float yTitle = (y1 + 7.0f);
                float yHead = ((yTitle + ((float) fh)) + 6.0f);
                float yStat = ((yHead + headSize) + 7.0f);
                float yHist = ((yStat + statH) + 7.0f);
                float yRows = ((yHist + ((float) fh)) + 3.0f);
                float fixedBottom = ((6.0f + footH) + 6.0f);
                float scrH = (((float) mc.getWindow().getGuiScaledHeight()) / Math.max(0.01f, this.appliedScale));
                int want = Math.max(1, Math.min(4, hist.size()));
                var tmp1 = ((int) Math.floor(((double) ((((scrH - 4.0f) - yRows) - fixedBottom) / rowH))));
                int fit = tmp1;
                int rows = Math.min(want, tmp1);
                if (rows < 1) {
                    return;
                    try {
                    }
                    float yFoot = ((yRows + (rowH * ((float) rows))) + 6.0f);
                    float y2 = ((yFoot + footH) + 6.0f);
                    Color green = new Color(90, 230, 150);
                    Color red = new Color(245, 100, 110);
                    Color grey = new Color(150, 148, 185);
                    LightningSkin.drawRoundedRect(g, x1, y1, x2, y2, 6.0f, true, true, true, true, new Color(14, 22, 28, 240), new Color(10, 17, 22, 240));
                    LightningSkin.glow(g, x1, y1, x2, y2, 6.0f, new Color(70, 220, 150, 235));
                    NeonFx.ring(g, x1, y1, x2, y2, 6.0f, 1.0f, new Color(70, 220, 150, 235));
                    LightningClient.FONT_MANAGER.drawTextWithShadow(g, "NG\u01af\u1edcI CH\u01a0I", Math.round((x1 + pad)), Math.round(yTitle), Color.WHITE);
                    String st = (!(!alive) ? "S\u1ed1ng S\u00f3t" : "\u0110\u00e3 ch\u1ebft");
                    float pw = (((float) LightningClient.FONT_MANAGER.getWidth(st)) + 8.0f);
                    float px1 = ((x2 - pad) - pw);
                    if (!(!alive)) {
                    } else {
                    }
                    LightningSkin.drawRoundedRectAll(px1, (yTitle - 2.0f), (px1 + pw), ((yTitle + ((float) fh)) + 1.0f), 4.0f, new Color(30, 90, 65, 200), new Color(100, 35, 45, 200));
                    LightningClient.FONT_MANAGER.drawText(g, st, Math.round((px1 + 4.0f)), Math.round(yTitle), (!(!alive) ? green : red));
                    drawHead(g, myName, (x1 + pad), yHead, headSize);
                    int tx = Math.round((((x1 + pad) + headSize) + 6.0f));
                    LightningClient.FONT_MANAGER.drawTextWithShadow(g, myName, tx, Math.round((yHead + 2.0f)), Color.WHITE);
                    if (FightLogModule.hasLast()) {
                        Object lastTxt = ("Tr\u1eadn g\u1ea7n nh\u1ea5t: " + (FightLogModule.isLastWin() ? "th\u1eafng" : "thua"));
                        Color lastCol = (FightLogModule.isLastWin() ? green : red);
                    } else {
                        lastTxt = "Ch\u01b0a c\u00f3 tr\u1eadn n\u00e0o";
                        lastCol = grey;
                    }
                    LightningClient.FONT_MANAGER.drawText(g, ((String) lastTxt), tx, Math.round(((yHead + 3.0f) + ((float) fh))), lastCol);
                    float gapB = 4.0f;
                    float bw = ((inner - (gapB * 2.0f)) / 3.0f);
                    var tmp2 = new String[3];
                    tmp2[0] = String.valueOf(kills);
                    tmp2[1] = String.valueOf(lostTotems);
                    tmp2[2] = String.valueOf(survived);
                    String[] vals = tmp2;
                    var tmp3 = new String[3];
                    tmp3[0] = "H\u1ea1 g\u1ee5c";
                    tmp3[1] = "Totem m\u1ea5t";
                    tmp3[2] = "S\u1ed1ng s\u00f3t";
                    String[] lbls = tmp3;
                    var tmp4 = new Color[3];
                    tmp4[0] = new Color(60, 225, 200);
                    tmp4[1] = new Color(251, 191, 36);
                    tmp4[2] = new Color(56, 189, 248);
                    Color[] cols = tmp4;
                    int i = 0;
                    while (i < 3) {
                        float bx1 = ((x1 + pad) + ((bw + gapB) * ((float) i)));
                        LightningSkin.drawRoundedRectAll(g, bx1, yStat, (bx1 + bw), (yStat + statH), 4.0f, new Color(26, 36, 46, 235));
                        float vw = (((float) LightningClient.FONT_MANAGER.getWidth(vals[i])) * numScale);
                        drawScaledText(g, vals[i], (bx1 + ((bw - vw) / 2.0f)), (yStat + 4.0f), numScale, cols[i]);
                        float ls = lblScale;
                        float lw = (((float) LightningClient.FONT_MANAGER.getWidth(lbls[i])) * ls);
                        if (lw > (bw - 4.0f)) {
                            ls = ((bw - 4.0f) / ((float) LightningClient.FONT_MANAGER.getWidth(lbls[i])));
                            lw = (((float) LightningClient.FONT_MANAGER.getWidth(lbls[i])) * ls);
                        }
                        drawScaledText(g, lbls[i], (bx1 + ((bw - lw) / 2.0f)), (((yStat + 4.0f) + (((float) fh) * numScale)) + 1.0f), ls, grey);
                        i++;
                    }
                    LightningClient.FONT_MANAGER.drawText(g, "L\u1ecaCH S\u1eec TR\u1eacN", Math.round((x1 + pad)), Math.round(yHist), grey);
                    if (hist.isEmpty()) {
                        LightningClient.FONT_MANAGER.drawText(g, "Ch\u01b0a c\u00f3 tr\u1eadn n\u00e0o", Math.round((x1 + pad)), Math.round(yRows), new Color(110, 110, 140));
                        break label_118;
                    }
                    i = 0;
                    while (i < rows && i < hist.size()) {
                        FightLogModule.Entry e = ((FightLogModule.Entry) hist.get(i));
                        float ry = (yRows + (rowH * ((float) i)));
                        Color cc = (!(!e.win) ? green : red);
                        float dy = ((ry + ((((float) fh) - 4.0f) / 2.0f)) + 0.5f);
                        LightningSkin.drawRoundedRectAll(g, (x1 + pad), dy, ((x1 + pad) + 4.0f), (dy + 4.0f), 2.0f, cc);
                        String right = ((!(!e.win) ? "H\u1ea1 g\u1ee5c" : "B\u1ecb h\u1ea1") + " -" + e.totems);
                        int rw = LightningClient.FONT_MANAGER.getWidth(right);
                        LightningClient.FONT_MANAGER.drawText(g, right, Math.round(((x2 - pad) - ((float) rw))), Math.round(ry), cc);
                        String nm = (e.name == null ? "?" : e.name);
                        float maxW = (((inner - 8.0f) - ((float) rw)) - 6.0f);
                        while (nm.length() > 2 && ((float) LightningClient.FONT_MANAGER.getWidth(nm)) > maxW) {
                            nm = nm.substring(0, (nm.length() - 1));
                        }
                        LightningClient.FONT_MANAGER.drawText(g, nm, Math.round(((x1 + pad) + 8.0f)), Math.round(ry), new Color(225, 225, 240));
                        i++;
                    }
                    label_118:
                    LightningSkin.drawRoundedRectAll(g, (x1 + pad), yFoot, (x2 - pad), (yFoot + footH), 4.0f, new Color(28, 60, 50, 170));
                    String ft = "\u0110\u00e3 l\u01b0u v\u00e0o module FightLog";
                    int fw = LightningClient.FONT_MANAGER.getWidth(ft);
                    LightningClient.FONT_MANAGER.drawText(g, ft, Math.round((x1 + ((w - ((float) fw)) / 2.0f))), Math.round((yFoot + 3.0f)), new Color(120, 225, 160));
                } catch (Throwable ex123) {
                    i = ex123;
                }
            }
        }
    }

    private void renderInfoCard(GuiGraphicsExtractor g) {
        try {
            if (!(this.frames.isEmpty())) break label_2;
            return;
            try {
                label_2:
                Frame core = ((Frame) this.frames.get((this.frames.size() - 1)));
                Minecraft mc = Minecraft.getInstance();
                int fh = LightningClient.FONT_MANAGER.getHeight();
                String name = "LightningClient";
                Object server = "Singleplayer";
                Object ping = "--";
                int pingVal = -1;
                try {
                    Object sd = mc.getCurrentServer();
                    if (sd == null) break label_13;
                    var tmp0 = sd.getClass().getField("ip").get(sd);
                    Object ipVal = tmp0;
                    if (tmp0 == null) break label_13;
                    server = String.valueOf(ipVal);
                    label_13:
                } catch (Throwable ex14) {
                    ipVal = ex14;
                    try {
                    }
                    Object conn = mc.getConnection();
                    Object player = selfPlayer();
                    if ((conn == null) || (player == null)) break label_24;
                    Object uuid = player.getClass().getMethod("getUUID", new Class[0]).invoke(player, new Object[0]);
                    var tmp1 = new Class[1];
                    tmp1[0] = UUID.class;
                    var tmp2 = new Object[1];
                    tmp2[0] = uuid;
                    Object info = conn.getClass().getMethod("getPlayerInfo", tmp1).invoke(conn, tmp2);
                    if (info == null) break label_24;
                    Object lat = info.getClass().getMethod("getLatency", new Class[0]).invoke(info, new Object[0]);
                    pingVal = ((Number) lat).intValue();
                    ping = (pingVal + "ms");
                    label_24:
                } catch (Throwable ex25) {
                    ipVal = ex25;
                    try {
                    }
                    String sv = LightningClient.SERVER_MANAGER.getServer();
                    if (sv != null && !(sv.isEmpty())) {
                        server = sv;
                    }
                    var tmp3 = LightningClient.SERVER_MANAGER.getPing();
                    int pg = tmp3;
                    if (tmp3 > 0) {
                        pingVal = pg;
                        ping = (pg + "ms");
                    }
                } catch (Throwable ex34) {
                    ipVal = ex34;
                }
                int enabled = 0;
                sd = Module.Category.values();
                uuid = sd.length;
                info = 0;
                while (info < uuid) {
                    Module.Category cat = sd[info];
                    float rowH = LightningClient.MODULE_MANAGER.getModules(cat).iterator();
                    while (rowH.hasNext()) {
                        Module m = ((Module) rowH.next());
                        if (!(m.isToggled())) {
                            break label_39;
                        }
                        enabled++;
                    }
                    info++;
                }
                float x1 = ((float) core.getX());
                float w = ((float) core.getWidth());
                float x2 = (x1 + w);
                float y1 = ((float) ((((core.getY() + core.getHeight()) + Math.min((core.getButtons().size() * 14), core.getMaxViewHeight())) + 4) + 8));
                rowH = (((float) fh) + 3.0f);
                float h = ((41.0f + (rowH * 2.0f)) + 4.0f);
                float y2 = (y1 + h);
                float scrH = (((float) mc.getWindow().getGuiScaledHeight()) / Math.max(0.01f, this.appliedScale));
                if (y2 > (scrH - 4.0f)) {
                    return;
                    try {
                    }
                    Color teal = new Color(60, 220, 200, 230);
                    Color grey = new Color(150, 150, 160);
                    LightningSkin.drawRoundedRect(g, x1, y1, x2, y2, 6.0f, true, true, true, true, new Color(20, 20, 24, 238), new Color(15, 15, 18, 240));
                    LightningSkin.glow(g, x1, y1, x2, y2, 6.0f, teal);
                    NeonFx.ring(g, x1, y1, x2, y2, 6.0f, 0.8f, teal);
                    float ix1 = (x1 + 7.0f);
                    float iy1 = (y1 + 7.0f);
                    LightningSkin.drawRoundedRectAll(g, ix1, iy1, (ix1 + 22.0f), (iy1 + 22.0f), 5.0f, new Color(52, 40, 95, 255));
                    try {
                        LightningSkin.bolt(g, (ix1 + 5.0f), (iy1 + 3.0f), 16.0f, 253);
                    } catch (Throwable ex64) {
                        int tx = ex64;
                    }
                    tx = Math.round(((ix1 + 22.0f) + 6.0f));
                    LightningClient.FONT_MANAGER.drawTextWithShadow(g, name, tx, Math.round((iy1 + 2.0f)), Color.WHITE);
                    Object srv = server;
                    float maxW = ((x2 - 6.0f) - ((float) tx));
                    while (((String) srv).length() > 3 && ((float) LightningClient.FONT_MANAGER.getWidth(((String) srv))) > maxW) {
                        srv = ((String) srv).substring(0, (((String) srv).length() - 1));
                    }
                    LightningClient.FONT_MANAGER.drawText(g, ((String) srv), tx, Math.round(((iy1 + 3.0f) + ((float) fh))), new Color(120, 190, 200));
                    float sy = ((iy1 + 22.0f) + 6.0f);
                    Renderer2D.renderQuad(g, (x1 + 7.0f), sy, (x2 - 7.0f), (sy + 1.0f), new Color(255, 255, 255, 40));
                    float ry = ((sy + 1.0f) + 5.0f);
                    if (pingVal < 0) {
                        break label_79;
                    }
                    if (pingVal <= 80) {
                        break label_79;
                    }
                    if (pingVal <= 160) {
                        break label_79;
                    }
                    label_79:
                    Color pc = new Color(240, 90, 90);
                    LightningClient.FONT_MANAGER.drawText(g, "Ping", Math.round((x1 + 8.0f)), Math.round(ry), grey);
                    int pw = LightningClient.FONT_MANAGER.getWidth(((String) ping));
                    LightningClient.FONT_MANAGER.drawText(g, ((String) ping), Math.round(((x2 - 8.0f) - ((float) pw))), Math.round(ry), pc);
                    float ry2 = (ry + rowH);
                    LightningClient.FONT_MANAGER.drawText(g, "B\u1eadt", Math.round((x1 + 8.0f)), Math.round(ry2), grey);
                    String en = String.valueOf(enabled);
                    int ew = LightningClient.FONT_MANAGER.getWidth(en);
                    LightningClient.FONT_MANAGER.drawText(g, en, Math.round(((x2 - 8.0f) - ((float) ew))), Math.round(ry2), Color.WHITE);
                } catch (Throwable ex89) {
                    core = ex89;
                }
            }
        }
    }

    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double d2) {
        double d3 = (mouseButtonEvent.x() / ((double) this.appliedScale));
        double d4 = (mouseButtonEvent.y() / ((double) this.appliedScale));
        int n = mouseButtonEvent.button();
        Object var11 = this.frames.iterator();
        while (var11.hasNext()) {
            Frame frame = ((Frame) var11.next());
            frame.mouseDragged(d3, d4, n, (d / ((double) this.appliedScale)), (d2 / ((double) this.appliedScale)));
        }
        return super.mouseDragged(mouseButtonEvent, d, d2);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double d2 = (mouseButtonEvent.x() / ((double) this.appliedScale));
        double d = (mouseButtonEvent.y() / ((double) this.appliedScale));
        var tmp0 = mouseButtonEvent.button();
        int n = tmp0;
        if (this.searchFrame.mouseClicked(d2, (mouseButtonEvent.y() / ((double) this.appliedScale)), tmp0)) return true;
        Object var8 = this.frames.iterator();
        while (var8.hasNext()) {
            Frame frame = ((Frame) var8.next());
            frame.mouseClicked(d2, d, n);
        }
        this.descriptionFrame.mouseClicked(d2, d, n);
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        double d = (mouseButtonEvent.x() / ((double) this.appliedScale));
        double d2 = (mouseButtonEvent.y() / ((double) this.appliedScale));
        int n = mouseButtonEvent.button();
        Object var7 = this.frames.iterator();
        while (var7.hasNext()) {
            Frame frame = ((Frame) var7.next());
            frame.mouseReleased(d, d2, n);
        }
        this.descriptionFrame.mouseReleased(d, d2, n);
        return super.mouseReleased(mouseButtonEvent);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        double d5 = (d / ((double) this.appliedScale));
        double d6 = (d2 / ((double) this.appliedScale));
        Object var13 = this.frames.iterator();
        while (var13.hasNext()) {
            Frame frame = ((Frame) var13.next());
            frame.mouseScrolled(d5, d6, d3, d4);
        }
        return false;
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int n = keyEvent.key();
        int n2 = keyEvent.scancode();
        int n3 = keyEvent.modifiers();
        if (n == 256 && !(BindButton.isAnyListening())) {
            requestClose();
            return true;
        }
        if (n == ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).getBind()) {
            if (!(!this.closing)) {
                cancelClose();
            }
            return true;
        }
        boolean bl = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), (Util.getPlatform() == Util.OS.OSX ? 343 : 341));
        if (!(!bl) && n == 70) {
            this.searchFrame.toggle();
            return true;
        }
        if (this.searchFrame.keyPressed(n, n2, n3)) return true;
        Object var6 = this.frames.iterator();
        while (var6.hasNext()) {
            Frame frame = ((Frame) var6.next());
            frame.keyPressed(n, n2, n3);
        }
        if (n == 256) return true;
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char c = ((char) characterEvent.codepoint());
        int n = 0;
        if (this.searchFrame.charTyped(c, 0)) return true;
        Object var4 = this.frames.iterator();
        while (var4.hasNext()) {
            Frame frame = ((Frame) var4.next());
            frame.charTyped(c, n);
        }
        return super.charTyped(characterEvent);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        ClickGuiModule clickGuiModule = ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class));
        if (clickGuiModule.blur.getValue()) {
            guiGraphicsExtractor.blurBeforeThisStratum();
        }
        Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, ((float) sw), ((float) sh), new Color(8, 8, 8, 140));
        String string = clickGuiModule.background.getValue();
        int n3 = Arrays.asList(ClickGuiModule.BG_MODES).indexOf(string);
        if (n3 >= 0 && n3 < (ClickGuiModule.BG_MODES.length - 1)) {
            float f2 = clickGuiModule.bgSpeed.getValue().floatValue();
            float f3 = (clickGuiModule.bgOpacity.getValue().floatValue() / 100.0f);
            Color color = clickGuiModule.color.getColor();
            boolean bl = clickGuiModule.isRainbow();
            ClickGuiBackground.renderBackground(guiGraphicsExtractor, sw, sh, n3, f2, f3, color, bl);
        }
    }

    public void removed() {
        this.searchFrame.setQuery("");
        this.searchFrame.setCursorIndex(0);
        this.searchFrame.setFocused(false);
        this.searchFrame.setVisible(false);
        super.removed();
        ((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).setToggled(false);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public static Color getButtonColor(int n, int n2) {
        if (((ClickGuiModule) LightningClient.MODULE_MANAGER.getModule(ClickGuiModule.class)).isRainbow()) {
        } else {
        }
        Color color = LightningSkin.accent(n);
        return ColorUtils.getColor(color, n2);
    }

    public ArrayList getFrames() {
        return this.frames;
    }

    public ArrayList getButtons() {
        return this.buttons;
    }

    public DescriptionFrame getDescriptionFrame() {
        return this.descriptionFrame;
    }

    public SearchFrame getSearchFrame() {
        return this.searchFrame;
    }

    public PingBypassFrame getPingBypassFrame() {
        return this.pingBypassFrame;
    }

    public Timer getLineTimer() {
        return this.lineTimer;
    }

    public boolean isShowLine() {
        return this.showLine;
    }

    public Color getColorClipboard() {
        return this.colorClipboard;
    }

    public float getAppliedScale() {
        return this.appliedScale;
    }

    public Animation getSlideAnim() {
        return this.slideAnim;
    }

    public boolean isClosing() {
        return this.closing;
    }

    public void setShowLine(boolean bl) {
        this.showLine = bl;
    }

    public void setColorClipboard(Color color) {
        this.colorClipboard = color;
    }

    public void setAppliedScale(float f) {
        this.appliedScale = f;
    }

    public void setClosing(boolean bl) {
        this.closing = bl;
    }

    public void setReadyToRemove(boolean bl) {
        this.readyToRemove = bl;
    }

    static {
        headLogged = false;
    }

}
