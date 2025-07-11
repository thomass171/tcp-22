package de.yard.threed.trafficservices.services;

import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.LatLon;
import de.yard.threed.trafficcore.GeoRoute;
import de.yard.threed.trafficcore.GeoRouteBuilder;
import de.yard.threed.trafficcore.SimpleEllipsoidCalculations;
import de.yard.threed.trafficservices.util.WebLatLon;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RouteService {

    public GeoRoute buildAirportToAirportRoute(LatLon runwayFromFrom, LatLon runwayFromTo,
                                               LatLon runwayToFrom, LatLon runwayToTo) {
        GeoRouteBuilder geoRouteBuilder = new GeoRouteBuilder(new SimpleEllipsoidCalculations(SimpleEllipsoidCalculations.eQuatorialEarthRadius));
        GeoRoute geoRoute = geoRouteBuilder.buildAirportToAirportRoute(
                GeoCoordinate.fromLatLon(GeoCoordinate.fromDegrees(runwayFromFrom.getLatDeg().getDegree(), runwayFromFrom.getLonDeg().getDegree())),
                GeoCoordinate.fromLatLon(GeoCoordinate.fromDegrees(runwayFromTo.getLatDeg().getDegree(), runwayFromTo.getLonDeg().getDegree())),
                GeoCoordinate.fromLatLon(GeoCoordinate.fromDegrees(runwayToFrom.getLatDeg().getDegree(), runwayToFrom.getLonDeg().getDegree())),
                GeoCoordinate.fromLatLon(GeoCoordinate.fromDegrees(runwayToTo.getLatDeg().getDegree(), runwayToTo.getLonDeg().getDegree()))
        );
        return geoRoute;
    }
}
