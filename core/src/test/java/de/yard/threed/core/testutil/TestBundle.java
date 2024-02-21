package de.yard.threed.core.testutil;

import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;

public class TestBundle extends Bundle {

    public TestBundle(String name, String[] directory, String basepath) {
        super(name, directory, basepath);
    }

    public void addAdditionalResource(String fname, BundleData bundleData) {
        directory.add(fname);
        super.resources.put(fname, bundleData);
    }
}
