package de.yard.threed.core.resource;

@FunctionalInterface
public interface BundleFactory {
    Bundle createBundle(String name, String[] directory, String basepath);
}
