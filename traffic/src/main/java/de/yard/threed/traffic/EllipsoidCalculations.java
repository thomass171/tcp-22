package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;

/**
 * geodetic calculations and geocentric and geodetic conversions.
 * WGS-84 assumes an ellipsoid, but an implementation might also implement calculations for perfect spheres.
 * These calculations might be very complex math and there is no 'correct' way. All are to some degree approximations.
 * The most important point is to use always the same formulas inside a 'cluster' (e.g. 'flightgear'). Otherwise
 * visual artifacts or other strange effects might be the result.
 *
 * See also https://en.wikipedia.org/wiki/Geographic_coordinate_conversion
 * and https://stackoverflow.com/questions/1185408/converting-from-longitude-latitude-to-cartesian-coordinates
 *
 * 10.5.24: Merge with {@link de.yard.threed.traffic.geodesy.MapProjection}? At least toCart() appears similar.
 */
public interface EllipsoidCalculations {
    //22.12.21 stoert hier GraphProjection/*Flight3D*/ getGraphBackProjection();

    /**
     * 8.6.17: Die Methode ist wohl für die Platzierung vom Modeln. Für Camera muss das Ergebnis noch ...(gespiegelt?) werden.
     */
    public  Quaternion buildRotation(GeoCoordinate location, Degree heading, Degree pitch) ;

    /**
     * Den Vector, der an der Location parallel zur Erdoberfläche nach Norden zeigt.
     * @param location
     * @return
     */
    public Vector3 getNorthHeadingReference(GeoCoordinate location);

    public  GeoCoordinate fromCart(Vector3 cart);

    /**
     * A elevation provider is needed for calculating 3D coordinates from geo coordinates. Otherwise
     * you might always be on sea level, which might be useful for perfect planets only. So this is considered deprecated.
     * 21.3.24: Elevation is no longer in GeoCordinate, so the sea level problem might be avoided by just having elevation null.
     * So now we have a priority problem. Lets define that if a provider is passed, it should be used independent of
     * elevation in GeoCoordinate?
     *
     * @param geoCoordinate
     * @param elevationprovider
     * @return
     */
    Vector3 toCart(GeoCoordinate geoCoordinate, ElevationProvider elevationprovider);
    @Deprecated
    Vector3 toCart(GeoCoordinate geoCoordinate);

    LatLon applyCourseDistance(LatLon latLon, Degree coursedeg, double dist);

    Degree courseTo(LatLon latLon, LatLon dest);

    double distanceTo(LatLon latLon, LatLon dest);
}
