package de.yard.threed.traffic;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.ElevationProvider;
import de.yard.threed.trafficcore.GeoRoute;

import java.util.List;

public class GeoRouteHelper {

    GeoRoute geoRoute;

    public GeoRouteHelper(GeoRoute geoRoute) {
        this.geoRoute = geoRoute;
    }

    /**
     * Difficult to decouple because takeoff/touchdown information should be retained.
     * Needs elevationProvider because elevation might be missing in route.
     *
     * @return
     */
    public Graph toGraph(EllipsoidCalculations rbcp, double cruisingaltitude, ElevationProvider elevationProvider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        Graph graph = new Graph(GraphOrientation.buildForFG());
        add(graph, geoRoute.waypointsBeforeTakeoff, rbcp, elevationProvider, missingElevationHandler);
        add(graph, geoRoute.takeoff, rbcp, elevationProvider, missingElevationHandler);
        add(graph, geoRoute.waypointsInFlight, rbcp, (latitudedeg, longitudedeg) -> cruisingaltitude, missingElevationHandler);
        add(graph, geoRoute.touchdown, rbcp, elevationProvider, missingElevationHandler);
        add(graph, geoRoute.waypointsAfterTouchdown, rbcp, elevationProvider, missingElevationHandler);
        return graph;
    }

    private void add(Graph graph, List<GeoCoordinate> l, EllipsoidCalculations rbcp, ElevationProvider elevationProvider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        for (GeoCoordinate gc : l) {
            add(graph, gc, rbcp, elevationProvider, missingElevationHandler);
        }
    }

    private void add(Graph graph, GeoCoordinate gc, EllipsoidCalculations rbcp, ElevationProvider elevationProvider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        GraphNode n = graph.addNode("", rbcp.toCart(gc, elevationProvider, missingElevationHandler));
        if (graph.getNodeCount() > 1) {
            graph.connectNodes(graph.getNode(graph.getNodeCount() - 2), n, "");
        }
    }


}
