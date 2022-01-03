package de.yard.threed.traffic;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.traffic.config.VehicleConfig;

public interface EntityBuilder {
    void configure(EcsEntity entity, VehicleConfig config);
}
