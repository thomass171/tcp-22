package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class BundleTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    /**
     * egg/hen problem during initial build? Needs intermediate
     * <p>
     * sh bin/deployBundle.sh -m engine
     */
    @Test
    public void testKeysInMap() {
        Bundle bundle = BundleRegistry.getBundle("engine");
        BundleResource bundleResource = new BundleResource("plane-darkgreen.bin");
        assertNotNull(bundle.getResource(bundleResource));
        // what about this? Normalize and find it.
        bundleResource = new BundleResource(bundle, new ResourcePath("."), "plane-darkgreen.bin");
        assertNotNull(bundle.getResource(bundleResource));
        // what about this? Normalize and find it.
        bundleResource = new BundleResource(bundle, "./plane-darkgreen.bin");
        assertNotNull(bundle.getResource(bundleResource));
    }

    @Test
    public void testSimplifyPath(){
        assertEquals("Models/Effects/interior", Bundle.simplifyPath("Models/Interior/Panel/Instruments/digital-clock/../../../../Effects/interior"));
        assertEquals("../../../Effects/interior", Bundle.simplifyPath("../../../Effects/interior"));
    }
}