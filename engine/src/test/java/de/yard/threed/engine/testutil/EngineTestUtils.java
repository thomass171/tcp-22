package de.yard.threed.engine.testutil;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.AsyncHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static de.yard.threed.core.testutil.TestUtils.assertTransform;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EngineTestUtils {

    public static void assertViewPoint(String expectedName, LocalTransform expected, ViewPoint actual) {
        assertEquals(expectedName, actual.name);
        assertTransform(expected, actual.transform);
    }

    public static String loadFileFromClasspath(String fileName) throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    public static String getHierarchy(SceneNode node, int maxlevel, boolean down) {
        String s = node.getName();
        Transform t = node.getTransform();
        if (maxlevel <= 0) {
            return s;
        }
        if (down) {
            if (t.getChildCount() == 0) {
                return s;
            }

            if (t.getChildCount() == 1) {
                return s + "->" + getHierarchy(t.getChild(0).getSceneNode(), maxlevel - 1, true);
            }
            s += "->[";
            for (int i = 0; i < t.getChildCount(); i++) {
                s += ((i > 0) ? "," : "") + getHierarchy(t.getChild(i).getSceneNode(), maxlevel - 1, true);
            }
            return s + "]";
        }
        // up: Even with 'up' arrow goes to the right(children)
        if (t.getParent() == null) {
            return s;
        }
        return getHierarchy(t.getParent().getSceneNode(), maxlevel - 1, down) + "->" + s;
    }

    /**
     * Also for grand children.
     */
    public static SceneNode getChild(SceneNode node, Integer... index) {
        for (int i = 0; i < index.length; i++) {
            node = node.getTransform().getChild(index[i]).getSceneNode();
        }
        return node;
    }

    public static void assertSceneNodeLevel(SceneNode sceneNode, String expectedName, String[] expectedChildren) {

        if (expectedName == null) {
            assertNull(sceneNode.getName());
        } else {
            assertEquals(expectedName, sceneNode.getName());
        }

        assertEquals(expectedChildren.length, sceneNode.getTransform().getChildCount());
        for (int i = 0; i < expectedChildren.length; i++) {
            if (expectedChildren[i] == null) {
                assertNull(sceneNode.getTransform().getChild(i).getSceneNode().getName());
            } else {
                assertEquals(expectedChildren[i], sceneNode.getTransform().getChild(i).getSceneNode().getName());
            }
        }
    }

    /**
     * Das, was sonst im Runnerhelper laeuft.
     */
    public static void processAsync() {
        /*13.12.23 not sure whether still needed. But AsyncHelper should trigger BundleLoadDelegate
        AsyncHelper.processAsync(AbstractSceneRunner.getInstance().getBundleLoader());
        List<Pair<BundleLoadDelegate, Bundle>> loadresult = Platform.getInstance().bundleLoader.processAsync();
        AbstractSceneRunner.getInstance().processDelegates(loadresult);*/
        AbstractSceneRunner.getInstance().processDelegates();

        // trigger BundleLoadDelegate.
        AsyncHelper.processAsync();
        // 15.12.23 Those extracted from AsyncHelper
        AbstractSceneRunner.getInstance().processFutures();
        AbstractSceneRunner.getInstance().processInvokeLaters();
    }
}
