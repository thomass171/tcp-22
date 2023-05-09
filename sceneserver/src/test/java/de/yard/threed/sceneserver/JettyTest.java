package de.yard.threed.sceneserver;


import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.javanative.JsonUtil;
import de.yard.threed.sceneserver.jsonmodel.Client;
import de.yard.threed.sceneserver.jsonmodel.Status;
import de.yard.threed.sceneserver.testutils.TestUtils;
import de.yard.threed.sceneserver.testutils.WSClient;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JettyTest {
    Server jettyServer;
    int port = 8090;

    @BeforeEach
    public void setup() throws Exception {

        ClientListener.init("localhost", de.yard.threed.core.Server.DEFAULT_BASE_PORT);
        ClientListener.getInstance();

        jettyServer = JettyServer.startJettyServer(port);
    }

    @AfterEach
    public void tearDown() {
        ClientListener.dropInstance();
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

        String response = TestUtils.httpGet(url);
        log.debug("response={}", response);

        Status status = JsonUtil.fromJson(response, Status.class);
        assertNotNull(status);
    }

    /**
     * From https://www.baeldung.com/async-http-client-websockets
     */
    @Test
    public void testToUpperSocket() throws Exception {
        String uri = "ws://localhost:8090/toUpper";

        StringBuffer response = new StringBuffer();

        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder.addWebSocketListener(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket websocket) {
                log.debug("onOpen");
            }

            @Override
            public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                log.debug("Got response {}", payload);
                response.append(payload);
            }

            @Override
            public void onClose(WebSocket websocket, int code, String reason) {
                log.debug("onClose");
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError", t);
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

            //TODO waitUntil();
            Thread.sleep(1000);
        }

        assertEquals("TEST MESSAGE", response.toString());
    }

    @Test
    public void testWSClient() throws Exception {

        NativeSocket wsSocket = WSClient.connectToServer(new de.yard.threed.core.Server("localhost", 8090));

        wsSocket.sendPacket(new Packet().add("m", "test message"));
    }

    @Test
    public void testJson() throws Exception {

        Status status = new Status();
        status.setCpuload(0.5);
        Client client = new Client();
        OffsetDateTime connectedAt = OffsetDateTime.of(2012, 1, 13, 5, 6, 7, 0, ZoneOffset.UTC);
        client.setConnectedAt(connectedAt);
        List<Client> clients = new ArrayList<>();
        clients.add(client);
        status.setClients(clients);

        String json = JsonUtil.toJson(status);
        log.debug("json={}", json);
        // should use ISO instead of single fields
        assertTrue(json.contains("2012-01-13T05:06:07Z"));

        status = JsonUtil.fromJson(json, Status.class);
        assertNotNull(status);
        assertEquals(connectedAt, status.getClients().get(0).getConnectedAt());
    }
}
