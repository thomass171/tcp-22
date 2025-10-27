package de.yard.threed.engine.gui;

import de.yard.threed.engine.*;

/**
 * This is not very specific. Just a scene node (control panel) that can be opened/closed (shown/hidden) and where parts can be selected/clicked/pressed.
 * Different to a control panel it can be closed/removed.
 * To be used by MenuBuilder/MenuCycler?
 */
public interface Menu extends GenericControlPanel {
    /**
     * Returns null if the menu was closed to indicate that. So getNode()==null is a "not closed" check.
     *
     * @return
     */
    SceneNode getNode();

    //position starting with 1
    //27.10.25 not sure whether selectionbykey was an option ever?
    @Deprecated
    void checkForSelectionByKey(int position);

    /**
     * Opposite of getNode()?
     */
    void remove();
}
