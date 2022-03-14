package de.yard.threed.engine.ecs;

/**
 * Created on 09.03.18.
 */
@FunctionalInterface
public interface EntityFilter {
    boolean matches(EcsEntity e);
}
