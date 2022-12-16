package de.yard.threed.engine.gui;

import de.yard.threed.core.Point;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;

/**
 * Just a provider/builder for a menu.
 *
 */
public interface MenuProvider {

    /**
     * 7.2.22: To make that clear: Only builds the menu, but doesn't attach it to a parent/camera. For attaching getAttachNode() is provided.
     */
    Menu buildMenu();

    //besser Camera als SceneNode liefern, die brauchts für picking ray bei mouseclick.
    //wobei, besser vielleicht beides?
    /*SceneNode*/Transform getAttachNode();

    //Camera getCamera();

    /**
     * either mouse button (mouselocation!=null) or right(!) (mouselocation==null) VR button was pressed
     * @param mouselocation
     */
    Ray getRayForUserClick(Point mouselocation);

    /**
     * Just a callback.
     */
    void menuBuilt();
}
