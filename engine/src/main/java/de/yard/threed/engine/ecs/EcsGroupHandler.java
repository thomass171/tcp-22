package de.yard.threed.engine.ecs;

/**
 *
 */
@FunctionalInterface
public interface EcsGroupHandler {
    void processGroups(EcsEntity entity, EcsGroup group);

}
