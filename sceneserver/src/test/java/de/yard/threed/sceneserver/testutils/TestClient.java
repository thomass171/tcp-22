package de.yard.threed.sceneserver.testutils;


import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketClient;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServerBusConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestClient {
    Log logger = Platform.getInstance().getLog(TestClient.class);
    SocketClient testClient;
    QueuingSocketListener listener;
    public static String USER_NAME = "carl";

    public TestClient() {
        testClient = new SocketClient("localhost", ClientListener.DEFAULT_PORT);
    }

    public void connectAndLogin() throws IOException {
        if (!SystemState.readyToJoin()) {
            logger.warn("not ready to join");
        }
        testClient.connect();
        listener = testClient.startListen();

        sendRequest(UserSystem.buildLoginRequest(USER_NAME, "34"));
    }

    public void sendRequest(Request request) {
        testClient.writePacket(SceneServerBusConnector.encodeRequest(request).getData());
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
