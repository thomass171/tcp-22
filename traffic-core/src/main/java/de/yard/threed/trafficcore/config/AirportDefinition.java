package de.yard.threed.trafficcore.config;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.trafficcore.model.SmartLocation;

import java.util.List;

/**
 * Analog VehicleDefinition. For XML and REST
 */
public interface AirportDefinition {

    String getHome();

    List<LocatedVehicle> getVehicles();

    List<SmartLocation> getLocations();
}
