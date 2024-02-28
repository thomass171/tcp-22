package de.yard.threed.engine.gui;

import de.yard.threed.engine.Camera;

@FunctionalInterface
public interface MenuBuilder {
    /**
     * 28.2.24: camera added, might be a deferred camera
     */
    Menu buildMenu(Camera camera);
}
