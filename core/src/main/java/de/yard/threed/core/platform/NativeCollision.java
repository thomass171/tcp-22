package de.yard.threed.core.platform;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;

/**
 * Created by thomass on 28.11.15.
 */
public interface NativeCollision {
    /**
     * Hier muss genau ein Model geliefert werden, das auch bei der intersect
     * Prüfung geprüft wurde, und nicht ein untergeordnetes. Sonst koennen
     * wichtige Propertiers wie z.B. der scale verloren gehen.
     * Ein uebergeordnetes darf auch nicht geliefert werden, ob wohl da
     * ja auch eine Intersection vorliegt.
     * 23.3.18: Also, eigentlich wird ja ueberhaupt keine Node getroffen, sondern
     * ein Collider. Das liefern einer Node ist zumindest fragwuerdig.
     * Und diese Regel da oben find ich reichlich unpassend. Der SceneGraph kann schon
     * viele Ebenen mit verschiedenstensten transforms enthalten. Wer will 
     * da schon die Grenze ziehen.
     * 
     * @return
     */
    public NativeSceneNode getSceneNode();

    /**
     * Je nach dem, wie es implementiert ist, ist der Punkt da. Sonst null.
     * @return
     */
    Vector3 getPoint();
}
