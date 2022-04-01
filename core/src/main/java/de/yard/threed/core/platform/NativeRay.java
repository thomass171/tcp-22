package de.yard.threed.core.platform;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;

import java.util.List;

/**
 * Created by thomass on 28.11.15.
 */
public interface NativeRay {
    /**
     * The direction independent from any handedness conversion.
     */
    Vector3 getDirection();

    /**
     * The origin independent from any handedness conversion.
     */
    Vector3 getOrigin();

    /**
     * Returns all collisions of the ray without considering any subgraph. Might be inefficient, but meets best the
     * opportunities of all platforms.
     * TODO: introduce a collider scene node marker to make it more efficient.
     */
    List<NativeCollision> getIntersections();

}
