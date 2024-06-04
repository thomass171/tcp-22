package de.yard.threed.traffic;

import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.Runway;

import java.util.ArrayList;
import java.util.List;

/**
 * A very generic (flight) route based on geo coordinates. Minimum requirement is a starting point and one waypoint.
 * If a waypoint is the latest point (ie. no end point), the route might end in a circle holding if its not continued.
 * Starting/end points might be runways or just points for helicopter and rockets.
 * <p>
 * Does not contain base (groundnet) movement. But why not!?
 * Coordinates might contain elevation, but at least for runways elevation shouldn't be set to
 * make sure elevation derived from terrain is used.
 * <p>
 * Doesn't use any symbolic names for Airports, Waypoints or runways, just geo cooridnates. So this route is
 * independent from any further information.
 * Also doen't use class {@link Runway} for the same reason. For the flight route the runway information isn't needed (only for building a
 * route).
 * <p>
 * Can be (de)serialized to a plain string.
 * Can be used up to "Low Orbit Tour"s.
 * <p>
 * Renamed GeoFlightRoute->GeoRoute to make clear its not limited to flying.
 */
public class GeoRoute {

    private static String LABEL_TAKEOFF = "takeoff";
    private static String LABEL_TOUCHDOWN = "touchdown";
    private static String LABEL_WAYPOINT = "wp";

    public static String SAMPLE_EDKB_EDDK = "wp:50.768,7.1672000->takeoff:50.7692,7.1617000->wp:50.7704,7.1557->wp:50.8176,7.0999->wp:50.8519,7.0921->touchdown:50.8625,7.1317000->wp:50.8662999,7.1443999";

    GeoCoordinate takeoff, touchdown;
    List<GeoCoordinate> waypointsBeforeTakeoff = new ArrayList<>();
    List<GeoCoordinate> waypointsInFlight = new ArrayList<>();
    List<GeoCoordinate> waypointsAfterTouchdown = new ArrayList<>();

    /**
     * Flight route with takeoff on a runway.
     *
     * @param start         The final hold point(?) on runway before ...??
     * @param takeoff
     * @param firstWayPoint
     */
    public GeoRoute(GeoCoordinate start, GeoCoordinate takeoff, GeoCoordinate firstWayPoint) {
        waypointsBeforeTakeoff.add(start);
        this.takeoff = takeoff;
        waypointsInFlight.add(firstWayPoint);
    }

    /**
     * Flight route with vertical takeoff.
     */
    public GeoRoute(GeoCoordinate takeoff, GeoCoordinate firstWayPoint) {
        this.takeoff = takeoff;
        waypointsInFlight.add(firstWayPoint);
    }

    private GeoRoute() {

    }

    public void addWaypoint(GeoCoordinate wp) {
        if (touchdown != null) {
            waypointsAfterTouchdown.add(wp);
        } else if (takeoff != null) {
            waypointsInFlight.add(wp);
        } else {
            waypointsBeforeTakeoff.add(wp);
        }
    }

    public void addLanding(GeoCoordinate lastInAir, GeoCoordinate touchdown, GeoCoordinate end) {
        waypointsInFlight.add(lastInAir);
        this.touchdown = touchdown;
        waypointsAfterTouchdown.add(end);
    }

    @Override
    public String toString() {
        String s = "";
        s += toString(waypointsBeforeTakeoff);
        s += "->";
        s += LABEL_TAKEOFF + ":" + takeoff.toString();
        s += "->";
        s += toString(waypointsInFlight);
        s += "->";
        s += LABEL_TOUCHDOWN + ":" + touchdown.toString();
        s += "->";
        s += toString(waypointsAfterTouchdown);
        return s;
    }

    /**
     * Difficult to decouple because takeoff/touchdown information should be retained.
     * Needs elevationProvider because elevation might be missing in route.
     * @return
     */
    public Graph toGraph(EllipsoidCalculations rbcp, double cruisingaltitude, ElevationProvider elevationProvider, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        Graph graph = new Graph();
        add(graph, waypointsBeforeTakeoff, rbcp, elevationProvider, missingElevationHandler);
        add(graph, takeoff, rbcp, elevationProvider, missingElevationHandler);
        add(graph, waypointsInFlight, rbcp, (latitudedeg, longitudedeg) -> cruisingaltitude, missingElevationHandler);
        add(graph, touchdown, rbcp, elevationProvider, missingElevationHandler);
        add(graph, waypointsAfterTouchdown, rbcp, elevationProvider, missingElevationHandler);
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

    public static GeoRoute parse(String s) {
        GeoRoute route = new GeoRoute();
        String[] mp = StringUtils.split(s, "->");
        List<GeoCoordinate> l = route.waypointsBeforeTakeoff;
        for (int i = 0; i < mp.length; i++) {
            String[] ip = StringUtils.split(mp[i], ":");
            if (ip[0].equals("wp")) {
                l.add(toGeoCoordinate(Util.parseLatLon(ip[1])));
            } else if (ip[0].equals("takeoff")) {
                route.takeoff = toGeoCoordinate(Util.parseLatLon(ip[1]));
                l = route.waypointsInFlight;
            } else if (ip[0].equals("touchdown")) {
                route.touchdown = toGeoCoordinate(Util.parseLatLon(ip[1]));
                l = route.waypointsAfterTouchdown;
            } else {
                //TODO log
            }
        }
        return route;
    }

    private static GeoCoordinate toGeoCoordinate(LatLon latLon) {
        return new GeoCoordinate(latLon.getLatDeg(), latLon.getLonDeg());
    }

    private String toString(List<GeoCoordinate> waypoints) {
        String s = "";
        for (int i = 0; i < waypoints.size(); i++) {
            s += (i > 0) ? "->" : "";
            s += LABEL_WAYPOINT + ":" + waypoints.get(i).toString();
        }
        return s;
    }
}
