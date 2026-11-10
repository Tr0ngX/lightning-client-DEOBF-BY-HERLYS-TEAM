package lightning.modules.impl.core;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.CategorySetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.text.FormattingUtils;

@RegisterModule(name = "Commands", description = "Manages the client's command and chat message system.", category = Module.Category.CORE, persistent = true, drawn = false)
public class CommandsModule extends Module {
    public CategorySetting watermarkCategory;
    public BooleanSetting watermark;
    public StringSetting watermarkText;
    public StringSetting opening;
    public StringSetting closing;
    public ModeSetting primaryWatermarkColor;
    public ModeSetting secondaryWatermarkColor;
    public CategorySetting messageCategory;
    public ModeSetting primaryMessageColor;
    public ModeSetting secondaryMessageColor;

    public CommandsModule() {
        this.watermarkCategory = new CategorySetting("Watermark", "The category that contains all of the settings for the chat watermark.");
        this.watermark = new BooleanSetting("Watermark", "Enabled", "Whether or not to have the client's watermark show up before every chat message.", new CategorySetting.Visibility(this.watermarkCategory), true);
        this.watermarkText = new StringSetting("WatermarkText", "Text", "The text that will be shown as the watermark.", new CategorySetting.Visibility(this.watermarkCategory), "Lightning");
        this.opening = new StringSetting("Opening", "The symbol that will be placed before the watermark text.", new CategorySetting.Visibility(this.watermarkCategory), "[");
        this.closing = new StringSetting("Closing", "The symbol that will be placed after the watermark text.", new CategorySetting.Visibility(this.watermarkCategory), "]");
        this.primaryWatermarkColor = new ModeSetting("PrimaryWatermarkColor", "Primary", "The primary color for the watermark.", new CategorySetting.Visibility(this.watermarkCategory), "Client", FormattingUtils.FORMATS);
        this.secondaryWatermarkColor = new ModeSetting("SecondaryWatermarkColor", "Secondary", "The secondary color for the watermark.", new CategorySetting.Visibility(this.watermarkCategory), "Dark Gray", FormattingUtils.FORMATS);
        this.messageCategory = new CategorySetting("Message", "The category that contains all of the settings for the chat messages themselves.");
        this.primaryMessageColor = new ModeSetting("PrimaryMessageColor", "Primary", "The primary color for the message.", new CategorySetting.Visibility(this.messageCategory), "Client", FormattingUtils.FORMATS);
        this.secondaryMessageColor = new ModeSetting("SecondaryMessageColor", "Secondary", "The secondary color for the message.", new CategorySetting.Visibility(this.messageCategory), "White", FormattingUtils.FORMATS);
    }

}
