package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.vr.VRController;

/**
 * Singleton. Es kann nur einen geben. Das ist im Prinzip der carrier der main camera.
 * Auch ausserhalb ECS nutzbar.
 * Soll in Erweiterung spaeter auch mal in FirstPersonController verwendet werden.
 * <p>
 * Drei Zustaende??:
 * a) standalone VR, nicht movable(??)
 * b) standalone (z.B. ReferenceScene), movable
 * c) attached
 * <p>
 * Kann justiert werden. Aber ohne Keybindings hier, um flexibel einsetzbar zu sein.
 * Darum hat der Observer eine logische Position, die um finetune ergaenzt wird.
 *
 * <p>
 * 8.5.2012
 */
public class Observer implements SimpleTransform {
    static Log logger = Platform.getInstance().getLog(Observer.class);

    // Ist in der Regel der Carrier, kann aber auch eine ProxyNode sein.
    private Transform observer;
    private static Observer instance = null;
    // boolean vrEnabled = false;
    public static double offsetstep = 0.1f;
    // der Wert des finetune
    public Vector3 fineTuneOffset = new Vector3();
    private Vector3 position = new Vector3();

    private Observer(Transform observer) {
        this.observer = observer;

     /*   if (EngineHelper.isEnabled("argv.enableVR")) {
            vrEnabled = true;
        }*/
    }

    /**
     * Ist eigentlich zu universell. Wird aber für z.B. ProxyNode und Backcompatibilty noch gebraucht. Deswegen direkt mal deprecated.
     * Wegen compatibility wird hier die Instanz auch ueberschrieben.
     * 15.5.21: Im Sinne von Entkopplung und Testen ist es aber ganz praktisch.
     */
    @Deprecated
    public static Observer buildForTransform(Transform observer) {
        if (instance != null) {
            logger.warn("Overwriting instance");
        }
        instance = new Observer(observer);
        return instance;
    }

    public static Observer buildForCamera(Camera camera) {
        instance = new Observer(camera.getCarrier().getTransform());
        return instance;
    }

    public static Observer buildForDefaultCamera() {
        Camera camera = Scene.getCurrent().getDefaultCamera();
        if (camera == null) {
            logger.warn("No camera (running in server?). Not building an observer");
            return null;
        }
        return buildForCamera(camera);
    }

    public static Observer getInstance() {
        return instance;
    }

    /**
     * Nur ausserhalb VR
     */
    public void incHeading(Degree inc, boolean z0) {
        FirstPersonController.incHeading(observer, inc, z0);
    }

    /**
     * Nur ausserhalb VR
     */
    public void incPitch(Degree inc) {
        FirstPersonController.incPitch(observer, inc);
    }

    public void fineTune(boolean up) {
        fineTuneOffset = fineTuneOffset.add(new Vector3(0, (up) ? offsetstep : -offsetstep, 0));
        adjustVR();
    }

    /**
     * besser den ganzen Avatar statt Camera verschieben, dann gehen die Controller mit.
     * Z.Z. ist er ja eh unsichtbar.
     * 15.8.19: jetzt auf vrcarrier statt scenenode.
     * 5.10.19: Immer absolut beginnen statt relativ. Und ich versuchs noch mal mit vrposition.
     * 5.5.21: Der Avatar (mainnode) selber bleibt wie gehabt wo er von aussen positioniert wurde.
     * MA35: jetzt unabhaengig von Avatar.
     */
    public void adjustVR() {
        Vector3 vrpos = new Vector3();//vrcarrier.getTransform().getPosition();
        // if (vrdown) {
        //      vrpos = vrOffsetPosition;
        //  }
        //faceNode.getTransform().setPosition(vrpos.negate());
        vrpos = position.add(fineTuneOffset);
        //vrcarrier.getTransform().setPosition(vrpos);
        //setMesh();
        observer.setPosition(getEffectivePosition());
        //logger.debug("adjustVR: vrdown=" + vrdown + ",vrpos.y=" + vrpos.getY());
    }

    /**
     * Wichtig fuer crosshair Funktion?
     * Ist doch nur fuer VR? Das kann hier doch raus?
     *
     * @param camera
     * @param mouselocation
     * @return
     */
    public Ray buildPickingRay(Camera camera, Point mouselocation) {
        Ray ray = camera.buildPickingRay(observer/*ac.getFaceNode().getTransform()*/, mouselocation);
        return ray;
    }

    /**
     * War mal in AvatarComponent.
     * TODO keybinding entkoppeln.
     */
    public void update() {
        // Feinjustierung über Buttons. Der "main move" should occur automatically when VR isType enabled.
        if (Input.GetKeyDown(KeyCode.X)) {
            //toggleVR();
            if (Input.GetKey(KeyCode.Shift)) {
                fineTuneOffset = fineTuneOffset.add(new Vector3(offsetstep, 0, 0));
            } else {
                fineTuneOffset = fineTuneOffset.add(new Vector3(-offsetstep, 0, 0));
            }
            adjustVR();
        }
        if (Input.GetKeyDown(KeyCode.Y)) {
            //toggleVR();
            if (Input.GetKey(KeyCode.Shift)) {
                fineTuneOffset = fineTuneOffset.add(new Vector3(0, offsetstep, 0));
            } else {
                fineTuneOffset = fineTuneOffset.add(new Vector3(0, -offsetstep, 0));
            }
            adjustVR();
        }
        if (Input.GetKeyDown(KeyCode.Z)) {
            //toggleVR();
            if (Input.GetKey(KeyCode.Shift)) {
                fineTuneOffset = fineTuneOffset.add(new Vector3(0, 0, offsetstep));
            } else {
                fineTuneOffset = fineTuneOffset.add(new Vector3(0, 0, -offsetstep));
            }
            adjustVR();
        }
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        adjustVR();

    }

    public Vector3 getEffectivePosition() {
        return position.add(fineTuneOffset);
    }

    @Override
    public Quaternion getRotation() {
        return observer.getRotation();
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getFinetune() {
        return fineTuneOffset;
    }

    public void initFineTune(double offset) {
        logger.debug("initFineTune: offset=" + offset);
        fineTuneOffset = new Vector3(0, offset, 0);
        adjustVR();
    }

    public void initFineTune(Vector3 offset) {
        logger.debug("initFineTune: offset=" + offset);
        fineTuneOffset = offset;
        adjustVR();
    }

    public void setRotation(Quaternion rotation) {
        observer.setRotation(rotation);
    }

    public void attach(VRController controller) {
        if (controller != null) {
            controller.getTransform().setParent(observer);
        }
    }

    /**
     * Be careful using this to avoid bypassing finetune.
     * Better use 'this', it implements transform.
     *
     * @return
     */
    public Transform getTransform() {
        return observer;
    }

    /**
     * For tests.
     */
    public static void reset() {
        instance = null;
    }

}
