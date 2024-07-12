package de.yard.threed.graph;

import de.yard.threed.core.Quaternion;

/**
 * 22.2.2020: Abstraction for the 3D rotation of an object on a graph at a graph position. Different for vehicles and planets.
 * Not really related to Graph(Orientation).
 * 23.6.20 Not a @FunctionalInterface, because C# cannot extend delegates.
 */

public interface RotationProvider {
    Quaternion get3DRotation(Graph graph, GraphPosition cp);
}
