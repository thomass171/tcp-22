package de.yard.threed.sceneserver;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JettyTest {
    Server jettyServer;
    int port=8090;

    @BeforeEach
     public void setup() throws Exception {
         jettyServer = JettyServer.startJettyServer(port);
    }

    @AfterEach
    public void tearDown(){
        try {
            log.debug("Stopping jetty");
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

    /**
     * From https://www.baeldung.com/async-http-client-websockets
     *
     * @throws Exception
     */
    @Test
    public void testSocket() throws Exception {
        String uri = "ws://localhost:8090/toUpper";

        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder
                = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder
                .addWebSocketListener(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket websocket) {
                        log.debug("onOpen");
                    }

                    @Override
                    public void onClose(WebSocket websocket, int code, String reason) {
                        log.debug("onClose");
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.debug("onError",t);
                    }
                }).build();

        WebSocket webSocket = Dsl.asyncHttpClient()
                .prepareGet(uri/*"ws://localhost:5590/websocket"*/)
                .addHeader("header_name", "header_value")
                .addQueryParam("key", "value")
                .setRequestTimeout(5000)
                .execute(wsHandler)
                .get();

        if (webSocket.isOpen()) {
            webSocket.sendTextFrame("test message");
            webSocket.sendBinaryFrame(new byte[]{'t', 'e', 's', 't'});
        }
    }

}
