package de.yard.threed.engine.gui;


import de.yard.threed.engine.Camera;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.vr.VrHelper;


/**
 * Small helper for avoiding duplicate code.
 * 27.1.2022 No longer related to avatar but some observer.
 * 8.2.22: Not only for a Observer but in general for any type of camera.
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
        //r11.5.21 eturn avatar.getFaceNode();
        //return Observer.getInstance().getTransform();
        return cameraForMouseClick.getCarrier().getTransform();
        //return avatar.getNode();
        //return sc.getDefaultCamera().getCarrier();
    }

    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            //Der Viewpoint kann bei Firefox 1.7m hoher sein als das Camera ausweist! Scheint hier trotzdem zu gehen.
            //return avatar.buildPickingRay(cameraForMouseClick/*sc.getDefaultCamera()*/, mouselocation);
            return cameraForMouseClick.buildPickingRay(cameraForMouseClick.getCarrierTransform()/*sc.getDefaultCamera()*/, mouselocation);
        }
        Ray ray = VrHelper.getController(1).getRay();
        return ray;
    }

    @Override
    public Menu buildMenu() {
        return menuBuilder.buildMenu();
    }
}
