package de.yard.threed.traffic.flight;

import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.Runway;

import java.util.ArrayList;
import java.util.List;

/**
 * A very generic flight route based on geo coordinates. Minimum requirement is a starting point and one waypoint.
 * If a waypoint is the latest point (ie. no end point), the route might end in a circle holding if its not continued.
 * Starting/end points might be runways or just points for helicopter and rockets.
 * <p>
 * Does not contain base (groundnet) movement. Coordinates might contain elevation, but at least for runways elevation shouldn't be set to
 * make sure elevation derived from terrain is used.
 * <p>
 * Doesn't use any symbolic names for Airports, Waypoints or runways, just geo cooridnates. So this route is
 * independent from any further information.
 * Also doen't use class {@link Runway} for the same reason. For the flight route the runway information isn't needed (only for building a
 * route).
 * <p>
 * Can be (de)serialized to a plain string.
 * Can be used up to "Low Orbit Tour"s.
 */
public class GeoFlightRoute {

    private static String LABEL_START = "start";
    private static String LABEL_TAKEOFF = "takeoff";

    GeoCoordinate start;
    // might be null for vertical takeoff
    GeoCoordinate takeoff;
    List<GeoCoordinate> waypoints = new ArrayList<>();

    /**
     * Flight route with takeoff on a runway.
     *
     * @param start         The final hold point(?) on runway before ...??
     * @param takeoff
     * @param firstWayPoint
     */
    public GeoFlightRoute(GeoCoordinate start, GeoCoordinate takeoff, GeoCoordinate firstWayPoint) {
        this.start = start;
        this.takeoff = takeoff;
        waypoints.add(firstWayPoint);
    }

    /**
     * Flight route with vertical takeoff.
     */
    public GeoFlightRoute(GeoCoordinate start, GeoCoordinate firstWayPoint) {
        this.start = start;
        this.takeoff = null;
        waypoints.add(firstWayPoint);
    }

    @Override
    public String toString(){
        String s="";
        s+=LABEL_START+":"+toString(start);
        return s;
    }

    private String toString(GeoCoordinate coordinate){
        String s = coordinate.getLatDeg().toString()+","+        coordinate.getLonDeg().toString();
        if (coordinate.getElevationM()!=null){
            // what is good separator? ':'?
            s += ","+coordinate.getElevationM().toString();
        }
        return s;
    }
}
