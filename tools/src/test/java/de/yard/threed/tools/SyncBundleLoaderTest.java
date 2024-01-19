package de.yard.threed.tools;

import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * <p>
 * Created by thomass on 15.01.24.
 */
public class SyncBundleLoaderTest {

    @BeforeEach
    public void setup() {
        new SimpleHeadlessPlatformFactory().createPlatform(ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
    }

    @Test
    public void testLoadBundle() {

        String bundleName = "engine";
        assertNull(BundleRegistry.getBundle(bundleName));
        SyncBundleLoader.loadBundleAndWait(bundleName);
        assertNotNull(BundleRegistry.getBundle(bundleName));
    }
}
