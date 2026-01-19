package de.yard.threed.graph;

import de.yard.threed.core.Quaternion;

public interface GraphVehicleRotation {
    Quaternion getVehicleOrientation(GraphPosition graphPosition, Graph graph);
}
