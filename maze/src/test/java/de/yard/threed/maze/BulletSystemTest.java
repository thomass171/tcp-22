package de.yard.threed.maze;

import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BulletSystemTest {

    SceneRunnerForTesting sceneRunner;

    @BeforeEach
    public void setup() {

        MazeDataProvider.reset();

        EcsTestHelper.setup(() -> {
            InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
            inputToRequestSystem.addKeyMapping(KeyCode.Space, MazeRequestRegistry.TRIGGER_REQUEST_FIRE);
            SystemManager.addSystem(inputToRequestSystem);
            SystemManager.addSystem(new MazeMovingAndStateSystem());
            SystemManager.addSystem(new BulletSystem());

        }, "engine", "maze");

        MazeSettings.init(MazeSettings.MODE_SOKOBAN);

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
    }

    /**
     *
     */
    @Test
    public void testFireRequest() throws Exception {

        MazeDataProvider.init("maze/Maze-P-Simple.txt");

        // load grid
        EcsTestHelper.processSeconds(2);

        // needs to be logged in (having an entity) maze user for sending requests.
        String clientId = "677";
        String username = "u0";
        EcsEntity userEntity = new EcsEntity(new UserComponent(username));
        userEntity.setName(username);
        userEntity.scenenode = new SceneNode();

        SystemManager.sendEvent(UserSystem.buildLoggedinEvent(username, clientId, userEntity.getId(), null));
        EcsTestHelper.processSeconds(2);

        SystemManager.putRequest(UserSystem.buildJoinRequest(userEntity.getId()));
        EcsTestHelper.processSeconds(2);

        // provide user with bullets
        SystemManager.sendEvent(BaseEventRegistry.buildUserAssembledEvent(userEntity));
        EcsTestHelper.processSeconds(2);

        assertEquals(3, MazeUtils.getBullets(userEntity).size());

        SimpleHeadlessPlatform.mockedKeyInput.add(KeyCode.KEY_SPACE);
        EcsTestHelper.processSeconds(2);

        // user still on home field.
        // TODO t.b.c. check fire reject a.s.o.
    }
}
