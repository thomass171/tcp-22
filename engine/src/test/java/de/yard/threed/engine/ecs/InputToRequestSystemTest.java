package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.InitMethod;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import org.junit.Before;
import org.junit.Test;

public class InputToRequestSystemTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    SceneRunnerForTesting sceneRunner;

    @Before
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                SystemManager.reset();
                // No visualization to reveal model-view coupling.
                SystemManager.addSystem(new InputToRequestSystem());

            }
        };

        TestFactory.initPlatformForTest( new String[]{"engine", "engine", "data"}, new PlatformFactoryHeadless(), initMethod);

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
        sceneRunner.runLimitedFrames(3);

    }
    @Test
    public void testDestinationMarker() {

        SceneNode directionMarker = new SceneNode();
        SceneNode localMarker = new SceneNode();

        //GridTeleporter gridTeleporter = new GridTeleporter(localMarker, directionMarker);
    }
}
