package de.yard.threed.sceneserver;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.EventRegistry;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.maze.MoverComponent;
import de.yard.threed.sceneserver.testutils.PlatformSceneServerFactoryForTesting;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.maze.RequestRegistry.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests with a real standalone scene server. Either with one started outside or started/stopped from here.
 * Needs more than the test utils used in other tests (eg. a client SystemManager)? Probably it needs a kind of full client scene?
 * Really? At least some event/request registrations might be missing.
 */
@Slf4j
public class RealServerTest {

    Process serverProcess = null;

    public void setup(String gridname) throws Exception {

        boolean startServerFromHere = true;


        HashMap<String, String> properties = new HashMap<String, String>();
        //Use a deterministic grid without bot/monster automovement
        //properties.put("argv.initialMaze", "maze/Maze-P-Simple.txt");
        properties.put("initialMaze", gridname);

        if (startServerFromHere) {
            List<String> args = new ArrayList<>();
            args.add("--throttle=100");
            args.add("--initialMaze=" + gridname);
            args.add("de.yard.threed.maze.MazeScene");
            serverProcess = TestUtils.execJavaProcess(de.yard.threed.sceneserver.Main.class, new ArrayList(), args);
        }

        // the client also needs a platform (headless or homebrew?,but not scene server)
        Platform platform = TestFactory.initPlatformForTest(new SimpleHeadlessPlatformFactory(), null);
        //de.yard.threed.engine.testutil.TestFactory.initPlatformForTest( bundles, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()),properties);
        //de.yard.threed.engine.testutil.TestFactory.initPlatformForTest( bundles, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()),properties);

        // or some other scene runner?
        //SceneRunnerForTesting sceneRunner =  (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        //int initialFrames = 10;
        //sceneRunner.runLimitedFrames(initialFrames);

        // MAke sure to register all needed events/requests, even with some components  in an outside server
        BaseEventRegistry baseEventRegistry = new BaseEventRegistry();
        EventRegistry eentRegistry = new EventRegistry();

    }

    @AfterEach
    public void tearDown() {

        if (serverProcess != null) {
            serverProcess.destroy();
            //TODO wait
        }
        serverProcess = null;
    }

    @Test
    public void testMultiUser() throws Exception {
        setup("maze/Area15x10.txt");
        log.debug("testMultiUser");

        // give server time. TODO check server is ready
        Thread.sleep(2000);
        // state is needed by test routines
        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient0 = new TestClient(TestClient.USER_NAME0);
        testClient0.assertConnectAndLogin(null);

        TestClient testClient1 = new TestClient(TestClient.USER_NAME1);
        testClient1.assertConnectAndLogin(null);

        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client
        testClient0.assertEventMazeLoaded("maze/Area15x10.txt");
        testClient1.assertEventMazeLoaded("maze/Area15x10.txt");

    }
}
