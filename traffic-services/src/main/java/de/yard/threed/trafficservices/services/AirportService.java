package de.yard.threed.trafficservices.services;

import de.yard.threed.core.LatLon;
import de.yard.threed.trafficcore.geodesy.GeoTools;
import de.yard.threed.trafficservices.util.AirportFilter;
import de.yard.threed.trafficservices.util.AirportResponse;
import de.yard.threed.trafficservices.util.AirportSearchResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AirportService implements RepositoryRestConfigurer {

    private final AptDatService aptDatService;

    public AirportSearchResponse findByFilter(AirportFilter filter) {
        List<AirportSearchResponse.Airport> airports = new ArrayList<>();
        List<AirportLine> airportLines = aptDatService.getAirports(filter);
        if (airportLines == null) {
            return null;
        }
        airportLines.forEach(a -> airports.add(new AirportSearchResponse.Airport(a.getIcao(), a.getName())));
        return new AirportSearchResponse(airports);

    }

    public AirportResponse findAirport(String icao) {
        List<AptLine> airportLines = aptDatService.getAirport(icao);
        if (airportLines == null) {
            return null;
        }

        if (airportLines.size() == 0) {
            return null;
        }
        AirportLine airportLine = (AirportLine) airportLines.get(0);
        AirportResponse response = new AirportResponse(airportLine.getIcao(), airportLine.getName());
        for (AptLine aptLine : airportLines){
            if (aptLine instanceof RunwayLine){
                RunwayLine runwayLine = (RunwayLine) aptLine;
                response.getRunways().add(new AirportResponse.Runway(
                        runwayLine.getFromLat(), runwayLine.getFromLon(), runwayLine.getFromNumber(),
                        runwayLine.getToLat(), runwayLine.getToLon(), runwayLine.getToNumber(),
                        runwayLine.getWidth(), GeoTools.heading( LatLon.fromDegrees(runwayLine.getFromLat(), runwayLine.getFromLon()),
                         LatLon.fromDegrees(runwayLine.getToLat(), runwayLine.getToLon())).getDegree()));
            }
        }
        return response;
    }
}
