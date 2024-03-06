package de.yard.threed.engine;

/**
 *
 */
@FunctionalInterface
public interface NodeFilter {
    boolean matches(SceneNode n);
}
