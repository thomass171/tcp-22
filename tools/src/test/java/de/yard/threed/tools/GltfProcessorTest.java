package de.yard.threed.tools;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.tools.testutil.Loader3DSBuilderForTest;
import de.yard.threed.tools.testutil.LoaderOBJBuilderForTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TODO reread and validate created gltf
 * <p>
 */
public class GltfProcessorTest {
    static Platform platform = ToolsPlatform.init();

    @Test
    public void testAC() throws Exception {

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/twosided-rectangle.ac"
        };
        new GltfProcessor().runMain(argv);
    }

    @Test
    public void testObjByDynamicLoader() throws Exception {

        int instances = LoaderOBJBuilderForTest.instanceCount;

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/cube.obj",
                "-l", "de.yard.threed.tools.testutil.LoaderOBJBuilderForTest"
        };
        new GltfProcessor().runMain(argv);

        // make sure dynamic loader was used
        assertEquals(instances + 1, LoaderOBJBuilderForTest.instanceCount);
    }

    @Test
    public void test3DSByDynamicLoader() throws Exception {

        int instances = Loader3DSBuilderForTest.instanceCount;

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/shut.3ds",
                "-l", "de.yard.threed.tools.testutil.Loader3DSBuilderForTest"
        };
        new GltfProcessor().runMain(argv);

        // make sure dynamic loader was used
        assertEquals(instances + 1, Loader3DSBuilderForTest.instanceCount);
    }
}
