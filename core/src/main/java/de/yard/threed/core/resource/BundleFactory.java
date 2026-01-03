package de.yard.threed.core.resource;

import de.yard.threed.core.platform.NativeResourceLoader;

@FunctionalInterface
public interface BundleFactory {
    Bundle createBundle(String name, boolean delayed, String[] directory, String basepath, NativeResourceLoader resourceLoader);
}
