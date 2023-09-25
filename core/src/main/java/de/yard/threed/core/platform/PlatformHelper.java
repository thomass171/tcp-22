package de.yard.threed.core.platform;

import de.yard.threed.core.StringUtils;
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
}
