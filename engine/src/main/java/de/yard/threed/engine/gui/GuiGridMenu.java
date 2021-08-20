package de.yard.threed.engine.gui;

import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;

/**
 * A menu based on GuiGrid.
 * Ein Browsemenu, dass durch einen Button geöffnet/geschlossen wird.
 * 20.3.20
 */
public class GuiGridMenu implements Menu {
    public GuiGrid menu;

    public GuiGridMenu(GuiGrid guiGrid) {
        menu = guiGrid;
    }


    @Override
    public SceneNode getNode() {
        return menu;
    }

    /**
     * geht per Delegate
     *
     * @param ray
     * @return
     */
    @Override
    public boolean checkForClickedArea(Ray ray) {

        return menu.checkForClickedArea(ray);
    }

    @Override
    public void checkForSelectionByKey(int position) {
        menu.checkForSelectionByKey(position);
    }

    @Override
    public void remove() {
        if (menu != null) {


            menu.remove();
        }
        menu = null;
    }
}
