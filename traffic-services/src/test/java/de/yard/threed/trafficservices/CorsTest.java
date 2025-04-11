package de.yard.threed.trafficservices;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static de.yard.threed.trafficservices.controller.AirportControllerTest.ENDPOINT_AIRPORT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@Slf4j
public class CorsTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .dispatchOptions(true).build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testEndpointAirport() throws Exception {

        MvcResult result = this.mockMvc.perform(options(ENDPOINT_AIRPORT+"/EDDK")
                .header(HttpHeaders.ORIGIN, "localhost")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Access-Control-Allow-Headers, Authorization, Origin, Accept, Access-Control-Request-Method, Access-Control-Request-Headers"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                // not sure '*' is really correct(??)
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Max-Age", "1800"))
                .andReturn();

        String allowMethods = result.getResponse().getHeader("Access-Control-Allow-Methods");
        assertFalse(allowMethods.contains("DELETE"));
        assertFalse(allowMethods.contains("PUT"));
        assertFalse(allowMethods.contains("PATCH"));
        assertTrue(allowMethods.contains("GET"));
    }


}