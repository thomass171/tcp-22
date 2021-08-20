package de.yard.threed.engine.graph;

import de.yard.threed.engine.SceneNode;

/**
 * Generic Visualizer
 *
 * 18.11.20
 */
public interface Visualizer<T> {
    void visualize(T t, SceneNode destinationnode);

    /**
     * Just an optional destination.
     * Might be null. Then the caller might use "world" or any other destination.
     */
    SceneNode getDestinationNode();
}
