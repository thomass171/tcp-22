package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpBundleResolverTest {

    @BeforeAll
    static void setup() {
        Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);
    }

    @Test
    public void testWithPattern() {
        String pattern = "b1,b2@http://example.org:8080/bundles";
        HttpBundleResolver resolver = new HttpBundleResolver(pattern);

        assertEquals("http://example.org:8080/bundles/b1", resolver.resolveBundle("b1").getPath());
        assertNull(resolver.resolveBundle("xyz"));
    }

    @Test
    public void testWithDefault() {
        HttpBundleResolver resolver = new HttpBundleResolver();

        assertEquals("bundles/b1", resolver.resolveBundle("b1").getPath());
        assertEquals("bundles/xyz", resolver.resolveBundle("xyz").getPath());
    }
}
