package de.yard.threed.sceneserver;

import de.yard.threed.core.Point;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.maze.EventRegistry;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.maze.MoverComponent;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.maze.RequestRegistry.*;
import static de.yard.threed.maze.RequestRegistry.TRIGGER_REQUEST_FORWARD;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MazeScene.
 */
@Slf4j
public class MazeSceneTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;
   // TestContext testContext;


    public void setup(String gridname) throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        //Use a deterministic grid without bot/monster automovement
        //properties.put("argv.initialMaze", "maze/Maze-P-Simple.txt");
        properties.put("initialMaze", gridname);
        sceneServer = TestUtils.setupServerForScene("de.yard.threed.maze.MazeScene", INITIAL_FRAMES, properties, 20);

    }

    @Test
    public void testLaunchSokobanWikipedia() throws Exception {
        setup("skbn/SokobanWikipedia.txt");
        log.debug("testLaunchSokobanWikipedia");
        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        // no user/avatar and graph yet. Only 2 diamonds. Bots are currently launched after after user joined.
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(2, entities.size(), "number of entities");
        // initial event missed by all clients
        assertEquals(1,((ServerSystem) SystemManager.findSystem(ServerSystem.TAG)).getSavedEvents().size());

        //SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient(TestClient.USER_NAME0);

        testClient.assertConnectAndLogin(sceneServer);
        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client
        testClient.assertEventMazeLoaded("skbn/SokobanWikipedia.txt");

        entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(1 + 2, entities.size(), "number of entites (player+2 boxes)");
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME0.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        //EcsEntity botEntity = SystemManager.findEntities(e -> "Bot0".equals(e.getName())).get(0);
        //assertNotNull(botEntity, "bot entity");


        MoverComponent mc = MoverComponent.getMoverComponent(userEntity);
        assertEquals(new GridOrientation().toString(), MazeUtils.getPlayerorientation(userEntity).toString(), "initial orientation");
        assertEquals(new Point(6, 1).toString(), MazeUtils.getMoverposition(userEntity).toString(), "initial location");

        testClient.sendRequestAndWait(sceneServer,new Request(TRIGGER_REQUEST_TURNRIGHT, userEntity.getId()));
        testClient.sendRequestAndWait(sceneServer,new Request(TRIGGER_REQUEST_FORWARD, userEntity.getId()));
        testClient.sendRequestAndWait(sceneServer,new Request(TRIGGER_REQUEST_TURNLEFT, userEntity.getId()));
        testClient.sendRequestAndWait(sceneServer,new Request(TRIGGER_REQUEST_FORWARD, userEntity.getId()));

        // 30 is not sufficient
        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 50);

        TestUtil.assertPoint("player location", new Point(7, 2), mc.getLocation());


    }

    /**
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
        setup("maze/Area15x10.txt");
        log.debug("testMultiUser");

        //SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient0 = new TestClient(TestClient.USER_NAME0);
        testClient0.assertConnectAndLogin(sceneServer);

        TestClient testClient1 = new TestClient(TestClient.USER_NAME1);
        testClient1.assertConnectAndLogin(sceneServer);

        // EVENT_MAZE_LOADED should have been resent after login, but only to the new client
        testClient0.assertEventMazeLoaded("maze/Area15x10.txt");
        testClient1.assertEventMazeLoaded("maze/Area15x10.txt");

        testClient0.assertEventEntityState(testClient0.getUserEntity().getId(), new Point(6, 4), GridOrientation.fromDirection("N"));
    }
}
