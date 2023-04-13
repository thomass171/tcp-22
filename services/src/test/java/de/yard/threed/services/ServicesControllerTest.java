package de.yard.threed.services;


import de.yard.threed.services.maze.Maze;
import de.yard.threed.services.maze.MazeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ServicesControllerTest {

    public static String ENDPOINT_CONFIRM_SECRET = "/mazes/confirmsecret";

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private MazeRepository mazeRepository;

    @Autowired
    AuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testConfirmSecretSuccess() throws Exception {

        long sokobanWikipediaId = mazeRepository.findByName("Sokoban Wikipedia").getId();

        Content responseContent = confirmSecret(sokobanWikipediaId, "Baskerville");

        assertNotNull(responseContent);
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testconfirmSecretFail() {

        long sokobanWikipediaId = mazeRepository.findByName("Sokoban Wikipedia").getId();


        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> confirmSecret(sokobanWikipediaId, "xx"));

        assertEquals(400, exception.getStatusCode());
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testconfirmSecretNotFound() {

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> confirmSecret(-1, "xx"));

        assertEquals(404, exception.getStatusCode());
    }

    @Test
    @Sql({"classpath:testGrids.sql"})
    public void testconfirmSecretBadParam() {

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> confirmSecret(-1, null));

        assertEquals(400, exception.getStatusCode());
    }

    private Content confirmSecret(long mazeId, String secret) throws Exception {
        URI uri = new URIBuilder("http://localhost:" + port + ENDPOINT_CONFIRM_SECRET)
                .addParameter((secret != null) ? "secret" : "xx", secret)
                .addParameter("mazeid", "" + mazeId)
                .build();

        Content responseContent = Request.get(uri).execute().returnContent();
        return responseContent;
    }

    private long protectSokobanWikipedia() {
        Maze m = mazeRepository.findByName("Sokoban Wikipedia");
        assertNotNull(m);
        m.setSecret(authorizationService.encryptSecret("Baskerville"));
        log.debug("{}",m.getSecret());
        mazeRepository.save(m);
        return m.getId();
    }
}