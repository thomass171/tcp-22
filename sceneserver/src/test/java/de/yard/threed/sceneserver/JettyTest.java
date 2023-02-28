package de.yard.threed.sceneserver;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class JettyTest {
    Server jettyServer;

    @BeforeEach
     public void setup() throws Exception {
         jettyServer = JettyServer.startJettyServer(8090);
    }

    @AfterEach
    public void tearDown(){
        try {
            jettyServer.stop();
            // need to wait?
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStatus() throws Exception {
        String url = "http://localhost:8090/status";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        HttpResponse response = client.execute(request);

        assertEquals(200, response.getStatusLine().getStatusCode());
    }
}
