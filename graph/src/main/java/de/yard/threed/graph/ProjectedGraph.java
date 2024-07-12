package de.yard.threed.graph;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;

/**
 * Should also contain forwardProjection? Anyway, important to remember, the projection
 * here is not the opposite of the forward projection.
 * 24.5.24: The phrase 'backProjection' is misleading. Its just a projection, so renamed to just 'projection'.
 * And - even more important - its a projection of graph positions!
 * 6.7.24 But after all it in fact is a back projection!
 * Known use cases are(?):
 * - PROBABLY NOT: projecting groundnet to 2D
 * - PROBABLY NOT: projecting 3D cartesian to 2D
 * - get the 3D world position for a vehicle on a groundnet graph
 */
public class ProjectedGraph extends Graph {
    private GraphProjection projection;

    public ProjectedGraph(GraphProjection projection) {
        this.projection = projection;
        if (projection == null) {
            throw new RuntimeException("projection is null");
        }
    }

    public ProjectedGraph(GraphValidator graphValidator, GraphOrientation orientatio, GraphProjection projection) {
        super(graphValidator, orientatio);
        this.projection = projection;
    }

    /**
     * Extracted from GraphMovingSystem.getPosRot().
     */
    @Override
    public LocalTransform getPosRot(GraphPosition cp, Quaternion customModelRotation) {
        if (projection == null) {
            // Aborting without projection might be an option as ProjectedGraph without projection
            // isn't consistent. But it is null when no backprojection is needed (in flat scenes).
            //throw new RuntimeException("projection is null");
            return super.getPosRot(cp/*, rotationProvider*/, customModelRotation);
        }
        // 4.7.24: Why isn't the rotationProvider used here. Probably this is only a side effect and not really
        // the consequence of a concept? Well, a ProjectedGraph has its very own way of retrieving
        // the final rotation.
        return projection.project(cp);
    }
}
