package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;

/**
 * Assuming the ellipsoid is a sphere.
 * From proposal in https://stackoverflow.com/questions/1185408/converting-from-longitude-latitude-to-cartesian-coordinates
 * which is also proposed at other locations. Defines
 * - the x-axis goes through long,lat (0,0), so longitude 0 meets the equator;
 * - the y-axis goes in direction of india
 * - the z-axis goes in direction of north pole.
 * <p>
 * See also https://stackoverflow.com/questions/378281/lat-lon-distance-heading-lat-lon
 * and http://edwilliams.org/avform147.htm
 */
public class SimpleEllipsoidCalculations implements EllipsoidCalculations {

    double radius;

    public SimpleEllipsoidCalculations(double radius) {
        this.radius = radius;
    }

    @Override
    public Quaternion buildRotation(GeoCoordinate location, Degree heading, Degree pitch) {
        return null;
    }

    @Override
    public Vector3 getNorthHeadingReference(GeoCoordinate location) {
        return null;
    }

    @Override
    public GeoCoordinate fromCart(Vector3 cart) {
        return null;
    }

    @Override
    public Vector3 toCart(GeoCoordinate geoCoordinate, ElevationProvider elevationprovider) {
        Vector3 cart = toCart(geoCoordinate);
        // TODO elevation??
        return cart;
    }

    @Override
    public Vector3 toCart(GeoCoordinate geoCoordinate) {
        double x = radius * MathUtil2.cos(geoCoordinate.getLatRad()) * MathUtil2.cos(geoCoordinate.getLonRad());
        double y = radius * MathUtil2.cos(geoCoordinate.getLatRad()) * MathUtil2.sin(geoCoordinate.getLonRad());
        double z = radius * MathUtil2.sin(geoCoordinate.getLatRad());
        return new Vector3(x, y, z);
    }

    @Override
    public LatLon applyCourseDistance(LatLon latLon, Degree coursedeg, double dist) {
        return null;
    }

    @Override
    public Degree courseTo(LatLon latLon, LatLon dest) {
        return null;
    }

    @Override
    public double distanceTo(LatLon latLon, LatLon dest) {
        return 0;
    }
}
