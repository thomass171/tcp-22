package de.yard.threed.trafficservices.controller;

import de.yard.threed.testutils.TestUtils;
import de.yard.threed.trafficservices.services.JsonService;
import de.yard.threed.trafficservices.util.TileSearchResponse;
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
public class TileControllerTest {

    public static String ENDPOINT_TILE = "/traffic/tile";

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

        MvcResult result = TestUtils.doGet(mockMvc, ENDPOINT_TILE + "/search/findByFilter");
        assertEquals(200, result.getResponse().getStatus());
        log.debug("response={}", result.getResponse().getContentAsString());
        TileSearchResponse response = jsonService.jsonToModel(result.getResponse().getContentAsString(), TileSearchResponse.class);

        assertEquals(2, response.getTiles().size());
       assertEquals("3072792", response.getTiles().get(0).getName());
       // asumme coors are correct
       assertEquals(50.375, response.getTiles().get(0).getPolygon().getPoints().get(0).getLat());
       assertEquals(50.375, response.getTiles().get(0).getPolygon().getPoints().get(1).getLat());
       assertEquals(50.5, response.getTiles().get(0).getPolygon().getPoints().get(2).getLat());
       assertEquals(50.5, response.getTiles().get(0).getPolygon().getPoints().get(3).getLat());
       assertEquals("2958144", response.getTiles().get(1).getName());
    }


}