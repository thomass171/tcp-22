package de.yard.threed.trafficcore.config;


import de.yard.threed.trafficcore.model.SmartLocation;

/**
 * Created on 06.03.18.
 * 20.11.23: Extracted from SceneVehicle
 */
public interface LocatedVehicle {
    String getName();
    SmartLocation getLocation();

}
