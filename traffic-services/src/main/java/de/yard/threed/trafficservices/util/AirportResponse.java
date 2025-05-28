package de.yard.threed.trafficservices.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirportResponse {
    String icao;
    String name;
    final List<Runway> runways = new ArrayList();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Runway {
        LatLonResponse from;
        String fromNumber;
        LatLonResponse to;
        String toNumber;
        double width;
        double heading;
    }
}
