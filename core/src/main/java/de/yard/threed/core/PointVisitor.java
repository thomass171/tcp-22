package de.yard.threed.core;

@FunctionalInterface
public interface PointVisitor {
    void visit(Point p);
}
