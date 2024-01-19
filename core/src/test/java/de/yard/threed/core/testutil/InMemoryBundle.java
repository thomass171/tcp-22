package de.yard.threed.core.testutil;


import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;


/**
 * Helper for testing, eg. GLTF generating.
 */
public class InMemoryBundle extends Bundle {
    public InMemoryBundle(String fname, String json, byte[] bindata) {
        super("", "", false,"");
        super.resources.put(fname + ".gltf", new BundleData(new SimpleByteBuffer(json.getBytes()), true));
        super.resources.put(fname + ".bin", new BundleData(new SimpleByteBuffer(bindata), false));
        directory = new String[]{fname + ".gltf", fname + ".bin"};
    }

    public void addAdditionalResource(String fname, BundleData bundleData) {
        String[] nd = new String[directory.length + 1];
        for (int i = 0; i < directory.length; i++) {
            nd[i] = directory[i];
        }
        nd[directory.length] = name;
        directory = nd;
        super.resources.put(fname, bundleData);
    }


}
