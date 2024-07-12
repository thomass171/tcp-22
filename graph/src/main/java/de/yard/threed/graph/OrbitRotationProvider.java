package de.yard.threed.graph;

import de.yard.threed.core.Quaternion;

/**
 * For planets and moons.
 * 4.7.24: The rotation in an orbit cannot be derived from an edge because the edge is a circle!
 * 22.2.2022
 */
public class OrbitRotationProvider implements RotationProvider {

    public OrbitRotationProvider( ) {
    }

    @Override
    public Quaternion get3DRotation(Graph graph, GraphPosition cp) {
        // just this for now
        return new Quaternion();
    }
}
