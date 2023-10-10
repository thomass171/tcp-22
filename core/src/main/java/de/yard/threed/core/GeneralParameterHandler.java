package de.yard.threed.core;

@FunctionalInterface
public interface GeneralParameterHandler<T> {
    void handle(T parameter);
}
