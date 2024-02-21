package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import de.yard.threed.core.platform.AsyncHttpResponse;
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
import static de.yard.threed.javanative.JavaUtil.sleepMs;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
@Slf4j
public class ResourceLoaderTest {

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
    public void testHttpLoad() throws Exception {
        runHttpLoad(false);
    }

    @Test
    public void testBundleLoad() throws Exception {
        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        byte[] binData = "bin data".getBytes(StandardCharsets.UTF_8);

        BundleResource bundleResource = new BundleResource(BundleRegistry.getBundle("engine"),"bike.xml");

        ResourceLoaderFromBundle resourceLoader = new ResourceLoaderFromBundle(bundleResource);

        AsyncHttpResponse response = loadResource(resourceLoader);

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());
        assertEquals("<?xml version=\"1.0\"?>", response.getContentAsString().substring(0,21));
    }

    private void runHttpLoad(boolean fails) throws Exception {

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());

        byte[] binData = "bin data".getBytes(StandardCharsets.UTF_8);

        WireMockHelper.mockHttpGet(wireMockServer, "/somepath/model.bin", binData);

        ResourceLoaderViaHttp resourceLoader = new ResourceLoaderViaHttp(new URL("http://localhost:" + wireMockServer.port() ,
                new ResourcePath("/somepath"),"model.bin"));

        AsyncHttpResponse response = loadResource(resourceLoader);

        wireMockServer.verify(1, RequestPatternBuilder.allRequests());

        assertEquals(0, AbstractSceneRunner.getInstance().futures.size());
        assertEquals("bin data", response.getContentAsString());
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
