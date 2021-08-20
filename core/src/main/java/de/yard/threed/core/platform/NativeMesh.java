package de.yard.threed.core.platform;

import de.yard.threed.core.Vector3;

/**
 * Created by thomass on 05.06.15.
 *
 *  15.6.16: Mesh ist nicht mehr Teil des Scenegraph, sondern Komponente eines SceneNodes (analog Unity)
 *  4.4.18: Kann auch eine Line repraesentieren.
 *
 */
public interface NativeMesh   /*extends NativeObject3D*/{
    /**
     * Material auslesen um es aendern zu koennen.
     * 6.10.17:Ob das mit nativen Loadern (z.B. gltf) geht, ist reichlich fraglich. Aber es kommt etwas, auf dem man dann manches machen kann. Naja.
     * @return
     */
    NativeMaterial getMaterial();

    /**
     * optional. für Platformen, die sowas brauchen, z.B. Unity.
     * 7.3.17: eigentlich nur wegen Unity BoxCollider
     * für Unity hier guenstiger als in NativeGeometry
     */
    void setBoxColliderSizeHint(Vector3 size);

    /**
     * Zu wem gehoere ich? Die SceneNode, deren Komponente dies Object3D ist. Kann nicht null sein.
     *
     * @return
     */
    NativeSceneNode getSceneNode();
}
