package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.apps.reference.ReferenceTests;
import de.yard.threed.engine.avatar.AvatarComponent;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.JoinComponent;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static de.yard.threed.core.Matrix4.cos;
import static de.yard.threed.core.Matrix4.sin;
import static de.yard.threed.core.testutil.TestUtils.assertVector3;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of first person movement. As long as some calculations are in the platform,
 * these should be runtime tested. But easier to handle from here.
 * <p>
 * Created by thomass on 31.08.23.
 */
public class FirstPersonTest {
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testTransform() {
        ReferenceTests.testFirstPersonTransform(null);

    }

}
