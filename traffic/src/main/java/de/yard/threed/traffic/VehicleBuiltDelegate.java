package de.yard.threed.traffic;


import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.traffic.config.VehicleConfig;

/**
 * Created on 09.01.19.
 */

@FunctionalInterface
public interface VehicleBuiltDelegate {

    void vehicleBuilt(EcsEntity ecsEntity, VehicleConfig config);

}
