package de.yard.threed.tools;

import de.yard.threed.core.loader.*;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.InMemoryBundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.tools.testutil.ModelAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class GLTFBuildingTest {
    Platform platform = CoreTestFactory.initPlatformForTest(new SimpleHeadlessPlatformFactory(), null,
            ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));

    @Test
    public void testMultiObjectAC() throws Exception {

        String acfile = "tools/src/test/resources/multi-object.ac";

        GltfBuilderResult lf = new GltfProcessor().convertToGltf(TestUtils.locatedTestFile(acfile), Optional.empty());
        NativeJsonValue gltf = platform.parseJson(lf.gltfstring);
        Assertions.assertNotNull(gltf, "parsedgltf");
        BundleResource gltfbr = new BundleResource(new InMemoryBundle("multi-object", lf.gltfstring, lf.bin), "multi-object.gltf");

        LoaderGLTF lf1 = LoaderGLTF.buildLoader(gltfbr, null);
        PortableModel pm = lf1.doload();

        ModelAssertions.assertMultiObject(pm,true,true, false);
    }


}
