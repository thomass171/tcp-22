package de.yard.threed.servermanager;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


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

    private CloseableHttpClient client;

    @BeforeEach
    void setup() {
        client = HttpClientBuilder.create().build();
    }

    @AfterEach
    void tearDown() {
        serverManagerService.stopAllServerAndClean();
    }

    @Test
    public void testStartListStop() throws Exception {

        // start server
        ServerInstance si = startServer("skbn/SokobanWikipedia.txt");
        int serverid = si.getId();

        // get list
        ServerInstanceList sil = getList();
        assertEquals(1, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_RUNNING, sil.getServerInstanceList().get(0).getState());

        // stop server
        stopServer(serverid);

        sil = getList();
        assertEquals(1, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_TERMINATED, sil.getServerInstanceList().get(0).getState());

        client.close();
    }

    @Test
    public void testStartListStopMultipleServer() throws Exception {

        List<ServerInstance> sis = new ArrayList<>();

        sis.add(startServer("skbn/SokobanWikipedia.txt"));
        ServerInstanceList sil = getList();
        assertEquals(1, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_RUNNING, sil.getServerInstanceList().get(0).getState());

        sis.add(startServer("maze/Area15x10.txt", 5895));
        sil = getList();
        assertEquals(2, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_RUNNING, sil.getServerInstanceList().get(0).getState());
        assertEquals(ServerManagerService.STATE_RUNNING, sil.getServerInstanceList().get(1).getState());

        // stop all server
        for (ServerInstance si : sis) {
            stopServer(si.getId());
        }

        sil = getList();
        assertEquals(2, sil.getServerInstanceList().size());
        assertEquals(ServerManagerService.STATE_TERMINATED, sil.getServerInstanceList().get(0).getState());
        assertEquals(ServerManagerService.STATE_TERMINATED, sil.getServerInstanceList().get(1).getState());

        client.close();
    }

    /**
     * Default base port is 5890.
     */
    private ServerInstance startServer(String gridName) throws Exception {
        return startServer(gridName, null);
    }

    private ServerInstance startServer(String gridName, Integer baseport) throws Exception {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("scenename", "de.yard.threed.maze.MazeScene"));
        nameValuePairs.add(new BasicNameValuePair("gridname", gridName));
        if (baseport != null) {
            nameValuePairs.add(new BasicNameValuePair("baseport", Integer.toString(baseport)));
        }
        URI uri = new URIBuilder("http://localhost:" + port + URL).addParameters(nameValuePairs).build();

        HttpPost post = new HttpPost(uri);
        CloseableHttpResponse response = client.execute(post);
        assertEquals(201, response.getCode());
        String responseBody = readResponse(response);
        log.debug("response={}", responseBody);
        ServerInstance si = objectMapper.readValue(responseBody, ServerInstance.class);
        assertEquals(-1, si.getPid());
        return si;
    }

    private ServerInstanceList getList() throws Exception {
        URI uri = new URIBuilder("http://localhost:" + port + URL + "/list").build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        assertEquals(200, response.getCode());
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

    private void stopServer(int serverid) throws Exception {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("id", "" + serverid));
        URI uri = new URIBuilder("http://localhost:" + port + URL).addParameters(nameValuePairs).build();
        HttpDelete delete = new HttpDelete(uri);
        CloseableHttpResponse response = client.execute(delete);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getCode());
        // no response
    }
}