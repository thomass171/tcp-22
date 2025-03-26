package de.yard.threed.traffic;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;

//@FunctionalInterface
public interface VehiclePositioner {
    void positionVehicle(EcsEntity vehicle);
    SceneNode getDestinationNode();
}
