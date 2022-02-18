package de.yard.threed.engine.gui;


import de.yard.threed.engine.Camera;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.vr.VrInstance;

/**
 * A menu provider that provides the menu hud like to the user, ie. attached to the viewpoint(camera).
 * 27.1.2022 No longer related to avatar but some observer.
 * 8.2.22: Not only for a Observer but in general for any type of camera.
 * 10.2.22: In VR the menu will be attached to camera space ahead of view, but doesn't move synced with head.
 *
 * <p>
 * 26.11.2019
 */
public class DefaultMenuProvider implements MenuProvider {

    Camera cameraForMouseClick;
    MenuBuilder menuBuilder;

    public DefaultMenuProvider(Camera cameraForMouseClick, MenuBuilder menuBuilder) {

        this.cameraForMouseClick = cameraForMouseClick;
        this.menuBuilder = menuBuilder;
    }

    @Override
    public Transform getAttachNode() {
        return cameraForMouseClick.getCarrier().getTransform();
    }

    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            return cameraForMouseClick.buildPickingRay(cameraForMouseClick.getCarrierTransform(), mouselocation);
        }
        Ray ray = VrInstance.getInstance().getController(1).getRay();
        return ray;
    }

    @Override
    public Menu buildMenu() {
        return menuBuilder.buildMenu();
    }
}
