package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Degree;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.core.testutil.WireMockHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.ResourceLoaderViaHttp;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.testutil.AdvancedHeadlessPlatformFactory;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.javacommon.JavaWebClient;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.yard.threed.core.testutil.TestUtils.loadFileFromResources;
import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.*;

/**
 * See also ModelBuildTest
 */
@Slf4j
public class PlatformModelLoaderTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new AdvancedHeadlessPlatformFactory());

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
    public void testHttpLoad() throws Exception {
        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        mockHttpLoadCesium(false);

        String baseUrl = "http://localhost:" + wireMockServer.port() + "/bundles";
        runLoad(new ResourceLoaderViaHttp(new URL(baseUrl,new ResourcePath("somebundle/cesiumbox"),"BoxTextured.gltf")));

        wireMockServer.verify(3, RequestPatternBuilder.allRequests());

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

    }

    @Test
    public void testBundleLoad() throws Exception {
        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        BundleResource bundleResource = new BundleResource(BundleRegistry.getBundle("engine"), "cesiumbox/BoxTextured.gltf");
        runLoad(new ResourceLoaderFromBundle(bundleResource));

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());
    }

    private void runLoad(ResourceLoader resourceLoader) throws Exception {
        List<NativeSceneNode> builtNodes = new ArrayList<>();
        Platform.getInstance().buildNativeModelPlain(resourceLoader, null, new ModelBuildDelegate() {
            @Override
            public void modelBuilt(BuildResult result) {
                if (result.getNode() == null) {
                    fail("no node");
                }
                builtNodes.add(result.getNode());
            }
        }, 0);

        TestUtils.waitUntil(() -> {
            TestHelper.processAsync();
            return builtNodes.size() > 0;
        }, 10000);

        assertEquals(1, builtNodes.size());
        assertTrue(Texture.hasTexture("CesiumLogoFlat.png"), "CesiumLogoFlat.texture");

        SceneNode cesiumBox = new SceneNode(builtNodes.get(0));
    }

    private void mockHttpLoadCesium(boolean gltfFails) throws Exception {

        byte[] binData = "bin data".getBytes(StandardCharsets.UTF_8);
        String bundleName = "somebundle";

        WireMockHelper.mockHttpGet(wireMockServer, "/somepath/model.bin", binData);
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/directory.txt",
                "cesiumbox/BoxTextured.gltf\ncesiumbox/BoxTextured0.bin\ncesiumbox/CesiumLogoFlat.png");
        if (gltfFails) {
            WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/cesiumbox/BoxTextured.gltf", 404);
        } else {
            WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/cesiumbox/BoxTextured.gltf",
                    loadFileFromResources("cesiumbox/BoxTextured.gltf"));
        }
        // currently bin needs to have the same base name as gltf.
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/cesiumbox/BoxTextured.bin",
                loadFileFromResources("cesiumbox/BoxTextured.bin"));
        WireMockHelper.mockHttpGet(wireMockServer, "/bundles/" + bundleName + "/cesiumbox/CesiumLogoFlat.png",
                loadFileFromResources("cesiumbox/CesiumLogoFlat.png"));

    }

    private AsyncHttpResponse loadResource(ResourceLoader resourceLoader) throws Exception {
        List<AsyncHttpResponse> responses = new ArrayList();

        resourceLoader.loadResource(response -> {
            log.debug("got it");
            responses.add(response);
        });

        TestUtils.waitUntil(() -> {
            sleepMs(200);
            TestHelper.processAsync();

            return responses.size() > 0;
        }, 2000);

        assertEquals(1, responses.size());
        return responses.get(0);
    }
}
