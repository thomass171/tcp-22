package de.yard.threed.graph;

import de.yard.threed.core.LocalTransform;

/**
 * Projection between two Graph coordinate systems. Avoids dependency to a MapProjection, which is
 * quite specific.
 * 
 * Created on 10.01.19.
 */
public interface GraphProjection {
    LocalTransform project( GraphPosition cp);
}
