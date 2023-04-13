package de.yard.threed.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static de.yard.threed.services.MazeRepositoryTest.ENDPOINT_MAZES;
import static de.yard.threed.services.ServicesControllerTest.ENDPOINT_CONFIRM_SECRET;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@Slf4j
public class CorsTest {

    String EXPECTED_Access_Control_Allow_Origin = "*";
    String EXPECTED_Access_Control_Max_Age = "1800";

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
    public void testEndpointMazes() throws Exception {

        MvcResult result = this.mockMvc.perform(options(ENDPOINT_MAZES)
                        .header(HttpHeaders.ORIGIN, "localhost")
                        .header("Access-Control-Request-Method", "GET"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().string("Access-Control-Allow-Origin", EXPECTED_Access_Control_Allow_Origin))
                .andExpect(header().string("Access-Control-Max-Age", EXPECTED_Access_Control_Max_Age))
                .andReturn();

        String allowMethods = result.getResponse().getHeader("Access-Control-Allow-Methods");
        assertTrue(allowMethods.contains("DELETE"));
        assertTrue(allowMethods.contains("PUT"));
        assertTrue(allowMethods.contains("PATCH"));
        assertTrue(allowMethods.contains("GET"));
    }

    @Test
    public void testEndpointConfirmsecret() throws Exception {

        this.mockMvc.perform(options(ENDPOINT_CONFIRM_SECRET)
                        .header(HttpHeaders.ORIGIN, "localhost")
                        .header("Access-Control-Request-Method", "GET"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().string("Access-Control-Allow-Origin", EXPECTED_Access_Control_Allow_Origin))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET"))
                .andExpect(header().string("Access-Control-Max-Age", EXPECTED_Access_Control_Max_Age));
    }
}