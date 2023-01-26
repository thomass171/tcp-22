package de.yard.threed.sceneserver;

import de.yard.threed.core.Point;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.maze.GridOrientation;
import de.yard.threed.maze.MazeUtils;
import de.yard.threed.maze.MoverComponent;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestContext;
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
TestContext testContext;

    @BeforeEach
    public void setup() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        //Use a deterministic grid without bot/monster automovement
        //properties.put("argv.initialMaze", "maze/Maze-P-Simple.txt");
        properties.put("argv.initialMaze", "skbn/SokobanWikipedia.txt");
        sceneServer = TestUtils.setupServerForScene("de.yard.threed.maze.MazeScene", INITIAL_FRAMES, properties,20);

    }

    @Test
    public void testLaunch() throws Exception {
        log.debug("testLaunch");
        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        // no user/avatar and graph yet. Only 2 diamonds. Bots are currently launched after after user joined.
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(2, entities.size(), "number of entities");

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        testContext=new TestContext(sceneServer,new TestClient());
        TestUtils.assertConnectAndLogin(sceneServer.getSceneRunner(), testContext.testClient);

        entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(1 + 2, entities.size(), "number of entites (player+2 boxes)");
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        //EcsEntity botEntity = SystemManager.findEntities(e -> "Bot0".equals(e.getName())).get(0);
        //assertNotNull(botEntity, "bot entity");

        MoverComponent mc = MoverComponent.getMoverComponent(userEntity);
        assertEquals(new GridOrientation().toString(), MazeUtils.getPlayerorientation(userEntity).toString(), "initial orientation");
        assertEquals(new Point(6, 1).toString(), MazeUtils.getMoverposition(userEntity).toString(), "initial location");

        testContext.sendRequestAndWait(new Request(TRIGGER_REQUEST_TURNRIGHT, userEntity.getId()));
        testContext.sendRequestAndWait(new Request(TRIGGER_REQUEST_FORWARD, userEntity.getId()));
        testContext.sendRequestAndWait(new Request(TRIGGER_REQUEST_TURNLEFT, userEntity.getId()));
        testContext.sendRequestAndWait(new Request(TRIGGER_REQUEST_FORWARD, userEntity.getId()));

        // 30 is not sufficient
        TestUtils.runAdditionalFrames(sceneServer.getSceneRunner(), 50);

        TestUtil.assertPoint("player location", new Point(7, 2), mc.getLocation());


    }

}
