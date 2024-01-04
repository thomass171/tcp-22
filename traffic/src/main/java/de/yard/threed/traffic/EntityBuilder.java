package de.yard.threed.traffic;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.traffic.config.VehicleDefinition;

public interface EntityBuilder {
    void configure(EcsEntity entity, VehicleDefinition config);
}
