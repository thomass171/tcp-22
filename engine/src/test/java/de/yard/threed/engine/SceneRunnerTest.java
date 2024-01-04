package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SceneRunnerTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {

        EngineTestFactory.resetInit();

        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    /**
     * Preload is very similar to regular bundle loading, so should always be possible.
     * Also use load bundle via HTTP in init chain.
     */
    @Test
    public void testInitChain() {
        assertEquals(0, BundleRegistry.getBundleNames().length);

        String bundleName = "bundle1";
        PlatformBundleLoaderTest.mockWebBundle(wireMockServer,bundleName, false);

        EngineTestFactory.initPlatformForTest(new String[]{"engine", "data", "http://localhost:" + wireMockServer.port() + "/bundles/bundle1"},
                new SimpleHeadlessPlatformFactory() );

        assertEquals(3, BundleRegistry.getBundleNames().length);
        Bundle engine = BundleRegistry.getBundle("engine");
        assertNotNull(engine);

        Bundle data = BundleRegistry.getBundle("data");
        assertNotNull(data);

        Bundle bundle1 = BundleRegistry.getBundle("bundle1");
        assertNotNull(bundle1);
    }

}
