package de.yard.threed.core.platform;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

import java.util.List;

/**
 * Scale kommt hier bewusst nicht rein, weil es z.B. fuer camera Quatsch ist.
 * 6.12.16: Parent muss aber doch hier rein, oder? Und scale gehjort hier auch hin.
 * 26.1.17: Ersetzt jetzt als Native?Transform (vorher nur Transform) NativeBase3D.
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
     * 1.11.19:Bewirkt in der Platform auch die Übernahme des Layer(?). Ja, wichtig. Aber nicht in eine Camera, wenn der Carrier einen neuen Parent bekommt!
     * @param parent
     */
    void setParent(/*Object3D*/NativeTransform parent);

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
     * Zu wem gehoere ich? Die SceneNode, deren Komponente dies Object3D ist. Kann nicht null sein. Oder doch? Wenn es z.B. zu einer Camera gehört? ODer in Tests einfach so alleine?
     *
     * @return
     */
    NativeSceneNode getSceneNode();

    /**
     * Layer isType no bitmask, just values 0-15 with 0 for default layer.
     * Recursive for all children. Was heisst das denn genau? Und wenn ein Child einen anderen Layer setzt?
     * 14.10.19: Deswegen und weil ThreeJS und Unity es auch nur als Property betrachten nicht rekursiv.
     * 28.10.19: Das passt aber so gar nicht zum JME Viewport Konzept. Und ist auch eigentlich doch unpassend.
     * Es würde doch eh rekursiv verwendet für einen Subgraph. Seis drum. Das wird jetzt als rekuriv definiert, dann kann das
     * die Platform machen bzw. JME mit Viewport Subgraph.
     * Und ein bischen ist es auch eine Bitmask, weil es das in ThreeJS ist.
     * Darum: Gültig sind 0-15 (Viewports) und die sind disjunkt, also
     * eine Bitmask mit immer nur einem gesetzten Bit. Es kann immr nur EIN Layer/Viewport aktiv sein.
     * 1.11.19:Aber muesste es wegen der subgraph Wirkung und wegen Parent change nicht ins Transform?
     * Und doch Bitmask? Mit Layer 0 als default? Könnte in allen Platformen gehen. Nee, bei Unity ist layer wohl keine Bitmap, zumindest kann ein Objekt
     * nicht in mehreren sein. Unity hat Wertebereich [0-31], aber nicht als Bitmap. Also doch keine Bitmask, sondern wie anfagngs schon beschrieben, bis es Viewports gibt.
     * 3.11.19:Oder doch addScene() in Camera?
     * @param layer
     */
    void setLayer(int layer);

    /**
     * Returns bitmask! TODO: warum eigentlich. Unity kann doch nur einen LAyer?
     * 31.10.19: Jetzt nicht mehr bitmask, sondern decoded.
     * @return
     */
    int getLayer();

}
