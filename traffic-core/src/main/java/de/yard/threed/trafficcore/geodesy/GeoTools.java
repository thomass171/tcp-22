package de.yard.threed.trafficcore.geodesy;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;

public class GeoTools {

    /**
     * From https://stackoverflow.com/questions/6404661/figuring-out-distance-and-course-between-two-coordinates
     * and https://www.movable-type.co.uk/scripts/latlong.html
     * <p>
     * BTW: FGs courseTo() uses the same algporithm
     */
    public static Degree heading(LatLon latLon, LatLon dest) {
        if (latLon.latRad == dest.latRad && latLon.lonRad == dest.lonRad) {
            return null;
        }

        //TODO handle singularity? At poles?
        double dlon = dest.lonRad - latLon.lonRad;

        double y = Math.sin(dlon) * Math.cos(dest.latRad);
        double x = Math.cos(latLon.latRad) * Math.sin(dest.latRad)
                - Math.sin(latLon.latRad) * Math.cos(dest.latRad) * Math.cos(dlon);
        double p = Math.atan2(y, x);
        return new Degree((p * 180.0 / Math.PI + 360.0) % 360.0);
    }
}
