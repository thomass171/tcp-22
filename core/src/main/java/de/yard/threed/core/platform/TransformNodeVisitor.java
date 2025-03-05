package de.yard.threed.core.platform;

/**
 * We also have more generic TreeNodeVisitor, so set to deprecated
 */
@FunctionalInterface
@Deprecated
public interface TransformNodeVisitor {
    void handleNode(NativeTransform node);
}
