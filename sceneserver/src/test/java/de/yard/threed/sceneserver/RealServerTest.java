package de.yard.threed.sceneserver;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.EventRegistry;
import de.yard.threed.sceneserver.testutils.TestClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static de.yard.threed.sceneserver.testutils.RealServer.startRealServer;
import static de.yard.threed.sceneserver.testutils.RealServer.stopRealServer;
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

    boolean startServerFromHere = true;

    public void setup(String gridname) throws Exception {
        if (startServerFromHere) {

            serverProcess = startRealServer(gridname);
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
        stopRealServer(serverProcess);
        serverProcess = null;
    }

    @Test
    public void testMultiUser() throws Exception {
        //Use a deterministic grid without bot/monster automovement
        setup("maze/Area15x10.txt");
        log.debug("testMultiUser");

        // state is needed by test routines
        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient0 = new TestClient(TestClient.USER_NAME0);
        testClient0.assertConnectAndLogin(null);

        TestClient testClient1 = new TestClient(TestClient.USER_NAME1);
        testClient1.assertConnectAndLogin(null);

        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client
        testClient0.assertEventMazeLoaded("maze/Area15x10.txt");
        testClient1.assertEventMazeLoaded("maze/Area15x10.txt");

        testClient0.disconnect();
        // testClient1 should get connection closed event. But for now the server socket isn't closed.
        //Event evt = testClient1.waitForEvent(DefaultBusConnector.EVENT_CONNECTION_CLOSED);
        //assertNotNull(evt);
    }
}
