package de.yard.threed.sceneserver.testutils;


import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketClient;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServerBusConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.engine.ecs.UserSystem.USER_REQUEST_LOGIN;


public class TestClient {
    SocketClient testClient;
    QueuingSocketListener listener;
    Log logger = Platform.getInstance().getLog(TestClient.class);

    public TestClient() {
        testClient = new SocketClient("localhost", ClientListener.DEFAULT_PORT);
    }

    public void connectAndLogin() throws IOException {
        if (!SystemState.readyToJoin()) {
            logger.warn("not ready to join");
        }
        testClient.connect();
        listener = testClient.startListen();
        testClient.writePacket(SceneServerBusConnector.buildPacket(USER_REQUEST_LOGIN, new String[]{"carl"}).getData());
    }

    public Packet getPacket() {
        List<String> packet = listener.getPacket();
        if (packet == null) {
            return null;
        }
        return Packet.buildFromBlock(packet);
    }

    public List<Packet> getAllPackets() {
        List<Packet> packets = new ArrayList<>();
        Packet packet;
        while ((packet = getPacket()) != null) {
            packets.add(packet);
        }
        return packets;
    }
}
