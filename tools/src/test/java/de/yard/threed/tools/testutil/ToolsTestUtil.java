package de.yard.threed.tools.testutil;

import de.yard.threed.core.testutil.InMemoryBundle;
import de.yard.threed.javanative.FileReader;

import java.io.File;
import java.io.FileInputStream;

public class ToolsTestUtil {
    /**
     * used in external projects
     */
    public static InMemoryBundle buildFromFilesystem(String gltfFilename, String baseNameInBundle) throws Exception {
        String gltfstring = FileReader.readAsString(new File(gltfFilename));
        byte[] bin = FileReader.readFully(new FileInputStream(new File(gltfFilename.replace("gltf", "bin"))));
        return new InMemoryBundle(baseNameInBundle, gltfstring, bin);
    }
}
