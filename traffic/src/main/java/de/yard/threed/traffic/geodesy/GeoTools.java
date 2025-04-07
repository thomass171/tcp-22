package de.yard.threed.traffic.geodesy;

import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector3;
import de.yard.threed.traffic.EllipsoidCalculations;

public class GeoTools {

    /**
     * Orthogonal vector from earth center
     */
    Vector3 upVector(LatLon latLon, EllipsoidCalculations elliCalcs) {
        return elliCalcs.toCart(GeoCoordinate.fromLatLon(latLon, 0), null, null).normalize();
    }

    Vector3 forwardVector(GeoCoordinate from, GeoCoordinate to, EllipsoidCalculations elliCalcs) {
        return elliCalcs.toCart(to, null, null).subtract(
                elliCalcs.toCart(from, null, null)).normalize();
    }
}
