package de.yard.threed.trafficservices.controller;


import de.yard.threed.core.Util;
import de.yard.threed.trafficcore.GeoRoute;
import de.yard.threed.trafficservices.services.RouteService;
import de.yard.threed.trafficservices.util.RouteBuildResponse;
import de.yard.threed.trafficservices.util.WebLatLon;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
//it's no REST @RestController
@Controller
@Slf4j
public class RouteController {

    private final RouteService routeService;

    @CrossOrigin
    @GetMapping("/traffic/route/buildAirportToAirport")
    public ResponseEntity<RouteBuildResponse> buildAirportToAirportRoute(
            // required true is default
            @RequestParam("runwayFromFrom") String runwayFromFrom,
            @RequestParam("runwayFromTo") String runwayFromTo,
            @RequestParam("runwayToFrom") String runwayToFrom,
            @RequestParam("runwayToTo") String runwayToTo) {

        GeoRoute geoRoute = routeService.buildAirportToAirportRoute(
                Util.parseLatLon(runwayFromFrom),
                Util.parseLatLon(runwayFromTo),
                Util.parseLatLon(runwayToFrom),
                Util.parseLatLon(runwayToTo));

        return ResponseEntity.ok(new RouteBuildResponse(geoRoute.toString()));
    }
}