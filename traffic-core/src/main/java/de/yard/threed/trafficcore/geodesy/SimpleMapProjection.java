package de.yard.threed.trafficcore.geodesy;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

/**
 * Ein 3D Mapping LatLon->2D xy Ebene.
 * 
 * Derived from osm2world MetricMapProjection.
 *
 * 17.11.21: This cannot handle elevation, so isn't suitable for creating 3D routes.
 * <p>
 * Created by thomass on 27.04.17.
 */
public class SimpleMapProjection extends OriginMapProjection {

    private double originX;
    private double originY;
    private double scaleFactor;
    public static double METERPERDEGREE = 1850f * 60f;

    public SimpleMapProjection(LatLon origin) {
        super(origin);
    }
    
    @Override
    public Vector2 project(LatLon coor) {
        return new Vector2(getLonDistance(coor, origin), getLatDistance(coor, origin));
    }

    @Override
    public LatLon unproject(Vector2 loc) {
      //SGGeod geod = SGGeod.fromDeg(origin.getLonDeg().add(new Degree(loc.getX() / METERPERDEGREE)),
        //        origin.getLatDeg().add(new Degree(loc.getY() / METERPERDEGREE)));
        //geod.setElevationM(origin.getElevationM());
        LatLon geod = new LatLon(
                origin.getLatDeg().add(new Degree(loc.getY() / METERPERDEGREE)),origin.getLonDeg().add(new Degree(loc.getX() / METERPERDEGREE)));
        return geod;
    }

    private double getLatDistance(LatLon d1, LatLon d2) {
        return  ((d1.getLatDeg().getDegree() - d2.getLatDeg().getDegree())) * METERPERDEGREE;
    }

    private double getLonDistance(LatLon d1, LatLon d2) {
        return  ((d1.getLonDeg().getDegree() - d2.getLonDeg().getDegree())) * METERPERDEGREE;
    }

    public Vector3 projectWithAltitude(GeoCoordinate coor, double altitude) {
        Vector2 v = project(coor);
        return new Vector3(v.getX(), v.getY(), altitude);
    }
}
