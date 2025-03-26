package de.yard.threed.traffic;

import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * Only used for 'navigator' in fg. 'navigator' will be positioned by teleport.
 */
public class SimpleVehiclePositioner implements VehiclePositioner {
    Vector3 position;

    public SimpleVehiclePositioner(Vector3 position) {
        this.position = position;
    }

    /**
     *
     */
    @Override
    public void positionVehicle(EcsEntity vehicle) {
        // nothing to do
    }

    @Override
    public SceneNode getDestinationNode() {
        //?? could also be world?
        return SphereSystem.getSphereNode();
    }
}
