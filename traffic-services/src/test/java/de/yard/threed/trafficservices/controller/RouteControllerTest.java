package de.yard.threed.trafficservices.controller;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.testutils.TestUtils;
import de.yard.threed.javacommon.MinimalisticPlatform;
import de.yard.threed.javacommon.MinimalisticPlatformFactory;
import de.yard.threed.trafficservices.services.JsonService;
import de.yard.threed.trafficservices.util.RouteBuildResponse;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class RouteControllerTest {

    Platform platform = CoreTestFactory.initPlatformForTest(new MinimalisticPlatformFactory(), null);

    public static String ENDPOINT_ROUTE = "/traffic/route";

    @Value(value = "${local.server.port}")
    private int port;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JsonService jsonService;

    @BeforeEach
    void setUp() {
        MinimalisticPlatform.getInstance();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testBuildAirportToAirport() throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("runwayFromFrom", "50.7680,7.1672");
        params.add("runwayFromTo", "50.7692,7.1617");
        params.add("runwayToFrom", "50.8625,7.1317");
        params.add("runwayToTo", "50.8663,7.1444");

        MvcResult result = TestUtils.doGet(mockMvc, ENDPOINT_ROUTE + "/buildAirportToAirport", params);
        assertEquals(200, result.getResponse().getStatus());
        log.debug("response={}", result.getResponse().getContentAsString());
        RouteBuildResponse response = jsonService.jsonToModel(result.getResponse().getContentAsString(), RouteBuildResponse.class);

        assertEquals("wp:50.7681904,7.1663272->takeoff:50.7697576,7.1591436->wp:50.7785403,7.1188541->wp:50.7841839,7.1078005->wp:50.7918687,7.1004420->wp:50.8006778,7.0976580->wp:50.8272422,7.0860276->wp:50.8360513,7.0832420->wp:50.8447672,7.0866863->wp:50.8517169,7.0957016->touchdown:50.8632697,7.1342719->wp:50.8662999,7.1443999", response.getGeoRoute());

    }


}