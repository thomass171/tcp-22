package de.yard.threed.tools;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;

import de.yard.threed.engine.loader.InvalidDataException;
import de.yard.threed.engine.loader.LoaderGLTF;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.tools.GltfBuilderResult;
import de.yard.threed.tools.GltfMemoryBundle;
import de.yard.threed.tools.ModelCreator;
import de.yard.threed.tools.ToolsPlatform;
import org.junit.jupiter.api.Test;


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
                result = ModelCreator.createModel("plane-darkGreen", "de.yard.threed.engine.geometry.PrimitiveCreator", new String[]{primitive, "darkGreen"});
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            NativeJsonValue gltf = platform.parseJson(result.gltfstring);
            TestUtil.assertNotNull("parsedgltf", gltf);
            BundleResource gltfbr = new BundleResource(new GltfMemoryBundle("plane-darkGreen", result.gltfstring, result.bin), "plane-darkGreen.gltf");
            try {
                LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);

            } catch (InvalidDataException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}
