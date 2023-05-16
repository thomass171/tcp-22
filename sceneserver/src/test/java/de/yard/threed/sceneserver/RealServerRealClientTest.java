package de.yard.threed.sceneserver;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.ClientSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javanative.JavaUtil;
import de.yard.threed.javanative.JsonUtil;
import de.yard.threed.maze.BotSystem;
import de.yard.threed.maze.BulletSystem;
import de.yard.threed.maze.MazeMovingAndStateSystem;
import de.yard.threed.maze.MazeVisualizationSystem;
import de.yard.threed.maze.testutils.MazeTestUtils;
import de.yard.threed.sceneserver.jsonmodel.Status;
import de.yard.threed.sceneserver.testutils.RealServer;
import de.yard.threed.sceneserver.testutils.SceneServerTestUtils;
import de.yard.threed.sceneserver.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.engine.BaseEventRegistry.USER_EVENT_JOINED;
import static de.yard.threed.engine.ecs.UserSystem.USER_EVENT_LOGGEDIN;
import static de.yard.threed.maze.MazeEventRegistry.EVENT_MAZE_LOADED;
import static de.yard.threed.maze.MazeEventRegistry.EVENT_MAZE_VISUALIZED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests with a real standalone scene server. Either with one started outside or started/stopped from here.
 * And a real client scene.
 */
@Slf4j
public class RealServerRealClientTest {

    Process serverProcess = null;

    public void setup(String gridname) throws Exception {
        serverProcess = RealServer.startRealServer(gridname);
    }

    @AfterEach
    public void tearDown() {
        JavaUtil.stopProcess(serverProcess);
        serverProcess = null;
    }

    @Test
    public void testSokobanWikipedia() throws Exception {
        setup("skbn/SokobanWikipedia.txt");
        log.debug("testSokobanWikipedia");

        HashMap<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("server", "localhost");
        SceneRunnerForTesting sceneRunner = MazeTestUtils.buildSceneRunnerForMazeScene("skbn/SokobanWikipedia.txt", additionalProperties, 10);
        LoggingSystemTracker systemTracker = sceneRunner.getSystemTracker();

        // a client mode scene has a system state? For sending login request?.
        //??assertTrue(SystemState.readyToJoin());


        assertNotNull(sceneRunner.getBusConnector());

        assertNull(SystemManager.findSystem(UserSystem.TAG));
        assertNull(SystemManager.findSystem(BulletSystem.TAG));
        assertNull(SystemManager.findSystem(BotSystem.TAG));
        assertNull(SystemManager.findSystem(MazeMovingAndStateSystem.TAG));
        assertNull(SystemManager.findSystem(AvatarSystem.TAG));

        assertNotNull(SystemManager.findSystem(ClientSystem.TAG));

        MazeVisualizationSystem mazeVisualizationSystem = ((MazeVisualizationSystem) SystemManager.findSystem(MazeVisualizationSystem.TAG));
        // As of 2022 no longer gridteleporter as default
        assertNull(mazeVisualizationSystem.gridTeleporter);

        // Login request has already been sent
        Thread.sleep(1000);
        sceneRunner.runLimitedFrames(5);
        //systemTracker.

        List<Event> eventlist = EcsTestHelper.toEventList(systemTracker.getPacketsReceivedFromNetwork());
        eventlist = EcsTestHelper.filterEventList(eventlist, (e) -> e.getType().getType() != BaseEventRegistry.EVENT_ENTITYSTATE.getType());
        // should have MAZE_LOADED, LOGIN JOINED and ASSEMBLED from network
        assertEquals(4, eventlist.size());
        assertEquals(EVENT_MAZE_LOADED.getType(), eventlist.get(0).getType().getType());
        assertEquals(USER_EVENT_LOGGEDIN.getType(), eventlist.get(1).getType().getType());
        assertEquals(USER_EVENT_JOINED.getType(), eventlist.get(2).getType().getType());
        assertEquals(BaseEventRegistry.EVENT_USER_ASSEMBLED.getType(), eventlist.get(3).getType().getType());

        // As result of EVENT_MAZE_LOADED we should have EVENT_MAZE_VISUALIZED locally
        assertEquals(1, EcsTestHelper.filterEventList(((SimpleEventBusForTesting) Platform.getInstance().getEventBus()).getEventHistory(), (e) -> e.getType().equals(EVENT_MAZE_VISUALIZED)).size(), "EVENT_MAZE_VISUALIZED");

        // Entity change events should be complete. The total number might vary.
        SceneServerTestUtils.assertAllEventEntityState(EcsTestHelper.toEventList(systemTracker.getPacketsReceivedFromNetwork()));

        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(2 + 1, entities.size(),
                "number of entites (2 boxes + player)");
        // first two entities are boxes. Nevertheless make sure its really the user entity.
        EcsEntity userEntity = entities.get(2);
        //TODO assertEquals(Ubuildername);

        // not only the scene node wrapper should exist but also the avatar one node below.
        // But there should be two children? avtar and some menu or observer? Hmm?
        assertNotNull(userEntity.getSceneNode());
        assertEquals(2, userEntity.getSceneNode().getTransform().getChildCount());
        log.debug("dump:{}", userEntity.getSceneNode().dump(" ", 1));

        // only a login request and a EVENT_MAZE_VISUALIZED should have been sent
        //TODO assertEquals(2, systemTracker.getPacketsSentToNetwork().size(), "packets sent to network");

        String response = TestUtils.httpGet("http://localhost:5891/status");
        log.debug("response={}", response);
        Status status = JsonUtil.fromJson(response, Status.class);
        assertNotNull(status);
        assertEquals(1, status.getClients().size());

    }
}
