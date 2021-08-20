package de.yard.threed.engine.ecs;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;


/**
 * 
 * <p>
 * Created by thomass on 07.8.17.
 */
public class ComponentTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testVelocityComponent() {
        VelocityComponent vc = new VelocityComponent();
        vc.setAcceleration(2.5);
        vc.setMovementSpeed(20);
        TestUtil.assertEquals("bremsweg",32,vc.getBrakingDistance());
        
    }


}
