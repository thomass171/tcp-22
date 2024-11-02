package de.yard.threed.core;

/**
 * Generic getter/setter that is lambda ready
 */
@FunctionalInterface
public interface ValueWrapper<T> {
    /**
     * pass null for getting value without setting. Yes, its confusing but useful to keep it lambda ready.
     */
    T value(T value);
}
