package de.yard.threed.traffic;

import de.yard.threed.engine.ecs.EcsEntity;

public interface VehicleLoaderResult {
    void applyResultsToEntity(EcsEntity vehicleEntity);
}
