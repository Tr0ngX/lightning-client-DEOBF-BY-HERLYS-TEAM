package lightning.modules.api;

import java.awt.Color;

public interface IAmbienceModule {
    boolean isToggled();

    Color getColor();

    boolean isLightmapEnabled();

    boolean isSkyEnabled();

    boolean isBlocksEnabled();

}
