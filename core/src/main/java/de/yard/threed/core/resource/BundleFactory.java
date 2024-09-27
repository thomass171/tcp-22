package de.yard.threed.core.resource;

@FunctionalInterface
public interface BundleFactory {
    Bundle createBundle(String name, boolean delayed, String[] directory, String basepath);
}
