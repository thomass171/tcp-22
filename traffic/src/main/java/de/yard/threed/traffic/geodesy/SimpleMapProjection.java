package de.yard.threed.traffic.geodesy;

import de.yard.threed.core.*;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.flight.FlightLocation;

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

    public static void projectGraph(Graph graph, MapProjection projection, EllipsoidCalculations rbcp ) {
        for (int i=0;i<graph.getNodeCount();i++){
            GraphNode n = graph.getNode(i);
            projectLocation(graph,n,projection,rbcp);
        }
        graph.orientation =  GraphOrientation.buildForZ0();
    }

    /**
     * 20.3.2018 Das ist eine haarige Geschichte.
     * MA31: Aus GraphNode als static hierhin.
     *
     * @param projection
     */
    public static void projectLocation(Graph graph, GraphNode node, MapProjection projection, EllipsoidCalculations rbcp ) {
        // elevation kommt nach Z. Das passt aber nicht fuer Bodenhoehe, bei der z 0 sein sollte. TODO
        //RoundBodyConversions rbcp = TrafficHelper.getRoundBodyConversionsProviderByDataprovider();
        //SGGeod coor = SGGeod.fromCart(node.getLocation());
        GeoCoordinate coor = rbcp.fromCart(node.getLocation());
        Vector2 v = projection.project(coor);
        Vector3 v3 = new Vector3(v.getX(), v.getY(), (float) coor.getElevationM());
        Vector3 location = v3;
        // 9.3.21: Ich sach ja, haarig.
        node.setLocationOnlyForSpecialPurposes(location);

        //for (GraphEdge edge : edges) {
        for (int i=0;i<graph.getEdgeCount();i++){
            GraphEdge edge=graph.getEdge(i);
            edge.recalcForProjection();
        }
    }
}
