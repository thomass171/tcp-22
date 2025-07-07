package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.graph.Graph;
import de.yard.threed.traffic.flight.FlightRouteGraph;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.model.Runway;

import java.util.ArrayList;
import java.util.List;

/**
 * Not really related to interface FlightRouteBuilder.
 * Extended in tcp-flightgear
 */
public class BasicRouteBuilder {
    public static double circuitAltitude = 300;
    public static double cruisingAltitude = 900;
    public static double takeoffToSidPointDistance = 3;//km;
    protected EllipsoidCalculations rbcp;
    private static Log logger = Platform.getInstance().getLog(BasicRouteBuilder.class);

    public BasicRouteBuilder(EllipsoidCalculations rbcp) {
        this.rbcp = rbcp;
    }

    /**
     *
     */
    public FlightRouteGraph fromGeoRoute(GeoRoute geoRoute, GeneralParameterHandler<GeoCoordinate> missingElevationHandler) {
        Graph graph = geoRoute.toGraph(rbcp, cruisingAltitude, (ElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION), missingElevationHandler);

        FlightRouteGraph flightRoute = new FlightRouteGraph(graph, graph.getEdge(1), graph.getEdge(graph.getEdgeCount() - 2));
        return flightRoute;
    }

    /**
     * Vehicle will be located at runwayFromFrom. Takeoff will be between runwayFromFrom and runwayFromTo.
     * Even though GeoCoordinate is used, the elevation might be null. This probably is the most common use case.
     * Derived from tcp-flightgear::RouteBuilder
     *
     * @param runwayFromFrom
     * @param runwayFromTo
     * @param runwayToFrom   this is considered the touchdown point
     * @param runwayToTo     might not be reached or exceeded depending on braking.
     */
    public GeoRoute buildAirportToAirportRoute(GeoCoordinate runwayFromFrom, GeoCoordinate runwayFromTo,
                                               GeoCoordinate runwayToFrom, GeoCoordinate runwayToTo) {
        // width, fromNumber and toNumber probably not needed
        double width = 0;
        Runway fromRunway = new Runway(runwayFromFrom, "", runwayFromTo, "", width);
        Runway toRunway = new Runway(runwayToFrom, "", runwayToTo, "", width);

        RunwayHelper runwayFromHelper = new RunwayHelper(fromRunway, rbcp);
        RunwayHelper runwayToHelper = new RunwayHelper(toRunway, rbcp);

        // Derived from tcp-flightgear::RouteBuilder.buildTakeOffGraph
        LatLon sidpoint = rbcp.applyCourseDistance(runwayFromHelper.getTakeoffPoint(), runwayFromHelper.getHeading(), takeoffToSidPointDistance);

        GeoRoute geoRoute = new GeoRoute(
                GeoCoordinate.fromLatLon(runwayFromHelper.getHoldingPoint()),
                GeoCoordinate.fromLatLon(runwayFromHelper.getTakeoffPoint()),
                GeoCoordinate.fromLatLon(sidpoint));

        double turnsegmentLen = 1;//km

        LatLon starpoint = rbcp.applyCourseDistance(runwayToHelper.getTouchdownpoint(), runwayToHelper.getHeading().reverse(), takeoffToSidPointDistance);
        Degree roughDirection = rbcp.courseTo(sidpoint, starpoint);

        // Add three climb elements for reaching main air way
        List<LatLon> departPoints = new ArrayList<>();
        int climbElements = 3;
        Degree diff = Degree.diff(runwayFromHelper.getHeading(), roughDirection);
        Degree degreeStep = diff.multiply(1.0 / climbElements);
        Degree departHeading = runwayFromHelper.getHeading();
        LatLon departExitPoint = sidpoint;
        logger.debug("runwayFrom.heading=" + runwayFromHelper.getHeading() + ",roughDirection=" + roughDirection + ",diff=" + diff + ",degreeStep=" + degreeStep);
        for (int i = 0; i < climbElements; i++) {
            departHeading = departHeading.add(degreeStep);
            logger.debug("departHeading=" + departHeading + "diff=" + diff);
            departExitPoint = rbcp.applyCourseDistance(departExitPoint, departHeading, turnsegmentLen);
            departPoints.add(departExitPoint);
        }
        for (int i = 0; i < departPoints.size(); i++) {
            geoRoute.addWaypoint(GeoCoordinate.fromLatLon(departPoints.get(i)));
        }

        // Prepare 'approach' before adding main airway (though currently only a direct connection)

        List<LatLon> approachPoints = new ArrayList<>();
        // Add three descend elements (the 'approach') for leaving main air way and reaching starpoint
        int descendElements = 3;
        diff = Degree.diff(roughDirection, runwayToHelper.getHeading());
        degreeStep = diff.multiply(1.0 / descendElements);
        Degree approachHeading = runwayToHelper.getHeading();
        LatLon approachEntryPoint = starpoint;
        logger.debug("roughDirection=" + roughDirection +",runwayTo.heading=" + runwayToHelper.getHeading() +  ",diff=" + diff + ",degreeStep=" + degreeStep);
        for (int i = 0; i < descendElements; i++) {
            approachHeading = approachHeading.subtract(degreeStep);
            logger.debug("approachHeading=" + approachHeading + "diff=" + diff);
            approachEntryPoint = rbcp.applyCourseDistance(approachEntryPoint, approachHeading.reverse(), turnsegmentLen);
            approachPoints.add(approachEntryPoint);
        }

        for (int i = approachPoints.size() - 1; i >= 0; i--) {
            geoRoute.addWaypoint(GeoCoordinate.fromLatLon(approachPoints.get(i)));
        }
        geoRoute.addLanding(
                GeoCoordinate.fromLatLon(starpoint),
                GeoCoordinate.fromLatLon(runwayToHelper.getTouchdownpoint()),
                // use end of runway for now
                runwayToTo);

        return geoRoute;
    }
}
