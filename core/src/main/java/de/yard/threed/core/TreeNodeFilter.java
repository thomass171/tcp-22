package de.yard.threed.core;

/**
 *
 */
@FunctionalInterface
public interface TreeNodeFilter<T> {
    boolean matches(TreeNode<T> n);
}
