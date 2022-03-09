package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.GridTeleportDestination;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.NameFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.core.testutil.TestUtil.assertNotNull;


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
        InitMethod initMethod = () -> SystemManager.addSystem(new MazeMovingAndStateSystem());
        SimpleHeadlessPlatformFactory platformFactory = new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting());
        TestFactory.initPlatformForTest(new String[]{"engine", "maze"}, platformFactory, initMethod);
    }

    @Test
    public void testSimpleNonVR() throws Exception {

        Observer.buildForDefaultCamera();
        assertNotNull("observer", Observer.getInstance());

        startSimpleTest();


    }

    @Test
    public void testSimpleVR() throws Exception {

        Platform.getInstance().setSystemProperty("argv.enableVR", "true");
        VrInstance.buildFromArguments();
        assertNotNull("VrInstance", VrInstance.getInstance());

        Observer.buildForDefaultCamera();
        assertNotNull("observer", Observer.getInstance());

        startSimpleTest();

    }

    private void startSimpleTest() {

        String testUserName = "testUserName";
        EcsEntity user = new EcsEntity(new UserComponent("user account"));
        user.setName(testUserName);
        //t.b.c.

    }
}
