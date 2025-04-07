package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.core.GeoCoordinate;

/**
 * geodetic calculations and geocentric and geodetic conversions.
 * WGS-84 assumes an ellipsoid, but an implementation might also implement calculations for perfect spheres.
 * These calculations might be very complex math and there is no 'correct' way. All are to some degree approximations.
 * The most important point is to use always the same formulas inside a 'cluster' (e.g. 'flightgear'). Otherwise
 * visual artifacts or other strange effects might be the result.
 *
 * Some calculations however are sphere/ellipsoid independent.
 * <p>
 * See also https://en.wikipedia.org/wiki/Geographic_coordinate_conversion
 * and https://stackoverflow.com/questions/1185408/converting-from-longitude-latitude-to-cartesian-coordinates
 * <p>
 * 10.5.24: Merge with {@link de.yard.threed.traffic.geodesy.MapProjection}? At least toCart() appears similar. 1.4.25: Be careful with 'similar'!!
 */
public abstract class EllipsoidCalculations {

    /**
     *
     * 8.6.17: Used for placing models. Fits to FG vehicle model. Needs mirroring for camera (?).
     */
    public Quaternion buildZUpRotation(GeoCoordinate location, Degree heading, Degree pitch){
        Quaternion rotation = buildZUpRotation(location);
        // Rotation typically is CCW, but heading is CW, so negate.
        // FG Scenery objects nevertheless use CCW Heading! The need to call with negated heading.
        rotation = rotation.multiply(Quaternion.buildQuaternionFromAngleAxis(heading, new Vector3(0, 0, -1)));
        rotation = rotation.multiply(Quaternion.buildQuaternionFromAngleAxis(pitch, new Vector3(0, 1, 0)));
        return rotation;
    }

    public Quaternion buildZUpRotation(GeoCoordinate location) {
        return buildZUpFromLatLonRad(location.getLatRad(), location.getLonRad());
    }

    /**
     * Calculate rotation for a place on earth (or general a sphere) to be parallel to surface with up orthogonal and facing ??
     * This calculation apparently is sphere/ellipsoid independent. The implementation is from FG (FgMath.fromLonLatRad()), but it appears a typical "Euler to Quaternion"
     * (reduced to two angles) calculation from http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm
     * described in detail in https://stackoverflow.com/questions/5437865/longitude-latitude-to-quaternion
     * Original FG comment:
     * > Return a quaternion rotation from the earth centered to the
     * > simulation usual horizontal local frame from given
     * > longitude and latitude.
     * > The horizontal local frame used in simulations is the frame with x-axis
     * > pointing north, the y-axis pointing eastwards and the z axis
     * > pointing downwards.
     * <p>
     * - https://github.com/moble/quaternion/blob/306630d69f382827ef097357ca6ee057a42c2103/quaternion.c#L19
     */
    public Quaternion buildFromLatLonRad(double lat, double lon) {
        double zd2 = 0.5 * lon;
        double yd2 = -0.25 * Math.PI - 0.5 * lat;
        double Szd2 = Math.sin(zd2);
        double Syd2 = Math.sin(yd2);
        double Czd2 = Math.cos(zd2);
        double Cyd2 = Math.cos(yd2);
        Quaternion q = new Quaternion((-Szd2 * Syd2), (Czd2 * Syd2), (Szd2 * Cyd2), (Czd2 * Cyd2));
        return q;
    }

    /**
     * From FG (FgMath.makeZUpFrameRelative()).
     * Create a Z-up local coordinate frame in the earth-centered frame of reference. This is what scenery models, etc. expect.
     */
    public Quaternion buildZUpFromLatLonRad(double lat, double lon) {
        Quaternion result = buildFromLatLonRad(lat, lon);
        // 180 degree rotation around Y axis
        result = result.multiply(new Quaternion(0, 1, 0, 0));
        return result;
    }

    /**
     * Den Vector, der an der Location parallel zur Erdoberfläche nach Norden zeigt.
     *
     * @param location
     * @return
     */
    public abstract Vector3 getNorthHeadingReference(GeoCoordinate location);

    public abstract GeoCoordinate fromCart(Vector3 cart);

    /**
     * A elevation provider is needed for calculating 3D coordinates from geo coordinates. Otherwise
     * you might always be on sea level, which might be useful for perfect planets only. So this is considered deprecated.
     * 21.3.24: Elevation is no longer in GeoCordinate, so the sea level problem might be avoided by just having elevation null.
     * So now we have a priority problem. Lets define that if a provider is passed, it should be used independent of
     * elevation in GeoCoordinate?
     * 29.5.24: Yes, it should be used if it is available. And we need a callback to inform the user that elevation could not be retrieved.
     * Any kind of default value is just not helpful.
     *
     * @param geoCoordinate
     * @param elevationprovider
     * @return
     */
    public abstract Vector3 toCart(GeoCoordinate geoCoordinate, ElevationProvider elevationprovider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler);

    @Deprecated
    public abstract Vector3 toCart(GeoCoordinate geoCoordinate);

    public abstract LatLon applyCourseDistance(LatLon latLon, Degree coursedeg, double dist);

    public abstract Degree courseTo(LatLon latLon, LatLon dest);

    public abstract double distanceTo(LatLon latLon, LatLon dest);


}
