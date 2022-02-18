package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.vr.VRController;

/**
 * Just a wrapper for a transform (usually carrier of the main camera) that allows internal fine tuning of the position independent from its
 * external position. Not a node on its own. Singleton.
 *
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

    // Usually the carrier, but might also be a proxy node.
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
    public void incHeading(Degree inc) {
        FirstPersonController.incHeading(observer, inc);
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
     * Apply local position/rotation with local offsets.
     * Needed/Useful in some VR systems (WebXR?) where too many chained parent nodes might spoil the VR y-position.
     */
    public void adjustVR() {

        observer.setPosition(getEffectivePosition());
        //observer.setRotation(getEffectiveRotation());
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

    /**
     *
     * @param position  Relative position. fineTuneOffset will be added.
     */
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
        initFineTune(new Vector3(0, offset, 0));
    }

    public void initFineTune(Vector3 offset) {

        logger.debug("initFineTune: offset=" + offset );
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

    public void dumpDebugInfo() {
        logger.info("fineTuneOffset=" + this.fineTuneOffset);
    }
}
