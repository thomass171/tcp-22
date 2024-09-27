package de.yard.threed.engine.testutil;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ViewPoint;

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

    public static String getHierarchy(SceneNode node, int maxlevel) {
        String s = node.getName();
        Transform t = node.getTransform();
        if (t.getChildCount() == 0 || maxlevel <= 0) {
            return s;
        }
        if (t.getChildCount() == 1) {
            return s + "->" + getHierarchy(t.getChild(0).getSceneNode(), maxlevel - 1);
        }
        s += "->[";
        for (int i = 0; i < t.getChildCount(); i++) {
            s += ((i > 0) ? "," : "") + getHierarchy(t.getChild(i).getSceneNode(), maxlevel - 1);
        }
        return s + "]";
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


}
