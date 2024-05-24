package de.yard.threed.graph;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.graph.RotationProvider;

/**
 * Should also contain forwardProjection? Anyway, important to remember, the projection
 * here is not the opposite of the forward projection.
 * 24.5.24: The phrase 'backProjection' is misleading. Its just a projection, so renamed to just 'projection'.
 * And - even more important - its a projection of graph positions!
 * Known use cases are(?):
 * - PROBABLY NOT: projecting groundnet to 2D
 * - PROBABLY NOT: projecting 3D cartesian to 2D
 * - get the 3D world position for a vehicle on a groundnet graph
 */
public class ProjectedGraph extends Graph {
    private GraphProjection projection;

    public ProjectedGraph(GraphProjection projection) {
        this.projection = projection;
    }

    public ProjectedGraph(GraphValidator graphValidator, GraphOrientation orientatio, GraphProjection projection) {
        super(graphValidator, orientatio);
        this.projection = projection;
    }

    /**
     * Extracted from GraphMovingSystem.getPosRot().
     */
    @Override
    public LocalTransform getPosRot(GraphPosition cp, RotationProvider rotationProvider) {
        if (projection == null) {
            return super.getPosRot(cp, rotationProvider);
        }
        return projection.project(cp);
    }
}
