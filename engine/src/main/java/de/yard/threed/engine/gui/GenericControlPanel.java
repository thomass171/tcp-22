package de.yard.threed.engine.gui;

import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;

/**
 * 3.5.21: Not necessarily a menu.
 */
public interface GenericControlPanel {

    /**
     * 30.12.19: Cannot send an ECS request here because of ECS independence.
     * Uses delegates for triggering an action.
     * Return true if any button/area was clicked, false otherwise.
     */
    boolean checkForClickedArea(Ray ray);
}
