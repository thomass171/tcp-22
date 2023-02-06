package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class InputToRequestSystemTest {

    SceneRunnerForTesting sceneRunner;

    @BeforeEach
    public void setup() {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                SystemManager.reset();
                InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
                inputToRequestSystem.addKeyMapping(KeyCode.KEY_K, RequestType.register(1016, "requestForKeyK"));
                SystemManager.addSystem(inputToRequestSystem);

            }
        };

        TestFactory.initPlatformForTest(new String[]{"engine", "engine", "data"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod,
                Configuration.buildDefaultConfigurationWithEnv(new HashMap<>()));

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
        assertEquals(0, SimpleHeadlessPlatform.mockedKeyInput.size(), "mockedKeyInput ");
        assertEquals(0, SystemManager.getRequestCount(), "requests ");

        String clientId = "677";
        int userEntitId = 343;
        SystemManager.sendEvent(UserSystem.buildLoggedinEvent("u0", clientId, userEntitId));
        SimpleHeadlessPlatform.mockedKeyInput.add(KeyCode.KEY_K);
        // process login
        EcsTestHelper.processSeconds(2);
        assertEquals(1, SystemManager.getRequestCount(), "requests ");

        Request request = SystemManager.getRequest(0);
        assertEquals("requestForKeyK", request.getType().getLabel());
    }
}
