package de.yard.threed.traffic;


import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.util.NearView;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.config.VehicleConfig;
import de.yard.threed.trafficcore.model.Vehicle;

/**
 * Loading a vehicle in FG is more complex than simple vehicle (XML,animations,aso).
 *
 * 11.11.21
 */
public interface VehicleLoader {
    void loadVehicle(Vehicle vehicle, VehicleConfig config, VehicleLoadedDelegate loaddelegate);
}
