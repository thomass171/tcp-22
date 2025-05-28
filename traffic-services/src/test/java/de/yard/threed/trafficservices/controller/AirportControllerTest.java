package de.yard.threed.trafficservices.controller;

import de.yard.threed.testutils.TestUtils;
import de.yard.threed.trafficservices.services.JsonService;
import de.yard.threed.trafficservices.util.AirportResponse;
import de.yard.threed.trafficservices.util.AirportSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class AirportControllerTest {

    public static String ENDPOINT_AIRPORT = "/traffic/airport";

    @Value(value = "${local.server.port}")
    private int port;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JsonService jsonService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testFilter() throws Exception {

        MvcResult result = TestUtils.doGet(mockMvc, ENDPOINT_AIRPORT + "/search/findByFilter?icao=ED");
        assertEquals(200, result.getResponse().getStatus());
        AirportSearchResponse response = jsonService.jsonToModel(result.getResponse().getContentAsString(), AirportSearchResponse.class);
        assertEquals(2, response.getAirports().size());
        assertEquals("EDDK", response.getAirports().get(0).getIcao());
        assertEquals("Koln Bonn", response.getAirports().get(0).getName());
        assertEquals("EDKB", response.getAirports().get(1).getIcao());
        assertEquals("Bonn-Hangelar", response.getAirports().get(1).getName());
    }

    @Test
    public void testAirport() throws Exception {

        MvcResult result = TestUtils.doGet(mockMvc, ENDPOINT_AIRPORT + "/EDDK");
        assertEquals(200, result.getResponse().getStatus());
        AirportResponse airportResponse = jsonService.jsonToModel(result.getResponse().getContentAsString(), AirportResponse.class);
        assertEquals("EDDK", airportResponse.getIcao());
        assertEquals("Koln Bonn", airportResponse.getName());
        List<AirportResponse.Runway> runwys = airportResponse.getRunways();
        assertEquals(3, runwys.size());
        assertEquals(50.880469, runwys.get(0).getFrom().getLat());
        assertEquals(7.129075, runwys.get(0).getFrom().getLon());
        assertEquals("14L", runwys.get(0).getFromNumber());
        assertEquals(50.855194, runwys.get(0).getTo().getLat());
        assertEquals(7.165692, runwys.get(0).getTo().getLon());
        assertEquals("32R", runwys.get(0).getToNumber());
        assertEquals(60.05, runwys.get(0).getWidth());
        assertEquals(137.548, runwys.get(0).getHeading(), 0.001);
    }

    @Test
    public void testInvalidParameter() throws Exception {

        MvcResult result = TestUtils.doGet(mockMvc, ENDPOINT_AIRPORT + "/search/findByFilter?icao=E");
        assertEquals(400, result.getResponse().getStatus());
    }
}