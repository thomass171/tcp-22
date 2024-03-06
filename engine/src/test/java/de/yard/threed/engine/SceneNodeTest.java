package de.yard.threed.engine;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.reference.ReferenceScene;
import de.yard.threed.engine.apps.reference.ReferenceTests;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class SceneNodeTest {
    static Platform platform = EngineTestFactory.initPlatformForTest( new String[] {"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testFindNode() {

        ArrayList<SceneNode> towerrechts = new ArrayList<SceneNode>();

        ReferenceScene.buildTower("r", towerrechts, 4, 3, 1, new Color[]{Color.RED, new Color(1.0f, 1.0f, 0), Color.GREEN}, false);

        assertEquals(1,SceneNode.findNodeByName("r 0",towerrechts.get(0)).size());
        assertEquals(1,SceneNode.findNodeByName("r 1",towerrechts.get(0)).size());
        assertEquals(1,SceneNode.findNodeByName("r 2",towerrechts.get(0)).size());
        assertEquals(0,SceneNode.findNodeByName("r 3",towerrechts.get(0)).size());

        assertEquals(0,SceneNode.findNodeByName("r 0",towerrechts.get(1)).size());
        assertEquals(1,SceneNode.findNodeByName("r 1",towerrechts.get(1)).size());
        assertEquals(1,SceneNode.findNodeByName("r 2",towerrechts.get(1)).size());

    }

}
