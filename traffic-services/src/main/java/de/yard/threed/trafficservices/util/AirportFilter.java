package de.yard.threed.trafficservices.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AirportFilter {
    // no regex, but a 'startswith' pattern
    String icao;
    // to come in future Degree minlat,maxlat,minlon,maxlon
}
