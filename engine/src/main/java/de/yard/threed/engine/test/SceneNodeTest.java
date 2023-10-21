package de.yard.threed.engine.test;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.SceneNode;

/**
 *
 */
public class SceneNodeTest {

    public void testRemove() {
        String name = "tmp-node";
        SceneNode sceneNode = new SceneNode(name);

        RuntimeTestUtil.assertEquals("", 1, (Platform.getInstance()).findSceneNodeByName(name).size());
        SceneNode.removeSceneNodeByName(name);
        RuntimeTestUtil.assertEquals("", 0, (Platform.getInstance()).findSceneNodeByName(name).size());
    }
}
