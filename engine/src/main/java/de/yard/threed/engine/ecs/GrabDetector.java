package de.yard.threed.engine.ecs;

import de.yard.threed.core.Vector3;

@FunctionalInterface
public interface GrabDetector {
    boolean canGrap(Vector3 worldPosition);
}
