package de.yard.threed.traffic;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.graph.Graph;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;

/**
 * Not really related to interface FlightRouteBuilder.
 * Extended in tcp-flightgear
 */
public class BasicRouteBuilder {
    public static double platzrundealtitude = 300;
    public static double cruisingaltitude = 900;
    protected EllipsoidCalculations rbcp;

    public BasicRouteBuilder(EllipsoidCalculations rbcp) {
        this.rbcp = rbcp;
    }


    /**
     *
     */
    public FlightRouteGraph fromGeoRoute(GeoRoute geoRoute, GeneralParameterHandler<GeoCoordinate> missingElevationHandler){
        Graph graph = geoRoute.toGraph(rbcp, cruisingaltitude,  (ElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION), missingElevationHandler);

        FlightRouteGraph flightRoute = new FlightRouteGraph(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
        return flightRoute;
    }
}
