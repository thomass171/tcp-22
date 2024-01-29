package de.yard.threed.core;

/**
 * Generic getter/setter that is lambda ready
 */
@FunctionalInterface
public interface ValueWrapper<T> {
    /**
     * pass null for getting value without setting.
     */
    T value(T value);
}
