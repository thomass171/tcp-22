package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestBundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * <p>
 */
public class BundleResourceTest {

    @BeforeAll
    static void setup() {
        Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);
    }

    @Test
    public void testFullQualifiedString() {

        // 13.2.24: Since ResourcePath now is defined to have no leading "/" als reject this full qualified string??
        BundleResource bundleResource = BundleResource.buildFromFullQualifiedString("b:/path/file.xml");
        assertEquals("b",bundleResource.getBundlename());
        assertEquals("/path",bundleResource.getPath().getPath());
        assertEquals("file.xml",bundleResource.getName());
        assertEquals("b:/path/file.xml",bundleResource.getFullQualifiedName());

        // 13.2.24: Should also be accepted.
        bundleResource = BundleResource.buildFromFullQualifiedString("b:path/file.xml");
        assertEquals("b",bundleResource.getBundlename());
        assertEquals("path",bundleResource.getPath().getPath());
        assertEquals("file.xml",bundleResource.getName());
        assertEquals("b:path/file.xml",bundleResource.getFullQualifiedName());
    }

    @Test
    public void testConstructor(){
        TestBundle bundle=new TestBundle("test",new String[]{},"/xy");
        BundleResource bundleResource=new BundleResource(bundle, "cesiumbox/BoxTextured.gltf");

        assertEquals("cesiumbox",bundleResource.getPath().getPath());
        assertEquals("BoxTextured.gltf",bundleResource.getName());
        assertEquals("test:cesiumbox/BoxTextured.gltf",bundleResource.getFullQualifiedName());
    }

    @Test
    public void testFullString() {

        BundleResource bundleResource = BundleResource.buildFromFullString("railing/Railing.xml");
        assertNull(bundleResource.getBundlename());
        assertEquals("railing",bundleResource.getPath().getPath());
        assertEquals("Railing.xml",bundleResource.getName());
    }
}
