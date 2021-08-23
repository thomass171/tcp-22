package de.yard.threed.tools;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;

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
}
