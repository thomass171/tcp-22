package de.yard.threed.sceneserver;


import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Packet;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.sceneserver.testutils.PlatformSceneServerFactoryForTesting;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientListenerTest {

    ClientListener clientListener;

    @BeforeEach
    public void setup() {

        ClientListener.dropInstance();

        // System (eg UserSystem) need a platform
        TestFactory.initPlatformForTest(new String[]{"data"}, new PlatformSceneServerFactoryForTesting(), (InitMethod) null,
                Configuration.buildDefaultConfigurationWithEnv(new HashMap<>()));

        clientListener = ClientListener.getInstance("localhost", -1);
        clientListener.start();
        // race condition. Wait for socket listening
        sleepMs(100);
    }

    @AfterEach
    public void tearDown() {

        ClientListener.dropInstance();
    }

    @Test
    public void testSimple() throws Exception {
        TestClient testClient = new TestClient(TestClient.USER_NAME0);
        testClient.connectAndLogin();

        TestUtils.waitForClientConnected();

        Packet requestLoginPacket = waitForClientPacket(clientListener.getClientConnections().get(0));
        assertNotNull(requestLoginPacket);
        assertEquals(1 + 2, requestLoginPacket.getData().size());
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
