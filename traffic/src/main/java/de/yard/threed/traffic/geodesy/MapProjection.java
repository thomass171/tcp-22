package de.yard.threed.traffic.geodesy;

/**
 */

import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector2;
import de.yard.threed.traffic.flight.FlightLocation;


/**
 * function that converts latitude/longitude coordinates
 * to internally used x/z coordinates. (without elevation).
 *
 * Derived from Osm2World.
 *
 * 17.11.21: This cannot handle elevation, so isn't suitable for creating 3D routes.
 * <p>
 * 10.5.24: Merge with {@link de.yard.threed.traffic.EllipsoidCalculations}? At least toCart() appears similar.
 * Created by thomass on 27.04.17.
 */
public interface MapProjection {

    Vector2 project(LatLon latlon);

    LatLon unproject(Vector2 loc);

    /**
     * returns the origin (i.e. the latlon that maps to (0,0)
     */
    LatLon getOrigin();
}

