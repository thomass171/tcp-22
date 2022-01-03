package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.GraphProjection;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;


public interface RoundBodyCalculations {
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
     * 20.12.21: Was soll hier eigenltich der ElevationProvider?
     *
     * @param geoCoordinate
     * @param elevationprovider
     * @return
     */
    Vector3 toCart(GeoCoordinate geoCoordinate, ElevationProvider elevationprovider);
    Vector3 toCart(GeoCoordinate geoCoordinate);

    LatLon applyCourseDistance(LatLon latLon, Degree coursedeg, double dist);

    Degree courseTo(LatLon latLon, LatLon dest);

    double distanceTo(LatLon latLon, LatLon dest);
}
