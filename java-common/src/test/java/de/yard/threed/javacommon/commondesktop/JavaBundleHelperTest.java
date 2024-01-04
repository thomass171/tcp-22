package de.yard.threed.javacommon.commondesktop;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.core.testutil.WireMockHelper;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.JavaBundleHelper;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Refer to bundle 'data', which is deployed at the very beginning of the build.
 */
@Slf4j
public class JavaBundleHelperTest {

    private WireMockServer wireMockServer;

    Platform platform = CoreTestFactory.initPlatformForTest(new SimpleHeadlessPlatformFactory(), null, new ConfigurationByEnv());

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
    public void testLoadTextureInBundleFromFs() {

        ResourcePath bundleBasedir = BundleResolver.resolveBundle("data", platform.bundleResolver);

        String texturename = "textures/SokobanTarget.png";

        // cannot load bundle here, so mock it. But InMemoryBundle has no basepath.
        Bundle bundle = new Bundle("data-mock", new String[]{"not relevant"}, bundleBasedir.getPath());
        BundleResource bundleResource = new BundleResource(bundle, new ResourcePath(""), texturename);

        BufferedImage bi = JavaBundleHelper.loadBundleTexture(URL.fromBundleResource(bundleResource));

        assertNotNull(bi);
        assertEquals(512, bi.getWidth());
        assertEquals(512, bi.getHeight());
    }

    @Test
    public void testLoadTextureInBundleViaHttp() throws Exception {

        ResourcePath bundleBasedir = BundleResolver.resolveBundle("data", platform.bundleResolver);

        String texturename = "textures/SokobanTarget.png";
        byte[] png = TestUtils.loadFileFromPath(Paths.get(bundleBasedir.getPath()+"/"+texturename));
        assertNotNull(png);

        mockWebTexture(wireMockServer, "/bundles/data/"+texturename, png, false);

        // cannot load bundle here? so mock it. But InMemoryBundle has no basepath.
        Bundle bundle = new Bundle("data-mock", new String[]{"not relevant"}, "http://localhost:8089/bundles/data");

        BundleResource bundleResource = new BundleResource(bundle, new ResourcePath(""), texturename);
        BufferedImage bi = JavaBundleHelper.loadBundleTexture(URL.fromBundleResource(bundleResource));

        assertNotNull(bi);
        assertEquals(512, bi.getWidth());
        assertEquals(512, bi.getHeight());

        wireMockServer.verify(1, RequestPatternBuilder.allRequests());
    }

    public static void mockWebTexture(WireMockServer wireMockServer, String path, byte[] png, boolean fail) {
        if (fail) {
            WireMockHelper.mockHttpGet(wireMockServer, path, 404);
        } else {
            WireMockHelper.mockHttpGet(wireMockServer, path, png);
        }
    }
}
