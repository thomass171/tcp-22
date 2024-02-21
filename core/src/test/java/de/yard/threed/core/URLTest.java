package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.resource.URL;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * <p>
 */
public class URLTest {

    @BeforeAll
    static void setup() {
        Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);
    }

    @Test
    public void test() {

        URL url = new URL(    "engine",new ResourcePath("cesiumbox"),"BoxTextured.gltf");
        assertEquals("BoxTextured.gltf",url.getName());
        assertEquals("BoxTextured",url.getBasename());
        assertEquals("gltf",url.getExtension());
        assertEquals("cesiumbox",url.getPath().getPath());
        assertEquals("engine/cesiumbox/BoxTextured.gltf",url.getAsString());

        url = new URL(    "engine",new ResourcePath("m"),"BoxTextured");
        assertEquals("BoxTextured",url.getBasename());
        assertEquals("",url.getExtension());
        assertEquals("m",url.getPath().getPath());
        assertEquals("engine/m/BoxTextured",url.getAsString());
    }


}
