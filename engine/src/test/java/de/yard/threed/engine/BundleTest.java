package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.testutil.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests fuer die scenery/terrasync bundle sind in SceneryTest.
 * Tests fuer Async sind in AsyncTest (ReferenceScene)
 * <p>
 * Created by thomass on 12.04.17.
 */
public class BundleTest {

    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    Bundle my777, fgdatabasicmodel;

    @Test
    public void testResolve() {
        my777 = TestHelper.buildDummyBundleModel777();
        fgdatabasicmodel = TestHelper.buildDummyBundleModelbasic();

        BundleResource br = new BundleResource(new ResourcePath("AI/Aircraft/737"), "737-AirBerlin.xml");
        br.bundle = fgdatabasicmodel;
        BundleResource result = BundleRegistry.findPath("Models/B737-300.ac", br);
        Assertions.assertEquals("AI/Aircraft/737/Models/B737-300.ac", result.getFullName());
    }
}
