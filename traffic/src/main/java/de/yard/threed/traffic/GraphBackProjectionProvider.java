package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;


public interface GraphBackProjectionProvider {
    GraphProjection/*Flight3D*/ getGraphBackProjection();

}
