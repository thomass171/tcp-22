package de.yard.threed.core.testutil;

import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;

public class TestBundle extends Bundle {

    public TestBundle(String name, String[] directory, String basepath) {
        super(name, directory, basepath);
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
