package de.yard.threed.engine.gui;


import de.yard.threed.engine.Camera;
import de.yard.threed.engine.PerspectiveCamera;
import de.yard.threed.engine.SceneNode;

/**
 * Eine SceneNode, die nicht in der world sondern immer an einer bestimmten Position im FOV ist.
 * Die Orientierung wird an die Cameraposition anpasst und damit steht die SceneNode immer
 * an der selben Stelle im FOV.
 * <p/>
 * ist nearplanesize die richtrige Groesse? Das ist doch nicht die Fenstergroesse, oder?
 * 3.3.17: Wie dem auch sei. Stand jetzt funktioniert es auf allen drei Platformen ganz ordentlich.
 * 4.10.18: Ich denke, nearplane ist schon richtig, denn darauf soll das FovElement ja abgebildet werden.
 * 2.10.19: Ohne Verzerrung oder Artefakte funktioniert es aber nur mit deferredCamera, weil die nearplan sonst zu nah sein muss.
 * 3.10.19 Losgelöst von Plane. (jetzt FovElementPlane)
 * 4.10.19: Alle FovElements liegen in Layer 1.
 * <p/>
 *
 */
public class FovElement extends SceneNode {
    protected int level = 0;
    public static int LAYER = 1;
    private static PerspectiveCamera deferredcamera = null;

    public FovElement(Camera camera) {
        //MA29 camera.add(getTransform());
        //Die FOVS kommen - ohne besonderen Grund - direkt an die Camera.
        //2.10.19: NeeNee FovElement soll GUI unabhängig sein.
        getTransform().setParent(camera.getCarrier().getTransform());
    }

    /**
     *
     * 2.10.19: Jetzt mal etwas generischer einfach eine SceneNode vor der Camera. Da
     * kann dann alles moegliche dran. Hud/FovElem,ent sind zu sehr menu/gui bezogen mit dem GurGrid und der Plane und in Realtion zur nearplane.
     * Das hat auch mit nearplane nichts mehr zu tun. Das ist einfach eine Node an der Camera und deferred rendered,
     * damit immer sichtbar. Die Sichtachse geht in Richtung minus-z.
     *
     */
    public static SceneNode buildSceneNodeForDeferredCamera(Camera camera){
        PerspectiveCamera deferredcamera = FovElement.getDeferredCamera(camera);
        SceneNode hud = new  SceneNode();
        //die deferred ist eh child der default camera
        hud.getTransform().setParent(deferredcamera.getCarrier().getTransform());
        hud.getTransform().setLayer(LAYER);
        return hud;
    }

    /**
     * HUD with own deferred rendering camera with layer 1 at any near distance.
     * All FovElements use the same camera.
     */
    public static PerspectiveCamera getDeferredCamera(Camera camera) {
        if (deferredcamera != null){
            return deferredcamera;
        }
        //5 (relativ weit weg), damit Probleme in ReferenceScene auffallen
        double near = 5;//1;
        deferredcamera = Camera.createAttachedDeferredCamera(camera,LAYER, near, near+1);
        //deferredcamera.setName("hud-camera");
        //15.11.19: Name analog "Main Camera" (Unity)
        deferredcamera.setName("Hud Camera");
        return deferredcamera;
    }
}
