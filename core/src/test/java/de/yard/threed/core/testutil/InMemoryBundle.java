package de.yard.threed.core.testutil;

import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;

/**
 * Helper for testing, eg. GLTF generating.
 */
public class InMemoryBundle extends TestBundle {
    public InMemoryBundle(String fname, String json, byte[] bindata) {
        super("", new String[]{}, "");
        super.resources.put(fname + ".gltf", new BundleData(new SimpleByteBuffer(json.getBytes()), true));
        super.resources.put(fname + ".bin", new BundleData(new SimpleByteBuffer(bindata), false));
        directory = new String[]{fname + ".gltf", fname + ".bin"};
    }
}
