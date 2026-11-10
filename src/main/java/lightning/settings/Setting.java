package lightning.settings;

import lombok.Generated;

public class Setting {
    private final String name;
    private final String tag;
    private final String description;
    private final Setting.Visibility visibility;

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getTag() {
        return this.tag;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public Setting.Visibility getVisibility() {
        return this.visibility;
    }

    @Generated
    public Setting(String arg0, String arg1, String arg2, Setting.Visibility arg3) {
        this.name = arg0;
        this.tag = arg1;
        this.description = arg2;
        this.visibility = arg3;
    }


    public class Visibility {
        private final Setting setting;
        private boolean visible;

        public Visibility() {
            this.visible = true;
            this.setting = null;
        }

        public void update() {
        }

        @Generated
        public Setting getSetting() {
            return this.setting;
        }

        @Generated
        public boolean isVisible() {
            return this.visible;
        }

        @Generated
        public void setVisible(boolean arg0) {
            this.visible = arg0;
        }

        @Generated
        public Visibility(Setting arg0) {
            this.visible = true;
            this.setting = arg0;
        }

    }
}
