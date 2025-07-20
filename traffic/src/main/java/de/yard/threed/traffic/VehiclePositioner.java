package de.yard.threed.traffic;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * 29.3.25 Maybe shouldn't be limited to vehicles but also for all model.
 */
//@FunctionalInterface
public interface VehiclePositioner {
    void positionVehicle(EcsEntity vehicle);

    /**
     * More generic.
     */
    void positionTransform(Transform transform);
    SceneNode getDestinationNode();
}
