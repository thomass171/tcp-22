package de.yard.threed.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is not easy to use.
 * We also have TransformNodeVisitor and GenericTreeNode.
 */
public class TreeNode<T> {
    private T element;
    private List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();

    public TreeNode(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }

    public void addChild(T child) {
        children.add(new TreeNode(child));
    }

    public void addChild(TreeNode<T> child) {
        children.add(child);
    }

    public int getChildCount() {
        return children.size();
    }

    public TreeNode<T> getChild(int index) {
        return children.get(index);
    }

    public T getChildElement(int index) {
        return children.get(index).getElement();
    }

    public List<TreeNode<T>> findNode(TreeNodeFilter<T> filter) {
        List<TreeNode<T>> nodelist = new ArrayList<TreeNode<T>>();
        // Also check 'this'.
        if (filter.matches(this)) {
            nodelist.add(this);
        }
        for (TreeNode child : children) {
            // shouldn't be null, but avoid NPE
            if (child != null) {
                nodelist.addAll(child.findNode(filter));
            }
        }
        return nodelist;
    }
}
