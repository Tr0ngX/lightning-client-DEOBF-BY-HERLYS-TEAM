package lightning.gui.api;

import java.util.List;

public interface ExpandableRow {
    String getRowName();

    boolean isOpen();

    float getOpenAmount();

    List getButtons();

    void setRevealHeight(int arg0);

    void setSearchQuery(String arg0);

}
