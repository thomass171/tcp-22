package de.yard.threed.servermanager;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
public class ServerControllerTest {

    private String URL = "/server";
    private String URL_LIST = "/server";
    private String URL_START = "/server";

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private ServerManagerService serverManagerService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        serverManagerService.stopAllServer();
    }

    @Test
    public void testStartListStop() throws Exception {

        CloseableHttpClient client = HttpClientBuilder.create().build();


        // start server
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("scenename", "de.yard.threed.maze.MazeScene"));
        nameValuePairs.add(new BasicNameValuePair("gridname", "skbn/SokobanWikipedia.txt"));
        URI uri = new URIBuilder("http://localhost:" + port + URL).addParameters(nameValuePairs).build();

        HttpPost post = new HttpPost(uri);
        CloseableHttpResponse response = client.execute(post);
        assertEquals(201, response.getStatusLine().getStatusCode());
        String responseBody = readResponse(response);
        log.debug("response={}", responseBody);
        ServerInstance si = objectMapper.readValue(responseBody, ServerInstance.class);
        assertEquals(-1, si.getPid());
        int serverid = si.getId();

        // get list
        ServerInstanceList sil = getList(client);
        assertEquals(1, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_RUNNING, sil.getServerInstanceList().get(0).getState());

        // stop server
        nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("id", ""+serverid));
        uri = new URIBuilder("http://localhost:" + port + URL).addParameters(nameValuePairs).build();
        HttpDelete delete = new HttpDelete(uri);
        response = client.execute(delete);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusLine().getStatusCode());
        // no response

        sil = getList(client);
        assertEquals(1, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_TERMINATED, sil.getServerInstanceList().get(0).getState());

        client.close();
    }

    private ServerInstanceList getList(CloseableHttpClient client) throws Exception {
        URI uri = new URIBuilder("http://localhost:" + port + URL + "/list").build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String responseBody = readResponse(response);
        log.debug("response={}", responseBody);
        ServerInstanceList sil = objectMapper.readValue(responseBody, ServerInstanceList.class);
        return sil;
    }

    private String readResponse(CloseableHttpResponse response) throws IOException {
        String responseBody = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return responseBody;
    }
}