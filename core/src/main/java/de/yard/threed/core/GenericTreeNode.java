package de.yard.threed.core;

import java.util.ArrayList;
import java.util.List;

/**
 * We also have TransformNodeVisitor and TreeNode
 */
public interface GenericTreeNode<T> {

    int getChildCount();

    GenericTreeNode getChild(int index);

    T getTreeElement();
}
