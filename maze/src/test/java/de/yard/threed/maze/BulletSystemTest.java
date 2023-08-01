package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.maze.MazeTheme.THEME_TRADITIONAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BulletSystemTest {

    SceneRunnerForTesting sceneRunner;

    @BeforeEach
    public void setup() {

        MazeDataProvider.reset();

        EcsTestHelper.setup(() -> {
            MazeTheme st = MazeTheme.buildFromIdentifier(THEME_TRADITIONAL);

            SystemManager.addSystem(new MazeMovingAndStateSystem(st));
            SystemManager.addSystem(new BulletSystem());
        }, "engine", "maze");

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
    }

    /**
     *
     */
    @Test
    public void testFireRequest() throws Exception {

        MazeDataProvider.init("maze/Maze-P-Simple.txt", null);

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

        // targetDirection is only optional
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(userEntity.getId(),null));
        EcsTestHelper.processSeconds(1);

        // user still on home field.
        List<Event> failEvents = EcsTestHelper.getEventsFromHistory(MazeEventRegistry.EVENT_MAZE_FIREFAILED);
        assertEquals(1, failEvents.size(), "fire fail events");
        Event failEvent = failEvents.get(0);
        assertEquals("fire from home field ignored", failEvent.getPayload().get("message"));

        // move and fire again
        SystemManager.putRequest(new Request(MazeRequestRegistry.TRIGGER_REQUEST_FORWARD,userEntity.getId()));
        EcsTestHelper.processSeconds(1);
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(userEntity.getId(),null));
        EcsTestHelper.processSeconds(1);
        // one bullet is gone
        assertEquals(2, MazeUtils.getBullets(userEntity).size());

    }
}
