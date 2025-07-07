package de.yard.threed.core;

/**
 * See https://en.wikipedia.org/wiki/Earth-centered,_Earth-fixed_coordinate_system and
 * https://en.wikipedia.org/wiki/Geodetic_datum
 * https://ntrs.nasa.gov/citations/19760028048
 * https://en.wikipedia.org/wiki/Height_above_sea_level
 * <p>
 * And note the difference between geocentric and geodetic!
 * 14.3.24: elevation made optional for cases where it is unknown/undefined.
 * 29.3.25: Moved from traffic to core
 * <p>
 * This class is an independent simplification.
 * <p>
 * 17.12.21
 */
public class GeoCoordinate extends LatLon {

    // height above the ellipsoid in meter
    Double elevationM;

    public GeoCoordinate(Degree lat, Degree lon) {
        super(lat, lon);
        this.elevationM = null;
    }

    public GeoCoordinate(Degree lat, Degree lon, double elevation) {
        super(lat, lon);
        this.elevationM = elevation;
    }

    public GeoCoordinate(double latitudeRad, double longitudeRad, double elevationM) {
        super(latitudeRad, longitudeRad);
        this.elevationM = elevationM;
    }

    public Double getElevationM() {
        return elevationM;
    }


    /*Don't loose elevation accidently public static GeoCoordinate fromLatLon(LatLon latLon){
        // 21.12.21:Elevation 0 ist doch Kappes,oder?
        return new GeoCoordinate(latLon.latRad, latLon.lonRad,0.0);
    }*/

    public static GeoCoordinate fromLatLon(LatLon latLon, double elevationM) {
        return new GeoCoordinate(latLon.latRad, latLon.lonRad, elevationM);
    }

    public static GeoCoordinate fromLatLon(LatLon latLon) {
        return new GeoCoordinate(latLon.getLatDeg(), latLon.getLonDeg());
    }

    @Override
    public String toString() {
        // 18.3.24: Prefer custom with specific format instead of super.toString();
        // precision of 5 corresponds to 1m on equator. Lets go up to 7. Clipping is just to avoid
        // strange representations like 52.4675500000001.
        int degreePrecision = 7;
        String s = getLatDeg().toString(8, degreePrecision) + "," + getLonDeg().toString(8, degreePrecision);
        if (getElevationM() != null) {
            // what is good separator? ':'?
            s += "," + Util.format(getElevationM(), 8, 2);
        }
        return s;
    }


    /**
     * 20.12.21:Das ist doch ne Kruecke.
     *
     * @param e
     */
    @Deprecated
    public void setElevationM(double e) {
        this.elevationM = e;
    }

    public static GeoCoordinate parse(String data) {
        if (data == null) {
            // might happen eg. when it is optional payload
            return null;
        }
        String[] s;
        s = StringUtils.split(data, ",");
        if (s.length == 2) {
            return new GeoCoordinate(Util.parseDegree(s[0]), Util.parseDegree(s[1]));
        }
        if (s.length == 3) {
            return new GeoCoordinate(Util.parseDegree(s[0]), Util.parseDegree(s[1]), Util.parseDouble(s[2]));
        }
        throw new RuntimeException("parse: invalid GeoCoordinate data " + data);
    }
}
