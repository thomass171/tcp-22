package de.yard.threed.tools;

import de.yard.threed.core.platform.Platform;
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
        GltfProcessor.runMain(argv);
    }

    @Test
    public void testObjByDynamicLoader() throws Exception {

        int instances = LoaderOBJBuilderForTest.instanceCount;

        String[] argv = new String[]{
                "-gltf", "-o", "src/test/resources/tmp", "src/test/resources/cube.obj",
                "-l", "de.yard.threed.tools.testutil.LoaderOBJBuilderForTest"
        };
        GltfProcessor.runMain(argv);

        // make sure dynamic loader was used
        assertEquals(instances + 1, LoaderOBJBuilderForTest.instanceCount);
    }
}
