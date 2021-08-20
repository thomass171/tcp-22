package de.yard.threed.engine.gui;

import de.yard.threed.core.Point;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;

/**
 * Just a provider/builder for a menu.
 *
 */
public interface MenuProvider {
    Menu buildMenu();

    //besser Camera als SceneNode liefern, die brauchts für picking ray bei mouseclick.
    //wobei, besser vielleicht beides?
    SceneNode getAttachNode();

    //Camera getCamera();

    /**
     * either mouse button (mouselocation!=null) or right(!) (mouselocation==null) VR button was pressed
     * @param mouselocation
     */
    Ray getRayForUserClick(Point mouselocation);
}
