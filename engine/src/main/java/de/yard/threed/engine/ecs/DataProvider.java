package de.yard.threed.engine.ecs;

/**
 * Using generics here is a challenge in C# due to different types in the map in SystemManager.
 *
 * Created on 19.03.18.
 */
@FunctionalInterface
public interface DataProvider {
    Object getData(Object[] parameter);
}
