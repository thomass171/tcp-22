package de.yard.threed.sceneserver.testutils;


import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Server;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.ClientBusConnector;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.testutil.EventFilter;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.maze.MazeEventRegistry;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeUtils;
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
    List<Request> allRequests = new ArrayList<>();
    LoggingSystemTracker systemTracker = new LoggingSystemTracker();

    public TestClient(String username) {
        this.username = username;
        //socketClient = new SocketClient("localhost", ClientListener.DEFAULT_PORT);
    }

    public void connectAndLogin() throws IOException {
        connectAndLogin(false);
    }

    public void connectAndLogin(boolean viaWebsocket) {
        // why? connect might just fail. assertTrue(SystemState.readyToJoin());
        NativeSocket socket;
        if (viaWebsocket) {
            socket = WSClient.connectToServer(new Server("localhost", Server.DEFAULT_BASE_PORT + 1));
        } else {
            socket = Platform.getInstance().connectToServer(new Server("localhost"));
        }
        clientBusConnector = new ClientBusConnector(socket);
        sendRequestToServer(UserSystem.buildLoginRequest(username, "34"));
    }

    /**
     * Also for using real server. Then sceneserver will be null.
     */
    public void assertConnectAndLogin(SceneServer sceneServer, boolean viaWebsocket) throws Exception {
        connectAndLogin(viaWebsocket);
        //TestUtils.waitForClientConnected();
        //TestUtils.waitForClientPacketAvailableInServer();

        if (sceneServer != null) {
            SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);
        } else {
            // give server time
            Thread.sleep(2000);
        }
        // Check login succeeded.
        // possible race condition with movements arriving before login/joined event
        List<Packet> packets = readLatestPackets();
        assertTrue(packets.size() > 0, "packets from server found");
        SceneServerTestUtils.assertEventPacket(UserSystem.USER_EVENT_LOGGEDIN, null, packets, 1);

        // join happened implicitly, so Avatar should exist.
        SceneServerTestUtils.assertEventPacket(UserSystem.USER_EVENT_JOINED, null, packets, 1);
    }

    public void assertConnectAndLogin(SceneServer sceneServer) throws Exception {
        assertConnectAndLogin(sceneServer, false);
    }

    public void sendRequestToServer(Request request) {
        //socketClient.writePacket(SceneServerBusConnector.encodeRequest(request).getData());
        clientBusConnector.pushPacket(SceneServerBusConnector.encodeRequest(request),null);
        logger.debug("Sent request " + request);
    }

    /**
     * difficult to make this reliable.
     *
     * @param request
     */
    public void sendRequestAndWait(SceneServer sceneServer, Request request) {
        while (MazeUtils.isAnyMoving() != null) {
            SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 1);
        }

        sendRequestToServer(request);

        do {
            SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 3);
        } while (SystemManager.getRequestCount() > 0);

        while (MazeUtils.isAnyMoving() != null) {
            SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 3);
        }
    }

    public void assertEventPacket(EventType eventType, int expectedFound) {
        List<Packet> packets = getAllPackets();
        assertTrue(packets.size() > 0, "all packets > 0");
        SceneServerTestUtils.assertEventPacket(eventType, null, packets, expectedFound);

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
            systemTracker.packetReceivedFromNetwork(packet);
        }

        for (Packet p : packets) {
            if (DefaultBusConnector.isEvent(p)) {
                Event event = DefaultBusConnector.decodeEvent(p);
                if (event == null) {
                    Assertions.fail("decode failed");
                }
                // Since this is no full client, immediately consider events to be processed.
                systemTracker.eventProcessed(event);
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
        Assertions.assertEquals(systemTracker.getPacketsReceivedFromNetwork().size(), systemTracker.getEventsProcessed().size());
        return systemTracker.getPacketsReceivedFromNetwork();
    }

    public EcsEntity getUserEntity() {
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME0.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        return userEntity;
    }

    public List<Event> findEvents(EventFilter filter) {
        List<Event> result = new ArrayList<Event>();
        for (Event e : systemTracker.getEventsProcessed()) {
            if (filter == null || filter.matches(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public Event waitForEvent(/*SceneServer sceneServer*/ EventType eventType) {

        int counter = 0;
        do {
            List<Event> events = findEvents(e -> e.getType().getType() == eventType.getType());
            if (events.size() > 0) {
                return events.get(0);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (counter++ < 5);
        return null;
    }

    public void assertEventMazeLoaded(String gridName) {
        SceneServerTestUtils.assertEvents(MazeEventRegistry.EVENT_MAZE_LOADED, systemTracker.getEventsProcessed(), 1, p -> {
            assertEquals(gridName, p.get("gridname"));
        });
        //ServerSystem not available with real server
        ServerSystem serverSystem = ((ServerSystem) SystemManager.findSystem(ServerSystem.TAG));
        if (serverSystem != null) {
            assertEquals(1, serverSystem.getSavedEvents().size());
        }
    }

    /**
     * Look for latest event of a specific entity.
     * Use assertLatestEventEntityState instead.
     */
    @Deprecated
    public void assertEventEntityState(int entityId, Point expectedLocation, GridOrientation expectedOrientation) {
        List<Event> entityStateEvents = EcsTestHelper.filterEventList(systemTracker.getEventsProcessed(), e -> {
            return e.getType().getType() == BaseEventRegistry.EVENT_ENTITYSTATE.getType() &&
                    (Integer) e.getPayload().get("entityid") == entityId;
        });

        Event latest = entityStateEvents.get(entityStateEvents.size() - 1);
        //assertEquals(gridName, p.get("gridname"));
        Vector3 position = (Vector3) latest.getPayload().get("position");
        assertNotNull(position, "position");
        TestUtils.assertVector3(MazeUtils.point2Vector3(expectedLocation), position);
        Quaternion rotation = (Quaternion) latest.getPayload().get("rotation");
        assertNotNull(rotation, "rotation");
        // rotation taken as is, but seem
        TestUtils.assertQuaternion(expectedOrientation.getRotation(), rotation, "rotation");
    }

    /**
     * Validate all
     */
    public void assertAllEventEntityState() {
        SceneServerTestUtils.assertAllEventEntityState(systemTracker.getEventsProcessed());
    }

    public void assertLatestEventEntityState(int entityId, Pair<String, String>[] expectedProperties) {
        List<Event> entityStateEvents = EcsTestHelper.filterEventList(systemTracker.getEventsProcessed(), e ->
                e.getType().getType() == BaseEventRegistry.EVENT_ENTITYSTATE.getType() &&
                        (Integer) e.getPayload().get("entityid") == entityId);

        if (entityStateEvents.size() == 0) {
            logger.error("no events found");
        }
        Event latest = entityStateEvents.get(entityStateEvents.size() - 1);
        TestUtils.assertEvent(BaseEventRegistry.EVENT_ENTITYSTATE, expectedProperties, latest, "");
    }

    public List<Integer> getKnownEntitiesFromEventEntityState() {
        List<Integer> result = new ArrayList();
        EcsTestHelper.filterEventList(systemTracker.getEventsProcessed(), e -> {
            if (e.getType().getType() == BaseEventRegistry.EVENT_ENTITYSTATE.getType()) {
                Integer id = (Integer) e.getPayload().get("entityid");
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
            return false;
        });
        return result;
    }

    /**
     * Just close the connection/socket. No logoff etc.
     * But not reliable in any test case.
     */
    public void disconnectByClose() {
        clientBusConnector.close();
    }
}
