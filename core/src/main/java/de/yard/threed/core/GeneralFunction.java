package de.yard.threed.core;

@FunctionalInterface
public interface GeneralFunction<R, T> {
    R handle(T parameter);
}
