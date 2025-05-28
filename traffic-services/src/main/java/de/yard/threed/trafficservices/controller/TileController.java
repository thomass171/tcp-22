package de.yard.threed.trafficservices.controller;


import de.yard.threed.core.Util;
import de.yard.threed.trafficservices.services.AirportService;
import de.yard.threed.trafficservices.services.TileService;
import de.yard.threed.trafficservices.util.AirportFilter;
import de.yard.threed.trafficservices.util.AirportResponse;
import de.yard.threed.trafficservices.util.AirportSearchResponse;
import de.yard.threed.trafficservices.util.TileResponse;
import de.yard.threed.trafficservices.util.TileSearchResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.stream.Collectors;

@AllArgsConstructor
//it's no REST @RestController
@Controller
@Slf4j
public class TileController {

    private final TileService tileService;

    /**
     * Currently no filter, so return all
     */
    @CrossOrigin
    @GetMapping("/traffic/tile/search/findByFilter")
    public ResponseEntity<TileSearchResponse> findByFilter() {

        TileSearchResponse response = null;
        try {
            response = new TileSearchResponse();
            response.setTiles(tileService.getTiles().stream().map(t -> TileResponse.buildFromTile(t)).collect(Collectors.toUnmodifiableList()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @GetMapping("/traffic/tile/{index}")
    public ResponseEntity<TileResponse> findTile(@PathVariable("index") int index) {

        Util.notyet();
        return (ResponseEntity<TileResponse>) ResponseEntity.noContent();
    }
}