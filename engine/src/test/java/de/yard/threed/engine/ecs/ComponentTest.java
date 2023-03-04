package de.yard.threed.engine.ecs;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * <p>
 * Created by thomass on 07.8.17.
 */
public class ComponentTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testVelocityComponent() {
        VelocityComponent vc = new VelocityComponent();
        vc.setAcceleration(2.5);
        vc.setMovementSpeed(20);
        Assertions.assertEquals(32, vc.getBrakingDistance(), "bremsweg");

    }


}
