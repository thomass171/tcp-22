package de.yard.threed.maze;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


/**
 * <p>
 * Created by thomass on 28.01.22.
 */
public class MazeMovingAndStateSystemTest {

    private WireMockServer wireMockServer;
    private SceneRunnerForTesting sceneRunner;

    /**
     *
     */
    @BeforeEach
    public void setup() {

        SystemState.state = 0;
        MazeDataProvider.reset();
        SystemManager.reset();

        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();

        InitMethod initMethod = () -> {
            SystemManager.addSystem(new MazeMovingAndStateSystem());

        };
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, platformFactory, initMethod, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testSimpleNonVR() throws Exception {

        Observer.buildForDefaultCamera();
        Assertions.assertNotNull(Observer.getInstance(), "observer");

        startSimpleTest();


    }

    @Test
    public void testSimpleVR() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put("enableVR", "true");
        Platform.getInstance().getConfiguration().addConfiguration(new ConfigurationByProperties(properties), true);

        VrInstance.buildFromArguments();
        Assertions.assertNotNull(VrInstance.getInstance(), "VrInstance");

        Observer.buildForDefaultCamera();
        Assertions.assertNotNull(Observer.getInstance(), "observer");

        startSimpleTest();

    }

    @Test
    public void testRemoteGridNotFound() throws Exception {

        MazeTestUtils.mockHttpGetSokobanWikipedia(wireMockServer);

        // shows wiremock log message 'url does not match'
        String url = "http://localhost:" + wireMockServer.port() + "/mazes/99";

        MazeDataProvider.init(url);

        sceneRunner.runLimitedFrames(10,0.1, 100);
    }

    private void startSimpleTest() {

        // what is the grid expected here?
        MazeDataProvider.init();

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);
        //t.b.c.

    }
}
