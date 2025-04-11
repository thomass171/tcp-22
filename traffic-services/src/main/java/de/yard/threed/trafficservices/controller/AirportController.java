package de.yard.threed.trafficservices.controller;


import de.yard.threed.trafficservices.services.AirportService;
import de.yard.threed.trafficservices.util.AirportFilter;
import de.yard.threed.trafficservices.util.AirportResponse;
import de.yard.threed.trafficservices.util.AirportSearchResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
//it's no REST @RestController
@Controller
@Slf4j
public class AirportController {

    private final AirportService airportService;

    /**
     * Currently only a simple icao 'startswith' filter
     */
    @CrossOrigin
    @GetMapping("/traffic/airport/search/findByFilter")
    public ResponseEntity<AirportSearchResponse> findByFilter(@RequestParam("icao") String icao) {

        if (icao == null || icao.length() < 2) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        AirportSearchResponse response = airportService.findByFilter(new AirportFilter(icao));
        if (response == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @GetMapping("/traffic/airport/{icao}")
    public ResponseEntity<AirportResponse> findAirport(@PathVariable("icao") String icao) {

        if (icao == null || icao.length() != 4) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        AirportResponse response = airportService.findAirport(icao);
        if (response == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(response);
    }
}