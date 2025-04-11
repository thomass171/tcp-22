package de.yard.threed.trafficservices.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * just a list of icaos/names
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirportSearchResponse {
    List<Airport> airports;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Airport{
        String icao;
        String name;
    }
}
