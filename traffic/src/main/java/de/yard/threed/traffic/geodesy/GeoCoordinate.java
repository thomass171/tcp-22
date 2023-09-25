package de.yard.threed.traffic.geodesy;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector3;

/**
 * See https://en.wikipedia.org/wiki/Earth-centered,_Earth-fixed_coordinate_system and
 * https://en.wikipedia.org/wiki/Geodetic_datum
 * https://ntrs.nasa.gov/citations/19760028048
 * https://en.wikipedia.org/wiki/Height_above_sea_level
 * <p>
 * And note the difference between geocentric and geodetic!
 *
 *
 * <p>
 * This class is an independent simplification.
 * <p>
 * 17.12.21
 */
public class GeoCoordinate extends LatLon {

    // height above the ellipsoid in meter
    double elevationM;

    public GeoCoordinate(Degree lat, Degree lon, double elevation) {
        super(lat, lon);
        this.elevationM = elevation;
    }

    public GeoCoordinate(double latitudeRad , double longitudeRad, double elevationM) {
        super(latitudeRad,longitudeRad);
        this.elevationM = elevationM;
    }

    public double getElevationM(){
        return elevationM;
    }


    /*Don't loose elevation accidently public static GeoCoordinate fromLatLon(LatLon latLon){
        // 21.12.21:Elevation 0 ist doch Kappes,oder?
        return new GeoCoordinate(latLon.latRad, latLon.lonRad,0.0);
    }*/

    public static GeoCoordinate fromLatLon(LatLon latLon, double elevationM){
        return new GeoCoordinate(latLon.latRad, latLon.lonRad,elevationM);
    }

    /**
     * 20.12.21:Das ist doch ne Kruecke.
     * @param e
     */
    @Deprecated
    public void setElevationM(double e) {
        this.elevationM=e;
    }
}
