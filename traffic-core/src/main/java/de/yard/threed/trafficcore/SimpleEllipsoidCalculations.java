package de.yard.threed.trafficcore;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.trafficcore.ElevationProvider;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.EllipsoidCalculations;
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
    // differs fromFG by 1m
    public static double eQuatorialEarthRadius = 6378137.0D;
    // to be more flexible
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
        return GeoCoordinate.fromLatLon(latLon, 0);
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

    /**
     * https://www.movable-type.co.uk/scripts/latlong.html
     */
    @Override
    public LatLon applyCourseDistance(LatLon latLon, Degree brng, double distInMeter) {

        double d = distInMeter / eQuatorialEarthRadius;
        double lat = latLon.getLatRad();
        double lon = latLon.getLonRad();
        double lat2 = Math.asin(Math.sin(lat) * Math.cos(d) +
                Math.cos(lat) * Math.sin(d) * Math.cos(brng.toRad()));
        double lon2 = lon + Math.atan2(Math.sin(brng.toRad()) * Math.sin(d) * Math.cos(lat),
                Math.cos(d) - Math.sin(lat) * Math.sin(lat2));
        return new LatLon(lat2, lon2);
    }

    @Override
    public Degree courseTo(LatLon latLon, LatLon dest) {
        return GeoTools.heading(latLon, dest);
    }

    /**
     * There are several algorithm known for this (complex) calculation and the results are more or less exact.
     * This implementation is from https://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
     * Returned distance is in kilometer.
     */
    @Override
    public double distanceTo(LatLon l1, LatLon dest) {

        double dlong = (dest.getLonRad() - l1.getLonRad());
        double dlat = (dest.getLatRad() - l1.getLatRad());
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(l1.getLatRad()/* * _d2r*/) * Math.cos(dest.getLatRad()/* * _d2r*/)
                * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = eQuatorialEarthRadius * c;

        return d / 1000.0;
    }
}
