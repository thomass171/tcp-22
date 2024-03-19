package de.yard.threed.core;

@FunctionalInterface
public interface ObjectBuilder<T> {
    T buildFromString(String s);
}
