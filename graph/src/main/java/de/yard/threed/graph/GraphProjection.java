package de.yard.threed.graph;

import de.yard.threed.core.LocalTransform;

/**
 * Projection between two Graph coordinate systems. Avoids dependency to a MapProjection, which is
 * quite specific. But be careful. Its a projection of graph positions, not the full graph.
 * Use case is to get the 3D world position for a vehicle on a groundnet graph.
 * 
 * Created on 10.01.19.
 */
public interface GraphProjection {
    LocalTransform project( GraphPosition cp);
}
