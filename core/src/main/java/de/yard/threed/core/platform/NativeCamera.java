package de.yard.threed.core.platform;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;

/**
 * Ob die Ableitung von Base3D gut ist, ist weiter offen.
 * 12.6.15: Mal ohne die Ableitung, weil scale und auch Rotation vielleicht zu speziell (oder allgemein?) sind.
 * JME hat auch keine solche Ableitung.
 * 9.3.16: Rotation dürfte gar nicht so speziell sein, aber lassen wir es mal weiterhin mit
 * der Ableitung. Immerhin hat JME diese merkwürdige 180 Grad Spiegelung.
 * <p>
 * 26.1.17: Umgestellt auf NativeTransform
 * <p>
 * 28.11.18: [[MA29]] Camera ist jetzt eine Component wie Mesh.
 * <p>
 * Created by thomass on 05.06.15.
 */
public interface NativeCamera {

    /**
     * 2.3.16: Die Signatur hier in der Platform ist local space!
     * 10.8.16: Ein Vektor für die Blickrichtung reicht eigentlich nicht aus, weil der up-Vector fehlt. (z.B. Blick auf Erdkugel).
     * 17.11.16: Wenn upVector null ist, wird ein Default genommen.
     * JME hat auch einen Parameter dafür in seiner lookat().
     * 1.2.18: Bei ThreeJS und Unity ist lookat aber im world space. ThreeJS schränkt noch ein, dass der parent keinen Transform haben darf.
     * Meine Nutzung ist anscheinend uneinheitlich. Vielleicht besser nicht mehr ueber die Platform? oder deprecated.
     * mal deprecated. Wird eh kaum verwendet.
     */
    /*29.9.18 @Deprecated
    void lookAt(Vector3 lookat, Vector3 upVector);*/
    public Matrix4 getProjectionMatrix();

    /**
     * Liefert die Matrix neutral von einer evtl. Handedness Konvertierung.
     * siehe auch DVK.
     *
     * @return
     */
    public Matrix4 getViewMatrix();

    //public Matrix4 getWorldModelMatrix();

    /**
     * 7.4.16: Ausgelagert in die Platform, weil Unity das outOfTheBox kann.
     * 22.4.16: Die Dimesnion weiss die Platform selber.
     * 4.11.19: Bei WebVR kann es bei Firefox sein, dass der reale Viewpoint auch mit disAbled VR(!) 1.7m hoeher ist als die Camera Position ausweist!
     * Das kann man nicht erkennen. Darum lieber den reale Viewpoint mit reingeben.
     * @param mouselocation
     * @return
     */
    NativeRay buildPickingRay(NativeTransform realViewPosition, Point mouselocation/*,Dimension screendimensions*/);

    /**
     * 20.5.16: Ein Model an die Camera haengen, das sich damit dann im CameraSpace befindet und sich immer mit der Camera bewegt.
     * (z.B. ein HUD). Das ist was anderes als attach! 26.11.18: Aber verwirrend. Kann auch Camera sein?
     *
     */
    //MA29 public void add(NativeTransform model);

    //MA29 public void detach();

    /**
     * Die Camera wird an ein Model gehangen. 26.11.18: oder umgekehrt?
     * NeeNee, nicht umgekehrt, aber verwirrend zu SceneNode.attach. TODO vereinheitlichen MA29
     */
    //MA29 void attach(NativeTransform model);

    double getNear();

    double getFar();

    double getAspect();

    double getFov();

    //MA29 NativeTransform getTransform();

    /**
     * Liefert die "echte" Camera Position unabhaengig von einem Carrier. Nur bei VR interessant.
     *
     * @return
     */
    Vector3 getVrPosition(boolean dumpInfo);

    /**
     * Set the layer to be rendered by this camera. No bitmask, just an index from 0-15.
     *
     * @param layer
     */
    void setLayer(int layer);

    /**
     * 7.10.19 Hat die Methode ienen Sinn, weil es Bitmap sein kann?
     * @return
     */
    int getLayer();

    void setName(String name);

    String getName();

    NativeSceneNode getCarrier();


    void setClearDepth(boolean clearDepth);

    void setClearBackground(boolean clearBackground);

    /**
     * dis/enable Rendering of this camera.
     * @param b
     */
    void setEnabled(boolean b);


    void  setFar(double far);
}
