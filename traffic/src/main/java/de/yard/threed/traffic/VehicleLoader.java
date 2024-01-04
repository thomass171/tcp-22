package de.yard.threed.traffic;


import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.trafficcore.model.Vehicle;

/**
 * Loading a vehicle in FG is more complex than simple vehicle (XML,animations,aso).
 *
 * 11.11.21
 */
public interface VehicleLoader {
    void loadVehicle(Vehicle vehicle, VehicleDefinition config, VehicleLoadedDelegate loaddelegate);
}
