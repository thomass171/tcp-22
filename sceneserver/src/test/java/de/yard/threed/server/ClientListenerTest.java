package de.yard.threed.server;


import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Packet;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.server.testutils.PlatformSceneServerFactoryForTesting;
import de.yard.threed.server.testutils.TestClient;
import de.yard.threed.server.testutils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientListenerTest {

    ClientListener clientListener;

    @BeforeEach
    public void setup() {

        ClientListener.dropInstance();

        // System (eg UserSystem) need a platform
        TestFactory.initPlatformForTest(new String[]{"data"}, new PlatformSceneServerFactoryForTesting(), (InitMethod)null);

        clientListener = ClientListener.getInstance("localhost",-1);
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
        TestClient testClient = new TestClient();
        testClient.connectAndLogin();

        TestUtils.waitForClientConnected();

        Packet packet = waitForClientPacket(clientListener.getClientConnections().get(0));
        assertNotNull(packet);
        assertEquals(2,packet.getData().size());
    }



    private Packet waitForClientPacket(ClientConnection clientConnection) {

        int cnt = 0;

        Packet packet;

        while ((packet = clientConnection.getPacket())==null) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("no packet from client");
            }
        }
        return packet;
    }


}
