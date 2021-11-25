package de.yard.threed.graph;

import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.graph.RotationProvider;

/**
 * Fuer Planeten und Monde.
 * 22.2.20202
 */
public class OrbitRotationProvider implements RotationProvider {
    GraphMovingComponent gmc;

    public OrbitRotationProvider( ) {
        this.gmc = gmc;
    }

    @Override
    public Quaternion get3DRotation() {
        //erstmal nur so
        return new Quaternion();
    }
}
