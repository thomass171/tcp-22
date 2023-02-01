package de.yard.threed.sceneserver.testutils;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.testutil.EventFilter;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.PayloadHook;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketClient;
import de.yard.threed.maze.EventRegistry;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServer;
import de.yard.threed.sceneserver.SceneServerBusConnector;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestClient {
    Log logger = Platform.getInstance().getLog(TestClient.class);
    SocketClient socketClient;
    QueuingSocketListener listener;
    public static String USER_NAME0 = "carl";
    public static String USER_NAME1 = "carl";
    public String username;
    List<Packet> allPackets = new ArrayList<>();
    List<Event> allEvents = new ArrayList<>();
    List<Request> allRequests = new ArrayList<>();

    public TestClient(String username) {
        this.username = "carl";
        socketClient = new SocketClient("localhost", ClientListener.DEFAULT_PORT);
    }

    public void connectAndLogin() throws IOException {
        assertTrue(SystemState.readyToJoin());
        socketClient.connect();
        listener = socketClient.startListen();

        sendRequestToServer(UserSystem.buildLoginRequest(username, "34"));
    }

    public void assertConnectAndLogin(SceneServer sceneServer) throws Exception {
        connectAndLogin();
        //TestUtils.waitForClientConnected();
        //TestUtils.waitForClientPacketAvailableInServer();

        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);

        // Check login succeeded.
        // possible race condition with movements arriving before login/joined event
        List<Packet> packets = readLatestPackets();
        assertTrue(packets.size() > 0);
        TestUtils.assertEventPacket(UserSystem.USER_EVENT_LOGGEDIN, null, packets, 1);

        // join happened implicitly, so Avatar should exist.
        TestUtils.assertEventPacket(UserSystem.USER_EVENT_JOINED, null, packets, 1);
    }

    public void sendRequestToServer(Request request) {
        socketClient.writePacket(SceneServerBusConnector.encodeRequest(request).getData());
        logger.debug("Sent request " + request);
    }

    /**
     * difficult to make this reliable.
     *
     * @param request
     */
    public void sendRequestAndWait(SceneServer sceneServer, Request request) {
        while (MazeUtils.isAnyMoving() != null) {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 1);
        }

        sendRequestToServer(request);

        do {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 3);
        } while (SystemManager.getRequestCount() > 0);

        while (MazeUtils.isAnyMoving() != null) {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 3);
        }
    }

    public void assertEventPacket(EventType eventType, int expectedFound) {
        List<Packet> packets = getAllPackets();
        assertTrue(packets.size() > 0, "all packets > 0");
        TestUtils.assertEventPacket(eventType, null, packets, expectedFound);

    }

    public Packet getPacket() {
        List<String> packet = listener.getPacket();
        if (packet == null) {
            return null;
        }
        return Packet.buildFromBlock(packet);
    }

    public List<Packet> readLatestPackets() {
        List<Packet> packets = new ArrayList<>();
        Packet packet;
        while ((packet = getPacket()) != null) {
            packets.add(packet);
        }
        allPackets.addAll(packets);

        for (Packet p : packets) {
            if (DefaultBusConnector.isEvent(p)) {
                Event event = DefaultBusConnector.decodeEvent(p);
                if (event == null) {
                    Assertions.fail("decode failed");
                }
                allEvents.add(event);
            } else {
                Request request = DefaultBusConnector.decodeRequest(p);
                if (request == null) {
                    Assertions.fail("decode failed");
                }
                allRequests.add(request);
            }
        }
        return packets;
    }

    public List<Packet> getAllPackets() {
        readLatestPackets();
        Assertions.assertEquals(allPackets.size(), allEvents.size());
        return allPackets;
    }

    public List<Event> findEvents(EventFilter filter) {
        List<Event> result = new ArrayList<Event>();
        for (Event e : allEvents) {
            if (filter == null || filter.matches(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public void assertEventMazeLoaded(String gridName) {
        TestUtils.assertEvent(EventRegistry.EVENT_MAZE_LOADED, allEvents, 1, p -> {
            assertEquals(gridName, p.get("gridname"));
        });

        assertEquals(1, ((ServerSystem) SystemManager.findSystem(ServerSystem.TAG)).getSavedEvents().size());

    }
}
