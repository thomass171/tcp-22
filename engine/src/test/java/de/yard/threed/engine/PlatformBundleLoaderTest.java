package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import de.yard.threed.core.HttpBundleResourceLoader;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.PlatformBundleLoader;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.core.testutil.WireMockHelper;
import de.yard.threed.engine.util.BooleanMethod;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@Slf4j
public class PlatformBundleLoaderTest {

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
    public void testHttpBundleLoad() throws Exception {
        Bundle bundle = runHttpBundleLoad(false);
        assertNotNull(bundle.getResource("model.gltf"));
    }

    @Test
    public void testHttpBundleLoadWithFailure() throws Exception {
        Bundle bundle = runHttpBundleLoad(true);
        assertNull(bundle.getResource("model.gltf"));
    }

    @Test
    public void testFileSystemBundleLoad() throws Exception {
        Bundle bundle = runFileSystemBundleLoad(false);
        assertNotNull(bundle.getResource("shader/Universal.vert"));
        assertTrue(bundle.exists("Iconset-LightBlue.png"));
        // Texture images are not really loaded.
        assertFalse(bundle.contains("Iconset-LightBlue.png"));
    }


    @Test
    public void testTestLoader() {
        String bundleName = "trffc-test";
        BundleRegistry.unregister(bundleName);
        EngineTestFactory.addBundleFromProjectDirectory(bundleName, "traffic/src/test/resources");
        Bundle bundle = BundleRegistry.getBundle(bundleName);
        assertEquals(3, bundle.getSize());
        assertNotNull(bundle);
        BundleRegistry.unregister(bundleName);
    }

    @Test
    public void testBundleNotFound() {
        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        List<Bundle> loadedBundle = new ArrayList();

        String bundleName = "unknownBundle";
        // location is retrieved from resolver
        NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, null);

        bundleLoader.loadBundle(bundleName, bundle -> {
            loadedBundle.add(bundle);
        }, resourceLoader);
        assertEquals(1, loadedBundle.size());
        assertNull(loadedBundle.get(0));

    }

    @Test
    public void testHttpBundleByResolver() {

        String bundleName = "httpBundle";
        // location is retrieved from resolver
        NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, null);
        // cannot be resolved without resolver
        assertNull(resourceLoader);

        Platform.getInstance().addBundleResolver(new HttpBundleResolver("httpBundle@http://host.org"));
        resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, null);
        // should be resolved with resolver
        assertNotNull(resourceLoader);
        assertEquals("de.yard.threed.core.HttpBundleResourceLoader", resourceLoader.getClass().getName());
        HttpBundleResourceLoader httpBundleResourceLoader = (HttpBundleResourceLoader) resourceLoader;
        assertEquals("http://host.org/httpBundle",httpBundleResourceLoader.getBasePath());
    }

    public void continueWith(BooleanMethod m, Runnable runnable) {
        if (m.isTrue()) {
            runnable.run();
        }
    }

    private Bundle runHttpBundleLoad(boolean gltfFails) throws Exception {

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());


        String bundleName = "bundle1";
        mockWebBundle(wireMockServer, bundleName, gltfFails);

        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        List<Bundle> loadedBundle = new ArrayList();
        String baseUrl = "http://localhost:" + wireMockServer.port() + "/bundles";
        NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, baseUrl);

        // launch twice to validate concurrent loading. Delegate should be executes twice, but download only once.
        bundleLoader.loadBundle(bundleName, bundle -> {
            log.debug("got it first");
            loadedBundle.add(bundle);
        }, resourceLoader);
        bundleLoader.loadBundle(bundleName, bundle -> {
            log.debug("got it second");
            loadedBundle.add(bundle);
        }, resourceLoader);

        TestUtils.waitUntil(() -> {
            sleepMs(200);
            TestHelper.processAsync();
            return loadedBundle.size() > 1;
        }, 2000);

        assertEquals(2, loadedBundle.size());
        assertEquals(0, bundleLoader.getLoadingbundles().size());
        wireMockServer.verify(3, RequestPatternBuilder.allRequests());

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());
        Bundle bundle = loadedBundle.get(0);
        assertNotNull(bundle);
        assertEquals(baseUrl + "/bundle1", bundle.getBasePath());
        return bundle;
    }

    public static void mockWebBundle(WireMockServer wireMockServer, String bundleName, boolean gltfFails) {
        String gltfData = "gltf data";
        byte[] binData = "bin data".getBytes(StandardCharsets.UTF_8);
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/directory.txt", "model.gltf\nmodel.bin");
        if (gltfFails) {
            WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/model.gltf", 404);
        } else {
            WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/model.gltf", gltfData);
        }
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/model.bin", binData);
    }

    private Bundle runFileSystemBundleLoad(boolean gltfFails) throws Exception {

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        String bundleName = "engine";
        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        List<Bundle> loadedBundle = new ArrayList();

        // location is retrieved from resolver
        NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, null);

        // launch twice to validate concurrent loading. Delegate should be executes twice, but download only once.
        // But Filesystem will be sync anyway, so here one after the other.
        bundleLoader.loadBundle(bundleName, bundle -> {
            log.debug("got it first");
            loadedBundle.add(bundle);
        }, resourceLoader);
        bundleLoader.loadBundle(bundleName, bundle -> {
            log.debug("got it second");
            loadedBundle.add(bundle);
        }, resourceLoader);

        TestUtils.waitUntil(() -> {
            sleepMs(200);
            TestHelper.processAsync();
            return loadedBundle.size() > 1;
        }, 2000);

        assertEquals(2, loadedBundle.size());
        assertEquals(0, bundleLoader.getLoadingbundles().size());

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());
        Bundle bundle = loadedBundle.get(0);
        assertNotNull(bundle);
        assertEquals(((SimpleHeadlessPlatform) Platform.getInstance()).hostdir + "/bundles/engine", bundle.getBasePath());
        return bundle;
    }


}
