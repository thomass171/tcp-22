package de.yard.threed.traffic.geodesy;

import de.yard.threed.core.*;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.trafficcore.geodesy.MapProjection;
import de.yard.threed.trafficcore.geodesy.OriginMapProjection;

/**
 * 22.4.25: Two graph related methods extracted from SimpleMapProjection
 */
public class GraphMapProjection {

    public static void projectGraph(Graph graph, MapProjection projection, EllipsoidCalculations rbcp ) {
        for (int i=0;i<graph.getNodeCount();i++){
            GraphNode n = graph.getNode(i);
            projectLocation(graph,n,projection,rbcp);
        }
        graph.setGraphOrientation(GraphOrientation.buildForZ0());
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
        Vector3 v3 = new Vector3(v.getX(), v.getY(), (double) coor.getElevationM());
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
