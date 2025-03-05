package de.yard.threed.javacommon.commondesktop;

import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.javacommon.commondesktop.testutil.ResourceReaderMock;
import de.yard.threed.outofbrowser.NativeResourceReader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * See also HttpBundleResolverTest.
 */
public class SimpleBundleResolverTest {

    @BeforeAll
    static void setup() {
        CoreTestFactory.initPlatformForTest(new SimpleHeadlessPlatformFactory(), null, new ConfigurationByEnv());
    }

    @Test
    public void testWithDefault() {
        NativeResourceReader resourceReader = new ResourceReaderMock();
        SimpleBundleResolver resolver = new SimpleBundleResolver("bundles", resourceReader);

        Assertions.assertEquals("bundles/b1", resolver.resolveBundle("b1").getPath());
        Assertions.assertEquals("bundles/xyz", resolver.resolveBundle("xyz").getPath());
    }

    @Test
    public void testWithSpecificBundle() {
        NativeResourceReader resourceReader = new ResourceReaderMock();
        SimpleBundleResolver resolver = new SimpleBundleResolver("bundles", resourceReader, "sbundle");

        Assertions.assertEquals("bundles/sbundle", resolver.resolveBundle("sbundle").getPath());
        Assertions.assertNull(resolver.resolveBundle("xyz"));
    }

    @Test
    public void testBundlePath() {
        NativeResourceReader resourceReader = new ResourceReaderMock();
        SimpleBundleResolver resolver = new SimpleBundleResolver("bundles", resourceReader);
        resolver.addBundlePath("b2","../subpath");

        Assertions.assertEquals("bundles/b1", resolver.resolveBundle("b1").getPath());
        Assertions.assertEquals("bundles/xyz", resolver.resolveBundle("xyz").getPath());
        Assertions.assertEquals("bundles/../subpath/b2", resolver.resolveBundle("b2").getPath());
    }

}
