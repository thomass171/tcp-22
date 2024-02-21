package de.yard.threed.engine;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class ResourceLoaderFromBundleTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void test() {
        Bundle bundle = BundleRegistry.getBundle("engine");
        assertNotNull(bundle);

        BundleResource bundleResource = new BundleResource(bundle, "cesiumbox/BoxTextured.gltf");
        ResourceLoaderFromBundle resourceLoader = new ResourceLoaderFromBundle(bundleResource);

        resourceLoader = (ResourceLoaderFromBundle) resourceLoader.fromReference("texture.png");

        assertEquals("cesiumbox", resourceLoader.bundleResource.getPath().getPath());
        assertEquals("texture.png", resourceLoader.bundleResource.getName());
        assertEquals("engine:cesiumbox/texture.png", resourceLoader.bundleResource.getFullQualifiedName());


    }
}