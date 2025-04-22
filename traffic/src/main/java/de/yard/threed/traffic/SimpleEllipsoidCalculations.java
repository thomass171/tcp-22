package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.geodesy.GeoTools;

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
public class SimpleEllipsoidCalculations extends EllipsoidCalculations {

    double radius;

    public SimpleEllipsoidCalculations(double radius) {
        this.radius = radius;
    }

    @Override
    public Vector3 getNorthHeadingReference(GeoCoordinate location) {
        Util.notyet();
        return null;
    }

    @Override
    public GeoCoordinate fromCart(Vector3 cart) {
        LatLon latLon = new LatLon(MathUtil2.asin(cart.getZ() / radius),
         MathUtil2.atan2(cart.getY(), cart.getX()));
        // TODO elevation
        return GeoCoordinate.fromLatLon(latLon,0);
    }

    @Override
    public Vector3 toCart(GeoCoordinate geoCoordinate, ElevationProvider elevationprovider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
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
        Util.notyet();
        return null;
    }

    @Override
    public Degree courseTo(LatLon latLon, LatLon dest) {
        return GeoTools.heading(latLon, dest);
    }

    @Override
    public double distanceTo(LatLon latLon, LatLon dest) {
        Util.notyet();
        return 0;
    }
}
