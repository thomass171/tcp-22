package de.yard.threed.core.platform;

@FunctionalInterface
public interface TransformNodeVisitor {
    void handleNode(NativeTransform node);
}
