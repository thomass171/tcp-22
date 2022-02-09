package de.yard.threed.engine.gui;

import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;

/**
 * A menu based on ControlPanel.
 *
 * 7.2.22
 */
public class ControlPanelMenu implements Menu {
    public ControlPanel controlPanel;

    public ControlPanelMenu(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }


    @Override
    public SceneNode getNode() {
        return controlPanel;
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {

        return controlPanel.checkForClickedArea(ray);
    }

    @Override
    public void checkForSelectionByKey(int position) {
        // Needs key bindings. For now not possible in a control panel.
    }

    @Override
    public void remove() {
        if (controlPanel != null) {
            // should be sufficient to remove the panel (all elements are attached, so will be removed)
            controlPanel.remove();
        }
        controlPanel = null;
    }
}
