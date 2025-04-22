package de.yard.threed.trafficcore.geodesy;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector2;

/**
 * function that converts latitude/longitude coordinates
 * to internally used x/z coordinates. (without elevation).
 *
 * Derived from Osm2World.
 *
 * 17.11.21: This cannot handle elevation, so isn't suitable for creating 3D routes.
 * <p>
 *
 */
public interface MapProjection {

    Vector2 project(LatLon latlon);

    LatLon unproject(Vector2 loc);

    /**
     * returns the origin (i.e. the latlon that maps to (0,0)
     */
    LatLon getOrigin();
}

