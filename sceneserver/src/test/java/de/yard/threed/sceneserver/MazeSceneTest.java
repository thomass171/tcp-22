package de.yard.threed.sceneserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.Event;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Point;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.maze.GridMovement;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeModelFactory;
import de.yard.threed.maze.MazeRequestRegistry;
import de.yard.threed.maze.MoverComponent;
import de.yard.threed.maze.testutils.MazeTestUtils;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.SceneServerTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.yard.threed.engine.BaseRequestRegistry.*;
import static de.yard.threed.maze.MazeRequestRegistry.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for MazeScene in a scene server in current JVM with a simple test client.
 */
@Slf4j
public class MazeSceneTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;
    // TestContext testContext;
    LoggingSystemTracker systemManagerTracker;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
    }

    private void init(String gridname) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("initialMaze", gridname);
        // Loading a grid from wiremock really takes some time. So renderbrake 20->40
        sceneServer = SceneServerTestUtils.setupServerForScene("de.yard.threed.maze.MazeScene", INITIAL_FRAMES, properties, 40);
        systemManagerTracker = new LoggingSystemTracker();
        SystemManager.setSystemTracker(systemManagerTracker);
    }

    @AfterEach
    public void tearDown() {
        sceneServer.stopServer();
        wireMockServer.stop();
    }

    @Test
    public void testLaunchSokobanWikipedia() throws Exception {
        runLaunchSokobanWikipedia(false, false);
    }

    @Test
    public void testLaunchSokobanWikipediaViaWebSocket() throws Exception {
        runLaunchSokobanWikipedia(true, false);
    }

    @Test
    public void testLaunchRemoteSokobanWikipedia() throws Exception {
        MazeTestUtils.mockHttpGetSokobanWikipedia(wireMockServer);
        runLaunchSokobanWikipedia(false, true);
    }

    @Test
    public void testLaunchRemoteSokobanWikipediaViaWebSocket() throws Exception {
        MazeTestUtils.mockHttpGetSokobanWikipedia(wireMockServer);
        runLaunchSokobanWikipedia(true, true);
    }

    public void runLaunchSokobanWikipedia(boolean viaWebSocket, boolean remoteGrid) throws Exception {
        init(remoteGrid ? "http://localhost:" + wireMockServer.port() + "/mazes/1" : "skbn/SokobanWikipedia.txt");
        String expectedGridname = remoteGrid ? "Sokoban Wikipedia" : "skbn/SokobanWikipedia.txt";

        log.debug("testLaunchSokobanWikipedia");
        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        // no user/avatar and graph yet. Only 2 diamonds. Bots are currently launched after after user joined.
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(2, entities.size(), "number of entities");
        // initial event missed by all clients
        assertEquals(1, ((ServerSystem) SystemManager.findSystem(ServerSystem.TAG)).getSavedEvents().size());

        //SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient(TestClient.USER_NAME0);

        EcsEntity userEntity = connectToSokobanWikipediaServer(testClient, viaWebSocket, expectedGridname);
        MoverComponent mc = MoverComponent.getMoverComponent(userEntity);

        testClient.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_TURNRIGHT, userEntity.getId()));
        testClient.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_FORWARD, userEntity.getId()));
        testClient.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_TURNLEFT, userEntity.getId()));
        testClient.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_FORWARD, userEntity.getId()));

        // 30 is not sufficient
        SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 50);

        TestUtils.assertPoint(new Point(7, 2), mc.getLocation(), "player location");

        systemManagerTracker.tag();
        SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);
        // entity change events go to network directly. So there shouldn't have been any event.
        assertEquals(0, systemManagerTracker.getLatestEventsProcessed().size());

        // first two should be boxes
        List<Integer> knownEntityIds = testClient.getKnownEntitiesFromEventEntityState();
        assertEquals(3, knownEntityIds.size());
        testClient.assertLatestEventEntityState(knownEntityIds.get(0), new Pair[]{
                new Pair("buildername", MazeModelFactory.BOX_BUILDER)
        });

        testClient.assertAllEventEntityState();

        String connectionId = UserComponent.getUserComponent(userEntity).getConnectionId();
        testClient.disconnectByClose();
        // should publish connection closed event.
        SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 5);
        assertEquals(1, systemManagerTracker.getLatestEventsProcessed().size());
        SceneServerTestUtils.assertEvents(BaseEventRegistry.EVENT_CONNECTION_CLOSED, systemManagerTracker.getLatestEventsProcessed(), 1, p -> {
            assertEquals(connectionId, p.get("connectionid"));
        });
        assertEquals(0, SystemManager.findEntities(e -> UserComponent.getUserComponent(e) != null).size());

        // connect again. Should again be located on start position.
        userEntity = connectToSokobanWikipediaServer(testClient, viaWebSocket, expectedGridname);
        mc = MoverComponent.getMoverComponent(userEntity);
        TestUtils.assertPoint(new Point(6, 1), mc.getLocation(), "player location");

    }

    /**
     * Use a deterministic grid without bot/monster automovement
     * <p>
     * ###############
     * #     @       #
     * #  #        # #
     * #  ## D # D## #
     * #  #   ###  # #
     * #     @ #     #
     * #    #D ####  #
     * #   ###   #D  #
     * #             #
     * ###############
     */
    @Test
    public void testMultiUser() throws Exception {
        init("maze/Area15x10.txt");
        log.debug("testMultiUser");

        //SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient0 = new TestClient(TestClient.USER_NAME0);
        testClient0.assertConnectAndLogin(sceneServer);

        TestClient testClient1 = new TestClient(TestClient.USER_NAME1);
        testClient1.assertConnectAndLogin(sceneServer);

        // client0 should be informed about entity of second. Count of 4 entity state events seems plausibel.
        List<Event> evs = testClient0.getAllEventsEntityState(testClient1.userEntityId);
        assertEquals(4, evs.size());
        List<Event> entityEventsWithBuilderName = EcsTestHelper.filterEventList(evs, (e) -> !StringUtils.isBlank((String) e.getPayload().get("buildername")));
        assertEquals(3, entityEventsWithBuilderName.size());

        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client. TODO check: is that really tested here?
        testClient0.assertEventMazeLoaded("maze/Area15x10.txt");
        testClient1.assertEventMazeLoaded("maze/Area15x10.txt");

        testClient0.assertEventEntityState(testClient0.getUserEntity().getId(), new Point(6, 4), GridOrientation.fromDirection("N"));

        // first two known entities should be diamonds
        List<Integer> knownEntityIds = testClient1.getKnownEntitiesFromEventEntityState();
        // 2 player (3 bullets each) + 4 diamonds
        assertEquals(12, knownEntityIds.size());
        testClient1.assertLatestEventEntityState(knownEntityIds.get(0), new Pair[]{
                new Pair("buildername", MazeModelFactory.DIAMOND_BUILDER)
        });
        List<EcsEntity> entities = SystemManager.findEntities(null);
        // In general all entities should have a builder name (at least in latest events)
        for (int entityId : knownEntityIds) {
            testClient1.assertLatestEventEntityState(entityId, e -> {
                assertFalse(StringUtils.isBlank((String) e.getPayload().get("buildername")), "entity event:" + e);
                return null;
            });
        }
        // TODO test bullets in inventory

        // testclient0 is on (6, 4) facing "N".
        testClient0.assertPositionAndOrientation(new Point(6, 4),"N");
        testClient1.assertPositionAndOrientation(new Point(6, 8),"E");

        // move both players to make firing/hitting possible.
        testClient0.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_FORWARD, testClient0.getUserEntity().getId()));
        testClient0.assertPositionAndOrientation(new Point(6, 5),"N");
        testClient1.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_TURNRIGHT, testClient1.getUserEntity().getId()));
        testClient1.assertPositionAndOrientation(new Point(6, 8),"S");
        testClient1.sendRequestAndWait(sceneServer, new Request(TRIGGER_REQUEST_FORWARD, testClient1.getUserEntity().getId()));
        testClient1.assertPositionAndOrientation(new Point(6, 7),"S");

        testClient0.sendRequestAndWait(sceneServer, MazeRequestRegistry.buildFireRequest(testClient0.getUserEntity().getId(),
                MoverComponent.getMoverComponent(testClient0.getUserEntity()).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        // wait for hitting client1 and check relocate of bot to home position
        SceneServerTestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 50);
        testClient1.assertPositionAndOrientation(new Point(6, 8),"S");
    }

    /**
     * A monster is a bot player.
     * ##########
     * #   M    #
     * #  D# #  #
     * #   # #  #
     * #    P D #
     * ##########
     */
    @Test
    public void testSingleUserWithMonster() throws Exception {

        init("maze/Maze-P-Simple.txt");
        log.debug("testSingleUserWithMonster");

        TestClient testClient0 = new TestClient(TestClient.USER_NAME0);
        testClient0.assertConnectAndLogin(sceneServer);

        EcsEntity botEntity = SystemManager.findEntities(e -> "Bot0".equals(e.getName())).get(0);
        assertNotNull(botEntity, "bot entity");

    }

    private EcsEntity connectToSokobanWikipediaServer(TestClient testClient, boolean viaWebSocket, String expectedGridname) throws Exception {

        testClient.assertConnectAndLogin(sceneServer, viaWebSocket);
        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client
        testClient.assertEventMazeLoaded(expectedGridname);

        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(1 + 2, entities.size(), "number of entites (player+2 boxes)");
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME0.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");

        testClient.assertPositionAndOrientation(new Point(6, 1),new GridOrientation());
        UserComponent userComponent = UserComponent.getUserComponent(userEntity);
        assertNotNull(userComponent, "userComponent");
        String connectionId = userComponent.getConnectionId();
        assertNotNull(connectionId, "connectionId");
        return userEntity;
    }
}
