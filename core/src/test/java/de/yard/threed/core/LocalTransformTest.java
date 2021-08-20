package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;

public class LocalTransformTest {

    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void testSimple() {

        LocalTransform lt = LocalTransform.buildFromConfig("0.2,0.3,-0.6,60.4,-70,+88.88");
        TestUtil.assertVector3(new Vector3(0.2, 0.3,-0.6), lt.position);
    }
}
