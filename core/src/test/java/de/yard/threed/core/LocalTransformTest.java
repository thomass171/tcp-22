package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.TestUtils;
import org.junit.jupiter.api.Test;

public class LocalTransformTest {

    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void testSimple() {

        LocalTransform lt = LocalTransform.buildFromConfig("0.2,0.3,-0.6,60.4,-70,+88.88");
        TestUtils.assertVector3(new Vector3(0.2, 0.3,-0.6), lt.position);
    }
}
