package de.yard.threed.tools;

import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;

import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.InMemoryBundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * <p>
 * Created by thomass on 06.01.19.
 */
public class ModelCreatorTest {
    static Platform platform = ToolsPlatform.init();

    @Test
    public void testPrimitive() {

        for (String primitive : new String[]{"plane", "sphere"}) {
            GltfBuilderResult result = null;
            try {
                result = ModelCreator.createModel("plane-darkGreen", "de.yard.threed.core.geometry.PrimitiveCreator", new String[]{primitive, "darkGreen"});
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            NativeJsonValue gltf = platform.parseJson(result.gltfstring);
            assertNotNull(gltf, "parsedgltf");
            BundleResource gltfbr = new BundleResource(new InMemoryBundle("plane-darkGreen", result.gltfstring, result.bin), "plane-darkGreen.gltf");
            try {
                LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
                assertNotNull(lf1.doload());
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     *
     */
    @Test
    public void testLocomotive() {

        GltfBuilderResult result = null;
        try {
            result = ModelCreator.createModel("loc", "de.yard.threed.traffic.railing.LocomotiveCreator", new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        NativeJsonValue gltf = platform.parseJson(result.gltfstring);
        assertNotNull(gltf, "parsedgltf");
        BundleResource gltfbr = new BundleResource(new InMemoryBundle("loc", result.gltfstring, result.bin), "loc.gltf");
        try {
            LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
            assertNotNull(lf1.doload());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
