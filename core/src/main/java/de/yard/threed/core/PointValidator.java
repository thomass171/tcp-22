package de.yard.threed.core;

@FunctionalInterface
public interface PointValidator {
    boolean isValid(Point p);
}
