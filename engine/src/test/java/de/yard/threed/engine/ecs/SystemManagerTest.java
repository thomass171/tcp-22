package de.yard.threed.engine.ecs;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.SegmentedPath;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 */
public class SystemManagerTest {
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testProvider() {
        SystemManager.reset();
        SystemManager.putDataProvider("n", new SimpleDataProvider());
        try {
            SystemManager.putDataProvider("n", new SimpleDataProvider());
            fail("exception expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }
}

class SimpleDataProvider implements DataProvider {

    @Override
    public Object getData(Object[] parameter) {
        return null;
    }
}
