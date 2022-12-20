package de.yard.threed.engine.gui;

import de.yard.threed.engine.*;

/**
 * This is not very specific. Just a scene node (control panel) that can be opened/closed (shown/hidden) and where parts can be selected/clicked/pressed.
 * Different to a control panel it can be closed/removed.
 */
public interface Menu extends GenericControlPanel {
    /**
     * Returns null if the menu was closed to indicate that. So getNode()==null is a "not closed" check.
     *
     * @return
     */
    SceneNode getNode();//build();



    //position starting with 1
    void checkForSelectionByKey(int position);

    /**
     * Opposite of getNode()?
     */
    void remove();
    //VRController getController1();
    //der Name getCamera() führt zu haesslichen Vererbungseffekte
    //Camera getMenuCamera();
}
