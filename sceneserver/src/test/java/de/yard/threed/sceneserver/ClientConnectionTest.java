package de.yard.threed.sceneserver;


import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Packet;
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

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class ClientConnectionTest {

    ClientListener clientListener;
    Server jettyServer;

    @BeforeEach
    public void setup() throws Exception {

        ClientListener.dropInstance();

        // System (eg UserSystem) need a platform
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

        Packet requestLoginPacket = waitForClientPacket(clientListener.getClientConnections().get(0));
        assertNotNull(requestLoginPacket);
        assertEquals(1 + 2, requestLoginPacket.getData().size());

        // disconnect not possible without server
        // testClient.disconnect();
    }

    @Test
    public void testWebsocketConnect() throws Exception {
        TestClient testClient = new TestClient(TestClient.USER_NAME0);
        testClient.connectAndLogin(true);

        SceneServerTestUtils.waitForClientConnected();

        Packet requestLoginPacket = waitForClientPacket(clientListener.getClientConnections().get(0));
        assertNotNull(requestLoginPacket);
        // request+user+id?
        assertEquals(1 + 2, requestLoginPacket.getData().size());

        // disconnect not possible without server
        // testClient.disconnect();
    }

    private Packet waitForClientPacket(ClientConnection clientConnection) {

        int cnt = 0;

        Packet packet;

        while ((packet = clientConnection.getPacket()) == null) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("no packet from client");
            }
        }
        return packet;
    }


}
