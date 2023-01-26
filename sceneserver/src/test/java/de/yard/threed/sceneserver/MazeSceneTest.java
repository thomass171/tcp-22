package de.yard.threed.sceneserver;

import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import de.yard.threed.sceneserver.testutils.TestClient;
import de.yard.threed.sceneserver.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.engine.BaseEventRegistry.BASE_EVENT_ENTITY_CHANGE;
import static de.yard.threed.sceneserver.testutils.TestUtils.waitForClientConnected;
import static de.yard.threed.sceneserver.testutils.TestUtils.waitForClientPacket;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MazeScene.
 */
@Slf4j
public class MazeSceneTest {

    static final int INITIAL_FRAMES = 10;

    SceneServer sceneServer;

    @BeforeEach
    public void setup() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("argv.initialMaze", "maze/Maze-P-Simple.txt");
        sceneServer = TestUtils.setupServerForScene("de.yard.threed.maze.MazeScene", INITIAL_FRAMES, properties);
    }

    @Test
    public void testLaunch() throws Exception {
        log.debug("testLaunch");
        assertEquals(INITIAL_FRAMES, sceneServer.getSceneRunner().getFrameCount());
        // no user/avatar and graph yet. Only 2 diamonds. Bots are currently launched after after user joined.
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(2, entities.size(), "number of entities");

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        TestClient testClient = new TestClient();
        TestUtils.assertConnectAndLogin(sceneServer.getSceneRunner(), testClient);

        entities = SystemManager.findEntities((EntityFilter) null);
        assertEquals(2 + 2 * (1+3), entities.size(), "number of entites (diamonds+player+bot+bullets each)");
        EcsEntity userEntity = SystemManager.findEntities(e -> TestClient.USER_NAME.equals(e.getName())).get(0);
        assertNotNull(userEntity, "user entity");
        EcsEntity botEntity = SystemManager.findEntities(e -> "Bot0".equals(e.getName())).get(0);
        assertNotNull(botEntity, "bot entity");

    }

}
