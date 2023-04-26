package de.yard.threed.sceneserver;


import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.sceneserver.testutils.PlatformSceneServerFactoryForTesting;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.SceneServerTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * No scene server is started but only the ClientListener and Jetty (for websocket connection).
 */
@Slf4j
public class ClientConnectionTest {

    ClientListener clientListener;
    Server jettyServer;

    @BeforeEach
    public void setup() throws Exception {

        ClientListener.dropInstance();

        EngineTestFactory.initPlatformForTest(new String[]{"data"}, new PlatformSceneServerFactoryForTesting(), (InitMethod) null,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));

        jettyServer = JettyServer.startJettyServer(de.yard.threed.core.Server.DEFAULT_BASE_PORT + 1);

        ClientListener.init("localhost", de.yard.threed.core.Server.DEFAULT_BASE_PORT);
        clientListener = ClientListener.getInstance();
        clientListener.start();
        // race condition. Wait for socket listening
        sleepMs(100);
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
    public void testSimple() throws Exception {
        TestClient testClient = new TestClient(TestClient.USER_NAME0);
        testClient.connectAndLogin();

        SceneServerTestUtils.waitForClientConnected();

        Packet requestLoginPacket = waitForFirstClientPacket();
        assertNotNull(requestLoginPacket);
        assertEquals(1 + 2, requestLoginPacket.getData().size());

        assertEquals(1, clientListener.getClientConnectionCount());
        // nothing to discard yet
        clientListener.discardClosedConnection();
        assertEquals(1, clientListener.getClientConnectionCount());

        // disconnect not possible without server, but possible by just closing socket.
        testClient.disconnectByClose();
        // give chance to detect closed peer. Might not be reliable. So do multiple times to have better chance. Hmm, tricky.
        log.debug("Sending packet to closed client");
        TestUtils.waitUntil(() -> {
            clientListener.publishPacketToClients(new Packet().add("key", "value"), null);
            clientListener.discardClosedConnection();
            return clientListener.getClientConnectionCount() == 0;
        }, 5000);

        // now connection should be discard
        assertEquals(0, clientListener.getClientConnectionCount());

        sleepMs(100);
    }

    @Test
    public void testWebsocketConnect() throws Exception {
        TestClient testClient = new TestClient(TestClient.USER_NAME0);
        testClient.connectAndLogin(true);

        SceneServerTestUtils.waitForClientConnected();

        Packet requestLoginPacket = waitForFirstClientPacket();
        assertNotNull(requestLoginPacket);
        // request+user+id?
        assertEquals(1 + 2, requestLoginPacket.getData().size());

        // disconnect not possible without server
        // testClient.disconnect();
    }

    private Packet waitForFirstClientPacket() {

        int cnt = 0;

        List<Pair<Packet, String>> l;

        while ((l = ClientListener.getInstance().getPacketsFromClients()).size() == 0) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("no packet from client");
            }
        }
        return l.get(0).getFirst();
    }


}
