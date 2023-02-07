package de.yard.threed.sceneserver.testutils;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.ecs.ClientBusConnector;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.testutil.EventFilter;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketClient;
import de.yard.threed.maze.EventRegistry;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.sceneserver.ClientListener;
import de.yard.threed.sceneserver.SceneServer;
import de.yard.threed.sceneserver.SceneServerBusConnector;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TestClient {
    Log logger = Platform.getInstance().getLog(TestClient.class);
    //SocketClient socketClient;
    //QueuingSocketListener listener;
    ClientBusConnector clientBusConnector;
    public static String USER_NAME0 = "carl";
    public static String USER_NAME1 = "wayne";
    public String username;
    List<Packet> allPackets = new ArrayList<>();
    List<Event> allEvents = new ArrayList<>();
    List<Request> allRequests = new ArrayList<>();

    public TestClient(String username) {
        this.username = username;
        //socketClient = new SocketClient("localhost", ClientListener.DEFAULT_PORT);
    }

    public void connectAndLogin() throws IOException {
        assertTrue(SystemState.readyToJoin());
        NativeSocket socket = Platform.getInstance().connectToServer("localhost", DefaultBusConnector.DEFAULT_PORT);
        clientBusConnector = new ClientBusConnector(socket);
        //socketClient.connect();
        //listener = socketClient.startListen();
        sendRequestToServer(UserSystem.buildLoginRequest(username, "34"));
    }

    /**
     * Also for using real server. Then sceneserver will be null.
     */
    public void assertConnectAndLogin(SceneServer sceneServer) throws Exception {
        connectAndLogin();
        //TestUtils.waitForClientConnected();
        //TestUtils.waitForClientPacketAvailableInServer();

        if (sceneServer != null) {
            TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);
        } else {
            // give server time
            Thread.sleep(2000);
        }
        // Check login succeeded.
        // possible race condition with movements arriving before login/joined event
        List<Packet> packets = readLatestPackets();
        assertTrue(packets.size() > 0, "packets from server found");
        TestUtils.assertEventPacket(UserSystem.USER_EVENT_LOGGEDIN, null, packets, 1);

        // join happened implicitly, so Avatar should exist.
        TestUtils.assertEventPacket(UserSystem.USER_EVENT_JOINED, null, packets, 1);
    }

    public void sendRequestToServer(Request request) {
        //socketClient.writePacket(SceneServerBusConnector.encodeRequest(request).getData());
        clientBusConnector.getSockets(null).get(0).sendPacket(SceneServerBusConnector.encodeRequest(request));
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
        //List<String> packet = listener.getPacket();
        Packet packet = clientBusConnector.getPacket();
        /*if (packet == null) {
            return null;
        }
        return Packet.buildFromBlock(packet);*/
        return packet;
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

    public EcsEntity getUserEntity() {
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME0.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        return userEntity;
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
        //ServerSystem not available with real server
        ServerSystem serverSystem = ((ServerSystem) SystemManager.findSystem(ServerSystem.TAG));
        if (serverSystem != null) {
            assertEquals(1, serverSystem.getSavedEvents().size());
        }
    }

    /**
     * Look for latest event of a specific entity
     */
    public void assertEventEntityState(int entityId, Point expectedLocation, GridOrientation expectedOrientation) {
        List<Event> entityStateEvents = TestHelper.filterEventList(allEvents, e -> {
            return e.getType().getType() == DefaultBusConnector.EVENT_ENTITYSTATE.getType() &&
                    (Integer) e.getPayload().get("entityid") == entityId;
        });

        Event latest = entityStateEvents.get(entityStateEvents.size() - 1);
        //assertEquals(gridName, p.get("gridname"));
        Vector3 position = (Vector3) latest.getPayload().get("position");
        assertNotNull(position, "position");
        TestUtil.assertVector3(MazeUtils.point2Vector3(expectedLocation), position);
        Quaternion rotation = (Quaternion) latest.getPayload().get("rotation");
        assertNotNull(rotation, "rotation");
        // rotation taken as is, but seem
        TestUtil.assertQuaternion("rotation", expectedOrientation.getRotation(), rotation);
    }
}
