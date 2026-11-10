package lightning.gui.api;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import lightning.LightningClient;
import lightning.gui.ClickGuiScreen;
import lightning.gui.impl.ProxyModuleButton;
import lightning.modules.impl.core.PingBypassModule;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PingBypassFrame extends Frame {
    private static final String CATEGORY_NAME = "PingBypass";
    private Set lastKnownModules;

    public PingBypassFrame(int arg0, int arg1, int arg2, int arg3) {
        super(null, arg0, arg1, arg2, arg3);
        this.lastKnownModules = Collections.emptySet();
    }

    private boolean isActive() {
        Object var1 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        if ((var1 == null) || (!(var1.isToggled()))) break label_0;
        return !(var1.getProxyModuleStates().isEmpty());
    }

    private void refreshButtons() {
        Object var1 = ((PingBypassModule) LightningClient.MODULE_MANAGER.getModule(PingBypassModule.class));
        if (var1 == null) return;
        Object var2 = new TreeSet(var1.getProxyModuleStates().keySet());
        if (var2.equals(this.lastKnownModules)) return;
        this.lastKnownModules = var2;
        getButtons().clear();
        Object var3 = var2.iterator();
        while (var3.hasNext()) {
            Object var4 = ((String) var3.next());
            getButtons().add(new ProxyModuleButton(var4, this, getHeight()));
        }
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3, String arg4) {
        if (!(isActive())) return;
        refreshButtons();
        if (!(isDragging())) break label_1;
        setX((arg1 - getDragX()));
        setY((arg2 - getDragY()));
        label_1:
        if (arg4 == null) break label_2;
        int var6 = (!(arg4.isEmpty()) ? 1 : 0);
        int var7 = getHeight();
        if (!(isOpen())) break label_9;
        var7++;
        Object var8 = getButtons().iterator();
        while (var8.hasNext()) {
            Object var9 = ((Button) var8.next());
            if (!(!var6) && (var9 instanceof ProxyModuleButton)) {
                Object var10 = ((ProxyModuleButton) var9);
                if (!(var10.getModuleName().toLowerCase().contains(arg4.toLowerCase()))) {
                    var9.setVisible(false);
                    break label_4;
                }
                var9.setVisible(true);
            } else {
                var9.setVisible(true);
            }
            if (!(var9.isVisible())) {
                break label_4;
            }
            var9.setX(getX());
            var9.setY((getY() + var7));
            var7 = (var7 + var9.getHeight());
        }
        label_9:
        setTotalHeight(var7);
        Renderer2D.renderQuad(arg0, ((float) getX()), ((float) getY()), ((float) (getX() + getWidth())), ((float) (getY() + getHeight())), new Color(21, 21, 21, 200));
        var8 = ClickGuiScreen.getButtonColor(getY(), 200);
        Renderer2D.renderQuad(arg0, ((float) getX()), ((float) ((getY() + getHeight()) - 1)), ((float) (getX() + getWidth())), ((float) (getY() + getHeight())), var8);
        LightningClient.FONT_MANAGER.drawTextWithShadow(arg0, "PingBypass", (getX() + getTextPadding()), (getY() + 2), Color.WHITE);
        if (!(isOpen())) break label_12;
        Renderer2D.renderQuad(arg0, ((float) getX()), ((float) (getY() + getHeight())), ((float) (getX() + getWidth())), ((float) ((getY() + var7) + 1)), new Color(16, 16, 16, 180));
        var9 = getButtons().iterator();
        while (var9.hasNext()) {
            var10 = ((Button) var9.next());
            if (!(var10.isVisible())) {
                break label_10;
            }
            var10.render(arg0, arg1, arg2, arg3);
        }
        label_12:
    }

    public void render(GuiGraphicsExtractor arg0, int arg1, int arg2, float arg3) {
        render(arg0, arg1, arg2, arg3, "");
    }

    public void mouseClicked(double arg0, double arg1, int arg2) {
        if (!(isActive())) return;
        if (!(isHovering(arg0, arg1))) break label_4;
        if (arg2 == 0) {
            setDragging(true);
            setDragX(((int) (arg0 - ((double) getX()))));
            setDragY(((int) (arg1 - ((double) getY()))));
            break label_4;
        }
        if (arg2 != 1) break label_4;
        setOpen(!(isOpen()));
        label_4:
        if (!(isOpen())) break label_7;
        Object var6 = getButtons().iterator();
        while (var6.hasNext()) {
            Object var7 = ((Button) var6.next());
            if (!(var7.isVisible())) {
                break label_5;
            }
            var7.mouseClicked(arg0, arg1, arg2);
        }
        label_7:
    }

    public void mouseReleased(double arg0, double arg1, int arg2) {
        if (!(isActive())) return;
        if (arg2 == 0) {
            setDragging(false);
        }
        Object var6 = getButtons().iterator();
        while (var6.hasNext()) {
            Object var7 = ((Button) var6.next());
            if (!(var7.isVisible())) {
                break label_2;
            }
            var7.mouseReleased(arg0, arg1, arg2);
        }
    }

    public void mouseScrolled(double arg0, double arg1, double arg2, double arg3) {
        if (!(isActive())) return;
        super.mouseScrolled(arg0, arg1, arg2, arg3);
    }

    public void keyPressed(int arg0, int arg1, int arg2) {
        if (!(isActive())) return;
        if (!(isOpen())) break label_3;
        Object var4 = getButtons().iterator();
        while (var4.hasNext()) {
            Object var5 = ((Button) var4.next());
            if (!(var5.isVisible())) {
                break label_1;
            }
            var5.keyPressed(arg0, arg1, arg2);
        }
        label_3:
    }

    public void charTyped(char arg0, int arg1) {
        if (!(isActive())) return;
        if (!(isOpen())) break label_3;
        Object var3 = getButtons().iterator();
        while (var3.hasNext()) {
            Object var4 = ((Button) var3.next());
            if (!(var4.isVisible())) {
                break label_1;
            }
            var4.charTyped(arg0, arg1);
        }
        label_3:
    }

}
