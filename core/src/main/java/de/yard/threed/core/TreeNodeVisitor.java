package de.yard.threed.core;

import de.yard.threed.core.platform.NativeTransform;

/**
 * More generic than TransformNodeVisitor
 */
@FunctionalInterface
public interface TreeNodeVisitor<T> {
    void handleNode(T node);
}
