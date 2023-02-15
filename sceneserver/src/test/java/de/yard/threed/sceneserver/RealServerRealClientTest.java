package de.yard.threed.sceneserver;

import de.yard.threed.core.Event;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.ClientSystem;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.maze.BotSystem;
import de.yard.threed.maze.BulletSystem;
import de.yard.threed.maze.MazeMovingAndStateSystem;
import de.yard.threed.maze.MazeVisualizationSystem;
import de.yard.threed.sceneserver.testutils.RealServer;
import de.yard.threed.sceneserver.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.engine.ecs.UserSystem.USER_EVENT_JOINED;
import static de.yard.threed.engine.ecs.UserSystem.USER_EVENT_LOGGEDIN;
import static de.yard.threed.maze.EventRegistry.EVENT_MAZE_LOADED;
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
        RealServer.stopRealServer(serverProcess);
        serverProcess = null;
    }

    @Test
    public void testSokobanWikipedia() throws Exception {
        setup("skbn/SokobanWikipedia.txt");
        log.debug("testSokobanWikipedia");

        HashMap<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("server", "localhost");
        SceneRunnerForTesting sceneRunner = de.yard.threed.maze.testutils.TestUtils.buildSceneRunnerForMazeScene("skbn/SokobanWikipedia.txt", additionalProperties, 10);
        LoggingSystemTracker systemTracker = sceneRunner.systemTracker;

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
        eventlist = TestHelper.filterEventList(eventlist, (e) -> e.getType().getType() != BaseEventRegistry.EVENT_ENTITYSTATE.getType());
        // should have MAZE_LOADED, LOGIN and JOINED
        assertEquals(3, eventlist.size());
        assertEquals(EVENT_MAZE_LOADED.getType(), eventlist.get(0).getType().getType());
        assertEquals(USER_EVENT_LOGGEDIN.getType(), eventlist.get(1).getType().getType());
        assertEquals(USER_EVENT_JOINED.getType(), eventlist.get(2).getType().getType());

        // Entity change events should be complete. The total number might vary.
        TestUtils.assertAllEventEntityState(EcsTestHelper.toEventList(systemTracker.getPacketsReceivedFromNetwork()));


        assertEquals(2 + 1, SystemManager.findEntities((EntityFilter) null).size(),
                "number of entites (2 boxes + player)");

        // only a login request should have been sent
        assertEquals(1, systemTracker.getPacketsSentToNetwork().size());

    }
}
