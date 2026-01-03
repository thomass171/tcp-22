package de.yard.threed.engine;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.CharsetException;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.NativeResourceLoader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.*;
import de.yard.threed.engine.testutil.EngineTestUtils;
import de.yard.threed.engine.testutil.ModelAssertions;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.TestHelper;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.outofbrowser.FileSystemBundleResourceLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static de.yard.threed.core.testutil.TestUtils.loadFileFromPath;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Cannot be located in core because DefaultPlatform has no json parser.
 */
public class LoaderGLTFTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testAlphaBlendModeTest() throws Exception {

        String basename = "data/gltf-sample-assets/AlphaBlendModeTest/AlphaBlendModeTest";
        String gltf = new String(loadFileFromPath(Paths.get(TestUtils.locatedTestFile(basename + ".gltf"))));
        NativeByteBuffer bin = new SimpleByteBuffer(loadFileFromPath(Paths.get(TestUtils.locatedTestFile(basename + ".bin"))));

        LoaderGLTF loaderGLTF = new LoaderGLTF(gltf, bin, null, basename + ".gltf");

        PortableModel portableModel = loaderGLTF.doload();

        ModelAssertions.assertAlphaBlendModeTest(portableModel);
    }

    @Test
    public void testBinless() throws Exception {

        Bundle bundle = EngineTestFactory.addTestResourcesBundle();

        String gltfname = "loader/binless.gltf";

        // 18.12.25 gltf files are no longer loaded immediately
        //BundleRegistry.getBundle("test-resources").getResource(gltfname).getContentAsString();
        EngineTestUtils.testLoadedResource(bundle.getOriginalResourceLoader(), gltfname, new AsyncJobDelegate<AsyncHttpResponse>() {
            @Override
            public void completed(AsyncHttpResponse response) {

                try {
                    String  gltf = response.getContentAsString();
                    LoaderGLTF loaderGLTF = new LoaderGLTF(gltf, null, null, gltfname);
                    PortableModel portableModel = loaderGLTF.doload();
                    PortableMaterial m = portableModel.findMaterial("ambiciousMaterial-pre2024");
                    assertNotNull(m);
                    assertNotNull(m.getTexture());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
