package de.yard.threed.tools;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.javanative.FileReader;

import java.io.File;
import java.io.FileInputStream;

/**
 * Helper zum Testen der GLTF Generierung
 */
public class GltfMemoryBundle extends Bundle {
    public GltfMemoryBundle(String fname, String json,byte[] bindata){
        super("","",false);
        super.resources.put(fname+".gltf",new BundleData(json.getBytes(),true));
        super.resources.put(fname+".bin",new BundleData(new SimpleByteBuffer(bindata),false));
        directory = new String[]{fname+".gltf",fname+".bin"};
    }

    /**
     * used in external projects
     */
    public static GltfMemoryBundle buildFromFilesystem(String gltfFilename, String baseNameInBundle) throws Exception {
        String gltfstring = FileReader.readAsString(new File(gltfFilename));
        byte[] bin = FileReader.readFully(new FileInputStream(new File(gltfFilename.replace("gltf","bin"))));
        return new GltfMemoryBundle(baseNameInBundle, gltfstring, bin);
    }
}
