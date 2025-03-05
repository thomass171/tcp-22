package de.yard.threed.core.platform;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

import java.util.List;

/**
 * The base object that builds a scene graph and connects parents and children.
 * A mesh will be a child.
 * 26.1.17: Replaces as NativeTransform NativeBase3D.
 * <p>
 * Created by thomass on 15.09.16.
 */
public interface NativeTransform {
    Quaternion getRotation();

    Vector3 getPosition();

    void setPosition(Vector3 vector3);

    void setRotation(Quaternion quaternion);

    Vector3 getScale();

    void setScale(Vector3 scale);

    void translateOnAxis(Vector3 vector3, double distance);

    void rotateOnAxis(Vector3 axis, /*Degree*/double angle);

    /**
     * 06.12.16
     * 21.12.17: Der parent darf auch null sein. Wobei das eigentlich Unsinn ist, weil ich ja eine eignenes scene.world als root habe.
     * 01.11.19: The platform must propagate the parent layer to the subtree. Thats important. But not for a carrier! That would spoil a hud attached to a moving camera.
     * So a carrier will not propagate neither into a camera nor in other children when the carrier gets a new parent!
     * 08.12.22: However, the carrier should have the same layer as the camera because otherwise objects attached to the camera will get the wrong layer
     * propagated. The carriers layer should always comply to the camera.
     * 9.12.22: Layer propagation needs to stop at a camera carrier, to avoid spoiling the camera layer and its subtree.
     */
    void setParent(NativeTransform parent);

    NativeTransform getParent();

    /**
     * Aus base3d hierhin verschoben, weil dafuer der Parent bekannt sein muss.
     * Liefert die Matrix neutral von einer evtl. Handedness Konvertierung.
     * siehe auch DVK.
     * 6.12.16: wieder zurück
     *
     * @return
     */
    public Matrix4 getWorldModelMatrix();

    public Matrix4 getLocalModelMatrix();

    /**
     * Auch wenn Unity nicht sowas wie add(Child) hat (wofuer auch, das ist ja eigentlich ein setParent), kann man doch die Childs ermitteln
     * 14.11.16: Liefert jetzt Object3D und nicht mehr SceneNode. Kann null liefern, obwohl das inkonsistent und offenbar ein Fehler ist.
     * 23.9.17: MA17 vereint zu getChildren(), um die unecht Filterung zu vereinfachen.
     *
     * @param index
     * @return
     */
    @Deprecated
    /*NativeObject3D*/NativeTransform getChild(int index);

    @Deprecated
    int getChildCount();

    List<NativeTransform> getChildren();

    /**
     * Who owns me? The SceneNode to which this is a component. Should(?) not be null.
     *
     * @return
     */
    NativeSceneNode getSceneNode();

    /**
     * Layer is no bitmask, just values 0-15 with 0 for default layer.
     * Recursive for all children. Propagated only once, so any child might set its own layer later.
     * 14.10.19: Deswegen und weil ThreeJS und Unity es auch nur als Property betrachten nicht rekursiv.
     * 28.10.19: Das passt aber so gar nicht zum JME Viewport Konzept. Und ist auch eigentlich doch unpassend.
     * Es würde doch eh rekursiv verwendet für einen Subgraph.
     * There was a long history of discussion recursive or not. Finally it is defined recursive and up to the platform
     * eg. JME mit Viewport Subgraph.
     * Und ein bischen ist es auch eine Bitmask, weil es das in ThreeJS ist.
     * Darum: Gültig sind 0-15 (Viewports) und die sind disjunkt, also
     * eine Bitmask mit immer nur einem gesetzten Bit. Es kann immr nur EIN Layer/Viewport aktiv sein.
     * In Unity layer is no bitmap, zumindest kann ein Objekt
     * nicht in mehreren sein. Unity hat Wertebereich [0-31], aber nicht als Bitmap.
     * 9.12.22: Layer propagation needs to stop at a camera carrier, to avoid spoiling the camera layer and its subtree.
     * (See @link setParent())
     * @param layer
     */
    void setLayer(int layer);

    /**
     * 31.10.19: Returns no longer a bitmask, but the decoded layer
     * (Unity only works with a single layer?)
     */
    int getLayer();

}
