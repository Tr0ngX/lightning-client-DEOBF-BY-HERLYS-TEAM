package lightning.modules.impl.miscellaneous;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lombok.Generated;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;

@RegisterModule(name = "BetterChat", description = "Improves the default minecraft chat.", category = Module.Category.MISCELLANEOUS)
public class BetterChatModule extends Module {
    public BooleanSetting noIndicators;
    public NumberSetting offset;
    public BooleanSetting animation;
    public NumberSetting delay;
    public NumberSetting textAlpha;
    public BooleanSetting timestamps;
    public StringSetting opening;
    public StringSetting closing;
    public ModeSetting background;
    public ColorSetting color;
    public BooleanSetting highlight;
    public ColorSetting highlightColor;
    public BooleanSetting highlightSound;
    private final Map animationMap;

    public BetterChatModule() {
        this.noIndicators = new BooleanSetting("NoIndicators", "Removes indicators from the chat.", false);
        this.offset = new NumberSetting("Offset", "The offset for the chat on the X axis.", 0, -100, 100);
        this.animation = new BooleanSetting("Animation", "Adds in an animation when adding new messages.", false);
        this.delay = new NumberSetting("Delay", "The delay of the animation.", new BooleanSetting.Visibility(this.animation, true), 200, 0, 300);
        this.textAlpha = new NumberSetting("TextAlpha", "The alpha of the chat text.", 255, 0, 255);
        this.timestamps = new BooleanSetting("Timestamps", "Adds timestamps to every chat message.", true);
        this.opening = new StringSetting("Opening", "The symbol that will be placed before the timestamp text.", new BooleanSetting.Visibility(this.timestamps, true), "<");
        this.closing = new StringSetting("Closing", "The symbol that will be placed after the timestamp text.", new BooleanSetting.Visibility(this.timestamps, true), ">");
        var tmp0 = new String[3];
        tmp0[0] = "Default";
        tmp0[1] = "Clear";
        tmp0[2] = "Custom";
        this.background = new ModeSetting("Background", "The background mode for the chat.", "Default", tmp0);
        var tmp1 = new String[1];
        tmp1[0] = "Custom";
        this.color = new ColorSetting("Color", "The color of the chat background.", new ModeSetting.Visibility(this.background, tmp1), new ColorSetting.Color(new Color(0, 0, 0, 127), false, false));
        this.highlight = new BooleanSetting("Highlight", "Highlights your name in incoming chat messages.", true);
        this.highlightColor = new ColorSetting("HighlightColor", "The color used to highlight your name in chat.", new BooleanSetting.Visibility(this.highlight, true), new ColorSetting.Color(new Color(85, 255, 255), false, false));
        this.highlightSound = new BooleanSetting("HighlightSound", "Plays a ding notification sound when your name is mentioned in chat.", new BooleanSetting.Visibility(this.highlight, true), true);
        this.animationMap = new HashMap();
    }

    public Long getAnimationStart(FormattedCharSequence arg0) {
        Object var2 = this.animationMap.entrySet().iterator();
        label_0:
        if (var2.hasNext()) {
            Object var3 = ((Map.Entry) var2.next());
            if (((GuiMessage.Line) var3.getKey()).content() != arg0) {
                break label_0;
            }
            return ((Long) var3.getValue());
        }
        return null;
    }

    @Generated
    public Map getAnimationMap() {
        return this.animationMap;
    }

}
