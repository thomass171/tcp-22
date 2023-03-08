package de.yard.threed.maze;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * Created by thomass on 28.01.22.
 */
public class MazeMovingAndStateSystemTest {

    /**
     *
     */
    @BeforeEach
    public void setup() {
        InitMethod initMethod = () -> {
            SystemManager.addSystem(new MazeMovingAndStateSystem());
            MazeDataProvider.reset();
            // what is the grid expected here?
            MazeDataProvider.init();
        };
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze"}, platformFactory, initMethod, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
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

    private void startSimpleTest() {

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);
        //t.b.c.

    }
}
