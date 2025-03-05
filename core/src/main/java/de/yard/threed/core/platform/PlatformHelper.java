package de.yard.threed.core.platform;

import de.yard.threed.core.GenericTreeNode;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.TreeNodeVisitor;
import de.yard.threed.core.Util;

import java.util.List;

/**
 * There is also an EngineHelper.
 */
public class PlatformHelper {

    /**
     * Also calls visitor for itself/first.
     * Extrcated from Transform.
     *
     * @param childVisitor
     */
    public static void traverseTransform(NativeTransform transform, TransformNodeVisitor childVisitor) {
        childVisitor.handleNode(transform);
        List<NativeTransform> children = transform.getChildren();
        for (int i = 0; i < children.size(); i++) {
            NativeTransform child = children.get(i);
            //childVisitor.handleNode(child);
            traverseTransform(child, childVisitor);
        }
    }

    /**
     * More generic traverse
     * Also calls visitor for itself/first.
     */
    public static <T> void traverse(GenericTreeNode<T> treeNode, TreeNodeVisitor<T> childVisitor) {
        childVisitor.handleNode(treeNode.getTreeElement());

        for (int i = 0; i < treeNode.getChildCount(); i++) {
            traverse(treeNode.getChild(i), childVisitor);
        }
    }
}
