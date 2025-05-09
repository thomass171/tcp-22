package de.yard.threed.traffic;

import de.yard.threed.graph.GraphProjection;
import de.yard.threed.trafficcore.geodesy.MapProjection;

/**
 * Provides a back projection for a projection. This is used to convert a position on
 * a projected non 3D graph to a 3D position.
 * Thats the reason for the phrase 'back', even though its not the opposite of the forward projection.
 *
 * 22.3.24: The back projection depends on the forward projection
 */
public interface GraphBackProjectionProvider {
    GraphProjection/*Flight3D*/ getGraphBackProjection(MapProjection forwardProjection);

}
