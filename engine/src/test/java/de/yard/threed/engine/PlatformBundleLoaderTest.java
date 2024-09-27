package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.HttpBundleResourceLoader;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.HttpBundleResolver;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.platform.PlatformBundleLoader;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.ResourceLoaderFromDelayedBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.core.testutil.WireMockHelper;
import de.yard.threed.engine.util.BooleanMethod;
import de.yard.threed.javacommon.JavaWebClient;
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

        JavaWebClient.close();
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
        Bundle bundle = runFileSystemBundleLoad(false, false);
        assertNotNull(bundle.getResource("shader/Universal.vert"));
        assertTrue(bundle.exists("Iconset-LightBlue.png"));
        // Texture images are not really loaded.
        assertFalse(bundle.contains("Iconset-LightBlue.png"));
    }

    @Test
    public void testFileSystemBundleLoadDelayed() throws Exception {
        Bundle bundle = runFileSystemBundleLoad(false, true);
        // resouce not yet loaded
        assertNull(bundle.getResource("shader/Universal.vert"));
        assertTrue(bundle.exists("shader/Universal.vert"));
        // Texture images are not really loaded.
        assertFalse(bundle.contains("shader/Universal.vert"));

        assertTrue(bundle.exists("cesiumbox/BoxTextured.gltf"));
        assertTrue(bundle.exists("cesiumbox/BoxTextured.bin"));
        assertFalse(bundle.exists("cesiumbox/BoxTextured.xx"));
        assertFalse(bundle.contains("cesiumbox/BoxTextured.gltf"));
        assertFalse(bundle.contains("cesiumbox/BoxTextured.bin"));

        ResourceLoader resourceLoader = new ResourceLoaderFromDelayedBundle(new BundleResource(bundle,"cesiumbox/BoxTextured.gltf"),
                Platform.getInstance().buildResourceLoader(bundle.name, null));

        // LoaderGLTF will use "reference" for bin
        BooleanHolder handled = new BooleanHolder(false);
        LoaderGLTF.load(resourceLoader, new GeneralParameterHandler<PortableModel>() {
            @Override
            public void handle(PortableModel parameter) {
                handled.setValue(true);
            }
        });

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return handled.getValue();
        }, 30000);

        assertTrue(bundle.contains("cesiumbox/BoxTextured.gltf"));
        assertTrue(bundle.contains("cesiumbox/BoxTextured.bin"));

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

        bundleLoader.loadBundle(bundleName, false, bundle -> {
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
        assertEquals("http://host.org/httpBundle", httpBundleResourceLoader.getBasePath());
    }

    /**
     * Loads external bundle. Nasty dependency, but useful for testing larger bundle performance.
     */
    @Test
    public void testExternalBundle() throws Exception {

        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        String bundleName = "fgdatabasic";
        List<Bundle> loadedBundle = new ArrayList();
        String baseUrl = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool";
        NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, baseUrl);

        bundleLoader.loadBundle(bundleName, false, bundle -> {
            log.debug("got it");
            loadedBundle.add(bundle);
        }, resourceLoader);

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return loadedBundle.size() > 0;
        }, 30000);

        assertEquals(1, loadedBundle.size());
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
        bundleLoader.loadBundle(bundleName, false, bundle -> {
            log.debug("got it first");
            loadedBundle.add(bundle);
        }, resourceLoader);
        bundleLoader.loadBundle(bundleName, false, bundle -> {
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

    /**
     * mock bundle with two files + directory.
     */
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

    private Bundle runFileSystemBundleLoad(boolean gltfFails, boolean delayed) throws Exception {

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        String bundleName = "engine";
        PlatformBundleLoader bundleLoader = new PlatformBundleLoader();

        List<Bundle> loadedBundle = new ArrayList();

        // location is retrieved from resolver
        NativeBundleResourceLoader resourceLoader = Platform.getInstance().buildResourceLoader(bundleName, null);

        // launch twice to validate concurrent loading. Delegate should be executes twice, but download only once.
        // But Filesystem will be sync anyway, so here one after the other.
        bundleLoader.loadBundle(bundleName, delayed, bundle -> {
            log.debug("got it first");
            loadedBundle.add(bundle);
        }, resourceLoader);
        bundleLoader.loadBundle(bundleName, delayed, bundle -> {
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
