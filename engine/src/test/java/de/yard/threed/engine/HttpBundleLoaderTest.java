package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.Packet;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.DefaultLog;
import de.yard.threed.core.platform.LevelLogFactory;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.engine.testutil.WireMockHelper;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@Slf4j
public class HttpBundleLoaderTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testBundleLoad() throws Exception {
        Bundle bundle = runBundleLoad(false);
        assertNotNull(bundle.getResource("model.gltf"));
    }

    @Test
    public void testBundleLoadWithFailure() throws Exception {
        Bundle bundle = runBundleLoad(true);
        assertNull(bundle.getResource("model.gltf"));
    }

    private Bundle runBundleLoad(boolean gltfFails) throws Exception {

        assertEquals(0,AbstractSceneRunner.getInstance().futures.size());

        //TODO add image to test not loading
        String bundleName = "bundle1";
        String gltfData = "gltf data";
        byte[] binData = "bin data".getBytes(StandardCharsets.UTF_8);
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/directory.txt", "model.gltf\nmodel.bin");
        if (gltfFails) {
            WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/model.gltf", 404);
        } else {
            WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/model.gltf", gltfData);
        }
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/model.bin", binData);

        HttpBundleLoader httpBundleLoader = new HttpBundleLoader();

        List<Bundle> loadedBundle = new ArrayList();

        httpBundleLoader.asyncBundleLoad("http://localhost:" + wireMockServer.port() + "/bundles/" + bundleName, bundle -> {
            loadedBundle.add(bundle);
        }, false);

        TestUtils.waitUntil(() -> {
            sleepMs(200);
            TestHelper.processAsync();
            return loadedBundle.size() > 0;
        }, 2000);

        assertEquals(1, loadedBundle.size());
        assertEquals(0,AbstractSceneRunner.getInstance().futures.size());
        Bundle bundle = loadedBundle.get(0);
        assertNotNull(bundle);
        return bundle;
    }


}
