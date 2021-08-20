package de.yard.threed.engine.gui;

import de.yard.threed.engine.Camera;

/**
 * Just a provider/builder for a control menu.
 *
 */
public interface ControlMenuBuilder {
    GuiGrid buildControlMenu(Camera camera);

}
