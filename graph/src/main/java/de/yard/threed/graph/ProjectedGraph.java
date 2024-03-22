package de.yard.threed.graph;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.graph.RotationProvider;

/**
 * Should also contain forwardProjection?
 */
public class ProjectedGraph extends Graph {
    public GraphProjection backProjection;

    public ProjectedGraph(GraphProjection backProjection) {
        this.backProjection = backProjection;
    }

    public ProjectedGraph(GraphValidator graphValidator, GraphOrientation orientatio, GraphProjection backProjection) {
        super(graphValidator, orientatio);
        this.backProjection = backProjection;
    }

    /**
     * Extracted from GraphMovingSystem.getPosRot().
     */
    @Override
    public LocalTransform getPosRot(GraphPosition cp, RotationProvider rotationProvider) {
        if (backProjection == null) {
            return super.getPosRot(cp, rotationProvider);
        }
        return backProjection.project(cp);
    }
}
