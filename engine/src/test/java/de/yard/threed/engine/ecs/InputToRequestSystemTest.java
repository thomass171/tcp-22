package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.InitMethod;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static de.yard.threed.core.testutil.TestUtil.assertNotNull;
import static org.junit.Assert.assertEquals;

public class InputToRequestSystemTest {

    SceneRunnerForTesting sceneRunner;

    @Before
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                SystemManager.reset();
                InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
                inputToRequestSystem.addKeyMapping(KeyCode.KEY_K, new RequestType("requestForKeyK"));
                SystemManager.addSystem(inputToRequestSystem);

            }
        };

        TestFactory.initPlatformForTest(new String[]{"engine", "engine", "data"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod);

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
        sceneRunner.runLimitedFrames(3);

    }

    @Test
    public void testDestinationMarker() {

        SceneNode directionMarker = new SceneNode();
        SceneNode localMarker = new SceneNode();

        //GridTeleporter gridTeleporter = new GridTeleporter(localMarker, directionMarker);
    }

    /**
     * Shouldn't send (key triggered) requests before logged in.
     */
    @Test
    public void testLogin() throws Exception {


        SimpleHeadlessPlatform.mockedKeyInput.add(KeyCode.KEY_K);
        EcsTestHelper.processSeconds(2);

        // key input should be ignored silently
        assertEquals("mockedKeyInput ", 0, SimpleHeadlessPlatform.mockedKeyInput.size());
        assertEquals("requests ", 0, SystemManager.getRequestCount());

        String clientId = "677";
        int userEntitId = 343;
        SystemManager.sendEvent(UserSystem.buildLoggedinEvent("u0", clientId, userEntitId));
        SimpleHeadlessPlatform.mockedKeyInput.add(KeyCode.KEY_K);
        // process login
        EcsTestHelper.processSeconds(2);
        assertEquals("requests ", 1, SystemManager.getRequestCount());

        Request request = SystemManager.getRequest(0);
        assertEquals("requestForKeyK", request.getType().getLabel());
    }
}
