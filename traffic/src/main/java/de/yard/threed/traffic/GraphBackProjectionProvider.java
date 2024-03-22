package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.traffic.geodesy.MapProjection;

/**
 * Provides a back projection for a projection.
 * 22.3.24: The back projection depends on the forward projection
 */
public interface GraphBackProjectionProvider {
    GraphProjection/*Flight3D*/ getGraphBackProjection(MapProjection forwardProjection);

}
