package de.yard.threed.traffic;

import de.yard.threed.core.Quaternion;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphVehicleRotation;

/**
 * The default vehicle rotation which is "no further rotation" (like roll) in addition to graph.
 */
public class DefaultGraphVehicleRotation implements GraphVehicleRotation {
    /**
     * A simple rotation...
     * Was once the default for "GraphMovingComponent.customModelRotation"(??)
     * Well, it seems to be the default orientation for a non rolling vehicle.
     */
    @Override
    public Quaternion getVehicleOrientation(GraphPosition graphPosition, Graph graph) {
        return FgVehicleSpace.getFgVehicleForwardRotation();
    }
}
