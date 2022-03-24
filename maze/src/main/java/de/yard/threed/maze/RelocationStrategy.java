package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.ecs.EcsEntity;

public interface RelocationStrategy {
    Point getLocation(EcsEntity player);
}
