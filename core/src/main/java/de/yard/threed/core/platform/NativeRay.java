package de.yard.threed.core.platform;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;

import java.util.List;

/**
 * Created by thomass on 28.11.15.
 */
public interface NativeRay {
    /**
     * Liefert die Direction neutral von einer evtl. Handedness Konvertierung.
     * siehe auch DVK.
     */
    Vector3 getDirection();

    /**
     * Liefert origin neutral von einer evtl. Handedness Konvertierung.
     * siehe auch DVK.
     */
    Vector3 getOrigin();

    /**
     * Einfach alle Collisions ermitteln, ohne einen (Teil)Graph zu uebergeben, in dem gesucht wird. Ist mehr Unity Like.
     * Und manchmal auch praktischer.
     */
    List<NativeCollision> getIntersections();

}
