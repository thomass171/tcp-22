package de.yard.threed.engine;

@FunctionalInterface
public interface TransformNodeVisitor {
    void handleNode(Transform node);
}
