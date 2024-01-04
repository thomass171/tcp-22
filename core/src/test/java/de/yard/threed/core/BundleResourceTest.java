package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * <p>
 */
public class BundleResourceTest {

    @BeforeAll
    static void setup() {
        Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);
    }

    @Test
    public void testFullQualifiedString() {

        BundleResource bundleResource = BundleResource.buildFromFullQualifiedString("b:/path/file.xml");
        assertEquals("b",bundleResource.getBundlename());
        assertEquals("/path",bundleResource.getPath().getPath());
        assertEquals("file.xml",bundleResource.getName());
    }

    @Test
    public void testFullString() {

        BundleResource bundleResource = BundleResource.buildFromFullString("railing/Railing.xml");
        assertNull(bundleResource.getBundlename());
        assertEquals("railing",bundleResource.getPath().getPath());
        assertEquals("Railing.xml",bundleResource.getName());
    }
}
