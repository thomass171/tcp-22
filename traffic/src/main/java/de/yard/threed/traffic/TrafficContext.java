package de.yard.threed.traffic;

import de.yard.threed.graph.GraphPosition;
import de.yard.threed.traffic.config.SceneVehicle;
import de.yard.threed.traffic.config.VehicleConfig;
import de.yard.threed.trafficcore.model.SmartLocation;

/**
 * Decoupling of traffic graph base functions (eg. vehicle launching) from Groundnet, Airport, etc.
 */
public interface TrafficContext {
    GraphPosition getStartPosition(VehicleConfig vconfig);

    int getVehicleCount();

    SceneVehicle getVehicle(int i);

    TrafficGraph getGraph();

    GraphPosition getStartPositionFromLocation(SmartLocation location);
}
