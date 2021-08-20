package de.yard.threed.engine.apps.vr;


import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.gui.BrowseMenu;
import de.yard.threed.engine.gui.Menu;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.vr.VRController;
import de.yard.threed.core.DimensionF;

/**
 * Ein Browsemenu, dass durch einen Button geöffnet/geschlossen wird.
 */
public class VrMainMenu implements Menu {
    Log logger;
    BrowseMenu menu;
    VRController controller1;
    Camera camera;

    VrMainMenu(Camera camera, Log logger, MenuItem[][] menuitems, VRController controller1) {
        this.logger = logger;
        this.camera = camera;
        //7.10.19: Mal nicht auf near plane sondern 3 in der Tiefe (wegen VR Verzerrung?)
        //Dann ist es hinterm Balken. Erstmal versuchen.
        //menu = BrowseMenu.buildForCamera(camera, menuitems);
        menu = new BrowseMenu(new DimensionF(3, 2), -3, -2.9, menuitems);
    }


    @Override
    public SceneNode getNode() {
        return menu.grid;
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {
        if (menu != null) {
            //geht per Delegate
            return menu.checkForButtonByRay(ray);
        }
        return false;
    }

    @Override
    public void checkForSelectionByKey(int position) {

    }

    @Override
    public void remove() {
        menu.remove();
        menu = null;
    }

}


