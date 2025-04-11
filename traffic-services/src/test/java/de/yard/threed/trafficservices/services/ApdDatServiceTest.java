package de.yard.threed.trafficservices.services;

import de.yard.threed.trafficservices.util.AirportFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
public class ApdDatServiceTest {

    @Autowired
    private AptDatService aptDatService;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testBasicOperations() throws Exception {

        List<AptLine> lines;
        lines = aptDatService.extractAirportFromApt("EDDK");
        assertEquals(4, lines.size());

        lines = aptDatService.extractAirportFromApt("EDxx");
        assertEquals(0, lines.size());

    }

    @Test
    public void testfindAirports() throws Exception {

        List<AirportLine> lines;
        lines = aptDatService.findAirports(new AirportFilter("EDDK"));
        assertEquals(1, lines.size());

        lines = aptDatService.findAirports(new AirportFilter("E"));
        assertEquals(2, lines.size());

    }

}