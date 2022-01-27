package de.yard.threed.engine.gui;


import de.yard.threed.engine.Camera;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.Avatar;
import de.yard.threed.engine.vr.VrHelper;


/**
 * Small helper for avoiding duplicate code.
 * 27.1.2022 No longer related to avatar but observer.
 *
 * 26.11.2019
 */
public abstract class ObserverMenuProvider implements MenuProvider {
   // Avatar avatar;
    Camera cameraForMouseClick;

    public ObserverMenuProvider(/*Avatar avatar,*/ Camera cameraForMouseClick) {
       // this.avatar = avatar;
        this.cameraForMouseClick=cameraForMouseClick;
    }

    @Override
    public /*SceneNode*/Transform getAttachNode() {
        //r11.5.21 eturn avatar.getFaceNode();
        return Observer.getInstance().getTransform();
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
        throw new RuntimeException("should be overridden(C#)");
    }
}
