package de.yard.threed.traffic;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.graph.Graph;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.trafficcore.GeoRouteBuilder;
import de.yard.threed.trafficcore.ElevationProvider;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.trafficcore.GeoRoute;

/**
 * Not really related to interface FlightRouteBuilder.
 * Extended in tcp-flightgear
 */
public class BasicRouteBuilder {
    private static Log logger = Platform.getInstance().getLog(BasicRouteBuilder.class);

    /**
     *
     */
    public static FlightRouteGraph fromGeoRoute(EllipsoidCalculations rbcp, GeoRoute geoRoute, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        GeoRouteHelper geoRouteHelper = new GeoRouteHelper(geoRoute);
        Graph graph = geoRouteHelper.toGraph(rbcp, GeoRouteBuilder.cruisingAltitude, (ElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION), missingElevationHandler);

        FlightRouteGraph flightRoute = new FlightRouteGraph(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
        return flightRoute;
    }


}
